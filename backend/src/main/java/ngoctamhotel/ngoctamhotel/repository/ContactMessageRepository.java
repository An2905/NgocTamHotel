package ngoctamhotel.ngoctamhotel.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ngoctamhotel.ngoctamhotel.model.ContactMessage;

@Repository
public class ContactMessageRepository {
    private final JdbcTemplate jdbcTemplate;

    public ContactMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ContactMessage create(String fullName, String email, String phone, String subject, String message) {
        jdbcTemplate.update("""
                INSERT INTO contact_messages (full_name, email, phone, subject, message)
                VALUES (?, ?, ?, ?, ?)
                """, fullName, email, phone, subject, message);

        return jdbcTemplate.queryForObject("""
                SELECT id, full_name, email, phone, subject, message, created_at
                FROM contact_messages
                ORDER BY id DESC LIMIT 1
                """, (rs, rowNum) -> new ContactMessage(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("subject"),
                rs.getString("message"),
                rs.getTimestamp("created_at").toLocalDateTime()));
    }
}
