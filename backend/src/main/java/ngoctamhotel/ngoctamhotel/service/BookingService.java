package ngoctamhotel.ngoctamhotel.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ngoctamhotel.ngoctamhotel.dto.request.BookingRequest;
import ngoctamhotel.ngoctamhotel.dto.response.BookingResponse;
import ngoctamhotel.ngoctamhotel.dto.response.RevenueResponse;
import ngoctamhotel.ngoctamhotel.model.Booking;
import ngoctamhotel.ngoctamhotel.model.Guest;
import ngoctamhotel.ngoctamhotel.model.Payment;
import ngoctamhotel.ngoctamhotel.model.Room;
import ngoctamhotel.ngoctamhotel.model.RoomType;
import ngoctamhotel.ngoctamhotel.repository.BookingRepository;
import ngoctamhotel.ngoctamhotel.repository.GuestRepository;
import ngoctamhotel.ngoctamhotel.repository.PaymentRepository;
import ngoctamhotel.ngoctamhotel.repository.RoomRepository;
import ngoctamhotel.ngoctamhotel.repository.RoomTypeRepository;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final PaymentRepository paymentRepository;

    public BookingService(BookingRepository bookingRepository, GuestRepository guestRepository,
            RoomRepository roomRepository, RoomTypeRepository roomTypeRepository,
            PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // Validate dates
        if (request.checkOutDate().isBefore(request.checkInDate())
                || request.checkOutDate().isEqual(request.checkInDate())) {
            throw new IllegalArgumentException("Ngày trả phòng phải sau ngày nhận phòng");
        }
        if (request.checkInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày nhận phòng không được trong quá khứ");
        }

        // Find room type
        RoomType roomType = roomTypeRepository.findById(request.roomTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại phòng"));

        // Find available room — dùng FOR UPDATE để lock row, chặn race condition overbooking
        List<Room> available = roomRepository.findAvailableForUpdate(
                request.checkInDate(), request.checkOutDate(), request.roomTypeId());
        if (available.isEmpty()) {
            throw new IllegalArgumentException("Không còn phòng trống cho ngày bạn chọn");
        }
        Room room = available.get(0);

        // Calculate total
        long nights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        BigDecimal total = roomType.basePrice().multiply(BigDecimal.valueOf(nights));

        // Create or find guest
        Guest guest = guestRepository.create(
                request.fullName().trim(),
                request.phone().trim(),
                request.email() != null ? request.email().trim().toLowerCase() : null,
                request.identityNumber());

        // Generate booking code
        String code = "BK-" + LocalDate.now().getYear() + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Create booking
        Booking booking = bookingRepository.create(
                code, guest.id(), room.id(),
                request.checkInDate(), request.checkOutDate(),
                request.adults() > 0 ? request.adults() : 1,
                request.children(),
                total, request.notes());

        return toResponse(booking, guest, room, roomType, null);
    }

    public BookingResponse getBookingByCode(String code) {
        Booking booking = bookingRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt phòng"));
        return enrichBooking(booking);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllEnriched();
    }

    public List<BookingResponse> getBookingsByDateRange(LocalDate from, LocalDate to) {
        return bookingRepository.findByDateRange(from, to).stream()
                .map(this::enrichBooking)
                .toList();
    }

    public void updateBookingStatus(Long id, String status) {
        bookingRepository.updateStatus(id, status);
    }

    public RevenueResponse getRevenue(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingRepository.findByDateRange(from, to);
        BigDecimal roomRevenue = bookings.stream()
                .filter(b -> !"CANCELLED".equals(b.status()))
                .map(b -> Objects.requireNonNullElse(b.totalAmount(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int total = bookings.size();
        int completed = (int) bookings.stream()
                .filter(b -> "CHECKED_OUT".equals(b.status()))
                .count();

        String period = from + " → " + to;
        return new RevenueResponse(period, roomRevenue, roomRevenue, total, completed);
    }

    private BookingResponse enrichBooking(Booking booking) {
        Guest guest = guestRepository.findById(booking.guestId()).orElse(null);
        Room room = roomRepository.findById(booking.roomId()).orElse(null);
        RoomType roomType = null;
        if (room != null) {
            roomType = roomTypeRepository.findById(room.roomTypeId()).orElse(null);
        }

        Payment payment = paymentRepository.findByBookingId(booking.id()).orElse(null);

        return new BookingResponse(
                booking.id(),
                booking.bookingCode(),
                guest != null ? guest.fullName() : "N/A",
                guest != null ? guest.phone() : "",
                guest != null ? guest.email() : "",
                room != null ? room.roomNumber() : "",
                roomType != null ? roomType.name() : "",
                booking.checkInDate(),
                booking.checkOutDate(),
                booking.adults(),
                booking.children(),
                booking.totalAmount(),
                booking.status(),
                payment != null ? payment.status() : "UNPAID",
                booking.createdAt());
    }

    private BookingResponse toResponse(Booking booking, Guest guest, Room room,
            RoomType roomType, Payment payment) {
        return new BookingResponse(
                booking.id(),
                booking.bookingCode(),
                guest.fullName(),
                guest.phone(),
                guest.email(),
                room.roomNumber(),
                roomType.name(),
                booking.checkInDate(),
                booking.checkOutDate(),
                booking.adults(),
                booking.children(),
                booking.totalAmount(),
                booking.status(),
                payment != null ? payment.status() : "UNPAID",
                booking.createdAt());
    }
}
