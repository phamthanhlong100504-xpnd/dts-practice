-- ============================================================================
-- V42: Drop redundant consolidated tables with no active code references
-- ============================================================================

DROP TABLE IF EXISTS practice_review_answers CASCADE;
DROP TABLE IF EXISTS practice_review_questions CASCADE;
DROP TABLE IF EXISTS practice_history_attempt_counters CASCADE;
DROP TABLE IF EXISTS learning_cursors CASCADE;
DROP TABLE IF EXISTS user_question_progress CASCADE;
