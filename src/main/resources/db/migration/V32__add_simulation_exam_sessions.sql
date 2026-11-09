-- Card05 mock exams are isolated from theory and chapter-learning progress.
CREATE TABLE simulation_exam_sessions (
 id UUID PRIMARY KEY, user_id UUID NOT NULL, mode VARCHAR(16) NOT NULL CHECK(mode='RANDOM'),
 license_class VARCHAR(16) NOT NULL, bank_version VARCHAR(96) NOT NULL, scoring_version VARCHAR(96) NOT NULL,
 manifest_sha256 CHAR(64) NOT NULL, policy_version VARCHAR(96) NOT NULL, passing_score INTEGER NOT NULL CHECK(passing_score BETWEEN 0 AND 50),
 status VARCHAR(24) NOT NULL CHECK(status IN ('IN_PROGRESS','READY_TO_SUBMIT','COMPLETED','CANCELLED')),
 completed_count INTEGER NOT NULL DEFAULT 0 CHECK(completed_count BETWEEN 0 AND 10), current_position INTEGER NOT NULL DEFAULT 1 CHECK(current_position BETWEEN 1 AND 10),
 create_key UUID NOT NULL, create_sha256 CHAR(64) NOT NULL, created_at TIMESTAMPTZ NOT NULL, started_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, completed_at TIMESTAMPTZ, cancelled_at TIMESTAMPTZ,
 UNIQUE(user_id,create_key), CHECK(manifest_sha256 ~ '^[a-f0-9]{64}$' AND create_sha256 ~ '^[a-f0-9]{64}$'),
 CHECK((status='IN_PROGRESS' AND completed_count<10 AND current_position=completed_count+1)
    OR (status IN ('READY_TO_SUBMIT','COMPLETED') AND completed_count=10 AND current_position=10) OR status='CANCELLED'),
 CHECK((status='COMPLETED')=(completed_at IS NOT NULL)), CHECK((status='CANCELLED')=(cancelled_at IS NOT NULL))
);
CREATE UNIQUE INDEX uq_simulation_exam_one_active ON simulation_exam_sessions(user_id) WHERE status IN ('IN_PROGRESS','READY_TO_SUBMIT');
CREATE TABLE simulation_exam_positions (
 session_id UUID NOT NULL REFERENCES simulation_exam_sessions(id), position INTEGER NOT NULL CHECK(position BETWEEN 1 AND 10),
 scenario_id UUID NOT NULL, chapter_name TEXT NOT NULL, duration_ms INTEGER NOT NULL CHECK(duration_ms>0),
 scenario_snapshot JSONB NOT NULL CHECK(jsonb_typeof(scenario_snapshot)='object'), object_etag VARCHAR(128) NOT NULL,
 playback_event_id UUID NOT NULL, playback_state VARCHAR(24) NOT NULL CHECK(playback_state IN ('NOT_STARTED','READY','PLAYING','INTERRUPTED','FINISHED')),
 granted_at TIMESTAMPTZ, segment_started_at TIMESTAMPTZ, last_observed_at TIMESTAMPTZ,
 last_media_time_ms INTEGER NOT NULL DEFAULT 0 CHECK(last_media_time_ms>=0 AND last_media_time_ms<=duration_ms),
 first_response_ms INTEGER CHECK(first_response_ms>=0 AND first_response_ms<=duration_ms), first_response_event_id UUID,
 is_completed BOOLEAN NOT NULL DEFAULT FALSE, score INTEGER CHECK(score BETWEEN 0 AND 5), completed_at TIMESTAMPTZ,
 PRIMARY KEY(session_id,position), UNIQUE(session_id,scenario_id),
 CHECK((first_response_ms IS NULL)=(first_response_event_id IS NULL)),
 CHECK(is_completed=(score IS NOT NULL) AND is_completed=(completed_at IS NOT NULL)),
 CHECK((playback_state='FINISHED')=is_completed)
);
CREATE TABLE simulation_exam_events (
 user_id UUID NOT NULL,event_id UUID NOT NULL,session_id UUID NOT NULL REFERENCES simulation_exam_sessions(id),
 kind VARCHAR(32) NOT NULL,request_sha256 CHAR(64) NOT NULL,response_json JSONB NOT NULL CHECK(jsonb_typeof(response_json)='object'),created_at TIMESTAMPTZ NOT NULL,
 PRIMARY KEY(user_id,event_id), CHECK(request_sha256 ~ '^[a-f0-9]{64}$')
);
CREATE TABLE simulation_exam_results (
 session_id UUID PRIMARY KEY REFERENCES simulation_exam_sessions(id), total_score INTEGER NOT NULL CHECK(total_score BETWEEN 0 AND 50),
 passing_score INTEGER NOT NULL CHECK(passing_score BETWEEN 0 AND 50), policy_version VARCHAR(96) NOT NULL,
 passed BOOLEAN NOT NULL, result_status VARCHAR(8) NOT NULL CHECK(result_status IN ('PASSED','FAILED')),
 result_json JSONB NOT NULL CHECK(jsonb_typeof(result_json)='object'),completed_at TIMESTAMPTZ NOT NULL,
 CHECK(passed=(total_score>=passing_score)),CHECK(result_status=CASE WHEN passed THEN 'PASSED' ELSE 'FAILED' END)
);
CREATE FUNCTION simulation_exam_guard_session() RETURNS TRIGGER LANGUAGE plpgsql AS $exam$
BEGIN
 IF TG_OP='DELETE' THEN RAISE EXCEPTION 'Simulation exam audit is retained'; END IF;
 IF OLD.status IN ('COMPLETED','CANCELLED') AND NEW IS DISTINCT FROM OLD THEN RAISE EXCEPTION 'Terminal simulation exam is immutable'; END IF;
 IF ROW(NEW.id,NEW.user_id,NEW.mode,NEW.license_class,NEW.bank_version,NEW.scoring_version,NEW.manifest_sha256,NEW.policy_version,NEW.passing_score,NEW.create_key,NEW.create_sha256,NEW.created_at,NEW.started_at)
 IS DISTINCT FROM ROW(OLD.id,OLD.user_id,OLD.mode,OLD.license_class,OLD.bank_version,OLD.scoring_version,OLD.manifest_sha256,OLD.policy_version,OLD.passing_score,OLD.create_key,OLD.create_sha256,OLD.created_at,OLD.started_at)
 THEN RAISE EXCEPTION 'Simulation exam snapshot is immutable'; END IF;
 RETURN NEW;
END;
$exam$;
CREATE TRIGGER simulation_exam_guard_session BEFORE UPDATE OR DELETE ON simulation_exam_sessions FOR EACH ROW EXECUTE FUNCTION simulation_exam_guard_session();
CREATE FUNCTION simulation_exam_guard_position() RETURNS TRIGGER LANGUAGE plpgsql AS $exam$
DECLARE parent_status TEXT;parent_position INTEGER;
BEGIN
 IF TG_OP='DELETE' THEN RAISE EXCEPTION 'Simulation exam position audit is retained'; END IF;
 SELECT status,current_position INTO parent_status,parent_position FROM simulation_exam_sessions WHERE id=OLD.session_id;
 IF parent_status<>'IN_PROGRESS' OR OLD.position<>parent_position OR OLD.is_completed THEN RAISE EXCEPTION 'Simulation exam position is frozen'; END IF;
 IF ROW(NEW.session_id,NEW.position,NEW.scenario_id,NEW.chapter_name,NEW.duration_ms,NEW.scenario_snapshot,NEW.object_etag)
 IS DISTINCT FROM ROW(OLD.session_id,OLD.position,OLD.scenario_id,OLD.chapter_name,OLD.duration_ms,OLD.scenario_snapshot,OLD.object_etag)
 THEN RAISE EXCEPTION 'Simulation exam position snapshot is immutable'; END IF;
 IF OLD.first_response_ms IS NOT NULL AND ROW(NEW.first_response_ms,NEW.first_response_event_id) IS DISTINCT FROM ROW(OLD.first_response_ms,OLD.first_response_event_id)
 THEN RAISE EXCEPTION 'First simulation exam reaction is immutable'; END IF;
 RETURN NEW;
END;
$exam$;
CREATE TRIGGER simulation_exam_guard_position BEFORE UPDATE OR DELETE ON simulation_exam_positions FOR EACH ROW EXECUTE FUNCTION simulation_exam_guard_position();
CREATE FUNCTION simulation_exam_guard_audit() RETURNS TRIGGER LANGUAGE plpgsql AS $exam$
BEGIN RAISE EXCEPTION 'Simulation exam result and event receipts are immutable'; END;
$exam$;
CREATE TRIGGER simulation_exam_guard_result BEFORE UPDATE OR DELETE ON simulation_exam_results FOR EACH ROW EXECUTE FUNCTION simulation_exam_guard_audit();
CREATE TRIGGER simulation_exam_guard_event BEFORE UPDATE OR DELETE ON simulation_exam_events FOR EACH ROW EXECUTE FUNCTION simulation_exam_guard_audit();
