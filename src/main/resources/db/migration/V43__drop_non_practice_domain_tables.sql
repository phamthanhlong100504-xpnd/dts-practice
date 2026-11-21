-- ============================================================================
-- V43: Drop non-practice domain tables (Progress, Result, Reference Content)
-- Practice Service is now purely a runtime engine for Exams and Simulations.
-- ============================================================================

-- 1. Drop triggers on core tables that point to dropped routines/tables
DROP TRIGGER IF EXISTS simulation_performance_attempt ON simulation_attempts;
DROP TRIGGER IF EXISTS simulation_performance_exam ON simulation_exam_sessions;
DROP TRIGGER IF EXISTS trg_practice_mode_correct_exp ON exam_answers;
DROP TRIGGER IF EXISTS trg_practice_history_attempt ON exams;

-- 2. Drop guard triggers on tables to be dropped
DROP TRIGGER IF EXISTS simulation_performance_perfect_guard ON simulation_performance_first_perfect;
DROP TRIGGER IF EXISTS simulation_performance_catalog_guard ON simulation_performance_catalogs;
DROP TRIGGER IF EXISTS simulation_performance_bookmark ON simulation_bookmarks;
DROP TRIGGER IF EXISTS trg_practice_result_receipt ON exam_progress_receipts;

-- 3. Drop views associated with non-practice domains
DROP VIEW IF EXISTS memory_tips CASCADE;
DROP VIEW IF EXISTS traffic_signs CASCADE;
DROP VIEW IF EXISTS practice_exp_outbox CASCADE;
DROP VIEW IF EXISTS practice_result_outbox CASCADE;
DROP VIEW IF EXISTS learning_progress_outbox CASCADE;
DROP VIEW IF EXISTS simulation_performance_outbox CASCADE;
DROP VIEW IF EXISTS chapter_review_sessions CASCADE;
DROP VIEW IF EXISTS critical_question_sessions CASCADE;
DROP VIEW IF EXISTS wrong_question_review_sessions CASCADE;
DROP VIEW IF EXISTS chapter_review_session_questions CASCADE;
DROP VIEW IF EXISTS critical_question_session_questions CASCADE;
DROP VIEW IF EXISTS wrong_question_review_session_questions CASCADE;

-- 4. Drop the 18 non-practice tables
DROP TABLE IF EXISTS chapter_learning_cursor CASCADE;
DROP TABLE IF EXISTS chapter_question_bookmarks CASCADE;
DROP TABLE IF EXISTS chapter_question_progress CASCADE;
DROP TABLE IF EXISTS critical_question_progress CASCADE;
DROP TABLE IF EXISTS wrong_question_progress CASCADE;
DROP TABLE IF EXISTS chapter_review_session_answers CASCADE;
DROP TABLE IF EXISTS critical_question_session_answers CASCADE;
DROP TABLE IF EXISTS wrong_question_review_answers CASCADE;
DROP TABLE IF EXISTS practice_review_sessions CASCADE;
DROP TABLE IF EXISTS simulation_bookmarks CASCADE;
DROP TABLE IF EXISTS simulation_performance_catalogs CASCADE;
DROP TABLE IF EXISTS simulation_performance_first_perfect CASCADE;
DROP TABLE IF EXISTS simulation_performance_source CASCADE;
DROP TABLE IF EXISTS practice_exp_capture_state CASCADE;
DROP TABLE IF EXISTS exam_progress_outbox CASCADE;
DROP TABLE IF EXISTS exam_progress_receipts CASCADE;
DROP TABLE IF EXISTS practice_outbox CASCADE;
DROP TABLE IF EXISTS reference_items CASCADE;
