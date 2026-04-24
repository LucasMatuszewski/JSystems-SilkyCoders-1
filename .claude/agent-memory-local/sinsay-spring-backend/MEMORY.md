# Sinsay Backend Agent Memory

## Project Structure
- Root: `/home/lucas/DEV/Projects/JSystems/SilkyCodders1/JSystems-SilkyCodders-1`
- Branch: `Lucas-sinsay-2nd-February-CopilotKit`
- Base package: `com.silkycoders1.jsystemssilkycodders1`

## R2DBC + Spring Data Reactive Key Patterns
- Spring Data R2DBC treats entities with pre-set `@Id` as UPDATE (not INSERT). Do NOT set UUID IDs manually when creating new entities — let the DB generate them via `DEFAULT RANDOM_UUID()` in schema.sql.
- `@DataR2dbcTest` is the test slice for reactive repositories (not `@DataJpaTest`).
- Tests need `TestPropertySource` with `r2dbc:h2:mem:///` URL for in-memory H2.
- `@SpringBootTest` tests also need R2DBC test properties now that R2DBC is on the classpath.
- `flatMapMany` (not `flatMap`) when returning `Flux` from a `Mono` chain.

## Test Counts (current)
- 98 total tests (after form resume integration tests added)

## Logging Patterns
- `truncateBase64(String value)` is a `static` package-private method on `AGUIAgentExecutor`
  — apply it to ANY debug log that might contain user-supplied text or photo data
- Logback config: `src/main/resources/logback-spring.xml` (console + rolling file, daily/7-day/50MB)
- To capture DEBUG logs in a unit test: cast to `ch.qos.logback.classic.Logger`, set level to DEBUG,
  attach a `ListAppender<ILoggingEvent>`, restore level in `@AfterEach` via `setLevel(null)`
- TDD trap: if the log statement is at DEBUG but the test ListAppender is attached without setting
  level to DEBUG, the test passes trivially — always set `logger.setLevel(Level.DEBUG)` in setUp

## Domain Entities (R2DBC, no JPA)
- `Session` (sessions table) - id, createdAt, updatedAt
- `FormSubmission` (form_submissions table) - id, sessionId, intent, productName, description, photoPath, verdict, submittedAt, verdictAt
- `ChatMessage` (chat_messages table) - id, sessionId, role, content, createdAt
- All use `@Data @Builder` from Lombok + `@Table` from Spring Data Relational

## Services
- `SessionService` - manages session lifecycle, chat messages, form submissions, verdict updates
- `PhotoStorageService` - saves base64-encoded photos to filesystem, returns relative path
- `PolicyService` - loads Sinsay policy documents from `docs/sinsay-documents/`

## Phase 4A: Verdict Flow with Multimodal Photo Analysis
- `AGUIAgentExecutor.buildGraphInput()` extracts form submission from `ResultMessage` entries
- `FormSubmissionData` record holds parsed form JSON (productName, type, description, photo, photoMimeType)
- `extractFormSubmission()` parses `ResultMessage.result()` JSON looking for `productName` field
- Multimodal image injected as `UserMessage.builder().text(...).media(media).build()`
- Spring AI 1.0.0: `UserMessage` requires builder pattern for media; no `(String, List<Media>)` constructor
- Spring AI 1.0.0: `Media` class is `org.springframework.ai.content.Media` (not `org.springframework.ai.model.Media`)
- Graceful degradation: invalid base64 logs warning, continues without multimodal message
- Intent from form `type` field overrides keyword detection
- `AGUILangGraphAgent.run()` is `final` — cannot override the interruption resume path

## Phase 4B: Image Resize + Ollama Timeout + Broken Pipe Handling
- `resizeImageIfNeeded(byte[] imageBytes, String mimeType)` is `static` package-private on `AGUIAgentExecutor`
  — called between `Base64.getDecoder().decode(data.photo())` and `new Media(...)` in `buildGraphInput()`
- Resize threshold: max 800px on longest side; preserves aspect ratio; uses `Image.SCALE_SMOOTH`
- Spring AI 1.0.0 `Media.getData()` returns `Object` — always use `media.getDataAsByteArray()` in tests (NOT `getData().getInputStream()`)
- `OllamaApi.Builder` in Spring AI 1.0.0 has NO `requestTimeout()` method — use `.webClientBuilder(WebClient.builder().clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(30)))))` instead
- `ReactorClientHttpConnector` is in `spring-web`; `HttpClient` is from `reactor.netty.http.client`
- `mapErrorToUserMessage()` on `AGUISSEController` is package-private (not private) so tests in `org.bsc.langgraph4j.agui` can call it
- "broken pipe" must be checked in BOTH `msg` and `causeMsg` — maps to same Ollama unavailable message as ConnectionRefused

## LangGraph4j Approval/Resume Flow (CRITICAL)
- `ApprovalNodeAction.interrupt()` checks `state.value("approval_result")`:
  - Empty -> triggers interruption (returns InterruptionMetadata)
  - Present -> does NOT interrupt (returns Optional.empty())
- `approvalAction()` reads `approval_result` and compares to `AgentEx.ApprovalState.APPROVED.name()`:
  - If "APPROVED" -> routes to tool execution node
  - If anything else -> treats as REJECTED, uses the value as Command routing ID
- **BUG FIX**: `AGUILangGraphAgent.run()` must pass `AgentEx.ApprovalState.APPROVED.name()` ("APPROVED")
  as the `approval_result` value when resuming from an interruption — NOT the raw ResultMessage content
  (which is the form JSON with base64 photo). The form data is already extracted by
  `AGUIAgentExecutor.buildGraphInput()` via `extractFormSubmission()`.
- `CompiledGraph.stream(Map, RunnableConfig)` correctly handles null map -> creates `GraphResume`
- To trigger tool call in streaming mode: mock `ChatModel.stream()` must return a Flux with
  `ChatResponse` containing `AssistantMessage` with `hasToolCalls()=true`. The `StreamingChatGenerator`
  extracts the last ChatResponse's AssistantMessage for state.
- Integration test pattern for 2-phase form flow: `AGUIFormResumeTests.java`

## Configuration
- R2DBC URL: `r2dbc:h2:file:///./data/sinsay-poc` (file-based, persists restarts)
- Schema: `src/main/resources/schema.sql` (auto-initialized)
- Photo dir: `sinsay.photo.dir` property (default: `data/photos`)
