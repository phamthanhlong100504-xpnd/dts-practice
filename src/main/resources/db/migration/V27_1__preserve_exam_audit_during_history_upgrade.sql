-- Prepare the still-pending V28 without modifying any executed migration.
-- Normal exam updates retain V4 behavior even between migration transactions.
SET LOCAL lock_timeout='5s';
LOCK TABLE public.exams IN SHARE ROW EXCLUSIVE MODE;

DO $guard$
BEGIN
    IF EXISTS(SELECT 1 FROM public.flyway_schema_history WHERE version IN ('28','29') AND success) THEN
        RAISE EXCEPTION 'History upgrade preparation must precede V28; existing changed timestamps require reconciliation';
    END IF;
    IF NOT EXISTS(SELECT 1 FROM pg_trigger WHERE tgrelid='public.exams'::regclass
        AND tgname='set_exams_updated_at' AND NOT tgisinternal AND tgenabled='O'
        AND tgfoid='public.update_updated_at_column()'::regprocedure) THEN
        RAISE EXCEPTION 'Unexpected exam audit trigger; no automatic replacement accepted';
    END IF;
END;
$guard$;

CREATE FUNCTION public.dts_preserve_exam_timestamp_during_history_upgrade()
RETURNS TRIGGER LANGUAGE plpgsql AS $body$
DECLARE
    before_row jsonb := to_jsonb(OLD);
    after_row jsonb := to_jsonb(NEW);
BEGIN
    IF ((after_row->'history_target_id') IS DISTINCT FROM (before_row->'history_target_id')
        OR (after_row->'history_attempt_no') IS DISTINCT FROM (before_row->'history_attempt_no'))
        AND ((after_row - ARRAY['history_target_id','history_attempt_no','updated_at']::text[])
            IS NOT DISTINCT FROM
            (before_row - ARRAY['history_target_id','history_attempt_no','updated_at']::text[])) THEN
        NEW.updated_at := OLD.updated_at;
    ELSE
        NEW.updated_at := NOW();
    END IF;
    RETURN NEW;
END;
$body$;

DROP TRIGGER set_exams_updated_at ON public.exams;
CREATE TRIGGER set_exams_updated_at BEFORE UPDATE ON public.exams
FOR EACH ROW EXECUTE FUNCTION public.dts_preserve_exam_timestamp_during_history_upgrade();
