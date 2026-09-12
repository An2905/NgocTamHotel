package ngoctamhotel.ngoctamhotel.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RoomTypeResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        int maxAdults,
        int maxChildren,
        List<String> amenities,
        String viewType,
        String imageUrl,
        int totalRooms,
        int availableRooms) {
}
