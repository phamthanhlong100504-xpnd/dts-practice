-- A question can be bookmarked before it is answered, so this table is
-- independent of chapter_question_progress.
CREATE TABLE chapter_question_bookmarks (
    user_id       UUID        NOT NULL,
    license_class VARCHAR(10) NOT NULL,
    bank_version  VARCHAR(64) NOT NULL,
    question_id   INTEGER     NOT NULL REFERENCES questions(id),
    chapter_id    INTEGER     NOT NULL,
    bookmarked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_chapter_question_bookmarks
        PRIMARY KEY (user_id, license_class, bank_version, question_id),
    CONSTRAINT ck_chapter_question_bookmarks_chapter CHECK (chapter_id > 0)
);

CREATE INDEX idx_chapter_question_bookmarks_by_chapter
    ON chapter_question_bookmarks
        (user_id, license_class, bank_version, chapter_id);