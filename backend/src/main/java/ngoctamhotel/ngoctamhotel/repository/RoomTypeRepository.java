package ngoctamhotel.ngoctamhotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ngoctamhotel.ngoctamhotel.model.RoomType;

@Repository
public class RoomTypeRepository {
    private final JdbcTemplate jdbcTemplate;

    public RoomTypeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RoomType> findAll() {
        return jdbcTemplate.query("""
                SELECT id, name, description, base_price, max_adults, max_children,
                       amenities, view_type, image_url, created_at
                FROM room_types
                ORDER BY base_price
                """, (rs, rowNum) -> new RoomType(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("base_price"),
                rs.getInt("max_adults"),
                rs.getInt("max_children"),
                rs.getString("amenities"),
                rs.getString("view_type"),
                rs.getString("image_url"),
                rs.getTimestamp("created_at").toLocalDateTime()));
    }

    public Optional<RoomType> findById(Long id) {
        List<RoomType> types = jdbcTemplate.query("""
                SELECT id, name, description, base_price, max_adults, max_children,
                       amenities, view_type, image_url, created_at
                FROM room_types
                WHERE id = ?
                """, (rs, rowNum) -> new RoomType(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("base_price"),
                rs.getInt("max_adults"),
                rs.getInt("max_children"),
                rs.getString("amenities"),
                rs.getString("view_type"),
                rs.getString("image_url"),
                rs.getTimestamp("created_at").toLocalDateTime()), id);
        return types.stream().findFirst();
    }
}
