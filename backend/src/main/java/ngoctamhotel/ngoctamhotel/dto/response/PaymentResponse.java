package ngoctamhotel.ngoctamhotel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long bookingId,
        String bookingCode,
        String method,
        BigDecimal amount,
        String status,
        String transactionRef,
        LocalDateTime paidAt) {
}
