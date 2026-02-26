package org.bsc.langgraph4j.agui;

import com.silkycoders1.jsystemssilkycodders1.service.PolicyService;
import com.silkycoders1.jsystemssilkycodders1.tools.SinsayTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * Integration tests for the SSE endpoint /langgraph4j/copilotkit that exercise the
 * REAL LangGraph4j graph and event production pipeline.
 *
 * Strategy: mock ChatModel (the external AI boundary), NOT AGUIAgent.
 * This lets the full graph run — tool call detection, event sequencing,
 * interruption handling, and AG-UI event production — while keeping tests
 * deterministic and offline.
 *
 * Real SSE event sequence observed from a live Ollama run (captured 2026-02-26):
 *   RUN_STARTED
 *   TEXT_MESSAGE_START (empty from START node path)
 *   TEXT_MESSAGE_END
 *   TEXT_MESSAGE_START (streaming begins)
 *   TEXT_MESSAGE_CONTENT x N (streamed chunks)
 *   TEXT_MESSAGE_END
 *   TEXT_MESSAGE_START (empty from END node)
 *   TEXT_MESSAGE_END
 *   RUN_FINISHED
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = com.silkycoders1.jsystemssilkycodders1.JSystemsSilkyCodders1Application.class
)
@AutoConfigureWebTestClient(timeout = "PT30S")
@TestPropertySource(properties = {
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb-integration;DB_CLOSE_DELAY=-1",
    "spring.r2dbc.username=sa",
    "spring.r2dbc.password=",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:schema.sql",
    // Use a non-matching agent value so neither @ConditionalOnProperty production bean registers.
    // The test's MockChatModelConfig provides the AGUIAgent bean directly.
    "ag-ui.agent=mock-integration-test",
    "ag-ui.model=OLLAMA_QWEN2_5_7B"
})
class AGUISSEIntegrationTests {

    /**
     * Test-specific Spring configuration that provides the AGUIAgent bean using a
     * subclass of AGUIAgentExecutor whose resolveModel() returns the mocked ChatModel.
     *
     * This is the correct mocking boundary: we intercept at the point where the
     * executor would normally build a real Ollama/OpenAI model, and replace it with
     * the mock. The entire LangGraph4j graph, tool detection, and AG-UI event production
     * pipeline still run for real.
     */
    @TestConfiguration
    static class MockChatModelConfig {

        @Bean("AGUIAgent")
        @ConditionalOnProperty(name = "ag-ui.agent", havingValue = "mock-integration-test")
        AGUIAgent agentWithMockModel(ChatModel mockChatModel,
                                    PolicyService policyService,
                                    SinsayTools sinsayTools) {
            return new AGUIAgentExecutor(null, policyService, sinsayTools) {
                @Override
                ChatModel resolveModel() {
                    return mockChatModel;
                }
            };
        }
    }

    /** The mocked ChatModel — intercepts all AI calls without hitting Ollama or OpenAI. */
    @MockitoBean
    private ChatModel mockChatModel;

    @Autowired
    private WebTestClient webTestClient;

    private static final String SIMPLE_CHAT_REQUEST = """
            {
              "threadId": "integration-thread-1",
              "runId":    "integration-run-1",
              "messages": [
                {"role": "user", "id": "msg-1", "content": "Cześć"}
              ]
            }
            """;

    /**
     * Builds a single-chunk streaming ChatResponse that the mock model returns.
     * Matches the structure that Spring AI streaming produces from Ollama:
     * multiple ChatResponse objects, each with one generation containing a text delta.
     */
    private static Flux<ChatResponse> streamingResponseOf(String... chunks) {
        return Flux.fromArray(chunks)
                .map(chunk -> new ChatResponse(
                        List.of(new Generation(new AssistantMessage(chunk)))
                ));
    }

    /**
     * Builds a non-streaming (batch) ChatResponse that the mock model returns
     * for the call(Prompt) method (used when tool call detection is needed).
     */
    private static ChatResponse batchResponseOf(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }

    @Nested
    @DisplayName("Happy path: simple chat message")
    class SimpleChatPath {

        @BeforeEach
        void configureMockForTextResponse() {
            // Streaming response captured from live Ollama run (kimi-k2.5 model):
            // The model streams the Polish greeting back in chunks.
            // stream() is called by ChatClient when streaming=true in the graph.
            given(mockChatModel.stream(any(Prompt.class)))
                    .willReturn(streamingResponseOf("Cześć", "! Jak", " mogę", " Ci ", "pomóc", "?"));

            // call() is used by the tool-detection phase (non-streaming)
            given(mockChatModel.call(any(Prompt.class)))
                    .willReturn(batchResponseOf("Cześć! Jak mogę Ci pomóc?"));
        }

        @Test
        @DisplayName("SSE stream starts with RUN_STARTED and ends with RUN_FINISHED for a simple chat message")
        void shouldEmitRunStartedAndRunFinishedBoundaryEvents() {
            // given: configured in @BeforeEach

            // when
            var events = webTestClient.post()
                    .uri("/langgraph4j/copilotkit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(SIMPLE_CHAT_REQUEST)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(AGUIEvent.class)
                    .getResponseBody()
                    .collectList()
                    .block();

            // then
            assertThat(events)
                    .as("Event stream must not be null or empty")
                    .isNotNull()
                    .isNotEmpty();

            assertThat(events.get(0).type())
                    .as("First event must be RUN_STARTED — the stream has not started until this arrives")
                    .isEqualTo(AGUIEvent.EventType.RUN_STARTED);

            assertThat(events.get(events.size() - 1).type())
                    .as("Last event must be RUN_FINISHED — the stream is not complete until this arrives")
                    .isEqualTo(AGUIEvent.EventType.RUN_FINISHED);
        }

        @Test
        @DisplayName("SSE stream contains at least one TEXT_MESSAGE_CONTENT event for a plain text response")
        void shouldEmitAtLeastOneTextMessageContentEventForSimpleChat() {
            // given: configured in @BeforeEach

            // when
            var events = webTestClient.post()
                    .uri("/langgraph4j/copilotkit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(SIMPLE_CHAT_REQUEST)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(AGUIEvent.class)
                    .getResponseBody()
                    .collectList()
                    .block();

            // then
            assertThat(events)
                    .as("Stream must contain at least one TEXT_MESSAGE_CONTENT event with the model's reply")
                    .anyMatch(e -> e.type() == AGUIEvent.EventType.TEXT_MESSAGE_CONTENT);
        }

        @Test
        @DisplayName("SSE stream contains no RUN_ERROR events when the model responds successfully")
        void shouldEmitNoRunErrorEventsOnSuccessfulChat() {
            // given: configured in @BeforeEach

            // when
            var events = webTestClient.post()
                    .uri("/langgraph4j/copilotkit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(SIMPLE_CHAT_REQUEST)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(AGUIEvent.class)
                    .getResponseBody()
                    .collectList()
                    .block();

            // then
            assertThat(events)
                    .as("No RUN_ERROR must be present in a successful chat response — " +
                            "if this fails, the graph is swallowing errors and converting them to error events")
                    .noneMatch(e -> e.type() == AGUIEvent.EventType.RUN_ERROR);
        }

        @Test
        @DisplayName("TEXT_MESSAGE_CONTENT deltas together contain the model's response text")
        void shouldDeliverModelResponseTextAsConcatenatedDeltas() {
            // given: configured in @BeforeEach — mock returns ["Cześć", "! Jak", " mogę", " Ci ", "pomóc", "?"]

            // when
            var events = webTestClient.post()
                    .uri("/langgraph4j/copilotkit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(SIMPLE_CHAT_REQUEST)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(AGUIEvent.class)
                    .getResponseBody()
                    .collectList()
                    .block();

            // then: collect all deltas and reconstruct the full text
            String reconstructedText = events.stream()
                    .filter(e -> e instanceof AGUIEvent.TextMessageContentEvent)
                    .map(e -> ((AGUIEvent.TextMessageContentEvent) e).delta())
                    .reduce("", String::concat);

            assertThat(reconstructedText)
                    .as("Concatenated TEXT_MESSAGE_CONTENT deltas must reproduce the model's response")
                    .isNotBlank()
                    .contains("Cześć");
        }

        @Test
        @DisplayName("Every TEXT_MESSAGE_START event is paired with a matching TEXT_MESSAGE_END event")
        void shouldEmitMatchingStartAndEndEventsForEachMessageBlock() {
            // given: configured in @BeforeEach

            // when
            var events = webTestClient.post()
                    .uri("/langgraph4j/copilotkit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(SIMPLE_CHAT_REQUEST)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(AGUIEvent.class)
                    .getResponseBody()
                    .collectList()
                    .block();

            // then
            long startCount = events.stream()
                    .filter(e -> e.type() == AGUIEvent.EventType.TEXT_MESSAGE_START)
                    .count();
            long endCount = events.stream()
                    .filter(e -> e.type() == AGUIEvent.EventType.TEXT_MESSAGE_END)
                    .count();

            assertThat(startCount)
                    .as("Every TEXT_MESSAGE_START must be paired with a TEXT_MESSAGE_END — " +
                            "mismatched counts indicate the graph is leaving message blocks open")
                    .isEqualTo(endCount)
                    .isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Error path: ChatModel throws Broken Pipe during streaming")
    class BrokenPipePath {

        @Test
        @DisplayName("RUN_ERROR event is emitted with Ollama message when ChatModel.stream() throws Broken Pipe")
        void shouldEmitRunErrorWhenChatModelStreamThrowsBrokenPipe() {
            // given: ChatModel.stream() fails with Broken Pipe (Ollama drops the connection mid-stream)
            given(mockChatModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new RuntimeException("Broken pipe")));
            given(mockChatModel.call(any(Prompt.class)))
                    .willReturn(batchResponseOf("Fallback text"));

            // when
            var events = webTestClient.post()
                    .uri("/langgraph4j/copilotkit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "threadId": "integration-thread-broken",
                              "runId":    "integration-run-broken",
                              "messages": [
                                {"role": "user", "id": "msg-b", "content": "Cześć"}
                              ]
                            }
                            """)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(AGUIEvent.class)
                    .getResponseBody()
                    .collectList()
                    .block();

            // then: the controller's onErrorResume must catch the graph failure and emit a RUN_ERROR
            assertThat(events)
                    .as("Stream must contain a RUN_ERROR event when the model throws Broken Pipe")
                    .anyMatch(e -> e.type() == AGUIEvent.EventType.RUN_ERROR);

            // and: the error message must mention Ollama so the user knows what to check
            var errorEvent = events.stream()
                    .filter(e -> e instanceof AGUIEvent.RunErrorEvent)
                    .map(e -> (AGUIEvent.RunErrorEvent) e)
                    .findFirst();

            assertThat(errorEvent)
                    .as("A RunErrorEvent must be present in the stream")
                    .isPresent();

            assertThat(errorEvent.get().message())
                    .as("Error message must contain 'Ollama' to guide the user toward the root cause")
                    .contains("Ollama");
        }

        @Test
        @DisplayName("No TEXT_MESSAGE_CONTENT is emitted when the model fails immediately")
        void shouldEmitNoTextContentWhenModelFailsImmediately() {
            // given: ChatModel fails before producing any chunks
            given(mockChatModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new RuntimeException("Broken pipe")));
            given(mockChatModel.call(any(Prompt.class)))
                    .willReturn(batchResponseOf(""));

            // when
            var events = webTestClient.post()
                    .uri("/langgraph4j/copilotkit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "threadId": "integration-thread-no-content",
                              "runId":    "integration-run-no-content",
                              "messages": [
                                {"role": "user", "id": "msg-nc", "content": "Cześć"}
                              ]
                            }
                            """)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(AGUIEvent.class)
                    .getResponseBody()
                    .collectList()
                    .block();

            // then: the error path must not produce text content
            assertThat(events)
                    .as("TEXT_MESSAGE_CONTENT must not be emitted when the model fails before producing output")
                    .noneMatch(e -> e.type() == AGUIEvent.EventType.TEXT_MESSAGE_CONTENT);
        }
    }

    @Nested
    @DisplayName("Domain invariant: RUN_STARTED always precedes RUN_FINISHED")
    class EventOrderingInvariants {

        @Test
        @DisplayName("RUN_STARTED event always appears before RUN_FINISHED in the event sequence")
        void shouldAlwaysEmitRunStartedBeforeRunFinished() {
            // given
            given(mockChatModel.stream(any(Prompt.class)))
                    .willReturn(streamingResponseOf("Witaj!"));
            given(mockChatModel.call(any(Prompt.class)))
                    .willReturn(batchResponseOf("Witaj!"));

            // when
            var events = webTestClient.post()
                    .uri("/langgraph4j/copilotkit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "threadId": "integration-thread-order",
                              "runId":    "integration-run-order",
                              "messages": [
                                {"role": "user", "id": "msg-ord", "content": "Witaj"}
                              ]
                            }
                            """)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(AGUIEvent.class)
                    .getResponseBody()
                    .collectList()
                    .block();

            // then
            var types = events.stream().map(AGUIEvent::type).toList();
            int startedIndex = types.indexOf(AGUIEvent.EventType.RUN_STARTED);
            int finishedIndex = types.lastIndexOf(AGUIEvent.EventType.RUN_FINISHED);

            assertThat(startedIndex)
                    .as("RUN_STARTED must be present in the event stream")
                    .isGreaterThanOrEqualTo(0);

            assertThat(finishedIndex)
                    .as("RUN_FINISHED must be present in the event stream")
                    .isGreaterThanOrEqualTo(0);

            assertThat(startedIndex)
                    .as("RUN_STARTED (index %d) must appear before RUN_FINISHED (index %d)",
                            startedIndex, finishedIndex)
                    .isLessThan(finishedIndex);
        }

        @Test
        @DisplayName("Exactly one RUN_STARTED and one RUN_FINISHED event are emitted per request")
        void shouldEmitExactlyOneRunStartedAndOneRunFinished() {
            // given
            given(mockChatModel.stream(any(Prompt.class)))
                    .willReturn(streamingResponseOf("Cześć!"));
            given(mockChatModel.call(any(Prompt.class)))
                    .willReturn(batchResponseOf("Cześć!"));

            // when
            var events = webTestClient.post()
                    .uri("/langgraph4j/copilotkit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "threadId": "integration-thread-count",
                              "runId":    "integration-run-count",
                              "messages": [
                                {"role": "user", "id": "msg-cnt", "content": "Cześć"}
                              ]
                            }
                            """)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(AGUIEvent.class)
                    .getResponseBody()
                    .collectList()
                    .block();

            // then
            long runStartedCount = events.stream()
                    .filter(e -> e.type() == AGUIEvent.EventType.RUN_STARTED)
                    .count();
            long runFinishedCount = events.stream()
                    .filter(e -> e.type() == AGUIEvent.EventType.RUN_FINISHED)
                    .count();

            assertThat(runStartedCount)
                    .as("Exactly one RUN_STARTED event must be emitted per request")
                    .isEqualTo(1);

            assertThat(runFinishedCount)
                    .as("Exactly one RUN_FINISHED event must be emitted per request")
                    .isEqualTo(1);
        }
    }
}
