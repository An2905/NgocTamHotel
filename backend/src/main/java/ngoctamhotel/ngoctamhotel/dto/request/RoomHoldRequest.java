package ngoctamhotel.ngoctamhotel.dto.request;

import java.time.LocalDate;
import java.util.UUID;

public record RoomHoldRequest(UUID categoryId, LocalDate checkInDate,
                              LocalDate checkOutDate, int numberOfRooms, UUID ownerKey) {}
