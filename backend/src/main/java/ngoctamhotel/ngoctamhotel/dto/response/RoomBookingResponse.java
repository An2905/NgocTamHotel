package ngoctamhotel.ngoctamhotel.dto.response;

import ngoctamhotel.ngoctamhotel.entity.enums.RoomBookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

public record RoomBookingResponse(
        UUID roomBookingId,
        //room info
        UUID categoryId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int adults,
        int children,
        // Trước: FE phải nhớ số phòng từ form; sau: trả số phòng thực sự đã lưu.
        int numberOfRooms,
        BigDecimal totalPrice,
        RoomBookingStatus status,
        LocalDateTime paymentDeadline,
        long paymentDeadlineEpochMillis,

        //customer info
        String fullName,
        String email,
        String phone,
        LocalDate dateOfBirth

        //payment info

) {}
