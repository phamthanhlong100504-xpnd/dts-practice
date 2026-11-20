-- ============================================================================
-- V39: Consolidate practice tables and provide zero-downtime compatibility views
-- ============================================================================

-- 1. Create consolidated table `practice_review_sessions`
CREATE TABLE IF NOT EXISTS practice_review_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    license_class VARCHAR NOT NULL,
    bank_version VARCHAR NOT NULL,
    review_type VARCHAR NOT NULL, -- 'CHAPTER', 'CRITICAL', 'WRONG'
    chapter_id INT NULL,
    mode VARCHAR NULL,
    status VARCHAR NOT NULL DEFAULT 'IN_PROGRESS',
    current_question_id INT NULL REFERENCES questions(id),
    total_questions INT NOT NULL,
    answered_questions INT NOT NULL DEFAULT 0,
    correct_count INT NOT NULL DEFAULT 0,
    wrong_count INT NOT NULL DEFAULT 0,
    unanswered_count INT NOT NULL DEFAULT 0,
    remaining_count INT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS practice_review_questions (
    session_id UUID NOT NULL REFERENCES practice_review_sessions(id) ON DELETE CASCADE,
    question_id INT NOT NULL REFERENCES questions(id),
    question_order INT NOT NULL,
    initial_learned BOOLEAN NOT NULL DEFAULT false,
    previous_wrong_answer VARCHAR NULL,
    last_wrong_at TIMESTAMPTZ NULL,
    PRIMARY KEY (session_id, question_id)
);

CREATE TABLE IF NOT EXISTS practice_review_answers (
    session_id UUID NOT NULL,
    question_id INT NOT NULL,
    selected_answer VARCHAR NOT NULL,
    is_correct BOOLEAN NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (session_id, question_id),
    FOREIGN KEY (session_id, question_id) REFERENCES practice_review_questions(session_id, question_id) ON DELETE CASCADE
);

-- 2. Create consolidated table `user_question_progress`
CREATE TABLE IF NOT EXISTS user_question_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    license_class VARCHAR NOT NULL,
    bank_version VARCHAR NOT NULL,
    question_id INT NOT NULL REFERENCES questions(id),
    chapter_id INT NULL,
    selected_answer VARCHAR NULL,
    is_correct BOOLEAN NULL,
    learned BOOLEAN NOT NULL DEFAULT false,
    learned_at TIMESTAMPTZ NULL,
    last_answered_at TIMESTAMPTZ NULL,
    last_studied_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    needs_review BOOLEAN NOT NULL DEFAULT false,
    last_correct BOOLEAN NULL,
    last_selected_answer VARCHAR NULL,
    last_wrong_answer VARCHAR NULL,
    last_wrong_at TIMESTAMPTZ NULL,
    is_bookmarked BOOLEAN NOT NULL DEFAULT false,
    bookmarked_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, license_class, bank_version, question_id)
);

-- 3. Create consolidated table `practice_outbox`
CREATE TABLE IF NOT EXISTS practice_outbox (
    event_sequence BIGSERIAL PRIMARY KEY,
    topic VARCHAR NOT NULL,
    event_id UUID NOT NULL,
    user_id UUID NULL,
    exam_id UUID NULL,
    license_class VARCHAR NULL,
    bank_version VARCHAR NULL,
    question_id INT NULL,
    event_type VARCHAR NOT NULL,
    schema_version INT NOT NULL DEFAULT 1,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    payload JSONB NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ NULL,
    last_error TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_practice_outbox_topic_pub ON practice_outbox(topic, published_at, next_attempt_at);

-- 4. Create consolidated table `learning_cursors`
CREATE TABLE IF NOT EXISTS learning_cursors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    cursor_domain VARCHAR NOT NULL, -- 'THEORY', 'SIMULATION'
    license_class VARCHAR NULL,
    bank_version VARCHAR NOT NULL,
    chapter_id INT NULL,
    last_question_id INT NULL REFERENCES questions(id),
    scenario_id UUID NULL,
    source_attempt_id UUID NULL REFERENCES simulation_attempts(id),
    revision BIGINT NOT NULL DEFAULT 1,
    last_studied_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, cursor_domain, license_class, bank_version, chapter_id)
);

-- 5. Create consolidated table `reference_items`
CREATE TABLE IF NOT EXISTS reference_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_type VARCHAR NOT NULL, -- 'MEMORY_TIP', 'TRAFFIC_SIGN'
    bank_version VARCHAR NOT NULL,
    source_item_id INT NOT NULL,
    content_builder_source_id UUID NULL,
    sort_order INT NULL,
    code VARCHAR NULL,
    title VARCHAR NOT NULL,
    category VARCHAR NULL,
    category_name VARCHAR NULL,
    meaning TEXT NULL,
    quick_rule TEXT NULL,
    explanation TEXT NULL,
    source_image VARCHAR NULL,
    media_file_id UUID NULL,
    applicable_licenses JSONB NULL,
    status VARCHAR NOT NULL DEFAULT 'PUBLISHED',
    published_at TIMESTAMP WITHOUT TIME ZONE NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (item_type, bank_version, source_item_id)
);

-- 6. Add columns to preset_exam_sets and simulation_exam_sessions
ALTER TABLE preset_exam_sets ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;

CREATE OR REPLACE FUNCTION adm04_protect_published_preset()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status = 'PUBLISHED' AND (
        NEW.code IS DISTINCT FROM OLD.code OR
        NEW.title IS DISTINCT FROM OLD.title OR
        NEW.license_class IS DISTINCT FROM OLD.license_class OR
        NEW.bank_version IS DISTINCT FROM OLD.bank_version OR
        NEW.total_questions IS DISTINCT FROM OLD.total_questions OR
        NEW.duration_minutes IS DISTINCT FROM OLD.duration_minutes OR
        NEW.logical_set_id IS DISTINCT FROM OLD.logical_set_id OR
        NEW.version_number IS DISTINCT FROM OLD.version_number
    ) THEN
        RAISE EXCEPTION 'Published preset is immutable; create a new reviewed version' USING ERRCODE='23514';
    END IF;
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

ALTER TABLE simulation_exam_sessions 
    ADD COLUMN IF NOT EXISTS total_score INT NULL,
    ADD COLUMN IF NOT EXISTS passing_score INT NULL,
    ADD COLUMN IF NOT EXISTS policy_version VARCHAR NULL,
    ADD COLUMN IF NOT EXISTS passed BOOLEAN NULL,
    ADD COLUMN IF NOT EXISTS result_status VARCHAR NULL,
    ADD COLUMN IF NOT EXISTS result_json JSONB NULL,
    ADD COLUMN IF NOT EXISTS result_completed_at TIMESTAMPTZ NULL;

CREATE OR REPLACE FUNCTION simulation_exam_guard_session()
RETURNS TRIGGER AS $$
BEGIN
 IF TG_OP='DELETE' THEN RAISE EXCEPTION 'Simulation exam audit is retained'; END IF;
 IF ROW(NEW.id,NEW.user_id,NEW.mode,NEW.license_class,NEW.bank_version,NEW.scoring_version,NEW.manifest_sha256,NEW.create_key,NEW.create_sha256,NEW.created_at,NEW.started_at)
 IS DISTINCT FROM ROW(OLD.id,OLD.user_id,OLD.mode,OLD.license_class,OLD.bank_version,OLD.scoring_version,OLD.manifest_sha256,OLD.create_key,OLD.create_sha256,OLD.created_at,OLD.started_at)
 THEN RAISE EXCEPTION 'Simulation exam snapshot is immutable'; END IF;
 RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 7. Zero-Downtime Compatibility Views & Triggers

-- Review Sessions Views
CREATE OR REPLACE VIEW chapter_review_sessions AS
SELECT 
    id, user_id, license_class, bank_version, chapter_id, status,
    current_question_id, total_questions, started_at, completed_at
FROM practice_review_sessions
WHERE review_type = 'CHAPTER';

CREATE OR REPLACE FUNCTION trg_chapter_review_sessions_io()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_review_sessions (
            id, user_id, license_class, bank_version, review_type, chapter_id,
            status, current_question_id, total_questions, started_at, completed_at
        ) VALUES (
            COALESCE(NEW.id, gen_random_uuid()), NEW.user_id, NEW.license_class, NEW.bank_version, 'CHAPTER',
            NEW.chapter_id, COALESCE(NEW.status, 'IN_PROGRESS'), NEW.current_question_id, NEW.total_questions,
            COALESCE(NEW.started_at, now()), NEW.completed_at
        ) RETURNING id INTO NEW.id;
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE practice_review_sessions SET
            status = NEW.status,
            current_question_id = NEW.current_question_id,
            completed_at = NEW.completed_at,
            updated_at = now()
        WHERE id = OLD.id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_review_sessions WHERE id = OLD.id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS chapter_review_sessions_iot ON chapter_review_sessions;
CREATE TRIGGER chapter_review_sessions_iot
INSTEAD OF INSERT OR UPDATE OR DELETE ON chapter_review_sessions
FOR EACH ROW EXECUTE FUNCTION trg_chapter_review_sessions_io();

CREATE OR REPLACE VIEW critical_question_sessions AS
SELECT 
    id, user_id, license_class, bank_version, mode, status,
    current_question_id, total_questions, started_at, completed_at, created_at, updated_at
FROM practice_review_sessions
WHERE review_type = 'CRITICAL';

CREATE OR REPLACE FUNCTION trg_critical_question_sessions_io()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_review_sessions (
            id, user_id, license_class, bank_version, review_type, mode,
            status, current_question_id, total_questions, started_at, completed_at, created_at, updated_at
        ) VALUES (
            COALESCE(NEW.id, gen_random_uuid()), NEW.user_id, NEW.license_class, NEW.bank_version, 'CRITICAL',
            NEW.mode, COALESCE(NEW.status, 'IN_PROGRESS'), NEW.current_question_id, NEW.total_questions,
            COALESCE(NEW.started_at, now()), NEW.completed_at, COALESCE(NEW.created_at, now()), COALESCE(NEW.updated_at, now())
        ) RETURNING id INTO NEW.id;
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE practice_review_sessions SET
            mode = NEW.mode,
            status = NEW.status,
            current_question_id = NEW.current_question_id,
            completed_at = NEW.completed_at,
            updated_at = now()
        WHERE id = OLD.id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_review_sessions WHERE id = OLD.id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS critical_question_sessions_iot ON critical_question_sessions;
CREATE TRIGGER critical_question_sessions_iot
INSTEAD OF INSERT OR UPDATE OR DELETE ON critical_question_sessions
FOR EACH ROW EXECUTE FUNCTION trg_critical_question_sessions_io();

CREATE OR REPLACE VIEW wrong_question_review_sessions AS
SELECT 
    id, user_id, license_class, bank_version, status,
    current_question_id, total_questions, answered_questions, correct_count, wrong_count,
    unanswered_count, remaining_count, started_at, completed_at
FROM practice_review_sessions
WHERE review_type = 'WRONG';

CREATE OR REPLACE FUNCTION trg_wrong_question_review_sessions_io()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_review_sessions (
            id, user_id, license_class, bank_version, review_type,
            status, current_question_id, total_questions, answered_questions, correct_count,
            wrong_count, unanswered_count, remaining_count, started_at, completed_at
        ) VALUES (
            COALESCE(NEW.id, gen_random_uuid()), NEW.user_id, NEW.license_class, NEW.bank_version, 'WRONG',
            COALESCE(NEW.status, 'IN_PROGRESS'), NEW.current_question_id, NEW.total_questions,
            COALESCE(NEW.answered_questions, 0), COALESCE(NEW.correct_count, 0), COALESCE(NEW.wrong_count, 0),
            COALESCE(NEW.unanswered_count, 0), NEW.remaining_count, COALESCE(NEW.started_at, now()), NEW.completed_at
        ) RETURNING id INTO NEW.id;
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE practice_review_sessions SET
            status = NEW.status,
            current_question_id = NEW.current_question_id,
            answered_questions = NEW.answered_questions,
            correct_count = NEW.correct_count,
            wrong_count = NEW.wrong_count,
            unanswered_count = NEW.unanswered_count,
            remaining_count = NEW.remaining_count,
            completed_at = NEW.completed_at,
            updated_at = now()
        WHERE id = OLD.id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_review_sessions WHERE id = OLD.id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS wrong_question_review_sessions_iot ON wrong_question_review_sessions;
CREATE TRIGGER wrong_question_review_sessions_iot
INSTEAD OF INSERT OR UPDATE OR DELETE ON wrong_question_review_sessions
FOR EACH ROW EXECUTE FUNCTION trg_wrong_question_review_sessions_io();

-- Review Questions Views
CREATE OR REPLACE VIEW chapter_review_session_questions AS
SELECT q.session_id, q.question_id, q.question_order
FROM practice_review_questions q
JOIN practice_review_sessions s ON s.id = q.session_id
WHERE s.review_type = 'CHAPTER';

CREATE OR REPLACE FUNCTION trg_chapter_review_session_questions_io()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_review_questions (session_id, question_id, question_order)
        VALUES (NEW.session_id, NEW.question_id, NEW.question_order)
        ON CONFLICT (session_id, question_id) DO UPDATE SET question_order = EXCLUDED.question_order;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_review_questions WHERE session_id = OLD.session_id AND question_id = OLD.question_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS chapter_review_session_questions_iot ON chapter_review_session_questions;
CREATE TRIGGER chapter_review_session_questions_iot
INSTEAD OF INSERT OR DELETE ON chapter_review_session_questions
FOR EACH ROW EXECUTE FUNCTION trg_chapter_review_session_questions_io();

CREATE OR REPLACE VIEW critical_question_session_questions AS
SELECT q.session_id, q.question_id, q.question_order, q.initial_learned
FROM practice_review_questions q
JOIN practice_review_sessions s ON s.id = q.session_id
WHERE s.review_type = 'CRITICAL';

CREATE OR REPLACE FUNCTION trg_critical_question_session_questions_io()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_review_questions (session_id, question_id, question_order, initial_learned)
        VALUES (NEW.session_id, NEW.question_id, NEW.question_order, COALESCE(NEW.initial_learned, false))
        ON CONFLICT (session_id, question_id) DO UPDATE SET 
            question_order = EXCLUDED.question_order,
            initial_learned = EXCLUDED.initial_learned;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_review_questions WHERE session_id = OLD.session_id AND question_id = OLD.question_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS critical_question_session_questions_iot ON critical_question_session_questions;
CREATE TRIGGER critical_question_session_questions_iot
INSTEAD OF INSERT OR DELETE ON critical_question_session_questions
FOR EACH ROW EXECUTE FUNCTION trg_critical_question_session_questions_io();

CREATE OR REPLACE VIEW wrong_question_review_session_questions AS
SELECT q.session_id, q.question_id, q.question_order, q.previous_wrong_answer, q.last_wrong_at
FROM practice_review_questions q
JOIN practice_review_sessions s ON s.id = q.session_id
WHERE s.review_type = 'WRONG';

CREATE OR REPLACE FUNCTION trg_wrong_question_review_session_questions_io()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_review_questions (session_id, question_id, question_order, previous_wrong_answer, last_wrong_at)
        VALUES (NEW.session_id, NEW.question_id, NEW.question_order, NEW.previous_wrong_answer, NEW.last_wrong_at)
        ON CONFLICT (session_id, question_id) DO UPDATE SET 
            question_order = EXCLUDED.question_order,
            previous_wrong_answer = EXCLUDED.previous_wrong_answer,
            last_wrong_at = EXCLUDED.last_wrong_at;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_review_questions WHERE session_id = OLD.session_id AND question_id = OLD.question_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS wrong_question_review_session_questions_iot ON wrong_question_review_session_questions;
CREATE TRIGGER wrong_question_review_session_questions_iot
INSTEAD OF INSERT OR DELETE ON wrong_question_review_session_questions
FOR EACH ROW EXECUTE FUNCTION trg_wrong_question_review_session_questions_io();

-- Review Answers Views
CREATE OR REPLACE VIEW chapter_review_session_answers AS
SELECT a.session_id, a.question_id, a.selected_answer, a.is_correct, a.answered_at
FROM practice_review_answers a
JOIN practice_review_sessions s ON s.id = a.session_id
WHERE s.review_type = 'CHAPTER';

CREATE OR REPLACE VIEW critical_question_session_answers AS
SELECT a.session_id, a.question_id, a.selected_answer, a.is_correct, a.answered_at
FROM practice_review_answers a
JOIN practice_review_sessions s ON s.id = a.session_id
WHERE s.review_type = 'CRITICAL';

CREATE OR REPLACE VIEW wrong_question_review_answers AS
SELECT a.session_id, a.question_id, a.selected_answer, a.is_correct, a.answered_at
FROM practice_review_answers a
JOIN practice_review_sessions s ON s.id = a.session_id
WHERE s.review_type = 'WRONG';

CREATE OR REPLACE FUNCTION trg_practice_review_answers_io()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_review_answers (session_id, question_id, selected_answer, is_correct, answered_at)
        VALUES (NEW.session_id, NEW.question_id, NEW.selected_answer, NEW.is_correct, COALESCE(NEW.answered_at, now()))
        ON CONFLICT (session_id, question_id) DO UPDATE SET
            selected_answer = EXCLUDED.selected_answer,
            is_correct = EXCLUDED.is_correct,
            answered_at = EXCLUDED.answered_at;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_review_answers WHERE session_id = OLD.session_id AND question_id = OLD.question_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS chapter_review_session_answers_iot ON chapter_review_session_answers;
CREATE TRIGGER chapter_review_session_answers_iot
INSTEAD OF INSERT OR DELETE ON chapter_review_session_answers
FOR EACH ROW EXECUTE FUNCTION trg_practice_review_answers_io();

DROP TRIGGER IF EXISTS critical_question_session_answers_iot ON critical_question_session_answers;
CREATE TRIGGER critical_question_session_answers_iot
INSTEAD OF INSERT OR DELETE ON critical_question_session_answers
FOR EACH ROW EXECUTE FUNCTION trg_practice_review_answers_io();

DROP TRIGGER IF EXISTS wrong_question_review_answers_iot ON wrong_question_review_answers;
CREATE TRIGGER wrong_question_review_answers_iot
INSTEAD OF INSERT OR DELETE ON wrong_question_review_answers
FOR EACH ROW EXECUTE FUNCTION trg_practice_review_answers_io();

-- Progress & Bookmarks Views
CREATE OR REPLACE VIEW chapter_question_progress AS
SELECT user_id, license_class, bank_version, question_id, chapter_id, selected_answer, is_correct, learned_at, last_answered_at
FROM user_question_progress
WHERE chapter_id IS NOT NULL;

CREATE OR REPLACE FUNCTION trg_chapter_question_progress_io()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO user_question_progress (
            user_id, license_class, bank_version, question_id, chapter_id,
            selected_answer, is_correct, learned, learned_at, last_answered_at, updated_at
        ) VALUES (
            NEW.user_id, NEW.license_class, NEW.bank_version, NEW.question_id, NEW.chapter_id,
            NEW.selected_answer, NEW.is_correct, (NEW.is_correct IS TRUE),
            COALESCE(NEW.learned_at, now()), COALESCE(NEW.last_answered_at, now()), now()
        ) ON CONFLICT (user_id, license_class, bank_version, question_id) DO UPDATE SET
            chapter_id = EXCLUDED.chapter_id,
            selected_answer = EXCLUDED.selected_answer,
            is_correct = EXCLUDED.is_correct,
            learned = EXCLUDED.learned,
            learned_at = EXCLUDED.learned_at,
            last_answered_at = EXCLUDED.last_answered_at,
            updated_at = now();
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM user_question_progress WHERE user_id = OLD.user_id AND question_id = OLD.question_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS chapter_question_progress_iot ON chapter_question_progress;
CREATE TRIGGER chapter_question_progress_iot
INSTEAD OF INSERT OR DELETE ON chapter_question_progress
FOR EACH ROW EXECUTE FUNCTION trg_chapter_question_progress_io();

CREATE OR REPLACE VIEW critical_question_progress AS
SELECT id, user_id, license_class, bank_version, question_id, learned, last_studied_at, created_at, updated_at,
       selected_answer, is_correct, learned_at, last_answered_at
FROM user_question_progress;

CREATE OR REPLACE FUNCTION trg_critical_question_progress_io()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO user_question_progress (
            id, user_id, license_class, bank_version, question_id, learned,
            last_studied_at, created_at, updated_at, selected_answer, is_correct, learned_at, last_answered_at
        ) VALUES (
            COALESCE(NEW.id, gen_random_uuid()), NEW.user_id, NEW.license_class, NEW.bank_version, NEW.question_id,
            COALESCE(NEW.learned, false), COALESCE(NEW.last_studied_at, now()), COALESCE(NEW.created_at, now()),
            COALESCE(NEW.updated_at, now()), NEW.selected_answer, NEW.is_correct, NEW.learned_at, NEW.last_answered_at
        ) ON CONFLICT (user_id, license_class, bank_version, question_id) DO UPDATE SET
            learned = EXCLUDED.learned,
            last_studied_at = EXCLUDED.last_studied_at,
            selected_answer = EXCLUDED.selected_answer,
            is_correct = EXCLUDED.is_correct,
            learned_at = EXCLUDED.learned_at,
            last_answered_at = EXCLUDED.last_answered_at,
            updated_at = now()
        RETURNING id INTO NEW.id;
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE user_question_progress SET
            learned = NEW.learned,
            last_studied_at = NEW.last_studied_at,
            selected_answer = NEW.selected_answer,
            is_correct = NEW.is_correct,
            learned_at = NEW.learned_at,
            last_answered_at = NEW.last_answered_at,
            updated_at = now()
        WHERE id = OLD.id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM user_question_progress WHERE id = OLD.id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS critical_question_progress_iot ON critical_question_progress;
CREATE TRIGGER critical_question_progress_iot
INSTEAD OF INSERT OR UPDATE OR DELETE ON critical_question_progress
FOR EACH ROW EXECUTE FUNCTION trg_critical_question_progress_io();

CREATE OR REPLACE VIEW wrong_question_progress AS
SELECT user_id, license_class, bank_version, question_id, needs_review, last_correct,
       last_selected_answer, last_wrong_answer, last_wrong_at, last_answered_at, created_at, updated_at
FROM user_question_progress
WHERE needs_review = true OR last_wrong_answer IS NOT NULL;

CREATE OR REPLACE FUNCTION trg_wrong_question_progress_io()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO user_question_progress (
            user_id, license_class, bank_version, question_id, needs_review, last_correct,
            last_selected_answer, last_wrong_answer, last_wrong_at, last_answered_at, created_at, updated_at
        ) VALUES (
            NEW.user_id, NEW.license_class, NEW.bank_version, NEW.question_id, NEW.needs_review, NEW.last_correct,
            NEW.last_selected_answer, NEW.last_wrong_answer, NEW.last_wrong_at, NEW.last_answered_at,
            COALESCE(NEW.created_at, now()), COALESCE(NEW.updated_at, now())
        ) ON CONFLICT (user_id, license_class, bank_version, question_id) DO UPDATE SET
            needs_review = EXCLUDED.needs_review,
            last_correct = EXCLUDED.last_correct,
            last_selected_answer = EXCLUDED.last_selected_answer,
            last_wrong_answer = EXCLUDED.last_wrong_answer,
            last_wrong_at = EXCLUDED.last_wrong_at,
            last_answered_at = EXCLUDED.last_answered_at,
            updated_at = now();
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE user_question_progress SET
            needs_review = NEW.needs_review,
            last_correct = NEW.last_correct,
            last_selected_answer = NEW.last_selected_answer,
            last_wrong_answer = NEW.last_wrong_answer,
            last_wrong_at = NEW.last_wrong_at,
            last_answered_at = NEW.last_answered_at,
            updated_at = now()
        WHERE user_id = OLD.user_id AND license_class = OLD.license_class AND bank_version = OLD.bank_version AND question_id = OLD.question_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM user_question_progress WHERE user_id = OLD.user_id AND license_class = OLD.license_class AND bank_version = OLD.bank_version AND question_id = OLD.question_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS wrong_question_progress_iot ON wrong_question_progress;
CREATE TRIGGER wrong_question_progress_iot
INSTEAD OF INSERT OR UPDATE OR DELETE ON wrong_question_progress
FOR EACH ROW EXECUTE FUNCTION trg_wrong_question_progress_io();

CREATE OR REPLACE VIEW chapter_question_bookmarks AS
SELECT user_id, license_class, bank_version, question_id, chapter_id, bookmarked_at
FROM user_question_progress
WHERE is_bookmarked = true;

CREATE OR REPLACE FUNCTION trg_chapter_question_bookmarks_io()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO user_question_progress (
            user_id, license_class, bank_version, question_id, chapter_id, is_bookmarked, bookmarked_at
        ) VALUES (
            NEW.user_id, NEW.license_class, NEW.bank_version, NEW.question_id, NEW.chapter_id, true, COALESCE(NEW.bookmarked_at, now())
        ) ON CONFLICT (user_id, license_class, bank_version, question_id) DO UPDATE SET
            is_bookmarked = true,
            bookmarked_at = COALESCE(EXCLUDED.bookmarked_at, now()),
            chapter_id = COALESCE(user_question_progress.chapter_id, EXCLUDED.chapter_id),
            updated_at = now();
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE user_question_progress SET is_bookmarked = false, bookmarked_at = NULL WHERE user_id = OLD.user_id AND question_id = OLD.question_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS chapter_question_bookmarks_iot ON chapter_question_bookmarks;
CREATE TRIGGER chapter_question_bookmarks_iot
INSTEAD OF INSERT OR DELETE ON chapter_question_bookmarks
FOR EACH ROW EXECUTE FUNCTION trg_chapter_question_bookmarks_io();

-- Outbox Views
CREATE OR REPLACE VIEW exam_progress_outbox AS
SELECT event_id, user_id, exam_id, event_type, schema_version, payload, attempts, next_attempt_at, published_at, last_error, created_at
FROM practice_outbox WHERE topic = 'exam-progress';

CREATE OR REPLACE VIEW learning_progress_outbox AS
SELECT event_sequence, event_id, user_id, license_class, bank_version, question_id, payload, attempts, next_attempt_at, published_at, last_error, created_at
FROM practice_outbox WHERE topic = 'learning-progress';

CREATE OR REPLACE VIEW practice_exp_outbox AS
SELECT event_id, event_type, user_id, payload, attempts, next_attempt_at, published_at, last_error, created_at
FROM practice_outbox WHERE topic = 'practice-exp';

CREATE OR REPLACE VIEW practice_result_outbox AS
SELECT event_id, exam_id, user_id, payload, attempts, next_attempt_at, published_at, last_error, created_at
FROM practice_outbox WHERE topic = 'practice-result';

CREATE OR REPLACE VIEW simulation_performance_outbox AS
SELECT event_sequence, event_id, event_type, occurred_at, payload, attempts, next_attempt_at, published_at, last_error, created_at
FROM practice_outbox WHERE topic = 'simulation-performance';

CREATE OR REPLACE FUNCTION trg_exam_progress_outbox_io() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_outbox (topic, event_id, user_id, exam_id, event_type, schema_version, payload, attempts, next_attempt_at, published_at, last_error, created_at)
        VALUES ('exam-progress', NEW.event_id, NEW.user_id, NEW.exam_id, NEW.event_type, COALESCE(NEW.schema_version, 1), NEW.payload, COALESCE(NEW.attempts, 0), COALESCE(NEW.next_attempt_at, now()), NEW.published_at, NEW.last_error, COALESCE(NEW.created_at, now()));
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE practice_outbox SET attempts = NEW.attempts, next_attempt_at = NEW.next_attempt_at, published_at = NEW.published_at, last_error = NEW.last_error
        WHERE event_id = OLD.event_id AND topic = 'exam-progress';
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_outbox WHERE event_id = OLD.event_id AND topic = 'exam-progress';
        RETURN OLD;
    END IF;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS exam_progress_outbox_iot ON exam_progress_outbox;
CREATE TRIGGER exam_progress_outbox_iot INSTEAD OF INSERT OR UPDATE OR DELETE ON exam_progress_outbox FOR EACH ROW EXECUTE FUNCTION trg_exam_progress_outbox_io();

CREATE OR REPLACE FUNCTION trg_learning_progress_outbox_io() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_outbox (topic, event_id, user_id, license_class, bank_version, question_id, event_type, payload, attempts, next_attempt_at, published_at, last_error, created_at)
        VALUES ('learning-progress', NEW.event_id, NEW.user_id, NEW.license_class, NEW.bank_version, NEW.question_id, 'LEARNING_PROGRESS_UPDATED', NEW.payload, COALESCE(NEW.attempts, 0), COALESCE(NEW.next_attempt_at, now()), NEW.published_at, NEW.last_error, COALESCE(NEW.created_at, now()))
        RETURNING event_sequence INTO NEW.event_sequence;
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE practice_outbox SET attempts = NEW.attempts, next_attempt_at = NEW.next_attempt_at, published_at = NEW.published_at, last_error = NEW.last_error
        WHERE event_sequence = OLD.event_sequence;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_outbox WHERE event_sequence = OLD.event_sequence;
        RETURN OLD;
    END IF;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS learning_progress_outbox_iot ON learning_progress_outbox;
CREATE TRIGGER learning_progress_outbox_iot INSTEAD OF INSERT OR UPDATE OR DELETE ON learning_progress_outbox FOR EACH ROW EXECUTE FUNCTION trg_learning_progress_outbox_io();

CREATE OR REPLACE FUNCTION trg_practice_exp_outbox_io() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_outbox (topic, event_id, event_type, user_id, payload, attempts, next_attempt_at, published_at, last_error, created_at)
        VALUES ('practice-exp', NEW.event_id, NEW.event_type, NEW.user_id, NEW.payload, COALESCE(NEW.attempts, 0), COALESCE(NEW.next_attempt_at, now()), NEW.published_at, NEW.last_error, COALESCE(NEW.created_at, now()));
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE practice_outbox SET attempts = NEW.attempts, next_attempt_at = NEW.next_attempt_at, published_at = NEW.published_at, last_error = NEW.last_error
        WHERE event_id = OLD.event_id AND topic = 'practice-exp';
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_outbox WHERE event_id = OLD.event_id AND topic = 'practice-exp';
        RETURN OLD;
    END IF;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS practice_exp_outbox_iot ON practice_exp_outbox;
CREATE TRIGGER practice_exp_outbox_iot INSTEAD OF INSERT OR UPDATE OR DELETE ON practice_exp_outbox FOR EACH ROW EXECUTE FUNCTION trg_practice_exp_outbox_io();

CREATE OR REPLACE FUNCTION trg_practice_result_outbox_io() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_outbox (topic, event_id, exam_id, user_id, event_type, payload, attempts, next_attempt_at, published_at, last_error, created_at)
        VALUES ('practice-result', NEW.event_id, NEW.exam_id, NEW.user_id, 'PRACTICE_RESULT_RECORDED', NEW.payload, COALESCE(NEW.attempts, 0), COALESCE(NEW.next_attempt_at, now()), NEW.published_at, NEW.last_error, COALESCE(NEW.created_at, now()));
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE practice_outbox SET attempts = NEW.attempts, next_attempt_at = NEW.next_attempt_at, published_at = NEW.published_at, last_error = NEW.last_error
        WHERE event_id = OLD.event_id AND topic = 'practice-result';
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_outbox WHERE event_id = OLD.event_id AND topic = 'practice-result';
        RETURN OLD;
    END IF;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS practice_result_outbox_iot ON practice_result_outbox;
CREATE TRIGGER practice_result_outbox_iot INSTEAD OF INSERT OR UPDATE OR DELETE ON practice_result_outbox FOR EACH ROW EXECUTE FUNCTION trg_practice_result_outbox_io();

CREATE OR REPLACE FUNCTION trg_simulation_performance_outbox_io() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO practice_outbox (topic, event_id, event_type, occurred_at, payload, attempts, next_attempt_at, published_at, last_error, created_at)
        VALUES ('simulation-performance', NEW.event_id, NEW.event_type, COALESCE(NEW.occurred_at, now()), NEW.payload, COALESCE(NEW.attempts, 0), COALESCE(NEW.next_attempt_at, now()), NEW.published_at, NEW.last_error, COALESCE(NEW.created_at, now()))
        RETURNING event_sequence INTO NEW.event_sequence;
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE practice_outbox SET attempts = NEW.attempts, next_attempt_at = NEW.next_attempt_at, published_at = NEW.published_at, last_error = NEW.last_error
        WHERE event_sequence = OLD.event_sequence;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM practice_outbox WHERE event_sequence = OLD.event_sequence;
        RETURN OLD;
    END IF;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS simulation_performance_outbox_iot ON simulation_performance_outbox;
CREATE TRIGGER simulation_performance_outbox_iot INSTEAD OF INSERT OR UPDATE OR DELETE ON simulation_performance_outbox FOR EACH ROW EXECUTE FUNCTION trg_simulation_performance_outbox_io();

-- Cursors Views
CREATE OR REPLACE VIEW chapter_learning_cursor AS
SELECT user_id, license_class, bank_version, chapter_id, last_question_id, last_studied_at
FROM learning_cursors
WHERE cursor_domain = 'THEORY';

CREATE OR REPLACE VIEW simulation_cursors AS
SELECT user_id, bank_version, chapter_id, scenario_id, source_attempt_id, revision, updated_at
FROM learning_cursors
WHERE cursor_domain = 'SIMULATION';

CREATE OR REPLACE FUNCTION trg_chapter_learning_cursor_io() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO learning_cursors (user_id, cursor_domain, license_class, bank_version, chapter_id, last_question_id, last_studied_at, updated_at)
        VALUES (NEW.user_id, 'THEORY', NEW.license_class, NEW.bank_version, NEW.chapter_id, NEW.last_question_id, COALESCE(NEW.last_studied_at, now()), now())
        ON CONFLICT (user_id, cursor_domain, license_class, bank_version, chapter_id) DO UPDATE SET
            last_question_id = EXCLUDED.last_question_id,
            last_studied_at = EXCLUDED.last_studied_at,
            updated_at = now();
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE learning_cursors SET last_question_id = NEW.last_question_id, last_studied_at = NEW.last_studied_at, updated_at = now()
        WHERE user_id = OLD.user_id AND cursor_domain = 'THEORY' AND license_class = OLD.license_class AND bank_version = OLD.bank_version AND chapter_id = OLD.chapter_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM learning_cursors WHERE user_id = OLD.user_id AND cursor_domain = 'THEORY' AND license_class = OLD.license_class AND bank_version = OLD.bank_version AND chapter_id = OLD.chapter_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS chapter_learning_cursor_iot ON chapter_learning_cursor;
CREATE TRIGGER chapter_learning_cursor_iot INSTEAD OF INSERT OR UPDATE OR DELETE ON chapter_learning_cursor FOR EACH ROW EXECUTE FUNCTION trg_chapter_learning_cursor_io();

CREATE OR REPLACE FUNCTION trg_simulation_cursors_io() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO learning_cursors (user_id, cursor_domain, license_class, bank_version, chapter_id, scenario_id, source_attempt_id, revision, updated_at)
        VALUES (NEW.user_id, 'SIMULATION', 'SIMULATION', NEW.bank_version, NEW.chapter_id, NEW.scenario_id, NEW.source_attempt_id, COALESCE(NEW.revision, 1), COALESCE(NEW.updated_at, now()))
        ON CONFLICT (user_id, cursor_domain, license_class, bank_version, chapter_id) DO UPDATE SET
            scenario_id = EXCLUDED.scenario_id,
            source_attempt_id = EXCLUDED.source_attempt_id,
            revision = EXCLUDED.revision,
            updated_at = EXCLUDED.updated_at;
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE learning_cursors SET scenario_id = NEW.scenario_id, source_attempt_id = NEW.source_attempt_id, revision = NEW.revision, updated_at = NEW.updated_at
        WHERE user_id = OLD.user_id AND cursor_domain = 'SIMULATION' AND chapter_id = OLD.chapter_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM learning_cursors WHERE user_id = OLD.user_id AND cursor_domain = 'SIMULATION' AND chapter_id = OLD.chapter_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS simulation_cursors_iot ON simulation_cursors;
CREATE TRIGGER simulation_cursors_iot INSTEAD OF INSERT OR UPDATE OR DELETE ON simulation_cursors FOR EACH ROW EXECUTE FUNCTION trg_simulation_cursors_io();

-- Reference Views
CREATE OR REPLACE VIEW memory_tips AS
SELECT 
    id, bank_version, source_item_id AS source_tip_id, content_builder_source_id AS content_builder_memory_tip_id,
    sort_order, title, quick_rule, explanation, status, published_at, created_at, updated_at
FROM reference_items
WHERE item_type = 'MEMORY_TIP';

CREATE OR REPLACE VIEW traffic_signs AS
SELECT 
    id, bank_version, source_item_id AS source_sign_id, content_builder_source_id AS content_builder_traffic_sign_id,
    code, title AS name, category, category_name, meaning, quick_rule AS quick_tip, source_image, media_file_id,
    applicable_licenses, status, published_at, created_at, updated_at
FROM reference_items
WHERE item_type = 'TRAFFIC_SIGN';

CREATE OR REPLACE FUNCTION trg_memory_tips_io() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO reference_items (
            id, item_type, bank_version, source_item_id, content_builder_source_id,
            sort_order, title, quick_rule, explanation, status, published_at, created_at, updated_at
        ) VALUES (
            COALESCE(NEW.id, gen_random_uuid()), 'MEMORY_TIP', NEW.bank_version, NEW.source_tip_id, NEW.content_builder_memory_tip_id,
            NEW.sort_order, NEW.title, NEW.quick_rule, NEW.explanation, COALESCE(NEW.status, 'PUBLISHED'),
            NEW.published_at, COALESCE(NEW.created_at, now()), COALESCE(NEW.updated_at, now())
        ) ON CONFLICT (item_type, bank_version, source_item_id) DO UPDATE SET
            title = EXCLUDED.title,
            quick_rule = EXCLUDED.quick_rule,
            explanation = EXCLUDED.explanation,
            sort_order = EXCLUDED.sort_order,
            status = EXCLUDED.status,
            updated_at = now();
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM reference_items WHERE id = OLD.id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS memory_tips_iot ON memory_tips;
CREATE TRIGGER memory_tips_iot INSTEAD OF INSERT OR DELETE ON memory_tips FOR EACH ROW EXECUTE FUNCTION trg_memory_tips_io();

CREATE OR REPLACE FUNCTION trg_traffic_signs_io() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO reference_items (
            id, item_type, bank_version, source_item_id, content_builder_source_id,
            code, title, category, category_name, meaning, quick_rule, source_image, media_file_id,
            applicable_licenses, status, published_at, created_at, updated_at
        ) VALUES (
            COALESCE(NEW.id, gen_random_uuid()), 'TRAFFIC_SIGN', NEW.bank_version, NEW.source_sign_id, NEW.content_builder_traffic_sign_id,
            NEW.code, NEW.name, NEW.category, NEW.category_name, NEW.meaning, NEW.quick_tip, NEW.source_image, NEW.media_file_id,
            NEW.applicable_licenses, COALESCE(NEW.status, 'PUBLISHED'), NEW.published_at, COALESCE(NEW.created_at, now()), COALESCE(NEW.updated_at, now())
        ) ON CONFLICT (item_type, bank_version, source_item_id) DO UPDATE SET
            code = EXCLUDED.code,
            title = EXCLUDED.title,
            category = EXCLUDED.category,
            category_name = EXCLUDED.category_name,
            meaning = EXCLUDED.meaning,
            quick_rule = EXCLUDED.quick_rule,
            source_image = EXCLUDED.source_image,
            media_file_id = EXCLUDED.media_file_id,
            applicable_licenses = EXCLUDED.applicable_licenses,
            status = EXCLUDED.status,
            updated_at = now();
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        DELETE FROM reference_items WHERE id = OLD.id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS traffic_signs_iot ON traffic_signs;
CREATE TRIGGER traffic_signs_iot INSTEAD OF INSERT OR DELETE ON traffic_signs FOR EACH ROW EXECUTE FUNCTION trg_traffic_signs_io();

-- Admin Presets & Simulation Exam Results Views
CREATE OR REPLACE VIEW admin_preset_active_versions AS
SELECT 
    logical_set_id,
    id AS active_version_id,
    1::bigint AS revision,
    updated_at,
    NULL::uuid AS updated_by
FROM preset_exam_sets
WHERE is_active = true;

CREATE OR REPLACE FUNCTION trg_admin_preset_active_versions_io() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' OR TG_OP = 'INSERT' THEN
        UPDATE preset_exam_sets SET is_active = (id = NEW.active_version_id)
        WHERE logical_set_id = NEW.logical_set_id;
        RETURN NEW;
    END IF;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS admin_preset_active_versions_iot ON admin_preset_active_versions;
CREATE TRIGGER admin_preset_active_versions_iot INSTEAD OF INSERT OR UPDATE ON admin_preset_active_versions FOR EACH ROW EXECUTE FUNCTION trg_admin_preset_active_versions_io();

CREATE OR REPLACE VIEW simulation_exam_results AS
SELECT 
    id AS session_id,
    total_score,
    passing_score,
    policy_version,
    passed,
    result_status,
    result_json,
    result_completed_at AS completed_at
FROM simulation_exam_sessions
WHERE total_score IS NOT NULL OR result_status IS NOT NULL;

CREATE OR REPLACE FUNCTION trg_simulation_exam_results_io() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        UPDATE simulation_exam_sessions SET
            total_score = NEW.total_score,
            passing_score = NEW.passing_score,
            policy_version = NEW.policy_version,
            passed = NEW.passed,
            result_status = NEW.result_status,
            result_json = NEW.result_json,
            result_completed_at = NEW.completed_at
        WHERE id = NEW.session_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE simulation_exam_sessions SET
            total_score = NULL,
            passing_score = NULL,
            policy_version = NULL,
            passed = NULL,
            result_status = NULL,
            result_json = NULL,
            result_completed_at = NULL
        WHERE id = OLD.session_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS simulation_exam_results_iot ON simulation_exam_results;
CREATE TRIGGER simulation_exam_results_iot INSTEAD OF INSERT OR UPDATE OR DELETE ON simulation_exam_results FOR EACH ROW EXECUTE FUNCTION trg_simulation_exam_results_io();

-- Compatibility Views for draft/review tables
CREATE OR REPLACE VIEW admin_preset_drafts AS
SELECT 
    gen_random_uuid() AS id,
    gen_random_uuid() AS logical_set_id,
    gen_random_uuid() AS source_published_set_id,
    ''::character varying AS code,
    ''::character varying AS title,
    ''::character varying AS license_class,
    ''::character varying AS bank_version,
    1 AS version_number,
    0 AS total_questions,
    0 AS duration_minutes,
    0 AS display_order,
    'ARCHIVED'::character varying AS status,
    1::bigint AS revision,
    NULL::bigint AS last_validated_revision,
    NULL::uuid AS created_by,
    NULL::uuid AS updated_by,
    now() AS created_at,
    now() AS updated_at
WHERE false;

CREATE OR REPLACE VIEW admin_preset_draft_questions AS
SELECT 
    gen_random_uuid() AS draft_id,
    0 AS question_id,
    0 AS question_order,
    now() AS created_at
WHERE false;

CREATE OR REPLACE VIEW admin_preset_draft_audit AS
SELECT 
    gen_random_uuid() AS id,
    gen_random_uuid() AS draft_id,
    ''::character varying AS action,
    1::bigint AS revision,
    '{}'::jsonb AS delta,
    NULL::uuid AS actor_id,
    now() AS performed_at
WHERE false;

CREATE OR REPLACE VIEW admin_preset_reviews AS
SELECT 
    gen_random_uuid() AS draft_id,
    ''::character varying AS state,
    1::bigint AS revision,
    ''::character varying AS fingerprint,
    NULL::uuid AS submitted_by,
    NULL::uuid AS approved_by,
    NULL::uuid AS published_version_id,
    NULL::text AS rejection_reason,
    now() AS created_at,
    now() AS updated_at
WHERE false;

CREATE OR REPLACE VIEW admin_preset_release_audit AS
SELECT 
    gen_random_uuid() AS id,
    gen_random_uuid() AS draft_id,
    gen_random_uuid() AS logical_set_id,
    gen_random_uuid() AS version_id,
    1 AS version_number,
    1::bigint AS revision,
    ''::character varying AS fingerprint,
    NULL::uuid AS actor_id,
    ''::character varying AS action,
    now() AS performed_at
WHERE false;

CREATE OR REPLACE VIEW admin_simulation_release_preparations AS
SELECT 
    gen_random_uuid() AS release_id,
    ''::character varying AS bank_version,
    ''::character varying AS snapshot_sha256,
    ''::character varying AS manifest_sha256,
    ''::character varying AS state,
    ''::text AS snapshot_json,
    now() AS prepared_at
WHERE false;
