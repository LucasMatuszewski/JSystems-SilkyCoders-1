# ADR: Backend — Spring Boot, OpenAI SDK, Streaming, Persistence

**Date:** 2026-03-27
**Status:** Accepted
**Relates to:** `docs/ADR/000-main-architecture.md`

---

## 1. Scope

Backend implementation: Spring Boot application, OpenAI Java SDK configuration for OpenRouter, multimodal image analysis, Vercel data stream format over SSE, policy document loading, session and chat message persistence in SQLite.

Does NOT cover: frontend, UI protocol choices, React components.

---

## 2. Context7 References

| Library | Context7 Handle | Used for |
|---|---|---|
| OpenAI Java SDK | `/openai/openai-java` | Chat completions, vision, streaming |
| Spring Boot | `/spring-projects/spring-boot` | Web MVC, JPA, config, DI |
| Lombok | `/projectlombok/lombok` | Entity/DTO boilerplate reduction |

---

## 3. Component Design

### Controllers (HTTP layer)
**SessionController** handles:
- `POST /api/sessions` — validate multipart input, delegate to `AnalysisService`, return JSON
- `GET /api/sessions/{id}` — load from DB, return session + message history

**ChatController** handles:
- `POST /api/sessions/{id}/messages` — validate, load session+history, delegate to `ChatService`, return `ResponseBodyEmitter`

Controllers do no business logic. They validate input, call services, and map responses.

### Services (business logic)
**AnalysisService**
- Receives: validated form data (intent, orderNumber, productName, description, MultipartFile image)
- Converts image to base64 data URI
- Calls `PolicyDocService.getSystemPrompt(intent)` to get system prompt
- Builds `ChatCompletionCreateParams` with: system message (policy docs), user message (description + image content block)
- Calls `OpenAIClient.chat().completions().create(params)` (synchronous, not streaming)
- Creates and persists `Session` entity
- Persists initial USER message (the description) and ASSISTANT message (the AI response)
- Returns `{ sessionId, message }` to controller

**ChatService**
- Receives: session entity, message history (ordered list), new user message text
- Builds `ChatCompletionCreateParams` with: system message (policy docs re-loaded), full history as USER/ASSISTANT messages, new user message
- Persists new USER message before calling API
- Calls `OpenAIClient.chat().completions().createStreaming(params)`
- For each chunk: extracts delta text, encodes as Vercel data stream line, writes to `ResponseBodyEmitter`
- After stream completes: writes finish line, calls `emitter.complete()`, persists full ASSISTANT message

**PolicyDocService**
- Reads `regulamin.md` from disk on application startup (or lazily — either is acceptable for PoC)
- Reads `reklamacje.md` and `zwrot-30-dni.md` similarly
- `getSystemPrompt(RETURN)` → concatenate: agent role description + regulamin content + zwrot-30-dni content + disclaimer instructions
- `getSystemPrompt(COMPLAINT)` → concatenate: agent role description + regulamin content + reklamacje content + disclaimer instructions
- Policy files path controlled by `POLICY_DOCS_PATH` environment variable (default: `./docs`)

### Configuration
**OpenAIConfig** (Spring @Configuration)
- Creates `OpenAIClient` bean using `OpenAIOkHttpClient.builder()` configured with `OPENAI_API_KEY`, `OPENAI_BASE_URL` from Spring environment
- Model name read from `OPENAI_MODEL` property (default: `openai/gpt-4o-mini`)

**WebConfig** (Spring @Configuration)
- CORS: allow `http://localhost:5173` in dev profile only

### Repositories (JPA)
- `SessionRepository extends JpaRepository<Session, UUID>`
- `ChatMessageRepository extends JpaRepository<ChatMessage, UUID>` with finder: `findBySessionIdOrderBySequenceNumberAsc(UUID sessionId)`

---

## 4. Data Structures

### Request DTOs (from HTTP layer)

**Initial analysis request** (multipart fields):
- `intent`: String, required, must be "RETURN" or "COMPLAINT"
- `orderNumber`: String, required, max 100 chars
- `productName`: String, required, max 255 chars
- `description`: String, required, max 5000 chars
- `image`: MultipartFile, required, max 10 MB, content type must be one of: `image/jpeg`, `image/png`, `image/webp`, `image/gif`

**Chat message request** (JSON body):
- `content`: String, required, max 5000 chars

### Response DTOs

**Initial analysis response:**
```
{
  "sessionId": "uuid-string",
  "message": "AI decision text..."
}
```

**Session load response:**
```
{
  "session": {
    "id": "uuid",
    "intent": "RETURN" | "COMPLAINT",
    "orderNumber": "string",
    "productName": "string",
    "description": "string",
    "createdAt": "ISO-8601 datetime"
  },
  "messages": [
    {
      "id": "uuid",
      "role": "USER" | "ASSISTANT",
      "content": "string",
      "sequenceNumber": 0
    }
  ]
}
```

### OpenAI API payload — initial analysis (conceptual)
The request to OpenAI for the initial analysis includes:
- `system` message: system prompt string (role description + policy docs + disclaimer instructions)
- `user` message with two content parts:
  1. Image content block: base64-encoded image as data URI (`data:<mimetype>;base64,<data>`)
  2. Text content block: user's description of the problem

### OpenAI API payload — chat continuation (conceptual)
- `system` message: same policy-based system prompt (re-loaded from disk, not stored in DB)
- `user`/`assistant` messages: full history loaded from DB, in sequenceNumber order
- Final `user` message: new message from user

---

## 5. Vercel Data Stream Format (Backend Responsibility)

The chat continuation endpoint must produce this exact format:

```
Content-Type: text/plain;charset=UTF-8
Transfer-Encoding: chunked
X-Vercel-AI-Data-Stream: v1

0:"Hello"\n
0:" there"\n
0:"!"\n
d:{"finishReason":"stop"}\n
```

Rules for encoding:
- Each text delta from OpenAI is JSON-escaped and wrapped: `0:"<escaped_text>"\n`
- Quotation marks inside text must be escaped: `"` → `\"`
- Newlines inside text must be escaped: `\n` → `\\n`
- The stream MUST end with the finish line: `d:{"finishReason":"stop"}\n`
- Each line ends with exactly one `\n` (not `\r\n`)

`ResponseBodyEmitter` is used (Spring MVC). It is returned from the controller immediately. A separate thread (from a thread pool) drives the OpenAI async stream and writes to the emitter. On stream completion or error, `emitter.complete()` or `emitter.completeWithError(e)` is called.

---

## 6. System Prompt Structure (Agent Instructions)

The system prompt assembled by `PolicyDocService` must contain these sections in order:

1. **Role definition**: "You are an AI assistant for Sinsay online store. Your purpose is to help customers estimate whether their return (zwrot) or complaint (reklamacja) is likely to be accepted based on Sinsay's policies and the product photo provided."

2. **Decision categories**: Define the three decision outcomes the agent must use — "Likely accepted", "Likely rejected", "Unclear — requires manual review".

3. **Mandatory disclaimer**: Every initial analysis response must state that the assessment is not legally binding and that the final decision is always made by a human Sinsay customer support agent.

4. **Scope boundary**: Answer questions about Sinsay policies, return/complaint procedures, and related topics based only on the provided documents. Redirect off-topic questions.

5. **Language**: Always respond in Polish.

6. **Policy document content**: Full content of the relevant `.md` files (appended as plain text).

---

## 7. Technical Decisions

### WebMVC vs WebFlux for streaming
**Status:** Accepted
**Context:** Chat endpoint must stream OpenAI response tokens to the client. Spring offers WebFlux (reactive) and WebMVC + `ResponseBodyEmitter` (servlet-based).
**Decision:** Spring WebMVC with `ResponseBodyEmitter`. The OpenAI Java SDK's async streaming (`createStreaming`) runs on its own thread pool. We bridge it to `ResponseBodyEmitter` from a `@Async` thread or executor service. WebMVC is simpler to set up and test; the reactive overhead of WebFlux is unnecessary for PoC.
**Rejected alternatives:**
- WebFlux + `Flux<String>`: More elegant for streaming but requires full reactive stack (different testing, config, driver compatibility for SQLite).
**Consequences:**
- (+) Simpler stack, familiar programming model, easier integration testing
- (-) Blocking thread per open SSE connection — acceptable for PoC load (~handful of concurrent users)
**Review trigger:** If concurrent users exceed ~50 or if reactive features become needed elsewhere in the system.

---

### Image encoding: base64 in JSON vs multipart forward
**Status:** Accepted
**Context:** OpenAI API accepts images either as URLs or base64 data URIs in the message content array. The initial form sends a `MultipartFile` to the backend.
**Decision:** Backend receives image as `MultipartFile`, reads bytes, encodes to base64, constructs data URI (`data:image/jpeg;base64,...`), and passes it in the OpenAI request content array. The image is never stored on disk by the backend.
**Rejected alternatives:**
- Storing image to filesystem and passing URL: Requires public URL accessible by OpenAI — not feasible in local dev without tunneling.
- Forwarding multipart to OpenAI: OpenAI Chat Completions API does not accept multipart — only JSON with base64 or URL.
**Consequences:**
- (+) No file storage, no cleanup needed, works fully locally
- (-) Large images increase memory usage briefly (10 MB image in heap while encoding) — acceptable for PoC
**Review trigger:** If image size limit is raised significantly (>20 MB).

---

### SQLite dialect and JPA config
**Status:** Accepted
**Context:** SQLite is not supported by Hibernate core. A community dialect is required.
**Decision:** Use `org.hibernate.community.dialect.SQLiteDialect` from `hibernate-community-dialects` artifact. JPA auto-creates tables (`spring.jpa.hibernate.ddl-auto=update` in dev, `validate` in prod-like). DB file path: `./sinsay_poc.db` (configurable via `SQLITE_DB_PATH` env var if needed).
**Rejected alternatives:**
- H2 file-mode: Compatible with Hibernate out of the box, but file is not a portable SQLite format — harder to inspect externally.
**Consequences:**
- (+) Standard SQLite format, inspectable with any SQLite tool
- (-) Community dialect may lag behind Hibernate releases; must verify on Hibernate upgrade
**Review trigger:** On any Hibernate version upgrade; if production deployment is planned.

---

## 8. Testing Strategy

### Philosophy
Test backend logic independently of OpenAI API calls. Use Mockito to stub `OpenAIClient`. Use a real in-memory H2 database (SQLite-compatible schema) for integration tests. Do not call real OpenAI/OpenRouter APIs in tests.

### Test layers

| Layer | Type | Tool | Scope |
|---|---|---|---|
| Unit | `PolicyDocService` | JUnit 5 | Correct docs loaded per intent, file not found handling |
| Unit | `AnalysisService` | JUnit 5 + Mockito | System prompt construction, base64 encoding, response mapping |
| Unit | `ChatService` | JUnit 5 + Mockito | Vercel stream format encoding, history reconstruction |
| Unit | Stream format encoder | JUnit 5 | Correct escaping of quotes, newlines, special chars |
| Integration | `POST /api/sessions` | Spring Boot Test + MockMvc + Mockito | Full endpoint: validation, service call, DB write, response shape |
| Integration | `GET /api/sessions/{id}` | MockMvc | Session load, 404 case |
| Integration | `POST /api/sessions/{id}/messages` | MockMvc | Stream format verified, DB persistence after stream |

### Key test scenarios

**PolicyDocService:**
- `getSystemPrompt(RETURN)` → result contains `regulamin.md` content, `zwrot-30-dni.md` content, does NOT contain `reklamacje.md` content
- `getSystemPrompt(COMPLAINT)` → result contains `regulamin.md` content, `reklamacje.md` content, does NOT contain `zwrot-30-dni.md` content
- System prompt contains role definition, disclaimer, and language instruction

**AnalysisService:**
- Base64 encoding produces valid data URI format
- System prompt is passed as system role message to OpenAI client
- Image content block is included in user message alongside text
- Session is persisted with correct intent, orderNumber, productName, description
- Two ChatMessage records created: role=USER (seq 0) and role=ASSISTANT (seq 1)

**ChatService — stream format:**
- Simple text "Hello world" → emits `0:"Hello world"\n` then `d:{"finishReason":"stop"}\n`
- Text containing a double quote `it's "great"` → emits `0:"it's \"great\""\n`
- Text containing newline → emits `0:"line1\\nline2"\n`
- After stream, ASSISTANT message is persisted with full content

**Validation:**
- Missing `intent` field → 400
- `intent` = "INVALID" → 400
- Missing `image` → 400
- Image MIME type `application/pdf` → 400
- Image size 10.1 MB → 400 (test with stub MultipartFile)
- All fields valid → 200

**Session persistence:**
- Session saved with correct UUID
- ChatMessages for session returned in sequenceNumber order
- GET with non-existent UUID → 404

### Technical acceptance criteria (Backend)

- TAC-BE-01: `PolicyDocService.getSystemPrompt(RETURN)` does not contain the string "reklamacj" (from reklamacje.md)
- TAC-BE-02: `PolicyDocService.getSystemPrompt(COMPLAINT)` does not contain the string "zwrot" from zwrot-30-dni.md
- TAC-BE-03: `AnalysisService` builds a user message with exactly 2 content parts: one image type, one text type
- TAC-BE-04: Stream encoder correctly JSON-escapes `"`, `\`, `\n` in text deltas
- TAC-BE-05: Chat endpoint response body ends with `d:{"finishReason":"stop"}\n`
- TAC-BE-06: After streaming completes, `ChatMessageRepository.findBySessionId(id)` returns one additional ASSISTANT message
- TAC-BE-07: Multipart upload with 10,485,761 bytes (10 MB + 1 byte) returns HTTP 400
- TAC-BE-08: `OpenAIClient` bean is configured with `baseUrl` and `apiKey` from environment — verify via `@SpringBootTest` with test properties
