package ngoctamhotel.ngoctamhotel.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ngoctamhotel.ngoctamhotel.dto.response.BookingResponse;
import ngoctamhotel.ngoctamhotel.dto.response.RevenueResponse;
import ngoctamhotel.ngoctamhotel.service.BookingService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final BookingService bookingService;

    public AdminController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings")
    public List<BookingResponse> listBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null) {
            return bookingService.getBookingsByDateRange(from, to);
        }
        return bookingService.getAllBookings();
    }

    @GetMapping("/revenue")
    public RevenueResponse getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return bookingService.getRevenue(from, to);
    }

    @PutMapping("/bookings/{id}/status")
    public Map<String, String> updateBookingStatus(@PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Thiếu trạng thái");
        }
        bookingService.updateBookingStatus(id, status);
        return Map.of("message", "Cập nhật trạng thái thành công");
    }
}
