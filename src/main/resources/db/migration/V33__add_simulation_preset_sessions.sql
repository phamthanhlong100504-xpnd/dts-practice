-- Add PRESET references to the existing Card05 engine. No theory/learning rows are rewritten.
ALTER TABLE simulation_exam_sessions DROP CONSTRAINT simulation_exam_sessions_mode_check;
ALTER TABLE simulation_exam_sessions ADD CONSTRAINT simulation_exam_sessions_mode_check CHECK(mode IN ('RANDOM','PRESET'));
ALTER TABLE simulation_exam_sessions ADD COLUMN preset_set_id UUID;
ALTER TABLE simulation_exam_sessions ADD COLUMN preset_version_id UUID;
ALTER TABLE simulation_exam_sessions ADD COLUMN preset_snapshot JSONB;
ALTER TABLE simulation_exam_sessions ADD CONSTRAINT simulation_exam_preset_scope_check CHECK(
 (mode='RANDOM' AND preset_set_id IS NULL AND preset_version_id IS NULL AND preset_snapshot IS NULL)
 OR (mode='PRESET' AND preset_set_id IS NOT NULL AND preset_version_id IS NOT NULL AND preset_snapshot IS NOT NULL
     AND jsonb_typeof(preset_snapshot)='object' AND passing_score=35
     AND (preset_snapshot->>'presetSetId') IS NOT NULL AND (preset_snapshot->>'presetSetId')=preset_set_id::text
     AND (preset_snapshot->>'presetVersionId') IS NOT NULL AND (preset_snapshot->>'presetVersionId')=preset_version_id::text
     AND (preset_snapshot->>'title') IS NOT NULL AND length(preset_snapshot->>'title') BETWEEN 1 AND 200 AND length(btrim(preset_snapshot->>'title'))>0
     AND (preset_snapshot->>'manifestSha256') IS NOT NULL AND (preset_snapshot->>'manifestSha256') ~ '^[a-f0-9]{64}$'
     AND (preset_snapshot->>'sourceSha256') IS NOT NULL AND (preset_snapshot->>'sourceSha256') ~ '^[a-f0-9]{64}$'
     AND (preset_snapshot->>'sourceVersion') IS NOT NULL AND (preset_snapshot->>'sourceVersion') ~ '^[A-Za-z0-9][A-Za-z0-9._-]{0,95}$'
     AND (preset_snapshot->>'code') IS NOT NULL AND (preset_snapshot->>'code') ~ '^SIM-SET-(0[1-9]|1[0-5])$'
     AND (preset_snapshot->>'displayOrder') IS NOT NULL AND (preset_snapshot->>'displayOrder')::integer BETWEEN 1 AND 15));
CREATE INDEX ix_simulation_preset_latest_completed ON simulation_exam_sessions(user_id,preset_version_id,completed_at DESC,id DESC)
 WHERE mode='PRESET' AND status='COMPLETED';

CREATE FUNCTION simulation_preset_guard_reference() RETURNS TRIGGER LANGUAGE plpgsql AS $preset$
BEGIN
 IF ROW(NEW.preset_set_id,NEW.preset_version_id,NEW.preset_snapshot)
 IS DISTINCT FROM ROW(OLD.preset_set_id,OLD.preset_version_id,OLD.preset_snapshot)
 THEN RAISE EXCEPTION 'Simulation preset reference is immutable'; END IF;
 RETURN NEW;
END;
$preset$;
CREATE TRIGGER simulation_preset_guard_reference BEFORE UPDATE ON simulation_exam_sessions FOR EACH ROW EXECUTE FUNCTION simulation_preset_guard_reference();
