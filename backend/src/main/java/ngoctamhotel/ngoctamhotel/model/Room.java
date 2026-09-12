package ngoctamhotel.ngoctamhotel.model;

import java.time.LocalDateTime;

public record Room(
        Long id,
        String roomNumber,
        Long roomTypeId,
        int floorNumber,
        String status,
        LocalDateTime createdAt) {
}
