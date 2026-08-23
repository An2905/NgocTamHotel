package ngoctamhotel.ngoctamhotel.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class JwtServiceTest {
    @Test
    void createsAndValidatesToken() {
        JwtService service = new JwtService("test-secret-with-at-least-32-characters", 3600);
        String token = service.createToken(UUID.randomUUID(), "admin");
        assertEquals("admin", service.getValidUsername(token));
    }
}

