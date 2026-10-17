-- Remove the migration-only guard after immutable V28 has populated its columns.
-- The shared V4 function and exam_answers audit trigger have never been changed.
SET LOCAL lock_timeout='5s';
LOCK TABLE public.exams IN SHARE ROW EXCLUSIVE MODE;

DO $guard$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM public.flyway_schema_history WHERE version='27.1' AND success)
        OR NOT EXISTS(SELECT 1 FROM public.flyway_schema_history WHERE version='28' AND success) THEN
        RAISE EXCEPTION 'Expected ordered history preparation and V28 are missing; do not repair or bypass migration history';
    END IF;
    IF NOT EXISTS(SELECT 1 FROM pg_trigger WHERE tgrelid='public.exams'::regclass
        AND tgname='set_exams_updated_at' AND NOT tgisinternal AND tgenabled='O'
        AND tgfoid='public.dts_preserve_exam_timestamp_during_history_upgrade()'::regprocedure) THEN
        RAISE EXCEPTION 'Unexpected temporary audit trigger; no automatic replacement accepted';
    END IF;
END;
$guard$;

DROP TRIGGER set_exams_updated_at ON public.exams;
CREATE TRIGGER set_exams_updated_at BEFORE UPDATE ON public.exams
FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();
DROP FUNCTION public.dts_preserve_exam_timestamp_during_history_upgrade();
