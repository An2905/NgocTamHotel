package ngoctamhotel.ngoctamhotel.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ngoctamhotel.ngoctamhotel.dto.request.BookingRequest;
import ngoctamhotel.ngoctamhotel.dto.response.BookingResponse;
import ngoctamhotel.ngoctamhotel.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        return bookingService.createBooking(request);
    }

    // Fix #12: Endpoint public — mask PII (SĐT + email) trước khi trả về
    @GetMapping("/{code}")
    public BookingResponse getBooking(@PathVariable String code) {
        BookingResponse full = bookingService.getBookingByCode(code);
        return new BookingResponse(
                full.id(),
                full.bookingCode(),
                full.guestName(),           // Giữ tên — khách cần biết đúng booking của mình
                maskPhone(full.guestPhone()),
                maskEmail(full.guestEmail()),
                full.roomNumber(),
                full.roomTypeName(),
                full.checkInDate(),
                full.checkOutDate(),
                full.adults(),
                full.children(),
                full.totalAmount(),
                full.status(),
                full.paymentStatus(),
                full.createdAt());
    }

    // Mask SĐT: 0912***678
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, 4) + "***" + phone.substring(phone.length() - 3);
    }

    // Mask email: ng***@gmail.com
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) return local.charAt(0) + "***" + domain;
        return local.substring(0, 2) + "***" + domain;
    }
}
