package ngoctamhotel.ngoctamhotel.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import ngoctamhotel.ngoctamhotel.dto.response.PaymentResponse;
import ngoctamhotel.ngoctamhotel.model.Booking;
import ngoctamhotel.ngoctamhotel.model.Payment;
import ngoctamhotel.ngoctamhotel.repository.BookingRepository;
import ngoctamhotel.ngoctamhotel.repository.PaymentRepository;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    public PaymentResponse createPayment(Long bookingId, String method) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt phòng"));

        if ("CANCELLED".equals(booking.status()) || "CHECKED_OUT".equals(booking.status())) {
            throw new IllegalArgumentException("Đơn đặt phòng không thể thanh toán");
        }

        String transactionRef;
        if ("VNPAY".equals(method)) {
            transactionRef = "VNP-" + System.currentTimeMillis() + "-" +
                    UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        } else {
            transactionRef = "BT-" + System.currentTimeMillis() + "-" +
                    UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        }

        Payment payment = paymentRepository.create(
                bookingId, method, booking.totalAmount(), transactionRef);

        return toResponse(payment, booking.bookingCode());
    }

    public PaymentResponse confirmPayment(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán"));

        paymentRepository.updateStatus(payment.id(), "COMPLETED");
        bookingRepository.updateStatus(bookingId, "CONFIRMED");

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        Payment updated = paymentRepository.findByBookingId(bookingId).orElseThrow();
        return toResponse(updated, booking.bookingCode());
    }

    public PaymentResponse failPayment(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán"));

        paymentRepository.updateStatus(payment.id(), "FAILED");

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        Payment updated = paymentRepository.findByBookingId(bookingId).orElseThrow();
        return toResponse(updated, booking.bookingCode());
    }

    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Chưa có thanh toán cho đơn này"));
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        return toResponse(payment, booking.bookingCode());
    }

    private PaymentResponse toResponse(Payment payment, String bookingCode) {
        return new PaymentResponse(
                payment.id(), payment.bookingId(), bookingCode,
                payment.method(), payment.amount(), payment.status(),
                payment.transactionRef(), payment.paidAt());
    }
}
