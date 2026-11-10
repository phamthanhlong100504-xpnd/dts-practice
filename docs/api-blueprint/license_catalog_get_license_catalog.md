# LicenseCatalog getLicenseCatalog

## Part 0 — Classification & Identity

- **API Name**: LicenseCatalog getLicenseCatalog
- **Module**: `practice`
- **Feature**: Catalogue hạng bằng dùng chung, quy tắc lý thuyết và khả năng mô phỏng
- **Authorization**: `practice:read`

## Part 1 — API Contract

- **HTTP Method**: `GET`
- **URL**: `/api/v1/exams/license-catalog`
- **Success Status**: `200`
- **Response Type**: `ApiResponse<List<LicenseCatalogItemResponse>>`

### Headers

| Name | Required | Description |
| --- | --- | --- |
| Authorization | Có | Bearer access JWT hợp lệ của Identity |
| Content-Type | Khi có body | application/json |

JWT claim `permissions` chứa tên thuần `practice:read`; authority kiểm tra tại controller là `PERM_practice:read`.
Permission không mở quyền truy cập dữ liệu cá nhân người khác. API có principal luôn lấy userId từ JWT sub.

### Response và lỗi

Các field lý thuyết giữ nguyên. Card 01 thêm `simulationEnabled:boolean`, `simulationReady:boolean`, `simulationBankVersion:string|null`; xem [catalogue mô phỏng](simulation_catalog.md) về tập hạng, nguồn PUBLISHED và cờ rollout mặc định tắt. Xem [DTO reference](dto_reference.md) về các field lý thuyết.

| Trường hợp | HTTP | Error contract |
| --- | --- | --- |
| JWT thiếu, hết hạn, sai issuer/chữ ký | 401 | Security handler; không bắt buộc có code |
| Đã xác thực nhưng thiếu quyền hoặc role import | 403 | ACCESS_DENIED tại method security |
| Tài nguyên/phiên không thuộc principal | Theo source hiện có | Giữ 404/NOT_FOUND khi service tra theo userId |
| Body/binding hoặc nghiệp vụ sai | Theo handler hiện có | Không tự đổi mã lỗi hoặc ép mọi input sai thành cùng mã |

Method security chạy trước service nghiệp vụ, sau bước bind/validate argument của Spring MVC. Payload sai có thể bị trả lỗi validation trước kiểm tra permission; bộ test thiếu quyền dùng request hợp lệ.

## Part 2 — Processing Specification

1. **Security Layer**: Giữ nguyên JwtValidationFilter và SecurityConfig. Chỉ dùng claims đã được xác minh, không tin X-User-* do client tự đặt.
2. **Controller Layer**: Kiểm `hasAuthority(PERM_practice:read)` trước gọi service.
3. **Service Layer**: Gọi [LicenseCatalogService.getCatalog](../../src/main/java/com/dts/practice/service/LicenseCatalogService.java), giữ nguyên logic lý thuyết; `SimulationLicenseCapabilities` bổ sung ba field mô phỏng từ nguồn PUBLISHED. Lỗi nguồn mô phỏng không làm mất catalogue lý thuyết.
4. **Ownership**: Dữ liệu cá nhân đi theo userId từ principal; các session được service tra theo cả id và user_id. Catalog/reference dùng nguồn đã publish/current bank như trước.

## Part 3 — Data Interaction

- Không thay query, transaction, chấm điểm lý thuyết, timer, pause/resume, đáp án phản hồi, receipt hoặc outbox; DTO thêm ba field mô phỏng.
- Request bị từ chối quyền không gọi service nghiệp vụ và không thực hiện import, lưu đáp án, đổi cursor/flag hoặc phát sự kiện.
- Chi tiết nguồn bảng, snapshot và sự kiện theo tài liệu nghiệp vụ liên kết và service nguồn.
- Các transaction và kiểm tra dữ liệu hiện có được giữ nguyên.

## Part 4 — Operational Notes

- Baseline kế thừa `2bc7fd78e8d41e8070da3ef924ec8989aa3dd9d0`; branch `feat/simulation-practice`.
- Không đổi JWT filter, schema/migration V1–V30 và V27.1, service/worker hoặc nguồn dữ liệu lý thuyết.
- Quyền mới cần được cấp tại Identity qua migration do người phụ trách Identity thực hiện; không sửa Identity trong task Practice. Sau cấp quyền phải lấy access JWT mới.
- Quy ước quyền và tích hợp Progress: [authorization.md](authorization.md).
- Controller nguồn: [LicenseCatalogController](../../src/main/java/com/dts/practice/controller/LicenseCatalogController.java).

Nghiệp vụ chi tiết: [exam_sessions.md](exam_sessions.md).
