-- Giữ các câu hỏi cũ để lịch sử thi tiếp tục tham chiếu đúng nội dung.
-- Các phiên bản mới sẽ được nhập thành bản ghi riêng.

ALTER TABLE questions ADD COLUMN bank_version VARCHAR(64);
ALTER TABLE questions ADD COLUMN source_question_id INTEGER;
ALTER TABLE questions ADD COLUMN applicable_licenses JSONB;
ALTER TABLE questions ADD COLUMN critical_licenses JSONB;
ALTER TABLE questions ADD COLUMN content_builder_question_id UUID;
ALTER TABLE questions ADD COLUMN media_file_id UUID;

UPDATE questions
SET bank_version = 'legacy-v1',
    source_question_id = id;

ALTER TABLE questions ALTER COLUMN bank_version SET NOT NULL;
ALTER TABLE questions ALTER COLUMN source_question_id SET NOT NULL;

ALTER TABLE questions
    ADD CONSTRAINT chk_questions_applicable_licenses_array
    CHECK (
        applicable_licenses IS NULL
        OR jsonb_typeof(applicable_licenses) = 'array'
    );

ALTER TABLE questions
    ADD CONSTRAINT chk_questions_critical_licenses_array
    CHECK (
        critical_licenses IS NULL
        OR jsonb_typeof(critical_licenses) = 'array'
    );

CREATE UNIQUE INDEX uq_questions_bank_source
    ON questions (bank_version, source_question_id);

CREATE UNIQUE INDEX uq_questions_content_builder_id
    ON questions (content_builder_question_id)
    WHERE content_builder_question_id IS NOT NULL;

CREATE INDEX idx_questions_bank_chapter
    ON questions (bank_version, chapter);

CREATE INDEX idx_questions_applicable_licenses
    ON questions USING GIN (applicable_licenses);

CREATE INDEX idx_questions_critical_licenses
    ON questions USING GIN (critical_licenses);