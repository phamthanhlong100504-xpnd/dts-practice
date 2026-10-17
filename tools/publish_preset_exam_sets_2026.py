"""Import fixed 2026 papers into local Practice PostgreSQL.

Default is a read-only check. --apply writes the selected papers.
"""

import argparse
import json
import subprocess
from pathlib import Path

from check_preset_exam_sets_2026 import (
    COUNTS_SQL,
    QUESTIONS_SQL,
    query,
    verify,
)
from generate_preset_exam_sets_2026 import (
    generate,
    require,
)


def quote(value):
    return "'" + str(value).replace("'", "''") + "'"


def make_sql(sets):
    values = []

    for item in sets:
        ids = item["sourceQuestionIds"]

        require(
            all(
                type(question_id) is int
                and 1 <= question_id <= 600
                for question_id in ids
            ),
            "ID câu nguồn không hợp lệ",
        )

        source_ids = (
            "ARRAY["
            + ",".join(str(question_id) for question_id in ids)
            + "]::integer[]"
        )

        values.append(
            "("
            + ",".join([
                quote(item["id"]),
                quote(item["code"]),
                quote(item["title"]),
                quote(item["licenseClass"]),
                quote(item["bankVersion"]),
                str(item["totalQuestions"]),
                str(item["durationMinutes"]),
                str(item["displayOrder"]),
                source_ids,
            ])
            + ")"
        )

    return (
        """BEGIN;

CREATE TEMP TABLE dts_preset_input (
    id UUID PRIMARY KEY,
    code VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    license_class VARCHAR(10) NOT NULL,
    bank_version VARCHAR(64) NOT NULL,
    total_questions INTEGER NOT NULL,
    duration_minutes INTEGER NOT NULL,
    display_order INTEGER NOT NULL,
    source_ids INTEGER[] NOT NULL
) ON COMMIT DROP;

INSERT INTO dts_preset_input
    (id, code, title, license_class, bank_version,
     total_questions, duration_minutes, display_order,
     source_ids)
VALUES
"""
        + ",\n".join(values)
        + ";\n"
        + """
DO $check$
DECLARE
    expected_count BIGINT;
    mapped_count BIGINT;
BEGIN
    SELECT SUM(cardinality(source_ids))
    INTO expected_count
    FROM dts_preset_input;

    SELECT COUNT(*)
    INTO mapped_count
    FROM dts_preset_input i
    CROSS JOIN LATERAL unnest(i.source_ids)
        AS u(source_id)
    JOIN questions q
      ON q.bank_version = i.bank_version
     AND q.source_question_id = u.source_id;

    IF mapped_count <> expected_count THEN
        RAISE EXCEPTION
            'Question mapping incomplete: % / %',
            mapped_count, expected_count;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM dts_preset_input i
        JOIN preset_exam_sets s ON s.id = i.id
        WHERE (
            s.code,
            s.title,
            s.license_class,
            s.bank_version,
            s.total_questions,
            s.duration_minutes,
            s.display_order
        ) IS DISTINCT FROM (
            i.code,
            i.title,
            i.license_class,
            i.bank_version,
            i.total_questions,
            i.duration_minutes,
            i.display_order
        )
        OR s.status NOT IN ('DRAFT', 'PUBLISHED')
    ) THEN
        RAISE EXCEPTION
            'Existing preset metadata differs from manifest';
    END IF;
END;
$check$;

INSERT INTO preset_exam_sets
    (id, code, title, license_class, bank_version,
     total_questions, duration_minutes, status,
     display_order)
SELECT
    id, code, title, license_class, bank_version,
    total_questions, duration_minutes, 'DRAFT',
    display_order
FROM dts_preset_input
ON CONFLICT (id) DO NOTHING;

INSERT INTO preset_exam_set_questions
    (preset_exam_set_id, question_id, question_order)
SELECT
    i.id,
    q.id,
    u.position::integer
FROM dts_preset_input i
CROSS JOIN LATERAL unnest(i.source_ids)
    WITH ORDINALITY AS u(source_id, position)
JOIN questions q
  ON q.bank_version = i.bank_version
 AND q.source_question_id = u.source_id
ON CONFLICT (preset_exam_set_id, question_order)
DO NOTHING;

DO $verify$
DECLARE
    item RECORD;
    actual INTEGER[];
BEGIN
    FOR item IN
        SELECT id, source_ids
        FROM dts_preset_input
    LOOP
        SELECT COALESCE(
            array_agg(
                q.source_question_id
                ORDER BY sq.question_order
            ),
            ARRAY[]::integer[]
        )
        INTO actual
        FROM preset_exam_set_questions sq
        JOIN questions q
          ON q.id = sq.question_id
        WHERE sq.preset_exam_set_id = item.id;

        IF actual IS DISTINCT FROM item.source_ids THEN
            RAISE EXCEPTION
                'Existing preset questions differ: %',
                item.id;
        END IF;
    END LOOP;
END;
$verify$;

UPDATE preset_exam_sets
SET
    status = 'PUBLISHED',
    updated_at = NOW()
WHERE id IN (
    SELECT id FROM dts_preset_input
)
AND status = 'DRAFT';

COMMIT;
"""
    )


def apply_sql(container, sql):
    command = [
        "docker",
        "exec",
        "-i",
        container,
        "psql",
        "-U",
        "postgres",
        "-d",
        "dts_practice",
        "-X",
        "-v",
        "ON_ERROR_STOP=1",
    ]

    result = subprocess.run(
        command,
        input=sql,
        capture_output=True,
        text=True,
        encoding="utf-8",
        check=False,
    )

    require(
        result.returncode == 0,
        "Import failed (rolled back): "
        + result.stderr.strip()[-3000:],
    )


def main():
    repo = Path(__file__).resolve().parent.parent

    parser = argparse.ArgumentParser(
        description=__doc__
    )
    parser.add_argument(
        "--manifest",
        type=Path,
        default=(
            repo
            / "data/preset-exams/2026/sets.json"
        ),
    )
    parser.add_argument(
        "--source-dir",
        type=Path,
        default=(
            repo.parent
            / "dts-content-builder"
            / "data/question-banks/2026/600"
        ),
    )
    parser.add_argument(
        "--container",
        default="dts-practice-postgres",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=0,
        help="First N sets for a local trial; 0 means all",
    )
    parser.add_argument(
        "--apply",
        action="store_true",
    )

    args = parser.parse_args()

    require(
        args.limit >= 0,
        "--limit must be >= 0",
    )

    source = json.loads(
        (
            args.source_dir
            / "questions.json"
        ).read_text(encoding="utf-8-sig")
    )
    rules = json.loads(
        (
            args.source_dir
            / "critical-by-license.json"
        ).read_text(encoding="utf-8-sig")
    )
    manifest = json.loads(
        args.manifest.read_text(
            encoding="utf-8-sig"
        )
    )

    require(
        manifest == generate(args.source_dir),
        "Manifest differs from source or generator; stopped",
    )

    rows = json.loads(
        query(args.container, QUESTIONS_SQL)
    )
    mapped = verify(
        manifest,
        source,
        rules,
        rows,
    )

    selected = manifest["sets"][
        :args.limit or None
    ]

    require(
        bool(selected),
        "No sets selected",
    )

    print(
        f"CHECK OK: {len(mapped)} sets, "
        f"{sum(map(len, mapped.values()))} "
        "mapped positions"
    )
    print(
        f"Batch: {len(selected)} sets, "
        f"{sum(len(item['sourceQuestionIds']) for item in selected)} "
        "questions"
    )
    print(
        "Before sets|questions|attempts:",
        query(args.container, COUNTS_SQL),
    )

    if not args.apply:
        print(
            "DRY RUN: read-only; use "
            "--apply --limit 1 for first local set"
        )
        return

    apply_sql(
        args.container,
        make_sql(selected),
    )

    print(
        "COMMIT OK. After sets|questions|attempts:",
        query(args.container, COUNTS_SQL),
    )


if __name__ == "__main__":
    main()