package ngoctamhotel.ngoctamhotel.service;

import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import vn.payos.model.v2.paymentRequests.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentLifecycleServiceTest {
    JdbcTemplate jdbc;
    PayOSService gateway;
    PaymentLifecycleService service;
    UUID booking;
    @BeforeEach void setup() {
        var ds = new DriverManagerDataSource("jdbc:h2:mem:pay_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1","sa","");
        jdbc = new JdbcTemplate(ds); gateway = mock(PayOSService.class);
        service = new PaymentLifecycleService(jdbc, gateway, new TransactionTemplate(new DataSourceTransactionManager(ds)));
        jdbc.execute("CREATE TABLE room_bookings(id VARCHAR(36) PRIMARY KEY, room_booking_status VARCHAR(30), payment_deadline TIMESTAMP)");
        jdbc.execute("CREATE TABLE payments(id VARCHAR(36) PRIMARY KEY, booking_id VARCHAR(36), payment_status VARCHAR(30), amount DECIMAL(12,2), payos_order_code BIGINT, payos_payment_link_id VARCHAR(100), checkout_url VARCHAR(200), paid_at TIMESTAMP, created_at TIMESTAMP)");
        booking = UUID.randomUUID();
        jdbc.update("INSERT INTO room_bookings VALUES (?, 'PENDING', DATEADD('HOUR',1,CURRENT_TIMESTAMP))",booking.toString());
        jdbc.update("INSERT INTO payments VALUES (?, ?, 'UNPAID', 100000, 12, 'link', 'https://example.test', NULL, CURRENT_TIMESTAMP)",UUID.randomUUID().toString(),booking.toString());
    }
    PaymentLink link(PaymentLinkStatus status, long paid) {
        return PaymentLink.builder().id("link").orderCode(12L).amount(100000L).amountPaid(paid)
                .amountRemaining(100000L-paid).createdAt("2026-11-22T10:00:00Z")
                .transactions(java.util.List.of()).status(status).build();
    }
    String state() { return jdbc.queryForObject("SELECT room_booking_status FROM room_bookings",String.class); }
    void expire() { jdbc.update("UPDATE room_bookings SET payment_deadline = DATEADD('HOUR',-1,CURRENT_TIMESTAMP)"); }
    @Test void paidConfirmsOnce() {
        when(gateway.lookup(12L)).thenReturn(link(PaymentLinkStatus.PAID,100000));
        service.reconcile(booking); service.reconcile(booking);
        assertEquals("CONFIRMED",state());
        assertEquals("PAID",jdbc.queryForObject("SELECT payment_status FROM payments",String.class));
        verify(gateway,times(1)).lookup(12L);
    }
    @Test void gatewayFailureDoesNotReleaseExpiredRooms() {
        expire();when(gateway.lookup(12L)).thenThrow(new RuntimeException("network"));
        assertThrows(RuntimeException.class,()->service.reconcile(booking));
        assertEquals("PENDING",state());
    }
    @Test void confirmedUnpaidExpiredLinkReleasesRooms() {
        expire();when(gateway.lookup(12L)).thenReturn(link(PaymentLinkStatus.CANCELLED,0));
        service.reconcile(booking);assertEquals("CANCELLED",state());
    }
    @Test void partialPaymentRequiresReviewAndKeepsRooms() {
        expire();when(gateway.lookup(12L)).thenReturn(link(PaymentLinkStatus.CANCELLED,50000));
        service.reconcile(booking);assertEquals("PENDING",state());
    }
    @Test void mismatchedAmountCannotConfirmBooking() {
        var result=link(PaymentLinkStatus.PAID,100000);result.setAmount(200000L);
        when(gateway.lookup(12L)).thenReturn(result);
        assertThrows(IllegalStateException.class,()->service.reconcile(booking));
        assertEquals("PENDING",state());
    }

    @Test void paymentWinsRaceWithDeadlineCancellation() {
        expire();
        when(gateway.lookup(12L)).thenReturn(link(PaymentLinkStatus.PENDING,0));
        when(gateway.cancel(12L)).thenReturn(link(PaymentLinkStatus.PAID,100000));
        service.reconcile(booking);
        assertEquals("CONFIRMED",state());
    }

    @Test void lateVerifiedPaymentDoesNotReallocateReleasedRoom() {
        jdbc.update("UPDATE room_bookings SET room_booking_status = 'CANCELLED'");
        jdbc.update("UPDATE payments SET payment_status = 'CANCELLED'");
        var payload = new vn.payos.model.webhooks.Webhook();
        var data = new vn.payos.model.webhooks.WebhookData();
        data.setOrderCode(12L);
        when(gateway.verify(payload)).thenReturn(data);
        when(gateway.lookup(12L)).thenReturn(link(PaymentLinkStatus.PAID,100000));
        service.webhook(payload);
        assertEquals("CANCELLED",state());
        assertEquals("PAID",jdbc.queryForObject("SELECT payment_status FROM payments",String.class));
        assertEquals(true,service.status(booking).get("needsReview"));
    }
}
