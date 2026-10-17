CREATE TABLE exam_progress_receipts (
    user_id               UUID NOT NULL,
    exam_id               UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    exam_type             VARCHAR(10) NOT NULL,
    bank_version          VARCHAR(64),
    mode                  VARCHAR(10) NOT NULL,
    status                VARCHAR(20) NOT NULL CHECK (status IN ('COMPLETED', 'TIMEOUT')),
    total_questions       INTEGER NOT NULL,
    correct_count         INTEGER NOT NULL,
    wrong_count           INTEGER NOT NULL,
    answered_wrong_count  INTEGER NOT NULL,
    unanswered_count      INTEGER NOT NULL,
    score                 INTEGER NOT NULL,
    passed                BOOLEAN NOT NULL,
    duration_seconds      INTEGER NOT NULL,
    started_at            TIMESTAMPTZ NOT NULL,
    completed_at          TIMESTAMPTZ NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_exam_progress_receipts PRIMARY KEY (user_id, exam_id)
);

CREATE INDEX idx_exam_progress_receipts_user_completed
    ON exam_progress_receipts (user_id, completed_at DESC);

CREATE TABLE exam_progress_outbox (
    event_id         UUID PRIMARY KEY,
    user_id          UUID NOT NULL,
    exam_id          UUID NOT NULL,
    event_type       VARCHAR(64) NOT NULL,
    schema_version   INTEGER NOT NULL,
    payload          JSONB NOT NULL,
    attempts         INTEGER NOT NULL DEFAULT 0,
    next_attempt_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at     TIMESTAMPTZ,
    last_error       TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_exam_progress_outbox_type_exam UNIQUE (event_type, exam_id)
);

CREATE INDEX idx_exam_progress_outbox_pending
    ON exam_progress_outbox (next_attempt_at, created_at)
    WHERE published_at IS NULL;