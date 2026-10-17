-- Separate EXP evidence. No rewriting/backfilling of existing answers or Progress payloads.
CREATE TABLE practice_exp_capture_state (
 singleton BOOLEAN PRIMARY KEY DEFAULT TRUE CHECK(singleton),
 capture_started_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
 policy_revision BIGINT NOT NULL DEFAULT 0,
 policy_hash VARCHAR(64)
);
INSERT INTO practice_exp_capture_state(singleton) VALUES(TRUE);

CREATE TABLE practice_exp_outbox (
 event_id UUID PRIMARY KEY,
 event_type VARCHAR(40) NOT NULL CHECK(event_type IN
  ('QUESTION_CORRECT_RECORDED','PRACTICE_EXP_SOURCE_READY')),
 user_id UUID,
 payload JSONB NOT NULL,
 attempts INTEGER NOT NULL DEFAULT 0 CHECK(attempts>=0),
 next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 published_at TIMESTAMPTZ,
 last_error VARCHAR(200),
 created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
 CHECK((event_type='QUESTION_CORRECT_RECORDED' AND user_id IS NOT NULL)
  OR (event_type='PRACTICE_EXP_SOURCE_READY' AND user_id IS NULL))
);
CREATE INDEX ix_practice_exp_outbox_pending ON practice_exp_outbox(next_attempt_at,created_at,event_id)
 WHERE published_at IS NULL;

CREATE FUNCTION dts_record_correct_question(p_user UUID,p_license TEXT,p_bank TEXT,
 p_question INTEGER,p_at TIMESTAMPTZ,p_context TEXT,p_origin TEXT) RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE v_id UUID; v_target UUID; v_capture TIMESTAMPTZ;
BEGIN
 SELECT capture_started_at INTO STRICT v_capture FROM practice_exp_capture_state WHERE singleton;
 IF p_at IS NULL OR p_at<v_capture OR p_user IS NULL THEN RETURN; END IF;
 -- The fact is emitted only by a graded-answer trigger. Also prove the bank/license namespace.
 IF NOT EXISTS(SELECT 1 FROM questions q WHERE q.id=p_question AND q.bank_version=p_bank
   AND jsonb_exists(q.applicable_licenses,p_license)) THEN
  -- Preserve legacy answer flows, but do not credit unverified/obsolete namespaces.
  RETURN;
 END IF;
 v_id:=md5('dts-exp:'||p_context||':'||p_origin||':'||
   to_char(p_at AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US'))::uuid;
 v_target:=md5('dts-question:'||p_license||':'||p_bank||':'||p_question)::uuid;
 INSERT INTO practice_exp_outbox(event_id,event_type,user_id,payload)
 VALUES(v_id,'QUESTION_CORRECT_RECORDED',p_user,jsonb_build_object(
  'eventId',v_id,'eventType','QUESTION_CORRECT_RECORDED','eventVersion',1,
  'occurredAt',p_at,'userId',p_user,'sourceType','PRACTICE_QUESTION','sourceId',v_id,
  'targetType','QUESTION','targetId',v_target,'licenseClass',p_license,
  'metadata',jsonb_build_object('bankVersion',p_bank,'questionId',p_question,
   'context',p_context,'captureStartedAt',v_capture)))
 ON CONFLICT(event_id) DO NOTHING;
END;
$$;

CREATE FUNCTION dts_question_exp_trigger() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE v_user UUID; v_license TEXT; v_bank TEXT; v_context TEXT; v_origin TEXT; v_at TIMESTAMPTZ;
BEGIN
 IF NEW.is_correct IS DISTINCT FROM TRUE THEN RETURN NEW; END IF;
 IF TG_TABLE_NAME='chapter_question_progress' THEN
  v_user:=NEW.user_id;v_license:=NEW.license_class;v_bank:=NEW.bank_version;
  v_at:=NEW.last_answered_at;v_context:='CHAPTER_LEARNING';
  v_origin:=NEW.user_id::text||':'||NEW.license_class||':'||NEW.bank_version||':'||NEW.question_id;
 ELSIF TG_TABLE_NAME='exam_answers' THEN
  SELECT e.user_id,e.exam_type,e.bank_version INTO v_user,v_license,v_bank FROM exams e
   WHERE e.id=NEW.exam_id AND e.mode='PRACTICE';
  IF NOT FOUND THEN RETURN NEW; END IF;
  v_at:=NEW.answered_at;v_context:='PRACTICE_MODE';v_origin:=NEW.id::text;
 ELSIF TG_TABLE_NAME='chapter_review_session_answers' THEN
  SELECT s.user_id,s.license_class,s.bank_version INTO v_user,v_license,v_bank
   FROM chapter_review_sessions s WHERE s.id=NEW.session_id;
  v_at:=NEW.answered_at;v_context:='CHAPTER_REVIEW';v_origin:=NEW.session_id::text||':'||NEW.question_id;
 ELSIF TG_TABLE_NAME='critical_question_session_answers' THEN
  SELECT s.user_id,s.license_class,s.bank_version INTO v_user,v_license,v_bank
   FROM critical_question_sessions s WHERE s.id=NEW.session_id;
  v_at:=NEW.answered_at;v_context:='CRITICAL_PRACTICE';v_origin:=NEW.session_id::text||':'||NEW.question_id;
 ELSIF TG_TABLE_NAME='wrong_question_review_answers' THEN
  SELECT s.user_id,s.license_class,s.bank_version INTO v_user,v_license,v_bank
   FROM wrong_question_review_sessions s WHERE s.id=NEW.session_id;
  v_at:=NEW.answered_at;v_context:='WRONG_REVIEW';v_origin:=NEW.session_id::text||':'||NEW.question_id;
 ELSE RAISE EXCEPTION 'Unsupported graded-answer source'; END IF;
 IF TG_OP='UPDATE' AND to_jsonb(OLD)->>'selected_answer' IS NOT DISTINCT FROM to_jsonb(NEW)->>'selected_answer'
  AND (to_jsonb(OLD)->>'is_correct')::boolean IS TRUE
  AND COALESCE(to_jsonb(OLD)->>'answered_at',to_jsonb(OLD)->>'last_answered_at')
    IS NOT DISTINCT FROM COALESCE(to_jsonb(NEW)->>'answered_at',to_jsonb(NEW)->>'last_answered_at') THEN RETURN NEW; END IF;
 PERFORM dts_record_correct_question(v_user,v_license,v_bank,NEW.question_id,v_at,v_context,v_origin);
 RETURN NEW;
END;
$$;

CREATE TRIGGER trg_chapter_correct_exp AFTER INSERT OR UPDATE ON chapter_question_progress
 FOR EACH ROW EXECUTE FUNCTION dts_question_exp_trigger();
CREATE TRIGGER trg_practice_mode_correct_exp AFTER INSERT OR UPDATE ON exam_answers
 FOR EACH ROW EXECUTE FUNCTION dts_question_exp_trigger();
CREATE TRIGGER trg_chapter_review_correct_exp AFTER INSERT OR UPDATE ON chapter_review_session_answers
 FOR EACH ROW EXECUTE FUNCTION dts_question_exp_trigger();
CREATE TRIGGER trg_critical_correct_exp AFTER INSERT OR UPDATE ON critical_question_session_answers
 FOR EACH ROW EXECUTE FUNCTION dts_question_exp_trigger();
CREATE TRIGGER trg_wrong_review_correct_exp AFTER INSERT OR UPDATE ON wrong_question_review_answers
 FOR EACH ROW EXECUTE FUNCTION dts_question_exp_trigger();
