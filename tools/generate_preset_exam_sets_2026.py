"""Generate fixed exam sets from the 2026 source bank.

Default: dry run. --write creates a manifest; neither mode writes database.
"""

import argparse
import hashlib
import json
import uuid
from collections import Counter
from pathlib import Path

BANK = "dts-2026-600-v1"
SET_COUNT = 20

MOTOR = {1: 8, 2: 1, 3: 1, 5: 8, 6: 6}

MATRIX = {
    "A1": (25, 19, 21, MOTOR),
    "A": (25, 19, 23, MOTOR),
    "B1": (25, 19, 23, MOTOR),
    "B": (30, 20, 27, {1: 8, 2: 1, 3: 1, 4: 1, 5: 10, 6: 8}),
    "C1": (35, 22, 32, {1: 10, 2: 1, 3: 2, 4: 1, 5: 11, 6: 9}),
    "C": (40, 24, 36, {1: 11, 2: 1, 3: 2, 4: 1, 5: 13, 6: 11}),
}

LARGE = (45, 26, 41, {1: 12, 2: 1, 3: 2, 4: 2, 5: 14, 6: 13})

for license_class in (
    "D1", "D2", "D", "BE", "C1E", "CE", "D1E", "D2E", "DE"
):
    MATRIX[license_class] = LARGE


def require(condition, message):
    if not condition:
        raise ValueError(message)


def canonical_sha256(value):
    content = json.dumps(
        value,
        ensure_ascii=False,
        sort_keys=True,
        separators=(",", ":"),
    )
    return hashlib.sha256(content.encode("utf-8")).hexdigest()


def rank(*parts):
    content = "|".join(
        str(part) for part in (BANK, *parts)
    )
    return hashlib.sha256(content.encode("utf-8")).hexdigest()


def choose(pool, count, usage, *seed):
    require(
        len(pool) >= count,
        f"Không đủ câu cho {seed}: cần {count}, có {len(pool)}",
    )

    selected = sorted(
        pool,
        key=lambda question_id: (
            usage[question_id],
            rank(*seed, question_id),
        ),
    )[:count]

    for question_id in selected:
        usage[question_id] += 1

    return selected


def generate(source_dir):
    questions = json.loads(
        (source_dir / "questions.json").read_text(
            encoding="utf-8-sig"
        )
    )
    rules = json.loads(
        (source_dir / "critical-by-license.json").read_text(
            encoding="utf-8-sig"
        )
    )
    critical_by_license = rules["criticalQuestionIdsByLicense"]

    require(
        rules.get("bankVersion") == BANK,
        "Sai phiên bản nhãn điểm liệt",
    )
    require(
        isinstance(questions, list) and len(questions) == 600,
        "Cần đúng 600 câu",
    )

    by_id = {
        question["id"]: question
        for question in questions
    }

    require(
        len(by_id) == 600
        and set(by_id) == set(range(1, 601)),
        "ID nguồn phải đủ 1–600",
    )
    require(
        sum(
            question["is_critical"] is True
            for question in questions
        ) == 60,
        "Bộ nguồn phải có đúng 60 câu điểm liệt",
    )
    require(
        set(critical_by_license) == set(MATRIX),
        "Danh sách 15 hạng không khớp",
    )

    expected_total = {
        "A1": 250,
        "A": 250,
        "B1": 300,
    }
    expected_critical = {
        "A1": 20,
        "A": 20,
        "B1": 30,
    }

    all_sets = []

    for license_class, (
        total,
        minutes,
        required_correct,
        quotas,
    ) in MATRIX.items():
        require(
            1 + sum(quotas.values()) == total,
            f"Ma trận {license_class} không đủ {total} câu",
        )

        eligible = {
            question_id
            for question_id, question in by_id.items()
            if license_class in question["applicable_licenses"]
        }

        require(
            len(eligible)
            == expected_total.get(license_class, 600),
            f"Tổng câu hạng {license_class} không đúng",
        )

        critical_list = critical_by_license[license_class]
        critical = set(critical_list)

        require(
            len(critical_list)
            == len(critical)
            == expected_critical.get(license_class, 60),
            f"Số câu điểm liệt hạng {license_class} không đúng",
        )
        require(
            critical <= eligible
            and all(
                by_id[question_id]["is_critical"] is True
                for question_id in critical
            ),
            f"Nhãn điểm liệt hạng {license_class} không khớp",
        )

        ordinary = {
            chapter: sorted(
                question_id
                for question_id in eligible - critical
                if by_id[question_id]["chapter_id"] == chapter
            )
            for chapter in quotas
        }

        for chapter, quota in quotas.items():
            require(
                len(ordinary[chapter]) >= quota,
                f"Hạng {license_class}, chương {chapter} "
                "thiếu câu thường",
            )

        usage = Counter()
        class_sets = []

        for number in range(1, SET_COUNT + 1):
            critical_id = choose(
                sorted(critical),
                1,
                usage,
                license_class,
                number,
                "critical",
            )[0]

            chosen = [critical_id]

            for chapter in sorted(quotas):
                chosen.extend(
                    choose(
                        ordinary[chapter],
                        quotas[chapter],
                        usage,
                        license_class,
                        number,
                        "chapter",
                        chapter,
                    )
                )

            require(
                len(chosen) == len(set(chosen)) == total,
                f"Đề {license_class}-{number:02d} "
                "bị trùng hoặc thiếu câu",
            )

            chosen.sort(
                key=lambda question_id: rank(
                    license_class,
                    number,
                    "order",
                    question_id,
                )
            )

            ordinary_count = Counter(
                by_id[question_id]["chapter_id"]
                for question_id in chosen
                if question_id != critical_id
            )

            require(
                dict(ordinary_count) == quotas,
                f"Đề {license_class}-{number:02d} "
                "sai ma trận chương",
            )

            class_sets.append({
                "id": str(
                    uuid.uuid5(
                        uuid.NAMESPACE_URL,
                        f"dts:preset:{BANK}:"
                        f"{license_class}:{number:02d}",
                    )
                ),
                "code": f"{number:02d}",
                "title": (
                    "Đề thi thử lý thuyết GPLX "
                    f"{license_class} - Đề {number:02d}"
                ),
                "licenseClass": license_class,
                "bankVersion": BANK,
                "totalQuestions": total,
                "durationMinutes": minutes,
                "requiredCorrect": required_correct,
                "displayOrder": number,
                "criticalSourceQuestionId": critical_id,
                "sourceQuestionIds": chosen,
            })

        require(
            len({
                tuple(item["sourceQuestionIds"])
                for item in class_sets
            }) == SET_COUNT,
            f"Hạng {license_class} có đề bị trùng hoàn toàn",
        )

        all_sets.extend(class_sets)

        distinct_critical = len({
            item["criticalSourceQuestionId"]
            for item in class_sets
        })

        print(
            f"{license_class:4} | {SET_COUNT} đề | "
            f"{total} câu/đề | {minutes} phút | "
            f"{distinct_critical} câu điểm liệt khác nhau"
        )

    require(
        len(all_sets) == 15 * SET_COUNT,
        "Tổng bộ đề không đúng 300",
    )

    return {
        "schemaVersion": 1,
        "bankVersion": BANK,
        "generationMethod": "balanced-hash-v1",
        "questionsSha256": canonical_sha256(questions),
        "criticalRulesSha256": canonical_sha256(rules),
        "sets": all_sets,
    }


def main():
    repo = Path(__file__).resolve().parent.parent

    parser = argparse.ArgumentParser(
        description=__doc__
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
        "--output",
        type=Path,
        default=(
            repo
            / "data/preset-exams/2026/sets.json"
        ),
    )

    action = parser.add_mutually_exclusive_group()
    action.add_argument(
        "--dry-run",
        action="store_true",
    )
    action.add_argument(
        "--write",
        action="store_true",
    )

    args = parser.parse_args()
    manifest = generate(args.source_dir)

    content = (
        json.dumps(
            manifest,
            ensure_ascii=False,
            indent=2,
        ) + "\n"
    ).encode("utf-8")

    digest = hashlib.sha256(content).hexdigest()
    question_placements = sum(
        len(item["sourceQuestionIds"])
        for item in manifest["sets"]
    )

    print(
        f"OK: {len(manifest['sets'])} bộ đề, "
        f"{question_placements} lượt câu; SHA256={digest}"
    )
    print(
        "Mẫu A1-01:",
        manifest["sets"][0]["sourceQuestionIds"],
    )

    if not args.write:
        print("DRY RUN: chưa ghi file, chưa ghi database")
        return

    args.output.parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    if args.output.exists():
        require(
            args.output.read_bytes() == content,
            f"File {args.output} đã tồn tại nhưng nội dung khác; "
            "không ghi đè",
        )
        print(f"File đã khớp: {args.output}")
        return

    args.output.write_bytes(content)
    print(
        f"Đã ghi: {args.output}; chưa ghi database"
    )


if __name__ == "__main__":
    main()