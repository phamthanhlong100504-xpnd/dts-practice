
CREATE TABLE wrong_question_review_sessions (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID        NOT NULL,
    license_class       VARCHAR(10) NOT NULL,
    bank_version        VARCHAR(64) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    current_question_id INTEGER     REFERENCES questions(id),
    total_questions     INTEGER     NOT NULL,
    answered_questions  INTEGER     NOT NULL DEFAULT 0,
    correct_count       INTEGER     NOT NULL DEFAULT 0,
    wrong_count         INTEGER     NOT NULL DEFAULT 0,
    unanswered_count    INTEGER     NOT NULL DEFAULT 0,
    remaining_count     INTEGER,
    started_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at        TIMESTAMPTZ,
    CONSTRAINT ck_wrong_review_session_status
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT ck_wrong_review_session_counts
        CHECK (total_questions > 0
           AND answered_questions >= 0
           AND answered_questions <= total_questions
           AND correct_count >= 0
           AND wrong_count >= 0
           AND unanswered_count >= 0
           AND (remaining_count IS NULL OR remaining_count >= 0)),
    CONSTRAINT ck_wrong_review_session_completed
        CHECK (status <> 'COMPLETED' OR
               (completed_at IS NOT NULL
                AND remaining_count IS NOT NULL
                AND answered_questions = correct_count + wrong_count
                AND total_questions = answered_questions + unanswered_count))
);

CREATE UNIQUE INDEX uq_wrong_review_one_active_per_user_license_bank
    ON wrong_question_review_sessions (user_id, license_class, bank_version)
    WHERE status = 'IN_PROGRESS';

CREATE INDEX idx_wrong_review_sessions_history
    ON wrong_question_review_sessions
        (user_id, license_class, bank_version, started_at DESC);

CREATE TABLE wrong_question_review_session_questions (
    session_id            UUID        NOT NULL
                          REFERENCES wrong_question_review_sessions(id)
                          ON DELETE CASCADE,
    question_id           INTEGER     NOT NULL REFERENCES questions(id),
    question_order        INTEGER     NOT NULL,
    previous_wrong_answer VARCHAR(10),
    last_wrong_at         TIMESTAMPTZ,
    CONSTRAINT pk_wrong_review_session_questions
        PRIMARY KEY (session_id, question_id),
    CONSTRAINT uq_wrong_review_session_question_order
        UNIQUE (session_id, question_order),
    CONSTRAINT ck_wrong_review_question_order
        CHECK (question_order > 0)
);

CREATE TABLE wrong_question_review_answers (
    session_id      UUID        NOT NULL,
    question_id     INTEGER     NOT NULL,
    selected_answer VARCHAR(10) NOT NULL,
    is_correct      BOOLEAN     NOT NULL,
    answered_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_wrong_review_answers
        PRIMARY KEY (session_id, question_id),
    CONSTRAINT fk_wrong_review_answers_snapshot
        FOREIGN KEY (session_id, question_id)
        REFERENCES wrong_question_review_session_questions (
            session_id, question_id
        )
        ON DELETE CASCADE,
    CONSTRAINT ck_wrong_review_selected_answer
        CHECK (length(trim(selected_answer)) > 0)
);