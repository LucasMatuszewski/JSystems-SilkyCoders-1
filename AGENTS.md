# Repository Guidelines

> **This file is the single source of truth for all AI agents working on this project.**
> It is accessible to Claude Code via a symlink: `CLAUDE.md → AGENTS.md`.
> **All procedures defined here are mandatory. Non-compliance blocks commits and task completion.**

**Primary references**: see `docs/PRD-Sinsay-PoC.md` and `docs/ADR-Sinsay-PoC.md` before making changes.

---

## Project Overview

This repo hosts a Proof of Concept for Sinsay returns/complaints verification using multimodal AI. The target flow is a form-to-chat experience where users submit order context and images, then receive a streamed verdict in Polish. The backend is Spring Boot + Spring AI; the frontend is React 19 + assistant-ui. Streaming must follow the Vercel AI SDK Data Stream Protocol.

---

## Current vs. Planned Structure

- **Current (in repo)**: single Spring Boot app under `src/` with Maven wrapper and minimal tests. React frontend under `Frontend/`.
- **Planned (ADR)**: a monorepo with separate `backend/` and `frontend/` directories and a build that bundles the frontend into the backend `static/` folder. If you introduce the monorepo layout, keep the existing root `pom.xml` aligned or migrate intentionally.

---

## Project Structure

```
├── src/                   # Spring Boot backend (Java / Maven)
│   ├── main/java/...      # Application source code
│   ├── main/resources/    # application.properties, static/, templates/
│   └── test/java/...      # JUnit tests
├── Frontend/              # React + Tailwind frontend (TypeScript / Vite)
├── docs/                  # PRD, ADR, and research notes
│   └── sinsay/            # Sinsay terms of returns and complaints (knowledge base)
├── pom.xml                # Maven build descriptor
├── AGENTS.md              # This file — root guidelines (all agents)
└── CLAUDE.md              # Symlink → AGENTS.md
```

**Specialist guidelines — read before working in each area:**

| Area                          | File                                       |
| ----------------------------- | ------------------------------------------ |
| Backend (Java / Spring Boot)  | [`src/AGENTS.md`](src/AGENTS.md)           |
| Frontend (React / TypeScript) | [`Frontend/AGENTS.md`](Frontend/AGENTS.md) |

### Target Modules (from ADR)

If/when split into modules, use the following layout and names:

- `backend/src/main/java/com/sinsay/`: `config/`, `controller/`, `service/`, `model/`
- `frontend/src/`: `app/` (screens), `components/ui/` (Shadcn), form + chat components

---

## Claude Code Tools and Resources

Claude Code has access to three categories of tools. **Agents must actively use these tools** to maximize code quality and development efficiency. Do not ignore available tools — their use is part of the standard workflow.

### Plugins (Enabled)

#### `code-review` — Automated PR Code Review

- **Command**: `/code-review`
- **What it does**: Launches 4 parallel agents to independently review a pull request — 2 agents check AGENTS.md/CLAUDE.md compliance, 1 scans for bugs in changed code, 1 analyzes git history for context. Issues are confidence-scored 0–100; only issues ≥80 are reported.
- **When to use**: Before merging any non-trivial PR. Run as the final step before marking a PR ready for merge.

#### `playwright` — Browser Automation and Visual Verification

- **What it does**: Provides browser automation tools (navigate, click, snapshot, screenshot, evaluate, network inspection) powered by Playwright MCP integration.
- **When to use**:
  - Frontend work: mandatory visual verification of every rendered page or component change
  - API testing: verify the running application responds correctly end-to-end
  - Self-code-review: open the running app and visually confirm the implementation before committing
- **Integration into workflow**: After any frontend change, use Playwright tools to open the page, take a screenshot, inspect the DOM, and confirm correctness before committing.

---

### MCP Servers (Model Context Protocol)

#### `jetbrains` — IntelliJ IDEA Integration

- **What it does**: Bridges Claude Code with IntelliJ IDEA 2025.3 — open files, navigate symbols, run configurations, access project structure, read diagnostics.
- **When to use**: When navigating the Java codebase, running Spring Boot, or checking live compiler errors before committing.
- **Note**: Requires IntelliJ IDEA to be running with the MCP server plugin active.

#### `context7` — Live Library Documentation

- **What it does**: Fetches up-to-date documentation for project libraries on demand. Avoids relying on potentially outdated training data.
- **When to use**: Whenever implementing features using a library below. Always fetch current docs before writing new integration code.

| Handler                                  | Library         |
| ---------------------------------------- | --------------- |
| `/websites/spring_io_projects_spring-ai` | Spring AI       |
| `/spring-projects/spring-boot`           | Spring Boot     |
| `/projectlombok/lombok`                  | Lombok          |
| `/openai/openai-java`                    | OpenAI Java SDK |
| `/websites/platform_openai`              | OpenAI Platform |
| `/vercel/ai`                             | Vercel AI SDK   |
| `/assistant-ui/assistant-ui`             | assistant-ui    |
| `/reactjs/react.dev`                     | React 19        |
| `/tailwindlabs/tailwindcss.com`          | Tailwind CSS    |
| `/shadcn-ui/ui`                          | Shadcn/ui       |

---

## General Quality Standards

These apply to **all code in this repository** regardless of language or layer.

- All new code must have corresponding tests (unit + integration as appropriate)
- Tests must cover happy path, boundary conditions, and at least one error/exception path
- No unused imports, dead code, or commented-out blocks
- Environment variables for all secrets; never hardcode credentials or API keys
- No `.db` database files or secret files committed to git
- Implementation must match the specification in `docs/PRD-Sinsay-PoC.md` and `docs/ADR-Sinsay-PoC.md`

For layer-specific quality standards, see `src/AGENTS.md` or `Frontend/AGENTS.md`.

---

## Test-Driven Development (Mandatory)

**TDD is not optional for any layer of this project.** Follow this process for every feature or bug fix:

1. **Read the spec** — read the relevant section of `docs/PRD-Sinsay-PoC.md` and/or `docs/ADR-Sinsay-PoC.md` before writing any code
2. **Design the tests** — list all behaviors, inputs, outputs, and error states as test method names before writing any test body
3. **Write failing tests first** — complete test files must exist before any production code
4. **Confirm tests fail** — run the test suite; if tests pass at this stage, they test nothing
5. **Implement minimum production code** — write only what is needed to make tests pass
6. **Confirm all tests pass** — run the full test suite; fix the implementation, not the tests
7. **Refactor** — clean up while keeping all tests green

For language-specific TDD commands and tooling, see `src/AGENTS.md` (backend) or `Frontend/AGENTS.md` (frontend).

---

## Universal Task Completion Criteria

A task is **complete** only when **all** of the following are true, regardless of whether it is frontend or backend work:

1. **Tests written first** — test files exist and were written before production code
2. **All tests pass** — the full test suite exits with zero failures and zero errors
3. **Implementation matches specification** — verified against PRD/ADR, not just "code looks right"
4. **No regressions** — the full test suite passes, not just the new tests
5. **Build is clean** — compiles/transpiles with no errors or warnings
6. **Security verified** — no secrets committed, no unsafe operations
7. **Commit message is correct** — follows `Area: short summary` convention (e.g., `Feature: add chat SSE endpoint`)

**If any criterion is not met, the task is NOT complete.** Do not mark it done or move to the next task.

---

## Universal Pre-Commit Checklist

**Every commit must pass this checklist. Do not commit if any item is unchecked.**

```
□ I read the PRD for the changed area before starting
□ Tests were written BEFORE implementing production code (TDD)
□ All new code has corresponding test files
□ All tests pass (zero failures, zero errors)
□ Build is clean (no compiler/transpiler errors or warnings)
□ No hardcoded secrets, API keys, or credentials in any file
□ No .db or secret files staged for commit
□ /code-review run on the changes before committing (for non-trivial changes)
□ Context7 MCP used to check latest docs for any library API used
□ Commit message follows: Area: short summary (e.g., `Feature: add chat SSE endpoint`)
```

For layer-specific checklist items, see `src/AGENTS.md` (backend) or `Frontend/AGENTS.md` (frontend).

---

## Commit & Pull Request Guidelines

- Follow `Area: short summary` convention (e.g., `Docs:`, `Feature:`, `Fix:`)
- PRs must include: goal, scope of changes, and any required setup notes (env vars, API keys)
- Run `/code-review` before marking a PR ready for review

---

## Security & Configuration

- SQLite DB files must remain local-only; never commit `.db` files
- No API keys, passwords, or tokens in any source or test file

---

## Agent Workflow

1. Read the specialist AGENTS.md for the area you are working in (`src/AGENTS.md` or `Frontend/AGENTS.md`)
2. Keep changes aligned with the PoC scope (no auth, no production deployment)
3. Use Context7 MCP to fetch current library documentation before implementing new integrations
4. Use the JetBrains MCP for IDE-level navigation and diagnostics when available
5. Run `/code-review` on all non-trivial PRs
6. When adding new structure (backend/frontend), update the relevant AGENTS.md files accordingly
7. After every correction, end with: Update your AGENTS.md so you don't make that mistake again.
8. Create a new commit after each step, make commits granular and often with descriptive messages.
9. Do not push changes to remote! Let user check changes locally.

---

## Symlink

This file is the single source of truth. Claude Code accesses it through a symlink:

```bash
# Run once from the project root to create the symlink:
ln -s AGENTS.md CLAUDE.md
```

Do not maintain a separate `CLAUDE.md` file — the symlink ensures both agents and Claude Code read identical content.
