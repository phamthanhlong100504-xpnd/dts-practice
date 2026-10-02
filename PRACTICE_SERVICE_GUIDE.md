
## 1. Mục đích

Tài liệu này mô tả:

- Phạm vi nghiệp vụ hiện tại của `dts-practice` đã được kiểm thử.
- Các thay đổi backend đã thực hiện để hỗ trợ phân hạng GPLX và hiển thị ảnh câu hỏi.
- Contract dữ liệu giữa Practice Service, frontend và Media Service.
- Cách cần nạp dữ liệu và vận hành để production có thể hiển thị câu hỏi và ảnh giống môi trường local đã kiểm thử.

## 2. Phạm vi nghiệp vụ đã kiểm thử

Các luồng sau đã được chạy thực tế trên local và hoạt động:

### 2.1. Câu hỏi theo hạng GPLX

- Lọc dữ liệu theo `licenseClass`.
- Thống kê tổng số câu hỏi theo hạng.
- Thống kê câu điểm liệt theo hạng.
- Phân bố câu hỏi theo chương.
- Ôn tập theo chương.

### 2.2. Thi thử / luyện tập

- Tạo exam session theo hạng GPLX.
- Lấy danh sách câu hỏi theo đúng hạng.
- Hiển thị câu điểm liệt.
- Chọn và lưu đáp án.
- Chế độ `PRACTICE` trả đúng/sai và giải thích.
- Chế độ `EXAM` không lộ đáp án trong lúc làm.
- Nộp bài.
- Xem kết quả.
- Xem lại bài từ lịch sử.

### 2.3. Media

Ảnh đã được kiểm thử thành công tại:

- Ôn tập theo chương.
- Màn hình làm bài thi/luyện tập.
- Màn hình xem kết quả bài thi.
- Màn hình xem lại bài từ lịch sử.


## 3. Dữ liệu ngân hàng câu hỏi hiện tại

Bank đã kiểm thử:

```text
bank_version = dts-2026-600-v1
```

Thống kê:

```text
total              = 600
with_media_file_id = 318
with_image_url      = 0
```

Điều này có nghĩa:

- 600 câu hỏi nằm trong Practice DB.
- 318 câu có ảnh.
- Ảnh được liên kết qua `media_file_id`.
- `image_url` không phải nguồn ảnh chính của bank hiện tại.


## 4. Mô hình dữ liệu media chuẩn

### 4.1. Question entity

`Question` có hai field liên quan media:

```java
@Column(name = "image_url", length = 500)
private String imageUrl;

@Column(name = "media_file_id")
private UUID mediaFileId;
```

Trong dữ liệu bank hiện tại:

```text
imageUrl    = null
mediaFileId = UUID của Media Service
```

### 4.2. Nguyên tắc

Practice Service chỉ giữ:

```text
mediaFileId
```

Practice không cần lưu:

- hostname của MinIO;
- IP VPS;
- URL presigned;
- đường dẫn file local;
- URL phụ thuộc môi trường.

Luồng chuẩn:

```text
Practice DB
Question.mediaFileId
        ↓
Practice API
        ↓
Frontend
        ↓
Media Service
        ↓
Object Storage / MinIO
        ↓
Ảnh hiển thị
```

## 5. Thay đổi backend đã thực hiện

## 5.1. `QuestionResponse`

File:

```text
src/main/java/com/dts/practice/dto/response/QuestionResponse.java
```

Đã bổ sung:

```java
UUID mediaFileId
```

Contract hiện tại:

```java
public record QuestionResponse(
        Integer id,
        Integer chapter,
        String questionText,
        Object options,
        Boolean isCritical,
        String imageUrl,
        UUID mediaFileId,
        String correctAnswer,
        String explanation
) {}
```

Mục đích:

- Giữ `imageUrl` để tương thích.
- Trả `mediaFileId` đúng với dữ liệu thực tế.
- Không dùng `imageUrl` để nhét UUID giả.


## 5.2. `QuestionMapper`

File:

```text
src/main/java/com/dts/practice/mapper/QuestionMapper.java
```

Entity và response đều có field:

```text
mediaFileId
```

nên MapStruct tự map:

```text
Question.mediaFileId
→
QuestionResponse.mediaFileId
```

Không cần gọi Media Service trong mapper.


## 5.3. `QuestionService`

File:

```text
src/main/java/com/dts/practice/service/QuestionService.java
```

Tại chỗ dựng lại `QuestionResponse`, đã giữ thêm:

```java
response.mediaFileId()
```

Ví dụ:

```java
return new QuestionResponse(
        response.id(),
        response.chapter(),
        response.questionText(),
        response.options(),
        isCritical,
        response.imageUrl(),
        response.mediaFileId(),
        response.correctAnswer(),
        response.explanation()
);

Nhờ đó các API câu hỏi theo chương / theo hạng / critical không làm mất media ID.


## 5.4. `ExamService` – Exam Session

File:

```text
src/main/java/com/dts/practice/service/ExamService.java
```

Trong `toSessionQuestion(...)`, đã bổ sung:

```java
response.mediaFileId()
```

Ví dụ:

```java
return new QuestionResponse(
        response.id(),
        response.chapter(),
        response.questionText(),
        response.options(),
        critical,
        response.imageUrl(),
        response.mediaFileId(),
        reveal
                ? response.correctAnswer()
                : null,
        reveal
                ? response.explanation()
                : null
);
```

Nhờ đó ảnh đi theo question trong:

- session mới;
- session resume;
- thi thử;
- luyện tập.

---

## 5.5. `ExamService` – Result / Review

Phần kết quả bài thi dùng:

```java
List<Map<String, Object>> answers
```

nên cần bổ sung riêng:

```java
detail.put(
        "mediaFileId",
        question.getMediaFileId()
);
```

Bên cạnh field cũ:

```java
detail.put(
        "imageUrl",
        question.getImageUrl()
);
```

Nhờ vậy response kết quả cũng có:

```json
{
  "imageUrl": null,
  "mediaFileId": "UUID"
}
```

và frontend có thể tải ảnh trong màn hình xem kết quả.


## 6. Contract frontend cần sử dụng

Frontend ưu tiên:

```text
mediaFileId
```

và chỉ dùng:

```text
imageUrl
```

làm fallback.

Logic:

```tsx
const src = mediaFileId ?? imageUrl;
```

Component media chịu trách nhiệm:

```text
mediaFileId
→ gọi Media API
→ lấy URL hiện tại
→ render ảnh
```

Frontend không nên tự dựng URL MinIO.


## 7. Phân hạng GPLX

Backend hiện hỗ trợ lọc theo `licenseClass`.

Các API câu hỏi cần truyền đúng hạng, ví dụ:

```text
GET /api/v1/questions/stats?licenseClass=A1
GET /api/v1/questions/critical?licenseClass=A1
GET /api/v1/questions/chapter/1?licenseClass=A1
```

Exam Service cũng sử dụng `examType` / hạng GPLX để tạo đúng tập câu hỏi và ma trận tương ứng.

Dữ liệu JSON import phải giữ đúng thông tin phân hạng để backend có thể lọc đúng sau khi nạp production.


## 8. Dữ liệu production cần được chuẩn bị như thế nào

Đầu vào production gồm:

```text
600 câu hỏi JSON
+
folder ảnh
```

Không nên lưu URL local trong JSON:

```text
http://localhost:9000/...
D:\...
presigned-url...
```

### Luồng import mong muốn

```text
Folder ảnh
    ↓
upload/import vào Media Service / object storage
    ↓
tạo Media record
    ↓
nhận media UUID
    ↓
ghi media UUID tương ứng vào Question.media_file_id
```

Sau import phải đảm bảo:

```text
Practice DB
questions.media_file_id = X

Media DB
medias.id = X

Media record X
→ trỏ tới object ảnh thật trong storage
```

## 9. UUID local và UUID production

UUID trên VPS không bắt buộc phải giống UUID local.

Ví dụ local:

```text
question 301
→ media UUID A
```

Production có thể là:

```text
question 301
→ media UUID B
```

Điều bắt buộc là trên cùng môi trường production:

```text
questions.media_file_id
```

phải trỏ tới:

```text
medias.id
```

thực sự tồn tại trên Media Service của production.

Nếu hệ thống import giữ nguyên UUID thì cũng được, miễn dữ liệu Media và Practice đồng bộ.

Sau khi backend mới được triển khai, cần đảm bảo:

1. `dts-practice` chạy với database production.
2. `dts-media` chạy với database/media storage production.
3. Gateway route được tới Practice và Media.
4. Object storage / MinIO được cấu hình đúng.
5. Bucket / quyền truy cập đúng.
6. Nạp bank 600 câu đúng phiên bản.
7. Nạp thông tin phân hạng GPLX.
8. Upload folder ảnh.
9. Tạo media records.
10. Gắn đúng `media_file_id` vào từng question.
11. Media API trả URL mà browser production truy cập được.

## 11. Kiểm tra dữ liệu sau khi import production

Có thể dùng:

```sql
SELECT
    COUNT(*) AS total,
    COUNT(media_file_id) AS with_media_file_id,
    COUNT(image_url) AS with_image_url
FROM questions
WHERE bank_version = 'dts-2026-600-v1';
```

Với bộ dữ liệu đã test local:

```text
total              = 600
with_media_file_id = 318
with_image_url      = 0
```

Nếu production dùng đúng cùng bộ JSON và folder ảnh thì số lượng kỳ vọng phải tương ứng.

## 12. Smoke test production

Sau khi deploy và import dữ liệu:

### 12.1. Practice

1. Login.
2. Vào trang luyện thi.
3. Chọn hạng GPLX.
4. Kiểm tra tổng số câu.
5. Kiểm tra số câu điểm liệt.
6. Mở một chương có câu ảnh.
7. Xác nhận ảnh hiển thị.

### 12.2. Exam

1. Tạo bài thi theo hạng.
2. Kiểm tra đúng số câu.
3. Kiểm tra câu điểm liệt.
4. Tìm câu có ảnh.
5. Xác nhận ảnh hiển thị.
6. Chọn đáp án.
7. Nộp bài.

### 12.3. Result / History

1. Mở kết quả vừa thi.
2. Kiểm tra ảnh trong chi tiết câu.
3. Vào lịch sử.
4. Mở lại bài.
5. Xác nhận ảnh vẫn hiển thị.


## 13. Khi ảnh không hiển thị trên production

Kiểm tra theo thứ tự:

```text
1. Practice response có mediaFileId không?
        ↓
2. mediaFileId có tồn tại trong Media DB không?
        ↓
3. Media API có trả URL không?
        ↓
4. URL đó browser có truy cập được không?
        ↓
5. Object tương ứng có tồn tại trong storage không?
```

Không nên sửa Practice thành URL hard-code để xử lý lỗi hạ tầng.


## 14. Kết luận

Luồng media sau khi sửa:

```text
Question
    ↓
mediaFileId
    ↓
Practice API
    ↓
Frontend
    ↓
Media Service
    ↓
Object Storage
```

