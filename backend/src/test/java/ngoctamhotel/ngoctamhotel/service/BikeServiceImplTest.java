package ngoctamhotel.ngoctamhotel.service;

import ngoctamhotel.ngoctamhotel.dto.request.BikeBookingRequest;
import ngoctamhotel.ngoctamhotel.entity.Bike;
import ngoctamhotel.ngoctamhotel.entity.enums.BikeStatus;
import ngoctamhotel.ngoctamhotel.repository.BikeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BikeServiceImplTest {
    private final BikeRepository repository = mock(BikeRepository.class);
    private final BikeService service = new BikeService(repository);
    private final LocalDate start = LocalDate.now().plusDays(1);
    private final LocalDate end = start.plusDays(2);

    private BikeBookingRequest request(int quantity) {
        return new BikeBookingRequest(start, end, quantity, "Nguyễn Văn A", "0901234567", null, null);
    }

    @Test
    void booksRequestedBikesAndUsesOnlyKnownDatabasePrices() {
        when(repository.findAvailableForUpdate(start, end, 2)).thenReturn(List.of(
                new Bike(UUID.randomUUID(), "Xe 01", BikeStatus.AVAILABLE, new BigDecimal("100000")),
                new Bike(UUID.randomUUID(), "Xe 02", BikeStatus.AVAILABLE, new BigDecimal("100000"))));

        var response = service.createBooking(request(2));

        assertEquals(2, response.bookingIds().size());
        assertEquals(2, response.numberOfBikes());
        assertEquals(600000, response.totalPrice().intValueExact());
    }

    @Test
    void rejectsBookingWhenNotEnoughBikesRemain() {
        when(repository.findAvailableForUpdate(start, end, 2)).thenReturn(List.of(
                new Bike(UUID.randomUUID(), "Xe 01", BikeStatus.AVAILABLE, BigDecimal.ZERO)));

        var error = assertThrows(ResponseStatusException.class, () -> service.createBooking(request(2)));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).saveBooking(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void omitsPriceWhenBikeRateIsNotSet() {
        when(repository.findAvailableForUpdate(start, end, 1)).thenReturn(List.of(
                new Bike(UUID.randomUUID(), "Xe 01", BikeStatus.AVAILABLE, BigDecimal.ZERO)));

        assertNull(service.createBooking(request(1)).totalPrice());
    }
}
