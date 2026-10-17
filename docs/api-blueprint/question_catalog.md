# Ngân hàng câu hỏi và cấu hình hạng bằng

## Part 0 — Phạm vi và nguồn

Tài liệu theo source local đã thu thập ngày 17/10/2026, trên nhánh `feature/2026-license-practice-exams`. Mô tả cả API mới và API giữ route nhưng thay đổi nghiệp vụ so với `main`. Chi tiết DTO dùng chung ở [dto_reference.md](dto_reference.md); quy ước lỗi/xác thực ở [contract_notes.md](contract_notes.md).

## Part 1 — Nghiệp vụ

Ngân hàng hiện tại lấy từ `CurrentQuestionBank.currentVersion()` = `dts-2026-600-v1`. ID câu hỏi trong API Practice là `questions.id` dạng số nguyên; không thay bằng UUID của Content Builder hoặc số thứ tự câu trong ngân hàng. `sourceQuestionId` dùng để đối chiếu nguồn import.

`licenseClass` lọc phạm vi câu hỏi và câu điểm liệt theo từng hạng. Dùng `/api/v1/exams/license-catalog` và `/api/v1/exams/config` để lấy cấu hình backend, không hardcode 25 câu/20 phút cho mọi hạng. Learning catalog lấy danh mục chương từ bảng phiên bản ngân hàng; import câu hỏi và migration V25 phối hợp tạo dữ liệu này.

Import là API ADMIN, khác API đọc dành cho học viên. Nhập đúng bankVersion và đủ metadata/option; phần kiểm tra và lưu nằm trong QuestionBankImportService. Các API đọc câu hỏi phục vụ học có thể chứa đáp án; màn thi EXAM phải dùng session API để tuân thủ chính sách ẩn đáp án.

## Part 2 — Endpoint và request

### `GET /api/v1/exams/config`

- HTTP thành công: **200**; response Java: `ApiResponse<ExamConfigResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamConfigController.java`](../../src/main/java/com/dts/practice/controller/ExamConfigController.java), method `getConfig`.

Get the canonical exam rules for a license class

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| RequestParam | `examType` | `String` | Bắt buộc |

DTO: `ExamConfigResponse`.

### `GET /api/v1/questions/learning-catalog`

- HTTP thành công: **200**; response Java: `ApiResponse<LearningBankCatalogResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/LearningBankCatalogController.java`](../../src/main/java/com/dts/practice/controller/LearningBankCatalogController.java), method `get`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| RequestParam | `licenseClass` | `String` | Bắt buộc |

DTO: `LearningBankCatalogResponse`.

### `GET /api/v1/exams/license-catalog`

- HTTP thành công: **200**; response Java: `ApiResponse<List<LicenseCatalogItemResponse>>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/LicenseCatalogController.java`](../../src/main/java/com/dts/practice/controller/LicenseCatalogController.java), method `getLicenseCatalog`.

List license classes, canonical exam rules and current bank counts

Không có query/body do controller khai báo.

DTO: `LicenseCatalogItemResponse`.

### `POST /api/v1/questions/admin/import-bank`

- HTTP thành công: **201**; response Java: `ApiResponse<Map<String, Object>>`.
- Quyền: **ADMIN**.
- Controller: [`src/main/java/com/dts/practice/controller/QuestionBankImportController.java`](../../src/main/java/com/dts/practice/controller/QuestionBankImportController.java), method `importBank`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| RequestBody | `request` | `ImportQuestionBankRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ImportQuestionBankRequest`.

### `GET /api/v1/questions/chapter/{chapter}`

- HTTP thành công: **200**; response Java: `ApiResponse<List<QuestionResponse>>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/QuestionController.java`](../../src/main/java/com/dts/practice/controller/QuestionController.java), method `getByChapter`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| PathVariable | `chapter` | `Integer` | Bắt buộc |
| RequestParam | `licenseClass` | `String` | Tùy chọn |

DTO: `QuestionResponse`.

### `GET /api/v1/questions/{id}`

- HTTP thành công: **200**; response Java: `ApiResponse<QuestionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/QuestionController.java`](../../src/main/java/com/dts/practice/controller/QuestionController.java), method `getById`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| PathVariable | `id` | `Integer` | Bắt buộc |
| RequestParam | `licenseClass` | `String` | Tùy chọn |

DTO: `QuestionResponse`.

### `GET /api/v1/questions/critical`

- HTTP thành công: **200**; response Java: `ApiResponse<List<QuestionResponse>>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/QuestionController.java`](../../src/main/java/com/dts/practice/controller/QuestionController.java), method `getCritical`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| RequestParam | `licenseClass` | `String` | Tùy chọn |

DTO: `QuestionResponse`.

### `GET /api/v1/questions/stats`

- HTTP thành công: **200**; response Java: `ApiResponse<Map<String, Object>>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/QuestionController.java`](../../src/main/java/com/dts/practice/controller/QuestionController.java), method `getStats`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| RequestParam | `licenseClass` | `String` | Tùy chọn |

## Part 3 — Luồng nối giao diện

Đọc `data` trong ApiResponse sau khi kiểm tra HTTP status và `success`. Với dữ liệu cá nhân, truyền đúng token phiên đăng nhập; không gửi userId để chọn người khác. Dùng trạng thái/cursor/ID backend trả về để phục hồi giao diện. Các request ghi trạng thái phải dùng chính ID câu/phiên trong response của luồng tương ứng.

## Part 4 — Lưu trữ và triển khai

Các service và bảng liên quan được tổng hợp trong [danh mục chức năng](../PRACTICE_FEATURES.md) và [migration catalog](../db/MIGRATION_CATALOG.md). Chạy migration theo thứ tự version, giữ nguyên checksum. Xem [LOCAL_SETUP.md](../LOCAL_SETUP.md) để cấu hình DB/Kafka/Progress và nhập dữ liệu. Tài liệu này không tạo endpoint hay thay đổi runtime.
