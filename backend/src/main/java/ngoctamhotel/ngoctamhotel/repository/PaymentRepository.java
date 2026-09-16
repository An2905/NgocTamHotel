package ngoctamhotel.ngoctamhotel.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ngoctamhotel.ngoctamhotel.model.Payment;

@Repository
public class PaymentRepository {
    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Payment create(Long bookingId, String method, BigDecimal amount, String transactionRef) {
        jdbcTemplate.update("""
                INSERT INTO payments (booking_id, method, amount, transaction_ref)
                VALUES (?, ?, ?, ?)
                """, bookingId, method, amount, transactionRef);

        return findByBookingId(bookingId).orElseThrow();
    }

    public Optional<Payment> findByBookingId(Long bookingId) {
        List<Payment> payments = jdbcTemplate.query("""
                SELECT id, booking_id, method, amount, status, transaction_ref, paid_at, created_at
                FROM payments
                WHERE booking_id = ?
                ORDER BY id DESC LIMIT 1
                """, (rs, rowNum) -> new Payment(
                rs.getLong("id"),
                rs.getLong("booking_id"),
                rs.getString("method"),
                rs.getBigDecimal("amount"),
                rs.getString("status"),
                rs.getString("transaction_ref"),
                rs.getTimestamp("paid_at") != null ? rs.getTimestamp("paid_at").toLocalDateTime() : null,
                rs.getTimestamp("created_at").toLocalDateTime()), bookingId);
        return payments.stream().findFirst();
    }

    public void updateStatus(Long id, String status) {
        if ("COMPLETED".equals(status)) {
            jdbcTemplate.update("UPDATE payments SET status = ?, paid_at = ? WHERE id = ?",
                    status, LocalDateTime.now(), id);
        } else {
            jdbcTemplate.update("UPDATE payments SET status = ? WHERE id = ?", status, id);
        }
    }
}
