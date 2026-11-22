package ngoctamhotel.ngoctamhotel.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JwtServiceTest {
    @Test
    void createsAndValidatesToken() {
        JwtService service = new JwtService("test-secret-with-at-least-32-characters", 3600);
        String token = service.createAdminToken("admin");
        assertEquals("admin", service.getValidUsername(token));
    }

    @Test
    void rejectsExpiredToken() {
        JwtService service = new JwtService("test-secret-with-at-least-32-characters", -1);
        assertThrows(IllegalArgumentException.class,
                () -> service.getValidUsername(service.createAdminToken("admin")));
    }
}
