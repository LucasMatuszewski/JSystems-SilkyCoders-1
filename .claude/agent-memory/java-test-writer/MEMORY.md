# Java Test Writer Memory

## Project: JSystems-SilkyCodders-1 (Sinsay PoC)

### Critical Pattern: Mocking AGUIAgentExecutor's ChatModel

The `AGUIAgentExecutor` creates its ChatModel inside `resolveModel()` — NOT via Spring DI.
Mocking `@MockitoBean ChatModel` alone is NOT enough; the production bean builds its own
Ollama/OpenAI client and never touches the Spring-managed ChatModel bean.

**Correct approach for integration tests:**
1. Set `ag-ui.agent=mock-integration-test` in `@TestPropertySource` — this prevents BOTH
   production `@ConditionalOnProperty` beans (`agentExecutor` and `sample`) from registering
2. Provide the `AGUIAgent` bean in a `@TestConfiguration` inner class with
   `@ConditionalOnProperty(name = "ag-ui.agent", havingValue = "mock-integration-test")`
   on the `@Bean` method — this prevents the test bean from interfering with other test contexts
3. The test bean creates an anonymous subclass of `AGUIAgentExecutor` that overrides
   `resolveModel()` to return the `@MockitoBean ChatModel`

**Why @ConditionalOnProperty on the test bean is required:**
`@TestConfiguration` inner classes in the `org.bsc.langgraph4j.agui` package ARE component-scanned
by `@SpringBootApplication` (which scans that package). Without the conditional, the bean
registers in ALL test contexts that load the app, causing `BeanDefinitionOverrideException`.

**Template:**
```java
@TestConfiguration
static class MockChatModelConfig {
    @Bean("AGUIAgent")
    @ConditionalOnProperty(name = "ag-ui.agent", havingValue = "mock-integration-test")
    AGUIAgent agentWithMockModel(ChatModel mockChatModel, PolicyService ps, SinsayTools t) {
        return new AGUIAgentExecutor(null, ps, t) {
            @Override ChatModel resolveModel() { return mockChatModel; }
        };
    }
}
```

### Real SSE Event Sequence (captured 2026-02-26, kimi-k2.5 via Ollama)

For a simple "Cześć" chat message, the real sequence is:
```
RUN_STARTED
TEXT_MESSAGE_START (empty — from START node path)
TEXT_MESSAGE_END
TEXT_MESSAGE_START (streaming begins)
TEXT_MESSAGE_CONTENT x N (one per token)
TEXT_MESSAGE_END
TEXT_MESSAGE_START (empty — from END node)
TEXT_MESSAGE_END
RUN_FINISHED
```

Note: there are always 3 TEXT_MESSAGE_START/END pairs for a plain text response (no tool calls).

### ChatModel Mock Response Structure

Spring AI `ChatModel.stream(Prompt)` returns `Flux<ChatResponse>`.
Each `ChatResponse` contains one `Generation(new AssistantMessage(textChunk))`.

```java
private static Flux<ChatResponse> streamingResponseOf(String... chunks) {
    return Flux.fromArray(chunks)
            .map(chunk -> new ChatResponse(List.of(new Generation(new AssistantMessage(chunk)))));
}
```

Mock BOTH `stream(Prompt)` (used by graph with streaming=true) AND `call(Prompt)` (used for
tool detection, fallback). If only one is mocked, Mockito returns null for the other and the
graph may NPE.

### Test Infrastructure Notes

- Test package `org.bsc.langgraph4j.agui` is component-scanned — inner `@TestConfiguration`
  classes there ARE visible to all `@SpringBootTest` contexts in the project
- Use unique `testdb-*` database names per test class to avoid R2DBC cross-context contamination
- `@AutoConfigureWebTestClient(timeout = "PT30S")` — needed for the graph execution time
- `@SpringBootTest(classes = JSystemsSilkyCodders1Application.class)` — required when test is in
  `org.bsc.langgraph4j.agui` package (not under `com.silkycoders1...` where app lives)

### Existing Test Coverage (do not duplicate)

- `AGUISSEControllerTests` — tests error handler in controller (mocks AGUIAgent — intentional,
  these are controller slice tests for error mapping only)
- `ImageProcessingTests` — image resize, broken pipe error mapping, model builder validation
- `AGUIAgentExecutorVerdictFlowTests` — buildGraphInput(), intent detection, form submission
- `AGUIAgentExecutorModelResolutionTests` — resolveModel() fallback chain
- `AGUIAgentExecutorSystemPromptTests` — system prompt generation with policy injection
- `AGUISSEIntegrationTests` — full graph integration with mocked ChatModel (NEW 2026-02-26)
