# Ôn luyện video mô phỏng — Card 01

## Part 0 — Classification & Identity

Module riêng trong Practice, kế thừa [catalogue và thumbnail](simulation_catalog.md). Identity cung cấp `simulations:read`, `simulations:learn`, `media:read`; Media xác minh ticket playback gắn với lượt luyện thực. Dữ liệu mô phỏng không dùng questions, exams hoặc tiến độ lý thuyết. Các hạng được hỗ trợ cùng dùng một ngân hàng chia sẻ tiến độ/bookmark theo user và bankVersion.

API native có prefix `/api/v1/simulations`; qua Gateway là `/api/practice/v1/simulations`. Mọi request gửi access JWT Identity. Quyền đọc không mở quyền học; quyền học không mở dữ liệu người khác. Chủ thể lấy từ JWT đã kiểm tra chữ ký, issuer, iat/exp và UUID subject; bỏ qua userId hoặc X-User-* do client tự đặt.

## Part 1 — API Contract

Mọi thành công HTTP 200 bọc `ApiResponse {success,message,data,timestamp,traceId}` và có `Cache-Control: no-store`. Dưới đây ghi đường dẫn sau prefix:

| Method | Path | Quyền | Request / data trả về |
| --- | --- | --- | --- |
| GET | `/overview` | read | Query licenseClass, bankVersion tùy chọn; Overview |
| GET | `/chapters/{chapterId}/scenarios` | read + media:read | Query licenseClass, bankVersion?, filter?, sort?, page?, size?; ChapterPage |
| GET | `/scenarios/{scenarioId}` | read + media:read | Query licenseClass, bankVersion?; Detail |
| POST | `/attempts` | read + learn | `{eventId,scenarioId,licenseClass,bankVersion}`; Attempt |
| GET | `/attempts/{attemptId}` | read | Attempt của chính user |
| GET | `/attempts/{attemptId}/playback` | read + learn + media:read | Playback, có ghi thời điểm cấp URL trên attempt |
| POST | `/attempts/{attemptId}/start` | read + learn | Event; Attempt |
| POST | `/attempts/{attemptId}/heartbeat` | read + learn | Event; Attempt |
| PUT | `/attempts/{attemptId}/response` | read + learn | Event phản ứng đầu tiên; Attempt |
| POST | `/attempts/{attemptId}/complete` | read + learn | Event; Attempt kèm Result |
| GET | `/attempts/{attemptId}/result` | read | Result đã lưu; chưa complete trả 409 |
| POST | `/attempts/{attemptId}/interrupt` | read + learn | `{eventId,playbackEventId,reason}`; Attempt |
| POST | `/attempts/{attemptId}/cancel` | read + learn | `{eventId}`; Attempt CANCELLED |
| PUT | `/bookmarks/{scenarioId}` | read + learn | `{licenseClass,bankVersion,bookmarked:boolean}`; trạng thái đã lưu |
| PUT | `/chapters/{chapterId}/cursor` | read + learn | `{eventId,licenseClass,bankVersion,scenarioId,expectedRevision}`; Cursor |

`read`/`learn` trong bảng là `simulations:read`/`simulations:learn`. eventId, scenarioId, attemptId, playbackEventId là UUID; chapterId là 1–6. licenseClass thuộc catalogue được hỗ trợ. bankVersion phải là version PUBLISHED; tạo lượt luyện bắt buộc pin version từ catalogue/overview, không tự chuyển version khi request gửi lại.

### Event và idempotency

```json
{"eventId":"<uuid-for-this-command>","playbackEventId":"<server-playback-uuid>","mediaTimeMs":17400}
```

Giữ nguyên eventId và payload khi retry sau timeout/mất mạng. Backend khóa theo user/event và user/attempt; cùng eventId, action và payload trả trạng thái hiện tại mà không tạo thêm lượt, phản ứng hoặc kết quả. Dùng cùng eventId cho payload/action/attempt khác trả `SIMULATION_EVENT_CONFLICT`. Các thao tác mới dùng eventId mới. Với các lần Space tiếp theo, phản ứng đầu tiên vẫn giữ nguyên. Complete lại một attempt đã COMPLETED trả kết quả cũ, không ghi lại hàng terminal.

Bookmark là PUT trạng thái tuyệt đối, gọi lặp không tạo bản ghi trùng. Cursor dùng CAS: lấy `cursor.revision` từ ChapterPage, gửi làm expectedRevision; cursor chưa có là 0. Khi có cập nhật khác, 409 và frontend đọc lại trước khi gửi lệnh mới. Retry cùng eventId không tăng revision thêm.

### Attempt, Playback và Result

Attempt có `attemptId,scenarioId,bankVersion,scoringVersion,status,playbackEventId,videoDurationMs,resumeAtMs,responseRecorded,firstResponseMs,requiresResume,createdAt,startedAt,completedAt,policy,result`. firstResponseMs và result null khi chưa có dữ liệu tương ứng. result chỉ có khi COMPLETED. Policy hiện hành: pause=false, seek=false, fullscreen=true, hintsWhilePlaying=false, heartbeatIntervalMs=3000, maxHeartbeatGapMs=12000.

Playback có `attemptId,playbackEventId,bankVersion,videoUrl,expiresAt,mimeType="video/mp4",durationMs,resumeAtMs`. URL hiện có hạn 300 giây. Practice dùng snapshot của attempt và ngân hàng vẫn PUBLISHED, ký ticket 60 giây bằng key domain `dts-media:simulation-playback:v1`, purpose SIMULATION_LEARNING, issuer dts-practice, audience dts-media, sub user, real attemptId, đúng videoMediaId/bankVersion/target SIMULATION_VIDEO. Ticket không được đưa cho frontend; Media kiểm tra trước khi cấp URL. Gia hạn URL khi đang phát giữ nguyên playbackEventId, phản ứng và mốc thời gian.

Result gồm `attemptId,scenarioId,status="COMPLETED",score,maxScore=5,responseRecorded,firstResponseMs,videoDurationMs,bankVersion,scoringVersion,scoringSegments,hazardSign,recognitionTip`. Mỗi segment có `{startMs,endMs,score,endInclusive}`. UI chỉ hiển thị điểm/analysis từ Result đã lưu. DTO giao diện có thể map videoDurationMs sang durationMs; không tính điểm tại frontend.

### Overview, ChapterPage, Detail

Overview có `scope {userId,licenseClass,bankVersion}`, totalScenarios=120, practicedScenarios, completionPercent, chapters. Chapter có chapterId, chapterNumber, chapterName, totalScenarios, practicedScenarios, completionPercent, status NOT_STARTED/IN_PROGRESS/COMPLETED.

ChapterPage có scope, chapter, items, filterCounts, page, size, totalPages, filteredCount, startScenarioId và cursor `{scenarioId,sourceAttemptId,revision}`. Filter ALL/UNPRACTICED/PRACTICED/BOOKMARKED; sort chỉ SCENARIO_ASC; page từ 0, size 1–12 (mặc định 12). Thứ tự ổn định theo scenarioNumber, page vượt dữ liệu trả items rỗng. startScenarioId là tình huống chưa hoàn thành đầu tiên; hết chương thì tình huống đầu chương để luyện lại. Cursor là vị trí tiếp tục riêng, có thể trỏ tình huống đã luyện.

Item có scenarioId, scenarioNumber, title, chapterId, durationMs, thumbnailUrl, thumbnailExpiresAt, isPracticed, bestScore (null hoặc 0–5), maxScore=5, isBookmarked, completedAttemptCount. Thumbnail lấy song song có giới hạn từ Media qua protocol đã nghiệm thu; lỗi cấp ảnh trả 503 thay vì gán dữ liệu thành công giả.

Detail có scope, scenario(Item), hintPreview `{hazardSign,recognitionTip}` hoặc null, policy, latestAttempt hoặc null, previousScenarioId/nextScenarioId trong cùng chương. Khi user có lượt PLAYING cho tình huống đó, hintPreview null. Mẹo được phép xem trước khi bắt đầu; đây là ôn tập, không phải cơ chế che thông tin cho thi chính thức.

## Part 2 — State và kiểm tra playback

| Trạng thái | Hành động hợp lệ | Kết quả |
| --- | --- | --- |
| CREATED | playback, start, cancel | Chưa được ghi phản ứng/complete |
| PLAYING | heartbeat, response, renew playback, complete, interrupt, cancel | Chỉ complete khi playback hợp lệ tới cuối |
| INTERRUPTED | playback, start tại resumeAtMs, cancel | Giữ checkpoint và phản ứng đầu tiên; playbackEventId mới |
| COMPLETED | get/result, retry complete/Space đã ghi | Snapshot, phản ứng, điểm và thời gian terminal bất biến |
| CANCELLED | get, retry cancel | Không có Result, không tính tiến độ |

Luồng frontend:

1. Đọc Detail/catalogue, POST create với eventId. GET playback cho đúng attempt, chờ video sẵn sàng và đặt currentTime=resumeAtMs do server trả. Không cho phép người dùng tự seek; việc đặt resume position là điều khiển của ứng dụng.
2. Khi bắt đầu phát liên tục, POST start với nonce và resumeAtMs. Gửi heartbeat khoảng 3 giây bằng media currentTime thực tế; dùng `Math.floor(currentTime*1000)` và chặn double binding Space. Mỗi lệnh logic có eventId ổn định qua retry.
3. Ghi Space đầu tiên bằng PUT response. Giá trị chấm điểm là mediaTimeMs của phản ứng, không bị thay bằng đồng hồ server. Server đối chiếu segment anchor/thời gian đã trôi, vị trí đơn điệu và lease heartbeat; cho phép sai số frame/network 1500 ms và phản ứng tới chậm tối đa 3000 ms. Sai lệch lớn/seek/pause không được chấp nhận.
4. Khi video ended, POST complete với vị trí cuối thật. Cần lease còn sống, checkpoint liên tục, vị trí trong 250 ms cuối và thời gian segment hợp lệ đã tới cuối. Chỉ gửi ended hoặc đợi hết thời lượng mà không heartbeat đều không đủ.
5. Khi video error, pause, seek hoặc mất mạng/rời trang, báo interrupt nếu gửi được: reason VIDEO_ERROR/NETWORK/PAGE_EXIT/PAUSED/SEEK. Đọc lại attempt khi reconnect; requiresResume=true nghĩa là phải interrupt nếu còn PLAYING, lấy playback mới và start từ checkpoint. Nonce cũ bị từ chối sau interrupt. Không tạo attempt mới tự động khi reload.
6. Complete timeout: GET attempt; nếu COMPLETED lấy Result, nếu chưa thì retry cùng complete event/payload hoặc báo lỗi/khôi phục theo trạng thái. Muốn luyện lại chủ động POST create với eventId mới.

Đây là kiểm tra tính liên tục hợp lý cho Ôn tập. Server không chứng minh được người dùng thực sự nhìn video hoặc ngăn client cố tình giả lập toàn bộ giao thức theo thời gian thật. Thi chính thức/DRM không thuộc Card 01 này.

## Part 3 — Data Interaction và scoring

V31 thêm simulation_attempts, simulation_attempt_events, simulation_bookmarks, simulation_cursors và trigger bảo vệ. Snapshot tình huống, version và binding bất biến; event append-only, first response không sửa; terminal không ghi lại. FK event/cursor bảo đảm thuộc cùng user. Không sửa bảng hay migration lý thuyết, không gửi outbox sang Progress/Result cho Card 01.

Score từ timeline snapshot: 5/4/3/2 lấy khoảng `[start,nextStart)`; mức 1 gồm endpoint cuối. Ngoài timeline hoặc không phản ứng là 0 sau complete hợp lệ. Tiến độ lấy distinct scenario có COMPLETED theo user+bankVersion, không dùng bestScore>0. Luyện lại tăng completedAttemptCount và bestScore tối đa, không tăng số tình huống đã luyện hai lần. Completed/cursor/event commit trong cùng transaction; cursor chỉ tự tiến nếu nó vẫn gắn với attempt đang complete, tránh ghi đè lượt mới ở tab khác. Các GET tổng hợp dùng snapshot DB REPEATABLE_READ và không cache.

## Part 4 — Errors, deployment và nghiệm thu

400 cho payload/query/UUID/version/vị trí ngoài video; 401 cho JWT không hợp lệ; 403 cho thiếu quyền/hạng không hỗ trợ; 404 cho attempt không thuộc user hoặc nội dung không tồn tại; 503 cho dependency/feature tắt. Các lỗi 409:

| Code | Hành động frontend |
| --- | --- |
| SIMULATION_EVENT_CONFLICT | Dừng retry với payload khác; eventId phải đại diện một lệnh |
| SIMULATION_PLAYBACK_REQUIRED | Lấy URL mới trước start |
| SIMULATION_ALREADY_STARTED | Đọc lại attempt thay vì reset timer |
| SIMULATION_RESUME_POSITION / SIMULATION_PLAYBACK_CONFLICT | Đọc nonce/checkpoint mới |
| SIMULATION_PLAYBACK_INTERRUPTED / SIMULATION_PLAYBACK_DISCONTINUITY | Khôi phục playback từ checkpoint, không complete giả |
| SIMULATION_NOT_PLAYING / SIMULATION_VIDEO_NOT_FINISHED | Giữ trạng thái hiện tại, gửi lệnh đúng vòng đời |
| SIMULATION_RESULT_NOT_READY | Reconcile attempt, chưa hiển thị kết quả |
| SIMULATION_TERMINAL | Đọc Result hoặc chủ động tạo lượt mới |
| SIMULATION_CURSOR_CONFLICT | Đọc revision mới trước ghi cursor |
| SIMULATION_BANK_CHANGED | Dừng cấp playback, kiểm tra snapshot/version |

Triển khai: Check toàn bộ test trên bản sao → Apply/Verify source → dừng Practice → DbCheck xác thực đủ SQL V1–V30 và V27.1, checksum không đổi → DbMigrate native Flyway chỉ áp dụng V31 → chạy lại Practice. Công cụ có thể dùng trực tiếp từ repo: [hướng dẫn migration](../../tools/simulation-learning-migration/README.md). Nếu Flyway startup đã ghi V31 thành công thì DbCheck nhận diện, DbMigrate chạy lại sẽ bỏ qua; không repair/baseline hoặc sửa SQL cũ.

`simulation.learning-enabled` mặc định true cho bản có đầy đủ API này; có thể đặt false để tắt các thao tác học. Chỉ chạy source mới sau khi V31 được xác nhận. Readiness ở license-catalog vẫn cần ngân hàng PUBLISHED; tính sẵn sàng của dependency được kiểm tra khi thao tác.

Test bao gồm ranh giới 0–5, khóa concurrency/idempotency, ownership, seek/gap/ended, error/resume, result snapshot, tiến độ 0 điểm, bookmark/cursor và bảo toàn data/history lý thuyết trên PostgreSQL riêng. Công cụ runtime tạo hai attempt completed (0 và 5 điểm) cùng một attempt cancelled trên tài khoản test, không xóa lịch sử; nó kiểm tra API và MP4 Range thật. Đây là probe giao thức theo thời gian thật, không phải phát video trong trình duyệt. Nhập thêm tài khoản test thứ hai để kiểm tra ownership runtime. Nghiệm thu web, Space/fullscreen/seek, reload và giao diện vẫn là bước riêng sau khi frontend nối API; không tự đánh dấu FullCard01Accepted.


## Replay kết quả đã hoàn thành

Card02 bổ sung GET /api/v1/simulations/attempts/{attemptId}/replay để cấp URL mới cho owned COMPLETED, chỉ đọc, không đổi attempt/nonce/grantedAt/result/progress. Dùng simulations:read + media:read; không cần simulations:learn. Endpoint /playback phía trên vẫn dành cho learning nonterminal. [DTO, policy và lỗi replay](simulation_review_needed.md).


### Danh sách bookmark toàn ngân hàng — Card 03

[Card 03](simulation_bookmarks.md) bổ sung hai GET đọc bookmark đã công bố, dùng nguyên PUT/bookmark store ở đây. Bookmark chưa luyện và best 5 vẫn xuất hiện; luyện lại không tự bỏ dấu. Không có migration hay lượt luyện riêng.


## Controlled video shortcuts — explicit opt-in

The legacy flow above remains the default. See [simulation-video-shortcuts.md](simulation-video-shortcuts.md) and its OpenAPI for V34-backed enable/skip-intro/advance-after-reaction. Only an acknowledged skip grants START at10000; allowSeek remains false. An acknowledged advance may complete one exam position before ended without inventing media duration. Existing scoring and history are unchanged.
