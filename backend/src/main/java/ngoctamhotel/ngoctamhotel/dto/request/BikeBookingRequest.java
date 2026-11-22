package ngoctamhotel.ngoctamhotel.dto.request;

import java.time.LocalDate;

public record BikeBookingRequest(
        LocalDate startDate,
        LocalDate endDate,
        int numberOfBikes,
        String fullName,
        String phone,
        String email,
        java.util.UUID roomBookingId
) {}
