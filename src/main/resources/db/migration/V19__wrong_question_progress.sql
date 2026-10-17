-- Screen 08A: one current review state per user, license, bank and question.
-- Historical answer rows are snapshots. Repeated chapter answers that were
-- overwritten before this migration cannot be recovered as separate events.

CREATE TABLE wrong_question_progress (
    user_id              UUID        NOT NULL,
    license_class        VARCHAR(10) NOT NULL,
    bank_version         VARCHAR(64) NOT NULL,
    question_id          INTEGER     NOT NULL REFERENCES questions(id),
    needs_review         BOOLEAN     NOT NULL,
    last_correct         BOOLEAN     NOT NULL,
    last_selected_answer VARCHAR(10) NOT NULL,
    last_wrong_answer    VARCHAR(10),
    last_wrong_at        TIMESTAMPTZ,
    last_answered_at     TIMESTAMPTZ NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_wrong_question_progress
        PRIMARY KEY (user_id, license_class, bank_version, question_id),
    CONSTRAINT ck_wrong_question_progress_state
        CHECK (needs_review <> last_correct)
);

CREATE INDEX idx_wrong_question_progress_active
    ON wrong_question_progress (
        user_id, license_class, bank_version, last_wrong_at DESC
    )
    WHERE needs_review;

-- Preserve the most recent recorded answer across finished exams, active
-- PRACTICE sessions and chapter learning. Active EXAM answers must not appear
-- here before the exam is finished, or the list can reveal their correctness.
WITH events AS (
    SELECT e.user_id,
           e.exam_type AS license_class,
           e.bank_version,
           a.question_id,
           a.selected_answer,
           a.is_correct,
           COALESCE(a.answered_at, a.updated_at, a.created_at) AS answered_at,
           0 AS source_priority,
           a.id::text AS source_key
    FROM exam_answers a
    JOIN exams e ON e.id = a.exam_id
    JOIN questions q ON q.id = a.question_id
    WHERE e.bank_version = 'dts-2026-600-v1'
      AND q.bank_version = e.bank_version
      AND jsonb_exists(q.applicable_licenses, e.exam_type)
      AND a.selected_answer IS NOT NULL
      AND a.is_correct IS NOT NULL
      AND (e.mode = 'PRACTICE' OR e.status IN ('COMPLETED', 'TIMEOUT'))

    UNION ALL

    SELECT p.user_id,
           p.license_class,
           p.bank_version,
           p.question_id,
           p.selected_answer,
           p.is_correct,
           p.last_answered_at AS answered_at,
           1 AS source_priority,
           p.question_id::text AS source_key
    FROM chapter_question_progress p
    JOIN questions q ON q.id = p.question_id
    WHERE p.bank_version = 'dts-2026-600-v1'
      AND q.bank_version = p.bank_version
      AND q.chapter = p.chapter_id
      AND jsonb_exists(q.applicable_licenses, p.license_class)
), ranked AS (
    SELECT events.*,
           ROW_NUMBER() OVER (
               PARTITION BY user_id, license_class, bank_version, question_id
               ORDER BY answered_at DESC, source_priority DESC, source_key DESC
           ) AS latest_rank,
           ROW_NUMBER() OVER (
               PARTITION BY user_id, license_class, bank_version,
                            question_id, is_correct
               ORDER BY answered_at DESC, source_priority DESC, source_key DESC
           ) AS correctness_rank
    FROM events
), last_wrong AS (
    SELECT user_id, license_class, bank_version, question_id,
           selected_answer AS last_wrong_answer,
           answered_at AS last_wrong_at
    FROM ranked
    WHERE is_correct = FALSE AND correctness_rank = 1
)
INSERT INTO wrong_question_progress (
    user_id, license_class, bank_version, question_id,
    needs_review, last_correct, last_selected_answer,
    last_wrong_answer, last_wrong_at, last_answered_at
)
SELECT r.user_id, r.license_class, r.bank_version, r.question_id,
       NOT r.is_correct, r.is_correct, r.selected_answer,
       w.last_wrong_answer, w.last_wrong_at, r.answered_at
FROM ranked r
LEFT JOIN last_wrong w
  ON w.user_id = r.user_id
 AND w.license_class = r.license_class
 AND w.bank_version = r.bank_version
 AND w.question_id = r.question_id
WHERE r.latest_rank = 1;