# Card 02 — Tình huống cần ôn lại và replay chỉ đọc

## Part 0 — Classification & Identity

Tiếp tục `feat/simulation-practice` trên nền Card 01 đã có V31. Chỉ bổ sung lớp đọc và API riêng, không thay engine chấm điểm, bảng/migration hay nghiệp vụ lý thuyết. Nội dung chương/tình huống lấy từ ngân hàng mô phỏng PUBLISHED của Content Builder, không lấy danh mục 600 câu.

Native prefix `/api/v1/simulations`; Gateway prefix `/api/practice/v1/simulations`. JWT Identity dùng chung với app. Chủ thể luôn từ JWT, không có tham số userId để chọn người khác. Đọc tổng quan cần `simulations:read`; danh sách có thumbnail và replay cần thêm `media:read`. Các API đọc này không yêu cầu `simulations:learn`; tạo lượt/ghi kết quả/bookmark vẫn dùng quyền Card 01. Không cần permission Identity mới.

## Part 1 — API Contract

| Method | Path sau prefix | Quyền | data trả về |
| --- | --- | --- | --- |
| GET | `/review-needed/overview` | simulations:read | ReviewOverview |
| GET | `/review-needed` | simulations:read + media:read | ReviewPage |
| GET | `/attempts/{attemptId}/replay` | simulations:read + media:read | Replay của chính user, chỉ khi COMPLETED |

Thành công HTTP 200, `Cache-Control: no-store`, envelope `ApiResponse {success,message,data,timestamp,traceId}` như Card 01. DTO/schema đầy đủ ở [OpenAPI](simulation-review-needed.openapi.json). Đây là contract lập từ source; không lấy file schema làm chứng cứ runtime đã chạy.

Overview/list nhận `licenseClass` bắt buộc theo nghiệp vụ, `bankVersion` tùy chọn để chọn current khi bỏ qua. Version đã pin không tự đổi khi luyện lại/reload. Các hạng ô tô hỗ trợ cùng ngân hàng dùng chung kết quả theo user/bank/scoring tương thích.

List nhận thêm:

| Query | Quy tắc |
| --- | --- |
| chapterId | Bỏ qua để lấy tất cả; truyền số 1–6 thuộc ngân hàng. Không truyền chuỗi "null" hoặc ID chương lý thuyết. |
| sort | Chỉ BEST_SCORE_ASC, mặc định BEST_SCORE_ASC. |
| page | Bắt đầu từ 0, tối đa 10000; vượt trang dữ liệu trả items rỗng. |
| size | 1–12, mặc định 12. |
| expectedDatasetVersion | Tùy chọn, SHA256 đã nhận từ response. Nếu dữ liệu đổi giữa các trang, trả 409 SIMULATION_REVIEW_CHANGED thay vì âm thầm ghép hai danh sách khác nhau. |

ReviewOverview: `scope {userId,licenseClass,bankVersion}`, `scoringVersion`, `practicedScenarios`, `totalNeedReview`, `chapters`, `datasetVersion`.

ReviewChapter: `chapterId,chapterNumber,chapterName,needReviewCount`. Luôn trả đủ sáu chương, kể cả count=0. Tổng needReviewCount bằng totalNeedReview.

ReviewPage: các trường tổng quan trên, thêm `chapterId` (null khi không lọc), `sort`, `filteredCount`, `items`, `page {number,size,totalElements,totalPages}`. `totalNeedReview` không đổi theo filter; `filteredCount=page.totalElements` là tổng sau lọc, không phải độ dài trang. Toàn bộ số liệu trong một response được đọc ở cùng transaction REPEATABLE_READ. Bên frontend không cần ghép overview/list từ hai thời điểm để dựng các trạng thái SIM-04.

ReviewItem: `scenarioId,scenarioNumber,chapterId,chapterNumber,chapterName,title,durationMs,thumbnailUrl,thumbnailExpiresAt,bestScore,maxScore,isPracticed,isBookmarked,completedAttemptCount`. Trong danh sách này, isPracticed=true, bestScore là số 0–4, maxScore=5. Không có bestScore=null hoặc 5 trong items; các mẫu null/5 để đối chiếu nằm ở Detail/Result Card 01 và các ca bị loại khỏi danh sách.

Tên trường thực tế dùng `scope.bankVersion` thay trường bankVersion ở root của ví dụ rút gọn trong tài liệu GAP. ID chương backend là số; interface trình bày frontend có thể ánh xạ sang string. Model frontend `ReviewNeededPage.page` là số: ánh xạ từ `data.page.number`, `totalPages` từ `data.page.totalPages`.

## Part 2 — Processing Specification

Chỉ lấy status COMPLETED và cùng user, bankVersion, scoringVersion, manifest của snapshot đã công bố. Group theo scenario, tính MAX(score) và số lượt hoàn thành. Ghép lại với các scenario thật của snapshot; không để hàng lịch sử ngoài snapshot xuất hiện trong danh sách.

- Chưa có COMPLETED: không cần ôn, không tăng practicedScenarios.
- Best 0–4: cần ôn; 0 điểm vẫn là đã luyện.
- Đã từng đạt 5 điểm: không cần ôn, dù lượt sau 1 điểm.
- CREATED/PLAYING/INTERRUPTED/CANCELLED hoặc lưu thất bại không ảnh hưởng best/count.
- practicedScenarios đếm scenario riêng biệt, không phải số attempt.
- Sort theo bestScore ASC rồi scenarioNumber ASC; không lấy điểm mới nhất thay MAX.

Không có projection cần đồng bộ sau complete: lần đọc tiếp theo tổng hợp trực tiếp từ kết quả đã commit. Tiến độ, bookmark và điểm vẫn là dữ liệu Card 01; không tạo bookmark riêng. PUT bookmark giữ body đầy đủ `{licenseClass,bankVersion,bookmarked}` và ACK hiện có. Bookmark không đổi điều kiện cần ôn.

`datasetVersion` thay đổi khi các dữ liệu ảnh hưởng response thay đổi, gồm best/count và bookmark trong snapshot. Chữ ký/expiry ảnh đổi không làm datasetVersion đổi. Token dùng chung giữa các hạng hỗ trợ cùng bank của cùng user. Frontend có thể giữ token khi chuyển trang; gặp REVIEW_CHANGED thì đọc lại không kèm token cũ, tải lại từ trang đầu/điều chỉnh trang hợp lệ, giữ filter/sort. Sau complete/bookmark phải invalidate đúng query user/license/bank. Không coi token là database snapshot được giữ vô hạn.

## Part 3 — Read-only Replay

Replay bổ sung điểm mở GAP-BE-REPLAY của Card 01. `GET /attempts/{attemptId}/playback` cũ giữ nguyên quy tắc learning, vẫn từ chối terminal; frontend dùng endpoint mới khi muốn xem video của kết quả COMPLETED.

Replay có `attemptId,scenarioId,bankVersion,scoringVersion,videoUrl,expiresAt,mimeType="video/mp4",durationMs,readOnly=true,policy {allowPause:true,allowSeek:true,allowFullscreen:true}`. Không có playbackEventId/grant/heartbeat cho một lượt học mới.

Backend kiểm ownership trước; yêu cầu COMPLETED, pinned bank vẫn PUBLISHED, manifest/scoring và snapshot/media binding khớp. Dùng protocol Media hiện có với real attemptId và cùng chủ thể để lấy URL; Media tiếp tục kiểm private/READY, chữ ký và scope. URL hiện có hạn 300 giây; frontend dùng expiresAt thực tế, có thể gọi lại replay sau reload/hết hạn.

Việc xem lại không tạo attempt, không ghi event, grantedAt, nonce, phản ứng, điểm, cursor hay tiến độ. Không gửi start/heartbeat/response/complete khi đang ở readOnly replay. Người dùng chọn **Ôn lại/Luyện lại** mới dùng POST create Card 01 để tạo một lượt được chấm riêng. Không xóa lịch sử và không dùng một attempt mới giả làm xem lại.

## Part 4 — Errors, Data Interaction & Acceptance

400 cho binding/UUID/query sai, hạng thiếu hoặc version sai; 401 cho JWT thiếu/hết hạn/sai; 403 cho thiếu quyền/hạng không hỗ trợ; 404 cho không có nội dung/version hoặc attempt không thuộc user. Không rò sự tồn tại attempt của người khác. 503 cho dependency/media không sẵn sàng; không trả successful empty để che lỗi. Shape lỗi native/security/Gateway giống cách xử lý Card 01: kiểm HTTP status trước rồi code nếu có.

| HTTP 409 | Cách xử lý |
| --- | --- |
| SIMULATION_REVIEW_CHANGED | Danh sách đã đổi; bỏ token cũ, đọc lại trang phù hợp với filter/sort hiện tại. |
| SIMULATION_REPLAY_NOT_READY | Attempt chưa COMPLETED hoặc đã CANCELLED; không cấp URL chỉ đọc. |
| SIMULATION_BANK_CHANGED | Pinned binding/scoring không khớp; không tự chuyển bank hoặc cấp media khác. |

Không có migration mới. V31 và toàn bộ migration đã chạy được bảo vệ bằng hash; partial index progress hiện có phục vụ truy vấn COMPLETED. Nếu một thay đổi DB tương lai cần thiết thì phải có migration mới riêng, không sửa V31. Gateway/Identity/Media/Content Builder dùng protocol/quyền đã nghiệm thu, không bổ sung quyền hoặc route ở đợt này.

Test mới gồm 20 ca nghiệp vụ Card02, PostgreSQL thật, transaction read-only/snapshot khi có commit đồng thời, JWT/quyền và protocol replay. Chạy kèm đủ 26 lớp hồi quy đã có; không lấy syntax parsing, synthetic report hoặc mock HTTP làm chứng cứ backend JUnit đã đạt.

Runtime sau Check/Apply/Verify và restart Practice dùng hai tài khoản test. Trên một scenario chưa luyện, tạo bốn lượt COMPLETED (0,4,5,1) và một lượt CANCELLED; giữ lịch sử/tiến độ/cursor, trả bookmark về trạng thái cũ. Kiểm tra API qua native/Gateway, actual MP4 Range, quyền user khác và replay không đổi Attempt/Result. Đây là probe giao thức theo thời gian thật, chưa phải nghiệm thu browser. Response thật đã che query chữ ký/token được xuất ngoài repo trong audit `api-examples` để bàn giao frontend.

Frontend tiếp tục do nhóm frontend tích hợp và nghiệm thu SIM-04 → Ôn lại tại SIM-03 → quay về SIM-04; giữ bố cục đã duyệt, không tự tính bestScore hoặc xóa item trước ACK backend. Bàn giao OpenAPI/DTO, response thực tế, kết quả JUnit/runtime và checklist sau khi các bước chạy thực tế đạt; không suy ra FullCard02Accepted từ HTTP200 hoặc việc đóng gói patch.
