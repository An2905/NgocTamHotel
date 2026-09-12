package ngoctamhotel.ngoctamhotel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "Thiếu mã booking") Long bookingId,
        @NotBlank(message = "Vui lòng chọn phương thức thanh toán") String method) {
}
