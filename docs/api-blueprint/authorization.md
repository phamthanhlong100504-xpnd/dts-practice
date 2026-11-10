# Phân quyền API Practice

## Part 0 — Classification & Identity

Rà soát đủ 55 mapping của 25 controller tại commit 6373cc8. Có 51 API học viên, 3 API import quản trị và 1 home public. API ảnh câu hỏi, health, docs và OPTIONS giữ chính sách SecurityConfig hiện có. Task chỉ sửa Practice và tài liệu Markdown trong repo.

## Part 1 — API Contract

Card 01 bổ sung hai GET độc lập với quyền lý thuyết: `/api/v1/simulations/catalog` cần `simulations:read`; `/api/v1/simulations/scenarios/{scenarioId}/thumbnail` cần cả `simulations:read` và `media:read`. Chủ thể theo JWT, ticket thumbnail gắn đúng ngân hàng/tình huống. Chi tiết: [simulation_catalog.md](simulation_catalog.md). Quyền mới đã được Identity cấp qua V12; không cần mở rộng quyền import để học viên xem thumbnail.

Các API lượt luyện Card 01 kiểm tra thêm `simulations:learn` cho create/start/heartbeat/response/complete/interrupt/cancel/bookmark/cursor; playback cần cả `media:read`. Đọc attempt/result chỉ tra theo đúng user; role admin không bypass ownership. Mọi API đọc/ghi và lỗi chính thức nằm trong [simulation_learning.md](simulation_learning.md).

| Method | Path | Authorization | Blueprint |
| --- | --- | --- | --- |
| GET | /api/v1/learning/chapters/{chapterId}/bookmarks | practice:read | [ChapterBookmarkController.list](chapter_bookmark_list.md) |
| PUT | /api/v1/learning/chapters/{chapterId}/bookmarks/{questionId} | practice:learn | [ChapterBookmarkController.set](chapter_bookmark_set.md) |
| GET | /api/v1/learning/chapters/{chapterId}/answers | practice:read | [ChapterLearnedAnswerQueryController.getAnswers](chapter_learned_answer_query_get_answers.md) |
| POST | /api/v1/learning/chapters/{chapterId}/answers | practice:learn | [ChapterLearningController.answer](chapter_learning_answer.md) |
| GET | /api/v1/learning/chapters | practice:read | [ChapterLearningController.overview](chapter_learning_overview.md) |
| PUT | /api/v1/learning/chapters/{chapterId}/cursor | practice:learn | [ChapterLearningController.saveCursor](chapter_learning_save_cursor.md) |
| POST | /api/v1/learning/chapters/{chapterId}/review-sessions/{sessionId}/answers | practice:learn | [ChapterReviewSessionController.answer](chapter_review_session_answer.md) |
| PUT | /api/v1/learning/chapters/{chapterId}/review-sessions/{sessionId}/cursor | practice:learn | [ChapterReviewSessionController.cursor](chapter_review_session_cursor.md) |
| POST | /api/v1/learning/chapters/{chapterId}/review-sessions/{sessionId}/finish | practice:learn | [ChapterReviewSessionController.finish](chapter_review_session_finish.md) |
| GET | /api/v1/learning/chapters/{chapterId}/review-sessions/{sessionId} | practice:read | [ChapterReviewSessionController.get](chapter_review_session_get.md) |
| POST | /api/v1/learning/chapters/{chapterId}/review-sessions | practice:learn | [ChapterReviewSessionController.start](chapter_review_session_start.md) |
| POST | /api/v1/questions/critical/answers | practice:learn | [CriticalAnswerController.submitAnswer](critical_answer_submit_answer.md) |
| PUT | /api/v1/questions/critical/sessions/{sessionId}/cursor | practice:learn | [CriticalCursorController.saveCursor](critical_cursor_save_cursor.md) |
| GET | /api/v1/questions/critical/overview | practice:read | [CriticalQuestionProgressController.getOverview](critical_question_progress_get_overview.md) |
| PUT | /api/v1/questions/critical/{questionId}/progress | practice:learn | [CriticalQuestionProgressController.updateProgress](critical_question_progress_update_progress.md) |
| GET | /api/v1/questions/critical/sessions/{sessionId} | practice:read | [CriticalSessionController.get](critical_session_get.md) |
| POST | /api/v1/questions/critical/sessions | practice:learn | [CriticalSessionController.start](critical_session_start.md) |
| GET | /api/v1/exams/config | practice:read | [ExamConfigController.getConfig](exam_config_get_config.md) |
| POST | /api/v1/exams/{examId}/finish | practice:exam | [ExamController.finishExam](exam_finish_exam.md) |
| GET | /api/v1/exams/active | practice:read | [ExamController.getActiveExam](exam_get_active_exam.md) |
| GET | /api/v1/exams/history | practice:read | [ExamController.getHistory](exam_get_history.md) |
| GET | /api/v1/exams/leaderboard | practice:read | [ExamController.getLeaderboard](exam_get_leaderboard.md) |
| GET | /api/v1/exams/{examId}/result | practice:read | [ExamController.getResult](exam_get_result.md) |
| GET | /api/v1/exams/{examId} | practice:read | [ExamController.getSession](exam_get_session.md) |
| POST | /api/v1/exams/{examId}/pause | practice:exam | [ExamController.pauseExam](exam_pause_exam.md) |
| POST | /api/v1/exams/{examId}/resume | practice:exam | [ExamController.resumeExam](exam_resume_exam.md) |
| POST | /api/v1/exams | practice:exam | [ExamController.startExam](exam_start_exam.md) |
| POST | /api/v1/exams/{examId}/answers | practice:exam | [ExamController.submitAnswer](exam_submit_answer.md) |
| PUT | /api/v1/exams/{examId}/cursor | practice:exam | [ExamCursorController.updateCursor](exam_cursor_update_cursor.md) |
| GET | /api/v1/exams/{examId}/progress | practice:read | [ExamProgressReceiptController.getProgress](exam_progress_receipt_get_progress.md) |
| GET | /api/v1/exams/{examId}/flags | practice:read | [ExamQuestionFlagController.getFlags](exam_question_flag_get_flags.md) |
| PUT | /api/v1/exams/{examId}/questions/{questionId}/flag | practice:exam | [ExamQuestionFlagController.updateFlag](exam_question_flag_update_flag.md) |
| POST | /api/v1/exams/{examId}/retry | practice:exam | [ExamRetryController.retryExam](exam_retry_retry_exam.md) |
| GET | / | Public | [HomeController.home](home_home.md) |
| GET | /api/v1/questions/learning-catalog | practice:read | [LearningBankCatalogController.get](learning_bank_catalog_get.md) |
| GET | /api/v1/exams/license-catalog | practice:read | [LicenseCatalogController.getLicenseCatalog](license_catalog_get_license_catalog.md) |
| GET | /api/v1/memory-tips | practice:read | [MemoryTipController.list](memory_tip_list.md) |
| GET | /api/v1/exams/presets | practice:read | [PresetExamSetController.list](preset_exam_set_list.md) |
| POST | /api/v1/exams/presets/{presetSetId}/sessions | practice:exam | [PresetExamSetController.startSession](preset_exam_set_start_session.md) |
| POST | /api/v1/questions/admin/import-bank | ROLE_ADMIN và practice:import | [QuestionBankImportController.importBank](question_bank_import_import_bank.md) |
| GET | /api/v1/questions/chapter/{chapter} | practice:read | [QuestionController.getByChapter](question_get_by_chapter.md) |
| GET | /api/v1/questions/{id} | practice:read | [QuestionController.getById](question_get_by_id.md) |
| GET | /api/v1/questions/critical | practice:read | [QuestionController.getCritical](question_get_critical.md) |
| GET | /api/v1/questions/stats | practice:read | [QuestionController.getStats](question_get_stats.md) |
| POST | /api/v1/reference-content/admin/import-memory-tips | ROLE_ADMIN và practice:import | [ReferenceContentBankImportController.importMemoryTips](reference_content_bank_import_import_memory_tips.md) |
| POST | /api/v1/reference-content/admin/import-traffic-signs | ROLE_ADMIN và practice:import | [ReferenceContentBankImportController.importTrafficSigns](reference_content_bank_import_import_traffic_signs.md) |
| GET | /api/v1/traffic-signs/{id} | practice:read | [TrafficSignController.get](traffic_sign_get.md) |
| GET | /api/v1/traffic-signs | practice:read | [TrafficSignController.list](traffic_sign_list.md) |
| GET | /api/v1/questions/wrong/overview | practice:read | [WrongQuestionController.getOverview](wrong_question_get_overview.md) |
| GET | /api/v1/questions/wrong | practice:read | [WrongQuestionController.getWrongQuestions](wrong_question_get_wrong_questions.md) |
| POST | /api/v1/wrong-questions/sessions/{sessionId}/finish | practice:learn | [WrongReviewSessionController.finish](wrong_review_session_finish.md) |
| GET | /api/v1/wrong-questions/sessions/{sessionId} | practice:read | [WrongReviewSessionController.get](wrong_review_session_get.md) |
| PUT | /api/v1/wrong-questions/sessions/{sessionId}/cursor | practice:learn | [WrongReviewSessionController.saveCursor](wrong_review_session_save_cursor.md) |
| POST | /api/v1/wrong-questions/sessions | practice:learn | [WrongReviewSessionController.start](wrong_review_session_start.md) |
| POST | /api/v1/wrong-questions/sessions/{sessionId}/answers | practice:learn | [WrongReviewSessionController.submitAnswer](wrong_review_session_submit_answer.md) |

## Part 2 — Processing Specification

- practice:read cho 27 API GET nghiệp vụ. Catalog/reference là dữ liệu dùng chung; lịch sử/phiên/tiến độ là dữ liệu cá nhân theo JWT sub.
- practice:learn cho 15 thao tác học/ôn: đáp án, bookmark/cursor, học/ôn chương, câu điểm liệt và vòng ôn câu sai.
- practice:exam cho 9 thao tác trên phiên thi thử của chính tài khoản: bắt đầu, pause/resume, trả lời, finish, cursor, flag, retry và phiên bộ đề. Cả mode EXAM và PRACTICE dùng nhóm phiên này. Không dùng exams:create của quản trị bài thi để yêu cầu học viên tạo lượt làm bài.
- practice:import cùng ROLE_ADMIN cho cả 3 API import. Có permission import nhưng thiếu role ADMIN vẫn bị chặn; role ADMIN một mình cũng không bypass permission.
- Các quyền độc lập; không có wildcard hoặc suy quyền khác từ role. Giữ ánh xạ PERM_<permission> đã có trong JwtValidationFilter, không sửa pipeline xác thực.
- Giữ kiểm ownership và lock tại service/repository. Không đặt guard trên toàn service vì scheduler/outbox gọi service trực tiếp không mang JWT học viên.

## Part 3 — Data Interaction

Không thay database Practice hoặc migration đã chạy. Không refactor bảng questions sang Content Builder trong đợt phân quyền; việc đó thuộc task khác. Không đổi grade, số câu, thời gian, pass/critical, snapshot, bank/hạng, receipt, dedup hoặc outbox.

Một số GET xử lý timeout/outbox theo source hiện có. Permission đọc không mở quyền chọn user khác; quyền học/thi cũng không cho ghi vào phiên của người khác. Kiểm tra quyền mới phải xảy ra trước khi gọi service nghiệp vụ.

## Part 4 — Operational Notes

JWT hiện tại chưa có permission mới sẽ nhận 403 sau khi chạy source mới. Cần bàn giao người phụ trách Identity tạo migration cấp các quyền đã thống nhất và lấy token mới trước rollout. Task này không thay repo Identity. Các API gọi chuyển tiếp Bearer (Progress learning overview và receipt) cũng cần practice:read.

Task002 xác thực Gateway và Task004 refactor nguồn questions chưa nằm trong source baseline này. Khi nhóm tích hợp các thay đổi đó phải đối chiếu principal/authority, nguồn API và test lại; không tự bỏ JWT filter hoặc đổi nguồn câu hỏi trong task phân quyền.

Test gồm JWT thực qua MockMvc trên mọi mapping, ma trận thiếu/đủ/sai nhóm quyền, role import, giả mạo header, ownership service và hồi quy source hiện có. Kiểm report XML thật; không chỉ dựa Maven exit vì pom baseline có testFailureIgnore=true. API/runtime/UI chỉ nghiệm thu sau khi chạy trên môi trường tích hợp thật.


## Card02 và replay chỉ đọc

GET /api/v1/simulations/review-needed/overview cần simulations:read. GET /review-needed và GET /attempts/{attemptId}/replay dưới cùng prefix cần simulations:read + media:read, không yêu cầu simulations:learn. Replay chỉ nhận owned COMPLETED và không ghi trạng thái học. Không có permission Identity mới. [Contract Card02](simulation_review_needed.md).


### Card 03 — Bookmark mô phỏng

GET `/simulations/bookmarks/overview` cần `simulations:read`; GET `/simulations/bookmarks` cần thêm `media:read`. PUT `/simulations/bookmarks/{scenarioId}` giữ `simulations:read` + `simulations:learn` và body `{licenseClass,bankVersion,bookmarked}`. User từ JWT, không thêm permission. Xem [Card 03](simulation_bookmarks.md).
