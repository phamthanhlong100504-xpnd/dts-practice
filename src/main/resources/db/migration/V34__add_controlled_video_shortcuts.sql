-- Refuse an unreviewed manually changed enqueue function before installing this migration.
DO $shortcut_outbox_guard$
BEGIN
    IF (SELECT md5(replace(p.prosrc,E'\r\n',E'\n')) FROM pg_proc p
            WHERE p.oid='public.dts_enqueue_practice_result(uuid,boolean)'::regprocedure)
            IS DISTINCT FROM '85d219533c3ac083f64362934a04c67c' THEN
        RAISE EXCEPTION 'Existing practice result enqueue function differs from reviewed V28; stop';
    END IF;
END;
$shortcut_outbox_guard$;

-- Explicit opt-in playback protocol. Legacy sessions/results/source snapshots are not rewritten.
ALTER TABLE simulation_exam_positions ADD COLUMN segment_start_ms integer NOT NULL DEFAULT 0;
ALTER TABLE simulation_exam_positions ADD CONSTRAINT simulation_exam_checkpoint_bounds
    CHECK(segment_start_ms BETWEEN 0 AND duration_ms AND last_media_time_ms>=segment_start_ms);
CREATE TABLE simulation_video_shortcut_state (
    target_kind varchar(8) NOT NULL CHECK(target_kind IN ('LEARNING','EXAM')),
    target_id uuid NOT NULL,
    user_id uuid NOT NULL,
    attempt_id uuid UNIQUE REFERENCES simulation_attempts(id),
    session_id uuid UNIQUE REFERENCES simulation_exam_sessions(id),
    protocol_version varchar(32) NOT NULL CHECK(protocol_version='video-shortcuts-v1'),
    secret_key uuid NOT NULL,
    revision bigint NOT NULL CHECK(revision>0),
    skipped_positions jsonb NOT NULL DEFAULT '[]'::jsonb CHECK(jsonb_typeof(skipped_positions)='array'),
    completion_reasons jsonb NOT NULL DEFAULT '{}'::jsonb CHECK(jsonb_typeof(completion_reasons)='object'),
    enabled_at timestamptz NOT NULL,
    PRIMARY KEY(target_kind,target_id),
    CHECK((target_kind='LEARNING' AND attempt_id IS NOT NULL AND attempt_id=target_id AND session_id IS NULL)
       OR (target_kind='EXAM' AND session_id IS NOT NULL AND session_id=target_id AND attempt_id IS NULL))
);
CREATE TABLE simulation_video_shortcut_events (
    user_id uuid NOT NULL,event_id uuid NOT NULL,
    target_kind varchar(8) NOT NULL,target_id uuid NOT NULL,
    operation varchar(24) NOT NULL CHECK(operation IN ('ENABLE','SKIP_INTRO','ADVANCE_AFTER_REACTION')),
    request_sha256 char(64) NOT NULL CHECK(request_sha256 ~ '^[0-9a-f]{64}$'),
    receipt_json jsonb NOT NULL CHECK(jsonb_typeof(receipt_json)='object'),
    created_at timestamptz NOT NULL,
    PRIMARY KEY(user_id,event_id),
    FOREIGN KEY(target_kind,target_id) REFERENCES simulation_video_shortcut_state(target_kind,target_id)
);

-- Screen13 concurrency regression found during full-suite validation.
-- Only the function definition changes; no historical rows/events are rewritten.
CREATE OR REPLACE FUNCTION dts_enqueue_practice_result(p_exam_id UUID,p_passed BOOLEAN DEFAULT NULL)
RETURNS BOOLEAN LANGUAGE plpgsql AS $body$
DECLARE e record; duration INTEGER; payload JSONB;
BEGIN
    -- Serialize by source exam before checking either unique outbox key.
    -- Receipt-trigger and historical backfill calls share this same row lock.
    PERFORM 1 FROM exams WHERE id=p_exam_id FOR UPDATE;
    SELECT x.*,r.exam_id AS receipt_id,r.score AS receipt_score,r.passed AS receipt_passed,
        r.correct_count AS receipt_correct,r.total_questions AS receipt_total,
        r.duration_seconds AS receipt_duration,r.started_at AS receipt_started,
        r.completed_at AS receipt_completed,s.title AS preset_title
    INTO e FROM exams x LEFT JOIN exam_progress_receipts r
        ON r.exam_id=x.id AND r.user_id=x.user_id
        LEFT JOIN preset_exam_sets s ON s.id=x.preset_exam_set_id
    WHERE x.id=p_exam_id;
    IF NOT FOUND OR e.mode<>'EXAM' THEN RETURN FALSE; END IF;
    IF EXISTS(SELECT 1 FROM practice_result_outbox WHERE exam_id=p_exam_id) THEN RETURN FALSE; END IF;
    IF e.receipt_id IS NULL AND (e.status NOT IN ('COMPLETED','TIMEOUT') OR p_passed IS NULL) THEN
        RAISE EXCEPTION 'Completed source grading is required';
    END IF;
    IF e.history_target_id IS NULL OR e.history_attempt_no IS NULL
        OR COALESCE(e.receipt_completed,e.completed_at) IS NULL
        OR COALESCE(e.receipt_started,e.started_at) IS NULL
        OR COALESCE(e.receipt_score,e.score) IS NULL
        OR COALESCE(e.receipt_correct,e.correct_count) IS NULL
        OR COALESCE(e.receipt_total,e.total_questions,0)<1 THEN
        RAISE EXCEPTION 'Historical exam snapshot is incomplete';
    END IF;
    IF COALESCE(e.receipt_score,e.score) NOT BETWEEN 0 AND 100
        OR COALESCE(e.receipt_correct,e.correct_count)<0
        OR COALESCE(e.receipt_correct,e.correct_count)>COALESCE(e.receipt_total,e.total_questions)
        OR COALESCE(e.receipt_completed,e.completed_at)<COALESCE(e.receipt_started,e.started_at) THEN
        RAISE EXCEPTION 'Historical exam snapshot is inconsistent';
    END IF;
    duration=e.receipt_duration;
    IF duration IS NULL THEN
        IF e.expires_at IS NOT NULL AND e.duration_minutes IS NOT NULL THEN
            duration=LEAST(e.duration_minutes*60,GREATEST(0,e.duration_minutes*60
                -GREATEST(0,EXTRACT(EPOCH FROM e.expires_at-e.completed_at))))::integer;
        ELSE
            duration=GREATEST(0,EXTRACT(EPOCH FROM e.completed_at-e.started_at))::integer;
        END IF;
    END IF;
    payload=jsonb_build_object(
        'eventId',e.id,'eventType','LEARNING_RESULT_CREATED','eventVersion',2,
        'occurredAt',COALESCE(e.receipt_completed,e.completed_at),'userId',e.user_id,
        'sourceType','PRACTICE_SESSION','sourceId',e.id,'targetType','EXAM','targetId',e.history_target_id,
        'attemptNo',e.history_attempt_no,
        'activityType',CASE WHEN e.preset_exam_set_id IS NULL THEN 'RANDOM_EXAM' ELSE 'PRESET_EXAM' END,
        'displayTitle',CASE WHEN e.preset_exam_set_id IS NULL THEN 'Thi thử ngẫu nhiên - '||e.exam_type ELSE e.preset_title END,
        'licenseClass',e.exam_type,'result',CASE WHEN COALESCE(e.receipt_passed,p_passed) THEN 'PASSED' ELSE 'FAILED' END,
        'score',COALESCE(e.receipt_score,e.score),'maxScore',100,'progress',100.0,
        'correctCount',COALESCE(e.receipt_correct,e.correct_count),'questionCount',COALESCE(e.receipt_total,e.total_questions),
        'durationSeconds',duration,'startedAt',COALESCE(e.receipt_started,e.started_at),
        'completedAt',COALESCE(e.receipt_completed,e.completed_at),'resultAvailable',TRUE,'reviewAvailable',TRUE,
        'resultSnapshot',jsonb_strip_nulls(jsonb_build_object(
            'totalQuestions',COALESCE(e.receipt_total,e.total_questions),'correctCount',COALESCE(e.receipt_correct,e.correct_count),
            'licenseClass',e.exam_type,'presetExamSetId',e.preset_exam_set_id,'bankVersion',e.bank_version)),
        'metadata',jsonb_strip_nulls(jsonb_build_object('presetExamSetId',e.preset_exam_set_id,
            'bankVersion',e.bank_version,'mode',e.mode,'legacySnapshot',e.receipt_id IS NULL)));
    INSERT INTO practice_result_outbox(event_id,exam_id,user_id,payload)
    VALUES(e.id,e.id,e.user_id,payload) ON CONFLICT(exam_id) DO NOTHING;
    RETURN FOUND;
END;
$body$;
