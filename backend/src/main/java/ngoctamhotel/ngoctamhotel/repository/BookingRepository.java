package ngoctamhotel.ngoctamhotel.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ngoctamhotel.ngoctamhotel.dto.response.BookingResponse;
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

    // Fix N+1: 1 query JOIN thay vì 4 queries/booking
    public List<BookingResponse> findAllEnriched() {
        return jdbcTemplate.query("""
                SELECT b.id, b.booking_code,
                       g.full_name, g.phone, g.email,
                       r.room_number, rt.name AS room_type_name,
                       b.check_in_date, b.check_out_date, b.adults, b.children,
                       b.total_amount, b.status,
                       COALESCE(p.status, 'UNPAID') AS payment_status,
                       b.created_at
                FROM bookings b
                JOIN guests g ON g.id = b.guest_id
                JOIN rooms r ON r.id = b.room_id
                JOIN room_types rt ON rt.id = r.room_type_id
                LEFT JOIN payments p ON p.booking_id = b.id
                ORDER BY b.created_at DESC
                """, (rs, rowNum) -> new BookingResponse(
                rs.getLong("id"),
                rs.getString("booking_code"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("room_number"),
                rs.getString("room_type_name"),
                rs.getDate("check_in_date").toLocalDate(),
                rs.getDate("check_out_date").toLocalDate(),
                rs.getInt("adults"),
                rs.getInt("children"),
                rs.getBigDecimal("total_amount"),
                rs.getString("status"),
                rs.getString("payment_status"),
                rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime() : null));
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
