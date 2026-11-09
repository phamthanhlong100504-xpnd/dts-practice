# Catalogue mô phỏng và thumbnail

## Part 0 — Classification & Identity

Module `simulation` trong Practice đọc ngân hàng do Content Builder công bố. Các API catalogue/thumbnail bên dưới giữ vai trò đọc nguồn; lượt luyện, phản ứng, kết quả và tiến độ dùng [API học mô phỏng](simulation_learning.md). Phần học/thi lý thuyết tiếp tục dùng các service, bảng và version hiện hành.

| Method | Native Practice | Qua Gateway | Quyền |
| --- | --- | --- | --- |
| GET | `/api/v1/simulations/catalog` | `/api/practice/v1/simulations/catalog` | `simulations:read` |
| GET | `/api/v1/simulations/scenarios/{scenarioId}/thumbnail` | `/api/practice/v1/simulations/scenarios/{scenarioId}/thumbnail` | `simulations:read` và `media:read` |
| GET | `/api/v1/exams/license-catalog` | `/api/practice/v1/exams/license-catalog` | `practice:read`, như trước |

Gửi `Authorization: Bearer <Identity access JWT>`. Module mới kiểm tra thêm thời hạn, thời điểm phát hành, issuer, chữ ký và UUID subject; không nhận danh tính từ `X-User-Id` hoặc query. Trường hợp quyền mô phỏng thiếu không được thay bằng quyền lý thuyết hoặc role quản trị.

## Part 1 — API Contract

### Catalogue ngân hàng

```http
GET /api/practice/v1/simulations/catalog?licenseClass=B
Authorization: Bearer <access-token>
```

Query `licenseClass` bắt buộc. Hạng được DTS bật mô phỏng: B, C1, C, D1, D2, D, BE, C1E, CE, D1E, D2E, DE. A1/A/B1 thuộc nhóm mô tô trong catalogue hiện hành, không bật mô phỏng. Các hạng được hỗ trợ cùng đọc một ngân hàng; query hạng không tạo bản sao dữ liệu theo hạng.

HTTP 200 bọc `ApiResponse`, `Cache-Control: no-store`. `data` gồm:

| Field | Kiểu | Ý nghĩa |
| --- | --- | --- |
| `userId` | UUID | Tài khoản từ JWT |
| `licenseClass` | string | Hạng đã chuẩn hóa uppercase |
| `bankVersion` / `scoringVersion` | string | Phiên bản snapshot PUBLISHED |
| `manifestSha256` | string | Dấu vân tay binding ngân hàng |
| `bankReady` | boolean | Snapshot hợp lệ, PUBLISHED và là current tại thời điểm đọc |
| `totalScenarios` | int | 120 |
| `chapters` | array | Sáu phần tử `{chapterId:int,name:string,totalScenarios:int}`, đúng thứ tự 1–6 |
| `scenarios` | array | 120 phần tử `{scenarioId:UUID,scenarioNumber:int,chapterId:int,title:string,durationMs:int}`, đúng thứ tự 1–120 |

Catalogue này cung cấp metadata nguồn, không phải API overview/chapter page có tiến độ. Không trả scoringTimeline, hazardSign, recognitionTip, videoMediaId hoặc URL video. Overview/bộ lọc và trạng thái học theo [simulation_learning.md](simulation_learning.md).

### Thumbnail trước attempt

```http
GET /api/practice/v1/simulations/scenarios/<scenario-uuid>/thumbnail?licenseClass=B&bankVersion=dts-simulation-120-card01-v1
Authorization: Bearer <access-token>
```

`scenarioId` phải là UUID chuẩn. `bankVersion` tùy chọn; nên truyền version vừa đọc ở catalogue để cùng một màn hình dùng cùng snapshot. Bỏ version sẽ đọc current. Version cụ thể phải tồn tại và PUBLISHED; ngân hàng cũ đã publish vẫn có thể đọc theo version khi ngân hàng mới trở thành current.

HTTP 200 bọc `ApiResponse`, `data` là `{mediaId:UUID,scenarioId:UUID,bankVersion:string,url:string,expiredAt:ISO-8601 datetime}`. URL có thời hạn do Media trả (hiện tại 300 giây). Gọi lại API để gia hạn; không lưu URL vào ngân hàng hoặc dùng nó làm định danh. Response không cache. API không nhận mediaId, userId hoặc ticket do frontend lựa chọn; binding được tra theo scenarioId trong snapshot đã xác thực.

### Catalogue hạng bằng

API cũ `/exams/license-catalog` giữ mọi field và logic tính số câu/quy tắc thi; thêm:

| Field | Kiểu | Ý nghĩa |
| --- | --- | --- |
| `simulationEnabled` | boolean | Hạng thuộc tập DTS hỗ trợ mô phỏng |
| `simulationReady` | boolean | Hạng được hỗ trợ, có ngân hàng hợp lệ và tính năng luyện đã được bật |
| `simulationBankVersion` | string/null | Version PUBLISHED khi hạng được hỗ trợ và nguồn sẵn sàng |

`simulation.learning-enabled` mặc định `true` từ bản có V31 và API luyện tập. Chỉ chạy bản mới sau khi xác nhận V31. Có thể đặt false để tắt thao tác học. `bankReady=true` của catalogue nguồn chỉ xác nhận snapshot nguồn hợp lệ. Khi Content Builder không sẵn sàng, các field mô phỏng báo chưa ready/null nhưng dữ liệu lý thuyết vẫn trả như trước. API mô phỏng trả 503 rõ ràng khi nguồn lỗi, không trả danh sách rỗng thay lỗi.

## Part 2 — Processing Specification

1. Practice dùng service token riêng để GET `/api/v1/content-builder/internal/simulation-banks/current` hoặc `/{bankVersion}`. Không chuyển access JWT của học viên sang API nguồn này.
2. Token theo protocol Content Builder đã triển khai: HS256; header `typ=dts-service+jwt`; issuer `dts-practice`; subject `simulation-bank-reader`; audience chỉ `dts-content-builder`; purpose `SIMULATION_SOURCE_READ`; jti UUID; iat/exp 45 giây. Key derive từ Base64 `jwt.access-secret` với context `dts:content-builder:simulation-source:v1`.
3. Chỉ chấp nhận PUBLISHED có publishedAt; current khi đọc current; version khớp khi pin. Kiểm tra đúng 6 chương với số lượng 29/14/20/10/17/30, 120 scenario theo thứ tự, ID duy nhất và 240 media binding khác nhau. Timeline tăng nghiêm ngặt, nằm trong thời lượng nguồn MP4; nguồn có hash và MIME phù hợp.
4. Để cấp thumbnail, Practice tra tình huống trong snapshot, ký ticket catalogue gắn sub người dùng, scenarioId, thumbnailMediaId và bankVersion. Media kiểm tra ticket riêng, cùng Bearer JWT của học viên, rồi cấp URL; key/purpose không dùng chung với playback của attempt.
5. Client HTTP chỉ gọi các service origin cấu hình, không theo redirect và giới hạn response 2 MiB. Connect timeout 3 giây, request timeout 8 giây. Không cache snapshot current, không log token, signed URL hoặc response nhạy cảm.

## Part 3 — Data Interaction

Không có migration hoặc ghi bảng Practice trong bước này. Catalogue lý thuyết vẫn đọc `QuestionRepository` hiện hành. Các bảng questions, exams, theory learning, receipt/outbox không đổi. Token nguồn và thumbnail chỉ tồn tại trong request, không lưu DB. Login của công cụ runtime tạo session Identity bình thường; không yêu cầu logout toàn bộ phiên.

## Part 4 — Operational Notes

| Property / environment | Mặc định |
| --- | --- |
| `simulation.content-origin` / `SIMULATION_CONTENT_ORIGIN` | `http://localhost:8082` |
| `simulation.media-origin` / `SIMULATION_MEDIA_ORIGIN` | `http://localhost:8080` |
| `simulation.learning-enabled` / `SIMULATION_LEARNING_ENABLED` | `true` từ bản V31 |

JWT secret lấy cấu hình Practice đang dùng, trùng khóa access của Identity và khóa nguồn Content Builder/Media. Không ghi secret vào Git hoặc thay bằng giá trị ví dụ. Nếu chạy container, cấu hình origin theo hostname service thực tế; origin chỉ có scheme/host/port, không có credentials/path/query.

Sau Apply+Verify bản học mô phỏng, xác nhận DbCheck/DbMigrate V31 trước khi restart Practice; Media tiếp tục dùng source đã nghiệm thu. Không migrate lại V7 Media hoặc V6 Content Builder. Không tự repair/baseline/clean để xử lý lỗi.

| HTTP / code | Trường hợp |
| --- | --- |
| 400 `SIMULATION_INVALID_REQUEST` | Thiếu hạng, UUID/version không hợp lệ |
| 401 `SIMULATION_UNAUTHORIZED` hoặc security handler | Thiếu hoặc sai access JWT, token không có expiry |
| 403 `ACCESS_DENIED` / `SIMULATION_FORBIDDEN` | Thiếu quyền mô phỏng/media |
| 403 `SIMULATION_LICENSE_NOT_SUPPORTED` | Hạng không hỗ trợ |
| 404 `SIMULATION_NOT_FOUND` | Version chưa publish/không tồn tại hoặc scenario không thuộc snapshot |
| 503 `SIMULATION_DEPENDENCY_UNAVAILABLE` | Nguồn current không có, lỗi kết nối/contract, Media chưa đáp ứng hoặc URL không hợp lệ |

Kiểm thử Java: bank validation, protocol HMAC qua loopback HTTP, Spring Security với JWT ký thật, khả năng catalogue và các test hồi quy lý thuyết. Các fixture HTTP là kiểm thử adapter; nghiệm thu tích hợp còn cần chạy thật qua Gateway/Identity/Content Builder/Media/MinIO. Công cụ runtime đọc đúng manifest đã publish trên máy và kiểm tra WebP Range/unsigned denial; không tạo lượt luyện hoặc điểm. Full Card 01 và UI vẫn chờ phần attempt/tiến độ cùng nghiệm thu trên web.
