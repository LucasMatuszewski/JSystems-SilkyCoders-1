# Backend Test Standards

> This file contains all implementation details for writing Java tests in this project.
> It is the single reference for test infrastructure, patterns, and commands.
> For the TDD process and when to run tests, see `src/CLAUDE.md`.
> For test quality philosophy and methodology, see `.claude/agents/java-test-writer.md`.

---

## Tech Stack

| Tool | Purpose |
|------|---------|
| JUnit 5 (Jupiter) | Test engine |
| Mockito (`@ExtendWith(MockitoExtension.class)`) | Mocking framework; use BDDMockito style (`given/then`) |
| AssertJ | Fluent assertions — prefer over Hamcrest |
| `reactor-test` / `StepVerifier` | Testing `Flux`/`Mono` streams reactively |
| Spring Boot Test (`@SpringBootTest`, `@WebFluxTest`) | Integration and slice tests |
| `WebTestClient` | HTTP client for testing SSE streaming endpoints |

All dependencies are provided by `spring-boot-starter-test` + `reactor-test` (already in `pom.xml`). Do not add new test dependencies without discussion.

---

## Commands

```bash
./mvnw test                          # run all tests
./mvnw test -Dtest=ClassName         # run a single test class
./mvnw test -Dtest=ClassName#method  # run a single test method
./mvnw clean compile                 # compile only, check for warnings
./mvnw clean verify                  # full build + all tests
```

Run tests after changing `.java` or `pom.xml` files. Do NOT run after changing only `.md` or frontend files.

---

## Naming and Location Conventions

- Test classes live in `src/test/java/` mirroring the production package structure
- Naming: `*Tests` suffix — e.g., `AGUIAgentExecutorVerdictFlowTests`
- Unit tests (no Spring context): `@ExtendWith(MockitoExtension.class)`
- WebFlux slice tests (controller only): `@WebFluxTest(ControllerClass.class)` with `WebTestClient`
- Full integration tests: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@AutoConfigureWebTestClient`

---

## Choosing the Right Test Type

| What you're testing | Test type | Mock boundary |
|---------------------|-----------|---------------|
| Business logic in a service/executor | Unit (`@ExtendWith(MockitoExtension.class)`) | Mock only external dependencies (repositories, other services) |
| AGUISSEController routing and error handling | Integration (`@SpringBootTest` + `WebTestClient`) | Mock `ChatModel` HTTP calls — NOT `AGUIAgent` |
| Repository queries | Integration (`@SpringBootTest`) | Real H2 in-memory database |
| Full SSE event sequence (the real graph) | Integration (`@SpringBootTest` + `WebTestClient`) | Mock `ChatModel` HTTP calls only |

**Critical rule**: Never mock `AGUIAgent` when writing tests for `AGUISSEController`. The whole point is to let the LangGraph4j graph run so the test exercises real routing, event production, and tool call logic. Mocking the agent tests only the error handling wiring in the controller.

---

## Integration Test Configuration

Use `@TestPropertySource` to override properties for test isolation:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1",
    "spring.r2dbc.username=sa",
    "spring.r2dbc.password=",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:schema.sql",
    "ag-ui.agent=agentExecutor",
    "ag-ui.model=OLLAMA_QWEN2_5_7B"
})
```

Each integration test class should use a unique in-memory database name (e.g., `testdb-sse`, `testdb-verdict`) to avoid state sharing between parallel test runs.

---

## Testing SSE Streams with WebTestClient

Collect the full event sequence and assert on it:

```java
var events = webTestClient.post()
    .uri("/langgraph4j/copilotkit")
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(runAgentInputJson)
    .accept(MediaType.TEXT_EVENT_STREAM)
    .exchange()
    .expectStatus().isOk()
    .returnResult(AGUIEvent.class)
    .getResponseBody()
    .collectList()
    .block();

// Assert the full sequence — not just "contains some event"
assertThat(events).extracting(AGUIEvent::type).containsExactly(
    AGUIEvent.EventType.RUN_STARTED,
    AGUIEvent.EventType.TEXT_MESSAGE_START,
    AGUIEvent.EventType.TEXT_MESSAGE_CONTENT,
    AGUIEvent.EventType.TEXT_MESSAGE_END,
    AGUIEvent.EventType.RUN_FINISHED
);

// Assert no errors in success paths
assertThat(events).noneMatch(e -> e.type() == AGUIEvent.EventType.RUN_ERROR);
```

For error path tests, assert the error event IS present and check its message content:

```java
assertThat(events).anyMatch(e ->
    e instanceof AGUIEvent.RunErrorEvent &&
    ((AGUIEvent.RunErrorEvent) e).message().contains("Ollama")
);
```

---

## Mocking the ChatModel (Not the Agent)

Mock at the HTTP boundary: mock the `ChatModel` bean so it returns a canned response without hitting Ollama or OpenAI. The full LangGraph4j graph, tool call detection, and AG-UI event production all run for real.

```java
@MockitoBean
private ChatModel chatModel;

@BeforeEach
void configureMockModel() {
    // Use response captured from real Ollama/OpenAI calls (see logs)
    // Do NOT invent response structures — use real payloads
    given(chatModel.call(any(Prompt.class))).willReturn(/* captured response */);
}
```

**Important**: Mock responses must be based on real API response payloads captured from actual model calls in the logs. Do not invent response structures — invented structures hide deserialization incompatibilities.

---

## Testing Reactive Flows with StepVerifier

For testing `Flux`/`Mono` in isolation without HTTP:

```java
StepVerifier.create(agent.run(input))
    .expectNextMatches(e -> e instanceof AGUIEvent.RunStartedEvent)
    .expectNextMatches(e -> e instanceof AGUIEvent.TextMessageStartEvent)
    .expectNextMatches(e -> e instanceof AGUIEvent.RunFinishedEvent)
    .verifyComplete();
```

Never use `block()` in unit tests. Use `StepVerifier` for direct reactive stream testing.

---

## Async Assertions

Never use `Thread.sleep()`. For polling async state, use Awaitility (can be added to `pom.xml` if needed):

```java
await().atMost(5, SECONDS).until(() -> someCondition);
```

For `StepVerifier`, configure a step timeout:

```java
StepVerifier.create(flux)
    .expectNext(event)
    .verifyComplete();
    // Default timeout is sufficient for mocked responses
```

---

## Unit Test Pattern (BDD Style)

```java
@ExtendWith(MockitoExtension.class)
class AGUIAgentExecutorVerdictFlowTests {

    @Mock
    private PolicyService policyService;

    private AGUIAgentExecutor sut;

    @BeforeEach
    void setUp() {
        sut = new AGUIAgentExecutor(null, policyService, new SinsayTools());
    }

    @Test
    void shouldDetectReturnIntentAndLoadReturnPolicy() {
        // given
        given(policyService.getPoliciesForIntent("return")).willReturn("RETURN_POLICY");
        var input = buildInput("Chcę zwrócić produkt", List.of());

        // when
        var graphInput = sut.buildGraphInput(input);

        // then
        var messages = (List<Message>) graphInput.get("messages");
        assertThat(((SystemMessage) messages.get(0)).getText()).contains("RETURN_POLICY");
    }
}
```

---

## Test Isolation for R2DBC / H2

R2DBC tests run against an in-memory H2 database. Each test class uses its own named in-memory instance (e.g., `testdb-sessions`, `testdb-sse`) to avoid cross-test contamination. The schema is applied via `classpath:schema.sql` on startup.

There is no automatic transaction rollback with R2DBC (unlike JPA). If a test inserts rows, use `@AfterEach` to delete them explicitly, or rely on each test using a unique database name.

---

## Project-Specific Anti-Patterns to Avoid

**Never do this in `AGUISSEControllerTests`:**
```java
// WRONG — mocks the agent, tests nothing real
@MockitoBean(name = "AGUIAgent")
private AGUIAgent uiAgent;  // This makes the entire LangGraph4j graph unreachable

given(uiAgent.run(any())).willReturn(Flux.just(event)); // Fake events bypass all real logic
```

**Do this instead:**
```java
// RIGHT — mock only the ChatModel; let the graph run for real
@MockitoBean
private ChatModel chatModel;

given(chatModel.call(any())).willReturn(capturedRealResponse);
// Now the full graph runs: tool detection, approval, event production
```

---

## Capturing Real Response Data for Mocks

To get real mock data:
1. Enable `logging.level.org.springframework.ai=DEBUG` in `application.properties`
2. Run the app and submit a real request (e.g., a chat message or a form with photo)
3. Copy the logged AI response payload from `logs/app.log` — use `tail -200 logs/app.log` or `grep`, never load the full file
4. Use that payload to construct your mock return value

This ensures mocks reflect the real API contract, not invented structures.

---

## Test Quality Rules — Mandatory Before Every Commit

### The fundamental test question
**Before committing any test, ask: "If I deleted the production code I just wrote, would this test fail?"**

If the answer is no — the test passes even without the implementation — delete or rewrite it. A passing test that doesn't catch a broken implementation is worse than no test: it creates false confidence.

### Test anti-patterns that must never appear in this project

**Anti-pattern 1: The wiring test disguised as an integration test**
```java
// WRONG — this is NOT an integration test. It's a unit test of event serialization.
// ChatModel always succeeds. No real AI behavior is tested.
@TestConfiguration
static class MockConfig {
    @Bean("AGUIAgent")
    AGUIAgent agent(ChatModel mock) {
        return new AGUIAgentExecutor(...) {
            @Override ChatModel resolveModel() { return mock; }  // always returns success
        };
    }
}
// This test passes even if Ollama is broken, the fallback is missing, or the model format changed.
```

**What to do instead**: Test the wiring separately from the behavior. Have one test that verifies events are emitted correctly (with mock). Have a SEPARATE test that verifies the error path (mock throws broken pipe → correct RunError event emitted → correct Polish message).

**Anti-pattern 2: Only testing the success path for AI model calls**
```java
// WRONG — only tests the happy path. Broken pipe, connection refused, timeout are the real failures.
given(chatModel.call(any())).willReturn(successResponse);
// Test passes. But when Ollama is sleeping, none of this is tested.
```

**Required**: Always add at least one test where the mock throws a transient error. Verify that the error is handled correctly (either fallback kicks in, or RUN_ERROR event is emitted with the correct Polish message).

**Anti-pattern 3: Modifying the assertion to match wrong behavior**
```java
// WRONG — changed assertion to make test "pass" without fixing the actual problem
// assertThat(message).contains("Ollama");      // original
assertThat(message).isNotNull();                 // weakened to make it pass
```

If a test is failing and you find yourself weakening the assertion instead of fixing the code, STOP. The test is correct. The production code is wrong. Fix the code.

**Anti-pattern 4: The "error is mapped correctly" test without end-to-end verification**

Testing `mapErrorToUserMessage()` in isolation proves the method works. It does NOT prove the error is ever actually caught in the real flow. After writing the unit test, also verify: given a real Ollama failure, does the user actually see the Polish error message? If you can't run the full stack in a unit test, write an E2E Playwright test that proves it.

### Test coverage requirements for AI model integration

For any feature that calls an AI model (Ollama, OpenAI), you must have:

| Scenario | Required test type |
|---|---|
| Success: model returns expected response | Integration test with mock ChatModel |
| Transient failure: broken pipe / connection refused | Unit test of FallbackChatModel; integration test verifying RUN_ERROR event |
| Non-transient failure: 401 Unauthorized | Unit test verifying error propagates without retry |
| Recovery: primary fails, fallback succeeds | Unit test of FallbackChatModel |
| End-to-end: full form submission → verdict visible | Playwright E2E test (pnpm e2e) |

If any row in this table is missing, the feature is not properly tested.
