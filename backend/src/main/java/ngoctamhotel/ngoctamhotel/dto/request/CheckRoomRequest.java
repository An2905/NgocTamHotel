package ngoctamhotel.ngoctamhotel.dto.request;

import ngoctamhotel.ngoctamhotel.entity.enums.RoomType;

import java.time.LocalDate;
import java.util.UUID;

public record CheckRoomRequest(
        LocalDate checkInDay,
        LocalDate checkOutDay,
        RoomType roomType,
        UUID roomCateId
) {
}
