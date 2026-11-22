package ngoctamhotel.ngoctamhotel.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BikeRepositoryTest {
    private BikeRepository repository;
    private LocalDate start;
    private LocalDate end;

    @BeforeEach
    void setUp() {
        var dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:bikes_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        var jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE bikes (id VARCHAR(36) PRIMARY KEY, name VARCHAR(100), bike_status VARCHAR(30), price DECIMAL(12,2))");
        jdbc.execute("CREATE TABLE bike_bookings (id VARCHAR(36) PRIMARY KEY, bike_booking_status VARCHAR(30), created_at DATE, start_date DATE, end_date DATE, bike_id VARCHAR(36), full_name VARCHAR(150), phone VARCHAR(30), email VARCHAR(255), room_booking_id VARCHAR(36))");
        jdbc.update("INSERT INTO bikes VALUES (?, 'Xe 01', 'AVAILABLE', 100000)", UUID.randomUUID().toString());
        repository = new BikeRepository(jdbc);
        start = LocalDate.now().plusDays(1);
        end = start.plusDays(2);
    }

    @Test
    void overlappingBookingRemovesBikeFromAvailability() {
        assertEquals(1, repository.countAvailable(start, end));
        var bike = repository.findAvailableForUpdate(start, end, 1).get(0);
        repository.saveBooking(UUID.randomUUID(), bike.getId(), "Nguyễn Văn A", "0901234567", null, start, end, null);

        assertEquals(0, repository.countAvailable(start.plusDays(1), end.plusDays(1)));
        assertEquals(0, repository.findAvailableForUpdate(start, end, 1).size());
        assertEquals(1, repository.countAvailable(end.plusDays(1), end.plusDays(2)));
    }
}
