# JWT Authentication

Tài liệu mô tả cách backend phát hành, xác thực và dùng JWT trong project — từ login đến gọi API REST và WebSocket.

---

## 1) Tổng quan

Project dùng **JWT stateless**: sau khi đăng nhập thành công, BE trả token; FE lưu token và gửi kèm mỗi request qua header `Authorization`.

```
FE ── POST /api/auth/login ──► BE (email + password + captcha)
FE ◄── JSON { token } ──────── BE

FE ── GET /api/team/... ─────► BE
     Header: Authorization: Bearer <jwt>
FE ◄── JSON response ───────── BE
```

**Không dùng:**

- Session cookie cho đăng nhập API (session chỉ dùng cho OTP đăng ký / reset password).
- Spring Security filter tự động parse JWT — mọi endpoint tự gọi `AuthService.validateRole()` hoặc `JwtUtil.extractClaims()` trong service layer.

**Thư viện:** [jjwt](https://github.com/jwtk/jjwt) (`jjwt-api`, `jjwt-impl`, `jjwt-jackson` trong `pom.xml`).

**File chính:**

| File | Vai trò |
|------|---------|
| `security/JwtUtil.java` | Tạo và parse token |
| `service/AuthService.java` | `validateRole()`, login, cấp token mới khi đổi profile |
| `config/SecurityConfig.java` | `permitAll()` — không chặn request ở filter |
| `config/WebSocketAuthInterceptor.java` | Xác thực JWT khi STOMP `CONNECT` |
| `exception/GlobalExceptionHandler.java` | Map lỗi JWT → HTTP 401 |

---

## 2) Cấu hình

Biến môi trường (xem `backend/.env.example`):

```properties
JWT_SECRET_KEY=your_very_secret_and_long_jwt_key_here
```

- Được inject vào `JwtUtil` qua `@Value("${JWT_SECRET_KEY}")`.
- Dùng **HMAC-SHA** (`Keys.hmacShaKeyFor(secret.getBytes(UTF_8))`) để ký và verify.
- Secret phải đủ dài và **không commit** lên git; mỗi môi trường (dev/staging/prod) nên có key riêng.

---

## 3) Cấu trúc token

Token được tạo trong `JwtUtil.generateToken(email, role, userId, fullName)`:

| Claim | Ý nghĩa | Ví dụ |
|-------|---------|-------|
| `sub` (subject) | Email đăng nhập | `student@fpt.edu.vn` |
| `role` | Vai trò trong DB | `STUDENT_FPT` |
| `userId` | UUID user | `a1b2c3d4-...` |
| `fullName` | Họ tên hiển thị | `Nguyen Van A` |
| `iat` | Thời điểm phát hành | auto |
| `exp` | Hết hạn | **24 giờ** (`86400000` ms) |

Ví dụ payload (decode, không verify):

```json
{
  "sub": "student@fpt.edu.vn",
  "role": "STUDENT_FPT",
  "userId": "uuid-here",
  "fullName": "Nguyen Van A",
  "iat": 1710000000,
  "exp": 1710086400
}
```

**Khi nào token được cấp mới:**

1. `POST /api/auth/login` — sau khi email/password đúng và account `APPROVED`.
2. `PUT /api/auth/profile` — sau khi cập nhật profile (đặc biệt khi đổi email, claims trong token cũ sẽ lệch).

Đổi mật khẩu (`PUT /api/auth/password`) **không** trả token mới — token cũ vẫn dùng được đến khi hết hạn.

---

## 4) Các role hỗ trợ

Role lấy từ cột `users.role` khi login. Các service kiểm tra role qua `validateRole(authHeader, "ROLE1", "ROLE2", ...)`.

| Role | Nhóm | API tiêu biểu |
|------|------|----------------|
| `STUDENT_FPT` | Sinh viên | `/api/team/*`, `/api/auth/github/*`, chat |
| `STUDENT_EXTERNAL` | Sinh viên | Giống `STUDENT_FPT` |
| `COORDINATOR` | Ban tổ chức | `/api/staff/*`, `/api/event/*` (mutations) |
| `EXPERT_INTERNAL` | Giám khảo / mentor | `/api/judge/*`, `/api/mentor/*` |
| `EXPERT_EXTERNAL` | Giám khảo / mentor | Giống `EXPERT_INTERNAL` |

So khớp role **không phân biệt hoa thường** (`equalsIgnoreCase`).

Một số endpoint public (không cần token): `POST /api/auth/login`, register, reset password OTP, v.v. Chi tiết từng route xem controller tương ứng.

---

## 5) Cách gửi token từ client

Header bắt buộc:

```
Authorization: Bearer <jwt>
```

**Frontend** (`frontend/src/api/client.js`):

- Token lưu `localStorage` key `hh_token`.
- Axios interceptor tự gắn header nếu token tồn tại và không phải chuỗi `"null"`.

**WebSocket** (`useChatStomp.js`):

- Gửi cùng header `Authorization: Bearer ...` trong STOMP `CONNECT` frame.
- `WebSocketAuthInterceptor` parse token, set `StompUserPrincipal` (userId + fullName) cho session chat.

---

## 6) Luồng xác thực trong backend

### 6.1 Login

```
AuthController.login()
  → AuthService.login()
      → verify captcha, email/password (BCrypt)
      → user.status == APPROVED
      → JwtUtil.generateToken(...)
      → LoginResponse { message, token }
```

Tài khoản chưa `APPROVED` → `401 Unauthorized` với message `"Login Denied: Account is not approved."`.

### 6.2 Protected REST endpoint (pattern chung)

```java
// Controller
@GetMapping("/example")
public ResponseEntity<?> example(@RequestHeader("Authorization") String authHeader) {
  return ResponseEntity.ok(someService.doSomething(authHeader));
}

// Service
public SomeResponse doSomething(String authHeader) {
  Claims claims = authService.validateRole(authHeader, "STUDENT_FPT", "STUDENT_EXTERNAL");
  String userId = claims.get("userId", String.class);
  // ... business logic
}
```

`validateRole()` thực hiện:

1. Header phải bắt đầu bằng `Bearer `.
2. `JwtUtil.extractClaims(token)` — verify chữ ký + expiration.
3. Đọc claim `role`, so với danh sách `allowedRoles`.
4. Trả về `Claims` nếu hợp lệ.

Một số method chỉ cần email, không cần check role cụ thể — dùng `extractEmailFromToken()` (private trong `AuthService`), ví dụ đổi password.

### 6.3 Spring Security

`SecurityConfig` cấu hình:

- CSRF **tắt** — API stateless, auth qua header.
- `anyRequest().permitAll()` — **không** có JWT filter toàn cục.

→ Mọi endpoint đều “mở” ở tầng filter; authorization nằm trong từng service. Endpoint quên gọi `validateRole()` sẽ không được bảo vệ.

---

## 7) Lỗi thường gặp

| Tình huống | HTTP | Message / handler |
|------------|------|-------------------|
| Thiếu header hoặc không có `Bearer ` | 401 | `"Invalid or missing token."` |
| Token hết hạn / chữ ký sai / format lỗi | 401 | `GlobalExceptionHandler` → `"Invalid token"` (`JwtException`) |
| Token hợp lệ nhưng thiếu claim `role` | 401 | `"Access Denied: Missing role."` |
| Token hợp lệ, role không nằm trong allowed list | 403 | `"Forbidden access."` |

Response body dạng `ErrorResponse` (message + status).

---

## 8) Ghi chú triển khai

**Refresh token:** Hiện **chưa** có. User hết hạn 24h phải login lại (hoặc gọi `PUT /api/auth/profile` để nhận token mới nếu vẫn còn token cũ hợp lệ).

**Logout:** Không có endpoint revoke — FE xóa `localStorage` (`hh_token`) là đủ phía client; token vẫn valid đến `exp` nếu bị lộ.

**Đổi `JWT_SECRET_KEY`:** Mọi token cũ invalidate ngay lập tức.

**Bảo mật:**

- Luôn dùng HTTPS trên production.
- Không log full token.
- Secret key đủ entropy (khuyến nghị ≥ 32 byte random, encode base64 hoặc chuỗi dài).

---

## 9) Checklist khi thêm API mới

1. Controller nhận `@RequestHeader("Authorization") String authHeader` (hoặc `required = false` nếu public).
2. Service gọi `authService.validateRole(authHeader, ...)` với đúng role.
3. Lấy `userId` từ `claims.get("userId", String.class)` thay vì tin userId từ body/query.
4. Document FE: endpoint cần role nào (theo convention comment trong `frontend/src/api/*.js`).
5. Nếu endpoint chat/WebSocket: đảm bảo client gửi `Authorization` lúc `CONNECT`.
