# Home home

## Part 0 — Classification & Identity

- **API Name**: Home home
- **Module**: `practice`
- **Feature**: Thông tin trạng thái service
- **Authorization**: `Public`

## Part 1 — API Contract

- **HTTP Method**: `GET`
- **URL**: `/`
- **Success Status**: `200`
- **Response Type**: `Map<String, Object>`

### Response và lỗi

Request/response, ID integer/UUID, đơn vị điểm và các field nullable giữ nguyên. Xem [DTO reference](dto_reference.md) và tài liệu nghiệp vụ liên kết.

## Part 2 — Processing Specification

1. **Security Layer**: Cho phép home như hiện tại.
2. **Controller Layer**: Trả thông tin service.
3. **Service Layer**: Trả thông tin service, không gọi service nghiệp vụ.
4. **Ownership**: Không có dữ liệu cá nhân.

## Part 3 — Data Interaction

- Không thay query, transaction, DTO, chấm điểm, timer, pause/resume, đáp án phản hồi, receipt hoặc outbox trong bản vá này.
- Request bị từ chối quyền không gọi service nghiệp vụ và không thực hiện import, lưu đáp án, đổi cursor/flag hoặc phát sự kiện.
- Chi tiết nguồn bảng, snapshot và sự kiện theo tài liệu nghiệp vụ liên kết và service nguồn.
- Các transaction và kiểm tra dữ liệu hiện có được giữ nguyên.

## Part 4 — Operational Notes

- Baseline `6373cc8627649ff6bfe0cd2fc5d736f26aaea802`; branch `fix/practice-api-authorization`.
- Không đổi JWT filter, schema/migration V1–V30 và V27.1, service/worker hoặc nguồn dữ liệu lý thuyết.
- Quyền mới cần được cấp tại Identity qua migration do người phụ trách Identity thực hiện; không sửa Identity trong task Practice. Sau cấp quyền phải lấy access JWT mới.
- Quy ước quyền và tích hợp Progress: [authorization.md](authorization.md).
- Controller nguồn: [HomeController](../../src/main/java/com/dts/practice/controller/HomeController.java).
