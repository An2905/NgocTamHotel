package ngoctamhotel.ngoctamhotel.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record User(
        UUID id,
        String username,
        String passwordHash,
        String email,
        String role,
        LocalDateTime createdAt) {
}
