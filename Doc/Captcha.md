# Captcha

Tài liệu này mô tả cách sử dụng Google reCAPTCHA trong project, luồng hoạt động ở frontend/backend, và cách implement captcha cho API mới.

## 1) Captcha dùng để làm gì?

Captcha được dùng để giảm request spam/bot ở các flow public, đặc biệt là các action chưa cần đăng nhập:

- Đăng nhập.
- Gửi OTP đăng ký.
- Gửi lại OTP đăng ký.

Hiện tại reset password chưa gắn captcha ở frontend/backend.

## 2) Cấu hình môi trường

Frontend cần site key:

```properties
VITE_RECAPTCHA_SITE_KEY=your_recaptcha_site_key
```

Backend cần secret key:

```properties
RECAPTCHA_SECRET=your_recaptcha_secret
```

Mapping trong code:

- FE site key: `frontend/src/utils/recaptcha.js`
- BE secret key: `backend/src/main/java/com/hackathon/hackathon/service/CaptchaService.java`
- BE dev config: `backend/src/main/resources/application-dev.properties`

## 3) Captcha hoạt động trên frontend như thế nào?

Luồng frontend:

1. Component form render captcha widget.
2. User tick captcha.
3. Trước khi gọi API, FE lấy token bằng `getCaptchaToken()`.
4. FE gửi token lên backend qua field `captchaToken`.
5. Sau request, FE reset captcha bằng `resetCaptcha()`.

Các file liên quan:

- `frontend/src/utils/recaptcha.js`
  - Đọc `VITE_RECAPTCHA_SITE_KEY`.
  - Load script `https://www.google.com/recaptcha/api.js?render=explicit`.
- `frontend/src/components/common/RecaptchaWidget.jsx`
  - Render widget Google reCAPTCHA.
  - Expose `getToken()` và `reset()` qua ref.
- `frontend/src/hooks/useRecaptcha.js`
  - Cung cấp API tiện dụng cho form:
    - `getCaptchaToken()`
    - `resetCaptcha()`
    - `RecaptchaField`

Ví dụ trong `LoginModal`:

- File: `frontend/src/components/common/LoginModal.jsx`
- Render captcha: `<RecaptchaField />`
- Lấy token: `const captchaToken = await getCaptchaToken()`
- Gửi API: `login({ ...form, captchaToken })`
- Reset sau request: `resetCaptcha()`

Ví dụ trong `RegisterModal`:

- File: `frontend/src/components/common/RegisterModal.jsx`
- Form gửi OTP đăng ký dùng `InfoRecaptchaField`.
- Flow resend OTP dùng `ResendRecaptchaField`.
- Cả hai đều gọi `sendRegisterOtp({ ...form, captchaToken })`.

## 4) Captcha hoạt động trên backend như thế nào?

Backend nhận `captchaToken` từ request body và verify token với Google.

Luồng backend:

1. Controller nhận request.
2. Service gọi `requireValidCaptcha(request.getCaptchaToken())`.
3. `CaptchaService.verify(token)` gửi request tới Google endpoint:
   - `https://www.google.com/recaptcha/api/siteverify`
4. Backend gửi form data:
   - `secret`: lấy từ `RECAPTCHA_SECRET`
   - `response`: token FE gửi lên
5. Google trả JSON có field `success`.
6. Nếu `success = false`, backend throw `BadRequestException("Invalid captcha.")`.

Các file liên quan:

- `backend/src/main/java/com/hackathon/hackathon/service/CaptchaService.java`
  - Verify token bằng `RestTemplate`.
  - Gửi `secret` và `response` dạng `application/x-www-form-urlencoded`.
- `backend/src/main/java/com/hackathon/hackathon/model/dto/response/CaptchaResponse.java`
  - Map response từ Google, hiện chỉ đọc field `success`.
- `backend/src/main/java/com/hackathon/hackathon/service/AuthService.java`
  - Hàm `requireValidCaptcha(...)`.
  - Gọi captcha trong các flow cần bảo vệ.

## 5) Luồng captcha trong từng chức năng

Captcha hiện có ở 3 chức năng public. Mỗi flow đi theo thứ tự **FE → Controller → Service → CaptchaService**. Dưới đây giải thích từng bước làm gì.

---

### Flow 1: Đăng nhập

**Endpoint:** `POST /api/auth/login`

| Bước | Layer | File / method | Ở đây xử lý gì? |
|------|-------|---------------|-----------------|
| 1 | FE form | `LoginModal.jsx` | Hiển thị form email/mật khẩu và widget captcha. User tick captcha rồi bấm đăng nhập. |
| 2 | FE hook | `useRecaptcha.js` → `getCaptchaToken()` | Lấy token captcha từ widget Google (chỉ lấy token, **không** verify). |
| 3 | FE API | `auth.js` → `login(...)` | Gửi `POST /api/auth/login` với `{ email, password, captchaToken }`. |
| 4 | Controller | `AuthController.login(...)` | Nhận request JSON, Spring validate `LoginRequest`, rồi gọi `authService.login(...)`. Controller **không** tự verify captcha. |
| 5 | Service | `AuthService.login(...)` | Kiểm tra email/password có nhập chưa → gọi `requireValidCaptcha(...)` → tra DB, so khớp mật khẩu, kiểm tra trạng thái tài khoản → trả JWT nếu hợp lệ. |
| 6 | Service helper | `AuthService.requireValidCaptcha(...)` | Gọi `CaptchaService.verify(token)`. Nếu Google trả `success = false` → throw `"Invalid captcha."` và dừng luồng đăng nhập. |
| 7 | Captcha | `CaptchaService.verify(...)` | Gửi `secret` + `response` (token FE gửi lên) tới Google `/api/siteverify`. Trả `true/false`. |
| 8 | FE cleanup | `LoginModal.jsx` → `resetCaptcha()` | Dù thành công hay lỗi, reset widget captcha trong `finally` để lần sau dùng token mới. |

**File liên quan:** `LoginRequest.java`, `frontend/src/api/auth.js`, `frontend/src/hooks/useRecaptcha.js`

---

### Flow 2: Gửi OTP đăng ký (bước nhập thông tin)

**Endpoint:** `POST /api/auth/register/otp`

| Bước | Layer | File / method | Ở đây xử lý gì? |
|------|-------|---------------|-----------------|
| 1 | FE form | `RegisterModal.jsx` (step `info`) | User điền họ tên, email, trường, MSSV, mật khẩu, tick captcha (`InfoRecaptchaField`), bấm tiếp tục. |
| 2 | FE hook | `getInfoCaptcha()` | Lấy token captcha từ widget ở bước nhập thông tin. |
| 3 | FE API | `auth.js` → `sendRegisterOtp(...)` | Gửi `POST /api/auth/register/otp` với toàn bộ thông tin đăng ký + `captchaToken`. |
| 4 | Controller | `AuthController.sendRegisterOtp(...)` | Nhận body `StudentRegisterRequest` và `HttpSession`, validate DTO, chuyển xuống service. Controller **không** gửi email hay tạo OTP. |
| 5 | Service | `AuthService.sendRegisterOtp(...)` | **Bước đầu tiên:** verify captcha. Sau đó kiểm tra email/MSSV chưa tồn tại → tạo OTP 6 số → gửi email → lưu OTP + thời hạn + dữ liệu đăng ký vào session. |
| 6 | Service helper | `AuthService.requireValidCaptcha(...)` | Giống flow đăng nhập: captcha sai thì throw lỗi ngay, không gửi OTP. |
| 7 | Captcha | `CaptchaService.verify(...)` | Hỏi Google token có hợp lệ không. |
| 8 | FE cleanup | `resetInfoCaptcha()` | Reset widget captcha bước info, chuyển sang step `otp` nếu thành công. |

**File liên quan:** `StudentRegisterRequest.java`, `frontend/src/api/auth.js`

---

### Flow 3: Gửi lại OTP đăng ký (bước nhập mã OTP)

**Endpoint:** `POST /api/auth/register/otp` (cùng endpoint với Flow 2)

| Bước | Layer | File / method | Ở đây xử lý gì? |
|------|-------|---------------|-----------------|
| 1 | FE form | `RegisterModal.jsx` (step `otp`) → `handleResend()` | User bấm "Gửi lại OTP". Form dùng **widget captcha riêng** (`ResendRecaptchaField`), không dùng lại widget ở bước info. |
| 2 | FE hook | `getResendCaptcha()` | Lấy token captcha mới từ widget resend. |
| 3 | FE API | `auth.js` → `sendRegisterOtp(...)` | Gửi lại cùng endpoint với dữ liệu form đã lưu + `captchaToken` mới. |
| 4 | Controller | `AuthController.sendRegisterOtp(...)` | Xử lý **giống hệt Flow 2**: nhận request, validate, chuyển xuống service. |
| 5 | Service | `AuthService.sendRegisterOtp(...)` | Vẫn verify captcha trước → tạo OTP mới → gửi email → ghi đè OTP + expire time trong session. |
| 6 | Captcha | `CaptchaService.verify(...)` | Mỗi lần gửi lại cần token captcha mới; token cũ không dùng lại được. |
| 7 | FE cleanup | `resetResendCaptcha()` | Reset widget resend sau request. |

**Lưu ý:** Flow 2 và 3 dùng chung endpoint + backend, nhưng FE có **2 widget captcha riêng** (`InfoRecaptchaField` vs `ResendRecaptchaField`) để mỗi lần gửi OTP đều tick captcha mới.

**File liên quan:** `RegisterModal.jsx` → `handleResend()`, `frontend/src/hooks/useRecaptcha.js`

## 6) Cách implement captcha vào API mới

### Backend

1. Thêm field `captchaToken` vào request DTO.

Ví dụ:

```java
private String captchaToken;
```

2. Inject/use `CaptchaService` trong service layer.

Trong project hiện tại, `AuthService` đã có:

```java
@Autowired private CaptchaService captchaService;
```

3. Verify token trước khi xử lý nghiệp vụ chính.

Pattern hiện tại trong `AuthService`:

```java
private void requireValidCaptcha(String captchaToken) {
  if (!captchaService.verify(captchaToken)) {
    throw new BadRequestException("Invalid captcha.");
  }
}
```

4. Gọi `requireValidCaptcha(...)` ở đầu method service.

Ví dụ đang có:

```java
public LoginResponse login(LoginRequest request) {
  requireValidEmail(request.getEmail());
  requireNonBlank(request.getPassword(), "Password");
  requireValidCaptcha(request.getCaptchaToken());
  ...
}
```

### Frontend

1. Import hook:

```javascript
import { useRecaptcha } from '../../hooks/useRecaptcha'
```

2. Tạo captcha hook trong component:

```javascript
const { getCaptchaToken, resetCaptcha, RecaptchaField } = useRecaptcha()
```

3. Render field trong form:

```jsx
<RecaptchaField />
```

4. Lấy token trước khi gọi API:

```javascript
const captchaToken = await getCaptchaToken()
await yourApi({ ...form, captchaToken })
```

5. Reset captcha sau request:

```javascript
finally {
  resetCaptcha()
}
```

## 7) Checklist khi thêm captcha cho API mới

- Request DTO có `captchaToken`.
- FE API wrapper truyền `captchaToken` trong body.
- Form FE render `<RecaptchaField />`.
- Form FE gọi `getCaptchaToken()` trước khi gọi API.
- Form FE gọi `resetCaptcha()` trong `finally`.
- Service backend gọi `requireValidCaptcha(...)` trước nghiệp vụ chính.
- Error `"Invalid captcha."` được map ở `frontend/src/utils/errors.js`.

## 8) Lưu ý

- Captcha token chỉ nên dùng một lần cho một request.
- Sau mỗi request nên reset widget để tránh reuse token cũ.
- Không verify captcha ở frontend; frontend chỉ lấy token, backend mới là nơi verify thật với Google.
- Không expose `RECAPTCHA_SECRET` ra frontend. Frontend chỉ được dùng `VITE_RECAPTCHA_SITE_KEY`.
