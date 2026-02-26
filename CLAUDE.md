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
7. **Refactor** — clean up while keeping all tests green

For language-specific TDD commands and tooling, see `src/CLAUDE.md` (backend) or `frontend/CLAUDE.md` (frontend).

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
□ Context7 MCP used to check latest docs for any library API used
□ Tests were written BEFORE implementing production code (TDD)
□ All new code has corresponding test files
□ All tests pass (zero failures, zero errors)
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

- SQLite DB files must remain local-only; never commit `.db` files
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
8. After every correction, end with: Update your CLAUDE.md so you don't make that mistake again.
9. Create a new commit after each step, make commits granular and often with descriptive messages.
10. Do not push changes to remote! Let user check changes locally.
