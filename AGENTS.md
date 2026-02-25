# Repository Guidelines

// TODO: split to multiple smaller files with granural control over folders where these filess will be.
// Only information **VERY** relevant to both FE/BE should be here, that we always want to be used by Agents (always auto delivered).
// TODO: Make Symlink to .juni/guidelines.md & to CLAUDE.md
// TODO: split like Cursor (patterns), sniper context delivery exactly where needed
// TODO: separate AGENTS.md in Java /src folder (since in root we have JAva app) or move whole BE app to /backend folder?)

> **This file is the single source of truth for all AI agents working on this project.**
> It is accessible to Claude Code via a symlink: `CLAUDE.md → AGENTS.md`.
> **All procedures defined here are mandatory. Non-compliance blocks commits and task completion.**

**Primary references**: see `docs/PRD-Sinsay-PoC.md` and `docs/ADR-Sinsay-PoC.md` before making changes.

---

## Project Overview

This repo hosts a Proof of Concept for Sinsay returns/complaints verification using multimodal AI. The target flow is a form-to-chat experience where users submit order context and images, then receive a streamed verdict in Polish. The backend is Spring Boot + Spring AI; the frontend is React 19 + assistant-ui. Streaming must follow the Vercel AI SDK Data Stream Protocol.

## Current vs. Planned Structure

- **Current (in repo)**: single Spring Boot app under `src/` with Maven wrapper and minimal tests.
- **Planned (ADR)**: a monorepo with separate `backend/` and `frontend/` directories and a build that bundles the frontend into the backend `static/` folder. If you introduce the monorepo layout, keep the existing root `pom.xml` aligned or migrate intentionally.

## Project Structure & Module Organization

- `src/main/java/com/silkycoders1/jsystemssilkycodders1/`: Spring Boot entry point.
- `src/main/resources/`: `application.properties`, plus `static/` and `templates/` (frontend build output targets `static/`).
- `src/test/java/com/silkycoders1/jsystemssilkycodders1/`: JUnit tests.
- `docs/`: PRD, ADR, and research notes used as requirements.
- `docs/sinay/`: terms of returns and complaints from Sinsay as input for AI Agent system prompt / knowledge base.
- `Frontend/`: React + Tailwind frontend (see `Frontend/AGENTS.md` for frontend-specific rules).

### Target Modules (from ADR)

If/when split into modules, use the following layout and names:

- `backend/src/main/java/com/sinsay/`: `config/`, `controller/`, `service/`, `model/`.
- `frontend/src/`: `app/` (screens), `components/ui/` (Shadcn), form + chat components.

---

## Claude Code Tools and Resources

Claude Code has access to three categories of tools. **Agents must actively use these tools** to maximize code quality and development efficiency. Do not ignore available tools — their use is part of the standard workflow.

### Plugins (Enabled)

Plugins extend Claude Code with slash commands and specialized agents.

#### `code-review` — Automated PR Code Review

- **Command**: `/code-review`
- **What it does**: Launches 4 parallel agents to independently review a pull request — 2 agents check AGENTS.md/CLAUDE.md compliance, 1 scans for bugs in changed code, 1 analyzes git history for context. Issues are confidence-scored 0–100; only issues ≥80 are reported to minimize false positives.
- **When to use**:
  - Before merging any non-trivial PR
  - After implementing a feature or fixing a bug to catch regressions
  - Whenever the task involves changes to critical paths (SSE adapter, AI model calls, persistence)
- **Integration into workflow**: Run `/code-review` as the final step before marking a PR ready for merge.

#### `playwright` — Browser Automation and Visual Verification

- **What it does**: Provides browser automation tools (navigate, click, snapshot, screenshot, evaluate, network inspection) powered by Playwright MCP integration.
- **When to use**:
  - Frontend work: mandatory visual verification of every rendered page or component change
  - API testing: verify the running application responds correctly end-to-end
  - DOM inspection: analyze component structure, accessibility tree, layout
  - Self-code-review: open the running app and visually confirm the implementation looks and behaves correctly before approving work
- **Integration into workflow**: After any frontend change, use Playwright tools to open the page, take a screenshot, inspect the DOM, and confirm correctness before committing.

---

### MCP Servers (Model Context Protocol)

MCP servers give Claude Code access to live, external data sources and IDE integrations.

#### `jetbrains` — IntelliJ IDEA Integration

- **What it does**: Bridges Claude Code with IntelliJ IDEA 2025.3, exposing IDE capabilities: open files, navigate symbols, run configurations, access project structure, and read diagnostics.
- **When to use**:
  - When navigating large Java codebases and needing accurate symbol resolution
  - When running or debugging Spring Boot from the IDE context
  - When checking live compiler errors or warnings before committing
- **Note**: Requires IntelliJ IDEA to be running with the MCP server plugin active.

#### `context7` — Live Library Documentation

- **What it does**: Fetches up-to-date documentation for project libraries on demand. Avoids relying on potentially outdated training data.
- **When to use**: Whenever implementing features using a library from the list below. Always fetch current docs before writing new integration code.
- **Available documentation handlers**:

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

### Skills

Skills are loaded knowledge packs that prime Claude Code with deep expertise in a specific domain. Invoke them when working in their target area.

#### `java-spring-boot` — Spring Boot Development

- **Invocation**: `Skill("java-spring-boot")`
- **Covers**: REST APIs with Spring MVC/WebFlux, Spring Security (OAuth2/JWT), Spring Data JPA, Actuator monitoring, Spring Cloud integration, production-ready configuration patterns.
- **When to use**: At the start of any task involving backend Spring Boot code — controllers, services, security config, data access, or application configuration.
- **Security note**: This skill has `Bash` tool access. Review any shell commands it suggests before execution.

#### `java-testing` — Java Testing

- **Invocation**: `Skill("java-testing")`
- **Covers**: JUnit 5, Mockito, AssertJ, Testcontainers, Spring Boot Test slices (`@WebMvcTest`, `@DataJpaTest`), MockMvc, TDD patterns, test data builders, JaCoCo coverage.
- **When to use**: When writing any test class — unit tests, integration tests, or API tests for the backend.
- **Security note**: Flagged as **High Risk by Gen Agent Trust Hub** (see security analysis in `docs/`). Analysis confirmed the risk is from overly broad `Bash` permissions and auto-generated reference files — **no malicious code was found**. The Java testing content is trustworthy. Review Bash commands before running. Socket: 0 alerts. Snyk: Low Risk.

---

## Quality Standards

The following standards define acceptable work for this project. **All work must meet these standards before a commit or task completion.**

### Backend (Spring Boot + Spring AI)

- All new Java classes must have corresponding JUnit 5 test classes
- Test coverage must reflect the full documented behavior (happy path + edge cases + error cases)
- The SSE `/api/chat` endpoint must be covered by integration tests verifying stream format compliance with the Vercel AI SDK Data Stream Protocol
- AI model integration (Spring AI / OpenAI) must be tested with mocked responses — never call the live API in tests
- Persistence layer (SQLite + JPA) must be tested with `@DataJpaTest` and an in-memory database
- No API keys or secrets may appear in any test or source file
- All tests must pass (`./mvnw test`) before committing
- Code must compile cleanly with no warnings (`./mvnw clean compile`)

### General Code Quality

- Java: 4-space indentation, standard Spring Boot naming conventions
- No unused imports, dead code, or commented-out blocks
- Lombok annotations used consistently; do not mix manual getters/setters with Lombok
- TypeScript/React: strict mode, no `any`, no type assertions with `as` unless unavoidable
- Environment variables for all secrets; never hardcode credentials

---

## Testing Infrastructure Setup (Backend — Greenfield)

> Spring Boot version: **3.5.9** | Java: **21** | Build: Maven

### Current Test Dependencies (already in `pom.xml`)

`spring-boot-starter-test` is included in `test` scope. It transitively provides:

- **JUnit 5** (Jupiter) — test engine
- **Mockito** — mocking framework
- **AssertJ** — fluent assertions
- **Spring Test** — `MockMvc`, `@SpringBootTest`, test slices
- **Hamcrest** — matcher library (use AssertJ preferentially)
- **JSONassert** and **JsonPath** — JSON response assertions

### Additional Dependencies to Add as Needed

When the task requires them, add to `pom.xml`:

```xml
<!-- Testcontainers (for integration tests with real DBs) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- SQLite in-memory for @DataJpaTest (if switching from file-based) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Awaitility (for async/SSE stream testing) -->
<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <scope>test</scope>
</dependency>
```

### Test Class Naming and Location

- Test classes: `src/test/java/...` mirroring the main source package structure
- Naming: `*Tests` suffix (e.g., `ChatControllerTests`, `OrderServiceTests`)
- Unit tests: no Spring context (`@ExtendWith(MockitoExtension.class)`)
- API/slice tests: `@WebMvcTest(ControllerClass.class)`
- Full integration tests: `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- Persistence tests: `@DataJpaTest`

### Running Tests

```bash
./mvnw test                          # run all tests
./mvnw test -Dtest=ChatControllerTests  # run single class
./mvnw clean verify                  # compile + test + package
```

---

## Test-Driven Development Process (Mandatory)

**TDD is not optional.** For every feature or bug fix, follow these steps in order:

### Step 1 — Read the Specification

Before writing any code, read the relevant section of `docs/PRD-Sinsay-PoC.md` and/or `docs/ADR-Sinsay-PoC.md`. Understand the exact expected behavior, inputs, outputs, and edge cases.

### Step 2 — Design the Test

Think through: What class/method will implement this? What are the inputs? What are all possible outputs and error states? Write these down as test method names before writing any test body.

### Step 3 — Write the Tests First

Write complete, failing test classes **before** implementing the production code. Tests must:

- Be named descriptively: `shouldReturnStreamedVerdictForValidReturnRequest()`
- Cover the happy path, boundary conditions, and at least one error/exception path
- Use `given / when / then` structure (BDD style with Mockito BDDMockito)
- Assert on specific values, not just non-null

### Step 4 — Run Tests and Confirm They Fail

```bash
./mvnw test -Dtest=YourTestClass
```

Tests must **fail** at this point (red). If they pass, the test is not testing anything.

### Step 5 — Implement the Production Code

Write the minimum production code needed to make the tests pass. Do not add functionality that is not covered by a test.

### Step 6 — Run All Tests and Confirm Green

```bash
./mvnw test
```

All tests must pass. If any test fails, fix the implementation (not the test) unless the test itself was wrong.

### Step 7 — Refactor

Clean up the code while keeping all tests green. Run tests after each refactor change.

---

## Pre-Commit Checklist (Backend)

**Every commit must pass this checklist. Do not commit if any item is unchecked.**

```
□ I have read the PRD/ADR for the changed area before starting
□ I wrote tests BEFORE implementing the production code (TDD)
□ All new Java classes have corresponding test classes
□ All tests pass: ./mvnw test (zero failures, zero errors)
□ Code compiles cleanly: ./mvnw clean compile (no warnings)
□ No hardcoded secrets, API keys, or credentials in any file
□ No .db database files staged for commit
□ Commit message follows the convention: Area: short summary (e.g., Feature: add chat SSE endpoint)
□ If touching the SSE adapter: verified stream format matches Vercel AI SDK Data Stream Protocol
□ If touching AI model calls: verified with mocked responses, not live API
□ /code-review run on the PR (for non-trivial changes)
□ Context7 MCP used to check latest docs for any library API used
```

---

## Task Completion Criteria (Backend)

A backend task is **complete** only when **all** of the following are true:

1. **Tests written first** — test classes exist and were written before production code
2. **All tests pass** — `./mvnw test` exits with zero failures and zero errors
3. **Implementation matches specification** — behavior verified against PRD/ADR, not just "code looks right"
4. **No regressions** — the full test suite passes, not just the new tests
5. **Code compiles cleanly** — no warnings that indicate incorrect usage
6. **Security verified** — no secrets committed, no unsafe shell command execution
7. **Commit message is correct** — follows `Area: summary` convention

**If any criterion is not met, the task is NOT complete.** Do not mark it done or move to the next task.

---

## Coding Style & Naming Conventions

- Java: 4-space indentation; standard Spring Boot conventions
- Packages: lowercase; keep package structure consistent with `com.silkycoders1...` or migrate to `com.sinsay` only if the ADR is implemented
- Classes: UpperCamelCase; methods/fields: lowerCamelCase
- Tests: `*Tests` suffix, mirrored package structure
- TypeScript/React (planned): use explicit types, PascalCase components, and file names matching component names

## TypeScript Usage

- Use TypeScript for all frontend code; prefer interfaces over types
- Avoid using the `any` type — prefer strict typing
- Import types from external npm packages when possible
- Create custom interfaces (preferred) or types for custom code
- Avoid type assertions with `as` or `!` when possible
- Use functional components with TypeScript interfaces
- Use strict mode in TypeScript for better type safety
- Use Type Guards for additional safety at execution time

## Build, Test, and Development Commands

- `./mvnw spring-boot:run`: run backend locally
- `./mvnw test`: run JUnit tests
- `./mvnw clean package`: build JAR
- `./mvnw clean verify`: full build + test validation

Planned commands once frontend exists:

- `cd Frontend && npm run dev`: run React app
- `cd Frontend && npm run build`: build frontend into `backend/src/main/resources/static`
- `cd Frontend && npm test`: run Vitest tests
- `cd Frontend && npm run lint`: ESLint
- `cd Frontend && npm run format:check`: Prettier check

## Backend Implementation Rules (Critical)

- **Streaming format**: SSE must emit Vercel Data Stream chunks (`0:"text"` and `8:[{...}]`), not raw JSON
- **Endpoint**: `POST /api/chat` returns `text/event-stream` and maps the Spring AI stream to the Vercel format
- **AI stack**: use `spring-ai-starter-model-openai` with chat model `gpt-4o` and `Media` attachments for images
- **Prompt policy**: select system prompt based on `intent` (`return` vs `complaint`). Respond to users in Polish
- **Persistence**: use SQLite with JPA; store request metadata, transcript, and verdicts. Never commit API keys

## Frontend Implementation Rules (Planned)

- **Form**: order number, purchase date, intent, description, image upload; validate with Zod
- **Chat UI**: `assistant-ui` components and `useChat` from Vercel AI SDK targeting `/api/chat`
- **Image handling**: resize images to max 1024px on the client before upload
- See `Frontend/AGENTS.md` for complete frontend quality standards and verification procedures

## Commit & Pull Request Guidelines

- Current commit history uses prefixes like `Docs:`. Follow `Area: short summary` (e.g., `Docs:`, `Feature:`, `Fix:`)
- PRs should include: goal, scope of changes, and any required setup notes (env vars, database files, API keys)
- Run `/code-review` before marking a PR ready for review

## Security & Configuration

- Configure keys in environment variables (e.g., `OPENAI_API_KEY`)
- SQLite DB file should be local-only; avoid committing `.db` files

## Agent Workflow Expectations

- Start by reading `docs/PRD-Sinsay-PoC.md` and `docs/ADR-Sinsay-PoC.md`
- Keep changes aligned with the PoC scope (no auth, no production deployment)
- When adding new structure (backend/frontend), update this guide accordingly
- Use the `java-spring-boot` and `java-testing` Skills at the start of relevant tasks
- Use Context7 MCP to fetch current library documentation before implementing new integrations
- Use the JetBrains MCP for IDE-level navigation and diagnostics when available
- Run `/code-review` on all non-trivial PRs

## Symlink

This file is the single source of truth. Claude Code accesses it through a symlink:

```bash
# Run once from the project root to create the symlink:
ln -s AGENTS.md CLAUDE.md
```

Do not maintain a separate `CLAUDE.md` file — the symlink ensures both agents and Claude Code read identical content.
