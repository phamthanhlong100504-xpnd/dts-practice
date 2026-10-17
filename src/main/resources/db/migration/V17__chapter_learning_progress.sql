-- Screen 07: answers are counted by unique question, never by a client-supplied counter.
-- The current questions table is the source for chapter totals and license membership.

CREATE TABLE chapter_question_progress (
    user_id          UUID        NOT NULL,
    license_class    VARCHAR(10) NOT NULL,
    bank_version     VARCHAR(64) NOT NULL,
    question_id      INTEGER     NOT NULL REFERENCES questions(id),
    chapter_id       INTEGER     NOT NULL,
    selected_answer  VARCHAR(10) NOT NULL,
    is_correct       BOOLEAN     NOT NULL,
    learned_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_answered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_chapter_question_progress
        PRIMARY KEY (user_id, license_class, bank_version, question_id),
    CONSTRAINT ck_chapter_question_progress_chapter CHECK (chapter_id > 0),
    CONSTRAINT ck_chapter_question_progress_answer
        CHECK (length(trim(selected_answer)) > 0)
);

CREATE INDEX idx_chapter_question_progress_overview
    ON chapter_question_progress
        (user_id, license_class, bank_version, chapter_id);

-- The cursor also covers a question the user is viewing but has not answered.
CREATE TABLE chapter_learning_cursor (
    user_id          UUID        NOT NULL,
    license_class    VARCHAR(10) NOT NULL,
    bank_version     VARCHAR(64) NOT NULL,
    chapter_id       INTEGER     NOT NULL,
    last_question_id INTEGER     NOT NULL REFERENCES questions(id),
    last_studied_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_chapter_learning_cursor
        PRIMARY KEY (user_id, license_class, bank_version, chapter_id),
    CONSTRAINT ck_chapter_learning_cursor_chapter CHECK (chapter_id > 0)
);