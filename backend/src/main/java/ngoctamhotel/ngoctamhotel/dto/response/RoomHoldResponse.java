package ngoctamhotel.ngoctamhotel.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoomHoldResponse(UUID holdId, LocalDateTime expiresAt,
                               long expiresAtEpochMillis, int numberOfRooms) {}
