#!/usr/bin/env python3
import argparse
import base64
import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path


def build_headers(email: str, api_token: str) -> dict[str, str]:
    basic = base64.b64encode(f"{email}:{api_token}".encode("utf-8")).decode("ascii")
    return {
        "Authorization": f"Basic {basic}",
        "Accept": "application/json",
        "Content-Type": "application/json",
    }


def request_json(method: str, url: str, headers: dict[str, str], payload: dict | None = None) -> dict:
    data = None if payload is None else json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(url, data=data, method=method, headers=headers)
    with urllib.request.urlopen(request) as response:
        return json.loads(response.read().decode("utf-8"))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", required=True)
    parser.add_argument("--email", required=True)
    parser.add_argument("--api-token", required=True)
    parser.add_argument("--space-id", required=True)
    parser.add_argument("--title", required=True)
    parser.add_argument("--body-file", required=True)
    parser.add_argument("--parent-id")
    args = parser.parse_args()

    base_url = args.base_url.rstrip("/")
    headers = build_headers(args.email, args.api_token)
    page_body = Path(args.body_file).read_text(encoding="utf-8")

    query = urllib.parse.urlencode({"space-id": args.space_id, "title": args.title})
    pages_url = f"{base_url}/wiki/api/v2/pages?{query}"

    try:
        page_list = request_json("GET", pages_url, headers)
        results = page_list.get("results", [])
        body_payload = {"representation": "storage", "value": page_body}

        if results:
            page = results[0]
            page_id = page["id"]
            page_details = request_json(
                "GET",
                f"{base_url}/wiki/api/v2/pages/{page_id}",
                headers,
            )
            version_number = page_details["version"]["number"] + 1
            payload = {
                "id": str(page_id),
                "status": "current",
                "title": args.title,
                "body": body_payload,
                "version": {"number": version_number, "message": "Updated by Jenkins pipeline"},
            }
            request_json("PUT", f"{base_url}/wiki/api/v2/pages/{page_id}", headers, payload)
        else:
            payload = {
                "spaceId": str(args.space_id),
                "status": "current",
                "title": args.title,
                "body": body_payload,
            }
            if args.parent_id:
                payload["parentId"] = str(args.parent_id)
            request_json("POST", f"{base_url}/wiki/api/v2/pages", headers, payload)
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        print(f"Confluence API error: {exc.code} {body}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
