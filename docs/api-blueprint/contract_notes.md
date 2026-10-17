# Quy ước contract Practice

## Part 0 — Địa chỉ và ranh giới service

Base URL trực tiếp local: `http://localhost:8087`. Các path trong tài liệu đã có `/api/v1`; Gateway phải giữ đúng route cấu hình của dự án khi chuyển tiếp. Gateway local dùng `PRACTICE_SERVICE_URL=http://localhost:8087`. Không suy ra tên Next frontend proxy từ tên controller.

Practice là nguồn câu hỏi đã import, học/luyện/thi thử và tương tác chuẩn. Examination quản lý bài thi chính thức. Result đọc sự kiện để phục vụ lịch sử hợp nhất, leaderboard EXP và analytics; Progress nhận sự kiện tiến độ. Một API thành công ở Practice không có nghĩa projection Result/Progress đã cập nhật tức thì.

## Part 1 — Xác thực và sở hữu

Gửi `Authorization: Bearer <access-token Identity>` và `Content-Type: application/json` cho body JSON. Không lưu token thực trong tài liệu, fixture hay Git. JWT_ACCESS_SECRET và issuer phải khớp Identity/Gateway. Hầu hết business API yêu cầu authenticated; import yêu cầu ADMIN qua method security. Tài nguyên cá nhân lấy userId từ principal, không cho query userId thay chủ sở hữu.

Security filter trả HTTP401/403 với shape `{success:false,message,data:null,timestamp}`. Controller advice trả business/validation lỗi theo ErrorResponse `{code,message,details,traceId,timestamp}`. Vì vậy frontend đọc cả HTTP status và body phù hợp, không giả định mọi lỗi có trường `data` hoặc mọi lỗi có `code`.

## Part 2 — Thành công và ID

```json
{
  "success": true,
  "message": "Success",
  "data": {},
  "timestamp": "2026-10-17T00:00:00Z",
  "traceId": null
}
```

`data` phụ thuộc từng endpoint; ví dụ này chỉ minh họa envelope. ApiResponse là wrapper của Practice, không dùng nguyên response Content Builder. Câu hỏi Practice dùng ID integer; sourceQuestionId là ID nguồn. Exam/preset/session/traffic-sign dùng UUID. `licenseClass`/`examType` là mã hạng, không phải user profile ID. Java Map<Integer,...> thành object với key JSON là chuỗi.

HTTP201 được khai báo ở POST /api/v1/exams và POST /api/v1/questions/admin/import-bank trong nhóm này; các POST tạo phiên preset/review khác hiện trả200. Không ép tất cả POST phải trả201 ở client.

## Part 3 — Kết quả và sự kiện

`wrongCount` lịch sử trong Exam bao gồm câu sai và chưa trả lời; dùng hai trường answeredWrongCount/unansweredCount để tách. Điểm Practice là 0–100; ngưỡng đạt dùng ExamMatrix và điều kiện điểm liệt. Không so trực tiếp với thang điểm hiển thị projection Result mà bỏ qua source policy.

Finish lưu receipt/outbox cùng transaction. Worker gửi lại sau lỗi; delivery semantics là có thể lặp, consumer cần idempotency. Không tuyên bố exactly-once ở tầng Kafka. PRACTICE tạo tiến độ nhưng không tạo một RANDOM_EXAM/PRESET_EXAM trong lịch sử Screen13. V30 capture tương tác mới cho EXP; không tự backfill mọi câu đã học thành EXP.

GET session/result có thể xử lý timeout; GET progress có thể bổ sung receipt/outbox. Không coi các endpoint này là công cụ audit DB chỉ đọc. Không chạy lại các công cụ runtime tạo fixture khi chuẩn bị Git.

## Part 4 — Đối chiếu source

Controller quyết định route và wrapper; request DTO chứa validation; service quyết định trạng thái/permission nghiệp vụ; migration quyết định invariant DB. Các file docs chỉ bổ sung giải thích, không sửa Java/YAML/SQL hay chạy database. Một số lỗi binding của Spring đi qua global handler; không cam kết tất cả input lỗi đều trả400 nếu chưa xác minh đúng nhánh code.
