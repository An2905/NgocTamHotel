package ngoctamhotel.ngoctamhotel.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateRequest(
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "chỉ được chứa chữ, số, dấu chấm, gạch dưới hoặc gạch ngang")
        String username,
        @Email @Size(max = 255) String email,
        @Pattern(regexp="^$|.{8,72}", message="Password phải chứa từ 8-72 ký tự") String password) {
}
