-- Keep the remaining time and current question while an exam is paused.
ALTER TABLE exams
    ADD COLUMN paused_at TIMESTAMPTZ,
    ADD COLUMN remaining_time_ms BIGINT,
    ADD COLUMN current_question_id INTEGER;

-- The status constraint was created by V1.
ALTER TABLE exams DROP CONSTRAINT exams_status_check;

ALTER TABLE exams
    ADD CONSTRAINT exams_status_check
    CHECK (status IN ('IN_PROGRESS', 'PAUSED', 'COMPLETED', 'TIMEOUT'));

ALTER TABLE exams
    ADD CONSTRAINT chk_exams_pause_state
    CHECK (
        status <> 'PAUSED'
        OR (
            paused_at IS NOT NULL
            AND remaining_time_ms > 0
            AND expires_at IS NULL
        )
    );

ALTER TABLE exams
    ADD CONSTRAINT chk_exams_current_question
    CHECK (
        current_question_id IS NULL
        OR current_question_id = ANY(question_ids)
    );

-- A paused exam also occupies the user's one active slot for that license.
DROP INDEX IF EXISTS uq_exams_one_active_per_user_license;

CREATE UNIQUE INDEX uq_exams_one_active_per_user_license
    ON exams (user_id, exam_type)
    WHERE status IN ('IN_PROGRESS', 'PAUSED');