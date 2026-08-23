package ngoctamhotel.ngoctamhotel.dto.response;

import java.util.UUID;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        UUID userId,
        String username,
        String email) {
}

