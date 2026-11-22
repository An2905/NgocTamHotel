package ngoctamhotel.ngoctamhotel.controller;

import ngoctamhotel.ngoctamhotel.entity.Bike;
import ngoctamhotel.ngoctamhotel.dto.request.BikeBookingRequest;
import ngoctamhotel.ngoctamhotel.dto.response.BikeBookingResponse;
import ngoctamhotel.ngoctamhotel.service.BikeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BikeController {
    private final BikeService service;

    public BikeController(BikeService service) {
        this.service = service;
    }

    @GetMapping("/bikes")
    public List<Bike> getAllBike() {
        return service.getAllBike();
    }

    @GetMapping("/bikes/availability")
    public int countAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return service.countAvailable(startDate, endDate);
    }

    @PostMapping("/bike-bookings")
    public BikeBookingResponse createBooking(@RequestBody BikeBookingRequest request) {
        return service.createBooking(request);
    }
}
