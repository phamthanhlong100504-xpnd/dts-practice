-- Giữ các lượt thi và câu hỏi cũ để xem lại lịch sử.
-- Lượt thi mới ghi phiên bản ngân hàng và đúng câu điểm liệt đã bốc.
ALTER TABLE exams
    ADD COLUMN bank_version VARCHAR(64) NOT NULL DEFAULT 'legacy-v1';

ALTER TABLE exams
    ADD COLUMN critical_question_id INTEGER REFERENCES questions(id);

-- Giữ hạng cũ cho lịch sử; API tạo đề mới chỉ chấp nhận 15 hạng hiện hành.
ALTER TABLE exams DROP CONSTRAINT IF EXISTS exams_exam_type_check;

ALTER TABLE exams ADD CONSTRAINT exams_exam_type_check CHECK (
    exam_type IN (
        'A1', 'A', 'B1', 'B', 'C1', 'C', 'D1', 'D2', 'D',
        'BE', 'C1E', 'CE', 'D1E', 'D2E', 'DE',
        'A2', 'B2', 'E', 'F'
    )
);