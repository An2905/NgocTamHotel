package ngoctamhotel.ngoctamhotel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BikeBookingResponse(
        List<UUID> bookingIds,
        List<String> bikeNames,
        LocalDate startDate,
        LocalDate endDate,
        int numberOfBikes,
        BigDecimal totalPrice,
        String status,
        String fullName,
        String phone
) {}
