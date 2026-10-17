# Bộ đề cố định

## Part 0 — Phạm vi và nguồn

Tài liệu theo source local đã thu thập ngày 17/10/2026, trên nhánh `feature/2026-license-practice-exams`. Mô tả cả API mới và API giữ route nhưng thay đổi nghiệp vụ so với `main`. Chi tiết DTO dùng chung ở [dto_reference.md](dto_reference.md); quy ước lỗi/xác thực ở [contract_notes.md](contract_notes.md).

## Part 1 — Nghiệp vụ

Danh sách chỉ trả bộ đề PUBLISHED của hạng và ngân hàng hiện tại, kèm tiến độ/lượt gần nhất theo người đang đăng nhập. Trạng thái giao diện gồm NOT_STARTED, IN_PROGRESS, PASSED, FAILED; trạng thái phiên chi tiết vẫn là IN_PROGRESS/PAUSED/COMPLETED/TIMEOUT.

Start preset nhận body tùy chọn, mặc định EXAM. Cùng preset và mode có thể tiếp tục phiên đang hoạt động; xung đột mode hoặc phiên khác được backend từ chối. Bộ đề được kiểm tra đúng cấu trúc, quota chương và câu điểm liệt trước khi tạo phiên. Sau khi có examId, dùng chung API answers/flags/cursor/pause/resume/finish/result của Exam.

Migration V13 tạo cấu trúc; `data/preset-exams/2026/sets.json` và công cụ publish tạo dữ liệu bộ đề. Pull source hoặc chạy migration không tự xuất bản các bộ đề trong manifest.

## Part 2 — Endpoint và request

### `GET /api/v1/exams/presets`

- HTTP thành công: **200**; response Java: `ApiResponse<PresetExamSetListResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/PresetExamSetController.java`](../../src/main/java/com/dts/practice/controller/PresetExamSetController.java), method `list`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| RequestParam | `licenseClass` | `String` | Bắt buộc |

DTO: `PresetExamSetListResponse`.

### `POST /api/v1/exams/presets/{presetSetId}/sessions`

- HTTP thành công: **200**; response Java: `ApiResponse<ExamSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/PresetExamSetController.java`](../../src/main/java/com/dts/practice/controller/PresetExamSetController.java), method `startSession`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `presetSetId` | `UUID` | Bắt buộc |
| RequestBody | `request` | `StartPresetExamRequest` | Tùy chọn |

DTO: `ExamSessionResponse`, `StartPresetExamRequest`.

## Part 3 — Luồng nối giao diện

Đọc `data` trong ApiResponse sau khi kiểm tra HTTP status và `success`. Với dữ liệu cá nhân, truyền đúng token phiên đăng nhập; không gửi userId để chọn người khác. Dùng trạng thái/cursor/ID backend trả về để phục hồi giao diện. Các request ghi trạng thái phải dùng chính ID câu/phiên trong response của luồng tương ứng.

## Part 4 — Lưu trữ và triển khai

Các service và bảng liên quan được tổng hợp trong [danh mục chức năng](../PRACTICE_FEATURES.md) và [migration catalog](../db/MIGRATION_CATALOG.md). Chạy migration theo thứ tự version, giữ nguyên checksum. Xem [LOCAL_SETUP.md](../LOCAL_SETUP.md) để cấu hình DB/Kafka/Progress và nhập dữ liệu. Tài liệu này không tạo endpoint hay thay đổi runtime.
