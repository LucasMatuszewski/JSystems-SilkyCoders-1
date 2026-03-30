# Backend Test Guidelines

## Framework

JUnit 5 + Mockito for unit tests. MockMvc for integration/controller tests.

## Database

Use **H2 in-memory** (not SQLite) in the `test` Spring profile. Configure a `application-test.properties` with H2 datasource using SQLite-compatible schema (same column types).

## Never Call Real APIs

Always mock `OpenAiClient` / any HTTP client in tests. Do not call real OpenAI or OpenRouter endpoints.

## Key Scenarios to Cover

- `PolicyDocService`: loads correct docs per intent (RETURN vs COMPLAINT); assembles system prompt with all 6 sections
- `AnalysisService`: base64 image encoding; multipart validation (missing fields, wrong MIME type, oversized file)
- `ChatService`: SSE stream format encoding — produces `start`, `text-start`, `text-delta`, `text-end` events with correct JSON; all events share same message UUID
- `SessionController`: returns 400 on invalid multipart; returns `{sessionId, message}` on success
- `ChatController`: streams SSE events (UI Message Stream format); includes `x-vercel-ai-ui-message-stream: v1` header; accepts `{ messages }` request body; persists assistant message after stream completes
- Session persistence: Session + ChatMessage saved to DB; `GET /api/sessions/{id}` returns full history

## Test Class Naming

`*Tests` suffix, mirrored package: `com.sinsay.*` → `src/test/java/com/sinsay/*Tests.java`
