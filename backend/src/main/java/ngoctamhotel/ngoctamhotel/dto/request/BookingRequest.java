package ngoctamhotel.ngoctamhotel.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequest(
        @NotNull(message = "Vui lòng chọn loại phòng") Long roomTypeId,
        @NotNull(message = "Vui lòng chọn ngày nhận phòng") LocalDate checkInDate,
        @NotNull(message = "Vui lòng chọn ngày trả phòng") LocalDate checkOutDate,
        int adults,
        int children,
        @NotBlank(message = "Vui lòng nhập họ tên") String fullName,
        @NotBlank(message = "Vui lòng nhập số điện thoại") String phone,
        @Email(message = "Email không hợp lệ") String email,
        String identityNumber,
        String notes) {
}
