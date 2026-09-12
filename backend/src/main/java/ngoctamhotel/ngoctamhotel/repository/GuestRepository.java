package ngoctamhotel.ngoctamhotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ngoctamhotel.ngoctamhotel.model.Guest;

@Repository
public class GuestRepository {
    private final JdbcTemplate jdbcTemplate;

    public GuestRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Guest create(String fullName, String phone, String email, String identityNumber) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO guests (full_name, phone, email, identity_number)
                    VALUES (?, ?, ?, ?)
                    """, fullName, phone, email, identityNumber);
        } catch (DuplicateKeyException exception) {
            // Guest may already exist – find and return
            Optional<Guest> existing = findByEmail(email);
            if (existing.isPresent()) return existing.get();
            throw new IllegalArgumentException("Thông tin khách đã tồn tại");
        }

        List<Guest> guests = jdbcTemplate.query("""
                SELECT id, full_name, phone, email, identity_number, created_at
                FROM guests
                WHERE email = ?
                ORDER BY id DESC LIMIT 1
                """, (rs, rowNum) -> new Guest(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("identity_number"),
                rs.getTimestamp("created_at").toLocalDateTime()), email);
        return guests.get(0);
    }

    public Optional<Guest> findById(Long id) {
        List<Guest> guests = jdbcTemplate.query("""
                SELECT id, full_name, phone, email, identity_number, created_at
                FROM guests
                WHERE id = ?
                """, (rs, rowNum) -> new Guest(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("identity_number"),
                rs.getTimestamp("created_at").toLocalDateTime()), id);
        return guests.stream().findFirst();
    }

    public Optional<Guest> findByEmail(String email) {
        List<Guest> guests = jdbcTemplate.query("""
                SELECT id, full_name, phone, email, identity_number, created_at
                FROM guests
                WHERE email = ?
                """, (rs, rowNum) -> new Guest(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("identity_number"),
                rs.getTimestamp("created_at").toLocalDateTime()), email);
        return guests.stream().findFirst();
    }
}
