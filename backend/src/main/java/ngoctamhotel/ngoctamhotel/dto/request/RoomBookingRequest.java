package ngoctamhotel.ngoctamhotel.dto.request;

import java.time.LocalDate;
import java.util.UUID;

// Trước: request chứa totalPrice, createdAt và thông tin payment.
// Sau: request chỉ chứa lựa chọn đặt phòng; service tự tính giá và thời điểm tạo.
public record RoomBookingRequest(
        UUID holdId,
        UUID ownerKey,
        //room info
        UUID categoryId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int adults,
        int children,
        String note,
        int numberOfRooms,

        //customer info
        String fullName,
        String email,
        String phone,
        LocalDate dateOfBirth
) {}
