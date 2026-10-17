# Chạy DTS Practice trên máy mới

## 1. Phạm vi và điều kiện

Repo `dts-practice` dùng Java21, Spring Boot3.3.2, Maven Wrapper/Maven3.9.9, PostgreSQL, Redis và Kafka. Port Practice local là8087; PostgreSQL container `dts-practice-postgres` publish5433 vào5432, database `dts_practice`. Python dùng cho công cụ nhập dữ liệu; không phải dependency của ứng dụng Java.

Source chức năng nằm dưới `src/main/java/com/dts/practice`, SQL có version nằm dưới `src/main/resources/db/migration`, dữ liệu bộ đề dưới `data/preset-exams`, công cụ nhập dưới `tools`. Tài liệu [PRACTICE_FEATURES.md](PRACTICE_FEATURES.md) và [api-blueprint/README.md](api-blueprint/README.md) giải thích luồng hiện hành. `.agents` là Git submodule tài liệu, không phải dependency runtime.

Để có chức năng giống môi trường đang chạy, cần cả schema, dữ liệu ngân hàng, media và các service liên quan. Git không chứa database người dùng, lịch sử thi hay secret local. Không đưa dump dữ liệu tài khoản hoặc file `.env` lên Git.

## 2. Vai trò các service

| Service | URL local Windows | Vai trò đối với Practice |
| --- | --- | --- |
| Identity | http://localhost:8081 | Đăng nhập, JWT và tài khoản |
| Content Builder | http://localhost:8082 | Nguồn ngân hàng câu hỏi, biển báo, mẹo published để import |
| Progress | http://localhost:8083 | Nhận sự kiện học/luyện qua HTTP outbox có internal key |
| Result | http://localhost:8086 | Consume learning-results, lịch sử hợp nhất/EXP/analytics |
| Practice | http://localhost:8087 | Học, ôn và thi thử |
| Examination | http://localhost:8088 | Thi chính thức; là service riêng |
| Media | http://localhost:8080 | Nội dung media do Content Builder tham chiếu |
| Gateway | http://localhost:8888 | Entry point cho frontend |
| Kafka | localhost:9092 | Broker gửi sự kiện Result; hạ tầng hiện dùng kèm Zookeeper |
| Redis | localhost:6379 | Cache và hạ tầng liên quan theo cấu hình |

Khởi động hạ tầng theo compose của nhóm và biến môi trường phù hợp. Chỉ dùng một instance/container cho mỗi port; không chạy đồng thời các compose local/infra/prod nếu cùng publish port. Profile Docker dùng hostname container; chạy Java trực tiếp trên Windows dùng localhost và port đã publish. Không dùng hostname `postgres`/`dts-practice` trong cấu hình Java host nếu máy không resolve được chúng.

## 3. Cấu hình trong PowerShell

Thực hiện trong repo Practice. Java và Maven Wrapper có thể cần mạng để tải dependency lần đầu. File `.env` không tự được Spring Boot đọc khi chạy bằng Maven; cần export environment hoặc cơ chế cấu hình IDE tương đương. `.env.example` cũ ghi SERVER_PORT=8082/DB_PORT=5432; với bố trí host ở tài liệu này, override thành8087/5433 như bên dưới. Không ghi đè file secret đang dùng để lấy ví dụ.

```powershell
Set-Location 'D:\DTS\dts-practice'
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:SERVER_PORT = '8087'
$env:DB_HOST = 'localhost'
$env:DB_PORT = '5433'
$env:DB_NAME = 'dts_practice'
$env:DB_USERNAME = 'postgres'
$env:KAFKA_BOOTSTRAP_SERVERS = 'localhost:9092'
$env:REDIS_HOST = 'localhost'
$env:REDIS_PORT = '6379'
$env:IDENTITY_SERVICE_URL = 'http://localhost:8081'
$env:EUREKA_ENABLED = 'false'
$env:JWT_ISSUER = 'dts-identity-service'
$env:PROGRESS_SERVICE_URL = 'http://localhost:8083'
$env:EXAM_PROGRESS_OUTBOX_ENABLED = 'true'
# Chỉ Flyway quản lý schema trong lệnh chạy của tài liệu này.
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'none'
```

Điền DB_PASSWORD, JWT_ACCESS_SECRET, REDIS_PASSWORD (nếu Redis yêu cầu) và EXAM_PROGRESS_INTERNAL_KEY bằng cấu hình riêng được nhóm cấp. JWT secret phải trùng bên phát token; internal key phải trùng Progress. Không dùng chuỗi placeholder làm secret thực. Đoạn nhập ẩn dưới đây không in giá trị ra console:

```powershell
function Read-DtsSecret([string]$Name) {
    $value = Read-Host $Name -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($value)
    try { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}
$env:DB_PASSWORD = Read-DtsSecret 'DB_PASSWORD'
$env:JWT_ACCESS_SECRET = Read-DtsSecret 'JWT_ACCESS_SECRET (same as Identity)'
$env:EXAM_PROGRESS_INTERNAL_KEY = Read-DtsSecret 'EXAM_PROGRESS_INTERNAL_KEY (same as Progress)'
# Nếu Redis không đặt mật khẩu, không nhập một mật khẩu giả.
```

Khởi động Progress/Result không phải điều kiện để mọi API Practice trả HTTP200, nhưng cần để tiến độ và projection toàn hệ thống theo kịp. Nếu chưa cấu hình internal key thì giữ EXAM_PROGRESS_OUTBOX_ENABLED=false, sau khi cấu hình đúng mới bật. Không bỏ key validation để làm worker chạy.

## 4. Migration và khởi động

Đọc [db/MIGRATION_CATALOG.md](db/MIGRATION_CATALOG.md) trước khi nâng cấp DB có dữ liệu. Repo có V1…V30 và version27.1. Không đổi tên/chỉnh SQL đã chạy, không xóa bảng lịch sử Flyway, không dùng Hibernate update thay migration. Bản local hiện có `ddl-auto: update` trong application.yml được giữ nguyên trong đợt chuẩn bị Git; biến môi trường `SPRING_JPA_HIBERNATE_DDL_AUTO=none` ở trên là override chủ động của lệnh chạy này.

Máy mới với database trống: người khởi động chủ động bật Flyway ở lần chạy đầu. Spring Boot sẽ thực thi các file migration còn thiếu trước khi ứng dụng phục vụ request. Đây là **lệnh có thể thay đổi schema/data theo SQL migration**, không phải bước kiểm tra chỉ đọc:

```powershell
$env:SPRING_FLYWAY_ENABLED = 'true'
.\mvnw.cmd spring-boot:run
```

DB hiện hữu đã đến V30 và ứng dụng đang phục vụ đúng: không chạy lại quy trình tạo mới. Trước khi nâng cấp, backup và xác nhận version/checksum; dùng một DB clone để chứng minh migration trước. `baseline-on-migrate` không chứng minh một DB cũ khớp schema. Không repair hoặc đặt baseline tùy ý để bỏ qua lỗi validation.

Sau khi các migration cần thiết đã chạy thành công và được xác nhận, các lần khởi động thông thường có thể tắt Flyway để không thực thi SQL khi chỉ chạy service:

```powershell
$env:SPRING_FLYWAY_ENABLED = 'false'
.\mvnw.cmd spring-boot:run
```

Khi pull thêm migration trong tương lai, bật lại Flyway có chủ đích sau bước review/backup. Với Linux/macOS, dùng `sh ./mvnw spring-boot:run` và export biến môi trường tương đương. JAR bootstrap có sẵn là bản đang dùng local; không thay Maven wrapper hay nâng dependency trong đợt đóng gói này.

## 5. Nhập ngân hàng dữ liệu cho máy mới

Các bước dưới đây là hướng dẫn triển khai; bộ chuẩn bị Git không chạy chúng. V2/V5/V7 chứa dữ liệu legacy, không tự thay thế ngân hàng phiên bản `dts-2026-600-v1`. Cần Content Builder đã có dữ liệu published và Media chứa file thật. Clone Content Builder cạnh Practice để công cụ bộ đề tìm được `../dts-content-builder/data/question-banks/2026/600`.

Đăng nhập bằng tài khoản ADMIN của môi trường mới, đặt access token tạm vào `DTS_IMPORT_TOKEN` trong terminal riêng. Có thể dùng `Read-DtsSecret` ở trên, không dán token vào source hoặc command history:

```powershell
$env:DTS_IMPORT_TOKEN = Read-DtsSecret 'DTS_IMPORT_TOKEN (Identity admin access token)'
```

Đồng bộ600 câu hỏi từ Content Builder local8082 đến Practice8087. Công cụ này hiện cố định hai URL local trong source; không có flag URL để tự đổi tên:

```powershell
py -3 tools\sync_question_bank_2026.py
# Sau khi phần đọc/validation nguồn thành công và đúng môi trường:
py -3 tools\sync_question_bank_2026.py --apply
```

Manifest bộ đề đã nằm trong Git, có digest nguồn và ID ổn định. Generator/checker không tạo dữ liệu trong DB bằng cách chạy schema migration; publish là thao tác dữ liệu qua công cụ hiện có. Công cụ dùng Docker CLI và container PostgreSQL local được chỉ định; xem `--help` trước khi đổi container:

```powershell
py -3 tools\generate_preset_exam_sets_2026.py --dry-run
py -3 tools\check_preset_exam_sets_2026.py --container dts-practice-postgres
py -3 tools\publish_preset_exam_sets_2026.py --container dts-practice-postgres
# Chỉ khi kế hoạch publish hợp lệ và đúng DB:
py -3 tools\publish_preset_exam_sets_2026.py --container dts-practice-postgres --apply
```

Publish bộ đề là bước seed dữ liệu nghiệp vụ sau migration, không sửa schema. Không chạy lại để "sửa" kết quả người dùng hoặc thay bộ đề đang có lịch sử. Những dữ liệu môi trường/seed được nhập bằng công cụ này không phải bản dump local phải copy tay.

Đồng bộ biển báo và mẹo sau khi Content Builder/Media đã được triển khai theo tài liệu repo đó. Import chỉ nhận nguồn published đúng số lượng/bank, không nhập fixture test vào DB học viên:

```powershell
py -3 tools\sync_reference_content_2026.py
py -3 tools\sync_reference_content_2026.py --kind signs --apply
py -3 tools\sync_reference_content_2026.py --kind tips --apply
Remove-Item Env:DTS_IMPORT_TOKEN
```

Mặc định tool reference dùng CB127.0.0.1:8082 và Practice127.0.0.1:8087; có `--content-builder-url`/`--practice-url`. Apply phải chọn một kind, không apply cả hai trong một lệnh. Công cụ bảo vệ remote/redirect; giữ nguyên các bảo vệ hiện có.

## 6. Gateway và frontend

Trên máy chạy Gateway trực tiếp Windows, đặt PRACTICE_SERVICE_URL=http://localhost:8087; các URL service khác cũng phải theo port thực tế thay vì tên DNS Docker. Các biến này phải nằm trong chính terminal/IDE khởi động Gateway. Thay biến ở một terminal khác không đổi environment của process đang chạy.

Tài liệu này chỉ chuẩn bị repo Practice. Không cần restart service đang chạy để áp dụng tài liệu/.gitignore/.gitattributes hay tạo commit. Trước khi đóng repo, người phụ trách tự retest giao diện như quy trình Content Builder; không chạy công cụ tạo fixture hay replay Kafka chỉ để chuẩn bị push.

## 7. Dữ liệu local và file loại trừ

`.env`, build target, log/crash log, cache Python và handoff cũ không đưa lên Git. Giữ JAR Maven wrapper, migration, test source/fixture, preset manifest, tool import, code và tài liệu. Không xóa image/static assets chỉ vì ít được sửa; URL ảnh câu hỏi legacy và media ID của bank mới có nguồn phục vụ khác nhau.

Push source không mang theo tài khoản, mật khẩu, lịch sử/tiến độ local hoặc object storage. Máy mới tự tạo tài khoản theo Identity và import nội dung theo các bước trên; lịch sử phát sinh khi sử dụng chức năng. Việc nhìn thấy dữ liệu test trên máy hiện tại không có nghĩa dữ liệu đó cần nằm trong repo.
