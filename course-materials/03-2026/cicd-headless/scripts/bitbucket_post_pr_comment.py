#!/usr/bin/env python3
import argparse
import base64
import json
import sys
import urllib.error
import urllib.request
from pathlib import Path


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--workspace", required=True)
    parser.add_argument("--repo-slug", required=True)
    parser.add_argument("--pull-request-id", required=True)
    parser.add_argument("--username", required=True)
    parser.add_argument("--app-password", required=True)
    parser.add_argument("--comment-file", required=True)
    args = parser.parse_args()

    comment_text = Path(args.comment_file).read_text(encoding="utf-8")
    payload = json.dumps({"content": {"raw": comment_text}}).encode("utf-8")

    auth = base64.b64encode(f"{args.username}:{args.app_password}".encode("utf-8")).decode("ascii")
    url = (
        f"https://api.bitbucket.org/2.0/repositories/"
        f"{args.workspace}/{args.repo_slug}/pullrequests/{args.pull_request_id}/comments"
    )

    request = urllib.request.Request(
        url,
        data=payload,
        method="POST",
        headers={
            "Authorization": f"Basic {auth}",
            "Content-Type": "application/json",
            "Accept": "application/json",
        },
    )

    try:
        with urllib.request.urlopen(request) as response:
            if response.status not in (200, 201):
                raise RuntimeError(f"Bitbucket API returned unexpected status {response.status}")
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        print(f"Failed to post Bitbucket PR comment: {exc.code} {body}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
