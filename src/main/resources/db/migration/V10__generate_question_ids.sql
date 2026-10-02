CREATE SEQUENCE practice_question_bank_id_seq;

SELECT setval(
    'practice_question_bank_id_seq',
    GREATEST((SELECT COALESCE(MAX(id), 1) FROM questions), 1),
    true
);

ALTER TABLE questions
    ALTER COLUMN id
    SET DEFAULT nextval('practice_question_bank_id_seq');

ALTER SEQUENCE practice_question_bank_id_seq
    OWNED BY questions.id;