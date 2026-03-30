# Weekly Claude Config Curation Prompt

Use this with Claude Code Cloud scheduled tasks or `/schedule` if that feature is available in your environment.

## Prompt

```text
Review this repository's Claude-related configuration and keep it healthy.

Scope:
- CLAUDE.md
- AGENTS.md
- .mcp.json
- .claude/settings.json
- .claude/rules/**
- .claude/skills/**

Tasks:
1. Remove duplicated or contradictory instructions.
2. Update stale links, renamed commands, or outdated library references when they are clearly obsolete.
3. Preserve project-specific workflow rules that still make sense.
4. Keep files concise and easier for agents to follow.
5. Create or update CLAUDE_CONFIG_AUDIT.md with:
   - what changed,
   - why it changed,
   - what still needs human review.
6. Open a PR with a short summary.

Hard constraints:
- Do not edit application source code, test code, or deployment manifests.
- Do not weaken repository-specific rules just to make files shorter.
- If no changes are needed, still create/update CLAUDE_CONFIG_AUDIT.md with "no changes needed".
- Use official docs when checking whether a Claude feature or command name changed.
```

## What to say on the training

- `/loop` is session-scoped and not durable.
- Claude docs explicitly say durable scheduling should use Cloud scheduled tasks, Desktop scheduled tasks, or GitHub Actions.
- Cloud scheduled tasks run in the cloud on a fresh clone and do not need your laptop to stay on.
- I have not verified a public, step-by-step admin workflow for forcing one org-wide weekly task across all repos, so I would present GitHub Actions as the team-safe default.
