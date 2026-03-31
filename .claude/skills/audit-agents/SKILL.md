---
name: audit-agents
description: Audit and maintain agent configuration files (.claude/agents, AGENTS.md, CLAUDE.md, agent-memory). Checks for duplicates, outdated info, naming conflicts, wrong technical facts, and files over 200 lines. Fixes all issues and commits.
---

# Audit & Maintain Agent Configuration Files

You are performing a systematic audit and maintenance pass on all agent configuration files.
Apply all fixes directly — do not just report issues without fixing them.

## Files to Audit

**Agent system prompts:**
- `.claude/agents/*.md` — one per agent role

**Agent memory files:**
- `.claude/agent-memory/*/MEMORY.md` — persistent cross-session memory per agent

**Project guidelines (AGENTS.md / CLAUDE.md):**
- `AGENTS.md` (root)
- `frontend/AGENTS.md`
- `backend/AGENTS.md`
- `frontend/tests/e2e/AGENTS.md`
- `frontend/tests/CLAUDE.md`
- Any other `AGENTS.md` or `CLAUDE.md` files in the repo (`Glob **/AGENTS.md` and `Glob **/CLAUDE.md`)

---

## Audit Checklist

### 1. Naming conflicts
- Read frontmatter of every `.claude/agents/*.md`
- Each `name:` value must be unique. If two files share a name, the duplicate (less complete one) must be removed or renamed.

### 2. Stale/wrong technical facts
Compare documented facts against actual source files:
- SSE streaming format: read `backend/src/main/java/com/sinsay/service/SseStreamEncoder.java` or `ChatService.java` to confirm the actual event format (`text/event-stream`, Vercel UI Message Stream `{"type":"text-delta"}`) and check it matches what agent files say.
- Package names, class names, file paths: spot-check against real files using Glob/Grep.
- Tech stack versions: check `backend/pom.xml` and `frontend/package.json`.
- MCP tools referenced (e.g., IntelliJ MCP): verify they are listed in the agent's frontmatter `mcpServers`. Remove references to MCPs that are not configured.

### 3. Outdated boilerplate in agent files
- Remove any "MEMORY.md is currently empty" statement from agent system prompts. The memory system auto-injects the real MEMORY.md content — this placeholder is always wrong and misleading.
- The persistent memory instructions (Guidelines, What to save, What NOT to save, Explicit user requests) are already part of Claude Code's built-in behavior when `memory: project` is set. Do NOT duplicate this ~35-line block in every agent file. Remove it from agent files.

### 4. Duplicate information across files
- If the same information appears verbatim (or near-verbatim) in both a root `AGENTS.md` and an agent system prompt, keep it in one place only:
  - Agent system prompts should hold agent-specific workflow and conventions.
  - AGENTS.md files should hold file/location facts, tech stack, and API contracts.
- Do not repeat SSE format specs in both `AGENTS.md` and agent files if they say the same thing.
- Do not repeat verification commands in both root `AGENTS.md` and agent files if they are identical.

### 5. Information already in Claude's built-in system prompt
The following is provided by Claude Code itself and should NOT be repeated in agent files:
- Generic commit rules ("one logical change per commit", "do not push unless asked")
- Generic TDD cycle description (write → fail → implement → pass → refactor)
- Generic Context7 usage instructions (resolve-library-id + query-docs)
- Generic git conventions

Keep only **project-specific** variants (e.g., commit message format `Backend: summary`).

### 6. Missing critical context
Every agent file should contain the minimum context to start working without reading other docs:
- **be-developer**: package root, SSE format, key service names, how to run tests
- **fe-developer**: component structure, Chat integration pattern, Vite proxy/build config, Vitest setup
- **qa-engineer**: test locations, stack (real backend, no mocks), how to run Playwright, image paths

If an agent file is only memory boilerplate with no project context, add the essential context.

### 7. File length
- Agent system prompts and AGENTS.md files should be under 200 lines.
- If a file exceeds 200 lines, remove duplication and boilerplate first. If still too long, extract secondary detail to a linked file (e.g., `docs/ADR/`) and reference it.

### 8. AGENTS.md scope
- Root `AGENTS.md`: project overview, commands, global workflow rules.
- `backend/AGENTS.md`: backend-only facts (tech stack, package structure, API contracts, SSE format, testing patterns).
- `frontend/AGENTS.md`: frontend-only facts (component structure, chat integration, Vite config, testing).
- `frontend/tests/e2e/AGENTS.md`: E2E-only facts (Playwright setup, image loading, no-mock rule, QA workflow phases).
- Each file should be self-contained for its scope — an agent working in that directory should not need to read parent files for routine tasks.

### 9. Orphaned memory files
- Check that each directory under `.claude/agent-memory/` has a corresponding `.claude/agents/*.md` file.
- If memory exists in an unexpected location (e.g., `backend/.claude/agent-memory/`), determine whether it's the correct location or a duplicate. The canonical location is `{project-root}/.claude/agent-memory/{agent-name}/MEMORY.md`.

---

## Fix Strategy

1. Read all files listed above in parallel.
2. Build a list of all issues found with file + line reference.
3. For each issue, apply the fix directly using Edit or Write tools.
4. After all fixes: re-read modified files and confirm no new issues were introduced.
5. Run a final line-count check on all modified files.
6. Commit: `Docs: audit and update agent configuration files`

---

## Output Format

After completing all fixes, produce a concise summary:

```
## Agent Audit Summary

### Fixed
- [file] issue description → fix applied

### No Action Needed
- [file] reason

### Manual Review Required
- [file] issue that could not be auto-fixed
```
