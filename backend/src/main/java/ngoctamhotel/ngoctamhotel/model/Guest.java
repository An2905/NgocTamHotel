package ngoctamhotel.ngoctamhotel.model;

import java.time.LocalDateTime;

public record Guest(
        Long id,
        String fullName,
        String phone,
        String email,
        String identityNumber,
        LocalDateTime createdAt) {
}
