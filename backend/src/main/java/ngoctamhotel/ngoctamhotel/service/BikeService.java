package ngoctamhotel.ngoctamhotel.service;

import ngoctamhotel.ngoctamhotel.dto.request.BikeBookingRequest;
import ngoctamhotel.ngoctamhotel.dto.response.BikeBookingResponse;
import ngoctamhotel.ngoctamhotel.entity.Bike;
import ngoctamhotel.ngoctamhotel.repository.BikeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BikeService {
    private final BikeRepository repo;

    public BikeService(BikeRepository repo) {
        this.repo = repo;
    }

    public List<Bike> getAllBike() {
        return repo.findAll();
    }

    public int countAvailable(LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        return repo.countAvailable(startDate, endDate);
    }

    @Transactional
    public BikeBookingResponse createBooking(BikeBookingRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu thông tin thuê xe");
        }
        validateDates(request.startDate(), request.endDate());
        if (request.numberOfBikes() < 1 || request.fullName() == null || request.fullName().isBlank()
                || request.phone() == null || request.phone().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin thuê xe không hợp lệ");
        }

        List<Bike> bikes = repo.findAvailableForUpdate(
                request.startDate(), request.endDate(), request.numberOfBikes());
        if (bikes.size() != request.numberOfBikes()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Không còn đủ xe cho khoảng ngày đã chọn");
        }

        String fullName = request.fullName().trim();
        String phone = request.phone().trim();
        String email = request.email() == null || request.email().isBlank()
                ? null : request.email().trim();

        List<UUID> bookingIds = bikes.stream().map(bike -> UUID.randomUUID()).toList();
        for (int i = 0; i < bikes.size(); i++) {
            repo.saveBooking(bookingIds.get(i), bikes.get(i).getId(), fullName, phone, email,
                    request.startDate(), request.endDate(), request.roomBookingId());
        }

        long rentalDays = ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;
        boolean priceKnown = bikes.stream().allMatch(bike -> bike.getPrice() != null && bike.getPrice().signum() > 0);
        BigDecimal totalPrice = priceKnown
                ? bikes.stream()
                    .map(Bike::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .multiply(BigDecimal.valueOf(rentalDays))
                : null;

        return new BikeBookingResponse(bookingIds, bikes.stream().map(Bike::getName).toList(),
                request.startDate(), request.endDate(), bikes.size(), totalPrice,
                "IN_PROGRESS", fullName, phone);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isBefore(LocalDate.now())
                || endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày thuê xe không hợp lệ");
        }
    }
}
