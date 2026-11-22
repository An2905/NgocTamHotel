package ngoctamhotel.ngoctamhotel.service;

import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import java.util.*;
import java.time.LocalDateTime;

@Service
@EnableScheduling
public class PaymentLifecycleService {
    private final JdbcTemplate jdbc;
    private final PayOSService gateway;
    private final TransactionTemplate tx;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentLifecycleService.class);

    public PaymentLifecycleService(JdbcTemplate jdbc, PayOSService gateway, TransactionTemplate tx) {
        this.jdbc = jdbc; this.gateway = gateway; this.tx = tx;
    }

    public long deadline(UUID booking) {
        var time = jdbc.queryForObject("SELECT payment_deadline FROM room_bookings WHERE id = ?",
                java.sql.Timestamp.class, booking.toString());
        if (time == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking chưa có hạn thanh toán");
        return time.toInstant().getEpochSecond();
    }

    // GET is read-only: polling never turns browser parameters into payment evidence.
    public Map<String,Object> status(UUID booking) {
        var rows = jdbc.queryForList("SELECT room_booking_status, payment_deadline FROM room_bookings WHERE id = ?", booking.toString());
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy booking");
        var row = rows.get(0);
        var states = jdbc.queryForList("SELECT payment_status FROM payments WHERE booking_id = ?", String.class, booking.toString());
        boolean paid = states.contains("PAID");
        boolean waiting = states.contains("PROCESSING");
        String bookingStatus = row.get("room_booking_status").toString();
        var deadline = (java.sql.Timestamp) row.get("payment_deadline");
        boolean overdue = deadline != null && !deadline.toLocalDateTime().isAfter(LocalDateTime.now());
        return Map.of("bookingStatus", bookingStatus, "paymentStatus", paid ? "PAID" : waiting ? "PROCESSING" : "UNPAID",
                "canPay", bookingStatus.equals("PENDING") && !overdue && !paid && !waiting,
                "needsReview", (paid && !bookingStatus.equals("CONFIRMED")) || (overdue && bookingStatus.equals("PENDING")));
    }

    public void webhook(Webhook webhook) {
        final vn.payos.model.webhooks.WebhookData data;
        try { data = gateway.verify(webhook); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Webhook không hợp lệ"); }
        // Unknown verified events include the gateway's webhook registration probe.
        var bookings = jdbc.queryForList("SELECT booking_id FROM payments WHERE payos_order_code = ?", String.class, data.getOrderCode());
        if (bookings.isEmpty()) return;
        // Fetch authoritative aggregate payment state: handles partial/multiple transfers.
        reconcile(UUID.fromString(bookings.get(0)), data.getOrderCode());
    }

    public void reconcile(UUID booking) {
        reconcile(booking, null);
    }

    private void reconcile(UUID booking, Long eventOrderCode) {
        tx.executeWithoutResult(transaction -> {
            var bookings = jdbc.queryForList("SELECT room_booking_status, payment_deadline FROM room_bookings WHERE id = ? FOR UPDATE", booking.toString());
            if (bookings.isEmpty()) return;
            var b = bookings.get(0);
            String state = b.get("room_booking_status").toString();
            var deadline = (java.sql.Timestamp)b.get("payment_deadline");
            boolean expired = deadline != null && !deadline.toLocalDateTime().isAfter(LocalDateTime.now());
            var payments = jdbc.queryForList("SELECT * FROM payments WHERE booking_id = ? ORDER BY created_at", booking.toString());
            boolean anyPaid = false;
            boolean allClosed = true;
            for (var p : payments) {
                String local = p.get("payment_status").toString();
                if (local.equals("PAID")) { anyPaid = true; continue; }
                Long code = ((Number)p.get("payos_order_code")).longValue();
                if (local.equals("CANCELLED") && !code.equals(eventOrderCode) && !expired) continue;
                PaymentLink link = gateway.lookup(code);
                validate(p, link, code);
                if (expired || p.get("checkout_url") == null) {
                    if (!link.getStatus().name().equals("PAID") && !link.getStatus().name().equals("CANCELLED") &&
                            !link.getStatus().name().equals("EXPIRED")) {
                        link = gateway.cancel(code);
                        validate(p, link, code);
                    }
                }
                long amountPaid = link.getAmountPaid() == null ? 0 : link.getAmountPaid();
                String remote = link.getStatus().name();
                if (remote.equals("PAID") && amountPaid == ((Number)p.get("amount")).longValue()) {
                    jdbc.update("UPDATE payments SET payment_status = 'PAID', paid_at = COALESCE(paid_at, CURRENT_TIMESTAMP), payos_payment_link_id = ? WHERE id = ?",
                            link.getId(), p.get("id"));
                    anyPaid = true;
                } else if ((remote.equals("CANCELLED") || remote.equals("EXPIRED")) && amountPaid == 0) {
                    jdbc.update("UPDATE payments SET payment_status = 'CANCELLED' WHERE id = ? AND payment_status <> 'PAID'", p.get("id"));
                } else {
                    allClosed = false;
                    if (amountPaid > 0) log.warn("Payment needs manual reconciliation: orderCode={}", code);
                }
            }
            if (anyPaid && state.equals("PENDING")) {
                jdbc.update("UPDATE room_bookings SET room_booking_status = 'CONFIRMED' WHERE id = ? AND room_booking_status = 'PENDING'", booking.toString());
            } else if (anyPaid && state.equals("CANCELLED")) {
                log.error("Late payment after room release; manual refund review required: booking={}", booking);
            } else if (expired && allClosed && state.equals("PENDING")) {
                jdbc.update("UPDATE room_bookings SET room_booking_status = 'CANCELLED' WHERE id = ? AND room_booking_status = 'PENDING'", booking.toString());
            }
        });
    }

    private void validate(Map<String,Object> p, PaymentLink link, Long code) {
        if (link == null || !code.equals(link.getOrderCode()) || link.getAmount() == null ||
                link.getAmount() != ((Number)p.get("amount")).longValue() ||
                (p.get("payos_payment_link_id") != null && !p.get("payos_payment_link_id").equals(link.getId()))) {
            throw new IllegalStateException("PayOS payment identity or amount mismatch");
        }
    }

    @Scheduled(fixedDelayString = "${payment.reconcile-delay-ms:60000}")
    public void reconcilePending() {
        for (String id : jdbc.queryForList("SELECT id FROM room_bookings WHERE room_booking_status = 'PENDING'", String.class)) {
            try { reconcile(UUID.fromString(id)); }
            catch (Exception e) { log.warn("Reconciliation pending for booking {}: {}", id, e.getClass().getSimpleName()); }
        }
    }
}
