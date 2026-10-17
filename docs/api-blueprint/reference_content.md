# Biển báo, mẹo ghi nhớ và import tham chiếu

## Part 0 — Phạm vi và nguồn

Tài liệu theo source local đã thu thập ngày 17/10/2026, trên nhánh `feature/2026-license-practice-exams`. Mô tả cả API mới và API giữ route nhưng thay đổi nghiệp vụ so với `main`. Chi tiết DTO dùng chung ở [dto_reference.md](dto_reference.md); quy ước lỗi/xác thực ở [contract_notes.md](contract_notes.md).

## Part 1 — Nghiệp vụ

Practice giữ bản sao đọc cho học viên, Content Builder quản lý nội dung gốc. Traffic sign dùng bank `dts-traffic-signs-v1`; memory tip dùng `dts-2026-600-v1`. Chỉ nội dung PUBLISHED của bank hiện hành được phục vụ.

List biển báo hỗ trợ group=cam/nguy_hiem/hieu_lenh/chi_dan/phu, tìm q theo mã hoặc tên không phân biệt hoa thường/dấu tiếng Việt, page từ 0, size từ 1 đến 100 (mặc định 20), thứ tự ổn định. Detail biển báo dùng UUID chuẩn. Memory tips trả toàn bộ danh sách theo thứ tự ổn định, không có endpoint detail riêng trong Practice hiện tại.

Hai API đọc dùng Cache-Control: no-store. Import là POST ADMIN riêng, không phải create/edit nội dung tùy ý của học viên. Công cụ sync_reference_content_2026.py đọc và kiểm tra nguồn trước; apply tách từng kind signs/tips. Migration V27 chỉ tạo bảng, dữ liệu published được import sau.

## Part 2 — Endpoint và request

### `GET /api/v1/memory-tips`

- HTTP thành công: **200**; response Java: `ResponseEntity<ApiResponse<MemoryTipListResponse>>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/MemoryTipController.java`](../../src/main/java/com/dts/practice/controller/MemoryTipController.java), method `list`.

List all published memory tips from the current shared bank

Không có query/body do controller khai báo.

DTO: `MemoryTipListResponse`.

### `POST /api/v1/reference-content/admin/import-traffic-signs`

- HTTP thành công: **201**; response Java: `ApiResponse<Map<String, Object>>`.
- Quyền: **ADMIN**.
- Controller: [`src/main/java/com/dts/practice/controller/ReferenceContentBankImportController.java`](../../src/main/java/com/dts/practice/controller/ReferenceContentBankImportController.java), method `importTrafficSigns`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| RequestBody | `request` | `ImportTrafficSignBankRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ImportTrafficSignBankRequest`.

### `POST /api/v1/reference-content/admin/import-memory-tips`

- HTTP thành công: **201**; response Java: `ApiResponse<Map<String, Object>>`.
- Quyền: **ADMIN**.
- Controller: [`src/main/java/com/dts/practice/controller/ReferenceContentBankImportController.java`](../../src/main/java/com/dts/practice/controller/ReferenceContentBankImportController.java), method `importMemoryTips`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| RequestBody | `request` | `ImportMemoryTipBankRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ImportMemoryTipBankRequest`.

### `GET /api/v1/traffic-signs`

- HTTP thành công: **200**; response Java: `ResponseEntity<ApiResponse<TrafficSignListResponse>>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/TrafficSignController.java`](../../src/main/java/com/dts/practice/controller/TrafficSignController.java), method `list`.

List traffic signs with group filtering, Vietnamese search, and stable pagination

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| RequestParam | `group` | `String` | Tùy chọn |
| RequestParam | `q` | `String` | Tùy chọn |
| RequestParam | `page` | `String` | Tùy chọn; mặc định `0` |
| RequestParam | `size` | `String` | Tùy chọn; mặc định `20` |

DTO: `TrafficSignListResponse`.

### `GET /api/v1/traffic-signs/{id}`

- HTTP thành công: **200**; response Java: `ResponseEntity<ApiResponse<TrafficSignResponse>>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/TrafficSignController.java`](../../src/main/java/com/dts/practice/controller/TrafficSignController.java), method `get`.

Get a published traffic sign from the current bank

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| PathVariable | `id` | `String` | Bắt buộc |

DTO: `TrafficSignResponse`.

## Part 3 — Luồng nối giao diện

Đọc `data` trong ApiResponse sau khi kiểm tra HTTP status và `success`. Với dữ liệu cá nhân, truyền đúng token phiên đăng nhập; không gửi userId để chọn người khác. Dùng trạng thái/cursor/ID backend trả về để phục hồi giao diện. Các request ghi trạng thái phải dùng chính ID câu/phiên trong response của luồng tương ứng.

## Part 4 — Lưu trữ và triển khai

Các service và bảng liên quan được tổng hợp trong [danh mục chức năng](../PRACTICE_FEATURES.md) và [migration catalog](../db/MIGRATION_CATALOG.md). Chạy migration theo thứ tự version, giữ nguyên checksum. Xem [LOCAL_SETUP.md](../LOCAL_SETUP.md) để cấu hình DB/Kafka/Progress và nhập dữ liệu. Tài liệu này không tạo endpoint hay thay đổi runtime.
