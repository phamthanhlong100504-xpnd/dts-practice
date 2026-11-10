-- Controlled Practice preset publication. No learner attempts, history, scores or EXP are rewritten.
-- The legacy preset ID remains the stable logical ID for all existing published sets.
ALTER TABLE preset_exam_sets ADD COLUMN logical_set_id UUID;
-- V37 protects existing published rows. Temporarily suspend only that metadata guard
-- inside the atomic Flyway transaction to backfill NEW identity columns; no historical
-- business column, question mapping, score or session is altered.
ALTER TABLE preset_exam_sets DISABLE TRIGGER trg_adm04_protect_published_preset;
UPDATE preset_exam_sets SET logical_set_id=id;
ALTER TABLE preset_exam_sets ENABLE TRIGGER trg_adm04_protect_published_preset;
ALTER TABLE preset_exam_sets ALTER COLUMN logical_set_id SET NOT NULL;
ALTER TABLE preset_exam_sets ADD COLUMN version_number INTEGER NOT NULL DEFAULT 1
    CHECK (version_number>0);
ALTER TABLE preset_exam_sets ADD COLUMN source_version_id UUID
    REFERENCES preset_exam_sets(id) ON DELETE RESTRICT;
ALTER TABLE preset_exam_sets DROP CONSTRAINT uq_preset_exam_sets_code;
ALTER TABLE preset_exam_sets ADD CONSTRAINT uq_preset_sets_code_version
    UNIQUE (license_class,bank_version,code,version_number);
ALTER TABLE preset_exam_sets ADD CONSTRAINT uq_preset_sets_logical_version
    UNIQUE (logical_set_id,version_number);
CREATE INDEX ix_preset_sets_logical ON preset_exam_sets(logical_set_id,version_number DESC);

-- The pointer is the sole authority for new learner sessions and learner catalog.
-- Inactive historical PUBLISHED rows are retained byte-for-byte for existing sessions.
CREATE TABLE admin_preset_active_versions (
    logical_set_id UUID PRIMARY KEY,
    active_version_id UUID REFERENCES preset_exam_sets(id) ON DELETE RESTRICT,
    revision BIGINT NOT NULL DEFAULT 1 CHECK(revision>0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by UUID
);
CREATE UNIQUE INDEX uq_active_preset_version ON admin_preset_active_versions(active_version_id)
    WHERE active_version_id IS NOT NULL;
INSERT INTO admin_preset_active_versions(logical_set_id,active_version_id)
SELECT id,id FROM preset_exam_sets WHERE status='PUBLISHED';

CREATE FUNCTION guard_preset_active_pointer() RETURNS TRIGGER LANGUAGE plpgsql AS $body$
DECLARE owner_id UUID; state TEXT;
BEGIN
  IF NEW.active_version_id IS NOT NULL THEN
     SELECT logical_set_id,status INTO owner_id,state FROM preset_exam_sets WHERE id=NEW.active_version_id;
     IF owner_id IS DISTINCT FROM NEW.logical_set_id OR state IS DISTINCT FROM 'PUBLISHED' THEN
         RAISE EXCEPTION 'Active preset must be a published version of the same logical set' USING ERRCODE='23514';
     END IF;
  END IF;
  RETURN NEW;
END;
$body$;
CREATE TRIGGER trg_guard_preset_active_pointer BEFORE INSERT OR UPDATE
    ON admin_preset_active_versions FOR EACH ROW EXECUTE FUNCTION guard_preset_active_pointer();

-- Approval state stays separate from legacy DRAFT-only editorial table.
CREATE TABLE admin_preset_reviews (
  draft_id UUID PRIMARY KEY REFERENCES admin_preset_drafts(id) ON DELETE RESTRICT,
  state VARCHAR(16) NOT NULL CHECK(state IN ('IN_REVIEW','APPROVED','REJECTED','PUBLISHED')),
  draft_revision BIGINT NOT NULL CHECK(draft_revision>0),
  fingerprint CHAR(64) NOT NULL CHECK(fingerprint ~ '^[0-9a-f]{64}$'),
  submitted_by UUID NOT NULL,
  submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  approved_by UUID,
  approved_at TIMESTAMPTZ,
  rejected_by UUID,
  rejected_at TIMESTAMPTZ,
  rejection_reason TEXT,
  published_version_id UUID REFERENCES preset_exam_sets(id) ON DELETE RESTRICT,
  CHECK ((state IN ('APPROVED','PUBLISHED') AND approved_by IS NOT NULL AND approved_at IS NOT NULL)
    OR state IN ('IN_REVIEW','REJECTED'))
);
CREATE INDEX ix_admin_preset_review_state ON admin_preset_reviews(state,submitted_at);
CREATE TABLE admin_preset_release_audit (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  draft_id UUID REFERENCES admin_preset_drafts(id) ON DELETE RESTRICT,
  version_id UUID REFERENCES preset_exam_sets(id) ON DELETE RESTRICT,
  logical_set_id UUID NOT NULL,
  actor_id UUID NOT NULL,
  action VARCHAR(24) NOT NULL CHECK(action IN ('SUBMIT','APPROVE','REJECT','PUBLISH','ARCHIVE')),
  snapshot_sha256 CHAR(64),
  reason TEXT,
  occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_release_audit_subject CHECK(draft_id IS NOT NULL OR version_id IS NOT NULL)
);
CREATE TRIGGER trg_preset_release_audit_immutable BEFORE UPDATE OR DELETE
    ON admin_preset_release_audit FOR EACH ROW EXECUTE FUNCTION adm04_preset_audit_immutable();

CREATE FUNCTION freeze_reviewed_preset_draft() RETURNS trigger LANGUAGE plpgsql AS $body$
BEGIN
  IF EXISTS(SELECT 1 FROM admin_preset_reviews r WHERE r.draft_id=OLD.id
      AND r.state IN ('IN_REVIEW','APPROVED','PUBLISHED')) THEN
      RAISE EXCEPTION 'Preset draft in review or published is immutable' USING ERRCODE='23514';
  END IF;
  RETURN NEW;
END;
$body$;
CREATE TRIGGER trg_freeze_reviewed_preset_draft BEFORE UPDATE OR DELETE
    ON admin_preset_drafts FOR EACH ROW EXECUTE FUNCTION freeze_reviewed_preset_draft();
CREATE FUNCTION freeze_reviewed_preset_questions() RETURNS trigger LANGUAGE plpgsql AS $body$
DECLARE draft_uuid UUID;
BEGIN
  draft_uuid := CASE WHEN TG_OP='DELETE' THEN OLD.draft_id ELSE NEW.draft_id END;
  IF EXISTS(SELECT 1 FROM admin_preset_reviews WHERE draft_id=draft_uuid
      AND state IN ('IN_REVIEW','APPROVED','PUBLISHED')) THEN
      RAISE EXCEPTION 'Questions of a reviewed preset are immutable' USING ERRCODE='23514';
  END IF;
  RETURN COALESCE(NEW,OLD);
END;
$body$;
CREATE TRIGGER trg_freeze_reviewed_preset_questions BEFORE INSERT OR UPDATE OR DELETE
    ON admin_preset_draft_questions FOR EACH ROW EXECUTE FUNCTION freeze_reviewed_preset_questions();

-- Version ID remains on exams.preset_exam_set_id for replay. Only the NEW event/history
-- target uses logical_set_id, which is the original ID for all existing published presets.
CREATE OR REPLACE FUNCTION dts_assign_practice_history_attempt() RETURNS trigger
LANGUAGE plpgsql AS $body$
DECLARE stable_target UUID;
BEGIN
  IF NEW.mode='EXAM' THEN
    IF NEW.preset_exam_set_id IS NOT NULL THEN
      SELECT logical_set_id INTO stable_target FROM preset_exam_sets WHERE id=NEW.preset_exam_set_id;
      IF stable_target IS NULL THEN RAISE EXCEPTION 'Unknown preset logical ID'; END IF;
    ELSE
      stable_target := md5('dts-practice:random:'||COALESCE(NEW.bank_version,'legacy')||':'||NEW.exam_type)::uuid;
    END IF;
    NEW.history_target_id=stable_target;
    INSERT INTO practice_history_attempt_counters(user_id,target_id,last_attempt_no)
    VALUES(NEW.user_id,stable_target,1)
    ON CONFLICT(user_id,target_id) DO UPDATE SET
      last_attempt_no=practice_history_attempt_counters.last_attempt_no+1
    RETURNING last_attempt_no INTO NEW.history_attempt_no;
  END IF;
  RETURN NEW;
END;
$body$;

-- No result outbox payload shape, Result credit SQL, EXP coefficients or legacy rows change.
