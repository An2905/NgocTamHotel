package ngoctamhotel.ngoctamhotel.dto.response;

import java.math.BigDecimal;

public record RevenueResponse(
        String period,
        BigDecimal roomRevenue,
        BigDecimal totalRevenue,
        int totalBookings,
        int completedBookings) {
}
