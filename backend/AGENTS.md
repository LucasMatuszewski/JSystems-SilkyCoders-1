# Backend Guidelines

## Tech Stack

- Java 21, Spring Boot 3.5.9, Spring Web MVC, Lombok
- **OpenAI Java SDK** (official) — NOT Spring AI
- LLM provider: OpenRouter (OpenAI-compatible endpoint)
- Persistence: SQLite via Hibernate Community Dialects + JPA
- Build: Maven (`backend/pom.xml`), no root aggregator

## Environment Variables

| Variable | Required | Default |
|---|---|---|
| `OPENAI_API_KEY` | yes | — |
| `OPENAI_BASE_URL` | yes | `https://openrouter.ai/api/v1` |
| `OPENAI_MODEL` | no | `openai/gpt-4o-mini` |
| `POLICY_DOCS_PATH` | no | `../docs` |

## Package Structure

`com.sinsay` — use this package for all new classes:
- `config/` — `WebConfig` (CORS), `OpenAiConfig`
- `controller/` — `SessionController`, `ChatController`
- `service/` — `AnalysisService`, `ChatService`, `PolicyDocService`
- `model/` — `Session`, `ChatMessage` (JPA entities)
- `repository/` — `SessionRepository`, `ChatMessageRepository`

## API Contracts

| Method | Path | Body | Response |
|---|---|---|---|
| `POST` | `/api/sessions` | multipart (intent, orderNumber, productName, description, image) | `{sessionId, message}` JSON |
| `GET` | `/api/sessions/{id}` | — | `{session, messages[]}` JSON |
| `POST` | `/api/sessions/{id}/messages` | `{messages, system, ...}` JSON (from AssistantChatTransport) | `text/event-stream` SSE (UI Message Stream) |

## Data Models

**Session**: `id` (UUID), `intent` (RETURN/COMPLAINT), `orderNumber`, `productName`, `description`, `createdAt`

**ChatMessage**: `id` (UUID), `sessionId` (FK), `role` (USER/ASSISTANT), `content` (TEXT), `sequenceNumber`, `createdAt`

## Vercel AI SDK UI Message Stream Format (CRITICAL)

`POST /api/sessions/{id}/messages` must return `text/event-stream` with SSE encoding and header `x-vercel-ai-ui-message-stream: v1`:

```
data: {"type":"start","messageId":"<uuid>"}

data: {"type":"text-start","id":"<uuid>"}

data: {"type":"text-delta","id":"<uuid>","delta":"Hello"}

data: {"type":"text-delta","id":"<uuid>","delta":" world"}

data: {"type":"text-end","id":"<uuid>"}
```

Each SSE event is `data: <JSON>\n\n`. The `messageId`/`id` should be the same UUID across all events for one assistant message. Use `SseEmitter` (not `Flux` or plain `ResponseBodyEmitter`).

## Initial Analysis (Non-Streaming)

`POST /api/sessions` uses `AnalysisService`:
1. Validate multipart fields (all required) + image (JPEG/PNG/WebP/GIF, max 10MB)
2. Base64-encode image inline (do not write to disk)
3. Load policy docs via `PolicyDocService` based on intent
4. Call OpenAI SDK synchronously (non-streaming)
5. Persist `Session` + initial `ChatMessage` (ASSISTANT role) to SQLite
6. Return `{sessionId, message}` JSON

## Chat Continuation (Streaming)

`POST /api/sessions/{id}/messages` uses `ChatService`:
- **Request body**: `{ messages: [...], system: "...", ... }` sent by `AssistantChatTransport`. Backend extracts only the last user message text from `messages[]`.
- **Steps**:
1. Extract last user message content from request `messages[]`
2. Persist user `ChatMessage` to DB
3. Load full session history from DB
4. Assemble system prompt from policy docs (ignore `system` from request)
5. Call OpenAI SDK with streaming enabled
6. Write SSE events to `SseEmitter`: `start` → `text-start` → `text-delta` (per chunk) → `text-end`
7. Persist assembled assistant response on stream completion

## System Prompt Structure

The system prompt sent to the LLM must include these 6 sections in order:
1. Role definition (returns/complaints evaluator for Sinsay)
2. Decision categories: "Prawdopodobnie zaakceptowane" / "Prawdopodobnie odrzucone" / "Niejasne"
3. Mandatory disclaimer (assessment is non-binding, human makes final decision)
4. Scope boundary (answer only Sinsay policy questions; redirect off-topic)
5. Language instruction (always respond in Polish)
6. Policy document content (concatenated markdown, intent-specific)
7.**Security constraints** (prompt injection protection, jailbreak prevention)
8. **Image analysis instructions** (two-stage: analyze image, then evaluate)
9. Policy content (regulamin.md + intent-specific doc)

**Policy doc selection** per intent:
- `RETURN`: `regulamin.md` + `zwrot-30-dni.md`
- `COMPLAINT`: `regulamin.md` + `reklamacje.md`

## Testing

**Integration Tests:** HTTP → Controller → DB (real). Mock the service layer (`AnalysisService`, `ChatService`) — do NOT mock `OpenAIClient` directly (the OpenAI SDK uses Kotlin final methods that Mockito cannot deep-stub, causing real SDK code to run and throw).
❌ WRONG: `@MockBean(answer = RETURNS_DEEP_STUBS) OpenAIClient`
✅ CORRECT: `@MockBean AnalysisService`, `@MockBean ChatService`

## Verification (MANDATORY)
1. `./mvnw test` — pass
2. `./mvnw clean package` — build succeeds
3. `./mvnw spring-boot:run` — APP STARTS (verify this!)
4. Final manual curl test to verify API works with running real BE (not mocked)

If app won't start → fix before commit.

## Coding Conventions

**Java:** 4-space indent, Spring Boot conventions. Package: `com.sinsay`. Tests: `*Tests` suffix.
