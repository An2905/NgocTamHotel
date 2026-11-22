# Tạo link thanh toán PayOS

## Cấu hình
Đặt PAYOS_CLIENT_ID, PAYOS_API_KEY, PAYOS_CHECKSUM_KEY trong backend/.env (không commit).
FRONTEND_URL phải là một URL giao diện, ví dụ http://localhost:5173.
DB cũ: chạy database/migrations/20261112_payos_order_code.sql sau migration alignment.
DB mới: schema đã có AUTO_INCREMENT cho payos_order_code.

## Luồng code
- PaymentController nhận POST /api/payments, body gồm bookingId và method=PAYOS.
- PaymentService kiểm tra booking PENDING và khóa hàng booking trong transaction ngắn.
- PaymentRepository INSERT payment PROCESSING; MySQL cấp payos_order_code.
  GeneratedKeyHolder lấy mã số đó; UUID payment.id vẫn là khóa nội bộ.
- PayOSService tạo request rồi gọi client.paymentRequests().create(request) qua mạng.
- PayOS trả paymentLinkId và checkoutUrl. Repository UPDATE hai giá trị này;
  PaymentResponse trả checkoutUrl cho PaymentPage chuyển trình duyệt tới PayOS.
- Yêu cầu lặp dùng lại link UNPAID đã lưu. Yêu cầu PROCESSING không được cấp mã mới.

## Giới hạn hiện tại
Chưa triển khai webhook xác nhận PAID, đối soát tự động khi timeout, hay cấp lại
link đã hết hạn/hủy. PROCESSING sau lỗi cần được đối soát theo orderCode trước
khi cho thử lại; không xóa payment để tạo mã mới tùy tiện.
Return URL chỉ điều hướng, không xác nhận đã nhận tiền. Không đánh dấu PAID ở FE.
Mỗi môi trường dùng kênh PayOS riêng hoặc bảo đảm dãy orderCode không đụng các mã
đã tồn tại trên cùng kênh, đặc biệt khi xóa/khôi phục DB.

Chưa chạy build/test hoặc gọi API PayOS thật trong lần thay đổi này.
