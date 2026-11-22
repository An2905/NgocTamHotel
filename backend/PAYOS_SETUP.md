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

## Xác nhận và đối soát
Đăng ký webhook trên kênh PayOS: https://<backend-public>/api/payments/webhook.
Endpoint phải truy cập được từ Internet. Backend xác minh chữ ký bằng SDK,
sau đó lấy trạng thái tổng của link từ API PayOS trước khi cập nhật tiền cọc.
DB phải có migration room_holds; thay đổi payment này không cần migration mới.

- GET /api/payments/booking/{bookingId} chỉ đọc trạng thái DB.
- FE hỏi tối đa 30 lần, mỗi 2 giây; nút kiểm tra trạng thái cho phép hỏi lại.
- BE đối soát booking PENDING mỗi 60 giây (payment.reconcile-delay-ms).
- PAID và CONFIRMED được lưu trong cùng transaction; webhook lặp không ghi nhận hai lần.
- Khi hết hạn, BE hủy link còn hoạt động rồi kiểm tra kết quả trước khi nhả phòng.
  Booking PENDING vẫn chặn phòng trong khi kết quả chưa rõ.
- Link CANCELLED/EXPIRED và chưa nhận tiền cho phép tạo payment mới nếu booking còn hạn.
- Link đã tạo nhưng mất response được tra cứu theo orderCode; nếu mất checkoutUrl,
  BE hủy link chưa trả tiền trước khi cho phép tạo lại.
- Timeout, mã đơn chưa tìm thấy, trả thiếu/thừa tiền: giữ chờ đối soát.
  Xem cảnh báo backend log; không xóa payment để thử lại tùy tiện.
- Tiền về sau khi booking CANCELLED được ghi nhận PAID nhưng không tự cấp lại phòng;
  status trả needsReview và log yêu cầu quản trị xử lý/hoàn tiền thủ công.

## Vận hành
Return URL chỉ điều hướng, không xác nhận đã nhận tiền. Không đánh dấu PAID ở FE.
Mỗi môi trường dùng kênh PayOS riêng hoặc bảo đảm dãy orderCode không đụng các mã
đã tồn tại trên cùng kênh, đặc biệt khi xóa/khôi phục DB.

Test dùng gateway giả lập và H2. Cần kiểm tra tích hợp với kênh PayOS đã cấu hình
trước khi dùng thực tế. Không có thao tác hoàn tiền tự động.
