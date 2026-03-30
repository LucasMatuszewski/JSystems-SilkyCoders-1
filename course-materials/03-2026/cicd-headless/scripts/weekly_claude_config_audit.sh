#!/usr/bin/env bash
set -euo pipefail

declare -a TARGETS=()

for file in CLAUDE.md AGENTS.md .mcp.json .claude/settings.json; do
  if [[ -f "$file" ]]; then
    TARGETS+=("$file")
  fi
done

if [[ -d ".claude/rules" ]]; then
  while IFS= read -r file; do
    TARGETS+=("$file")
  done < <(find .claude/rules -type f | sort)
fi

if [[ -d ".claude/skills" ]]; then
  while IFS= read -r file; do
    TARGETS+=("$file")
  done < <(find .claude/skills -type f | sort)
fi

if [[ "${#TARGETS[@]}" -eq 0 ]]; then
  echo "No Claude config files found. Exiting."
  exit 0
fi

printf '%s\n' "${TARGETS[@]}" > .claude-config-targets.txt

PROMPT=$(cat <<'EOF'
Audit and, if needed, update the repository's Claude-related configuration files.

Goals:
1. Remove outdated or duplicated instructions.
2. Update stale links or command names when they are clearly obsolete.
3. Preserve project-specific workflow rules that still make sense.
4. Keep the files concise and internally consistent.
5. Create or update CLAUDE_CONFIG_AUDIT.md with:
   - what changed,
   - why it changed,
   - what still needs manual review.

Hard constraints:
- Only edit files listed in .claude-config-targets.txt and CLAUDE_CONFIG_AUDIT.md.
- Do not modify application source code, tests, or CI files.
- Prefer the smallest safe edit set.
- If no changes are needed, still update CLAUDE_CONFIG_AUDIT.md with a short "no changes needed" note.
EOF
)

claude --bare -p "$PROMPT" \
  --allowedTools "Read,Edit,Write" \
  --max-turns 8
