package ngoctamhotel.ngoctamhotel.repository;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;

import ngoctamhotel.ngoctamhotel.entity.Admin;
import org.springframework.stereotype.Repository;

@Repository
public class AdminRepository {
    public final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Admin> findByName(String name) {
        return jdbcTemplate.query("SELECT name, password FROM admin WHERE name = ?",
                (rs, rowNum) -> new Admin(rs.getString("name"), rs.getString("password")), name)
                .stream().findFirst();
    }

    public void create(String name, String encodedPassword) {
        jdbcTemplate.update("INSERT INTO admin (name, password) VALUES (?, ?)", name, encodedPassword);
    }
}
