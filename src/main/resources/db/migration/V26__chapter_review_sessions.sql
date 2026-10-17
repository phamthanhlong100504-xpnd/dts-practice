
CREATE TABLE chapter_review_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    license_class VARCHAR(10) NOT NULL,
    bank_version VARCHAR(64) NOT NULL,
    chapter_id INTEGER NOT NULL CHECK (chapter_id BETWEEN 1 AND 6),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS'
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    current_question_id INTEGER NOT NULL REFERENCES questions(id),
    total_questions INTEGER NOT NULL CHECK (total_questions > 0),
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT ck_chapter_review_completed
        CHECK (status <> 'COMPLETED' OR completed_at IS NOT NULL)
);

CREATE UNIQUE INDEX uq_chapter_review_one_active
    ON chapter_review_sessions (
        user_id, license_class, bank_version, chapter_id
    )
    WHERE status = 'IN_PROGRESS';

CREATE INDEX idx_chapter_review_history
    ON chapter_review_sessions (
        user_id, license_class, bank_version, chapter_id, started_at DESC
    );

CREATE TABLE chapter_review_session_questions (
    session_id UUID NOT NULL
        REFERENCES chapter_review_sessions(id) ON DELETE CASCADE,
    question_id INTEGER NOT NULL REFERENCES questions(id),
    question_order INTEGER NOT NULL CHECK (question_order > 0),
    PRIMARY KEY (session_id, question_id),
    UNIQUE (session_id, question_order)
);

CREATE TABLE chapter_review_session_answers (
    session_id UUID NOT NULL,
    question_id INTEGER NOT NULL,
    selected_answer VARCHAR(10) NOT NULL
        CHECK (length(btrim(selected_answer)) > 0),
    is_correct BOOLEAN NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (session_id, question_id),
    FOREIGN KEY (session_id, question_id)
        REFERENCES chapter_review_session_questions (session_id, question_id)
        ON DELETE CASCADE
);