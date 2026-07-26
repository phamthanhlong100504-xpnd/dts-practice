-- V1: Practice Service Schema

CREATE TABLE IF NOT EXISTS questions (
    id              INTEGER PRIMARY KEY,
    chapter         INTEGER NOT NULL CHECK (chapter BETWEEN 1 AND 6),
    question_text   TEXT NOT NULL,
    options         JSONB NOT NULL,
    correct_answer  VARCHAR(1) NOT NULL,
    is_critical     BOOLEAN NOT NULL DEFAULT FALSE,
    explanation     TEXT,
    image_url       VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS exams (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    exam_type       VARCHAR(10) NOT NULL DEFAULT 'A1' CHECK (exam_type IN ('A1', 'A2', 'B1', 'B2', 'C', 'D', 'E', 'F')),
    question_ids    INTEGER[] NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'TIMEOUT')),
    score           INTEGER,
    total_questions INTEGER NOT NULL,
    correct_count   INTEGER DEFAULT 0,
    wrong_count     INTEGER DEFAULT 0,
    started_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS exam_answers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id         UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    question_id     INTEGER NOT NULL REFERENCES questions(id),
    selected_answer VARCHAR(1),
    is_correct      BOOLEAN,
    answered_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_exams_user_id ON exams(user_id);
CREATE INDEX IF NOT EXISTS idx_exams_status ON exams(status);
CREATE INDEX IF NOT EXISTS idx_exam_answers_exam_id ON exam_answers(exam_id);
CREATE INDEX IF NOT EXISTS idx_questions_chapter ON questions(chapter);
CREATE INDEX IF NOT EXISTS idx_questions_critical ON questions(is_critical) WHERE is_critical = TRUE;
