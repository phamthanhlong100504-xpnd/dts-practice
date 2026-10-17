-- A committed change to canonical Practice progress always has a durable event.
-- Keep this independent of the exam-finish receipt/outbox.
CREATE SEQUENCE learning_progress_event_sequence;

CREATE TABLE learning_progress_outbox (
    event_sequence  BIGINT PRIMARY KEY,
    event_id        UUID NOT NULL UNIQUE,
    user_id         UUID NOT NULL,
    license_class   VARCHAR(10) NOT NULL,
    bank_version    VARCHAR(64) NOT NULL,
    question_id     INTEGER NOT NULL,
    payload         JSONB NOT NULL,
    attempts        INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMPTZ,
    last_error      TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_learning_progress_outbox_pending
    ON learning_progress_outbox (next_attempt_at, event_sequence)
    WHERE published_at IS NULL;

CREATE FUNCTION enqueue_learning_progress(
    p_user_id UUID,
    p_license_class VARCHAR,
    p_bank_version VARCHAR,
    p_question_id INTEGER,
    p_dimension VARCHAR,
    p_learned BOOLEAN,
    p_needs_review BOOLEAN,
    p_studied_at TIMESTAMPTZ
) RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE
    v_chapter INTEGER;
    v_critical BOOLEAN;
    v_sequence BIGINT;
    v_id UUID;
BEGIN
    SELECT q.chapter,
           COALESCE(q.is_critical, FALSE) AND
               jsonb_exists(COALESCE(q.critical_licenses, '[]'::jsonb),
                            p_license_class)
    INTO v_chapter, v_critical
    FROM questions q
    WHERE q.id = p_question_id
      AND q.bank_version = p_bank_version
      AND jsonb_exists(q.applicable_licenses, p_license_class);

    -- Ignore legacy rows with an invalid bank/license rather than inventing progress.
    IF NOT FOUND THEN
        RETURN;
    END IF;

    v_sequence := nextval('learning_progress_event_sequence');
    v_id := gen_random_uuid();
    INSERT INTO learning_progress_outbox (
        event_sequence, event_id, user_id, license_class, bank_version,
        question_id, payload
    ) VALUES (
        v_sequence, v_id, p_user_id, p_license_class, p_bank_version,
        p_question_id,
        jsonb_build_object(
            'eventId', v_id,
            'eventType', 'QUESTION_PROGRESS_CHANGED',
            'schemaVersion', 1,
            'source', 'dts-practice',
            'eventSequence', v_sequence,
            'userId', p_user_id,
            'licenseClass', p_license_class,
            'bankVersion', p_bank_version,
            'questionId', p_question_id,
            'chapterId', v_chapter,
            'critical', v_critical,
            'dimension', p_dimension,
            'learned', p_learned,
            'needsReview', p_needs_review,
            'studiedAt', p_studied_at
        )
    );
END;
$$;

CREATE FUNCTION chapter_learning_event_trigger()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    PERFORM enqueue_learning_progress(
        NEW.user_id, NEW.license_class, NEW.bank_version, NEW.question_id,
        'CHAPTER', TRUE, NULL, NEW.last_answered_at
    );
    RETURN NEW;
END;
$$;

CREATE FUNCTION critical_learning_event_trigger()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    PERFORM enqueue_learning_progress(
        NEW.user_id, NEW.license_class, NEW.bank_version, NEW.question_id,
        'CRITICAL', NEW.learned, NULL, NEW.last_studied_at
    );
    RETURN NEW;
END;
$$;

CREATE FUNCTION review_learning_event_trigger()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    PERFORM enqueue_learning_progress(
        NEW.user_id, NEW.license_class, NEW.bank_version, NEW.question_id,
        'REVIEW', NULL, NEW.needs_review, NEW.last_answered_at
    );
    RETURN NEW;
END;
$$;

CREATE TRIGGER chapter_learning_event
AFTER INSERT OR UPDATE ON chapter_question_progress
FOR EACH ROW EXECUTE FUNCTION chapter_learning_event_trigger();

CREATE TRIGGER critical_learning_event
AFTER INSERT OR UPDATE ON critical_question_progress
FOR EACH ROW EXECUTE FUNCTION critical_learning_event_trigger();

CREATE TRIGGER review_learning_event
AFTER INSERT OR UPDATE ON wrong_question_progress
FOR EACH ROW EXECUTE FUNCTION review_learning_event_trigger();

-- Snapshot existing canonical state before the worker starts. Replaying these
-- events uses the same idempotent consumer as future changes.
DO $$
DECLARE r RECORD;
BEGIN
    FOR r IN SELECT * FROM chapter_question_progress LOOP
        PERFORM enqueue_learning_progress(
            r.user_id, r.license_class, r.bank_version, r.question_id,
            'CHAPTER', TRUE, NULL, r.last_answered_at
        );
    END LOOP;
    FOR r IN SELECT * FROM critical_question_progress LOOP
        PERFORM enqueue_learning_progress(
            r.user_id, r.license_class, r.bank_version, r.question_id,
            'CRITICAL', r.learned, NULL, r.last_studied_at
        );
    END LOOP;
    FOR r IN SELECT * FROM wrong_question_progress LOOP
        PERFORM enqueue_learning_progress(
            r.user_id, r.license_class, r.bank_version, r.question_id,
            'REVIEW', NULL, r.needs_review, r.last_answered_at
        );
    END LOOP;
END;
$$;
