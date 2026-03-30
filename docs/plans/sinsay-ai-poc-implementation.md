# Sinsay AI PoC — Full Implementation Plan

## Context

Build the complete Sinsay AI PoC application from near-scratch. Currently the backend is a bare Spring Boot scaffold (wrong package `com.silkycoders1`, no dependencies beyond web+lombok). The frontend directory is empty (no package.json, no source files). The goal is a working single-page app where customers submit return/complaint forms with photos, get AI-powered policy assessments, and can continue chatting.

All specs are defined in: PRD, ADR-000 (architecture), ADR-001 (backend), ADR-002 (frontend), design-guidelines.md.

---

## Visual Reference Files

These files must be provided to FE and QA agents for UI implementation and validation:

| Asset | Path | Purpose |
|---|---|---|
| Form wireframe | `docs/wireframe-form.png` | Reference layout for IntakeForm screen |
| Chat wireframe | `docs/wireframe-decision+chat.png` | Reference layout for ChatView screen |
| Sinsay homepage | `assets/sinsay-homepage.png` | Brand consistency reference |
| Design guidelines | `docs/design-guidelines.md` | Full design system spec |
| Logo SVG | `assets/logo.svg` | Sinsay wordmark |
| Favicon | `assets/sinsay-favicon.ico` | Browser favicon |

---

## Agents

| Agent | Specialization | Work Directory |
|---|---|---|
| `be-developer` | Java 21, Spring Boot, Maven, JPA, OpenAI SDK | `backend/` |
| `fe-developer` | React 19, TypeScript, Vite, Tailwind, Shadcn/ui, assistant-ui | `frontend/` |
| `qa-engineer` | Playwright E2E tests | `frontend/tests/e2e/` |

---

## Workflow Rules (apply to ALL tasks, remind agents in every prompt)

1. **TDD**: Write tests FIRST. Run them and confirm they FAIL. Then implement. Then run tests and confirm they PASS. Never skip tests.
2. **Verify before commit**: Backend: `cd backend && ./mvnw test && ./mvnw clean package`. Frontend: `cd frontend && npm test && npm run lint && npm run format:check && npm run build`.
3. **Commit after every task**: One logical change per commit. Format: `Area: short summary`.
4. **Read specs first**: Before coding, read the relevant PRD sections, ADR files, and AGENTS.md for the affected area.
5. **No push**: Do not push to remote unless explicitly asked.
6. **Context7 MCP**: Use `resolve-library-id` + `query-docs` for any library listed in AGENTS.md before using it.

---

## Dependency Matrix

```
BE-1 ──────┬──> BE-2 ──┬──> BE-4 ──> BE-6 ──> BE-7 ──> BE-8 ──┐
           │           │                                        │
           └──> BE-3 ──┤                                        ├──> QA-1 ──> QA-2 ──> QA-3
                       │                                        │
                       └──> BE-5 ──────────────────┘            │
                                                                │
FE-1 ──┬──> FE-2 ──┬──> FE-4 ──┬──> FE-6 ──> FE-7 ────────────┘
       │           │            │
       └──> FE-3 ──┘            │
       │                        │
       └──> FE-5 ───────────────┘
```

## Parallelism Map

| Step | Slot 1 (be-developer) | Slot 2 (fe-developer) |
|---|---|---|
| 1 | **BE-1** | **FE-1** |
| 2 | **BE-2** + **BE-3** (sequential) | **FE-2** + **FE-3** (sequential) |
| 3 | **BE-4** + **BE-5** (sequential) | **FE-4** + **FE-5** (sequential) |
| 4 | **BE-6** | **FE-6** |
| 5 | **BE-7** | **FE-7** |
| 6 | **BE-8** | — |
| 7 | — | **QA-1** (qa-engineer) |
| 8 | — | **QA-2** (qa-engineer) |
| 9 | — | **QA-3** (qa-engineer) |

---

## Phase 1: Project Foundation

### BE-1: Migrate package structure and add Maven dependencies
- **Agent:** `be-developer`
- **Depends on:** none
- **Parallel with:** FE-1

**Context to provide:**
- Current pom.xml has groupId `com.silkycoders-1`, only deps: web, lombok, test
- Current app class is at `com.silkycoders1.jsystemssilkycodders1.JSystemsSilkyCodders1Application`
- Target package: `com.sinsay`, target app class: `SinsayApplication`

**Spec references to include in prompt:**
- `backend/AGENTS.md` — package structure, env vars, tech stack
- `docs/ADR/001-backend.md` §7 — SQLite dialect decision, §3 — component list

**TDD steps:**
1. After migration, write `SinsayApplicationTests` in `com.sinsay` that verifies Spring context loads with test profile
2. Verify the build compiles and context loads
3. Run test — confirm it passes

**Implementation:**
1. Delete old package dirs `com/silkycoders1/jsystemssilkycodders1` (both main and test)
2. Create `com/sinsay/SinsayApplication.java` with `@SpringBootApplication`
3. Create `com/sinsay/SinsayApplicationTests.java` with `@SpringBootTest` context load
4. Update `pom.xml`: groupId → `com.sinsay`, artifactId → `sinsay-poc`. Add deps: `spring-boot-starter-data-jpa`, `org.xerial:sqlite-jdbc` (runtime), `org.hibernate.orm:hibernate-community-dialects` (runtime), `com.openai:openai-java` (use Context7 for `/openai/openai-java` to get latest version), `com.h2database:h2` (test scope)
5. Configure `application.properties`: SQLite datasource, Hibernate dialect, `ddl-auto=update`, multipart max 10MB, custom props for `openai.model` and `policy-docs.path`
6. Create `application-test.properties`: H2 in-memory, `ddl-auto=create-drop`

**Validation:** `cd backend && ./mvnw test && ./mvnw clean package` — context loads, build succeeds
**Commit:** `Backend: migrate to com.sinsay package and add project dependencies`

---

### FE-1: Initialize React project with all tooling
- **Agent:** `fe-developer`
- **Depends on:** none
- **Parallel with:** BE-1

**Context to provide:**
- Frontend directory is completely empty (only AGENTS.md and test CLAUDE.md files exist — preserve these files)
- Must use React 19, TypeScript strict, Vite, Tailwind CSS, Shadcn/ui
- Build output goes to `../backend/src/main/resources/static/`
- Dev proxy: `/api` → `http://localhost:8080`
- Configure Tailwind with Sinsay brand tokens from `assets/design-tokens.json`

**Spec references to include in prompt:**
- `frontend/AGENTS.md` — tech stack, component structure, vite config
- `docs/ADR/002-frontend.md` §2 — Context7 library references, §6 — Vite proxy decision
- `docs/design-guidelines.md` — colors, fonts, spacing for Tailwind config
- `assets/design-tokens.json` — exact token values to embed in Tailwind config

**TDD steps:**
1. Create smoke test `App.test.tsx` that renders `<App />` and verifies it mounts
2. Configure ESLint + Prettier, verify they pass on the initial code and after changes

**Implementation:**
1. Initialize Vite project with React+TS template in `frontend/`
2. Install production deps: `react@19 react-dom@19 @assistant-ui/react @assistant-ui/react-ai-sdk ai zod`
3. Install dev deps: `vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event msw jsdom eslint prettier @vitejs/plugin-react tailwindcss @tailwindcss/vite`
4. Configure `vite.config.ts`: React plugin, Tailwind plugin, proxy `/api` → `http://localhost:8080`, build outDir `../backend/src/main/resources/static/`
5. Configure `tsconfig.json` with strict mode
6. Initialize Shadcn/ui (`npx shadcn@latest init`) — use Context7 for `/shadcn-ui/ui`
7. Configure Tailwind with Sinsay design tokens from `assets/design-tokens.json`: brand colors (#16181D, #E09243, #E90000, #0BB407), text colors, bg colors, font family (Euclid Circular B with Arial/Helvetica fallbacks), spacing scale
8. Copy `assets/logo.svg` → `frontend/public/logo.svg`, `assets/sinsay-favicon.ico` → `frontend/public/favicon.ico`
9. Create minimal `App.tsx` placeholder + `App.test.tsx` smoke test
10. Add scripts to `package.json`: `test`, `lint`, `format:check`

**Validation:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build` — all pass
**Commit:** `Frontend: initialize Vite + React 19 + Tailwind + Shadcn project`

---

## Phase 2: Data Layer + Hooks

### BE-2: JPA entities and repositories
- **Agent:** `be-developer`
- **Depends on:** BE-1
- **Parallel with:** FE-2

**Context to provide:**
- Data models: Session (UUID, Intent enum RETURN/COMPLAINT, orderNumber, productName, description TEXT, createdAt), ChatMessage (UUID, sessionId FK, Role enum USER/ASSISTANT, content TEXT, sequenceNumber, createdAt)
- Use Lombok for boilerplate. H2 for tests.

**Spec references to include in prompt:**
- `docs/ADR/000-main-architecture.md` §5 — Data Models
- `docs/ADR/001-backend.md` §3 — Repositories
- `backend/src/test/CLAUDE.md` — test guidelines

**TDD steps:**
1. Write `SessionRepositoryTests`: save Session, find by ID, verify all fields round-trip
2. Write `ChatMessageRepositoryTests`: save multiple messages, verify `findBySessionIdOrderBySequenceNumberAsc` returns correct order
3. Run tests — fail (no entities). Implement. Run tests — pass.

**Implementation:**
1. Create enums: `com.sinsay.model.Intent` (RETURN, COMPLAINT), `com.sinsay.model.Role` (USER, ASSISTANT)
2. Create `Session` JPA entity with Lombok, UUID auto-generated PK, `@Enumerated(STRING)`, `@Column(columnDefinition = "TEXT")` for description, `createdAt` via `@PrePersist`
3. Create `ChatMessage` JPA entity with sessionId (UUID FK), sequenceNumber, same patterns
4. Create `SessionRepository extends JpaRepository<Session, UUID>`
5. Create `ChatMessageRepository` with `findBySessionIdOrderBySequenceNumberAsc(UUID)`

**Validation:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add JPA entities and repositories for Session and ChatMessage`

---

### BE-3: PolicyDocService
- **Agent:** `be-developer`
- **Depends on:** BE-1
- **Parallel with:** FE-2, FE-3 (runs sequentially after BE-2 in same agent slot)

**Context to provide:**
- Loads policy .md files from disk, assembles 6-section system prompt
- Policy selection: RETURN → regulamin.md + zwrot-30-dni.md, COMPLAINT → regulamin.md + reklamacje.md
- System prompt sections (in order): role definition, decision categories ("Prawdopodobnie zaakceptowane" / "Prawdopodobnie odrzucone" / "Niejasne"), mandatory disclaimer, scope boundary, language (Polish), policy doc content
- Path from `policy-docs.path` property (default `../docs`)

**Spec references to include in prompt:**
- `docs/ADR/001-backend.md` §3 (PolicyDocService) and §6 (System Prompt Structure)
- `backend/AGENTS.md` — System Prompt Structure section

**TDD steps (TAC-BE-01, TAC-BE-02):**
1. `getSystemPrompt(RETURN)` contains regulamin + zwrot content, does NOT contain reklamacje-unique text
2. `getSystemPrompt(COMPLAINT)` contains regulamin + reklamacje content, does NOT contain zwrot-unique text
3. System prompt contains role definition, disclaimer, language instruction
4. Missing file → descriptive exception

**Implementation:**
1. Create `PolicyDocService` @Service with `@Value("${policy-docs.path:../docs}")`
2. Load files with `Files.readString(Path)`
3. `getSystemPrompt(Intent)` concatenates all 6 sections

**Validation:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add PolicyDocService for intent-based system prompt assembly`

---

### FE-2: useSession hook and form validation
- **Agent:** `fe-developer`
- **Depends on:** FE-1
- **Parallel with:** BE-2, BE-3

**Context to provide:**
- localStorage key: `sinsay_session_id`
- Zod validation: intent (RETURN|COMPLAINT, required), orderNumber (required), productName (required), description (required)
- Image validation separate from Zod: MIME types image/jpeg, image/png, image/webp, image/gif; max 10MB

**Spec references to include in prompt:**
- `docs/ADR/002-frontend.md` §3 (useSession hook) and §4 (Form state, Validation error state, localStorage entry)
- `frontend/AGENTS.md` — Form Fields table, Session Flow section
- `frontend/tests/CLAUDE.md` — useSession test scenarios

**TDD steps:**
1. `useSession` tests: returns null initially, `setSessionId` writes, `clearSession` removes, reads existing
2. Zod schema tests: rejects missing fields, accepts valid data
3. Image validation tests: accepts jpeg, rejects pdf, rejects >10MB

**Implementation:**
1. `src/hooks/useSession.ts` → `{ sessionId, setSessionId, clearSession }`
2. `src/lib/validation.ts` → Zod schema + `validateImage(file)` function
3. Export types: `FormData`, `FormErrors`

**Validation:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: add useSession hook and Zod form validation`

---

### FE-3: ImageUpload component
- **Agent:** `fe-developer`
- **Depends on:** FE-1
- **Parallel with:** BE-2, BE-3 (runs sequentially after FE-2 in same agent slot)

**Context to provide:**
- Drag-and-drop zone with click-to-browse. Validates MIME and size on selection (immediate).
- Shows thumbnail + filename. Resizes to max 1024px via canvas. "Usuń" remove button.
- Error text in Polish: "Dozwolone formaty: JPEG, PNG, WebP, GIF", "Maksymalny rozmiar pliku: 10 MB"
- **Look at the wireframe** `docs/wireframe-form.png` for the image upload area layout (it shows a drag-drop zone within the form)

**Spec references to include in prompt:**
- `docs/wireframe-form.png` — visual reference for the upload area within the form
- `docs/ADR/002-frontend.md` §3 (Image upload sub-component)
- `frontend/AGENTS.md` — Image Handling section
- `frontend/tests/CLAUDE.md` — ImageUpload test scenarios
- `docs/PRD-Product-Requirements-Document.md` §9 (Screen 1 — Zdjecie produktu) and §6 (AC-03, AC-04)

**TDD steps (TAC-FE-02, TAC-FE-03):**
1. Renders drop zone with format/size text
2. Valid JPEG → thumbnail, filename, no error
3. PDF → error "Dozwolone formaty: JPEG, PNG, WebP, GIF"
4. >10MB → error mentioning "10 MB"
5. "Usuń" click → cleared
6. `onChange` called with File or null

**Implementation:**
1. `src/components/ImageUpload.tsx` — Props: `value`, `onChange`, `error?`
2. Native drag events + hidden file input
3. MIME/size validation on drop/select
4. Canvas resize utility
5. `URL.createObjectURL` for thumbnail

**Validation:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: add ImageUpload component with validation and preview`

---

## Phase 3: Core Services + Frontend Views

### BE-4: OpenAI config and AnalysisService
- **Agent:** `be-developer`
- **Depends on:** BE-2, BE-3
- **Parallel with:** FE-4

**Context to provide:**
- OpenAI Java SDK with `OPENAI_API_KEY`, `OPENAI_BASE_URL` from env. Model from `openai.model` property.
- AnalysisService: multipart form → base64 image → OpenAI sync call → persist Session + 2 ChatMessages → return `{sessionId, message}`
- Image as base64 data URI in user message (2 content parts: image + text)

**Spec references to include in prompt:**
- `docs/ADR/001-backend.md` §3 (OpenAIConfig, AnalysisService) and §4 (DTOs, OpenAI payload)
- `backend/AGENTS.md` — Initial Analysis section, env vars
- Use Context7 for `/openai/openai-java`

**TDD steps (TAC-BE-03, TAC-BE-08):**
1. OpenAIConfig creates bean with configured baseUrl/apiKey (test properties)
2. AnalysisService builds 2-part user message (image + text) — mock client, capture params
3. Base64 encoding produces valid data URI
4. Session persisted with correct fields
5. 2 ChatMessages: USER seq=0, ASSISTANT seq=1

**Implementation:**
1. `OpenAIConfig` @Configuration → `OpenAIClient` bean via `OpenAIOkHttpClient.builder()`
2. `AnalysisService` → `analyze(Intent, orderNumber, productName, description, MultipartFile)` — base64, build params, call sync, persist, return

**Validation:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add OpenAI config and AnalysisService for initial form analysis`

---

### BE-5: ChatService with SSE UI Message Stream encoder
- **Agent:** `be-developer`
- **Depends on:** BE-2, BE-3
- **Parallel with:** FE-4, FE-5 (runs sequentially after BE-4 in same agent slot)

**Context to provide:**
- Vercel AI SDK v6 UI Message Stream format over SSE (NOT the legacy `0:"text"\n` format)
- Each SSE event: `data: <JSON>\n\n`. Event types: `start`, `text-start`, `text-delta`, `text-end`
- All events share the same message UUID as `messageId` (in `start`) and `id` (in `text-*`)
- SseEmitter (Spring MVC), NOT Flux or plain ResponseBodyEmitter
- Response header: `x-vercel-ai-ui-message-stream: v1`
- OpenAI streaming: `client.chat().completions().createStreaming(params)`
- Persist USER before stream, ASSISTANT after

**Spec references to include in prompt:**
- `docs/ADR/001-backend.md` §3 (ChatService) and §5 (Vercel AI SDK UI Message Stream Format)
- `backend/AGENTS.md` — Chat Continuation + Vercel format sections
- Use Context7 for `/openai/openai-java` streaming API

**TDD steps (TAC-BE-04, TAC-BE-05, TAC-BE-06):**
1. `SseStreamEncoder.encodeStart(messageId)` → `{"type":"start","messageId":"<uuid>"}`
2. `SseStreamEncoder.encodeTextStart(messageId)` → `{"type":"text-start","id":"<uuid>"}`
3. `SseStreamEncoder.encodeTextDelta(messageId, "Hello")` → `{"type":"text-delta","id":"<uuid>","delta":"Hello"}`
4. `SseStreamEncoder.encodeTextEnd(messageId)` → `{"type":"text-end","id":"<uuid>"}`
5. After `streamResponse()` (mocked OpenAI), ASSISTANT message persisted with full content
6. USER message persisted before streaming

**Implementation:**
1. `SseStreamEncoder` utility with static methods producing JSON strings for each event type
2. `ChatService` → `streamResponse(Session, List<ChatMessage>, String, SseEmitter)` — writes SSE events

**Validation:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add ChatService with SSE UI Message Stream streaming`

---

### FE-4: IntakeForm component
- **Agent:** `fe-developer`
- **Depends on:** FE-2, FE-3
- **Parallel with:** BE-4, BE-5

**Context to provide:**
- 5-field form matching the wireframe layout
- Zod validation on submit. Inline errors in Polish.
- Submit as multipart/form-data to `POST /api/sessions`
- Loading state: "Analizuję..." button text, disabled
- On success: `onSuccess(sessionId, message, formData)` callback
- **MUST match the wireframe** `docs/wireframe-form.png` for layout and field arrangement
- **MUST follow design guidelines** from `docs/design-guidelines.md` for button style, spacing, typography

**Spec references to include in prompt:**
- `docs/wireframe-form.png` — LOOK AT THIS for exact form layout
- `docs/PRD-Product-Requirements-Document.md` §9 (Screen 1 — field descriptions, placeholders, button text)
- `docs/ADR/002-frontend.md` §3 (IntakeForm) and §5 (Form → Chat Transition)
- `frontend/AGENTS.md` — Form Fields table
- `frontend/tests/CLAUDE.md` — IntakeForm test scenarios
- `docs/design-guidelines.md` — Primary CTA button style (bg #E09243, no border-radius, padding 12px 32px, font-weight 600)
- `assets/design-tokens.json` — exact values

**TDD steps (TAC-FE-01, TAC-FE-04, TAC-FE-08):**
1. Renders all 5 fields with Polish labels
2. Submit with all empty → 5 inline error messages
3. Valid submit (MSW mock): button shows "Analizuję...", disabled
4. On success: localStorage has sessionId, `onSuccess` called
5. On API error: error shown, button re-enabled

**Implementation:**
1. `src/components/IntakeForm.tsx` — Shadcn/ui RadioGroup, Input, Textarea, ImageUpload
2. Layout matches wireframe: centered form, logo at top, heading "Sprawdź zwrot lub reklamację"
3. Sinsay-styled submit button (accent color, no border-radius)
4. All strings in Polish

**Validation:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: add IntakeForm component with validation and submission`

---

### FE-5: ChatView component
- **Agent:** `fe-developer`
- **Depends on:** FE-2
- **Parallel with:** BE-4, BE-5 (runs sequentially after FE-4 in same agent slot)

**Context to provide:**
- Uses `useChatRuntime` with `AssistantChatTransport` from `@assistant-ui/react-ai-sdk`
- Summary bar at top, "Nowa sesja" button, assistant-ui Thread component
- Session resume: GET /api/sessions/{id}, map to `UIMessage` format with `parts: [{ type: 'text', text }]`
- **MUST match the wireframe** `docs/wireframe-decision+chat.png` for chat layout
- **MUST follow design guidelines** for message styling, input area

**Spec references to include in prompt:**
- `docs/wireframe-decision+chat.png` — LOOK AT THIS for chat layout, summary bar, message bubbles
- `docs/ADR/002-frontend.md` §3 (ChatView, ChatRuntime setup) and §4 (Message format)
- `frontend/AGENTS.md` — Chat Integration section
- `docs/design-guidelines.md` — colors, typography for chat styling
- Use Context7 for `/assistant-ui/assistant-ui` and `/vercel/ai`

**TDD steps (TAC-FE-05, TAC-FE-06, TAC-FE-07):**
1. Renders summary bar with session info
2. Renders initial messages from props
3. "Nowa sesja" click → callback fired
4. Mount with sessionId (MSW mock GET → history) → messages rendered
5. Mount with sessionId (MSW mock GET → 404) → `onSessionInvalid` fired

**Implementation:**
1. `src/components/ChatView.tsx` — layout matching wireframe
2. `useChatRuntime({ transport: new AssistantChatTransport({ api: /api/sessions/${sessionId}/messages }) })`
3. `AssistantRuntimeProvider` + `Thread` + summary bar + "Nowa sesja"
4. Map session history messages to `UIMessage` format: `{ id, role, parts: [{ type: 'text', text }] }`

**Validation:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: add ChatView component with assistant-ui streaming runtime`

---

## Phase 4: Controllers + App Integration

### BE-6: SessionController
- **Agent:** `be-developer`
- **Depends on:** BE-4
- **Parallel with:** FE-6

**Context to provide:**
- `POST /api/sessions` (multipart) and `GET /api/sessions/{id}` (JSON)
- POST validates all 5 fields + image MIME/size. Delegates to AnalysisService. Returns `{sessionId, message}`.
- GET returns `{session, messages[]}` ordered by sequenceNumber. 404 for unknown.
- WebConfig for CORS (allow localhost:5173 in dev)

**Spec references to include in prompt:**
- `docs/ADR/000-main-architecture.md` §6 — API Contracts
- `docs/ADR/001-backend.md` §3 (Controllers) and §4 (DTOs)
- `backend/AGENTS.md` — API Contracts table

**TDD steps (TAC-01 through TAC-08):**
1. POST valid → 200 with `{sessionId, message}` (mock AnalysisService)
2. POST missing intent → 400
3. POST invalid intent → 400
4. POST missing image → 400
5. POST PDF image → 400
6. POST >10MB image → 400
7. GET valid ID → 200 with session + ordered messages
8. GET unknown ID → 404

**Implementation:**
1. DTOs: `SessionResponse`, `SessionLoadResponse`, `MessageDto`
2. `SessionController` @RestController @RequestMapping("/api/sessions")
3. `WebConfig` @Configuration: CORS for localhost:5173

**Validation:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add SessionController with form submission and session load endpoints`

---

### BE-7: ChatController
- **Agent:** `be-developer`
- **Depends on:** BE-5, BE-6
- **Parallel with:** FE-6 (runs sequentially after BE-6 in same agent slot)

**Context to provide:**
- `POST /api/sessions/{id}/messages` — accepts JSON `{ messages: [...], system: "...", ... }` from `AssistantChatTransport`
- Backend extracts only the last user message text from `messages[]` array
- Returns `text/event-stream` SSE with Vercel AI SDK v6 UI Message Stream format
- Header `x-vercel-ai-ui-message-stream: v1`
- SseEmitter, async executor for streaming
- 404 for unknown session, 400 for empty/missing messages

**Spec references to include in prompt:**
- `docs/ADR/000-main-architecture.md` §6 — chat endpoint
- `docs/ADR/001-backend.md` §3 (ChatController) and §5 (UI Message Stream format)
- `backend/AGENTS.md` — Chat Continuation section

**TDD steps (TAC-09, TAC-10, TAC-11):**
1. POST valid `{ messages: [{ role: "user", content: "hello" }] }` → `text/event-stream`, `x-vercel-ai-ui-message-stream: v1` header
2. Body contains SSE events with `text-delta` type (mock ChatService writes test data)
3. Body contains `text-start` and `text-end` events
4. USER + ASSISTANT messages persisted
5. Unknown session → 404
6. Empty messages array → 400

**Implementation:**
1. `ChatRequest` DTO with `messages` (list of `{ role, content }` objects) — extract last user message
2. `ChatController` — SseEmitter, async task, SSE headers

**Validation:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add ChatController with SSE streaming chat endpoint`

---

### FE-6: App.tsx routing and session flow
- **Agent:** `fe-developer`
- **Depends on:** FE-4, FE-5
- **Parallel with:** BE-6

**Context to provide:**
- Root component controls view: `{ view: 'form' | 'chat', sessionId }`
- No router — conditional rendering
- Mount: check localStorage → show ChatView or IntakeForm
- Form success → ChatView with initial messages
- "Nowa sesja" → clear localStorage → IntakeForm
- Session 404 → clear → IntakeForm

**Spec references to include in prompt:**
- `docs/ADR/002-frontend.md` §3 (App.tsx) and §5 (Transition flows)
- `frontend/AGENTS.md` — Session Flow section

**TDD steps:**
1. No sessionId → IntakeForm rendered
2. sessionId in localStorage (MSW mock GET) → ChatView rendered
3. Form success → ChatView, localStorage set
4. "Nowa sesja" → IntakeForm, localStorage cleared
5. 404 → IntakeForm, localStorage cleared

**Implementation:**
1. Update `App.tsx`: `useSession` hook, conditional rendering, callbacks

**Validation:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: wire App.tsx with form-to-chat routing and session management`

---

## Phase 5: Design Polish + Integration Tests

### FE-7: Apply Sinsay design system and responsive polish
- **Agent:** `fe-developer`
- **Depends on:** FE-6
- **Parallel with:** BE-8

**Context to provide:**
- **MUST look at wireframes** to verify layout matches:
  - `docs/wireframe-form.png` — form screen layout
  - `docs/wireframe-decision+chat.png` — chat screen layout
- **MUST look at Sinsay homepage** `assets/sinsay-homepage.png` for brand consistency
- Sinsay aesthetic: clean, minimalist, sharp corners on buttons, accent orange, Euclid Circular B font
- Primary button: bg #E09243, white text, border-radius 0, padding 12px 32px, font-weight 600
- Must work on 375px mobile (AC-20)

**Spec references to include in prompt:**
- `docs/wireframe-form.png` — form wireframe to match
- `docs/wireframe-decision+chat.png` — chat wireframe to match
- `assets/sinsay-homepage.png` — brand consistency reference
- `docs/design-guidelines.md` — full design system
- `assets/design-tokens.json` — exact values
- `docs/PRD-Product-Requirements-Document.md` §9 (UI Description)

**TDD steps (TAC-FE-08, TAC-FE-09):**
1. All user-visible text in Polish
2. Form and chat usable at 375px viewport (no horizontal overflow)
3. Logo renders in form view

**Implementation:**
1. Compare current UI against wireframes — adjust layout to match
2. Apply design tokens: accent button, typography, spacing
3. Style form: centered, logo, heading, field arrangement per wireframe
4. Style chat: summary bar, message differentiation, fixed input per wireframe
5. Style ImageUpload: dashed border drop zone
6. Responsive: 375px minimum, vertical stacking on mobile
7. Verify all strings are Polish

**Validation:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: apply Sinsay design system with brand colors, typography, and responsive layout`

---

### BE-8: Full-flow integration test
- **Agent:** `be-developer`
- **Depends on:** BE-7
- **Parallel with:** FE-7

**Context to provide:**
- Comprehensive integration test: create session → load session → chat message → verify DB
- `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `TestRestTemplate`, `@MockitoBean` for OpenAI
- H2 test profile

**Spec references to include in prompt:**
- `docs/ADR/000-main-architecture.md` §10 — TAC-01 through TAC-12
- `backend/src/test/CLAUDE.md`

**TDD steps:**
1. POST /api/sessions (mock OpenAI) → 200, get sessionId
2. GET /api/sessions/{sessionId} → session + messages in order
3. POST /api/sessions/{sessionId}/messages with `{ messages: [{ role: "user", content: "..." }] }` (mock streaming) → SSE UI Message Stream format response with `x-vercel-ai-ui-message-stream: v1` header
4. GET again → 4 messages total

**Implementation:** `FullFlowIntegrationTests`

**Validation:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add full-flow integration test for session lifecycle`

---

## Phase 6: E2E Tests

### QA-1: Playwright setup + form validation + visual check
- **Agent:** `qa-engineer`
- **Depends on:** FE-7, BE-8
- **Parallel with:** none

**Context to provide:**
- Set up Playwright in project. Test against Vite dev server (5173) with `page.route()` API mocking.
- Form has 5 required fields. Polish labels. AC-01 through AC-06.
- **MUST visually compare** the rendered form against:
  - `docs/wireframe-form.png` — verify form layout matches wireframe
  - `assets/sinsay-homepage.png` — verify brand consistency (colors, typography, overall feel)
  - `docs/design-guidelines.md` — verify button styles, spacing, colors match spec
- Take screenshots of the form for visual verification

**Spec references to include in prompt:**
- `docs/wireframe-form.png` — wireframe to visually compare against
- `assets/sinsay-homepage.png` — brand reference
- `docs/design-guidelines.md` — design spec
- `docs/PRD-Product-Requirements-Document.md` §6 (AC-01 through AC-06)
- `frontend/tests/e2e/CLAUDE.md` — E2E guidelines
- `frontend/tests/e2e/AGENTS.md`

**Implementation:**
1. Install Playwright, configure `playwright.config.ts`
2. Test: form renders with all 5 fields
3. Test: empty submit → 5 validation errors
4. Test: PDF upload → format error
5. Test: >10MB → size error
6. Test: valid submit → "Analizuję..." loading state
7. **Visual validation**: take screenshot of form, compare against wireframe `docs/wireframe-form.png` — verify layout is similar (centered form, logo at top, fields in order, styled button). Report any significant deviations.
8. **Design check**: verify button has accent color, form uses brand typography

**Validation:** `npx playwright test` — all pass
**Commit:** `QA: add Playwright setup and form validation E2E tests`

---

### QA-2: Form-to-chat flow + visual check
- **Agent:** `qa-engineer`
- **Depends on:** QA-1

**Context to provide:**
- After form submit (mocked API), chat view should appear with AI message
- sessionId in localStorage. Chat input functional. "Nowa sesja" returns to form.
- **MUST visually compare** chat view against `docs/wireframe-decision+chat.png`

**Spec references to include in prompt:**
- `docs/wireframe-decision+chat.png` — wireframe to compare chat layout against
- `assets/sinsay-homepage.png` — brand consistency
- `docs/design-guidelines.md` — design spec
- `docs/PRD-Product-Requirements-Document.md` §6 (AC-07, AC-12, AC-15, AC-18)
- `docs/ADR/002-frontend.md` §5 (Form → Chat Transition)

**Implementation:**
1. Test: fill form + submit (mock API) → chat appears with AI message
2. Test: sessionId in localStorage
3. Test: chat input visible, can type
4. Test: "Nowa sesja" → form, localStorage cleared
5. Test: mock streaming → assistant message appears
6. **Visual validation**: take screenshot of chat view, compare against wireframe `docs/wireframe-decision+chat.png` — verify summary bar, message layout, input area. Report deviations.

**Validation:** `npx playwright test`
**Commit:** `QA: add form-to-chat flow E2E tests`

---

### QA-3: Session resume + responsive + final visual audit
- **Agent:** `qa-engineer`
- **Depends on:** QA-2

**Context to provide:**
- Session resume via localStorage. 404 handling. 375px viewport. All text Polish.
- **Final visual audit**: compare both screens at desktop and mobile widths against wireframes and design system

**Spec references to include in prompt:**
- `docs/wireframe-form.png` — form wireframe (for mobile check)
- `docs/wireframe-decision+chat.png` — chat wireframe (for mobile check)
- `assets/sinsay-homepage.png` — overall brand feel
- `docs/design-guidelines.md` — responsive expectations
- `docs/PRD-Product-Requirements-Document.md` §6 (AC-15 through AC-20)
- `docs/ADR/002-frontend.md` §7 (TAC-FE-05, TAC-FE-06, TAC-FE-09)

**Implementation:**
1. Test: localStorage sessionId → reload → mock GET → chat with history
2. Test: invalid sessionId → 404 → form, localStorage cleared
3. Test: 375px viewport → form without horizontal scroll
4. Test: 375px viewport → chat without horizontal scroll
5. Test: Polish text strings present (labels, buttons)
6. **Final visual audit**: take screenshots at 1440px and 375px for both form and chat. Read wireframes and homepage screenshot. Compare. Report a summary of visual alignment: what matches, what deviates, what needs attention. This is informational — create the report as a test output or log.

**Validation:** `npx playwright test`
**Commit:** `QA: add session resume, responsive, and visual audit E2E tests`

---

## Post-Implementation Verification Checklist

After ALL tasks complete, run the full validation suite:

1. **Backend unit+integration tests:** `cd backend && ./mvnw clean test` — all pass
2. **Backend build:** `cd backend && ./mvnw clean package` — JAR builds
3. **Frontend unit tests:** `cd frontend && npm test` — all pass
4. **Frontend lint:** `cd frontend && npm run lint` — no errors
5. **Frontend format:** `cd frontend && npm run format:check` — no violations
6. **Frontend build:** `cd frontend && npm run build` — builds into backend static/
7. **E2E tests:** `cd frontend && npx playwright test` — all pass
8. **Manual smoke test:** Start backend with `OPENAI_API_KEY`, open browser, submit form, verify chat with real AI
