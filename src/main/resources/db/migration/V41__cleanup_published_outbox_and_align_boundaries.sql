-- V41__cleanup_published_outbox_and_align_boundaries.sql
-- Purges published outbox events and registers alignment with dts-progress.

DELETE FROM practice_outbox WHERE published_at IS NOT NULL;
DELETE FROM exam_progress_outbox WHERE published_at IS NOT NULL;
