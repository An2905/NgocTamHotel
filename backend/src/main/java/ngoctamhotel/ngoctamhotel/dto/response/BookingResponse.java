package ngoctamhotel.ngoctamhotel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        String bookingCode,
        String guestName,
        String guestPhone,
        String guestEmail,
        String roomNumber,
        String roomTypeName,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int adults,
        int children,
        BigDecimal totalAmount,
        String status,
        String paymentStatus,
        LocalDateTime createdAt) {
}
