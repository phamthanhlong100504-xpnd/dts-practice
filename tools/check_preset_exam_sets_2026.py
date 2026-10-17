"""Read-only comparison of fixed sets with the Practice question bank."""

import argparse
import json
import subprocess
from pathlib import Path

from generate_preset_exam_sets_2026 import (
    BANK,
    MATRIX,
    generate,
    require,
)


QUESTIONS_SQL = """
SELECT COALESCE(
    json_agg(json_build_object(
        'id', id,
        'sourceQuestionId', source_question_id,
        'chapter', chapter,
        'isCritical', is_critical,
        'applicableLicenses', applicable_licenses,
        'criticalLicenses', critical_licenses
    ) ORDER BY source_question_id),
    '[]'::json
)::text
FROM questions
WHERE bank_version = 'dts-2026-600-v1';
"""

COUNTS_SQL = """
SELECT (SELECT COUNT(*) FROM preset_exam_sets) || '|' ||
       (SELECT COUNT(*) FROM preset_exam_set_questions) || '|' ||
       (SELECT COUNT(*) FROM exams
        WHERE preset_exam_set_id IS NOT NULL);
"""


def query(container, sql):
    command = [
        "docker",
        "exec",
        container,
        "psql",
        "-U",
        "postgres",
        "-d",
        "dts_practice",
        "-X",
        "-A",
        "-t",
        "-v",
        "ON_ERROR_STOP=1",
        "-c",
        sql,
    ]

    result = subprocess.run(
        command,
        capture_output=True,
        text=True,
        encoding="utf-8",
        check=False,
    )

    require(
        result.returncode == 0,
        "Không đọc được database Practice: "
        + result.stderr.strip(),
    )

    return result.stdout.strip()


def verify(manifest, source, rules, rows):
    require(
        manifest.get("bankVersion") == BANK,
        "Manifest sai phiên bản",
    )
    require(
        isinstance(rows, list) and len(rows) == 600,
        f"Practice cần đúng 600 câu {BANK}, "
        f"hiện có {len(rows)}",
    )

    db = {
        row["sourceQuestionId"]: row
        for row in rows
    }

    require(
        len(db) == 600
        and set(db) == set(range(1, 601)),
        "Practice bị thiếu hoặc trùng source_question_id",
    )
    require(
        len({row["id"] for row in rows}) == 600,
        "Practice bị trùng ID bản ghi câu hỏi",
    )

    critical_by_license = rules[
        "criticalQuestionIdsByLicense"
    ]

    for question in source:
        source_id = question["id"]
        row = db[source_id]

        expected_critical = {
            license_class
            for license_class in MATRIX
            if source_id in critical_by_license[
                license_class
            ]
        }

        require(
            row["chapter"] == question["chapter_id"],
            f"Câu nguồn {source_id}: chương không khớp",
        )
        require(
            row["isCritical"] is question["is_critical"],
            f"Câu nguồn {source_id}: is_critical không khớp",
        )
        require(
            set(row["applicableLicenses"] or [])
            == set(question["applicable_licenses"]),
            f"Câu nguồn {source_id}: hạng áp dụng không khớp",
        )
        require(
            set(row["criticalLicenses"] or [])
            == expected_critical,
            f"Câu nguồn {source_id}: nhãn điểm liệt không khớp",
        )

    require(
        len(manifest["sets"]) == 300,
        "Manifest cần đúng 300 đề",
    )

    for item in manifest["sets"]:
        license_class = item["licenseClass"]
        total, minutes, required, quotas = MATRIX[
            license_class
        ]
        source_ids = item["sourceQuestionIds"]
        label = f"{license_class}-{item['code']}"

        require(
            len(source_ids)
            == len(set(source_ids))
            == total,
            f"Đề {label}: thiếu hoặc trùng câu",
        )
        require(
            item["durationMinutes"] == minutes
            and item["requiredCorrect"] == required,
            f"Đề {label}: sai thời gian hoặc điểm đạt",
        )

        critical_ids = set(
            critical_by_license[license_class]
        )

        require(
            set(source_ids) & critical_ids
            == {item["criticalSourceQuestionId"]},
            f"Đề {label}: sai câu điểm liệt",
        )

        by_chapter = {}

        for source_id in source_ids:
            row = db[source_id]

            require(
                license_class
                in row["applicableLicenses"],
                f"Đề {label}: câu {source_id} sai hạng",
            )

            if source_id not in critical_ids:
                chapter = row["chapter"]
                by_chapter[chapter] = (
                    by_chapter.get(chapter, 0) + 1
                )

        require(
            by_chapter == quotas,
            f"Đề {label}: sai ma trận chương",
        )

    return {
        item["id"]: [
            db[source_id]["id"]
            for source_id in item["sourceQuestionIds"]
        ]
        for item in manifest["sets"]
    }


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

    args = parser.parse_args()

    source = json.loads(
        (args.source_dir / "questions.json").read_text(
            encoding="utf-8-sig"
        )
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
        "Manifest khác nguồn hoặc thuật toán; "
        "dừng để kiểm tra",
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
    counts = query(
        args.container,
        COUNTS_SQL,
    )

    require(
        len(mapped) == 300,
        "Thiếu ánh xạ đề",
    )
    require(
        sum(map(len, mapped.values())) == 11700,
        "Thiếu ánh xạ câu",
    )

    first_id = manifest["sets"][0]["id"]
    source_48_id = next(
        row["id"]
        for row in rows
        if row["sourceQuestionId"] == 48
    )

    print(
        "CHECK OK: metadata của 600 câu Practice "
        "khớp JSON nguồn"
    )
    print(
        "300 bộ đề / 11700 vị trí câu "
        "ánh xạ được vào Practice"
    )
    print(
        "A1-01: nguồn 48 -> Practice ID",
        source_48_id,
        "; số câu:",
        len(mapped[first_id]),
    )
    print(
        "Bản ghi hiện có "
        "preset_sets|preset_questions|attempts =",
        counts,
    )
    print(
        "Chỉ đọc database; chưa INSERT/UPDATE/DELETE"
    )


if __name__ == "__main__":
    main()