# BE Developer Memory

## Project: Sinsay AI PoC Backend

- Spring Boot 3.5.9, Java 21, Maven
- Package root: `com.sinsay`
- OpenAI Java SDK 4.20.0 via OpenRouter

## Key Patterns

### Model Injection
`OpenAIConfig` exposes a `@Bean String openaiModel()`. Services inject it via constructor with `@Qualifier("openaiModel")`. Do NOT use `@Value` in services — the bean is the source of truth.

### API Key Fallback in OpenAIConfig
`OpenAIConfig` supports fallback from `OPENAI_API_KEY` to `OPENROUTER_API_KEY`. If `OPENAI_API_KEY` is blank or unset, falls back to `OPENROUTER_API_KEY`. If neither is set, throws `IllegalStateException` with clear error message directing users to OpenRouter. Spring properties: `openai.api-key` (maps to `OPENAI_API_KEY`), `openrouter.api-key` (maps to `OPENROUTER_API_KEY`).

### Multimodal Content Parts (OpenAI Java SDK 4.x)
Use `builder.addUserMessageOfArrayOfContentParts(List<ChatCompletionContentPart>)`.
- Image: `ChatCompletionContentPart.ofImageUrl(ChatCompletionContentPartImage.builder().imageUrl(ImageUrl.builder().url(dataUri).build()).build())`
- Text: `ChatCompletionContentPart.ofText(ChatCompletionContentPartText.builder().text(desc).build())`

### SSE Streaming (Vercel AI SDK v6 format)
Use `SseEmitter`. Header: `x-vercel-ai-ui-message-stream: v1`. Events: `start` → `text-start` → `text-delta` (per chunk) → `text-end`.

### Test Constructor Pattern
Unit tests instantiate services directly in `@BeforeEach`. When constructor signature changes (e.g. adding model param), update all test instantiation calls immediately.

### JPA Entity ID Pre-setting Bug
NEVER call `.id(UUID.randomUUID())` on a Lombok builder for a `@GeneratedValue` entity before saving. Spring Data JPA's `save()` calls `merge()` (not `persist()`) when `id != null`, causing `ObjectOptimisticLockingFailureException` in async threads (e.g., ChatService executor). Let JPA assign the ID.

### Integration Tests: Mock Services Not OpenAIClient
Do NOT use `@MockBean(answer = RETURNS_DEEP_STUBS) OpenAIClient` in `@SpringBootTest` integration tests — the OpenAI SDK has Kotlin final methods that Mockito cannot deep-stub, causing real SDK code to run and throw `RuntimeException("API error")`. Instead, mock `AnalysisService` and `ChatService` directly (the Spring `@Service` layer), matching the pattern in `SessionControllerTests` and `ChatControllerTests`.

### Integration Tests: Cross-class Context Sharing
`@DirtiesContext(BEFORE_CLASS)` + `@TestInstance(PER_CLASS)` pattern for ordered multi-step integration tests that share state across `@Test` methods. Use an instance field (not `static`) for shared state — works because PER_CLASS creates one JUnit instance per class. Add `@DirtiesContext(BEFORE_CLASS)` to ensure a fresh Spring context when the test class starts (prevents inheriting stale `@MockBean` stubs from other test classes that dirtied their context).

### SSE Async Body Assertion with MockMvc
For `SseEmitter` streams in MockMvc:
1. Perform the request normally — `andReturn()` to get `MvcResult`
2. Call `mockMvc.perform(asyncDispatch(asyncResult))` to wait for async completion
3. Read body from `asyncResult.getResponse().getContentAsString()` — NOT from the asyncDispatch result

## Key Files
- `backend/src/main/java/com/sinsay/config/OpenAIConfig.java` — model bean, client bean
- `backend/src/main/java/com/sinsay/service/ChatService.java` — streaming chat
- `backend/src/main/java/com/sinsay/service/AnalysisService.java` — initial analysis, multimodal
- `backend/src/main/java/com/sinsay/controller/ChatController.java` — POST /api/sessions/{id}/messages
- `backend/src/test/java/com/sinsay/FullFlowIntegrationTests.java` — full lifecycle integration test
