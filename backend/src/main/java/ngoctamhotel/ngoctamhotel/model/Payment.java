package ngoctamhotel.ngoctamhotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
        Long id,
        Long bookingId,
        String method,
        BigDecimal amount,
        String status,
        String transactionRef,
        LocalDateTime paidAt,
        LocalDateTime createdAt) {
}
