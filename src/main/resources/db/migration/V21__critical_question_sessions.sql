-- Critical learning is cumulative; a review round has independent answers.
-- Keep every V12 progress row, including learned rows with no stored answer.
ALTER TABLE critical_question_progress
    ADD COLUMN selected_answer VARCHAR(10),
    ADD COLUMN is_correct BOOLEAN,
    ADD COLUMN learned_at TIMESTAMPTZ,
    ADD COLUMN last_answered_at TIMESTAMPTZ;

UPDATE critical_question_progress
SET learned_at = last_studied_at
WHERE learned = TRUE AND learned_at IS NULL;

ALTER TABLE critical_question_progress
    ADD CONSTRAINT ck_critical_progress_selected_answer
    CHECK (
        selected_answer IS NULL
        OR length(btrim(selected_answer)) > 0
    );

CREATE TABLE critical_question_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    license_class VARCHAR(10) NOT NULL,
    bank_version VARCHAR(64) NOT NULL,
    mode VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    current_question_id INTEGER REFERENCES questions(id),
    total_questions INTEGER NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_critical_session_mode
        CHECK (mode IN ('CONTINUE', 'REVIEW')),

    CONSTRAINT ck_critical_session_status
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),

    CONSTRAINT ck_critical_session_total
        CHECK (total_questions > 0),

    CONSTRAINT ck_critical_session_completed
        CHECK (
            status <> 'COMPLETED'
            OR completed_at IS NOT NULL
        )
);

-- Một user + hạng + bank chỉ có một vòng đang thực hiện.
CREATE UNIQUE INDEX uq_critical_session_one_active_user_license_bank
    ON critical_question_sessions (
        user_id,
        license_class,
        bank_version
    )
    WHERE status = 'IN_PROGRESS';

CREATE INDEX idx_critical_session_history
    ON critical_question_sessions (
        user_id,
        license_class,
        bank_version,
        started_at DESC
    );

CREATE TABLE critical_question_session_questions (
    session_id UUID NOT NULL
        REFERENCES critical_question_sessions(id)
        ON DELETE CASCADE,

    question_id INTEGER NOT NULL
        REFERENCES questions(id),

    question_order INTEGER NOT NULL,

    -- CONTINUE ghi nhận các câu đã học trước khi tạo session.
    -- REVIEW luôn bắt đầu với initial_learned = FALSE.
    initial_learned BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_critical_session_questions
        PRIMARY KEY (session_id, question_id),

    CONSTRAINT uq_critical_session_question_order
        UNIQUE (session_id, question_order),

    CONSTRAINT ck_critical_session_question_order
        CHECK (question_order > 0)
);

CREATE TABLE critical_question_session_answers (
    session_id UUID NOT NULL,
    question_id INTEGER NOT NULL,
    selected_answer VARCHAR(10) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_critical_session_answers
        PRIMARY KEY (session_id, question_id),

    CONSTRAINT fk_critical_session_answers_question
        FOREIGN KEY (session_id, question_id)
        REFERENCES critical_question_session_questions (
            session_id,
            question_id
        )
        ON DELETE CASCADE,

    CONSTRAINT ck_critical_session_selected_answer
        CHECK (length(btrim(selected_answer)) > 0)
);