package ngoctamhotel.ngoctamhotel.model;

import java.time.LocalDateTime;

public record ContactMessage(
        Long id,
        String fullName,
        String email,
        String phone,
        String subject,
        String message,
        LocalDateTime createdAt) {
}
