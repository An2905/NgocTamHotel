package ngoctamhotel.ngoctamhotel.service;

import ngoctamhotel.ngoctamhotel.dto.request.PaymentRequest;
import ngoctamhotel.ngoctamhotel.dto.response.PaymentResponse;
import ngoctamhotel.ngoctamhotel.entity.Payment;
import ngoctamhotel.ngoctamhotel.entity.enums.PaymentStatus;
import ngoctamhotel.ngoctamhotel.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Điều phối: controller -> kiểm tra booking -> DB cấp orderCode -> PayOS -> lưu link -> FE.
 * Tạo link chưa phải đã nhận tiền. Chỉ webhook/đối soát đã xác minh mới được ghi PAID. */
@Service
public class PaymentService {
    private static final BigDecimal ROOM_DEPOSIT = new BigDecimal("100000");
    private final PaymentRepository repo;
    private final PayOSService payOS;
    private final TransactionTemplate transaction;

    public PaymentService(PaymentRepository repo, PayOSService payOS, TransactionTemplate transaction) {
        this.repo = repo;
        this.payOS = payOS;
        this.transaction = transaction;
    }

    public PaymentResponse createPayment(PaymentRequest request) {
        if (request == null || request.bookingId() == null || request.method() == null
                || !"PAYOS".equalsIgnoreCase(request.method().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin thanh toán không hợp lệ");
        }
        payOS.checkConfigured();
        // Transaction ngắn: khóa booking, chọn/tạo payment rồi COMMIT trước khi gọi mạng.
        Payment payment = transaction.execute(status -> reservePayment(request.bookingId()));
        if (payment == null) throw new IllegalStateException("Không tạo được payment");
        if (payment.getCheckoutUrl() != null) return response(payment);

        try {
            var link = payOS.createLink(payment);
            if (link == null || link.getPaymentLinkId() == null || link.getCheckoutUrl() == null
                    || !link.getCheckoutUrl().startsWith("https://")) {
                throw new IllegalStateException("PayOS chưa trả link thanh toán hợp lệ");
            }
            payment.setPayosPaymentLinkId(link.getPaymentLinkId());
            payment.setCheckoutUrl(link.getCheckoutUrl());
            transaction.executeWithoutResult(status -> repo.saveLink(payment));
            payment.setStatus(PaymentStatus.UNPAID);
            return response(payment);
        } catch (Exception error) {
            // Có thể PayOS đã tạo link nhưng mạng/DB bị lỗi. Giữ PROCESSING và orderCode
            // để đối soát, không tạo mã mới hoặc tự đánh dấu thất bại rồi thu tiền lần nữa.
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Chưa lấy được link thanh toán; yêu cầu đang chờ đối soát", error);
        }
    }

    private Payment reservePayment(UUID bookingId) {
        var booking = repo.lockBooking(bookingId);
        if (booking.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy booking");
        if (!"PENDING".equals(booking.get(0))) throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Booking không còn ở trạng thái chờ thanh toán");
        var active = repo.findActive(bookingId);
        if (active.size() > 1) throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking cần đối soát các payment cũ");
        if (!active.isEmpty()) {
            Payment existing = active.get(0);
            if (existing.getStatus() == PaymentStatus.UNPAID && existing.getCheckoutUrl() != null) return existing;
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment đã thanh toán hoặc đang chờ đối soát");
        }
        Payment payment = new Payment(UUID.randomUUID(), bookingId, null, null, null,
                ROOM_DEPOSIT, PaymentStatus.PROCESSING, LocalDateTime.now(), null, "PAYOS");
        repo.createPayment(payment);
        return payment;
    }

    public boolean checkBooking(UUID bookingId) {
        return bookingId != null && repo.existsBooking(bookingId);
    }

    private PaymentResponse response(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getBookingId(), payment.getPaymentMethod(),
                payment.getAmountVnd(), payment.getStatus().name(), null, payment.getPaidAt(),
                payment.getPayosOrderCode(), payment.getPayosPaymentLinkId(), payment.getCheckoutUrl());
    }
}
