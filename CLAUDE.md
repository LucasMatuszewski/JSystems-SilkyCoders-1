# Repository Guidelines

> **This file is the single source of truth for all AI agents working on this project.**
> **All procedures defined here are mandatory. Non-compliance blocks commits and task completion.**

**Primary references**: when you need details about the project see:

- `docs/PRD-Sinsay-PoC.md` - Product Requirement Document (functional requirements)
- `docs/ADR-Sinsay-PoC.md` - Architecture Decision Record (technical decisions)

---

## Project Overview

This repo hosts a Proof of Concept for Sinsay (fashion brand) returns/complaints verification using multimodal AI. The target flow is a chat-to-form-to-chat experience where users start conversation, if they want to submit a return or complaint, agent shows them the form to submit order context and images, then agent sends a streamed verdict in chat again where user can continue the conversation. The backend is Spring Boot (WebFlux) + LangGraph4j + Spring AI with AG-UI protocol; the frontend is Next.js 16 + CopilotKit UI (based on AG-UI Protocol to let agent communicate with frontend and display UI components).

---

## Tech Stack Summary

| Layer    | Technologies                                                                                          |
| -------- | ----------------------------------------------------------------------------------------------------- |
| Backend  | Spring Boot 3.5.9 · Java 21 · Maven · Spring WebFlux · LangGraph4j 1.6.2 · Spring AI 1.0.0 · Ollama |
| Frontend | Next.js 16 · React 19 · TypeScript · Tailwind CSS 4 · CopilotKit 1.51 · pnpm                         |
| Protocol | AG-UI (Agent-UI) via SSE · CopilotKit runtime adapter · LangGraphHttpAgent                            |

---

## Current vs. Planned Structure

- **Current (in repo)**: Spring Boot reactive app under `src/` with Maven wrapper and tests. Next.js frontend under `frontend/`.
- **Planned (ADR)**: a monorepo with a build that bundles the frontend into the backend `static/` folder.

---

## Project Structure

```
├── src/                           # Spring Boot backend (Java / Maven)
│   ├── main/java/
│   │   ├── com/silkycoders1/jsystemssilkycodders1/   # App entry point + bean config
│   │   └── org/bsc/langgraph4j/agui/                 # AG-UI protocol: agents, events, SSE controller
│   ├── main/resources/
│   │   └── application.properties                    # Server port, model config, agent selection
│   └── test/java/                                    # JUnit tests (mirrored packages)
├── frontend/                      # Next.js + CopilotKit frontend (TypeScript / pnpm)
│   ├── src/app/                   # Next.js App Router pages and components
│   │   ├── api/langgraph4j/       # API route proxying to Spring Boot backend
│   │   └── component/             # Chat UI components (CopilotKit)
│   └── package.json               # pnpm project config
├── docs/                          # PRD, ADR, and research notes
│   └── sinsay/                    # Sinsay terms of returns and complaints (knowledge base)
├── pom.xml                        # Maven build descriptor
└── CLAUDE.md                      # This file — root guidelines (all agents)
```

**Specialist guidelines — read before working in each area:**

| Area                          | File                                         |
| ----------------------------- | -------------------------------------------- |
| Backend (Java / Spring Boot)  | [`src/CLAUDE.md`](src/CLAUDE.md)             |
| Frontend (React / TypeScript) | [`frontend/CLAUDE.md`](frontend/CLAUDE.md)   |

---

## Claude Code Tools and Resources

Claude Code has access to three categories of tools: plugins, MCP servers, and skills.
**Agents must actively use these tools** to maximize code quality and development efficiency.
Do not ignore available tools — their use is part of the standard workflow.

### Plugins (Enabled)

#### `code-review` — Automated PR Code Review

- **Command**: `/code-review`
- **What it does**: Launches 4 parallel agents to independently review a pull request — 2 agents check CLAUDE.md compliance, 1 scans for bugs in changed code, 1 analyzes git history for context. Issues are confidence-scored 0–100; only issues ≥80 are reported.
- **When to use**: Before merging any non-trivial PR. Run as the final step before marking a PR ready for merge.

> MCP Servers and Skills are described in `src/CLAUDE.md` and `frontend/CLAUDE.md` respectively.

---

## General Quality Standards

These apply to **all code in this repository** regardless of language or layer.

- All new code must have corresponding tests (unit + integration as appropriate)
- Tests must cover happy path, boundary conditions, and at least one error/exception path
- No unused imports, dead code, or commented-out blocks
- Environment variables for all secrets; never hardcode credentials or API keys
- No `.db` database files or secret files committed to git
- Implementation must match the specification in `docs/PRD-Sinsay-PoC.md` and `docs/ADR-Sinsay-PoC.md`

For layer-specific quality standards, see `src/CLAUDE.md` or `frontend/CLAUDE.md`.

---

## Test-Driven Development (Mandatory)

**TDD is not optional for any layer of this project.** Follow this process for every feature or bug fix:

1. **Read the spec** — read the relevant section of `docs/PRD-Sinsay-PoC.md` and/or `docs/ADR-Sinsay-PoC.md` before writing any code
2. **Design the tests** — list all behaviors, inputs, outputs, and error states as test method names before writing any test body
3. **Write failing tests first** — complete test files must exist before any production code
4. **Confirm tests fail** — run the test suite; if tests pass at this stage, they test nothing
5. **Implement minimum production code** — write only what is needed to make tests pass
6. **Confirm all tests pass** — run the full test suite; fix the implementation, not the tests
7. **Prove test honesty** — see "Test Honesty Verification" below; skip this and the task is not complete
8. **Refactor** — clean up while keeping all tests green

For language-specific TDD commands and tooling, see `src/CLAUDE.md` (backend) or `frontend/CLAUDE.md` (frontend).

---

## Test Honesty Verification (Mandatory — Not Optional)

This project has a documented problem: agents make tests pass by weakening them instead of fixing the code. This is called "specification gaming" and it destroys the value of the test suite. **Every agent working on this project must follow these rules.**

### Prove your tests are honest

After all tests pass (step 6 of TDD), do this for every new or modified test:

**Step 1 — Sabotage your own code**: Comment out, rename, or intentionally break the production code you just wrote. Run the tests. **They must fail.** If they pass with your code removed, the tests don't actually test your implementation — delete them and write real ones.

**Step 2 — Name the failure**: For each test, write one sentence: "This test would fail if [specific production code change]." If you can't complete this sentence, the test is not testing anything specific.

**Step 3 — Test the failure path**: For every success-path test, write at least one failure-path test. If your feature handles AI model errors, write a test where the mock throws an error and verify the right error handling fires. A feature with only happy-path tests is half-tested.

### Forbidden actions — these are test dishonesty

**You are NEVER allowed to do any of the following to make a test pass:**
- Change an expected value to match wrong output (`assertThat(x).isEqualTo("wrong")` → `assertThat(x).isEqualTo("also-wrong")`)
- Remove an assertion that was catching a real bug
- Add `|| alternative` conditions to assertions that hide the real failure
- Increase a timeout to mask slow or flaky behavior
- Mock away the component you are supposed to be testing
- Skip or ignore a test case without leaving a comment explaining why and a tracking issue

**If a test is failing, there are exactly two acceptable responses:**
1. Fix the production code so the test passes (preferred — the code is wrong)
2. Delete the test entirely and rewrite it because the requirements changed (rare)

**Changing the test to match broken behavior is never acceptable.** If you find yourself wanting to change a test assertion, ask "is the code wrong or is the test wrong?" If the code is wrong, fix the code.

---

## Universal Task Completion Criteria

A task is **complete** only when **all** of the following are true, regardless of whether it is frontend or backend work:

1. **Tests written first** — test files exist and were written before production code
2. **All tests pass** — the full test suite exits with zero failures and zero errors
3. **E2E tests pass** — if the change touches any user-visible behavior: run `cd frontend && pnpm e2e` with the full stack running (backend + Ollama + frontend). Tests must pass with a live backend, not just mocks. A task is NOT complete if E2E tests are skipped.
4. **Implementation matches specification** — verified against PRD/ADR, not just "code looks right"
5. **No regressions** — the full test suite passes, not just the new tests
6. **Build is clean** — compiles/transpiles with no errors or warnings
7. **Security verified** — no secrets committed, no unsafe operations
8. **Commit message is correct** — follows `Area: short summary` convention (e.g., `Feature: add chat SSE endpoint`)

**If any criterion is not met, the task is NOT complete.** Do not mark it done or move to the next task.

> **Critical**: "All tests pass" means the unit/integration test suite. It does NOT mean the app works. Unit tests use mocks and cannot detect runtime failures. E2E tests are the only verification that the actual running app works correctly. Both are required.

---

## Universal Pre-Commit Checklist

**Every commit must pass this checklist. Do not commit if any item is unchecked.**

```
□ I read the PRD for the changed area before starting
□ Context7 MCP used to check latest docs for any library API used
□ Tests were written BEFORE implementing production code (TDD)
□ All new code has corresponding test files
□ All tests pass (zero failures, zero errors)
□ E2E tests run with full stack (pnpm e2e) — for any user-visible change
□ Build is clean (no compiler/transpiler errors or warnings)
□ No hardcoded secrets, API keys, or credentials in any file
□ No .db or secret files staged for commit
□ Code formatting is aligned with the project conventions
□ /code-review run on the changes before committing (for non-trivial changes)
□ Commit message follows: Area: short summary (e.g., `Feature: add chat SSE endpoint`)
```

For layer-specific checklist items, see `src/CLAUDE.md` (backend) or `frontend/CLAUDE.md` (frontend).

---

## Commit & Pull Request Guidelines

- Follow `Area: short summary` convention (e.g., `Docs:`, `Feature:`, `Fix:`)
- PRs must include: goal, scope of changes, and any required setup notes (env vars, API keys)
- Run `/code-review` before marking a PR ready for review

---

## Security & Configuration

- H2 database files (`*.mv.db`) and the `data/` directory must remain local-only; never commit database files
- No API keys, passwords, or tokens in any source or test file
- AI model API keys are read from environment variables (`OPENAI_API_KEY`, `GITHUB_MODELS_TOKEN`) or use local Ollama (no key needed)

---

## Agent Workflow

1. Read the specialist CLAUDE.md for the area you are working in (`src/CLAUDE.md` or `frontend/CLAUDE.md`)
2. Keep changes aligned with the PoC scope (no auth, no production deployment)
3. Use Context7 MCP to fetch current library documentation before implementing new integrations
4. Use the JetBrains MCP for IDE-level navigation and diagnostics when available
5. Run `/code-review` on all non-trivial PRs
6. Before committing, always run linting and formatting fix (auto formatting)
7. When adding new structure (backend/frontend), update the relevant CLAUDE.md files accordingly
8. After every correction, update your CLAUDE.md so you don't make that mistake again.
9. Create a new commit after each step, make commits granular and often with descriptive messages.
10. Do not push changes to remote! Let user check changes locally.
11. When you discover outdated, incorrect, or misaligned information in any project file (docs, config, code comments), remove or correct it directly. Do not document the change inside the file itself — report what was changed in the chat window only. All project files must remain internally consistent with no historical notes, divergence annotations, or correction markers left in the content.

---

## Logs and Screenshots

### Log Files
- Application logs are written to `logs/app.log` (daily rolling, git-ignored)
- **Never load the full log file into context** — it can be 10k+ lines with large stack traces
- Always use one of these approaches instead:
  ```bash
  tail -200 logs/app.log           # last 200 lines — start here
  grep -n "ERROR\|WARN" logs/app.log | grep -v "at org\.\|at java\." | tail -100  # errors only, no stack trace noise
  grep -n "keyword" logs/app.log | tail -50   # targeted search
  ```
- Stack traces in logs are long. If you need one, identify the first ERROR line then read ±20 lines around it

### Screenshots
- Playwright screenshots go to: `frontend/src/e2e/screenshots/` (git-ignored)
- Always save screenshots to that folder — NOT to `/tmp/`
- Playwright failure screenshots: configured via `playwright.config.ts` (`outputDir`)
- After any frontend change: take a screenshot and save it to `frontend/src/e2e/screenshots/` for human review

---

## Sub-Agent Session Evaluation (Mandatory for Coordinating Agents)

After any task delegated to a sub-agent completes, the coordinating agent **must** evaluate the sub-agent's decisions before accepting the work. Read the sub-agent's memory file and the code it produced.

### What to Check (checklist — run after every delegated task)

**Test gaming — the most critical failure mode:**
- Did the sub-agent change an expected value to match wrong output?
- Did the sub-agent remove an assertion or add `|| fallback` to make a test pass?
- Did the sub-agent mock away the thing it was supposed to be testing?
- Did tests pass before the production code existed? (sign: test written after code, or mock always returns success regardless of input)
- Are all mocks returning success only? Is there even ONE mock that simulates the failure path?

**Coverage gaps:**
- For every AI model call in the feature: is there a test where the model throws? Does that test verify the user sees the right error?
- Is there an E2E Playwright test that exercises the full feature with the real stack?
- Can any test be satisfied by an empty implementation? (if so, it's useless)

**Process violations:**
- Did the sub-agent run E2E tests? Quote the output — not just "I ran the tests"
- Did tests come before production code? Check the git history if uncertain
- Did the sub-agent verify the running app actually works (not just that tests pass)?

**Code quality:**
- Were exceptions swallowed (empty catch, log and ignore)?
- Are error messages user-visible? (Backend errors in Polish, shown to user in frontend)
- Does the implementation actually handle the failure scenario described in the task?

### What to Do When Issues Are Found
1. Fix the bad test or code BEFORE accepting the work — do not merge hollow tests
2. Update the relevant agent's instructions (`.claude/agents/`) to prevent the same mistake next time
3. If the pattern is project-wide, update this file

> **Note**: This evaluation is not bureaucracy — it is the only reliable way to catch specification gaming before it accumulates. A passing test suite that doesn't catch real failures is more dangerous than no tests at all, because it provides false confidence.
