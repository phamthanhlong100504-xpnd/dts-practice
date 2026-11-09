# Card 03 — Tình huống đã đánh dấu

## Part 0 — Classification & Identity

Tiếp tục `feat/simulation-practice` trên nền Card 01/02 đã nghiệm thu API. Card 03 bổ sung tổng quan/danh sách đọc từ bảng `simulation_bookmarks` hiện có, dùng chung PUT Card 01. Không có bảng, migration, quyền hoặc engine luyện mới. Các service Identity/Content Builder/Media/Gateway giữ protocol đã có.

Native prefix `/api/v1/simulations`; Gateway prefix `/api/practice/v1/simulations`. User lấy từ JWT. Các hạng hỗ trợ dùng cùng bank chia sẻ bookmark user/bank/scenario, không chia riêng theo license. Version pin dùng chính sách Card 01, không tự chuyển current giữa lượt luyện hoặc khi reload.

## Part 1 — API Contract

| Method | Path sau prefix | Quyền | data |
| --- | --- | --- | --- |
| GET | `/bookmarks/overview` | simulations:read | BookmarkOverview |
| GET | `/bookmarks` | simulations:read + media:read | BookmarkPage |
| PUT | `/bookmarks/{scenarioId}` | simulations:read + simulations:learn | BookmarkAck hiện có |

Thành công HTTP 200 và `Cache-Control: no-store`; envelope `{success,message,data,timestamp,traceId}`. Schema chi tiết: [OpenAPI](simulation-bookmarks.openapi.json). Đây là contract đối chiếu source, không phải bằng chứng runtime đã đạt.

GET nhận `licenseClass` bắt buộc theo nghiệp vụ, `bankVersion` tùy chọn (bỏ qua lấy current). GET list nhận thêm:

| Query | Quy tắc |
| --- | --- |
| chapterId | Bỏ qua để lấy tất cả, hoặc số chương 1–6 thuộc bank mô phỏng. |
| sort | Chỉ SCENARIO_ASC, cũng là mặc định. |
| page | 0-based, mặc định 0; giới hạn 0–10000. |
| size | Mặc định 12, giới hạn 1–12. |
| expectedDatasetVersion | Tùy chọn SHA256 từ API này để phát hiện thay đổi giữa các trang. |

BookmarkOverview có `scope {userId,licenseClass,bankVersion}`, `scoringVersion`, `totalBookmarked`, đủ sáu `chapters` (chapterId, chapterNumber, chapterName, bookmarkedCount), `datasetVersion`. Tổng bookmarkedCount bằng totalBookmarked, giữ chương count=0.

BookmarkPage có các trường trên, thêm `chapterId` nullable, `sort`, `filteredCount`, `items`, `page {number,size,totalElements,totalPages}`. `totalBookmarked` luôn toàn ngân hàng; `filteredCount=page.totalElements` là tổng sau lọc, không phải độ dài trang. Các trường số liệu được đọc trong cùng transaction read-only REPEATABLE_READ. Trang vượt dữ liệu trả items rỗng, không tự kẹp page.number về trang cuối.

BookmarkItem có `scenarioId,scenarioNumber,chapterId,chapterNumber,chapterName,title,durationMs,thumbnailUrl,thumbnailExpiresAt,isPracticed,bestScore,maxScore,isBookmarked,completedAttemptCount`. Chưa COMPLETED: isPracticed=false, bestScore=null, count=0. Đã COMPLETED 0: isPracticed=true, bestScore=0. Best có thể 5; maxScore=5, isBookmarked=true trong mọi item trả về. ID chương backend là số; UI dùng string thì ánh xạ ở adapter. Version nằm ở scope.bankVersion.

PUT giữ **body đầy đủ** `{licenseClass,bankVersion,bookmarked}`; ACK `{scenarioId,bankVersion,bookmarked}`. Mẫu chỉ có bookmarked trong tài liệu GAP là bản rút gọn, không thay contract hiện đang chạy. Không thêm eventId, userId hoặc một API ghi bookmark khác.

## Part 2 — Processing Specification

Đọc các bookmark user/bank hiện tại, ghép với các scenario thật trong snapshot PUBLISHED. Bookmark không cần lịch sử COMPLETED và không áp dụng điều kiện bestScore<5 của Card 02. Thông tin luyện dùng MAX(score) và số lượt COMPLETED tương thích bank/scoring/manifest từ bộ đọc Card 02; không lấy trạng thái đang luyện, lý thuyết hoặc score mới nhất thay best.

- Sort scenarioNumber ASC, phân trang tối đa 12; chỉ cấp URL thumbnail cho trang được chọn.
- Luyện/đạt 5 hoặc thất bại khi luyện không tự bỏ bookmark.
- PUT true lặp không tạo trùng; PUT false lặp giữ trạng thái false. ACK sau transaction commit; lỗi phải rollback, giữ trạng thái đã lưu.
- PUT bookmark chỉ ghi bảng bookmark, không sửa attempt/event/result/cursor/progress. SIM-02/SIM-03/SIM-04/SIM-05 cùng đọc một nguồn.
- Dữ liệu bookmark ngoài snapshot được công bố không hiện trong tổng/list. API ngân hàng/media lỗi trả lỗi, không thay bằng successful empty.

`datasetVersion` bao gồm user, bank, scoring, manifest, membership bookmark đã công bố và best/count của các bookmark đó. Thay đổi điểm của một tình huống không được đánh dấu không làm đổi token; URL/expiry mới cũng không đổi token. Không dùng token Card 02 thay cho token Card 03.

Sau PUT/complete ACK, frontend invalidate đúng các query user/bank liên quan, giữ filter/sort và điều chỉnh page hợp lệ. List có `expectedDatasetVersion` cũ trả 409 `SIMULATION_BOOKMARKS_CHANGED`: bỏ token cũ, tải lại từ trang đầu hoặc trang hợp lệ với filter hiện tại. Đây là phát hiện thay đổi giữa request, không phải snapshot DB giữ vô hạn hoặc dịch vụ push thời gian thực.

## Part 3 — Navigation & Media

SIM-05 → Luyện ngay/Ôn lại → SIM-03 dùng scenarioId + bankVersion và engine/queue Card 01. Chưa luyện thì tạo lượt đầu, đã luyện thì tạo lượt mới có ý định; giữ kết quả cũ. Sau result ACK, reload SIM-05 cho best/count mới, bookmark vẫn được giữ trừ khi user bỏ dấu.

Xem video của result COMPLETED dùng `/attempts/{attemptId}/replay` từ Card 02, không tự tạo lượt hoặc gửi lệnh học trong replay. Không thêm API lịch sử hay mô hình lượt riêng cho Card 03.

URL thumbnail có hạn dùng `thumbnailExpiresAt`; refetch list hoặc endpoint thumbnail Card 01 theo scope để lấy URL mới. Không dựng URL từ bucket/key ở frontend. Media lỗi phải phân biệt với danh sách rỗng.

## Part 4 — Errors, Data Interaction & Acceptance

400 SIMULATION_INVALID_REQUEST cho query/UUID/version sai, 401 JWT thiếu/sai, 403 thiếu quyền/hạng không hỗ trợ, 404 SIMULATION_NOT_FOUND cho nguồn/scenario không hợp lệ, 409 SIMULATION_BOOKMARKS_CHANGED cho token stale, 503 SIMULATION_DEPENDENCY_UNAVAILABLE khi dependency chưa sẵn sàng. Business errors có code/message/details/traceId/timestamp; security/Gateway có thể dùng envelope khác. Xử lý HTTP status trước, đọc code khi có.

Không có migration mới: V31 đã có PK(user_id,bank_version,scenario_id) và partial index COMPLETED phù hợp tập 120 tình huống. Giữ nguyên V31, các migration trước và source Java Card 01/02/lý thuyết. Không chạy SQL thủ công để tạo bảng khác.

Check phải chạy đầy đủ hồi quy đã có cùng unit/security/PostgreSQL mới. Kiểm thử bao phủ C03-T01…19, rollback lỗi sau thao tác DELETE thật, PUT trùng đồng thời, repeatable-read khi thiết bị khác commit, và service bookmark lý thuyết với bảng thật trong transaction rollback. Không dùng synthetic XML/control fixture làm bằng chứng JUnit đạt.

Runtime dùng hai tài khoản test, API native/Gateway và JWT thật; chọn scenario chưa luyện, đánh dấu, hoàn thành điểm 0 rồi 5, xác nhận bookmark còn giữ và best cập nhật, PUT false/true retry, user khác độc lập, token stale, thumbnail Range và các response thực tế đã che chữ ký. Các lượt hoàn thành được giữ làm lịch sử; bookmark của tài khoản test được trả lại trạng thái ban đầu. Runtime giao thức không phải nghiệm thu browser. Bên frontend nhận contract/response thật sau runtime đạt và nghiệm thu SIM-02 → SIM-05 → SIM-03 → quay lại SIM-05, cùng luồng bỏ dấu/lỗi lưu trên web.
