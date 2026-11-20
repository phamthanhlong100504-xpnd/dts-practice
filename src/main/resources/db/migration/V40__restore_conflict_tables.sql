-- V40__restore_conflict_tables.sql
-- Restores physical base tables for relations targeted by INSERT ... ON CONFLICT (...) DO UPDATE in dts-practice.
-- PostgreSQL does not support ON CONFLICT clauses on views.

-- 1. simulation_cursors
DROP TRIGGER IF EXISTS simulation_cursors_iot ON simulation_cursors;
DROP VIEW IF EXISTS simulation_cursors CASCADE;
CREATE TABLE IF NOT EXISTS simulation_cursors (
    user_id UUID NOT NULL,
    bank_version VARCHAR(96) NOT NULL,
    chapter_id INTEGER NOT NULL CHECK (chapter_id BETWEEN 1 AND 6),
    scenario_id UUID NOT NULL,
    source_attempt_id UUID,
    revision BIGINT NOT NULL CHECK (revision > 0),
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, bank_version, chapter_id)
);

INSERT INTO simulation_cursors (user_id, bank_version, chapter_id, scenario_id, source_attempt_id, revision, updated_at)
SELECT user_id, bank_version, chapter_id, scenario_id, source_attempt_id, revision, updated_at
FROM learning_cursors
WHERE cursor_domain = 'SIMULATION'
ON CONFLICT (user_id, bank_version, chapter_id) DO UPDATE SET
    scenario_id = EXCLUDED.scenario_id,
    source_attempt_id = EXCLUDED.source_attempt_id,
    revision = EXCLUDED.revision,
    updated_at = EXCLUDED.updated_at;

-- 2. chapter_learning_cursor
DROP TRIGGER IF EXISTS chapter_learning_cursor_iot ON chapter_learning_cursor;
DROP VIEW IF EXISTS chapter_learning_cursor CASCADE;
CREATE TABLE IF NOT EXISTS chapter_learning_cursor (
    user_id          UUID        NOT NULL,
    license_class    VARCHAR(10) NOT NULL,
    bank_version     VARCHAR(64) NOT NULL,
    chapter_id       INTEGER     NOT NULL CHECK (chapter_id > 0),
    last_question_id INTEGER     NOT NULL REFERENCES questions(id),
    last_studied_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, license_class, bank_version, chapter_id)
);

INSERT INTO chapter_learning_cursor (user_id, license_class, bank_version, chapter_id, last_question_id, last_studied_at)
SELECT user_id, license_class, bank_version, chapter_id, last_question_id, last_studied_at
FROM learning_cursors
WHERE cursor_domain = 'THEORY'
ON CONFLICT (user_id, license_class, bank_version, chapter_id) DO UPDATE SET
    last_question_id = EXCLUDED.last_question_id,
    last_studied_at = EXCLUDED.last_studied_at;

-- 3. chapter_question_progress
DROP TRIGGER IF EXISTS chapter_question_progress_iot ON chapter_question_progress;
DROP VIEW IF EXISTS chapter_question_progress CASCADE;
CREATE TABLE IF NOT EXISTS chapter_question_progress (
    user_id          UUID        NOT NULL,
    license_class    VARCHAR(10) NOT NULL,
    bank_version     VARCHAR(64) NOT NULL,
    question_id      INTEGER     NOT NULL REFERENCES questions(id),
    chapter_id       INTEGER     NOT NULL CHECK (chapter_id > 0),
    selected_answer  VARCHAR(10) NOT NULL,
    is_correct       BOOLEAN     NOT NULL,
    learned_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_answered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, license_class, bank_version, question_id)
);

INSERT INTO chapter_question_progress (
    user_id, license_class, bank_version, question_id, chapter_id,
    selected_answer, is_correct, learned_at, last_answered_at
)
SELECT 
    user_id, license_class, bank_version, question_id, chapter_id,
    COALESCE(selected_answer, 'A'), COALESCE(is_correct, false),
    COALESCE(learned_at, now()), COALESCE(last_answered_at, now())
FROM user_question_progress
WHERE chapter_id IS NOT NULL AND selected_answer IS NOT NULL
ON CONFLICT (user_id, license_class, bank_version, question_id) DO UPDATE SET
    selected_answer = EXCLUDED.selected_answer,
    is_correct = EXCLUDED.is_correct,
    last_answered_at = EXCLUDED.last_answered_at;

-- 4. critical_question_progress
DROP TRIGGER IF EXISTS critical_question_progress_iot ON critical_question_progress;
DROP VIEW IF EXISTS critical_question_progress CASCADE;
CREATE TABLE IF NOT EXISTS critical_question_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    license_class VARCHAR(10) NOT NULL,
    bank_version VARCHAR(64) NOT NULL,
    question_id INTEGER NOT NULL REFERENCES questions(id),
    learned BOOLEAN NOT NULL DEFAULT FALSE,
    last_studied_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    selected_answer VARCHAR(10) NULL,
    is_correct BOOLEAN NULL,
    learned_at TIMESTAMPTZ NULL,
    last_answered_at TIMESTAMPTZ NULL,
    UNIQUE (user_id, license_class, bank_version, question_id)
);

INSERT INTO critical_question_progress (
    id, user_id, license_class, bank_version, question_id,
    learned, last_studied_at, created_at, updated_at,
    selected_answer, is_correct, learned_at, last_answered_at
)
SELECT 
    id, user_id, license_class, bank_version, question_id,
    learned, last_studied_at, created_at, updated_at,
    selected_answer, is_correct, learned_at, last_answered_at
FROM user_question_progress
ON CONFLICT (user_id, license_class, bank_version, question_id) DO UPDATE SET
    learned = EXCLUDED.learned,
    last_studied_at = EXCLUDED.last_studied_at,
    selected_answer = EXCLUDED.selected_answer,
    is_correct = EXCLUDED.is_correct,
    learned_at = EXCLUDED.learned_at,
    last_answered_at = EXCLUDED.last_answered_at,
    updated_at = EXCLUDED.updated_at;

-- 5. wrong_question_progress
DROP TRIGGER IF EXISTS wrong_question_progress_iot ON wrong_question_progress;
DROP VIEW IF EXISTS wrong_question_progress CASCADE;
CREATE TABLE IF NOT EXISTS wrong_question_progress (
    user_id              UUID        NOT NULL,
    license_class        VARCHAR(10) NOT NULL,
    bank_version         VARCHAR(64) NOT NULL,
    question_id          INTEGER     NOT NULL REFERENCES questions(id),
    needs_review         BOOLEAN     NOT NULL,
    last_correct         BOOLEAN     NOT NULL,
    last_selected_answer VARCHAR(10) NOT NULL,
    last_wrong_answer    VARCHAR(10),
    last_wrong_at        TIMESTAMPTZ,
    last_answered_at     TIMESTAMPTZ NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, license_class, bank_version, question_id)
);

INSERT INTO wrong_question_progress (
    user_id, license_class, bank_version, question_id,
    needs_review, last_correct, last_selected_answer, last_wrong_answer,
    last_wrong_at, last_answered_at, created_at, updated_at
)
SELECT 
    user_id, license_class, bank_version, question_id,
    needs_review, COALESCE(last_correct, false), COALESCE(last_selected_answer, 'A'), last_wrong_answer,
    last_wrong_at, COALESCE(last_answered_at, now()), created_at, updated_at
FROM user_question_progress
WHERE needs_review IS TRUE OR last_wrong_answer IS NOT NULL
ON CONFLICT (user_id, license_class, bank_version, question_id) DO UPDATE SET
    needs_review = EXCLUDED.needs_review,
    last_correct = EXCLUDED.last_correct,
    last_selected_answer = EXCLUDED.last_selected_answer,
    last_wrong_answer = EXCLUDED.last_wrong_answer,
    last_wrong_at = EXCLUDED.last_wrong_at,
    last_answered_at = EXCLUDED.last_answered_at,
    updated_at = EXCLUDED.updated_at;

-- 6. chapter_question_bookmarks
DROP TRIGGER IF EXISTS chapter_question_bookmarks_iot ON chapter_question_bookmarks;
DROP VIEW IF EXISTS chapter_question_bookmarks CASCADE;
CREATE TABLE IF NOT EXISTS chapter_question_bookmarks (
    user_id       UUID        NOT NULL,
    license_class VARCHAR(10) NOT NULL,
    bank_version  VARCHAR(64) NOT NULL,
    question_id   INTEGER     NOT NULL REFERENCES questions(id),
    chapter_id    INTEGER     NOT NULL CHECK (chapter_id > 0),
    bookmarked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, license_class, bank_version, question_id)
);

INSERT INTO chapter_question_bookmarks (
    user_id, license_class, bank_version, question_id, chapter_id, bookmarked_at
)
SELECT 
    user_id, license_class, bank_version, question_id, COALESCE(chapter_id, 1), COALESCE(bookmarked_at, now())
FROM user_question_progress
WHERE is_bookmarked IS TRUE
ON CONFLICT (user_id, license_class, bank_version, question_id) DO UPDATE SET
    chapter_id = EXCLUDED.chapter_id,
    bookmarked_at = EXCLUDED.bookmarked_at;

-- 7. Review answers tables
DROP TRIGGER IF EXISTS chapter_review_session_answers_iot ON chapter_review_session_answers;
DROP VIEW IF EXISTS chapter_review_session_answers CASCADE;
CREATE TABLE IF NOT EXISTS chapter_review_session_answers (
    session_id UUID NOT NULL,
    question_id INTEGER NOT NULL REFERENCES questions(id),
    selected_answer VARCHAR(10) NOT NULL CHECK (length(btrim(selected_answer)) > 0),
    is_correct BOOLEAN NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (session_id, question_id)
);

INSERT INTO chapter_review_session_answers (session_id, question_id, selected_answer, is_correct, answered_at)
SELECT a.session_id, a.question_id, a.selected_answer, a.is_correct, a.answered_at
FROM practice_review_answers a
JOIN practice_review_sessions s ON s.id = a.session_id
WHERE s.review_type = 'CHAPTER'
ON CONFLICT (session_id, question_id) DO UPDATE SET
    selected_answer = EXCLUDED.selected_answer,
    is_correct = EXCLUDED.is_correct,
    answered_at = EXCLUDED.answered_at;

DROP TRIGGER IF EXISTS critical_question_session_answers_iot ON critical_question_session_answers;
DROP VIEW IF EXISTS critical_question_session_answers CASCADE;
CREATE TABLE IF NOT EXISTS critical_question_session_answers (
    session_id UUID NOT NULL,
    question_id INTEGER NOT NULL REFERENCES questions(id),
    selected_answer VARCHAR(10) NOT NULL CHECK (length(btrim(selected_answer)) > 0),
    is_correct BOOLEAN NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (session_id, question_id)
);

INSERT INTO critical_question_session_answers (session_id, question_id, selected_answer, is_correct, answered_at)
SELECT a.session_id, a.question_id, a.selected_answer, a.is_correct, a.answered_at
FROM practice_review_answers a
JOIN practice_review_sessions s ON s.id = a.session_id
WHERE s.review_type = 'CRITICAL'
ON CONFLICT (session_id, question_id) DO UPDATE SET
    selected_answer = EXCLUDED.selected_answer,
    is_correct = EXCLUDED.is_correct,
    answered_at = EXCLUDED.answered_at;

DROP TRIGGER IF EXISTS wrong_question_review_answers_iot ON wrong_question_review_answers;
DROP VIEW IF EXISTS wrong_question_review_answers CASCADE;
CREATE TABLE IF NOT EXISTS wrong_question_review_answers (
    session_id UUID NOT NULL,
    question_id INTEGER NOT NULL REFERENCES questions(id),
    selected_answer VARCHAR(10) NOT NULL CHECK (length(btrim(selected_answer)) > 0),
    is_correct BOOLEAN NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (session_id, question_id)
);

INSERT INTO wrong_question_review_answers (session_id, question_id, selected_answer, is_correct, answered_at)
SELECT a.session_id, a.question_id, a.selected_answer, a.is_correct, a.answered_at
FROM practice_review_answers a
JOIN practice_review_sessions s ON s.id = a.session_id
WHERE s.review_type = 'WRONG'
ON CONFLICT (session_id, question_id) DO UPDATE SET
    selected_answer = EXCLUDED.selected_answer,
    is_correct = EXCLUDED.is_correct,
    answered_at = EXCLUDED.answered_at;

-- 8. exam_progress_outbox
DROP TRIGGER IF EXISTS exam_progress_outbox_iot ON exam_progress_outbox;
DROP VIEW IF EXISTS exam_progress_outbox CASCADE;
CREATE TABLE IF NOT EXISTS exam_progress_outbox (
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

INSERT INTO exam_progress_outbox (
    event_id, user_id, exam_id, event_type, schema_version, payload,
    attempts, next_attempt_at, published_at, last_error, created_at
)
SELECT 
    event_id, user_id, exam_id, event_type, schema_version, payload,
    attempts, next_attempt_at, published_at, last_error, created_at
FROM practice_outbox
WHERE topic = 'exam-progress'
ON CONFLICT (event_id) DO NOTHING;
