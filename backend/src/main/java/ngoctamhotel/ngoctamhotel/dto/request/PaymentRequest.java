package ngoctamhotel.ngoctamhotel.dto.request;

import java.util.UUID;

public record PaymentRequest(
        UUID bookingId,
        String method
) {
}
