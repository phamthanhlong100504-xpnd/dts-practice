# Học theo chương, bookmark và câu đã học

## Part 0 — Phạm vi và nguồn

Tài liệu theo source local đã thu thập ngày 17/10/2026, trên nhánh `feature/2026-license-practice-exams`. Mô tả cả API mới và API giữ route nhưng thay đổi nghiệp vụ so với `main`. Chi tiết DTO dùng chung ở [dto_reference.md](dto_reference.md); quy ước lỗi/xác thực ở [contract_notes.md](contract_notes.md).

## Part 1 — Nghiệp vụ

Tiến độ phân vùng theo userId, licenseClass, bankVersion và questionId. Tổng số câu/chương được lấy từ ngân hàng; client gửi tương tác câu cụ thể, không gửi bộ đếm để ghi đè tổng tiến độ.

POST answer chấm theo câu hỏi hiện hành, cập nhật tiến độ chuẩn và dữ liệu câu sai; sự kiện Progress được ghi bền trong learning_progress_outbox cùng transaction. Cursor ghi vị trí tiếp tục học. Bookmark độc lập với câu đã trả lời, có thể lưu trước khi học.

API answers trả đáp án đã lưu của người đang đăng nhập để phục hồi giao diện. Không dùng localStorage làm nguồn tiến độ toàn hệ thống. Sự kiện EXP bổ sung của V30 lấy từ tương tác chuẩn sau thời điểm bật capture; không biến một câu trả lời lặp thành nhiều câu học mới trong Result.

## Part 2 — Endpoint và request

### `GET /api/v1/learning/chapters/{chapterId}/bookmarks`

- HTTP thành công: **200**; response Java: `ApiResponse<ChapterBookmarksResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ChapterBookmarkController.java`](../../src/main/java/com/dts/practice/controller/ChapterBookmarkController.java), method `list`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `chapterId` | `int` | Bắt buộc |
| RequestParam | `licenseClass` | `String` | Bắt buộc |

DTO: `ChapterBookmarksResponse`.

### `PUT /api/v1/learning/chapters/{chapterId}/bookmarks/{questionId}`

- HTTP thành công: **200**; response Java: `ApiResponse<ChapterBookmarkResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ChapterBookmarkController.java`](../../src/main/java/com/dts/practice/controller/ChapterBookmarkController.java), method `set`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `chapterId` | `int` | Bắt buộc |
| PathVariable | `questionId` | `Integer` | Bắt buộc |
| RequestBody | `request` | `SetChapterBookmarkRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ChapterBookmarkResponse`, `SetChapterBookmarkRequest`.

### `GET /api/v1/learning/chapters/{chapterId}/answers`

- HTTP thành công: **200**; response Java: `ApiResponse<List<ChapterLearnedAnswerResponse>>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ChapterLearnedAnswerQueryController.java`](../../src/main/java/com/dts/practice/controller/ChapterLearnedAnswerQueryController.java), method `getAnswers`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `chapterId` | `int` | Bắt buộc |
| RequestParam | `licenseClass` | `String` | Bắt buộc |

DTO: `ChapterLearnedAnswerResponse`.

### `GET /api/v1/learning/chapters`

- HTTP thành công: **200**; response Java: `ApiResponse<ChapterLearningOverviewResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ChapterLearningController.java`](../../src/main/java/com/dts/practice/controller/ChapterLearningController.java), method `overview`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| RequestParam | `licenseClass` | `String` | Bắt buộc |

DTO: `ChapterLearningOverviewResponse`.

### `POST /api/v1/learning/chapters/{chapterId}/answers`

- HTTP thành công: **200**; response Java: `ApiResponse<ChapterLearningAnswerResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ChapterLearningController.java`](../../src/main/java/com/dts/practice/controller/ChapterLearningController.java), method `answer`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `chapterId` | `int` | Bắt buộc |
| RequestBody | `request` | `RecordChapterAnswerRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ChapterLearningAnswerResponse`, `RecordChapterAnswerRequest`.

### `PUT /api/v1/learning/chapters/{chapterId}/cursor`

- HTTP thành công: **200**; response Java: `ApiResponse<ChapterLearningCursorResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ChapterLearningController.java`](../../src/main/java/com/dts/practice/controller/ChapterLearningController.java), method `saveCursor`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `chapterId` | `int` | Bắt buộc |
| RequestBody | `request` | `SaveChapterCursorRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ChapterLearningCursorResponse`, `SaveChapterCursorRequest`.

## Part 3 — Luồng nối giao diện

Đọc `data` trong ApiResponse sau khi kiểm tra HTTP status và `success`. Với dữ liệu cá nhân, truyền đúng token phiên đăng nhập; không gửi userId để chọn người khác. Dùng trạng thái/cursor/ID backend trả về để phục hồi giao diện. Các request ghi trạng thái phải dùng chính ID câu/phiên trong response của luồng tương ứng.

## Part 4 — Lưu trữ và triển khai

Các service và bảng liên quan được tổng hợp trong [danh mục chức năng](../PRACTICE_FEATURES.md) và [migration catalog](../db/MIGRATION_CATALOG.md). Chạy migration theo thứ tự version, giữ nguyên checksum. Xem [LOCAL_SETUP.md](../LOCAL_SETUP.md) để cấu hình DB/Kafka/Progress và nhập dữ liệu. Tài liệu này không tạo endpoint hay thay đổi runtime.
