package ngoctamhotel.ngoctamhotel.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ngoctamhotel.ngoctamhotel.model.User;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByUsername(String username) {
        List<User> users = jdbcTemplate.query("""
                SELECT id, username, password_hash, email, role, created_at
                FROM users
                WHERE username = ?
                """, (rs, rowNum) -> new User(
                UUID.fromString(rs.getString("id")),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime() : null),
                username);
        return users.stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        List<User> users = jdbcTemplate.query("""
                SELECT id, username, password_hash, email, role, created_at
                FROM users
                WHERE email = ?
                """, (rs, rowNum) -> new User(
                UUID.fromString(rs.getString("id")),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime() : null),
                email);
        return users.stream().findFirst();
    }

    public User create(String username, String passwordHash, String email) {
        UUID id = UUID.randomUUID();
        try {
            jdbcTemplate.update("""
                    INSERT INTO users (id, username, password_hash, email)
                    VALUES (?, ?, ?, ?)
                    """, id.toString(), username, passwordHash, email);
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("Username hoặc email đã tồn tại");
        }

        return jdbcTemplate.queryForObject("""
                SELECT id, username, password_hash, email, role, created_at
                FROM users
                WHERE id = ?
                """, (rs, rowNum) -> new User(
                UUID.fromString(rs.getString("id")),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime() : null), id.toString());
    }

    public User update(UUID id, String username, String email, String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("password_hash kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng");
        }
        try {
            int rowsAffected = jdbcTemplate.update("""
                    UPDATE users
                    SET username = ?, email = ?, password_hash = ?
                    WHERE id = ?
                    """, username, email, passwordHash, id.toString());

            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Kh\u00f4ng t\u00ecm th\u1ea5y user");
            }
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("Username ho\u1eb7c email \u0111\u00e3 t\u1ed3n t\u1ea1i");
        }

        return jdbcTemplate.queryForObject("""
                SELECT id, username, password_hash, email, role, created_at
                FROM users
                WHERE id = ?
                """, (rs, rowNum) -> new User(
                UUID.fromString(rs.getString("id")),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime() : null), id.toString());
    }
}
