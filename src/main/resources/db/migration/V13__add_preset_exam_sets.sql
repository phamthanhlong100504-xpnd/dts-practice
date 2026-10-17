-- Bộ đề cố định thuộc một hạng và một phiên bản ngân hàng câu hỏi.
-- Migration chỉ tạo cấu trúc; không tự sinh các bộ đề xem trước của frontend.

CREATE TABLE preset_exam_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    license_class VARCHAR(10) NOT NULL,
    bank_version VARCHAR(64) NOT NULL,
    total_questions INTEGER NOT NULL,
    duration_minutes INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    display_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_preset_exam_sets_code
        UNIQUE (license_class, bank_version, code),
    CONSTRAINT chk_preset_exam_sets_license
        CHECK (license_class IN (
            'A1', 'A', 'B1', 'B', 'C1', 'C', 'D1', 'D2', 'D',
            'BE', 'C1E', 'CE', 'D1E', 'D2E', 'DE'
        )),
    CONSTRAINT chk_preset_exam_sets_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT chk_preset_exam_sets_counts
        CHECK (
            total_questions BETWEEN 1 AND 60
            AND duration_minutes BETWEEN 1 AND 120
            AND display_order > 0
        )
);

CREATE INDEX idx_preset_exam_sets_listing
    ON preset_exam_sets (license_class, bank_version, display_order)
    WHERE status = 'PUBLISHED';

-- question_id là ID của bản ghi trong Practice.questions,
-- không phải source_question_id (số thứ tự 1–600 của tài liệu gốc).
CREATE TABLE preset_exam_set_questions (
    preset_exam_set_id UUID NOT NULL
        REFERENCES preset_exam_sets(id) ON DELETE RESTRICT,
    question_id INTEGER NOT NULL
        REFERENCES questions(id) ON DELETE RESTRICT,
    question_order INTEGER NOT NULL
        CHECK (question_order > 0),

    CONSTRAINT pk_preset_exam_set_questions
        PRIMARY KEY (preset_exam_set_id, question_order),
    CONSTRAINT uq_preset_exam_set_questions_question
        UNIQUE (preset_exam_set_id, question_id)
);

CREATE INDEX idx_preset_exam_set_questions_question
    ON preset_exam_set_questions (question_id);

-- NULL nghĩa là phiên thi thử ngẫu nhiên hoặc phiên cũ.
ALTER TABLE exams
    ADD COLUMN preset_exam_set_id UUID
        REFERENCES preset_exam_sets(id) ON DELETE RESTRICT;

CREATE INDEX idx_exams_user_preset_started
    ON exams (user_id, preset_exam_set_id, started_at DESC)
    WHERE preset_exam_set_id IS NOT NULL;