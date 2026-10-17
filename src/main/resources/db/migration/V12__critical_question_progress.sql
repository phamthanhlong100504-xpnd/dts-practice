CREATE TABLE critical_question_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    license_class VARCHAR(10) NOT NULL,
    bank_version VARCHAR(64) NOT NULL,
    question_id INTEGER NOT NULL REFERENCES questions(id),
    learned BOOLEAN NOT NULL DEFAULT FALSE,
    last_studied_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_critical_question_progress_scope
        UNIQUE (user_id, license_class, bank_version, question_id),

    CONSTRAINT chk_critical_question_progress_license
        CHECK (license_class IN (
            'A1', 'A', 'B1', 'B', 'C1', 'C', 'D1', 'D2', 'D',
            'BE', 'C1E', 'CE', 'D1E', 'D2E', 'DE'
        ))
);