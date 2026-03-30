#!/usr/bin/env bash
set -euo pipefail

BASE_BRANCH="${1:?Usage: claude_pr_review.sh <base-branch> [output-dir]}"
OUTPUT_DIR="${2:-.claude-ci}"

mkdir -p "$OUTPUT_DIR"

git fetch origin "$BASE_BRANCH" --depth=1
git diff --merge-base "origin/$BASE_BRANCH" HEAD > "$OUTPUT_DIR/pr.diff"

if [[ ! -s "$OUTPUT_DIR/pr.diff" ]]; then
  printf '{"result":"## Summary\\n\\nNo changes to review.\\n"}\n' > "$OUTPUT_DIR/review.json"
else
  REVIEW_PROMPT=$(cat <<'EOF'
Review the provided pull request diff.

Return Markdown only with these sections:
## Blocking issues
## Non-blocking issues
## Missing tests
## Summary

Rules:
- Focus on correctness, regressions, security, weak assertions, and contract mismatches.
- Do not suggest speculative refactors or style-only changes.
- If no issues exist in a section, say "None".
EOF
)

  claude --bare -p "$REVIEW_PROMPT" \
    --output-format json \
    --max-turns 3 \
    < "$OUTPUT_DIR/pr.diff" > "$OUTPUT_DIR/review.json"
fi

python3 - "$OUTPUT_DIR/review.json" "$OUTPUT_DIR/review.md" <<'PY'
import json
import sys
from pathlib import Path

review_json = Path(sys.argv[1])
review_md = Path(sys.argv[2])

payload = json.loads(review_json.read_text(encoding="utf-8"))
result = payload.get("result", "").strip()
if not result:
    result = "## Summary\n\nClaude returned an empty review.\n"
review_md.write_text(result + "\n", encoding="utf-8")
PY
