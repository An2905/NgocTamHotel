# API Naming Convention & Rules

Tài liệu này mô tả cách đặt tên API cho backend và lấy ví dụ trực tiếp từ `StaffController`.

## 1) Cách đặt tên API (chuẩn đang dùng)

- Dùng tiền tố module: `/api/<module>`
  - Ví dụ: `/api/staff`, `/api/auth`, `/api/team`
- Đặt tên path theo **resource/domain**, dùng `kebab-case` cho segment nhiều từ.
  - Ví dụ: `team-registration`, `delete-preview`, `check-in`
- Dùng HTTP method để thể hiện hành vi chính:
  - `GET`: đọc dữ liệu
  - `POST`: tạo mới
  - `PUT`: cập nhật
  - `DELETE`: xóa

## 2) Rules đặt API

- **Rule 1 - Base path theo bounded context**
  - Controller phải có base path rõ ràng theo nghiệp vụ.
  - Ví dụ: `@RequestMapping("/api/staff")`.

- **Rule 2 - Tên endpoint là danh từ trước, hành vi sau**
  - Ưu tiên `/universities`, `/criteria`, `/events`.
  - Hạn chế endpoint kiểu verb-first như `/create-university`.

- **Rule 3 - Nested resource rõ quan hệ**
  - Khi thao tác trên sub-domain, dùng nested path.
  - Ví dụ tốt: `/events/detail`, `/events/export`, `/team-registration/status`.

- **Rule 4 - Query param cho filter/search**
  - Danh sách và filter để trong query string, không nhét vào path.
  - Ví dụ: `/accounts?role=ALL&input=abc`.

- **Rule 5 - Input thay đổi trạng thái dùng body DTO**
  - Các API đổi trạng thái/patch logic nghiệp vụ nên nhận request DTO.
  - Ví dụ: `ChangeAccountStatusRequest`, `CheckInTeamRequest`.

- **Rule 6 - Trả response theo DTO rõ nghĩa**
  - Không trả raw object lẫn lộn; map thành response class.
  - Ví dụ: `EventDetailResponse`, `StaffEmailFilterResponse`.

- **Rule 7 - Giữ nhất quán động từ theo method**
  - Không dùng `GET` để update/xóa.
  - Không dùng `POST` cho endpoint chỉ đọc dữ liệu.

## 3) Ví dụ thực tế từ StaffController

Base path:

- `@RequestMapping("/api/staff")`

Ví dụ endpoint:

- `POST /api/staff/register`
  - Tạo tài khoản staff mới.
- `PUT /api/staff/change-status`
  - Đổi trạng thái tài khoản.
- `GET /api/staff/accounts?role=ALL&input=...`
  - Lấy danh sách account có filter.
- `GET /api/staff/events?status=ALL`
  - Lấy danh sách sự kiện.
- `GET /api/staff/events/detail?eventId=...`
  - Lấy chi tiết 1 event.
- `PUT /api/staff/team-registration/status`
  - Duyệt/từ chối đăng ký đội.
- `GET /api/staff/universities`
  - Lấy danh sách trường.
- `POST /api/staff/universities`
  - Tạo trường mới.
- `PUT /api/staff/universities`
  - Cập nhật trường.
- `DELETE /api/staff/universities`
  - Xóa trường.
- `GET /api/staff/criteria?roundId=...`
  - Lấy tiêu chí theo vòng.
- `PUT /api/staff/check-in/team`
  - Check-in theo team.
- `PUT /api/staff/check-in/member`
  - Check-in theo thành viên.
- `GET /api/staff/emails/filter?...`
  - Lọc email theo nhiều tiêu chí.

## 4) Gợi ý chuẩn hóa thêm (khuyến nghị)

- Với endpoint chi tiết, có thể chuẩn REST hơn bằng path variable:
  - Hiện tại: `GET /events/detail?eventId=...`
  - Khuyến nghị: `GET /events/{eventId}`
- Với update/xóa criteria:
  - Hiện tại: `PUT /criteria?criteriaId=...`, `DELETE /criteria?criteriaId=...`
  - Khuyến nghị: `PUT /criteria/{criteriaId}`, `DELETE /criteria/{criteriaId}`

Không bắt buộc đổi ngay; ưu tiên giữ backward compatibility cho frontend hiện tại.
