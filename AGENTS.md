# Repository Guidelines

## Project

**Sinsay AI PoC** — multimodal AI assistant for e-commerce returns (*Zwrot*) and complaints (*Reklamacja*). Users submit a form with photo; backend analyzes against Sinsay policy docs using an LLM; result is streamed as a chat conversation. All user-facing text in **Polish**.

**Key docs**
DO NOT READ them every time! Follow the Plan - it should contain all necesary information.
If you are sub-agent you should get all the details in prompt from main agent.
Load these long detailed files only when you are in doubt and need full picture:
- `docs/PRD-Product-Requirements-Document.md` — product requirements and acceptance criteria
- `docs/ADR/000-main-architecture.md` — architecture overview and data models
- `docs/ADR/001-backend.md` — backend implementation details
- `docs/ADR/002-frontend.md` — frontend implementation details

**Sinsay policy docs**
Sinsay AI Chat Agent knowledge base used to make decision when talking with the client:
`docs/regulamin.md`, `docs/reklamacje.md`, `docs/zwrot-30-dni.md`

---

## Repository Layout

```
backend/          Spring Boot app (Java 21, Maven)
frontend/         React 19 SPA (TypeScript, Vite)
docs/ADR/         Architecture Decision Records
docs/             PRD + Sinsay policy markdown files
```

---

## Commands

```bash
# Backend
cd backend && ./mvnw spring-boot:run     # run (requires OPENAI_API_KEY env var)
cd backend && ./mvnw test                # run JUnit tests
cd backend && ./mvnw clean package       # build JAR (output: backend/target/)

# Frontend
cd frontend && npm run dev               # dev server (proxies /api/* to :8080)
cd frontend && npm run build             # build into backend/src/main/resources/static/
cd frontend && npm test                  # Vitest
cd frontend && npm run lint              # ESLint
cd frontend && npm run format:check      # Prettier
```

---

## Critical Integration: Vercel AI SDK UI Message Stream Protocol (v6)

The frontend uses `useChatRuntime` with `AssistantChatTransport` from `@assistant-ui/react-ai-sdk`. The backend SSE response **must** use the Vercel AI SDK v6 UI Message Stream format or streaming will break:

```
Content-Type: text/event-stream
x-vercel-ai-ui-message-stream: v1

data: {"type":"start","messageId":"<uuid>"}

data: {"type":"text-start","id":"<uuid>"}

data: {"type":"text-delta","id":"<uuid>","delta":"Hello"}

data: {"type":"text-delta","id":"<uuid>","delta":" world"}

data: {"type":"text-end","id":"<uuid>"}
```

Use `SseEmitter` (Spring MVC), not `Flux` or plain `ResponseBodyEmitter`. The `AssistantChatTransport` sends `{ messages, system, tools }` to the backend; extract only the last user message content.

---

## Agent Workflow

### Before Starting Any Task
1. Read the relevant PRD (`docs/PRD-Product-Requirements-Document.md`) and ADR files (`docs/ADR/`) for the affected area.
2. Read `backend/AGENTS.md` if the task touches `backend/`, or `frontend/AGENTS.md` if it touches `frontend/`.
3. Define the expected behavior from the specification before writing or changing any code.

### TDD Rules
For every feature and bug fix:
1. Start from the specification, not the existing implementation.
2. Write or extend tests **before** production code.
3. Run the new tests and confirm they fail for the expected reason.
4. Implement the minimum code needed to make them pass.
5. Run the full verification suite for the changed scope (see below).
6. Refactor only while tests stay green.

If the area has no suitable test infrastructure yet, add it as part of the task — do not silently skip tests.

### Verification (required before every commit)

**Backend** (run from `backend/`):
```bash
./mvnw test          # all JUnit tests pass
./mvnw clean package # build succeeds
```

**Frontend** (run from `frontend/`):
```bash
npm test             # Vitest passes
npm run lint         # ESLint — no errors
npm run format:check # Prettier — no violations
npm run build        # Vite build succeeds
```

Verify only the scope relevant to your change. If the change affects runtime behavior, confirm the app starts correctly.

**Test Strategy:**
| Type | Mocks | Who |
|---|---|---|
| Unit | All deps | be/fe-dev |
| Integration | Only OpenAI (external) | be-dev |
| E2E | NOTHING (real stack) | qa-engineer |

**Verification:** Always start the app before committing. Tests passing ≠ app working. Be sure the app really works, not only pass tests.

**Env Vars:** See `.env.example` (OPENROUTER_API_KEY or OPENAI_API_KEY required)

### Commit Rules
- Commit only after verification passes and the changed scope is in a working state.
- Keep commits focused: one logical change per commit.
- Format: `Area: short summary` (e.g. `Backend:`, `Frontend:`, `Docs:`)
- Do **not** push to remote unless the user explicitly asks.

### Completion Criteria
A task is complete only when:
- Implementation matches the relevant PRD, ADR, and design guidance
- Tests were written first and pass honestly
- Verification for the changed scope passed with no errors or warnings
- The commit message is focused and the repository is in a consistent, reviewable state

---

## Context7 MCP Library IDs

| Library | Context7 ID |
|---|---|
| OpenAI Java SDK | `/openai/openai-java` |
| Spring Boot | `/spring-projects/spring-boot` |
| Lombok | `/projectlombok/lombok` |
| Vercel AI SDK | `/vercel/ai` |
| assistant-ui | `/assistant-ui/assistant-ui` |
| React | `/reactjs/react.dev` |
| Tailwind CSS | `/tailwindlabs/tailwindcss.com` |
| Shadcn/ui | `/shadcn-ui/ui` |
