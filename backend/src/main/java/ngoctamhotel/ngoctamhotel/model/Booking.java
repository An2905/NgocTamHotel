package ngoctamhotel.ngoctamhotel.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Booking(
        Long id,
        String bookingCode,
        Long guestId,
        Long roomId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int adults,
        int children,
        BigDecimal totalAmount,
        String status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
