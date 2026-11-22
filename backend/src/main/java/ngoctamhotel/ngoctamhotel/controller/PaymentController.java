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

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        return paymentService.createPayment(request);
    }
}
