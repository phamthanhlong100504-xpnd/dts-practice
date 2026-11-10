# ExamCursor updateCursor

## Part 0 — Classification & Identity

- **API Name**: ExamCursor updateCursor
- **Module**: `practice`
- **Feature**: Thực hiện phiên thi thử của chính tài khoản
- **Authorization**: `practice:exam`

## Part 1 — API Contract

- **HTTP Method**: `PUT`
- **URL**: `/api/v1/exams/{examId}/cursor`
- **Success Status**: `200`
- **Response Type**: `ApiResponse<ExamSessionResponse>`

### Headers

| Name | Required | Description |
| --- | --- | --- |
| Authorization | Có | Bearer access JWT hợp lệ của Identity |
| Content-Type | Khi có body | application/json |

JWT claim `permissions` chứa tên thuần `practice:exam`; authority kiểm tra tại controller là `PERM_practice:exam`.
Permission không mở quyền truy cập dữ liệu cá nhân người khác. API có principal luôn lấy userId từ JWT sub.

### Path Variables

| Name | Java Type | Description |
| --- | --- | --- |
| examId | UUID | Giữ kiểu và validation hiện có; kiểm tra tài nguyên/chủ sở hữu tại service |

### Request Body

Body JSON theo `UpdateExamCursorRequest`. Giữ nguyên Bean Validation và kiểm tra nghiệp vụ hiện có.
Định nghĩa field/constraint: [UpdateExamCursorRequest](../../src/main/java/com/dts/practice/dto/request/UpdateExamCursorRequest.java) và [DTO reference](dto_reference.md).

```java
public record UpdateExamCursorRequest(
        @NotNull Integer currentQuestionId
) {}
```

### Response và lỗi

Request/response, ID integer/UUID, đơn vị điểm và các field nullable giữ nguyên. Xem [DTO reference](dto_reference.md) và tài liệu nghiệp vụ liên kết.

| Trường hợp | HTTP | Error contract |
| --- | --- | --- |
| JWT thiếu, hết hạn, sai issuer/chữ ký | 401 | Security handler; không bắt buộc có code |
| Đã xác thực nhưng thiếu quyền hoặc role import | 403 | ACCESS_DENIED tại method security |
| Tài nguyên/phiên không thuộc principal | Theo source hiện có | Giữ 404/NOT_FOUND khi service tra theo userId |
| Body/binding hoặc nghiệp vụ sai | Theo handler hiện có | Không tự đổi mã lỗi hoặc ép mọi input sai thành cùng mã |

Method security chạy trước service nghiệp vụ, sau bước bind/validate argument của Spring MVC. Payload sai có thể bị trả lỗi validation trước kiểm tra permission; bộ test thiếu quyền dùng request hợp lệ.

## Part 2 — Processing Specification

1. **Security Layer**: Giữ nguyên JwtValidationFilter và SecurityConfig. Chỉ dùng claims đã được xác minh, không tin X-User-* do client tự đặt.
2. **Controller Layer**: Kiểm `hasAuthority(PERM_practice:exam)` trước gọi service.
3. **Service Layer**: Gọi [ExamCursorService.updateCursor](../../src/main/java/com/dts/practice/service/ExamCursorService.java), giữ nguyên kiểm tra và luồng xử lý của service.
4. **Ownership**: Dữ liệu cá nhân đi theo userId từ principal; các session được service tra theo cả id và user_id. Catalog/reference dùng nguồn đã publish/current bank như trước.

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
- Controller nguồn: [ExamCursorController](../../src/main/java/com/dts/practice/controller/ExamCursorController.java).

Nghiệp vụ chi tiết: [exam_sessions.md](exam_sessions.md).
