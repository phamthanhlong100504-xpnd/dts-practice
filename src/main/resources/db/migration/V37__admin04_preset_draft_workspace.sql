-- BE04 G1: isolated fixed-exam editorial drafts. Existing published presets, exams and EXP are untouched.
CREATE TABLE admin_preset_drafts (
    id UUID PRIMARY KEY,
    logical_set_id UUID NOT NULL,
    source_published_set_id UUID REFERENCES preset_exam_sets(id) ON DELETE RESTRICT,
    code VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    license_class VARCHAR(10) NOT NULL,
    bank_version VARCHAR(64) NOT NULL,
    version_number INTEGER NOT NULL CHECK (version_number > 0),
    total_questions INTEGER NOT NULL CHECK (total_questions BETWEEN 1 AND 60),
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes BETWEEN 1 AND 120),
    display_order INTEGER NOT NULL CHECK (display_order > 0),
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' CHECK (status = 'DRAFT'),
    revision BIGINT NOT NULL DEFAULT 1 CHECK (revision > 0),
    last_validated_revision BIGINT,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_admin_preset_draft_version UNIQUE(logical_set_id, version_number),
    CONSTRAINT chk_admin_preset_validated_revision CHECK (
        last_validated_revision IS NULL OR last_validated_revision <= revision
    )
);
CREATE INDEX idx_admin_preset_drafts_list
    ON admin_preset_drafts(license_class, bank_version, updated_at DESC, id);

CREATE TABLE admin_preset_draft_questions (
    draft_id UUID NOT NULL REFERENCES admin_preset_drafts(id) ON DELETE CASCADE,
    question_order INTEGER NOT NULL CHECK(question_order > 0),
    question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE RESTRICT,
    source_question_id INTEGER NOT NULL CHECK(source_question_id > 0),
    PRIMARY KEY(draft_id, question_order),
    CONSTRAINT uq_admin_preset_draft_question UNIQUE(draft_id, question_id),
    CONSTRAINT uq_admin_preset_draft_source UNIQUE(draft_id, source_question_id)
);

CREATE TABLE admin_preset_draft_audit (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    draft_id UUID NOT NULL REFERENCES admin_preset_drafts(id) ON DELETE RESTRICT,
    actor_id UUID NOT NULL,
    action VARCHAR(40) NOT NULL,
    old_revision BIGINT,
    new_revision BIGINT NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_admin_preset_draft_audit ON admin_preset_draft_audit(draft_id, id);

CREATE FUNCTION adm04_preset_audit_immutable() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'ADM04 preset audit is append-only' USING ERRCODE='23514';
END $$;
CREATE TRIGGER trg_adm04_preset_audit_immutable
    BEFORE UPDATE OR DELETE ON admin_preset_draft_audit
    FOR EACH ROW EXECUTE FUNCTION adm04_preset_audit_immutable();

CREATE FUNCTION adm04_preset_draft_identity_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.id IS DISTINCT FROM OLD.id
       OR NEW.logical_set_id IS DISTINCT FROM OLD.logical_set_id
       OR NEW.source_published_set_id IS DISTINCT FROM OLD.source_published_set_id
       OR NEW.bank_version IS DISTINCT FROM OLD.bank_version
       OR NEW.license_class IS DISTINCT FROM OLD.license_class
       OR NEW.version_number IS DISTINCT FROM OLD.version_number
       OR NEW.created_by IS DISTINCT FROM OLD.created_by
       OR NEW.created_at IS DISTINCT FROM OLD.created_at
       OR NEW.status IS DISTINCT FROM 'DRAFT'
       OR NEW.revision <> OLD.revision + 1 THEN
        RAISE EXCEPTION 'ADM04 draft immutable identity/revision violation' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER trg_adm04_preset_draft_identity_guard
    BEFORE UPDATE ON admin_preset_drafts
    FOR EACH ROW EXECUTE FUNCTION adm04_preset_draft_identity_guard();

-- Existing learner preset rows are immutable. A future reviewed version must use a separate release migration.
CREATE FUNCTION adm04_protect_published_preset() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.status = 'PUBLISHED' THEN
        RAISE EXCEPTION 'Published preset is immutable; create a new reviewed version' USING ERRCODE='23514';
    END IF;
    RETURN COALESCE(NEW, OLD);
END $$;
CREATE TRIGGER trg_adm04_protect_published_preset
    BEFORE UPDATE OR DELETE ON preset_exam_sets
    FOR EACH ROW EXECUTE FUNCTION adm04_protect_published_preset();

CREATE FUNCTION adm04_protect_published_preset_questions() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE old_id UUID;
BEGIN
    old_id := CASE WHEN TG_OP = 'INSERT' THEN NEW.preset_exam_set_id ELSE OLD.preset_exam_set_id END;
    IF EXISTS(SELECT 1 FROM preset_exam_sets WHERE id=old_id AND status='PUBLISHED') THEN
        RAISE EXCEPTION 'Published preset questions are immutable' USING ERRCODE='23514';
    END IF;
    IF TG_OP = 'UPDATE' AND NEW.preset_exam_set_id IS DISTINCT FROM OLD.preset_exam_set_id THEN
        RAISE EXCEPTION 'Cannot move preset questions between sets' USING ERRCODE='23514';
    END IF;
    RETURN COALESCE(NEW, OLD);
END $$;
CREATE TRIGGER trg_adm04_protect_published_preset_questions
    BEFORE INSERT OR UPDATE OR DELETE ON preset_exam_set_questions
    FOR EACH ROW EXECUTE FUNCTION adm04_protect_published_preset_questions();
