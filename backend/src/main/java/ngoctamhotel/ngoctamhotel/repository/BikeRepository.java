package ngoctamhotel.ngoctamhotel.repository;

import ngoctamhotel.ngoctamhotel.entity.Bike;
import ngoctamhotel.ngoctamhotel.entity.enums.BikeStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class BikeRepository {
    private final JdbcTemplate jdbcTemplate;

    public BikeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Bike> findAll() {
        return jdbcTemplate.query("SELECT id, name, bike_status, price FROM bikes ORDER BY name",
                (rs, rowNum) -> new Bike(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        BikeStatus.valueOf(rs.getString("bike_status")),
                        rs.getBigDecimal("price")
                ));
    }

    public int countAvailable(LocalDate startDate, LocalDate endDate) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM bikes bike
                WHERE bike.bike_status = 'AVAILABLE'
                  AND NOT EXISTS (
                    SELECT 1 FROM bike_bookings booking
                    WHERE booking.bike_id = bike.id
                      AND booking.bike_booking_status = 'IN_PROGRESS'
                      AND booking.start_date <= ? AND booking.end_date >= ?
                  )
                """, Integer.class, Date.valueOf(endDate), Date.valueOf(startDate));
        return count == null ? 0 : count;
    }

    public List<Bike> findAvailableForUpdate(LocalDate startDate, LocalDate endDate, int quantity) {
        return jdbcTemplate.query("""
                SELECT bike.id, bike.name, bike.bike_status, bike.price FROM bikes bike
                WHERE bike.bike_status = 'AVAILABLE'
                  AND NOT EXISTS (
                    SELECT 1 FROM bike_bookings booking
                    WHERE booking.bike_id = bike.id
                      AND booking.bike_booking_status = 'IN_PROGRESS'
                      AND booking.start_date <= ? AND booking.end_date >= ?
                  )
                ORDER BY bike.name
                LIMIT ? FOR UPDATE
                """, (rs, rowNum) -> new Bike(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        BikeStatus.valueOf(rs.getString("bike_status")),
                        rs.getBigDecimal("price")
                ), Date.valueOf(endDate), Date.valueOf(startDate), quantity);
    }

    public void saveBooking(UUID bookingId, UUID bikeId, String fullName, String phone, String email,
                            LocalDate startDate, LocalDate endDate, UUID roomBookingId) {
        jdbcTemplate.update("""
                INSERT INTO bike_bookings
                    (id, bike_booking_status, created_at, start_date, end_date, bike_id, full_name, phone, email, room_booking_id)
                VALUES (?, 'IN_PROGRESS', ?, ?, ?, ?, ?, ?, ?, ?)
                """, bookingId.toString(), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()),
                Date.valueOf(startDate), Date.valueOf(endDate), bikeId.toString(), fullName, phone, email, roomBookingId == null ? null : roomBookingId.toString());
    }

}
