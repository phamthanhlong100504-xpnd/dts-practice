# Practice API blueprints

Nguồn đối chiếu: current local → `main` tại commit `237b9f5a27939e5dba946a656cae50e6d08f7d75`. Nhánh feature trước khi đóng gói: `19db6ccff8596d24f5d20676930818db47375a51`. Các file này mô tả code hiện có, không mở rộng API.

| Nhóm | Tài liệu |
| --- | --- |
| Ngân hàng câu hỏi và cấu hình hạng bằng | [question_catalog.md](question_catalog.md) |
| Phiên luyện tập, thi ngẫu nhiên và kết quả | [exam_sessions.md](exam_sessions.md) |
| Bộ đề cố định | [preset_exams.md](preset_exams.md) |
| Học theo chương, bookmark và câu đã học | [chapter_learning.md](chapter_learning.md) |
| Vòng ôn tập theo chương | [chapter_review.md](chapter_review.md) |
| Học và ôn câu điểm liệt | [critical_questions.md](critical_questions.md) |
| Danh sách câu sai và vòng ôn câu sai | [wrong_questions.md](wrong_questions.md) |
| Biển báo, mẹo ghi nhớ và import tham chiếu | [reference_content.md](reference_content.md) |
| Quy ước chung | [contract_notes.md](contract_notes.md) |
| DTO request/response | [dto_reference.md](dto_reference.md) |

Danh mục bao phủ **54 route/method** của **24 controller** mới/thay đổi hoặc thuộc thay đổi feature so với main. Các API cũ khác giữ tài liệu hiện có; không lấy practice-openapi-live.json làm chứng cứ runtime mới.

## Authorization và blueprint từng API

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

Xem [ma trận và rollout](authorization.md).
