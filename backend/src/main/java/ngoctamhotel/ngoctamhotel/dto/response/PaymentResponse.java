package ngoctamhotel.ngoctamhotel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID bookingId,
        String method,
        BigDecimal amount,
        String status,
        String transactionRef,
        LocalDateTime paidAt,
        Long payosOrderCode,
        String payosPaymentLinkId,
        String checkoutUrl) {
}
