# Chức năng và nghiệp vụ DTS Practice

Tài liệu mô tả source đang được chuẩn bị đưa lên nhánh feature hiện tại, không thay đổi nghiệp vụ đã chạy trên web.

## Ngân hàng và cấu hình

Bản ghi ngân hàng có bankVersion, sourceQuestionId, hạng áp dụng và điểm liệt theo hạng. Giữ ID nội bộ để lịch sử cũ tham chiếu đúng câu. Catalogue/phân bổ đề đọc từ backend.

- Service: `QuestionService, QuestionBankImportService, CurrentQuestionBank, LicenseCatalogService, LearningBankCatalogService, ExamMatrix`.
- Dữ liệu chính: `questions, question_bank_chapters`.

## Học theo chương

Ghi đáp án và chấm chuẩn, số câu đã học dựa trên câu riêng biệt. Phục hồi cursor/answers và bookmark độc lập. Sinh sự kiện tiến độ trong transaction.

- Service: `ChapterLearningService, ChapterLearnedAnswerQueryService, ChapterBookmarkService`.
- Dữ liệu chính: `chapter_question_progress, chapter_learning_cursor, chapter_question_bookmarks`.

## Ôn theo chương

Chỉ mở vòng khi đã học đủ chương; snapshot câu hỏi theo hạng/bank, trả409 CHAPTER_LEARNING_INCOMPLETE nếu chưa đủ. Có answer/cursor/finish riêng.

- Service: `ChapterReviewSessionService`.
- Dữ liệu chính: `chapter_review_sessions, chapter_review_session_questions`.

## Câu điểm liệt

CONTINUE tiếp tục học tích lũy; REVIEW chỉ sau khi đã học đủ. Restart REVIEW đánh dấu phiên cũ SUPERSEDED trong cùng transaction tạo phiên mới.

- Service: `CriticalQuestionProgressService, CriticalSessionService, CriticalAnswerService, CriticalCursorService`.
- Dữ liệu chính: `critical_question_progress, critical_question_sessions, critical_question_session_questions`.

## Câu sai

Theo dõi trạng thái cần ôn, snapshot danh sách vào vòng, giữ đáp án sai trước. Không ghi đè lịch sử thi gốc khi ôn lại.

- Service: `WrongQuestionProgressService, WrongQuestionService, WrongReviewSessionService, WrongReviewInteractionService, WrongReviewFinishService`.
- Dữ liệu chính: `wrong_question_progress, wrong_question_review_sessions, wrong_question_review_session_questions`.

## Luyện và thi thử

EXAM/PRACTICE, ngân hàng và ma trận hạng. Một phiên IN_PROGRESS/PAUSED mỗi người/hạng. Pause/resume/timeout có trạng thái DB, flags/cursor tách đáp án, retry tạo lượt mới.

- Service: `ExamService, ActiveExamService, ExamSessionFeedbackService, ExamCursorService, ExamQuestionFlagService, ExamRetryService`.
- Dữ liệu chính: `exams, exam_answers`.

## Bộ đề cố định

Chỉ dùng bộ PUBLISHED đúng bank/hạng, kiểm tra quota/câu điểm liệt. Cùng preset/mode đang dở có thể tiếp tục. Kết quả giữ lại nhiều lượt.

- Service: `PresetExamSetService, PresetExamSetValidator`.
- Dữ liệu chính: `preset_exam_sets, preset_exam_set_questions`.

## Tiến độ Exam và học

Receipt và sự kiện logic bền cùng transaction; worker gửi HTTP sang Progress bằng internal key. Retry không tạo thêm receipt cùng user/exam.

- Service: `ExamProgressReceiptService, ExamProgressOutboxWorker, LearningProgressOutboxWorker`.
- Dữ liệu chính: `exam_progress_receipts, exam_progress_outbox, learning_progress_outbox`.

## Lịch sử hợp nhất

RANDOM_EXAM/PRESET_EXAM mode EXAM được đóng băng dữ liệu nguồn để Result consume learning-results. PRACTICE vẫn có tiến độ nhưng không tạo exam history hợp nhất. Lỗi backfill ghi riêng và không làm hàng sau bị kẹt.

- Service: `PracticeResultBackfillWorker, PracticeResultOutboxWorker, PracticeResultKafkaConfig`.
- Dữ liệu chính: `practice_result_outbox, practice_result_backfill_failures; history fields on exams`.

## EXP tương tác mới

V30 giữ riêng evidence tương tác câu hỏi mới, gửi JSON tới learning-results. Result quyết định tính EXP theo câu đúng riêng biệt; câu gửi lại không đồng nghĩa tăng thêm EXP.

- Service: `PracticeExpOutboxWorker, PracticeExpSchedulingConfig`.
- Dữ liệu chính: `practice_exp_capture_state, practice_exp_outbox`.

## Biển báo và mẹo

Import ADMIN từ nguồn published Content Builder; API học viên chỉ đọc current bank, list/filter/detail biển báo và danh sách mẹo. Media giữ ID nguồn để frontend tải ảnh.

- Service: `TrafficSignReadService, MemoryTipReadService, TrafficSignBankImportService, MemoryTipBankImportService`.
- Dữ liệu chính: `traffic_signs, memory_tips`.

## Ranh giới repo và cấu trúc

Controller nhận principal/request và trả ApiResponse; DTO định nghĩa contract; service chứa transaction/nghiệp vụ; repository JPA hoặc JdbcTemplate thực thi DB; entity ánh xạ; config quản lý security/cache/scheduler/Kafka. SQL migration tạo constraint/trigger/function cần thiết, không chỉ bảng JPA. Giữ cấu trúc này khi thành viên bổ sung chức năng.

Practice không sở hữu bài thi chính thức, public-profile người dùng hay projection Screen14/16. Tài khoản lấy từ Identity; câu hỏi/nội dung từ Content Builder; tiến độ tổng hợp ở Progress; lịch sử hợp nhất/EXP/analytics ở Result. Đổi dữ liệu của projection không phải sửa nguồn bài thi.

Xem [LOCAL_SETUP.md](LOCAL_SETUP.md), [API blueprints](api-blueprint/README.md) và [migration catalog](db/MIGRATION_CATALOG.md). Các tài liệu/API-DOCS/OpenAPI snapshot cũ được giữ để tránh xóa nhầm lịch sử; file blueprint mới là mô tả các luồng bổ sung trong đợt này.
