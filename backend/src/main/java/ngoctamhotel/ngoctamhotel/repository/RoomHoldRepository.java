package ngoctamhotel.ngoctamhotel.repository;

import ngoctamhotel.ngoctamhotel.dto.request.RoomHoldRequest;
import ngoctamhotel.ngoctamhotel.entity.Room;
import ngoctamhotel.ngoctamhotel.entity.enums.RoomStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class RoomHoldRepository {
    private final JdbcTemplate jdbc;
    public RoomHoldRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    // Serialize allocations within one category. Availability checks alone do not prevent races.
    public void lockCategory(UUID categoryId) {
        var rows = jdbc.query("SELECT id FROM room_categories WHERE id = ? FOR UPDATE",
                (rs, n) -> rs.getString(1), categoryId.toString());
        if (rows.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Không tìm thấy loại phòng");
    }

    public List<Room> selectAvailable(RoomHoldRequest request) {
        return jdbc.query("""
                SELECT r.* FROM rooms r WHERE r.category_id = ?
                AND r.room_status NOT IN ('MAINTENANCE', 'INACTIVE')
                AND NOT EXISTS (SELECT 1 FROM booking_rooms br
                  JOIN room_bookings b ON b.id = br.booking_id
                  WHERE br.room_id = r.id
                  AND (b.room_booking_status IN ('CONFIRMED', 'CHECKED_IN')
                    OR (b.room_booking_status = 'PENDING'))
                  AND b.check_in_date < ? AND b.check_out_date > ?)
                AND NOT EXISTS (SELECT 1 FROM room_hold_rooms hr
                  JOIN room_holds h ON h.id = hr.hold_id
                  WHERE hr.room_id = r.id AND h.status = 'ACTIVE' AND h.expires_at > NOW()
                  AND h.check_in_date < ? AND h.check_out_date > ?)
                ORDER BY r.room_number LIMIT ?
                """, (rs, n) -> new Room(UUID.fromString(rs.getString("id")),
                        rs.getString("room_number"), RoomStatus.valueOf(rs.getString("room_status")),
                        UUID.fromString(rs.getString("category_id"))),
                request.categoryId().toString(), Date.valueOf(request.checkOutDate()),
                Date.valueOf(request.checkInDate()), Date.valueOf(request.checkOutDate()),
                Date.valueOf(request.checkInDate()), request.numberOfRooms());
    }

    public void save(UUID id, RoomHoldRequest request, LocalDateTime expiresAt, List<Room> rooms) {
        jdbc.update("INSERT INTO room_holds (id, category_id, check_in_date, check_out_date, number_of_rooms, owner_key, expires_at, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')",
                id.toString(), request.categoryId().toString(), Date.valueOf(request.checkInDate()),
                Date.valueOf(request.checkOutDate()), request.numberOfRooms(), request.ownerKey().toString(), Timestamp.valueOf(expiresAt));
        for (Room room : rooms) jdbc.update("INSERT INTO room_hold_rooms (hold_id, room_id) VALUES (?, ?)",
                id.toString(), room.getId().toString());
    }

    public Hold findForUpdate(UUID id) {
        var rows = jdbc.query("SELECT * FROM room_holds WHERE id = ? FOR UPDATE", (rs, n) ->
                new Hold(UUID.fromString(rs.getString("category_id")),
                        rs.getDate("check_in_date").toLocalDate(), rs.getDate("check_out_date").toLocalDate(),
                        rs.getInt("number_of_rooms"), rs.getTimestamp("expires_at").toLocalDateTime(),
                        rs.getString("status"), UUID.fromString(rs.getString("owner_key"))), id.toString());
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<Room> rooms(UUID id) {
        return jdbc.query("SELECT r.* FROM room_hold_rooms hr JOIN rooms r ON r.id = hr.room_id WHERE hr.hold_id = ?",
                (rs, n) -> new Room(UUID.fromString(rs.getString("id")), rs.getString("room_number"),
                        RoomStatus.valueOf(rs.getString("room_status")), UUID.fromString(rs.getString("category_id"))),
                id.toString());
    }

    public void changeStatus(UUID id, String from, String to) {
        int changed = jdbc.update("UPDATE room_holds SET status = ? WHERE id = ? AND status = ?",
                to, id.toString(), from);
        if (changed != 1) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT, "Lượt giữ phòng không còn hiệu lực");
    }

    public record Hold(UUID categoryId, LocalDate checkInDate, LocalDate checkOutDate,
                       int numberOfRooms, LocalDateTime expiresAt, String status, UUID ownerKey) {}
}
