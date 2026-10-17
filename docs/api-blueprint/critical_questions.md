# Học và ôn câu điểm liệt

## Part 0 — Phạm vi và nguồn

Tài liệu theo source local đã thu thập ngày 17/10/2026, trên nhánh `feature/2026-license-practice-exams`. Mô tả cả API mới và API giữ route nhưng thay đổi nghiệp vụ so với `main`. Chi tiết DTO dùng chung ở [dto_reference.md](dto_reference.md); quy ước lỗi/xác thực ở [contract_notes.md](contract_notes.md).

## Part 1 — Nghiệp vụ

Câu điểm liệt được lọc đúng hạng và ngân hàng. Tiến độ học tích lũy và đáp án trong mỗi vòng được lưu riêng.

Start nhận mode CONTINUE hoặc REVIEW. REVIEW chỉ được mở khi hoàn tất phần học điểm liệt; restart chỉ hỗ trợ REVIEW và có thể đánh dấu vòng trước là SUPERSEDED. Nếu đang có vòng khác mode, service trả conflict thay vì ghi đè.

Gửi đáp án và cursor bằng sessionId chính chủ. API PUT progress giữ tương thích cho đánh dấu learned; không đồng nhất learned với một bộ kết quả thi EXAM. Trạng thái/vị trí/feedback của session là nguồn để giao diện tiếp tục vòng sau refresh.

## Part 2 — Endpoint và request

### `POST /api/v1/questions/critical/answers`

- HTTP thành công: **200**; response Java: `ApiResponse<CriticalAnswerResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/CriticalAnswerController.java`](../../src/main/java/com/dts/practice/controller/CriticalAnswerController.java), method `submitAnswer`.

Answer a question in the current critical-question round

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| RequestBody | `request` | `SubmitCriticalAnswerRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `CriticalAnswerResponse`, `SubmitCriticalAnswerRequest`.

### `PUT /api/v1/questions/critical/sessions/{sessionId}/cursor`

- HTTP thành công: **200**; response Java: `ApiResponse<CriticalSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/CriticalCursorController.java`](../../src/main/java/com/dts/practice/controller/CriticalCursorController.java), method `saveCursor`.

Save the current question in a critical-question round

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `sessionId` | `UUID` | Bắt buộc |
| RequestBody | `request` | `UpdateCriticalCursorRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `CriticalSessionResponse`, `UpdateCriticalCursorRequest`.

### `GET /api/v1/questions/critical/overview`

- HTTP thành công: **200**; response Java: `ApiResponse<CriticalProgressOverviewResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/CriticalQuestionProgressController.java`](../../src/main/java/com/dts/practice/controller/CriticalQuestionProgressController.java), method `getOverview`.

Get critical question learning progress

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| RequestParam | `licenseClass` | `String` | Bắt buộc |

DTO: `CriticalProgressOverviewResponse`.

### `PUT /api/v1/questions/critical/{questionId}/progress`

- HTTP thành công: **200**; response Java: `ApiResponse<CriticalProgressOverviewResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/CriticalQuestionProgressController.java`](../../src/main/java/com/dts/practice/controller/CriticalQuestionProgressController.java), method `updateProgress`.

Mark a critical question as learned or not learned

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `questionId` | `Integer` | Bắt buộc |
| RequestBody | `request` | `UpdateCriticalProgressRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `CriticalProgressOverviewResponse`, `UpdateCriticalProgressRequest`.

### `POST /api/v1/questions/critical/sessions`

- HTTP thành công: **200**; response Java: `ApiResponse<CriticalSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/CriticalSessionController.java`](../../src/main/java/com/dts/practice/controller/CriticalSessionController.java), method `start`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| RequestBody | `request` | `StartCriticalSessionRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `CriticalSessionResponse`, `StartCriticalSessionRequest`.

### `GET /api/v1/questions/critical/sessions/{sessionId}`

- HTTP thành công: **200**; response Java: `ApiResponse<CriticalSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/CriticalSessionController.java`](../../src/main/java/com/dts/practice/controller/CriticalSessionController.java), method `get`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `sessionId` | `UUID` | Bắt buộc |

DTO: `CriticalSessionResponse`.

## Part 3 — Luồng nối giao diện

Đọc `data` trong ApiResponse sau khi kiểm tra HTTP status và `success`. Với dữ liệu cá nhân, truyền đúng token phiên đăng nhập; không gửi userId để chọn người khác. Dùng trạng thái/cursor/ID backend trả về để phục hồi giao diện. Các request ghi trạng thái phải dùng chính ID câu/phiên trong response của luồng tương ứng.

## Part 4 — Lưu trữ và triển khai

Các service và bảng liên quan được tổng hợp trong [danh mục chức năng](../PRACTICE_FEATURES.md) và [migration catalog](../db/MIGRATION_CATALOG.md). Chạy migration theo thứ tự version, giữ nguyên checksum. Xem [LOCAL_SETUP.md](../LOCAL_SETUP.md) để cấu hình DB/Kafka/Progress và nhập dữ liệu. Tài liệu này không tạo endpoint hay thay đổi runtime.
