package ngoctamhotel.ngoctamhotel.service;

import ngoctamhotel.ngoctamhotel.dto.request.RoomBookingRequest;
import ngoctamhotel.ngoctamhotel.entity.Room;
import ngoctamhotel.ngoctamhotel.entity.RoomBooking;
import ngoctamhotel.ngoctamhotel.entity.enums.RoomStatus;
import ngoctamhotel.ngoctamhotel.repository.RoomRepository;
import ngoctamhotel.ngoctamhotel.repository.RoomHoldRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoomServiceImplTest {
    private final RoomRepository repository = mock(RoomRepository.class);
    private final RoomHoldRepository holds = mock(RoomHoldRepository.class);
    private final RoomService service = new RoomService(repository, holds);
    private final UUID categoryId = UUID.randomUUID();
    private final UUID ownerKey = UUID.randomUUID();

    private RoomBookingRequest request() {
        return new RoomBookingRequest(UUID.randomUUID(), ownerKey, categoryId, LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 17), 2, 0, null, 2,
                "Nguyễn Văn A", "a@example.com", "0901234567", null);
    }

    @Test
    void createsOneBookingForTwoRoomsAndCalculatesPriceOnServer() {
        Room first = new Room(UUID.randomUUID(), "101", RoomStatus.AVAILABLE, categoryId);
        Room second = new Room(UUID.randomUUID(), "102", RoomStatus.AVAILABLE, categoryId);
        when(holds.findForUpdate(any())).thenAnswer(invocation -> new RoomHoldRepository.Hold(
                categoryId, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 17), 2,
                java.time.LocalDateTime.now().plusMinutes(5), "ACTIVE", ownerKey));
        when(holds.rooms(any())).thenReturn(List.of(first, second));
        when(repository.findCategoryPrice(categoryId)).thenReturn(new BigDecimal("350000"));
        when(repository.findCategoryMaxGuests(categoryId)).thenReturn(2);
        when(repository.save(any(RoomBooking.class))).thenAnswer(invocation -> {
            RoomBooking booking = invocation.getArgument(0);
            booking.setId(UUID.randomUUID());
            return booking;
        });

        var response = service.createBooking(request());

        assertNotNull(response.roomBookingId());
        assertEquals(2, response.numberOfRooms());
        assertEquals(0, new BigDecimal("1400000").compareTo(response.totalPrice()));
    }

    @Test
    void rejectsBookingWhenFewerRoomsRemainThanRequested() {
        when(holds.findForUpdate(any())).thenAnswer(invocation -> new RoomHoldRepository.Hold(
                categoryId, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 17), 2,
                java.time.LocalDateTime.now().plusMinutes(5), "ACTIVE", ownerKey));
        when(holds.rooms(any())).thenReturn(
                List.of(new Room(UUID.randomUUID(), "101", RoomStatus.AVAILABLE, categoryId)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.createBooking(request()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(repository, never()).save(any());
    }
}
