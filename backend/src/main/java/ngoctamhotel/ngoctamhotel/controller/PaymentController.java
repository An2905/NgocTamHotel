package ngoctamhotel.ngoctamhotel.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ngoctamhotel.ngoctamhotel.dto.request.PaymentRequest;
import ngoctamhotel.ngoctamhotel.dto.response.PaymentResponse;
import ngoctamhotel.ngoctamhotel.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody PaymentRequest request) {
        return paymentService.createPayment(request.bookingId(), request.method());
    }

    @PutMapping("/{bookingId}/confirm")
    public PaymentResponse confirmPayment(@PathVariable Long bookingId) {
        return paymentService.confirmPayment(bookingId);
    }

    @PutMapping("/{bookingId}/fail")
    public PaymentResponse failPayment(@PathVariable Long bookingId) {
        return paymentService.failPayment(bookingId);
    }
}
