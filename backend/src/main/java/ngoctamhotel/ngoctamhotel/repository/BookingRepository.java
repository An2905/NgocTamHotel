package ngoctamhotel.ngoctamhotel.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ngoctamhotel.ngoctamhotel.model.Booking;

@Repository
public class BookingRepository {
    private final JdbcTemplate jdbcTemplate;

    public BookingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Booking create(String bookingCode, Long guestId, Long roomId,
            LocalDate checkIn, LocalDate checkOut,
            int adults, int children, BigDecimal totalAmount, String notes) {
        jdbcTemplate.update("""
                INSERT INTO bookings (booking_code, guest_id, room_id,
                    check_in_date, check_out_date, adults, children, total_amount, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, bookingCode, guestId, roomId,
                checkIn, checkOut, adults, children, totalAmount, notes);

        return findByCode(bookingCode).orElseThrow();
    }

    public Optional<Booking> findByCode(String bookingCode) {
        List<Booking> bookings = jdbcTemplate.query("""
                SELECT id, booking_code, guest_id, room_id,
                       check_in_date, check_out_date, adults, children,
                       total_amount, status, notes, created_at, updated_at
                FROM bookings
                WHERE booking_code = ?
                """, (rs, rowNum) -> new Booking(
                rs.getLong("id"),
                rs.getString("booking_code"),
                rs.getLong("guest_id"),
                rs.getLong("room_id"),
                rs.getDate("check_in_date").toLocalDate(),
                rs.getDate("check_out_date").toLocalDate(),
                rs.getInt("adults"),
                rs.getInt("children"),
                rs.getBigDecimal("total_amount"),
                rs.getString("status"),
                rs.getString("notes"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()), bookingCode);
        return bookings.stream().findFirst();
    }

    public Optional<Booking> findById(Long id) {
        List<Booking> bookings = jdbcTemplate.query("""
                SELECT id, booking_code, guest_id, room_id,
                       check_in_date, check_out_date, adults, children,
                       total_amount, status, notes, created_at, updated_at
                FROM bookings
                WHERE id = ?
                """, (rs, rowNum) -> new Booking(
                rs.getLong("id"),
                rs.getString("booking_code"),
                rs.getLong("guest_id"),
                rs.getLong("room_id"),
                rs.getDate("check_in_date").toLocalDate(),
                rs.getDate("check_out_date").toLocalDate(),
                rs.getInt("adults"),
                rs.getInt("children"),
                rs.getBigDecimal("total_amount"),
                rs.getString("status"),
                rs.getString("notes"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()), id);
        return bookings.stream().findFirst();
    }

    public List<Booking> findAll() {
        return jdbcTemplate.query("""
                SELECT id, booking_code, guest_id, room_id,
                       check_in_date, check_out_date, adults, children,
                       total_amount, status, notes, created_at, updated_at
                FROM bookings
                ORDER BY created_at DESC
                """, (rs, rowNum) -> new Booking(
                rs.getLong("id"),
                rs.getString("booking_code"),
                rs.getLong("guest_id"),
                rs.getLong("room_id"),
                rs.getDate("check_in_date").toLocalDate(),
                rs.getDate("check_out_date").toLocalDate(),
                rs.getInt("adults"),
                rs.getInt("children"),
                rs.getBigDecimal("total_amount"),
                rs.getString("status"),
                rs.getString("notes"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()));
    }

    public List<Booking> findByDateRange(LocalDate from, LocalDate to) {
        return jdbcTemplate.query("""
                SELECT id, booking_code, guest_id, room_id,
                       check_in_date, check_out_date, adults, children,
                       total_amount, status, notes, created_at, updated_at
                FROM bookings
                WHERE created_at >= ? AND created_at < ?
                ORDER BY created_at DESC
                """, (rs, rowNum) -> new Booking(
                rs.getLong("id"),
                rs.getString("booking_code"),
                rs.getLong("guest_id"),
                rs.getLong("room_id"),
                rs.getDate("check_in_date").toLocalDate(),
                rs.getDate("check_out_date").toLocalDate(),
                rs.getInt("adults"),
                rs.getInt("children"),
                rs.getBigDecimal("total_amount"),
                rs.getString("status"),
                rs.getString("notes"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()),
                from, to.plusDays(1));
    }

    public void updateStatus(Long id, String status) {
        int rows = jdbcTemplate.update("UPDATE bookings SET status = ? WHERE id = ?", status, id);
        if (rows == 0) throw new IllegalArgumentException("Không tìm thấy đơn đặt phòng");
    }
}
