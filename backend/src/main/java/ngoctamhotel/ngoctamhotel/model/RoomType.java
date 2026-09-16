package ngoctamhotel.ngoctamhotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RoomType(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        int maxAdults,
        int maxChildren,
        String amenities,
        String viewType,
        String imageUrl,
        LocalDateTime createdAt) {
}
