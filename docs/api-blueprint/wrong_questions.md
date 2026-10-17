# Danh sách câu sai và vòng ôn câu sai

## Part 0 — Phạm vi và nguồn

Tài liệu theo source local đã thu thập ngày 17/10/2026, trên nhánh `feature/2026-license-practice-exams`. Mô tả cả API mới và API giữ route nhưng thay đổi nghiệp vụ so với `main`. Chi tiết DTO dùng chung ở [dto_reference.md](dto_reference.md); quy ước lỗi/xác thực ở [contract_notes.md](contract_notes.md).

## Part 1 — Nghiệp vụ

Danh sách câu sai dùng trạng thái chuẩn theo người, hạng và ngân hàng; dữ liệu lịch sử câu trả lời không bị đổi thành dữ liệu ôn mới. Service đồng bộ các câu đã trả lời từ bài luyện/thi hoàn thành và tương tác học để xác định phạm vi câu sai.

Vòng ôn lưu snapshot tập câu, đáp án và cursor theo sessionId. Start có thể trả vòng đang học; answer/cursor/finish dùng đúng ID đó. Finish lặp trên vòng hoàn thành trả lại trạng thái đã lưu, không tạo một bài thi EXAM hay thay thế lịch sử gốc.

## Part 2 — Endpoint và request

### `GET /api/v1/questions/wrong/overview`

- HTTP thành công: **200**; response Java: `ApiResponse<WrongQuestionOverviewResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/WrongQuestionController.java`](../../src/main/java/com/dts/practice/controller/WrongQuestionController.java), method `getOverview`.

Count distinct questions answered incorrectly

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| RequestParam | `licenseClass` | `String` | Bắt buộc |

DTO: `WrongQuestionOverviewResponse`.

### `GET /api/v1/questions/wrong`

- HTTP thành công: **200**; response Java: `ApiResponse<List<WrongQuestionResponse>>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/WrongQuestionController.java`](../../src/main/java/com/dts/practice/controller/WrongQuestionController.java), method `getWrongQuestions`.

List distinct questions answered incorrectly

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| RequestParam | `licenseClass` | `String` | Bắt buộc |

DTO: `WrongQuestionResponse`.

### `POST /api/v1/wrong-questions/sessions`

- HTTP thành công: **200**; response Java: `ApiResponse<WrongReviewSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/WrongReviewSessionController.java`](../../src/main/java/com/dts/practice/controller/WrongReviewSessionController.java), method `start`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| RequestBody | `request` | `StartWrongReviewRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `StartWrongReviewRequest`, `WrongReviewSessionResponse`.

### `GET /api/v1/wrong-questions/sessions/{sessionId}`

- HTTP thành công: **200**; response Java: `ApiResponse<WrongReviewSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/WrongReviewSessionController.java`](../../src/main/java/com/dts/practice/controller/WrongReviewSessionController.java), method `get`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `sessionId` | `UUID` | Bắt buộc |

DTO: `WrongReviewSessionResponse`.

### `POST /api/v1/wrong-questions/sessions/{sessionId}/answers`

- HTTP thành công: **200**; response Java: `ApiResponse<WrongReviewAnswerResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/WrongReviewSessionController.java`](../../src/main/java/com/dts/practice/controller/WrongReviewSessionController.java), method `submitAnswer`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `sessionId` | `UUID` | Bắt buộc |
| RequestBody | `request` | `SubmitWrongReviewAnswerRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `SubmitWrongReviewAnswerRequest`, `WrongReviewAnswerResponse`.

### `PUT /api/v1/wrong-questions/sessions/{sessionId}/cursor`

- HTTP thành công: **200**; response Java: `ApiResponse<WrongReviewSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/WrongReviewSessionController.java`](../../src/main/java/com/dts/practice/controller/WrongReviewSessionController.java), method `saveCursor`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `sessionId` | `UUID` | Bắt buộc |
| RequestBody | `request` | `SaveWrongReviewCursorRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `SaveWrongReviewCursorRequest`, `WrongReviewSessionResponse`.

### `POST /api/v1/wrong-questions/sessions/{sessionId}/finish`

- HTTP thành công: **200**; response Java: `ApiResponse<WrongReviewSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/WrongReviewSessionController.java`](../../src/main/java/com/dts/practice/controller/WrongReviewSessionController.java), method `finish`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `sessionId` | `UUID` | Bắt buộc |

DTO: `WrongReviewSessionResponse`.

## Part 3 — Luồng nối giao diện

Đọc `data` trong ApiResponse sau khi kiểm tra HTTP status và `success`. Với dữ liệu cá nhân, truyền đúng token phiên đăng nhập; không gửi userId để chọn người khác. Dùng trạng thái/cursor/ID backend trả về để phục hồi giao diện. Các request ghi trạng thái phải dùng chính ID câu/phiên trong response của luồng tương ứng.

## Part 4 — Lưu trữ và triển khai

Các service và bảng liên quan được tổng hợp trong [danh mục chức năng](../PRACTICE_FEATURES.md) và [migration catalog](../db/MIGRATION_CATALOG.md). Chạy migration theo thứ tự version, giữ nguyên checksum. Xem [LOCAL_SETUP.md](../LOCAL_SETUP.md) để cấu hình DB/Kafka/Progress và nhập dữ liệu. Tài liệu này không tạo endpoint hay thay đổi runtime.
