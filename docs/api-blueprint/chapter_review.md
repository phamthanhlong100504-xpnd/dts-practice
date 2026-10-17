# Vòng ôn tập theo chương

## Part 0 — Phạm vi và nguồn

Tài liệu theo source local đã thu thập ngày 17/10/2026, trên nhánh `feature/2026-license-practice-exams`. Mô tả cả API mới và API giữ route nhưng thay đổi nghiệp vụ so với `main`. Chi tiết DTO dùng chung ở [dto_reference.md](dto_reference.md); quy ước lỗi/xác thực ở [contract_notes.md](contract_notes.md).

## Part 1 — Nghiệp vụ

Vòng ôn tập có sessionId, danh sách câu và đáp án riêng được lưu trong PostgreSQL. Tạo/lấy phiên, gửi đáp án, cập nhật cursor và finish qua cùng chapterId. Các kiểm tra chủ sở hữu và chương thuộc về service; không lấy ID phiên từ tài khoản khác.

Chỉ mở vòng sau khi đã học đủ câu của chương; nếu chưa đủ service trả409 `CHAPTER_LEARNING_INCOMPLETE`.

Vòng ôn là luồng riêng với học tích lũy theo chương; không dùng endpoint Exam để kết thúc vòng này. DTO ChapterReviewSessionResponse là nguồn trạng thái và kết quả của vòng.

## Part 2 — Endpoint và request

### `POST /api/v1/learning/chapters/{chapterId}/review-sessions`

- HTTP thành công: **200**; response Java: `ApiResponse<ChapterReviewSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ChapterReviewSessionController.java`](../../src/main/java/com/dts/practice/controller/ChapterReviewSessionController.java), method `start`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `chapterId` | `int` | Bắt buộc |
| RequestBody | `request` | `StartChapterReviewRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ChapterReviewSessionResponse`, `StartChapterReviewRequest`.

### `GET /api/v1/learning/chapters/{chapterId}/review-sessions/{sessionId}`

- HTTP thành công: **200**; response Java: `ApiResponse<ChapterReviewSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ChapterReviewSessionController.java`](../../src/main/java/com/dts/practice/controller/ChapterReviewSessionController.java), method `get`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `chapterId` | `int` | Bắt buộc |
| PathVariable | `sessionId` | `UUID` | Bắt buộc |

DTO: `ChapterReviewSessionResponse`.

### `POST /api/v1/learning/chapters/{chapterId}/review-sessions/{sessionId}/answers`

- HTTP thành công: **200**; response Java: `ApiResponse<ChapterReviewSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ChapterReviewSessionController.java`](../../src/main/java/com/dts/practice/controller/ChapterReviewSessionController.java), method `answer`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `chapterId` | `int` | Bắt buộc |
| PathVariable | `sessionId` | `UUID` | Bắt buộc |
| RequestBody | `request` | `SubmitChapterReviewAnswerRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ChapterReviewSessionResponse`, `SubmitChapterReviewAnswerRequest`.

### `PUT /api/v1/learning/chapters/{chapterId}/review-sessions/{sessionId}/cursor`

- HTTP thành công: **200**; response Java: `ApiResponse<ChapterReviewSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ChapterReviewSessionController.java`](../../src/main/java/com/dts/practice/controller/ChapterReviewSessionController.java), method `cursor`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `chapterId` | `int` | Bắt buộc |
| PathVariable | `sessionId` | `UUID` | Bắt buộc |
| RequestBody | `request` | `SaveChapterReviewCursorRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ChapterReviewSessionResponse`, `SaveChapterReviewCursorRequest`.

### `POST /api/v1/learning/chapters/{chapterId}/review-sessions/{sessionId}/finish`

- HTTP thành công: **200**; response Java: `ApiResponse<ChapterReviewSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ChapterReviewSessionController.java`](../../src/main/java/com/dts/practice/controller/ChapterReviewSessionController.java), method `finish`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `chapterId` | `int` | Bắt buộc |
| PathVariable | `sessionId` | `UUID` | Bắt buộc |

DTO: `ChapterReviewSessionResponse`.

## Part 3 — Luồng nối giao diện

Đọc `data` trong ApiResponse sau khi kiểm tra HTTP status và `success`. Với dữ liệu cá nhân, truyền đúng token phiên đăng nhập; không gửi userId để chọn người khác. Dùng trạng thái/cursor/ID backend trả về để phục hồi giao diện. Các request ghi trạng thái phải dùng chính ID câu/phiên trong response của luồng tương ứng.

## Part 4 — Lưu trữ và triển khai

Các service và bảng liên quan được tổng hợp trong [danh mục chức năng](../PRACTICE_FEATURES.md) và [migration catalog](../db/MIGRATION_CATALOG.md). Chạy migration theo thứ tự version, giữ nguyên checksum. Xem [LOCAL_SETUP.md](../LOCAL_SETUP.md) để cấu hình DB/Kafka/Progress và nhập dữ liệu. Tài liệu này không tạo endpoint hay thay đổi runtime.
