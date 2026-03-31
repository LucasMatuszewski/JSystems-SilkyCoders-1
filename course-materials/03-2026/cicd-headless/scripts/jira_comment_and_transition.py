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
        if response.status == 204:
            return {}
        return json.loads(response.read().decode("utf-8"))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", required=True)
    parser.add_argument("--email", required=True)
    parser.add_argument("--api-token", required=True)
    parser.add_argument("--issue-key", required=True)
    parser.add_argument("--comment-file", required=True)
    parser.add_argument("--transition-name", required=True)
    args = parser.parse_args()

    base_url = args.base_url.rstrip("/")
    headers = build_headers(args.email, args.api_token)
    comment_text = Path(args.comment_file).read_text(encoding="utf-8")

    comment_url = f"{base_url}/rest/api/3/issue/{urllib.parse.quote(args.issue_key)}/comment"
    transitions_url = f"{base_url}/rest/api/3/issue/{urllib.parse.quote(args.issue_key)}/transitions"

    try:
        request_json("POST", comment_url, headers, {"body": comment_text})
        transitions = request_json("GET", transitions_url, headers)
        transition_id = None
        for transition in transitions.get("transitions", []):
            if transition.get("name") == args.transition_name:
                transition_id = transition.get("id")
                break
        if transition_id is None:
            raise RuntimeError(f"Transition '{args.transition_name}' not available for {args.issue_key}")

        request_json("POST", transitions_url, headers, {"transition": {"id": transition_id}})
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        print(f"Jira API error: {exc.code} {body}", file=sys.stderr)
        return 1
    except Exception as exc:
        print(str(exc), file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
