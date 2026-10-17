# Phiên luyện tập, thi ngẫu nhiên và kết quả

## Part 0 — Phạm vi và nguồn

Tài liệu theo source local đã thu thập ngày 17/10/2026, trên nhánh `feature/2026-license-practice-exams`. Mô tả cả API mới và API giữ route nhưng thay đổi nghiệp vụ so với `main`. Chi tiết DTO dùng chung ở [dto_reference.md](dto_reference.md); quy ước lỗi/xác thực ở [contract_notes.md](contract_notes.md).

## Part 1 — Nghiệp vụ

`POST /exams` tạo phiên ngẫu nhiên theo ExamMatrix. `mode=EXAM` và `mode=PRACTICE` có cách hiển thị đáp án khác nhau. Một người chỉ có một phiên hoạt động cho mỗi hạng; trạng thái IN_PROGRESS và PAUSED đều chiếm phiên. Conflict ACTIVE_EXAM_EXISTS cần được xử lý bằng luồng tiếp tục phiên.

Lấy `/active?examType=...` trước khi tạo phiên. `pause` lưu câu hiện tại và thời gian còn lại, chuyển expiresAt thành null. `resume` tính lại expiresAt từ thời gian còn lại. Không tự suy ra hết giờ ở client; dùng status, expiresAt, remainingTimeMs và serverTime. GET session có thể xử lý timeout, do đó không phải mọi GET đều chỉ đọc DB.

Lưu câu trả lời bằng selectedAnswer là nhãn option. PRACTICE trả feedback ngay và khi tải lại có answerFeedback cho câu đã trả lời; EXAM chưa kết thúc giữ chính sách ẩn đáp án. Flags và cursor được lưu riêng, không tính là trả lời.

Finish lặp lại trên COMPLETED/TIMEOUT trả kết quả đã có; receipt và outbox dùng khóa duy nhất để tránh nhân sự kiện logic. `wrongCount` cũ gộp câu sai và câu bỏ trống; dùng `answeredWrongCount` và `unansweredCount` để tách hai loại. Điểm là tỷ lệ đúng trên 100, điều kiện đạt vẫn theo ngưỡng số câu đúng và câu điểm liệt của hạng.

Retry tạo examId mới với bộ câu hỏi của nguồn đã hoàn thành hợp lệ, giữ lượt cũ. GET progress có thể tạo receipt/outbox cho lượt cũ đã hoàn thành; không gọi để lấy dữ liệu giả định chỉ đọc.

`/exams/history` và `/exams/leaderboard` là API Practice hiện có. Lịch sử hợp nhất Screen13 và leaderboard/analytics Screen14/16 thuộc Result, có contract và thang hiển thị riêng; không đổi hai API này cho nhau. Bài thi chính thức thuộc Examination.

## Part 2 — Endpoint và request

### `POST /api/v1/exams`

- HTTP thành công: **201**; response Java: `ApiResponse<ExamSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamController.java`](../../src/main/java/com/dts/practice/controller/ExamController.java), method `startExam`.

Start a new exam or practice session

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| RequestBody | `request` | `StartExamRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ExamSessionResponse`, `StartExamRequest`.

### `GET /api/v1/exams/active`

- HTTP thành công: **200**; response Java: `ApiResponse<ActiveExamResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamController.java`](../../src/main/java/com/dts/practice/controller/ExamController.java), method `getActiveExam`.

Get the latest active session for the current user and license class

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| RequestParam | `examType` | `String` | Bắt buộc |

DTO: `ActiveExamResponse`.

### `GET /api/v1/exams/{examId}`

- HTTP thành công: **200**; response Java: `ApiResponse<ExamSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamController.java`](../../src/main/java/com/dts/practice/controller/ExamController.java), method `getSession`.

Get exam session details

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `examId` | `UUID` | Bắt buộc |

DTO: `ExamSessionResponse`.

### `POST /api/v1/exams/{examId}/pause`

- HTTP thành công: **200**; response Java: `ApiResponse<ExamSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamController.java`](../../src/main/java/com/dts/practice/controller/ExamController.java), method `pauseExam`.

Pause the timer and save the current question

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `examId` | `UUID` | Bắt buộc |
| RequestBody | `request` | `PauseExamRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ExamSessionResponse`, `PauseExamRequest`.

### `POST /api/v1/exams/{examId}/resume`

- HTTP thành công: **200**; response Java: `ApiResponse<ExamSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamController.java`](../../src/main/java/com/dts/practice/controller/ExamController.java), method `resumeExam`.

Resume a paused exam with its remaining time

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `examId` | `UUID` | Bắt buộc |

DTO: `ExamSessionResponse`.

### `POST /api/v1/exams/{examId}/answers`

- HTTP thành công: **200**; response Java: `ApiResponse<SubmitAnswerResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamController.java`](../../src/main/java/com/dts/practice/controller/ExamController.java), method `submitAnswer`.

Submit an answer for a question (in practice mode, returns explanation)

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `examId` | `UUID` | Bắt buộc |
| RequestBody | `request` | `SubmitAnswerRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `SubmitAnswerRequest`, `SubmitAnswerResponse`.

### `POST /api/v1/exams/{examId}/finish`

- HTTP thành công: **200**; response Java: `ApiResponse<ExamResultResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamController.java`](../../src/main/java/com/dts/practice/controller/ExamController.java), method `finishExam`.

Finish exam and get results

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `examId` | `UUID` | Bắt buộc |

DTO: `ExamResultResponse`.

### `GET /api/v1/exams/{examId}/result`

- HTTP thành công: **200**; response Java: `ApiResponse<ExamResultResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamController.java`](../../src/main/java/com/dts/practice/controller/ExamController.java), method `getResult`.

Get exam result (must be completed)

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `examId` | `UUID` | Bắt buộc |

DTO: `ExamResultResponse`.

### `GET /api/v1/exams/history`

- HTTP thành công: **200**; response Java: `ApiResponse<Page<ExamHistoryResponse>>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamController.java`](../../src/main/java/com/dts/practice/controller/ExamController.java), method `getHistory`.

Get exam history for current user

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| Pageable | `pageable` | `Pageable` | Spring Pageable: page, size, sort; không phải query licenseClass |

DTO: `ExamHistoryResponse`.

### `GET /api/v1/exams/leaderboard`

- HTTP thành công: **200**; response Java: `ApiResponse<List<ExamService.LeaderboardEntry>>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamController.java`](../../src/main/java/com/dts/practice/controller/ExamController.java), method `getLeaderboard`.

Get top scores leaderboard

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| RequestParam | `examType` | `String` | Tùy chọn |
| RequestParam | `period` | `String` | Tùy chọn; mặc định `all` |

DTO: `LeaderboardEntry`.

### `PUT /api/v1/exams/{examId}/cursor`

- HTTP thành công: **200**; response Java: `ApiResponse<ExamSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamCursorController.java`](../../src/main/java/com/dts/practice/controller/ExamCursorController.java), method `updateCursor`.

Save the current question in an active exam

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `examId` | `UUID` | Bắt buộc |
| RequestBody | `request` | `UpdateExamCursorRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ExamSessionResponse`, `UpdateExamCursorRequest`.

### `GET /api/v1/exams/{examId}/progress`

- HTTP thành công: **200**; response Java: `ApiResponse<ExamProgressReceiptResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamProgressReceiptController.java`](../../src/main/java/com/dts/practice/controller/ExamProgressReceiptController.java), method `getProgress`.

Get the canonical progress receipt for a finished exam

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `examId` | `UUID` | Bắt buộc |

DTO: `ExamProgressReceiptResponse`.

### `GET /api/v1/exams/{examId}/flags`

- HTTP thành công: **200**; response Java: `ApiResponse<ExamFlagsResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamQuestionFlagController.java`](../../src/main/java/com/dts/practice/controller/ExamQuestionFlagController.java), method `getFlags`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `examId` | `UUID` | Bắt buộc |

DTO: `ExamFlagsResponse`.

### `PUT /api/v1/exams/{examId}/questions/{questionId}/flag`

- HTTP thành công: **200**; response Java: `ApiResponse<ExamFlagsResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamQuestionFlagController.java`](../../src/main/java/com/dts/practice/controller/ExamQuestionFlagController.java), method `updateFlag`.

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `examId` | `UUID` | Bắt buộc |
| PathVariable | `questionId` | `Integer` | Bắt buộc |
| RequestBody | `request` | `UpdateExamFlagRequest` | Bắt buộc; kiểm tra Bean Validation trong DTO |

DTO: `ExamFlagsResponse`, `UpdateExamFlagRequest`.

### `POST /api/v1/exams/{examId}/retry`

- HTTP thành công: **201**; response Java: `ApiResponse<ExamSessionResponse>`.
- Quyền: Bearer token hợp lệ; dữ liệu cá nhân lấy từ principal của token.
- Controller: [`src/main/java/com/dts/practice/controller/ExamRetryController.java`](../../src/main/java/com/dts/practice/controller/ExamRetryController.java), method `retryExam`.

Retry a completed exam with the same questions and order

| Vị trí | Tên | Kiểu | Quy tắc |
| --- | --- | --- | --- |
| AuthenticationPrincipal | `user` | `JwtUserDetails` | Từ token, không gửi trong body/query |
| PathVariable | `examId` | `UUID` | Bắt buộc |

DTO: `ExamSessionResponse`.

## Part 3 — Luồng nối giao diện

Đọc `data` trong ApiResponse sau khi kiểm tra HTTP status và `success`. Với dữ liệu cá nhân, truyền đúng token phiên đăng nhập; không gửi userId để chọn người khác. Dùng trạng thái/cursor/ID backend trả về để phục hồi giao diện. Các request ghi trạng thái phải dùng chính ID câu/phiên trong response của luồng tương ứng.

## Part 4 — Lưu trữ và triển khai

Các service và bảng liên quan được tổng hợp trong [danh mục chức năng](../PRACTICE_FEATURES.md) và [migration catalog](../db/MIGRATION_CATALOG.md). Chạy migration theo thứ tự version, giữ nguyên checksum. Xem [LOCAL_SETUP.md](../LOCAL_SETUP.md) để cấu hình DB/Kafka/Progress và nhập dữ liệu. Tài liệu này không tạo endpoint hay thay đổi runtime.
