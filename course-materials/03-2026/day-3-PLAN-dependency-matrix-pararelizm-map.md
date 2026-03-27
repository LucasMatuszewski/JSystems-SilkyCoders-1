# Sinsay AI PoC — Full Implementation Plan

## Context

Build the complete Sinsay AI PoC application from near-scratch. Currently the backend is a bare Spring Boot scaffold (wrong package, no dependencies beyond web+lombok). The frontend directory is empty (no package.json, no source files). The goal is a working single-page app where customers submit return/complaint forms with photos, get AI-powered policy assessments, and can continue chatting.

All specs are defined in: PRD, ADR-000 (architecture), ADR-001 (backend), ADR-002 (frontend), design-guidelines.md.

---

## Agents

| Agent | Specialization | Work Directory |
|---|---|---|
| `be-developer` | Java 21, Spring Boot, Maven, JPA, OpenAI SDK | `backend/` |
| `fe-developer` | React 19, TypeScript, Vite, Tailwind, Shadcn/ui, assistant-ui | `frontend/` |
| `qa-engineer` | Playwright E2E tests | `frontend/tests/e2e/` |

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

| Step | Slot 1 (be-developer) | Slot 2 (fe-developer) | Slot 3 (qa-engineer) |
|---|---|---|---|
| 1 | **BE-1** | **FE-1** | — |
| 2 | **BE-2** | *(waits for FE-1)* | — |
| 3 | **BE-3** + **BE-5** (sequential) | **FE-2** + **FE-3** (sequential) | — |
| 4 | **BE-4** | *(waits for FE-2+FE-3)* | — |
| 5 | **BE-6** | **FE-4** + **FE-5** (sequential) | — |
| 6 | **BE-7** | *(waits for FE-4+FE-5)* | — |
| 7 | **BE-8** | **FE-6** | — |
| 8 | — | **FE-7** | — |
| 9 | — | — | **QA-1** |
| 10 | — | — | **QA-2** |
| 11 | — | — | **QA-3** |

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

**Implementation:**
1. Delete old package dirs `com/silkycoders1/jsystemssilkycodders1` (both main and test)
2. Create `com/sinsay/SinsayApplication.java` with `@SpringBootApplication`
3. Create `com/sinsay/SinsayApplicationTests.java` with `@SpringBootTest` context load
4. Update `pom.xml`:
   - groupId → `com.sinsay`, artifactId → `sinsay-poc`
   - Add: `spring-boot-starter-data-jpa`, `org.xerial:sqlite-jdbc` (runtime), `org.hibernate.orm:hibernate-community-dialects` (runtime), `com.openai:openai-java` (use Context7 to get latest version), `com.h2database:h2` (test scope)
5. Configure `application.properties`: SQLite datasource (`jdbc:sqlite:./sinsay_poc.db`), Hibernate dialect (`org.hibernate.community.dialect.SQLiteDialect`), `ddl-auto=update`, `spring.servlet.multipart.max-file-size=10MB`, `spring.servlet.multipart.max-request-size=10MB`, custom props `openai.model=openai/gpt-4o-mini`, `policy-docs.path=../docs`
6. Create `src/main/resources/application-test.properties`: H2 in-memory datasource, H2 dialect, `ddl-auto=create-drop`

**Verify:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: migrate to com.sinsay package and add project dependencies`

---

### FE-1: Initialize React project with all tooling
- **Agent:** `fe-developer`
- **Depends on:** none
- **Parallel with:** BE-1

**Context to provide:**
- Frontend directory is completely empty (only AGENTS.md and test CLAUDE.md files exist)
- Must use React 19, TypeScript strict, Vite, Tailwind CSS, Shadcn/ui
- Build output goes to `../backend/src/main/resources/static/`
- Dev proxy: `/api` → `http://localhost:8080`

**Spec references to include in prompt:**
- `frontend/AGENTS.md` — tech stack, component structure, vite config
- `docs/ADR/002-frontend.md` §2 — Context7 library references, §6 — Vite proxy decision
- `docs/design-guidelines.md` — colors, fonts, spacing for Tailwind config
- `assets/design-tokens.json` — exact token values

**TDD steps:**
1. After project init, create a smoke test that renders `<App />` and verifies it mounts
2. Configure ESLint + Prettier, verify they pass

**Implementation:**
1. Initialize Vite project with React+TS template in `frontend/`
2. Install production deps: `react@19 react-dom@19 @assistant-ui/react @assistant-ui/react-ai-sdk ai zod`
3. Install dev deps: `vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event msw jsdom eslint prettier @vitejs/plugin-react tailwindcss @tailwindcss/vite`
4. Configure `vite.config.ts`: React plugin, Tailwind plugin, proxy `/api` → `http://localhost:8080`, build outDir `../backend/src/main/resources/static/`
5. Configure `tsconfig.json` with strict mode, path aliases
6. Initialize Shadcn/ui (`npx shadcn@latest init`) — use Context7 for current docs
7. Configure Tailwind with Sinsay design tokens: brand colors (#16181D, #E09243, #E90000, #0BB407), text colors, bg colors, font family (Euclid Circular B with fallbacks), spacing scale
8. Copy `assets/logo.svg` → `frontend/public/logo.svg`, `assets/sinsay-favicon.ico` → `frontend/public/favicon.ico`
9. Create minimal `App.tsx` with placeholder, create `App.test.tsx` smoke test
10. Add scripts to `package.json`: `test`, `lint`, `format:check`

**Verify:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: initialize Vite + React 19 + Tailwind + Shadcn project`

---

## Phase 2: Data Layer

### BE-2: JPA entities and repositories
- **Agent:** `be-developer`
- **Depends on:** BE-1
- **Parallel with:** FE-2, FE-3

**Context to provide:**
- Data models from ADR: Session (UUID, Intent enum, orderNumber, productName, description, createdAt), ChatMessage (UUID, sessionId FK, Role enum, content TEXT, sequenceNumber, createdAt)
- Use Lombok for boilerplate reduction
- H2 in-memory for tests (application-test.properties already configured)

**Spec references to include in prompt:**
- `docs/ADR/000-main-architecture.md` §5 — Data Models (exact field definitions)
- `docs/ADR/001-backend.md` §3 — Repositories
- `backend/src/test/CLAUDE.md` — test guidelines

**TDD steps:**
1. Write `SessionRepositoryTests`: save Session, find by ID, verify all fields
2. Write `ChatMessageRepositoryTests`: save multiple messages for a session, verify `findBySessionIdOrderBySequenceNumberAsc` returns correct order
3. Run tests — they should fail (entities don't exist yet)
4. Implement entities and repositories
5. Run tests — they should pass

**Implementation:**
1. Create `com.sinsay.model.Intent` enum: `RETURN`, `COMPLAINT`
2. Create `com.sinsay.model.Role` enum: `USER`, `ASSISTANT`
3. Create `com.sinsay.model.Session` JPA entity with Lombok `@Data`, `@Entity`, UUID id with `@GeneratedValue`, Intent enum with `@Enumerated(STRING)`, other fields, `@Column(columnDefinition = "TEXT")` for description, `createdAt` with `@PrePersist`
4. Create `com.sinsay.model.ChatMessage` JPA entity similarly, with sessionId (UUID FK), sequenceNumber (Integer)
5. Create `SessionRepository extends JpaRepository<Session, UUID>`
6. Create `ChatMessageRepository extends JpaRepository<ChatMessage, UUID>` with custom query method `findBySessionIdOrderBySequenceNumberAsc(UUID sessionId)`

**Verify:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add JPA entities and repositories for Session and ChatMessage`

---

## Phase 3: Core Services + UI Components

### BE-3: PolicyDocService
- **Agent:** `be-developer`
- **Depends on:** BE-1
- **Parallel with:** FE-2, FE-3

**Context to provide:**
- Must load policy .md files from disk and assemble a 6-section system prompt
- Policy doc selection: RETURN → regulamin.md + zwrot-30-dni.md, COMPLAINT → regulamin.md + reklamacje.md
- System prompt structure (6 sections in order): role definition, decision categories, mandatory disclaimer, scope boundary, language instruction, policy doc content
- Path configurable via `policy-docs.path` property (default `../docs`)

**Spec references to include in prompt:**
- `docs/ADR/001-backend.md` §3 (PolicyDocService section) and §6 (System Prompt Structure — all 6 sections defined)
- `backend/AGENTS.md` — System Prompt Structure section with exact section content

**TDD steps (TAC-BE-01, TAC-BE-02):**
1. `getSystemPrompt(RETURN)` → result contains "regulamin" content and "zwrot" content, does NOT contain unique reklamacje text
2. `getSystemPrompt(COMPLAINT)` → result contains "regulamin" content and "reklamacje" content, does NOT contain unique zwrot text
3. System prompt contains the role definition string, disclaimer text, "Polish" / "polsk" language instruction
4. Missing file → descriptive exception

**Implementation:**
1. Create `PolicyDocService` as `@Service` with `@Value("${policy-docs.path:../docs}")` for doc directory
2. Load files using `Files.readString(Path)` — lazy load on first call or at startup
3. `getSystemPrompt(Intent intent)` → concatenates: role definition + decision categories + disclaimer + scope + language + relevant policy docs
4. Use `Intent` enum to select which docs to include

**Verify:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add PolicyDocService for intent-based system prompt assembly`

---

### BE-5: ChatService with Vercel stream encoder
- **Agent:** `be-developer`
- **Depends on:** BE-2, BE-3
- **Parallel with:** FE-2, FE-3

**Context to provide:**
- Must produce Vercel Data Stream Protocol format: `0:"text"\n` for each chunk, `d:{"finishReason":"stop"}\n` at end
- Escape rules: `"` → `\"`, newline → `\\n`
- Uses ResponseBodyEmitter (not Flux)
- OpenAI SDK streaming: `client.chat().completions().createStreaming(params)`
- Must persist USER message before streaming and ASSISTANT message after stream completes

**Spec references to include in prompt:**
- `docs/ADR/001-backend.md` §3 (ChatService section) and §5 (Vercel Data Stream Format — exact format rules)
- `backend/AGENTS.md` — Chat Continuation section + Vercel format section
- Use Context7 for `/openai/openai-java` — streaming API reference

**TDD steps (TAC-BE-04, TAC-BE-05, TAC-BE-06):**
1. Unit test `VercelStreamEncoder.encodeTextChunk("Hello")` → `0:"Hello"\n`
2. Unit test encoder with text containing `"` → properly escaped `\"`
3. Unit test encoder with text containing newline → `\\n`
4. Unit test `VercelStreamEncoder.encodeFinish()` → `d:{"finishReason":"stop"}\n`
5. Integration test: after `streamResponse()` completes (with mocked OpenAI client), verify ASSISTANT message is persisted with full accumulated content
6. Verify USER message is persisted before streaming starts

**Implementation:**
1. Create `com.sinsay.service.VercelStreamEncoder` utility: `encodeTextChunk(String text)` and `encodeFinish()` static methods
2. Create `com.sinsay.service.ChatService` with injected dependencies
3. Method `streamResponse(Session, List<ChatMessage>, String newMessage, ResponseBodyEmitter)`:
   - Persist new USER ChatMessage
   - Build ChatCompletionCreateParams (system prompt + full history + new message)
   - Call OpenAI streaming API
   - For each delta chunk: encode + write to emitter
   - On completion: write finish line, persist ASSISTANT message, emitter.complete()

**Verify:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add ChatService with Vercel data stream format streaming`

---

### BE-4: OpenAI configuration and AnalysisService
- **Agent:** `be-developer`
- **Depends on:** BE-2, BE-3
- **Parallel with:** FE-2, FE-3

**Context to provide:**
- OpenAI Java SDK configured with `OPENAI_API_KEY` and `OPENAI_BASE_URL` from environment
- Model from `openai.model` property (default `openai/gpt-4o-mini`)
- AnalysisService: receives multipart form data, base64-encodes image, calls OpenAI synchronously (non-streaming), persists Session + 2 ChatMessages
- Image sent as base64 data URI in user message content array (2 parts: image + text)

**Spec references to include in prompt:**
- `docs/ADR/001-backend.md` §3 (OpenAIConfig, AnalysisService sections) and §4 (Request DTOs, OpenAI API payload — initial analysis)
- `backend/AGENTS.md` — Initial Analysis section, env vars
- Use Context7 for `/openai/openai-java` — client builder, chat completions, vision/image content

**TDD steps (TAC-BE-03, TAC-BE-08):**
1. Test OpenAIConfig creates bean with configured baseUrl and apiKey (use `@SpringBootTest` with test properties)
2. Test AnalysisService builds user message with exactly 2 content parts (image + text) — mock OpenAIClient, capture the params
3. Test base64 encoding produces valid data URI format (`data:image/jpeg;base64,...`)
4. Test Session is persisted with correct fields after analysis
5. Test 2 ChatMessages created: role=USER (seq=0) with description text, role=ASSISTANT (seq=1) with AI response

**Implementation:**
1. Create `com.sinsay.config.OpenAIConfig` (@Configuration): creates `OpenAIClient` bean via `OpenAIOkHttpClient.builder()`, reads apiKey and baseUrl from environment
2. Create `com.sinsay.service.AnalysisService`:
   - Inject OpenAIClient, PolicyDocService, SessionRepository, ChatMessageRepository
   - `analyze(Intent, String orderNumber, String productName, String description, MultipartFile image)`:
     - Base64 encode: `Base64.getEncoder().encodeToString(image.getBytes())`
     - Build data URI: `"data:" + image.getContentType() + ";base64," + base64`
     - Build system message from PolicyDocService
     - Build user message with image content block + text content block
     - Call `client.chat().completions().create(params)` synchronously
     - Create and save Session entity
     - Create and save USER ChatMessage (seq=0) and ASSISTANT ChatMessage (seq=1)
     - Return sessionId + assistant message text

**Verify:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add OpenAI config and AnalysisService for initial form analysis`

---

### FE-2: useSession hook and form validation
- **Agent:** `fe-developer`
- **Depends on:** FE-1
- **Parallel with:** BE-2, BE-3, BE-4, BE-5

**Context to provide:**
- localStorage key: `sinsay_session_id`
- Zod validation for form fields: intent (RETURN|COMPLAINT, required), orderNumber (string, required), productName (string, required), description (string, required)
- Image validation is separate (File object, not serializable by Zod): MIME types image/jpeg, image/png, image/webp, image/gif; max 10MB (10,485,760 bytes)

**Spec references to include in prompt:**
- `docs/ADR/002-frontend.md` §3 (useSession hook) and §4 (Form state, Validation error state, localStorage entry)
- `frontend/AGENTS.md` — Form Fields table, Session Flow section
- `frontend/tests/CLAUDE.md` — useSession test scenarios

**TDD steps:**
1. `useSession` hook tests: returns null initially, `setSessionId("abc")` writes to localStorage, `clearSession()` removes it, reads existing value on init
2. Zod schema tests: rejects missing intent, rejects empty orderNumber, accepts all-valid data
3. Image validation tests: accepts image/jpeg, rejects application/pdf, rejects file > 10MB

**Implementation:**
1. Create `src/hooks/useSession.ts`: `useSession()` → `{ sessionId, setSessionId, clearSession }`
2. Create `src/lib/validation.ts`: Zod schema for form fields + `validateImage(file: File)` function returning error string or null
3. Export types: `FormData`, `FormErrors`

**Verify:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: add useSession hook and Zod form validation`

---

### FE-3: ImageUpload component
- **Agent:** `fe-developer`
- **Depends on:** FE-1
- **Parallel with:** BE-2, BE-3, BE-4, BE-5, FE-2

**Context to provide:**
- Drag-and-drop zone with click-to-browse fallback
- Validates MIME type and size on file selection (immediate error, before form submit)
- Shows thumbnail preview via `URL.createObjectURL` and filename after valid selection
- Resizes to max 1024px on longest side before reporting to parent (canvas resize)
- Has "remove" button to clear selection
- Accepted formats: JPEG, PNG, WebP, GIF. Max size: 10 MB
- Error text in Polish: "Dozwolone formaty: JPEG, PNG, WebP, GIF" and "Maksymalny rozmiar pliku: 10 MB"

**Spec references to include in prompt:**
- `docs/ADR/002-frontend.md` §3 (Image upload sub-component)
- `frontend/AGENTS.md` — Image Handling section
- `frontend/tests/CLAUDE.md` — ImageUpload test scenarios
- `docs/PRD-Product-Requirements-Document.md` §9 (Screen 1 — Zdjęcie produktu field description) and §6 (AC-03, AC-04)

**TDD steps (TAC-FE-02, TAC-FE-03):**
1. Renders drop zone with accepted format/size text
2. Valid JPEG selection → thumbnail preview shown, filename displayed, no error
3. PDF file → error "Dozwolone formaty: JPEG, PNG, WebP, GIF" shown immediately
4. File > 10MB → error mentioning "10 MB" shown immediately
5. "Remove"/"Usuń" button click → selection cleared, drop zone shown again
6. `onChange` callback called with File on valid selection, null on remove

**Implementation:**
1. Create `src/components/ImageUpload.tsx`
2. Props: `value: File | null`, `onChange: (file: File | null) => void`, `error?: string`
3. Drag-and-drop via native HTML drag events + hidden file input
4. MIME/size validation on `onDrop` and `onChange`
5. Canvas resize utility for max 1024px dimension
6. Thumbnail via `URL.createObjectURL`

**Verify:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: add ImageUpload component with validation and preview`

---

## Phase 4: Controllers + Frontend Views

### BE-6: SessionController
- **Agent:** `be-developer`
- **Depends on:** BE-4
- **Parallel with:** FE-4

**Context to provide:**
- Two endpoints: `POST /api/sessions` (multipart) and `GET /api/sessions/{id}` (JSON)
- POST validates: all 5 fields required, image MIME type (jpeg/png/webp/gif only), image size (max 10MB)
- POST delegates to AnalysisService, returns `{sessionId, message}`
- GET returns `{session: {...}, messages: [...]}` ordered by sequenceNumber
- GET returns 404 for unknown UUID
- Also add WebConfig for CORS (allow localhost:5173 in dev profile)

**Spec references to include in prompt:**
- `docs/ADR/000-main-architecture.md` §6 — API Contracts (POST /api/sessions, GET /api/sessions/{id})
- `docs/ADR/001-backend.md` §3 (Controllers section) and §4 (Request/Response DTOs)
- `backend/AGENTS.md` — API Contracts table

**TDD steps (covers TAC-01 through TAC-08):**
1. POST with valid multipart → 200, body has `sessionId` (UUID format) and non-empty `message` (mock AnalysisService)
2. POST missing `intent` → 400
3. POST with `intent=INVALID` → 400
4. POST missing `image` → 400
5. POST with `content-type=application/pdf` image → 400
6. POST with image > 10MB → 400
7. GET with valid ID → 200, body has `session` object and `messages` array in order
8. GET with unknown UUID → 404

**Implementation:**
1. Create response DTOs: `SessionResponse(sessionId, message)`, `SessionLoadResponse(session, messages)`, `MessageDto(id, role, content, sequenceNumber)`
2. Create `SessionController` with `@RestController @RequestMapping("/api/sessions")`
3. POST: validate multipart params, validate image content type and size, delegate to AnalysisService
4. GET: find session, load messages, map to DTOs
5. Create `WebConfig` (@Configuration): CORS allow `http://localhost:5173` for dev

**Verify:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add SessionController with form submission and session load endpoints`

---

### BE-7: ChatController
- **Agent:** `be-developer`
- **Depends on:** BE-5, BE-6
- **Parallel with:** FE-5

**Context to provide:**
- Endpoint: `POST /api/sessions/{id}/messages` — receives JSON `{content}`, returns streaming `text/plain;charset=UTF-8`
- Must set header `X-Vercel-AI-Data-Stream: v1`
- Uses ResponseBodyEmitter, delegates streaming to ChatService on an async executor
- Returns 404 if session not found, 400 if content empty

**Spec references to include in prompt:**
- `docs/ADR/000-main-architecture.md` §6 — chat endpoint contract
- `docs/ADR/001-backend.md` §3 (ChatController section) and §5 (Vercel Data Stream Format)
- `backend/AGENTS.md` — Chat Continuation section

**TDD steps (TAC-09, TAC-10, TAC-11):**
1. POST with valid content → response `Content-Type: text/plain`, has `X-Vercel-AI-Data-Stream: v1` header
2. Response body contains `0:"..."` lines (mock ChatService to write test data to emitter)
3. Response ends with `d:{"finishReason":"stop"}\n`
4. After streaming: USER and ASSISTANT messages persisted in DB
5. POST with unknown sessionId → 404
6. POST with empty/missing content → 400

**Implementation:**
1. Create `ChatRequest` DTO: `String content`
2. Create `ChatController` with `@RestController`
3. POST endpoint: load session + history, create `ResponseBodyEmitter(0L)` (no timeout for streaming), set content type + headers, submit async task that calls `ChatService.streamResponse()`, return emitter
4. Error handling: session not found → 404, validation → 400

**Verify:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add ChatController with streaming chat endpoint`

---

### FE-4: IntakeForm component
- **Agent:** `fe-developer`
- **Depends on:** FE-2, FE-3
- **Parallel with:** BE-6, BE-7

**Context to provide:**
- 5-field form: intent (radio: "Zwrot"/"Reklamacja"), orderNumber (text), productName (text), description (textarea), image (ImageUpload component)
- Validation with Zod on submit (not on keystroke). Inline errors per field in Polish.
- Submit as multipart/form-data to `POST /api/sessions`
- Loading state: button text "Analizuję...", disabled, spinner
- On success: receive `{sessionId, message}`, call `onSuccess(sessionId, message, formData)` callback
- On API error: show error near submit button, re-enable form
- All labels/placeholders in Polish per PRD §9

**Spec references to include in prompt:**
- `docs/PRD-Product-Requirements-Document.md` §9 (Screen 1 — exact field descriptions, placeholders, button text)
- `docs/ADR/002-frontend.md` §3 (IntakeForm component) and §5 (Form → Chat Transition)
- `frontend/AGENTS.md` — Form Fields table
- `frontend/tests/CLAUDE.md` — IntakeForm test scenarios
- `docs/design-guidelines.md` — Primary CTA button style, form layout

**TDD steps (TAC-FE-01, TAC-FE-04, TAC-FE-08):**
1. Renders all 5 fields with Polish labels ("Typ zgłoszenia", "Numer zamówienia", "Nazwa produktu", "Opis problemu", "Zdjęcie produktu")
2. Submit with all empty → 5 inline error messages visible
3. Submit with valid data (MSW mock POST /api/sessions → 200): button shows "Analizuję...", becomes disabled
4. On MSW success response: `localStorage.getItem('sinsay_session_id')` returns the UUID from response
5. On MSW success: `onSuccess` callback called with sessionId and message
6. On MSW error (500): error shown near submit button, button re-enabled

**Implementation:**
1. Create `src/components/IntakeForm.tsx`
2. Use Shadcn/ui RadioGroup for intent, Input for text fields, Textarea for description, ImageUpload for image
3. Form state managed with useState, errors with Zod validation
4. Submit: build FormData, fetch POST /api/sessions, handle response
5. All user-facing strings in Polish

**Verify:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: add IntakeForm component with validation and submission`

---

### FE-5: ChatView component
- **Agent:** `fe-developer`
- **Depends on:** FE-2
- **Parallel with:** BE-6, BE-7, FE-4

**Context to provide:**
- Uses `useChatRuntime` from `@assistant-ui/react-ai-sdk` with `api: /api/sessions/${sessionId}/messages`
- Wraps in `AssistantRuntimeProvider`, renders assistant-ui `Thread` component
- Summary bar at top showing intent, productName, orderNumber (read-only, collapsed)
- "Nowa sesja" button in top-right
- On mount (session resume): fetch `GET /api/sessions/{sessionId}`, map messages to `{id, role, content}` format, pass as `initialMessages`
- On 404 from GET: call `onSessionInvalid` to clear state

**Spec references to include in prompt:**
- `docs/ADR/002-frontend.md` §3 (ChatView, ChatRuntime setup) and §4 (Message format)
- `frontend/AGENTS.md` — Chat Integration section
- Use Context7 for `/assistant-ui/assistant-ui` and `/vercel/ai` — runtime setup, Thread component

**TDD steps (TAC-FE-05, TAC-FE-06, TAC-FE-07):**
1. Renders summary bar with intent, productName, orderNumber from props
2. Renders initial messages when provided as props
3. "Nowa sesja" button click → `onNewSession` callback fired
4. On mount with sessionId (MSW mock GET /api/sessions/{id} → history): renders loaded messages
5. On mount with sessionId (MSW mock GET → 404): `onSessionInvalid` callback fired

**Implementation:**
1. Create `src/components/ChatView.tsx`
2. Props: `sessionId`, `initialMessages?`, `sessionInfo?` (intent, productName, orderNumber), `onNewSession`, `onSessionInvalid`
3. On mount (no initialMessages): fetch session from API, map messages
4. Setup `useChatRuntime({ api, initialMessages })`
5. Render `AssistantRuntimeProvider` + `Thread` + summary bar + "Nowa sesja" button

**Verify:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: add ChatView component with assistant-ui streaming runtime`

---

## Phase 5: Integration + Design

### FE-6: App.tsx routing and session flow
- **Agent:** `fe-developer`
- **Depends on:** FE-4, FE-5
- **Parallel with:** BE-8

**Context to provide:**
- App.tsx is the root — single state controls which view is shown: `{ view: 'form' | 'chat', sessionId: string | null }`
- No routing library — just conditional rendering
- On mount: check localStorage for sessionId → if found, show ChatView; if not, show IntakeForm
- Form success: store sessionId, switch to ChatView with initial messages
- "Nowa sesja": clear localStorage, switch to IntakeForm
- Session invalid (404): clear localStorage, switch to IntakeForm

**Spec references to include in prompt:**
- `docs/ADR/002-frontend.md` §3 (App.tsx) and §5 (Form → Chat Transition, Session Resume flows)
- `frontend/AGENTS.md` — Session Flow section

**TDD steps:**
1. No sessionId in localStorage → IntakeForm rendered, no ChatView
2. sessionId in localStorage (MSW mock GET → valid session) → ChatView rendered, no IntakeForm
3. Form onSuccess → view switches to ChatView, localStorage has sessionId
4. "Nowa sesja" → view switches to IntakeForm, localStorage cleared
5. Session 404 → IntakeForm rendered, localStorage cleared

**Implementation:**
1. Update `App.tsx`: use `useSession` hook for state
2. Conditional rendering based on view state
3. Wire callbacks: `handleFormSuccess`, `handleNewSession`, `handleSessionInvalid`
4. Pass initial messages from form response to ChatView on transition

**Verify:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: wire App.tsx with form-to-chat routing and session management`

---

### FE-7: Apply Sinsay design system and responsive polish
- **Agent:** `fe-developer`
- **Depends on:** FE-6
- **Parallel with:** BE-8

**Context to provide:**
- Sinsay visual identity: clean, minimalist fashion retail aesthetic
- Sharp corners on buttons (border-radius: 0), accent orange (#E09243) for CTA buttons
- Font: Euclid Circular B (with web fallbacks: Arial, Helvetica, sans-serif) — note: actual font files may not be available, use the fallback stack
- Primary button: bg #E09243, text white, no border-radius, padding 12px 32px, font-weight 600
- Form layout: centered, Sinsay logo at top, heading "Sprawdź zwrot lub reklamację"
- Chat layout: summary bar at top, messages differentiated (user right, AI left), fixed input at bottom
- Must work on 375px mobile viewport (AC-20)

**Spec references to include in prompt:**
- `docs/design-guidelines.md` — full design system reference
- `assets/design-tokens.json` — exact color/spacing/radius values
- `docs/PRD-Product-Requirements-Document.md` §9 (UI Description — both screens)

**TDD steps (TAC-FE-08, TAC-FE-09):**
1. All user-visible text in Polish (check key labels exist in rendered output)
2. Form and chat usable at 375px viewport (check no horizontal overflow)
3. Sinsay logo renders in form view

**Implementation:**
1. Verify Tailwind config has correct design tokens (from FE-1)
2. Style IntakeForm: centered max-width container, logo at top, heading, field styling, primary CTA button with Sinsay styling
3. Style ChatView: summary bar styling, message bubble differentiation, fixed bottom input
4. Style ImageUpload: drag-drop zone with dashed border, preview styling
5. Add responsive breakpoints: 375px minimum, form stacks vertically on mobile
6. Verify all text strings are Polish

**Verify:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build`
**Commit:** `Frontend: apply Sinsay design system with brand colors, typography, and responsive layout`

---

### BE-8: Full-flow integration test
- **Agent:** `be-developer`
- **Depends on:** BE-7
- **Parallel with:** FE-6, FE-7

**Context to provide:**
- Write a comprehensive integration test that exercises the full API flow: create session → load session → send chat message → verify DB state
- Use `@SpringBootTest(webEnvironment = RANDOM_PORT)` with `TestRestTemplate`
- Mock OpenAI client bean with `@MockitoBean`
- Use H2 test profile

**Spec references to include in prompt:**
- `docs/ADR/000-main-architecture.md` §10 — TAC-01 through TAC-12
- `backend/src/test/CLAUDE.md` — test guidelines

**TDD steps:**
1. POST /api/sessions with valid multipart (mock OpenAI response) → 200, get sessionId
2. GET /api/sessions/{sessionId} → 200, session fields match, messages in order
3. POST /api/sessions/{sessionId}/messages with `{content: "test"}` (mock OpenAI streaming) → streaming response in Vercel format
4. GET /api/sessions/{sessionId} again → now has 4 messages (initial USER+ASSISTANT + chat USER+ASSISTANT)

**Implementation:**
1. Create `FullFlowIntegrationTests` using `@SpringBootTest` with random port
2. Configure mock OpenAI client to return test responses
3. Test complete session lifecycle
4. Verify Vercel stream format in response body
5. Verify DB persistence after each step

**Verify:** `cd backend && ./mvnw test && ./mvnw clean package`
**Commit:** `Backend: add full-flow integration test for session lifecycle`

---

## Phase 6: E2E Tests

### QA-1: Playwright setup and form validation E2E
- **Agent:** `qa-engineer`
- **Depends on:** FE-7, BE-8
- **Parallel with:** none

**Context to provide:**
- Set up Playwright in the project (install, config)
- Test against frontend dev server (Vite on 5173) with API routes mocked via `page.route()`
- Form has 5 fields, all required. Polish labels.
- AC-01 through AC-06 from PRD define form behavior

**Spec references to include in prompt:**
- `docs/PRD-Product-Requirements-Document.md` §6 (AC-01 through AC-06)
- `frontend/tests/e2e/CLAUDE.md` — E2E framework guidelines
- `frontend/tests/e2e/AGENTS.md`

**Implementation (tests ARE the deliverable):**
1. Install Playwright: `npm init playwright@latest` in frontend/
2. Configure `playwright.config.ts`: base URL `http://localhost:5173`, use `page.route()` for API mocking
3. Test: page loads, form renders with all 5 fields
4. Test: empty submit → 5 validation error messages visible
5. Test: upload PDF → format error message
6. Test: upload >10MB file → size error message
7. Test: valid submit → loading state on button ("Analizuję...")

**Verify:** `npx playwright test`
**Commit:** `QA: add Playwright setup and form validation E2E tests`

---

### QA-2: Form-to-chat flow E2E
- **Agent:** `qa-engineer`
- **Depends on:** QA-1

**Context to provide:**
- After valid form submit (mocked API returns `{sessionId, message}`), chat view should appear
- sessionId should be in localStorage
- Chat input should be visible and functional
- "Nowa sesja" button returns to form, clears localStorage

**Spec references to include in prompt:**
- `docs/PRD-Product-Requirements-Document.md` §6 (AC-07, AC-12, AC-15, AC-18)
- `docs/ADR/002-frontend.md` §5 (Form → Chat Transition)

**Implementation:**
1. Test: fill valid form + submit (mock API) → chat view appears with AI message
2. Test: sessionId is in localStorage after submit
3. Test: chat input visible, can type text
4. Test: "Nowa sesja" button → form appears, localStorage cleared
5. Test: mock streaming response → assistant message appears in chat

**Verify:** `npx playwright test`
**Commit:** `QA: add form-to-chat flow E2E tests`

---

### QA-3: Session resume and responsive E2E
- **Agent:** `qa-engineer`
- **Depends on:** QA-2

**Context to provide:**
- Setting sessionId in localStorage before page load should show chat with loaded history
- Invalid sessionId (API 404) should clear localStorage and show form
- App must work on 375px viewport
- All UI text in Polish

**Spec references to include in prompt:**
- `docs/PRD-Product-Requirements-Document.md` §6 (AC-15 through AC-20)
- `docs/ADR/002-frontend.md` §7 (TAC-FE-05, TAC-FE-06, TAC-FE-09)

**Implementation:**
1. Test: set `sinsay_session_id` in localStorage → reload → mock GET returns history → chat renders with messages
2. Test: set invalid sessionId → reload → mock GET returns 404 → form renders, localStorage cleared
3. Test: 375px viewport → form renders without horizontal scroll
4. Test: 375px viewport → chat renders without horizontal scroll
5. Test: verify key Polish text strings present (form labels, button text)

**Verify:** `npx playwright test`
**Commit:** `QA: add session resume and responsive E2E tests`

---

## Verification Checklist (Final)

After all tasks complete, verify the full system:

1. **Backend:** `cd backend && ./mvnw clean test && ./mvnw clean package` — all tests pass, JAR builds
2. **Frontend:** `cd frontend && npm test && npm run lint && npm run format:check && npm run build` — all checks pass, static files land in backend/src/main/resources/static/
3. **E2E:** `cd frontend && npx playwright test` — all Playwright tests pass
4. **Manual smoke:** Start backend with `OPENAI_API_KEY`, open browser, submit form, verify chat works with real AI responses
