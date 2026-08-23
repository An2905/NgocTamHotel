# Ngoc Tam Hotel

Monorepo gồm website đặt phòng, PMS/CMS nội bộ và API xác thực.

## Công nghệ

- Backend chính: Java 17, Spring Boot 4.1, Maven, Spring JDBC, Spring Security
- Frontend: React 19, Vite 8, React Router
- Database: Microsoft SQL Server

## Khởi tạo database

Chạy `database/scripts/sqlserver_schema.sql` bằng SQL Server Management Studio.

Thiết lập biến môi trường trước khi chạy backend:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="1433"
$env:DB_NAME="NgocTamHotel"
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="your-password"
$env:JWT_SECRET="replace-with-at-least-32-random-characters"
```

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

Mở `http://localhost:5173`. PMS và CMS sẽ tự chuyển đến `/login` nếu chưa đăng nhập.

## Tạo tài khoản seed

Trong môi trường dev, gửi request sau một lần để tạo tài khoản đầu tiên:

```powershell
$body = @{
  username = "admin"
  password = "Admin@123"
  email = "admin@ngoctamhotel.local"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/auth/register" `
  -ContentType "application/json" `
  -Body $body
```

Sau khi seed xong, đặt `REGISTRATION_ENABLED=false` và khởi động lại backend để khóa endpoint đăng ký.

## API xác thực

- `POST /api/auth/register`: tạo user và BCrypt hash mật khẩu.
- `POST /api/auth/login`: trả JWT dùng chung cho PMS và CMS.
