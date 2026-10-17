# Danh mục migration Practice

31 file SQL tương ứng V1…V30 và V27.1. Các file hiện có được giữ nguyên byte; không tạo migration giả hoặc viết lại lịch sử để đóng gói Git. SHA256 dưới đây dùng so khớp file, không thay thế checksum Flyway.

| Version | File | Chức năng | SHA256 |
| --- | --- | --- | --- |
| 1 | [V1__init_practice_schema.sql](../../src/main/resources/db/migration/V1__init_practice_schema.sql) | init practice schema | `b31c1a47bd121e3c098eb63831ee304d95c0c6d33fed234830a089b67939a67d` |
| 2 | [V2__seed_questions.sql](../../src/main/resources/db/migration/V2__seed_questions.sql) | seed questions | `b41a55f005464d9cbf922e2334bfed34a360ce416bb9dc44da92af16ce85b92b` |
| 3 | [V3__add_time_limit_and_mode.sql](../../src/main/resources/db/migration/V3__add_time_limit_and_mode.sql) | add time limit and mode | `10a5c572409d735784386aa32b4d690a10b540bc69c9e195dcb97ee36816a523` |
| 4 | [V4__add_updated_at.sql](../../src/main/resources/db/migration/V4__add_updated_at.sql) | add updated at | `bb235b150dc9ac9f46b66f350e82837f983fbe5fd2598f242e7a5d67f9c06861` |
| 5 | [V5__seed_question_images.sql](../../src/main/resources/db/migration/V5__seed_question_images.sql) | seed question images | `e14bf5dc5306ea8f9e7f79f8ba743ffea8204742baa346f7949a59e9b8062601` |
| 6 | [V6__add_username_to_exams.sql](../../src/main/resources/db/migration/V6__add_username_to_exams.sql) | add username to exams | `745482598d5871269680965d3321c8c6243dd8c532ae3bb89662392e1f4e744a` |
| 7 | [V7__seed_question_explanations.sql](../../src/main/resources/db/migration/V7__seed_question_explanations.sql) | seed question explanations | `17d4ba8131a75d9674b03573e4935bc83d1d93e26d4a69511dc2ed24f6148145` |
| 8 | [V8__add_full_name_to_exams.sql](../../src/main/resources/db/migration/V8__add_full_name_to_exams.sql) | add full name to exams | `2f83e8775f3aa79f00c6f9362596310f9e304c1adeb7110df14a75427e6f435d` |
| 9 | [V9__version_question_bank.sql](../../src/main/resources/db/migration/V9__version_question_bank.sql) | version question bank | `4510deede376974e6dcb46ec0bdb96d64b573f679b667f35a790e42c8e77baf8` |
| 10 | [V10__generate_question_ids.sql](../../src/main/resources/db/migration/V10__generate_question_ids.sql) | generate question ids | `cda610a6d295f46b5afc3cce06372b632e7d7f340ccf0de6a4434a97a3131d12` |
| 11 | [V11__version_exams_and_expand_license_classes.sql](../../src/main/resources/db/migration/V11__version_exams_and_expand_license_classes.sql) | version exams and expand license classes | `27f6dca8c9495d2f94d0dc80f36536127f4e0bfa197cfe796b9e9842f9559097` |
| 12 | [V12__critical_question_progress.sql](../../src/main/resources/db/migration/V12__critical_question_progress.sql) | critical question progress | `867bb4bd7eec8329711b32b4ee94a9dcb8c91aa4d844c37df3fa1ad14394fa61` |
| 13 | [V13__add_preset_exam_sets.sql](../../src/main/resources/db/migration/V13__add_preset_exam_sets.sql) | add preset exam sets | `da6425335089ad1718a8cba955529a0722463003a4c45dbb97b9b26be52bcbda` |
| 14 | [V14__one_active_exam_per_user_license.sql](../../src/main/resources/db/migration/V14__one_active_exam_per_user_license.sql) | one active exam per user license | `e77b0a66dc953d166b4af167144468c4c6378a085eb6d02cafd28c8937cd3728` |
| 15 | [V15__pause_exam_sessions.sql](../../src/main/resources/db/migration/V15__pause_exam_sessions.sql) | pause exam sessions | `73f18e057705cd195a1f575f3a04f12bd12d84db36ad9a0a68c3168e56a8e99f` |
| 16 | [V16__persist_exam_question_flags.sql](../../src/main/resources/db/migration/V16__persist_exam_question_flags.sql) | persist exam question flags | `1a011be2aeda81bad66fdf4c3ebc6c169b84310c3aa8ba5d7122b57ee76124f5` |
| 17 | [V17__chapter_learning_progress.sql](../../src/main/resources/db/migration/V17__chapter_learning_progress.sql) | chapter learning progress | `30fae1b9de93fc3995608f850b9493874773b453dc64b6feb5dbac2551c66b8a` |
| 18 | [V18__chapter_question_bookmarks.sql](../../src/main/resources/db/migration/V18__chapter_question_bookmarks.sql) | chapter question bookmarks | `70db6441a8ca4f61918cf4499ee3ba3d73beb0c1fb413c832498ff09198c7c87` |
| 19 | [V19__wrong_question_progress.sql](../../src/main/resources/db/migration/V19__wrong_question_progress.sql) | wrong question progress | `64950442408355bf7fcd870dd0553133ffc120a282b44ea268414a677116811c` |
| 20 | [V20__wrong_question_review_sessions.sql](../../src/main/resources/db/migration/V20__wrong_question_review_sessions.sql) | wrong question review sessions | `f1467db0b66c2b661eeb03041fa4b6687f60f2af718f0df8e389536d869bb3f4` |
| 21 | [V21__critical_question_sessions.sql](../../src/main/resources/db/migration/V21__critical_question_sessions.sql) | critical question sessions | `f4ad54d92a3ca84fa13c48598aef07fbef7ffe115b39b1f6e3588495d989e66c` |
| 22 | [V22__critical_review_restart.sql](../../src/main/resources/db/migration/V22__critical_review_restart.sql) | critical review restart | `c9b639c950780ed34e2acba7c77062133a29c9af22549a56305d859537b5da79` |
| 23 | [V23__exam_progress_receipts.sql](../../src/main/resources/db/migration/V23__exam_progress_receipts.sql) | exam progress receipts | `b6a5d663bc613d5803095885500b21a251912b1885e99cfe5b6493c7a76a3827` |
| 24 | [V24__learning_progress_outbox.sql](../../src/main/resources/db/migration/V24__learning_progress_outbox.sql) | learning progress outbox | `0eadd359c4ab4be1d100034f230525775cedc50f58b6b3e9dce19b2cef2a5036` |
| 25 | [V25__question_bank_chapters.sql](../../src/main/resources/db/migration/V25__question_bank_chapters.sql) | question bank chapters | `8a4ec03329a3357df806a1fa157999cfdc47795b0d114834cec16db8e97ed865` |
| 26 | [V26__chapter_review_sessions.sql](../../src/main/resources/db/migration/V26__chapter_review_sessions.sql) | chapter review sessions | `162dde998e0b3509e5fe8a1873a8eef5b9308597ca2b2413168b4cd885f046bb` |
| 27 | [V27__add_reference_content_tables.sql](../../src/main/resources/db/migration/V27__add_reference_content_tables.sql) | add reference content tables | `f04c39f5e402a68a55202ee6490744df4748f3a4e80623d8fdd0d5241fde6a2a` |
| 27.1 | [V27_1__preserve_exam_audit_during_history_upgrade.sql](../../src/main/resources/db/migration/V27_1__preserve_exam_audit_during_history_upgrade.sql) | preserve exam audit during history upgrade | `cdd8877216513b4324fb553c190d0c3fd9b33c1b7b0a8011b53b5b0aca466268` |
| 28 | [V28__practice_result_history_outbox.sql](../../src/main/resources/db/migration/V28__practice_result_history_outbox.sql) | practice result history outbox | `efbe7b5c234925a36dbb0e5711a90bda84868a832c35c6226fb1ddc9336a8ec3` |
| 29 | [V29__restore_exam_audit_after_history_upgrade.sql](../../src/main/resources/db/migration/V29__restore_exam_audit_after_history_upgrade.sql) | restore exam audit after history upgrade | `479351f671a9a3454568b7be1ef20a676552156bbf005fc84555a41adfd1c0ed` |
| 30 | [V30__question_exp_outbox.sql](../../src/main/resources/db/migration/V30__question_exp_outbox.sql) | question exp outbox | `59a37ad3d9faaf629ec43498c38edbbcfdaddc95f0d3ee93f4dd5f4872656ac2` |

## Trình tự và audit

Flyway sắp version theo số, không theo tên file alphabet: V27 → V27_1 (27.1) → V28 → V29 → V30. V27.1 giữ audit trong nâng cấp lịch sử V28; V29 khôi phục trigger audit gốc sau nâng cấp. Cả hai cần được commit cùng dòng migration, không gộp lại hoặc xóa sau khi local đã chạy.

V13/V27 tạo cấu trúc bộ đề và nội dung tham chiếu; import/seed dữ liệu là bước riêng. V28 còn thiết lập outbox/đóng băng dữ liệu lịch sử; V30 tạo capture/outbox EXP mới. SQL có chức năng/trigger không biểu diễn đầy đủ bằng Hibernate entity, vì vậy tự tạo bảng từ entity không tái tạo hệ thống.

Không sửa/xóa/chạy lại V1…V30 để sửa ba phiên TIMEOUT lịch sử có timestamp ngược đã được chấp nhận giữ nguyên. Không repair Flyway để che checksum sai. Đối với DB đã chạy, migration kế tiếp phải dùng version mới sau khi review/backup/clone proof.

Gói chuẩn bị Git này không chạy migration, không tạo DB, không đổi dữ liệu nghiệp vụ. Cách chủ động chạy SQL bằng Flyway lúc triển khai có trong [LOCAL_SETUP.md](../LOCAL_SETUP.md).
