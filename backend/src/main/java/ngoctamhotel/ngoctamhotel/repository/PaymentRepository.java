package ngoctamhotel.ngoctamhotel.repository;

import ngoctamhotel.ngoctamhotel.entity.Payment;
import ngoctamhotel.ngoctamhotel.entity.enums.PaymentStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/** Chỉ đọc/ghi SQL. Service quyết định booking nào được phép thanh toán. */
@Repository
public class PaymentRepository {
    private final JdbcTemplate jdbc;
    public PaymentRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean existsBooking(UUID id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM room_bookings WHERE id = ?)", Boolean.class, id.toString()));
    }

    // Khóa cùng booking trong transaction để hai request không tạo hai payment cùng lúc.
    public List<String> lockBooking(UUID id) {
        return jdbc.query("SELECT CASE WHEN room_booking_status = 'PENDING' AND payment_deadline <= NOW() THEN 'EXPIRED' ELSE room_booking_status END FROM room_bookings WHERE id = ? FOR UPDATE",
                (rs, n) -> rs.getString(1), id.toString());
    }

    public List<Payment> findActive(UUID id) {
        return jdbc.query("""
                SELECT * FROM payments WHERE booking_id = ?
                AND payment_status IN ('UNPAID', 'PROCESSING', 'PAID')
                ORDER BY created_at DESC, id DESC
                """, (rs, n) -> new Payment(UUID.fromString(rs.getString("id")), id,
                rs.getObject("payos_order_code", Long.class), rs.getString("payos_payment_link_id"),
                rs.getString("checkout_url"), rs.getBigDecimal("amount"),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("paid_at") == null ? null : rs.getTimestamp("paid_at").toLocalDateTime(),
                rs.getString("method")), id.toString());
    }

    public void createPayment(Payment payment) {
        // Bỏ payos_order_code khỏi INSERT: MySQL AUTO_INCREMENT tự cấp mã duy nhất.
        // UUID id vẫn là khóa chính nội bộ; GeneratedKeyHolder lấy mã số MySQL vừa cấp.
        var keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement("""
                    INSERT INTO payments (id, booking_id, payment_status, method, amount, created_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, payment.getId().toString());
            statement.setString(2, payment.getBookingId().toString());
            statement.setString(3, payment.getStatus().name());
            statement.setString(4, payment.getPaymentMethod());
            statement.setBigDecimal(5, payment.getAmountVnd());
            statement.setTimestamp(6, Timestamp.valueOf(payment.getCreatedAt()));
            return statement;
        }, keys);
        Number code = keys.getKey();
        if (code == null) throw new IllegalStateException("DB không trả payos_order_code");
        payment.setPayosOrderCode(code.longValue());
    }

    public void saveLink(Payment payment) {
        int updated = jdbc.update("""
                UPDATE payments SET payos_payment_link_id = ?, checkout_url = ?, payment_status = 'UNPAID'
                WHERE id = ? AND payment_status = 'PROCESSING'
                """, payment.getPayosPaymentLinkId(), payment.getCheckoutUrl(), payment.getId().toString());
        if (updated != 1) throw new IllegalStateException("Không thể cập nhật link payment");
    }
}
