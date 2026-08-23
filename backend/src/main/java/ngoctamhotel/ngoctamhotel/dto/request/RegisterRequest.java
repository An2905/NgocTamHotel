package ngoctamhotel.ngoctamhotel.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "chỉ được chứa chữ, số, dấu chấm, gạch dưới hoặc gạch ngang")
        String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Email @Size(max = 255) String email) {
}

