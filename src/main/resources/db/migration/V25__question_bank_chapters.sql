-- Chapter labels belong to a bank version; question membership is read from questions.
CREATE TABLE question_bank_chapters (
    bank_version VARCHAR(64) NOT NULL,
    chapter_id INTEGER NOT NULL CHECK (chapter_id > 0),
    chapter_name VARCHAR(255) NOT NULL CHECK (btrim(chapter_name) <> ''),
    PRIMARY KEY (bank_version, chapter_id)
);

-- Titles previously used by WrongReviewSessionService for this bank.
INSERT INTO question_bank_chapters (bank_version, chapter_id, chapter_name) VALUES
    ('dts-2026-600-v1', 1, 'Quy định chung và quy tắc giao thông đường bộ'),
    ('dts-2026-600-v1', 2, 'Văn hóa giao thông, đạo đức người lái xe, kỹ năng PCCC và cứu hộ, cứu nạn'),
    ('dts-2026-600-v1', 3, 'Kỹ thuật lái xe'),
    ('dts-2026-600-v1', 4, 'Cấu tạo và sửa chữa'),
    ('dts-2026-600-v1', 5, 'Báo hiệu đường bộ'),
    ('dts-2026-600-v1', 6, 'Giải thế sa hình và kỹ năng xử lý tình huống giao thông');