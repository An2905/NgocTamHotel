package ngoctamhotel.ngoctamhotel.controller;

import ngoctamhotel.ngoctamhotel.dto.request.PaymentRequest;
import ngoctamhotel.ngoctamhotel.dto.response.PaymentResponse;
import ngoctamhotel.ngoctamhotel.service.PaymentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final ngoctamhotel.ngoctamhotel.service.PaymentLifecycleService lifecycle;

    public PaymentController(PaymentService paymentService, ngoctamhotel.ngoctamhotel.service.PaymentLifecycleService lifecycle) {
        this.paymentService = paymentService;
        this.lifecycle = lifecycle;
    }

    @PostMapping
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @org.springframework.web.bind.annotation.GetMapping("/booking/{bookingId}")
    public java.util.Map<String,Object> status(@org.springframework.web.bind.annotation.PathVariable java.util.UUID bookingId) {
        return lifecycle.status(bookingId);
    }

    @PostMapping("/webhook")
    public void webhook(@RequestBody vn.payos.model.webhooks.Webhook webhook) {
        lifecycle.webhook(webhook);
    }
}
