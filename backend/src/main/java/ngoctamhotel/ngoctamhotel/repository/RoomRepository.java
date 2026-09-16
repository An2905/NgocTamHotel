package ngoctamhotel.ngoctamhotel.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ngoctamhotel.ngoctamhotel.model.Room;

@Repository
public class RoomRepository {
    private final JdbcTemplate jdbcTemplate;

    public RoomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Room> findByRoomTypeId(Long roomTypeId) {
        return jdbcTemplate.query("""
                SELECT id, room_number, room_type_id, floor_number, status, created_at
                FROM rooms
                WHERE room_type_id = ?
                ORDER BY room_number
                """, (rs, rowNum) -> new Room(
                rs.getLong("id"),
                rs.getString("room_number"),
                rs.getLong("room_type_id"),
                rs.getInt("floor_number"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()), roomTypeId);
    }

    public int countByRoomTypeId(Long roomTypeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM rooms WHERE room_type_id = ?", Integer.class, roomTypeId);
        return count != null ? count : 0;
    }

    public int countAvailableByRoomTypeId(Long roomTypeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM rooms WHERE room_type_id = ? AND status = 'AVAILABLE'",
                Integer.class, roomTypeId);
        return count != null ? count : 0;
    }

    public List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut, Long roomTypeId) {
        return jdbcTemplate.query("""
                SELECT r.id, r.room_number, r.room_type_id, r.floor_number, r.status, r.created_at
                FROM rooms r
                WHERE r.room_type_id = ?
                  AND r.status = 'AVAILABLE'
                  AND r.id NOT IN (
                      SELECT b.room_id FROM bookings b
                      WHERE b.status NOT IN ('CANCELLED', 'CHECKED_OUT')
                        AND b.check_in_date < ? AND b.check_out_date > ?
                  )
                ORDER BY r.room_number
                LIMIT 1
                """, (rs, rowNum) -> new Room(
                rs.getLong("id"),
                rs.getString("room_number"),
                rs.getLong("room_type_id"),
                rs.getInt("floor_number"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()),
                roomTypeId, checkOut, checkIn);
    }

    public Optional<Room> findById(Long id) {
        List<Room> rooms = jdbcTemplate.query("""
                SELECT id, room_number, room_type_id, floor_number, status, created_at
                FROM rooms
                WHERE id = ?
                """, (rs, rowNum) -> new Room(
                rs.getLong("id"),
                rs.getString("room_number"),
                rs.getLong("room_type_id"),
                rs.getInt("floor_number"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()), id);
        return rooms.stream().findFirst();
    }

    public List<Room> findAll() {
        return jdbcTemplate.query("""
                SELECT id, room_number, room_type_id, floor_number, status, created_at
                FROM rooms
                ORDER BY room_number
                """, (rs, rowNum) -> new Room(
                rs.getLong("id"),
                rs.getString("room_number"),
                rs.getLong("room_type_id"),
                rs.getInt("floor_number"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()));
    }
}
