ALTER TABLE critical_question_sessions
    DROP CONSTRAINT ck_critical_session_status;

ALTER TABLE critical_question_sessions
    ADD CONSTRAINT ck_critical_session_status
    CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'SUPERSEDED'));