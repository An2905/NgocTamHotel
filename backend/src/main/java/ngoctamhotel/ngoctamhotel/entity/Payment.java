package ngoctamhotel.ngoctamhotel.entity;

import ngoctamhotel.ngoctamhotel.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Trước: @GeneratedValue chỉ có tác dụng khi JPA lưu entity, nhưng repository dùng JdbcTemplate.
// Sau: payment id do luồng tạo payment cấp; model khớp các cột DATETIME và PayOS trong DB.
public class Payment {
    private UUID id;

    // booking_id là khóa ngoại; một booking có thể có nhiều lần thử thanh toán.
    private UUID bookingId;

    // Trước: long mặc định 0 ngay cả trước khi PayOS tạo link.
    // Sau: Long có thể null cho payment chưa có order code.
    private Long payosOrderCode;
    private String payosPaymentLinkId;
    private String checkoutUrl;
    private BigDecimal amountVnd;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String paymentMethod;

    public Payment(UUID id, UUID bookingId, Long payosOrderCode, String payosPaymentLinkId, String checkoutUrl, BigDecimal amountVnd, PaymentStatus status, LocalDateTime createdAt, LocalDateTime paidAt, String paymentMethod) {
        this.id = id;
        this.bookingId = bookingId;
        this.payosOrderCode = payosOrderCode;
        this.payosPaymentLinkId = payosPaymentLinkId;
        this.checkoutUrl = checkoutUrl;
        this.amountVnd = amountVnd;
        this.status = status;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
        this.paymentMethod = paymentMethod;
    }

    public Payment() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public Long getPayosOrderCode() {
        return payosOrderCode;
    }

    public void setPayosOrderCode(Long payosOrderCode) {
        this.payosOrderCode = payosOrderCode;
    }

    public String getPayosPaymentLinkId() {
        return payosPaymentLinkId;
    }

    public void setPayosPaymentLinkId(String payosPaymentLinkId) {
        this.payosPaymentLinkId = payosPaymentLinkId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public BigDecimal getAmountVnd() {
        return amountVnd;
    }

    public void setAmountVnd(BigDecimal amountVnd) {
        this.amountVnd = amountVnd;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
