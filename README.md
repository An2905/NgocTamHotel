# Ngoc Tam Hotel

Monorepo gồm website đặt phòng, frontend React và backend Java.

## Công nghệ

- Backend chính: Java 17, Spring Boot 4.1, Maven, Spring JDBC, Spring Security
- Frontend: React 19, Vite 8, React Router
- Database: MySQL 8

## Khởi tạo database

Khi backend khởi động, Spring chạy `backend/src/main/resources/schema.sql`
trên database được chọn bởi `DB_NAME`. Script tạo các bảng còn thiếu cho
database mới; `CREATE TABLE IF NOT EXISTS` không cập nhật cấu trúc bảng cũ.
Với database đã có dữ liệu, chạy migration tương ứng trong
`database/migrations` một lần trước khi khởi động backend với code mới.
Có thể chạy `database/scripts/seeding.sql` thủ công để thêm dữ liệu mẫu.
`database/scripts/schema.sql` là bản dùng khi muốn tạo schema thủ công.

Thiết lập biến môi trường trước khi chạy backend:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="ngoc_tam_hotel"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-password"
$env:JWT_SECRET="replace-with-at-least-32-random-characters"
$env:ADMIN_BOOTSTRAP_PASSWORD="choose-a-strong-admin-password"
```

`spring.sql.init.mode=always` trong backend bật việc chạy schema khi ứng dụng
khởi động; lệnh build Maven không chạy file SQL này. Seed và migration không
tự chạy. Giữ định nghĩa bảng ở hai file `schema.sql` đồng bộ khi sửa schema.
Lần khởi động đầu tiên, `ADMIN_BOOTSTRAP_PASSWORD` tạo tài khoản `admin` với mật khẩu
BCrypt nếu tài khoản này chưa tồn tại. Sau khi tạo, có thể bỏ biến này khỏi môi trường.
Admin đăng nhập bằng `POST /api/admin/login` với JSON `{"username":"admin","password":"..."}`.

## Chạy ứng dụng

Backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Frontend:

```powershell
cd frontend
npm install
npm run dev
```

Mở `http://localhost:5173`. Khách không cần tài khoản: chọn phòng và nhập thông tin
trực tiếp tại `/booking`. Frontend không còn trang đăng nhập hay đăng ký.

## Giới hạn tích hợp hiện tại

Backend hiện tại chưa có `POST /api/bookings` và `GET /api/rooms` trả JSON như
frontend cần, đồng thời vẫn còn lỗi biên dịch. Schema và frontend đã được cập nhật,
nhưng thao tác đặt phòng thực tế sẽ chưa hoàn tất cho đến khi backend cung cấp các API đó.
