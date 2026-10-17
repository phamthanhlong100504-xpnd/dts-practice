-- Screen 13. Progress receipt/outbox schemas and delivery are unchanged.
LOCK TABLE exams IN SHARE ROW EXCLUSIVE MODE;
ALTER TABLE exams ADD COLUMN history_target_id UUID;
ALTER TABLE exams ADD COLUMN history_attempt_no INTEGER CHECK (history_attempt_no > 0);

-- Stable target for random exams: a bank/license family, not an individual attempt.
UPDATE exams SET history_target_id=COALESCE(preset_exam_set_id,
    md5('dts-practice:random:'||COALESCE(bank_version,'legacy')||':'||exam_type)::uuid)
WHERE mode='EXAM';
WITH numbered AS (
    SELECT id,ROW_NUMBER() OVER (PARTITION BY user_id,history_target_id ORDER BY started_at,id)::integer AS n
    FROM exams WHERE mode='EXAM'
)
UPDATE exams e SET history_attempt_no=n.n FROM numbered n WHERE e.id=n.id;

CREATE TABLE practice_history_attempt_counters (
    user_id UUID NOT NULL, target_id UUID NOT NULL,
    last_attempt_no INTEGER NOT NULL CHECK(last_attempt_no > 0),
    PRIMARY KEY(user_id,target_id)
);
INSERT INTO practice_history_attempt_counters(user_id,target_id,last_attempt_no)
SELECT user_id,history_target_id,MAX(history_attempt_no) FROM exams
WHERE mode='EXAM' GROUP BY user_id,history_target_id;

CREATE FUNCTION dts_assign_practice_history_attempt() RETURNS trigger
LANGUAGE plpgsql AS $body$
BEGIN
    IF NEW.mode='EXAM' THEN
        NEW.history_target_id=COALESCE(NEW.preset_exam_set_id,
            md5('dts-practice:random:'||COALESCE(NEW.bank_version,'legacy')||':'||NEW.exam_type)::uuid);
        INSERT INTO practice_history_attempt_counters(user_id,target_id,last_attempt_no)
        VALUES(NEW.user_id,NEW.history_target_id,1)
        ON CONFLICT(user_id,target_id) DO UPDATE SET
            last_attempt_no=practice_history_attempt_counters.last_attempt_no+1
        RETURNING last_attempt_no INTO NEW.history_attempt_no;
    END IF;
    RETURN NEW;
END;
$body$;
CREATE TRIGGER trg_practice_history_attempt BEFORE INSERT ON exams
FOR EACH ROW EXECUTE FUNCTION dts_assign_practice_history_attempt();

CREATE TABLE practice_result_outbox (
    event_id UUID PRIMARY KEY, exam_id UUID NOT NULL UNIQUE, user_id UUID NOT NULL,
    payload JSONB NOT NULL CHECK(jsonb_typeof(payload)='object'),
    attempts INTEGER NOT NULL DEFAULT 0, next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ, last_error VARCHAR(100), created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_practice_result_outbox_pending ON practice_result_outbox(next_attempt_at,created_at,event_id)
WHERE published_at IS NULL;
CREATE TABLE practice_result_backfill_failures (
    exam_id UUID PRIMARY KEY, error_code VARCHAR(100) NOT NULL,
    failed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_practice_result_backfill_source ON exams(completed_at,id)
WHERE mode='EXAM' AND status IN ('COMPLETED','TIMEOUT');

-- Receipt is the authoritative snapshot when it exists. p_passed is accepted only
-- for historical exams without receipts, after ExamService revalidates its result.
CREATE FUNCTION dts_enqueue_practice_result(p_exam_id UUID,p_passed BOOLEAN DEFAULT NULL)
RETURNS BOOLEAN LANGUAGE plpgsql AS $body$
DECLARE e record; duration INTEGER; payload JSONB;
BEGIN
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

CREATE FUNCTION dts_queue_practice_result_receipt() RETURNS trigger
LANGUAGE plpgsql AS $body$
BEGIN
    BEGIN
        PERFORM dts_enqueue_practice_result(NEW.exam_id);
    EXCEPTION WHEN raise_exception OR check_violation THEN
        -- Invalid legacy history must not roll back an otherwise valid Progress receipt.
        INSERT INTO practice_result_backfill_failures(exam_id,error_code)
        VALUES(NEW.exam_id,'HISTORY_SNAPSHOT_INVALID')
        ON CONFLICT(exam_id) DO UPDATE SET error_code=EXCLUDED.error_code,failed_at=NOW();
    END;
    RETURN NEW;
END;
$body$;
-- Same source transaction; a rollback never leaves a result event queued.
CREATE TRIGGER trg_practice_result_receipt AFTER INSERT ON exam_progress_receipts
FOR EACH ROW WHEN(NEW.mode='EXAM') EXECUTE FUNCTION dts_queue_practice_result_receipt();
