# Nâng cấp schema học mô phỏng V31

Runner Python và Java test-tool được commit cùng SQL. Có thể chạy từ repo đã checkout mà không cần ZIP cũ. Yêu cầu Java21, Maven, Python và Docker Desktop; container PostgreSQL local `dts-practice-postgres` đang chạy với DB `dts_practice`, port đã publish và các biến POSTGRES_* hợp lệ. Password chỉ đọc trong bộ nhớ và chuyển qua environment cho JVM con; không in hoặc đặt trong CLI.

Lịch sử được hỗ trợ: đủ 31 migration SQL V1–V30 cùng V27.1, checksum khớp source; hoặc thêm V31 đã thành công. Runner từ chối DB khác/public schema khác, BASELINE/partial history/checksum sai hoặc simulation tables tồn tại thiếu history. Không tự sửa dữ liệu/histories để vượt preflight.

```powershell
& 'D:\setup\python\python.exe' `
  '.\tools\simulation-learning-migration\Run-SimulationLearningMigration.py' `
  --root 'D:\DTS\dts-practice' `
  --audit-root 'D:\DTS-audits\simulation-practice-learning-db' --mode check
```

Runner tạo bản sao source bên ngoài repo, chạy SimulationMigrationToolPostgresTest và lấy runtime classpath, sau đó xác thực DB thật bằng connection read-only và native Flyway validate. Nó không khởi động Spring/service. Check không áp dụng SQL. Khi Check PASS, dùng đúng AuditPath:

```powershell
& 'D:\setup\python\python.exe' `
  '.\tools\simulation-learning-migration\Run-SimulationLearningMigration.py' `
  --root 'D:\DTS\dts-practice' `
  --audit-root 'D:\DTS-audits\simulation-practice-learning-db' `
  --mode migrate --check-audit '<AuditPath của Check>'
```

Migrate kiểm tra lại source/hash/Git từ Check, validate toàn bộ history và gọi Flyway target31 với outOfOrder=false, baselineOnMigrate=false, cleanDisabled=true. Khi lịch sử cũ đủ, chỉ V31 được chạy; lặp lại skip. V31 chỉ tạo bốn bảng simulation, index/identity sequence và hai function/trigger mới. Không tác động bảng lý thuyết. Giữ result.json/migration.log/maven.log trong AuditPath để bàn giao. Native validation dùng read-only connection; migrate có write connection riêng và khóa history của Flyway.

Gói patch cung cấp DbCheck/DbMigrate và tái sử dụng candidate audit đã chạy đủ test để không chạy lại cùng bộ test. Standalone runner mặc định tự kiểm thử tool trước live DB. Source SQL được pin SHA256 chuẩn hóa LF; không dùng repair/baseline/clean hoặc chỉnh sửa migration cũ. Nếu DbMigrate lỗi/timeout, đọc lại DbCheck trước khi quyết định bước tiếp; không suy đoán DB chưa thay đổi. Dữ liệu học và lịch sử completed không có thao tác xóa trong công cụ này.
