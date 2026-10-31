# QuestionBankImport importBank

## Part 0 — Classification & Identity

- **API Name**: QuestionBankImport importBank
- **Module**: `practice`
- **Feature**: Import ngân hàng đã chuẩn bị
- **Authorization**: `ROLE_ADMIN và practice:import`

## Part 1 — API Contract

- **HTTP Method**: `POST`
- **URL**: `/api/v1/questions/admin/import-bank`
- **Success Status**: `201`
- **Response Type**: `ApiResponse<Map<String, Object>>`

### Headers

| Name | Required | Description |
| --- | --- | --- |
| Authorization | Có | Bearer access JWT hợp lệ của Identity |
| Content-Type | Khi có body | application/json |

JWT claim `permissions` chứa tên thuần `practice:import`; authority kiểm tra tại controller là `PERM_practice:import`.
Ngoài permission, API import tiếp tục bắt buộc ROLE_ADMIN. Quyền đọc/học/thi không cấp quyền import.

### Request Body

Body JSON theo `ImportQuestionBankRequest`. Giữ nguyên Bean Validation và kiểm tra nghiệp vụ hiện có.
Định nghĩa field/constraint: [ImportQuestionBankRequest](../../src/main/java/com/dts/practice/dto/request/ImportQuestionBankRequest.java) và [DTO reference](dto_reference.md).

```java
public record ImportQuestionBankRequest(
        @NotBlank String bankVersion,
        @NotEmpty @Size(min = 600, max = 600)
        List<@Valid BankQuestion> questions
) {
    public record BankQuestion(
            @NotNull UUID contentBuilderQuestionId,
            @NotNull @Min(1) @Max(600) Integer sourceQuestionId,
            @NotNull @Min(1) @Max(6) Integer chapter,
            @NotBlank String questionText,
            @NotEmpty @Size(min = 2, max = 4)
            List<@Valid BankOption> options,
            @NotBlank @Pattern(regexp = "[A-D]") String correctAnswer,
            @NotNull Boolean isCritical,
            @NotEmpty List<@NotBlank String> applicableLicenses,
            @NotNull List<@NotBlank String> criticalLicenses,
            String explanation,
            UUID mediaFileId
    ) {}

    public record BankOption(
            @NotBlank @Pattern(regexp = "[A-D]") String label,
            @NotBlank String text
    ) {}
}
```
Payload import phải đủ số lượng và cấu trúc nguồn theo DTO: ngân hàng câu hỏi 600, biển báo 221 hoặc mẹo 12. Không dùng payload giả để ghi database nghiệp vụ.

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
2. **Controller Layer**: Kiểm `hasRole(ADMIN) và hasAuthority(PERM_practice:import)` trước gọi service.
3. **Service Layer**: Gọi [QuestionBankImportService.importBank](../../src/main/java/com/dts/practice/service/QuestionBankImportService.java), giữ nguyên kiểm tra và luồng xử lý của service.
4. **Ownership**: Import nội dung dùng chung theo quyền quản trị; không phải thao tác học cá nhân.

## Part 3 — Data Interaction

- Không thay query, transaction, DTO, chấm điểm, timer, pause/resume, đáp án phản hồi, receipt hoặc outbox trong bản vá này.
- Request bị từ chối quyền không gọi service nghiệp vụ và không thực hiện import, lưu đáp án, đổi cursor/flag hoặc phát sự kiện.
- Import kiểm ngân hàng đã tồn tại rồi INSERT; giữ cơ chế từ chối import lặp, không thêm upsert/xóa/ghi đè.
- Các transaction và kiểm tra dữ liệu hiện có được giữ nguyên.

## Part 4 — Operational Notes

- Baseline `6373cc8627649ff6bfe0cd2fc5d736f26aaea802`; branch `fix/practice-api-authorization`.
- Không đổi JWT filter, schema/migration V1–V30 và V27.1, service/worker hoặc nguồn dữ liệu lý thuyết.
- Quyền mới cần được cấp tại Identity qua migration do người phụ trách Identity thực hiện; không sửa Identity trong task Practice. Sau cấp quyền phải lấy access JWT mới.
- Quy ước quyền và tích hợp Progress: [authorization.md](authorization.md).
- Controller nguồn: [QuestionBankImportController](../../src/main/java/com/dts/practice/controller/QuestionBankImportController.java).

Nghiệp vụ chi tiết: [question_catalog.md](question_catalog.md).
