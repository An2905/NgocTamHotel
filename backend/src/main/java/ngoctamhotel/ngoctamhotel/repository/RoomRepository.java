package ngoctamhotel.ngoctamhotel.repository;

import ngoctamhotel.ngoctamhotel.dto.request.CheckRoomRequest;
import ngoctamhotel.ngoctamhotel.dto.request.RoomBookingRequest;
import ngoctamhotel.ngoctamhotel.entity.Room;
import ngoctamhotel.ngoctamhotel.entity.RoomBooking;
import ngoctamhotel.ngoctamhotel.entity.RoomCategory;
import ngoctamhotel.ngoctamhotel.entity.enums.RoomStatus;
import ngoctamhotel.ngoctamhotel.entity.enums.RoomType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class RoomRepository {
    private final JdbcTemplate jdbcTemplate;

    public RoomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int findRoomByRequest(CheckRoomRequest request) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM rooms r
                JOIN room_categories c ON c.id = r.category_id
                WHERE r.category_id = ?
                  AND c.room_type = ?
                  AND r.room_status NOT IN ('MAINTENANCE', 'INACTIVE')
                  -- Trước: đọc b.room_id; sau: nối booking_rooms để xét từng phòng trong booking.
                  AND NOT EXISTS (
                      SELECT 1
                      FROM booking_rooms br
                      JOIN room_bookings b ON b.id = br.booking_id
                      WHERE br.room_id = r.id
                        AND (b.room_booking_status IN ('CONFIRMED', 'CHECKED_IN')
                          OR (b.room_booking_status = 'PENDING'))
                        AND b.check_in_date < ?
                        AND b.check_out_date > ?
                  )
                  AND NOT EXISTS (
                      SELECT 1 FROM room_hold_rooms hr JOIN room_holds h ON h.id = hr.hold_id
                      WHERE hr.room_id = r.id AND h.status = 'ACTIVE' AND h.expires_at > NOW()
                        AND h.check_in_date < ? AND h.check_out_date > ?
                  )
                """,
                Integer.class,
                request.roomCateId().toString(),
                request.roomType().name(),
                java.sql.Date.valueOf(request.checkOutDay()),
                java.sql.Date.valueOf(request.checkInDay()),
                java.sql.Date.valueOf(request.checkOutDay()),
                java.sql.Date.valueOf(request.checkInDay())
        );
        return count == null ? 0 : count;
    }

    public List<RoomCategory> findAllRoomCategories() {
        return jdbcTemplate.query(
                "SELECT * FROM room_categories",
                (rs, rowNum) -> new RoomCategory(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("category_name"),
                        rs.getBigDecimal("price"),
                        rs.getString("description"),
                        rs.getInt("max_guests"),
                        rs.getString("amenities"),
                        RoomType.valueOf(rs.getString("room_type"))
                )
        );
    }

    public List<Room> findRoomsByRequest(RoomBookingRequest request) {
        return jdbcTemplate.query(
                """
            SELECT r.*
            FROM rooms r
            WHERE r.category_id = ?
              AND r.room_status NOT IN ('MAINTENANCE', 'INACTIVE')
              -- Trước: đọc b.room_id; sau: kiểm tra qua bảng booking_rooms.
              AND NOT EXISTS (
                  SELECT 1
                  FROM booking_rooms br
                  JOIN room_bookings b ON b.id = br.booking_id
                  WHERE br.room_id = r.id
                    AND (b.room_booking_status IN ('CONFIRMED', 'CHECKED_IN')
                      OR (b.room_booking_status = 'PENDING'))
                    AND b.check_in_date < ?
                    AND b.check_out_date > ?
              )
              AND NOT EXISTS (SELECT 1 FROM room_hold_rooms hr JOIN room_holds h ON h.id = hr.hold_id
                  WHERE hr.room_id = r.id AND h.status = 'ACTIVE' AND h.expires_at > NOW()
                  AND h.check_in_date < ? AND h.check_out_date > ?)
            ORDER BY r.room_number
            LIMIT ? FOR UPDATE
            """,
                (rs, rowNum) -> new Room(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("room_number"),
                        RoomStatus.valueOf(rs.getString("room_status")),
                        UUID.fromString(rs.getString("category_id"))
                ),
                request.categoryId().toString(),
                java.sql.Date.valueOf(request.checkOutDate()),
                java.sql.Date.valueOf(request.checkInDate()),
                java.sql.Date.valueOf(request.checkOutDate()),
                java.sql.Date.valueOf(request.checkInDate()),
                request.numberOfRooms()
        );
    }

    public BigDecimal findCategoryPrice(UUID categoryId) {
        return jdbcTemplate.queryForObject(
                "SELECT price FROM room_categories WHERE id = ?",
                BigDecimal.class,
                categoryId.toString()
        );
    }

    public int findCategoryMaxGuests(UUID categoryId) {
        return jdbcTemplate.queryForObject("SELECT max_guests FROM room_categories WHERE id = ?",
                Integer.class, categoryId.toString());
    }

    public void setPaymentDeadline(UUID bookingId, LocalDateTime deadline) {
        jdbcTemplate.update("UPDATE room_bookings SET payment_deadline = ? WHERE id = ?",
                Timestamp.valueOf(deadline), bookingId.toString());
    }

    public RoomBooking save(RoomBooking booking) {
        UUID bookingId = booking.getId() != null
                ? booking.getId()
                : UUID.randomUUID();

        booking.setId(bookingId);
        // Thông tin liên hệ được lưu ngay trong booking, không còn customer_id.
        jdbcTemplate.update("""
            INSERT INTO room_bookings
                (id, check_in_date, check_out_date, adults, children,
                 total_pricce, room_booking_status, created_at, note, full_name, phone, email, date_of_birth, category_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                bookingId.toString(),
                Date.valueOf(booking.getCheckInDate()),
                Date.valueOf(booking.getCheckOutDate()),
                booking.getAdults(),
                booking.getChildren(),
                booking.getTotalPricce(),
                booking.getRoomBookingStatus().name(),
                java.sql.Timestamp.valueOf(booking.getCreatedAt()),
                booking.getNote(),
                booking.getFullName(),
                booking.getPhone(),
                booking.getEmail(),
                booking.getDateOfBirth() == null ? null : Date.valueOf(booking.getDateOfBirth()),
                booking.getRoomCategoryId().toString()
        );

        // Trước: INSERT thêm vào room_bookings với cùng id gây trùng khóa chính.
        // Sau: mỗi phòng được lưu thành một dòng trong booking_rooms.
        for (Room room : booking.getRooms()) {
            jdbcTemplate.update("""
                INSERT INTO booking_rooms (booking_id, room_id)
                VALUES (?, ?)
                """,
                    bookingId.toString(),
                    room.getId().toString()
            );
        }

        return booking;
    }

}
