-- Additive simulation evidence. No theory facts, scoring, old migrations or results are rewritten.
-- Current approved library is exactly the canonical120 situations. Card01 learning/bookmarks
-- remain user+bank_version shared across licenses; first-perfect credit uses one stable library ordinal.
LOCK TABLE simulation_attempts,simulation_bookmarks,simulation_exam_sessions,simulation_exam_results IN SHARE ROW EXCLUSIVE MODE;
CREATE TABLE simulation_performance_source (
 singleton BOOLEAN PRIMARY KEY DEFAULT TRUE CHECK(singleton),
 capture_started_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
 last_sequence BIGINT NOT NULL DEFAULT 0 CHECK(last_sequence>=0),
 policy_revision BIGINT NOT NULL DEFAULT 1 CHECK(policy_revision>0),
 exp_policy_version TEXT NOT NULL DEFAULT 'SIMULATION_EXP_V1',
 -- Seed pinned to ExamMatrix in this reviewed source. The worker reads that SAME Java policy
 -- and snapshots later changes for future events only, never recomputing historical N.
 question_counts JSONB NOT NULL DEFAULT '{"B":30,"C1":35,"C":40,"D1":45,"D2":45,"D":45,"BE":45,"C1E":45,"CE":45,"D1E":45,"D2E":45,"DE":45}'::jsonb,
 backfill_complete BOOLEAN NOT NULL DEFAULT FALSE,
 current_bank_version VARCHAR(96), current_catalog_sha256 CHAR(64)
);
INSERT INTO simulation_performance_source(singleton) VALUES(TRUE);
CREATE TABLE simulation_performance_outbox (
 event_sequence BIGINT PRIMARY KEY CHECK(event_sequence>0), event_id UUID NOT NULL UNIQUE,
 event_type TEXT NOT NULL, occurred_at TIMESTAMPTZ NOT NULL, payload JSONB NOT NULL,
 attempts INTEGER NOT NULL DEFAULT 0 CHECK(attempts>=0), next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 published_at TIMESTAMPTZ,last_error VARCHAR(160),created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);
CREATE INDEX simulation_performance_pending ON simulation_performance_outbox(event_sequence) WHERE published_at IS NULL;
CREATE TABLE simulation_performance_first_perfect (
 user_id UUID NOT NULL, achievement_scope TEXT NOT NULL DEFAULT 'DTS_SIMULATION_120_V1',
 scenario_number INTEGER NOT NULL CHECK(scenario_number BETWEEN 1 AND 120),
 source_attempt_id UUID NOT NULL UNIQUE, license_class VARCHAR(16) NOT NULL,
 achieved_at TIMESTAMPTZ NOT NULL, exp_eligible BOOLEAN NOT NULL,
 PRIMARY KEY(user_id,achievement_scope,scenario_number)
);
CREATE TABLE simulation_performance_catalogs (
 bank_version VARCHAR(96) PRIMARY KEY,manifest_sha256 CHAR(64) NOT NULL,
 catalog_sha256 CHAR(64) NOT NULL,catalog JSONB NOT NULL,observed_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);
CREATE FUNCTION dts_simulation_performance_guard() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF TG_TABLE_NAME='simulation_performance_outbox' AND TG_OP='UPDATE' AND
    ROW(NEW.event_sequence,NEW.event_id,NEW.event_type,NEW.occurred_at,NEW.payload,NEW.created_at)
    IS NOT DISTINCT FROM ROW(OLD.event_sequence,OLD.event_id,OLD.event_type,OLD.occurred_at,OLD.payload,OLD.created_at)
 THEN RETURN NEW; END IF;
 RAISE EXCEPTION 'Simulation performance source evidence is retained and immutable';
END $$;
CREATE TRIGGER simulation_performance_outbox_guard BEFORE UPDATE OR DELETE ON simulation_performance_outbox
 FOR EACH ROW EXECUTE FUNCTION dts_simulation_performance_guard();
CREATE TRIGGER simulation_performance_perfect_guard BEFORE UPDATE OR DELETE ON simulation_performance_first_perfect
 FOR EACH ROW EXECUTE FUNCTION dts_simulation_performance_guard();
CREATE TRIGGER simulation_performance_catalog_guard BEFORE UPDATE OR DELETE ON simulation_performance_catalogs
 FOR EACH ROW EXECUTE FUNCTION dts_simulation_performance_guard();

CREATE FUNCTION dts_simulation_emit(p_kind TEXT,p_at TIMESTAMPTZ,p_data JSONB) RETURNS BIGINT LANGUAGE plpgsql AS $$
DECLARE s simulation_performance_source%ROWTYPE; eid UUID; data JSONB;
BEGIN
 -- Transaction-scoped coordinator makes the source sequence commit ordered and gap-free,
 -- including bookmark deletions and rollback. This is not a video playback clock/credit.
 PERFORM pg_advisory_xact_lock(773504,1);
 UPDATE simulation_performance_source SET last_sequence=last_sequence+1 WHERE singleton RETURNING * INTO STRICT s;
 eid:=md5('dts-simulation-performance-v1:'||s.capture_started_at::text||':'||s.last_sequence::text)::uuid;
 data:=p_data||jsonb_build_object('captureStartedAt',s.capture_started_at);
 IF p_kind='SIMULATION_SYNC_MARKER' THEN data:=data||jsonb_build_object('throughSequence',s.last_sequence-1); END IF;
 INSERT INTO simulation_performance_outbox(event_sequence,event_id,event_type,occurred_at,payload)
 VALUES(s.last_sequence,eid,p_kind,p_at,jsonb_build_object('eventId',eid,'eventType',p_kind,'eventVersion',1,
  'eventSequence',s.last_sequence,'source','dts-practice','occurredAt',p_at,'data',data));
 RETURN s.last_sequence;
END $$;

CREATE FUNCTION dts_simulation_emit_exam(p_id UUID,p_live BOOLEAN) RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE s simulation_exam_sessions%ROWTYPE;r simulation_exam_results%ROWTYPE;p simulation_performance_source%ROWTYPE;n INTEGER;
BEGIN
 SELECT * INTO STRICT s FROM simulation_exam_sessions WHERE id=p_id AND status='COMPLETED';
 SELECT * INTO STRICT r FROM simulation_exam_results WHERE session_id=p_id;
 PERFORM pg_advisory_xact_lock(773504,1);
 SELECT * INTO STRICT p FROM simulation_performance_source WHERE singleton;
 n:=CASE WHEN p_live AND s.completed_at>=p.capture_started_at THEN (p.question_counts->>s.license_class)::integer ELSE NULL END;
 PERFORM dts_simulation_emit('SIMULATION_EXAM_COMPLETED',s.completed_at,jsonb_build_object(
  'userId',s.user_id,'licenseClass',s.license_class,'sourceType','SIMULATION_SESSION','sourceId',s.id,
  'activityType',CASE s.mode WHEN 'RANDOM' THEN 'SIM_RANDOM_EXAM' ELSE 'SIM_PRESET_EXAM' END,
  'mode',s.mode,'bankVersion',s.bank_version,'scoringVersion',s.scoring_version,'manifestSha256',s.manifest_sha256,
  'score',r.total_score,'maxScore',50,'result',r.result_status,'completedAt',s.completed_at,
  'startedAt',s.started_at,'durationSeconds',NULL,'scoringPolicyVersion',r.policy_version,'passingScore',r.passing_score,
  'presetSetId',s.preset_set_id,'presetVersionId',s.preset_version_id,
  'displayTitle',CASE s.mode WHEN 'PRESET' THEN s.preset_snapshot->>'title' ELSE 'Thi thử mô phỏng ngẫu nhiên' END,
  'expPolicyVersion',p.exp_policy_version,'expPolicyRevision',p.policy_revision,'nSnapshot',n,
  'expEligible',p_live AND s.completed_at>=p.capture_started_at AND n IS NOT NULL,
  'expExclusionReason',CASE WHEN NOT p_live OR s.completed_at<p.capture_started_at THEN 'HISTORICAL_BACKFILL' WHEN n IS NULL THEN 'SOURCE_POLICY_PENDING' ELSE NULL END));
END $$;
CREATE FUNCTION dts_simulation_emit_practice(p_id UUID,p_live BOOLEAN) RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE a simulation_attempts%ROWTYPE;p simulation_performance_source%ROWTYPE;first_claim BOOLEAN:=FALSE;inserted INTEGER;
BEGIN
 SELECT * INTO STRICT a FROM simulation_attempts WHERE id=p_id AND status='COMPLETED';
 PERFORM pg_advisory_xact_lock(773504,1);
 SELECT * INTO STRICT p FROM simulation_performance_source WHERE singleton;
 IF a.score=5 THEN
  INSERT INTO simulation_performance_first_perfect(user_id,scenario_number,source_attempt_id,license_class,achieved_at,exp_eligible)
  VALUES(a.user_id,a.scenario_number,a.id,a.license_class,a.completed_at,p_live AND a.completed_at>=p.capture_started_at)
  ON CONFLICT(user_id,achievement_scope,scenario_number) DO NOTHING;
  GET DIAGNOSTICS inserted=ROW_COUNT;first_claim:=inserted=1 AND p_live AND a.completed_at>=p.capture_started_at;
 END IF;
 PERFORM dts_simulation_emit('SIMULATION_PRACTICE_COMPLETED',a.completed_at,jsonb_build_object(
  'userId',a.user_id,'licenseClass',a.license_class,'sourceType','SIMULATION_ATTEMPT','sourceId',a.id,
  'bankVersion',a.bank_version,'scoringVersion',a.scoring_version,'scenarioId',a.scenario_id,
  'scenarioNumber',a.scenario_number,'chapterId',a.chapter_id,'score',a.score,'maxScore',5,
  'completedAt',a.completed_at,'achievementScope','DTS_SIMULATION_120_V1',
  'expPolicyVersion',p.exp_policy_version,'firstPerfectEligible',first_claim,
  'expExclusionReason',CASE WHEN NOT p_live OR a.completed_at<p.capture_started_at THEN 'HISTORICAL_BACKFILL' WHEN first_claim THEN NULL ELSE 'NOT_FIRST_PERFECT' END));
END $$;
CREATE FUNCTION dts_simulation_performance_trigger() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE b simulation_bookmarks%ROWTYPE;
BEGIN
 IF TG_TABLE_NAME='simulation_bookmarks' THEN
  IF TG_OP='DELETE' THEN b:=OLD;ELSE b:=NEW;END IF;
  PERFORM dts_simulation_emit('SIMULATION_BOOKMARK_CHANGED',clock_timestamp(),jsonb_build_object(
   'userId',b.user_id,'bankVersion',b.bank_version,'scenarioId',b.scenario_id,'marked',TG_OP<>'DELETE'));
  IF TG_OP='DELETE' THEN RETURN OLD;ELSE RETURN NEW;END IF;
 ELSIF TG_TABLE_NAME='simulation_attempts' THEN
  IF NEW.status='COMPLETED' AND (TG_OP='INSERT' OR OLD.status<>'COMPLETED') THEN PERFORM dts_simulation_emit_practice(NEW.id,TRUE);END IF;
 ELSIF TG_TABLE_NAME='simulation_exam_sessions' THEN
  IF NEW.status='COMPLETED' AND (TG_OP='INSERT' OR OLD.status<>'COMPLETED') THEN PERFORM dts_simulation_emit_exam(NEW.id,TRUE);END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER simulation_performance_attempt AFTER INSERT OR UPDATE ON simulation_attempts FOR EACH ROW EXECUTE FUNCTION dts_simulation_performance_trigger();
CREATE TRIGGER simulation_performance_exam AFTER INSERT OR UPDATE ON simulation_exam_sessions FOR EACH ROW EXECUTE FUNCTION dts_simulation_performance_trigger();
CREATE TRIGGER simulation_performance_bookmark AFTER INSERT OR DELETE ON simulation_bookmarks FOR EACH ROW EXECUTE FUNCTION dts_simulation_performance_trigger();

-- Existing immutable facts are exported once, without regrading or retroactive EXP.
-- First-perfect exclusions are seeded BEFORE replay, including all bank publications/licenses.
INSERT INTO simulation_performance_first_perfect(user_id,scenario_number,source_attempt_id,license_class,achieved_at,exp_eligible)
 SELECT DISTINCT ON(user_id,scenario_number) user_id,scenario_number,id,license_class,completed_at,FALSE
 FROM simulation_attempts WHERE status='COMPLETED' AND score=5 ORDER BY user_id,scenario_number,completed_at,id;
DO $$ DECLARE item RECORD;BEGIN
 PERFORM dts_simulation_emit('SIMULATION_SYNC_BEGIN',clock_timestamp(),jsonb_build_object('backfill',TRUE,'achievementScope','DTS_SIMULATION_120_V1'));
 FOR item IN SELECT id FROM simulation_exam_sessions WHERE status='COMPLETED' ORDER BY completed_at,id LOOP
  PERFORM dts_simulation_emit_exam(item.id,FALSE);
 END LOOP;
 FOR item IN SELECT id FROM simulation_attempts WHERE status='COMPLETED' ORDER BY completed_at,id LOOP
  PERFORM dts_simulation_emit_practice(item.id,FALSE);
 END LOOP;
 FOR item IN SELECT * FROM simulation_bookmarks ORDER BY user_id,bank_version,scenario_id LOOP
  PERFORM dts_simulation_emit('SIMULATION_BOOKMARK_CHANGED',item.updated_at,jsonb_build_object(
   'userId',item.user_id,'bankVersion',item.bank_version,'scenarioId',item.scenario_id,'marked',TRUE,'backfill',TRUE));
 END LOOP;
 UPDATE simulation_performance_source SET backfill_complete=TRUE WHERE singleton;
 PERFORM dts_simulation_emit('SIMULATION_SYNC_END',clock_timestamp(),jsonb_build_object('backfillComplete',TRUE));
END $$;
