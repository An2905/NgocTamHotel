package ngoctamhotel.ngoctamhotel.service;

import ngoctamhotel.ngoctamhotel.entity.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

/** Cầu nối tới API PayOS. PaymentService cấp mã đơn/số tiền; PayOS trả linkId và URL.
 * Class này không ghi DB và không quyết định booking đã thanh toán hay chưa. */
@Service
public class PayOSService {
    private final PayOS client;
    private final String frontendUrl;

    public PayOSService(@Value("${payos.client-id:}") String clientId,
                        @Value("${payos.api-key:}") String apiKey,
                        @Value("${payos.checksum-key:}") String checksumKey,
                        @Value("${app.frontend-url}") String frontendUrl) {
        // Chưa cấu hình khóa vẫn cho ứng dụng khởi động; chỉ từ chối khi tạo payment.
        this.client = clientId.isBlank() || apiKey.isBlank() || checksumKey.isBlank()
                ? null : new PayOS(clientId, apiKey, checksumKey);
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    public void checkConfigured() {
        if (client == null) throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình khóa PayOS");
    }

    public CreatePaymentLinkResponse createLink(Payment payment, long deadline) {
        checkConfigured();
        String page = frontendUrl + "/payment/" + payment.getBookingId();
        // Builder chỉ tạo dữ liệu gửi đi. amount là tiền cọc BE đã xác định.
        var request = CreatePaymentLinkRequest.builder()
                .orderCode(payment.getPayosOrderCode())
                .amount(payment.getAmountVnd().longValueExact())
                .description("Coc phong")
                .expiredAt(deadline)
                .returnUrl(page).cancelUrl(page).build();
        // ĐÂY là lệnh gọi mạng tới PayOS; response chứa paymentLinkId và checkoutUrl.
        return client.paymentRequests().create(request);
    }

    public vn.payos.model.v2.paymentRequests.PaymentLink lookup(Long code) {
        checkConfigured();
        return client.paymentRequests().get(code);
    }

    public vn.payos.model.v2.paymentRequests.PaymentLink cancel(Long code) {
        checkConfigured();
        return client.paymentRequests().cancel(code, "Booking deadline or checkout recovery");
    }

    public vn.payos.model.webhooks.WebhookData verify(vn.payos.model.webhooks.Webhook webhook) {
        checkConfigured();
        return client.webhooks().verify(webhook);
    }
}
