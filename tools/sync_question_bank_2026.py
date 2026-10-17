"""Đọc bộ câu hỏi 2026 từ Content Builder và kiểm tra trước khi nhập Practice."""

import argparse
import json
import os
from collections import Counter
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

BANK_VERSION = "dts-2026-600-v1"
CONTENT_BUILDER = "http://127.0.0.1:8082"
PRACTICE = "http://127.0.0.1:8087"

LICENSES = (
    "A1", "A", "B1", "B", "C1", "C", "D1", "D2", "D",
    "BE", "C1E", "CE", "D1E", "D2E", "DE",
)

A1_CRITICAL = {
    19, 20, 21, 22, 24, 26, 27, 28, 30, 47,
    48, 52, 53, 63, 64, 65, 68, 70, 71, 72,
}

B1_CRITICAL = A1_CRITICAL | {
    73, 74, 87, 89, 90, 91, 92, 215, 254, 255,
}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def call(method, url, token, data=None):
    headers = {"Authorization": "Bearer " + token}
    if data is not None:
        headers["Content-Type"] = "application/json"

    request = Request(url, data=data, headers=headers, method=method)

    try:
        with urlopen(request, timeout=60) as response:
            return json.load(response)
    except HTTPError as error:
        detail = error.read(500).decode("utf-8", errors="replace")
        raise RuntimeError(
            f"{method} {url}: HTTP {error.code}: {detail}"
        ) from error
    except URLError as error:
        raise RuntimeError(
            f"{method} {url}: không kết nối được ({error.reason})"
        ) from error


def load_bank(token):
    candidates = []
    page = 0

    while True:
        query = urlencode({
            "status": "PUBLISHED",
            "page": page,
            "size": 100,
            "sort": "createdAt,asc",
        })

        result = call(
            "GET",
            f"{CONTENT_BUILDER}/api/v1/content-builder/questions?{query}",
            token,
        )

        require(
            isinstance(result.get("content"), list),
            "Phản hồi phân trang của Content Builder không hợp lệ",
        )

        candidates.extend(
            item
            for item in result["content"]
            if (item.get("metadata") or {}).get("bankVersion")
            == BANK_VERSION
        )

        if result.get("last") is True:
            break

        page += 1
        require(page < 100, "Có quá nhiều trang câu hỏi; dừng để kiểm tra")

    require(
        len(candidates) == 600,
        f"Content Builder trả {len(candidates)} câu thuộc "
        f"{BANK_VERSION}; cần đúng 600",
    )

    questions = []

    for index, item in enumerate(candidates, start=1):
        url = (
            f"{CONTENT_BUILDER}/api/v1/content-builder/questions/"
            f"{item['id']}?includeOptions=true"
        )
        detail = call("GET", url, token)
        metadata = detail.get("metadata") or {}
        source_id = metadata.get("sourceQuestionId")

        require(
            detail.get("status") == "PUBLISHED",
            f"Câu {source_id}: chưa xuất bản",
        )
        require(
            detail.get("type") == "SINGLE_CHOICE",
            f"Câu {source_id}: sai loại câu hỏi",
        )
        require(
            metadata.get("bankVersion") == BANK_VERSION,
            f"Câu {source_id}: sai phiên bản",
        )

        options = sorted(
            detail.get("options") or [],
            key=lambda option: option["sortOrder"],
        )

        require(
            len(options) in (2, 3, 4),
            f"Câu {source_id}: số đáp án không hợp lệ",
        )
        require(
            [option.get("sortOrder") for option in options]
            == list(range(len(options))),
            f"Câu {source_id}: thứ tự đáp án không bắt đầu từ 0",
        )
        require(
            [
                int(
                    (option.get("metadata") or {}).get(
                        "sourceOptionId", -1
                    )
                )
                for option in options
            ]
            == list(range(1, len(options) + 1)),
            f"Câu {source_id}: ID đáp án nguồn không khớp thứ tự",
        )

        correct = [
            position
            for position, option in enumerate(options)
            if option.get("isCorrect") is True
        ]
        require(
            len(correct) == 1,
            f"Câu {source_id}: cần đúng một đáp án đúng",
        )

        media_ids = detail.get("mediaFileIds") or []
        require(
            len(media_ids) <= 1,
            f"Câu {source_id}: có nhiều hơn một ảnh",
        )

        explanations = detail.get("explanations") or {}

        questions.append({
            "contentBuilderQuestionId": detail["id"],
            "sourceQuestionId": source_id,
            "chapter": metadata.get("chapterId"),
            "questionText": detail["content"],
            "options": [
                {
                    "label": chr(65 + position),
                    "text": option["content"],
                }
                for position, option in enumerate(options)
            ],
            "correctAnswer": chr(65 + correct[0]),
            "isCritical": metadata.get("isCritical"),
            "applicableLicenses": metadata.get("applicableLicenses"),
            "criticalLicenses": metadata.get("criticalLicenses"),
            "explanation": explanations.get("text"),
            "mediaFileId": media_ids[0] if media_ids else None,
        })

        if index % 100 == 0:
            print(f"Đã đọc chi tiết {index}/600 câu")

    return questions


def validate(questions):
    ids = [question["sourceQuestionId"] for question in questions]

    require(
        len(ids) == 600 and set(ids) == set(range(1, 601)),
        "ID nguồn phải đủ và duy nhất từ 1 đến 600",
    )
    require(
        len({
            question["contentBuilderQuestionId"]
            for question in questions
        }) == 600,
        "Trùng UUID câu hỏi Content Builder",
    )
    require(
        Counter(question["chapter"] for question in questions)
        == {1: 180, 2: 25, 3: 58, 4: 37, 5: 185, 6: 115},
        "Số câu theo chương không khớp dữ liệu đã duyệt",
    )
    require(
        sum(question["isCritical"] is True for question in questions)
        == 60,
        "Không đủ 60 câu điểm liệt toàn bộ",
    )
    require(
        sum(
            question["mediaFileId"] is not None
            for question in questions
        ) == 318,
        "Không đủ 318 liên kết ảnh",
    )
    require(
        {
            question["sourceQuestionId"]
            for question in questions
            if not (question["explanation"] or "").strip()
        } == {466, 474, 475},
        "Danh sách câu thiếu giải thích đã thay đổi",
    )

    for question in questions:
        source_id = question["sourceQuestionId"]
        applicable = question["applicableLicenses"]
        critical = question["criticalLicenses"]

        require(
            isinstance(applicable, list)
            and len(applicable) == len(set(applicable))
            and set(applicable) <= set(LICENSES),
            f"Câu {source_id}: applicableLicenses không hợp lệ",
        )
        require(
            isinstance(critical, list)
            and len(critical) == len(set(critical))
            and set(critical) <= set(applicable),
            f"Câu {source_id}: criticalLicenses không hợp lệ",
        )
        require(
            not critical or question["isCritical"] is True,
            f"Câu {source_id}: nhãn điểm liệt mâu thuẫn",
        )
        require(
            question["questionText"]
            and all(option["text"] for option in question["options"]),
            f"Câu {source_id}: thiếu nội dung",
        )

    for license_class in LICENSES:
        count = sum(
            license_class in question["applicableLicenses"]
            for question in questions
        )
        critical_ids = {
            question["sourceQuestionId"]
            for question in questions
            if license_class in question["criticalLicenses"]
        }

        if license_class in ("A1", "A"):
            expected_count = 250
            expected_critical = A1_CRITICAL
        elif license_class == "B1":
            expected_count = 300
            expected_critical = B1_CRITICAL
        else:
            expected_count = 600
            expected_critical = None

        require(
            count == expected_count,
            f"Hạng {license_class}: {count} câu; cần {expected_count}",
        )
        require(
            critical_ids == expected_critical
            if expected_critical is not None
            else len(critical_ids) == 60,
            f"Hạng {license_class}: danh sách điểm liệt không khớp",
        )

        print(
            f"{license_class:4} | {count:3} câu | "
            f"{len(critical_ids):2} điểm liệt"
        )


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--apply",
        action="store_true",
        help="POST toàn bộ 600 câu vào Practice local",
    )
    args = parser.parse_args()

    token = os.environ.get("DTS_IMPORT_TOKEN")
    require(token, "Thiếu DTS_IMPORT_TOKEN trong cửa sổ PowerShell này")

    questions = load_bank(token)
    questions.sort(key=lambda question: question["sourceQuestionId"])
    validate(questions)

    if not args.apply:
        print("CHECK OK: chỉ đọc Content Builder; chưa ghi Practice.")
        return

    payload = json.dumps(
        {"bankVersion": BANK_VERSION, "questions": questions},
        ensure_ascii=False,
    ).encode("utf-8")

    result = call(
        "POST",
        f"{PRACTICE}/api/v1/questions/admin/import-bank",
        token,
        payload,
    )
    print("Practice đã nhập:", result.get("data"))


if __name__ == "__main__":
    try:
        main()
    except (ValueError, RuntimeError, KeyError, TypeError) as error:
        raise SystemExit(f"DỪNG: {error}") from error