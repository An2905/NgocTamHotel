package ngoctamhotel.ngoctamhotel.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactRequest(
        @NotBlank(message = "Vui lòng nhập họ tên") String fullName,
        @NotBlank(message = "Vui lòng nhập email") @Email(message = "Email không hợp lệ") String email,
        String phone,
        String subject,
        @NotBlank(message = "Vui lòng nhập nội dung tin nhắn") String message) {
}
