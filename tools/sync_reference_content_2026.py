
import argparse
import json
import os
import re
import uuid

from collections import Counter
from datetime import datetime
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, urlsplit
from urllib.request import (
    HTTPRedirectHandler,
    ProxyHandler,
    Request,
    build_opener,
)


SIGN_VERSION = "dts-traffic-signs-v1"
TIP_VERSION = "dts-2026-600-v1"

SIGN_COUNT = 221
TIP_COUNT = 12
PAGE_SIZE = 100

CATEGORIES = {
    "cam": 59,
    "nguy_hiem": 83,
    "hieu_lenh": 41,
    "chi_dan": 25,
    "phu": 13,
}

LICENSES = {
    "A1", "A", "B1", "B", "C1", "C",
    "D1", "D2", "D", "BE", "C1E",
    "CE", "D1E", "D2E", "DE",
}


class NoRedirect(HTTPRedirectHandler):

    def redirect_request(
        self, req, fp, code, msg, headers, newurl
    ):
        raise RuntimeError(
            "API redirected; refuse to forward bearer token"
        )


HTTP = build_opener(
    ProxyHandler({}),
    NoRedirect(),
)


def require(condition, message):
    if not condition:
        raise ValueError(message)


def nonblank(value, maximum=None):
    return (
        isinstance(value, str)
        and bool(value.strip())
        and (
            maximum is None
            or len(value) <= maximum
        )
    )


def valid_uuid(value, label):
    require(
        isinstance(value, str),
        f"{label}: UUID must be a string",
    )

    try:
        parsed = uuid.UUID(value)
    except ValueError as error:
        raise ValueError(
            f"{label}: invalid UUID"
        ) from error

    require(
        str(parsed) == value.lower(),
        f"{label}: UUID is not canonical",
    )

    return value


def valid_timestamp(value, label):
    require(
        nonblank(value) and "T" in value,
        f"{label}: missing publishedAt",
    )

    try:
        parsed = datetime.fromisoformat(value)
    except ValueError as error:
        raise ValueError(
            f"{label}: invalid publishedAt"
        ) from error

    require(
        parsed.tzinfo is None,
        f"{label}: publishedAt must be LocalDateTime",
    )

    return value


def base_url(value, allow_remote):
    candidate = (value or "").strip().rstrip("/")
    parts = urlsplit(candidate)

    require(
        parts.scheme in ("http", "https")
        and parts.hostname,
        "API URL must be http(s)://host[:port]",
    )

    require(
        not parts.username
        and not parts.password
        and parts.path in ("", "/")
        and not parts.query
        and not parts.fragment,
        "API URL must not contain credentials, path or query",
    )

    local = parts.hostname in (
        "localhost",
        "127.0.0.1",
        "::1",
    )

    require(
        local or allow_remote,
        "Remote API blocked: use --allow-remote",
    )

    require(
        local or parts.scheme == "https",
        "Remote API must use HTTPS",
    )

    return candidate


def api(method, base, path, token, body=None):
    data = None

    if body is not None:
        data = json.dumps(
            body,
            ensure_ascii=False,
            separators=(",", ":"),
        ).encode("utf-8")

    headers = {
        "Authorization": "Bearer " + token,
        "Accept": "application/json",
    }

    if data is not None:
        headers["Content-Type"] = (
            "application/json; charset=utf-8"
        )

    request = Request(
        base + path,
        data=data,
        headers=headers,
        method=method,
    )

    try:
        with HTTP.open(
            request,
            timeout=45,
        ) as response:

            result = json.load(response)

            require(
                isinstance(result, dict),
                f"{method} {path}: invalid JSON object",
            )

            return result

    except HTTPError as error:
        detail = error.read(300).decode(
            "utf-8",
            errors="replace",
        )

        raise RuntimeError(
            f"{method} {path}: HTTP {error.code}: {detail}"
        ) from error

    except URLError as error:
        raise RuntimeError(
            f"{method} {path}: cannot connect "
            f"({error.reason})"
        ) from error


def fetch_pages(
    base,
    token,
    endpoint,
    version,
    sort,
    expected,
):
    records = []
    page = 0

    while True:
        query = urlencode({
            "bankVersion": version,
            "page": page,
            "size": PAGE_SIZE,
            "sort": sort,
        })

        result = api(
            "GET",
            base,
            f"{endpoint}?{query}",
            token,
        )

        chunk = result.get("content")

        require(
            isinstance(chunk, list),
            f"{endpoint}: missing content array",
        )

        require(
            type(result.get("pageNumber")) is int
            and result["pageNumber"] == page,
            f"{endpoint}: incorrect pageNumber",
        )

        require(
            type(result.get("totalElements")) is int
            and result["totalElements"] == expected,
            f"{endpoint}: expected exactly "
            f"{expected} published records",
        )

        require(
            type(result.get("last")) is bool,
            f"{endpoint}: invalid last flag",
        )

        records.extend(chunk)

        require(
            len(records) <= expected,
            f"{endpoint}: too many records",
        )

        print(
            f"SOURCE_PAGE="
            f"{endpoint.rsplit('/', 1)[-1]}:{page} "
            f"ROWS={len(chunk)}"
        )

        if result["last"]:
            break

        require(
            chunk and page < 10,
            f"{endpoint}: incomplete pagination",
        )

        page += 1

    require(
        len(records) == expected,
        f"{endpoint}: got {len(records)} records, "
        f"expected {expected}",
    )

    return records


def validate_signs(items):
    sources = set()
    originals = set()
    medias = set()
    codes = set()
    images = set()

    categories = Counter()
    converted = []

    for entry in items:
        require(
            isinstance(entry, dict),
            "Sign: malformed API item",
        )

        sid = entry.get("sourceId")

        require(
            type(sid) is int
            and 1 <= sid <= SIGN_COUNT
            and sid not in sources,
            f"Sign: invalid/duplicate sourceId {sid}",
        )

        sources.add(sid)
        label = f"Sign {sid}"

        require(
            entry.get("bankVersion") == SIGN_VERSION
            and entry.get("status") == "PUBLISHED",
            f"{label}: wrong version/status",
        )

        cbid = valid_uuid(
            entry.get("id"),
            f"{label} source UUID",
        )

        mid = valid_uuid(
            entry.get("mediaFileId"),
            f"{label} Media UUID",
        )

        require(
            cbid not in originals
            and mid not in medias,
            f"{label}: duplicate UUID",
        )

        originals.add(cbid)
        medias.add(mid)

        for key, maximum in (
            ("code", 32),
            ("name", 255),
            ("category", 32),
            ("categoryName", 100),
            ("meaning", None),
            ("quickTip", None),
            ("sourceImage", 255),
        ):
            require(
                nonblank(entry.get(key), maximum),
                f"{label}: invalid {key}",
            )

        code = entry["code"].strip().casefold()
        image = entry["sourceImage"]

        require(
            code not in codes,
            f"{label}: duplicate code",
        )

        require(
            bool(
                re.fullmatch(
                    r"[A-Za-z0-9_-]+\.webp",
                    image,
                )
            )
            and image not in images,
            f"{label}: invalid/duplicate image",
        )

        codes.add(code)
        images.add(image)

        category = entry["category"]

        require(
            category in CATEGORIES,
            f"{label}: invalid category",
        )

        categories[category] += 1

        licenses = entry.get(
            "applicableLicenses"
        )

        require(
            isinstance(licenses, list)
            and len(licenses) > 0
            and all(
                isinstance(value, str)
                and value in LICENSES
                for value in licenses
            )
            and len(licenses) == len(set(licenses)),
            f"{label}: invalid license list",
        )

        converted.append({
            "contentBuilderTrafficSignId": cbid,
            "sourceSignId": sid,
            "code": entry["code"],
            "name": entry["name"],
            "category": category,
            "categoryName": entry["categoryName"],
            "meaning": entry["meaning"],
            "quickTip": entry["quickTip"],
            "sourceImage": image,
            "mediaFileId": mid,
            "applicableLicenses": licenses,
            "status": "PUBLISHED",
            "publishedAt": valid_timestamp(
                entry.get("publishedAt"),
                label,
            ),
        })

    require(
        sources == set(range(1, SIGN_COUNT + 1)),
        "Sign source IDs must cover 1..221",
    )

    require(
        categories == CATEGORIES,
        f"Sign category counts mismatch: "
        f"{dict(categories)}",
    )

    converted.sort(
        key=lambda sign: sign["sourceSignId"]
    )

    print("TRAFFIC_SIGNS=221")
    print("TRAFFIC_SIGN_MEDIA_IDS=221")
    print(
        "TRAFFIC_SIGN_CATEGORIES="
        + json.dumps(dict(sorted(categories.items())))
    )

    return {
        "bankVersion": SIGN_VERSION,
        "signs": converted,
    }


def validate_tips(items):
    sources = set()
    orders = set()
    originals = set()
    converted = []

    for entry in items:
        require(
            isinstance(entry, dict),
            "Tip: malformed API item",
        )

        sid = entry.get("sourceId")
        order = entry.get("sortOrder")

        require(
            type(sid) is int
            and 1 <= sid <= TIP_COUNT
            and sid not in sources,
            f"Tip: invalid/duplicate sourceId {sid}",
        )

        require(
            type(order) is int
            and 1 <= order <= TIP_COUNT
            and order not in orders,
            f"Tip {sid}: invalid/duplicate sortOrder",
        )

        sources.add(sid)
        orders.add(order)

        label = f"Tip {sid}"

        require(
            entry.get("bankVersion") == TIP_VERSION
            and entry.get("status") == "PUBLISHED",
            f"{label}: wrong version/status",
        )

        cbid = valid_uuid(
            entry.get("id"),
            f"{label} source UUID",
        )

        require(
            cbid not in originals,
            f"{label}: duplicate source UUID",
        )

        originals.add(cbid)

        for key, maximum in (
            ("title", 255),
            ("quickRule", None),
            ("explanation", None),
        ):
            require(
                nonblank(entry.get(key), maximum),
                f"{label}: invalid {key}",
            )

        converted.append({
            "contentBuilderMemoryTipId": cbid,
            "sourceTipId": sid,
            "sortOrder": order,
            "title": entry["title"],
            "quickRule": entry["quickRule"],
            "explanation": entry["explanation"],
            "status": "PUBLISHED",
            "publishedAt": valid_timestamp(
                entry.get("publishedAt"),
                label,
            ),
        })

    require(
        sources == set(range(1, TIP_COUNT + 1))
        and orders == set(range(1, TIP_COUNT + 1)),
        "Tip IDs/orders must cover 1..12",
    )

    converted.sort(
        key=lambda tip: tip["sortOrder"]
    )

    print("MEMORY_TIPS=12")

    return {
        "bankVersion": TIP_VERSION,
        "tips": converted,
    }


def main():
    parser = argparse.ArgumentParser()

    parser.add_argument(
        "--content-builder-url",
        default=os.environ.get(
            "DTS_CONTENT_BUILDER_URL",
            "http://127.0.0.1:8082",
        ),
    )

    parser.add_argument(
        "--practice-url",
        default=os.environ.get(
            "DTS_PRACTICE_URL",
            "http://127.0.0.1:8087",
        ),
    )

    parser.add_argument(
        "--kind",
        choices=("both", "signs", "tips"),
        default="both",
    )

    parser.add_argument(
        "--apply",
        action="store_true",
    )

    parser.add_argument(
        "--allow-remote",
        action="store_true",
    )

    args = parser.parse_args()

    require(
        not args.apply or args.kind != "both",
        "--apply requires --kind signs or --kind tips",
    )

    cb_url = base_url(
        args.content_builder_url,
        args.allow_remote,
    )

    token = (
        os.environ.get("DTS_IMPORT_TOKEN") or ""
    ).strip()

    require(
        bool(token),
        "Missing DTS_IMPORT_TOKEN",
    )

    payloads = {}

    if args.kind in ("both", "signs"):
        signs = fetch_pages(
            cb_url,
            token,
            "/api/v1/content-builder/traffic-signs",
            SIGN_VERSION,
            "sourceId,asc",
            SIGN_COUNT,
        )

        payloads["signs"] = validate_signs(signs)

    if args.kind in ("both", "tips"):
        tips = fetch_pages(
            cb_url,
            token,
            "/api/v1/content-builder/memory-tips",
            TIP_VERSION,
            "sortOrder,asc",
            TIP_COUNT,
        )

        payloads["tips"] = validate_tips(tips)

    print("SOURCE_VALIDATION=PASS")

    if not args.apply:
        print("DRY_RUN=PASS")
        print("PRACTICE_POST_REQUESTS=0")
        print("DATABASE_WRITES=0")
        return

    practice_url = base_url(
        args.practice_url,
        args.allow_remote,
    )

    kind = args.kind

    route = {
        "signs": (
            "/api/v1/reference-content/admin/"
            "import-traffic-signs"
        ),
        "tips": (
            "/api/v1/reference-content/admin/"
            "import-memory-tips"
        ),
    }[kind]

    result = api(
        "POST",
        practice_url,
        route,
        token,
        payloads[kind],
    )

    data = result.get("data")

    expected_count = (
        SIGN_COUNT
        if kind == "signs"
        else TIP_COUNT
    )

    require(
        isinstance(data, dict)
        and data.get("imported") == expected_count,
        "POST completed but unexpected response; "
        "CHECK Practice DB before retry",
    )

    print(
        f"PRACTICE_{kind.upper()}_IMPORTED="
        f"{expected_count}"
    )
    print("APPLY=PASS")


if __name__ == "__main__":
    try:
        main()
    except (
        ValueError,
        RuntimeError,
        KeyError,
        TypeError,
    ) as error:
        raise SystemExit(
            f"STOP: {error}"
        ) from error
