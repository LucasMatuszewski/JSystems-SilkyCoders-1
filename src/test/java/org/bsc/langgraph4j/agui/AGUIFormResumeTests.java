package org.bsc.langgraph4j.agui;

import com.silkycoders1.jsystemssilkycodders1.service.PolicyService;
import com.silkycoders1.jsystemssilkycodders1.tools.SinsayTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

/**
 * Integration test for the form submission -> graph resume -> verdict flow.
 *
 * This test exercises the critical two-phase flow:
 * 1. User sends "chce zwrocic" -> model detects return intent -> calls showReturnForm tool
 *    -> graph interrupts at approval_showReturnForm -> TOOL_CALL events emitted to client
 * 2. User fills form and submits -> CopilotKit sends ResultMessage with form JSON
 *    -> graph resumes from checkpoint -> model produces verdict -> TEXT_MESSAGE events emitted
 *
 * The bug: in step 2, the form JSON was being passed as the approval_result value,
 * causing the conditional edge to fail because it expects "APPROVED" or "REJECTED".
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = com.silkycoders1.jsystemssilkycodders1.JSystemsSilkyCodders1Application.class
)
@AutoConfigureWebTestClient(timeout = "PT30S")
@TestPropertySource(properties = {
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb-form-resume;DB_CLOSE_DELAY=-1",
    "spring.r2dbc.username=sa",
    "spring.r2dbc.password=",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:schema.sql",
    "ag-ui.agent=mock-form-resume-test",
    "ag-ui.model=OLLAMA_QWEN2_5_7B"
})
class AGUIFormResumeTests {

    @TestConfiguration
    static class MockChatModelConfig {

        @Bean("AGUIAgent")
        @ConditionalOnProperty(name = "ag-ui.agent", havingValue = "mock-form-resume-test")
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

    @MockitoBean
    private ChatModel mockChatModel;

    @Autowired
    private WebTestClient webTestClient;

    /**
     * Builds a ChatResponse with an AssistantMessage that requests a tool call
     * to showReturnForm. This mimics what the real model returns when it detects
     * return intent in the user's message.
     *
     * The tool call structure matches real Ollama/OpenAI response format:
     * AssistantMessage with hasToolCalls() = true and a ToolCall for "showReturnForm".
     */
    private static ChatResponse toolCallResponse() {
        var toolCall = new AssistantMessage.ToolCall(
                "call-123",
                "function",
                "showReturnForm",
                "{\"type\":\"return\"}"
        );
        var assistantMessage = new AssistantMessage(
                "",
                Map.of(),
                List.of(toolCall)
        );
        return new ChatResponse(List.of(new Generation(assistantMessage)));
    }

    /**
     * Builds a ChatResponse with a plain text verdict message.
     * This is what the model returns after the form is approved and the tool
     * is executed — the model sees the tool result and produces a verdict.
     */
    private static ChatResponse verdictResponse() {
        var assistantMessage = new AssistantMessage(
                "Zwrot mozliwy. Produkt spelnia warunki zwrotu.",
                Map.of("finishReason", "STOP")
        );
        return new ChatResponse(List.of(new Generation(assistantMessage)));
    }

    private static Flux<ChatResponse> streamingVerdictResponse() {
        return Flux.just(
                new ChatResponse(List.of(new Generation(new AssistantMessage("Zwrot ")))),
                new ChatResponse(List.of(new Generation(new AssistantMessage("mozliwy. ")))),
                new ChatResponse(List.of(new Generation(new AssistantMessage("Produkt spelnia warunki."))))
        );
    }

    /**
     * Returns the tool call response as a streaming Flux. The StreamingChatGenerator
     * in LangGraph4j extracts the last ChatResponse's AssistantMessage, so the final
     * chunk must contain the tool call to trigger the shouldContinue -> action_dispatcher path.
     */
    private static Flux<ChatResponse> streamingToolCallResponse() {
        return Flux.just(toolCallResponse());
    }

    @Test
    @DisplayName("Phase 1: return intent triggers tool call interruption with TOOL_CALL events")
    void shouldEmitToolCallEventsWhenModelDetectsReturnIntent() {
        // given: model returns a tool call for showReturnForm (both call and stream
        // must be mocked because the graph may use either depending on streaming config)
        given(mockChatModel.call(any(Prompt.class))).willReturn(toolCallResponse());
        given(mockChatModel.stream(any(Prompt.class))).willReturn(streamingToolCallResponse());

        // when: user sends return intent
        var events = webTestClient.post()
                .uri("/langgraph4j/copilotkit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "threadId": "form-resume-thread-1",
                          "runId":    "form-resume-run-1",
                          "messages": [
                            {"role": "user", "id": "msg-1", "content": "Chce zwrocic produkt"}
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

        // then: events must include TOOL_CALL_START for showReturnForm
        assertThat(events)
                .as("Events must contain TOOL_CALL_START for showReturnForm — " +
                        "this indicates the graph correctly detected the tool call and interrupted")
                .anyMatch(e -> e.type() == AGUIEvent.EventType.TOOL_CALL_START);

        assertThat(events)
                .as("Events must contain TOOL_CALL_END — closing the tool call block")
                .anyMatch(e -> e.type() == AGUIEvent.EventType.TOOL_CALL_END);

        // and: no RUN_ERROR — the interruption is expected behavior, not an error
        assertThat(events)
                .as("No RUN_ERROR should be emitted for a normal tool call interruption")
                .noneMatch(e -> e.type() == AGUIEvent.EventType.RUN_ERROR);
    }

    @Test
    @DisplayName("Phase 2: form submission resumes graph and produces verdict (no routing error)")
    void shouldResumeGraphAndProduceVerdictAfterFormSubmission() {
        // given: first call triggers tool call interruption
        given(mockChatModel.call(any(Prompt.class))).willReturn(toolCallResponse());
        given(mockChatModel.stream(any(Prompt.class))).willReturn(streamingToolCallResponse());

        // Phase 1: trigger interruption
        var phase1Events = webTestClient.post()
                .uri("/langgraph4j/copilotkit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "threadId": "form-resume-thread-2",
                          "runId":    "form-resume-run-2a",
                          "messages": [
                            {"role": "user", "id": "msg-1", "content": "Chce zwrocic produkt"}
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

        // Verify phase 1 produced tool call events (precondition for phase 2)
        assertThat(phase1Events)
                .as("Phase 1 must produce TOOL_CALL_START — prerequisite for resume test")
                .anyMatch(e -> e.type() == AGUIEvent.EventType.TOOL_CALL_START);

        // given: after form submission, model should return a verdict text
        given(mockChatModel.call(any(Prompt.class))).willReturn(verdictResponse());
        given(mockChatModel.stream(any(Prompt.class))).willReturn(streamingVerdictResponse());

        // when: Phase 2 — submit form data with same threadId to resume graph
        var phase2Events = webTestClient.post()
                .uri("/langgraph4j/copilotkit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "threadId": "form-resume-thread-2",
                          "runId":    "form-resume-run-2b",
                          "messages": [
                            {"role": "user", "id": "msg-1", "content": "Chce zwrocic produkt"},
                            {"role": "tool", "id": "result-1", "toolCallId": "call-123", "name": "showReturnForm", "content": "{\\"productName\\":\\"Sukienka\\",\\"type\\":\\"return\\",\\"description\\":\\"Za duza\\"}"}
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

        // then: the graph must resume without error
        assertThat(phase2Events)
                .as("Phase 2 must NOT produce RUN_ERROR — the graph should resume from checkpoint " +
                        "with approval_result=APPROVED, not fail with edge mapping error")
                .noneMatch(e -> e.type() == AGUIEvent.EventType.RUN_ERROR);

        // and: the stream must start and finish normally
        assertThat(phase2Events)
                .as("Phase 2 must produce RUN_STARTED event")
                .anyMatch(e -> e.type() == AGUIEvent.EventType.RUN_STARTED);

        assertThat(phase2Events)
                .as("Phase 2 must produce RUN_FINISHED event — the graph completed successfully")
                .anyMatch(e -> e.type() == AGUIEvent.EventType.RUN_FINISHED);
    }

    @Test
    @DisplayName("Phase 2: model is called at least twice — once for tool detection, once for verdict after form submit")
    void shouldCallModelAtLeastTwiceWhenFormIsSubmittedWithContext() {
        // given: first call triggers tool call interruption (Phase 1)
        given(mockChatModel.call(any(Prompt.class))).willReturn(toolCallResponse());
        given(mockChatModel.stream(any(Prompt.class))).willReturn(streamingToolCallResponse());

        // Phase 1: trigger interruption
        var phase1Events = webTestClient.post()
                .uri("/langgraph4j/copilotkit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "threadId": "form-resume-thread-3",
                          "runId":    "form-resume-run-3a",
                          "messages": [
                            {"role": "user", "id": "msg-1", "content": "Chce zwrocic produkt"}
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

        // Verify phase 1 produced tool call events (precondition for phase 2)
        assertThat(phase1Events)
                .as("Phase 1 must produce TOOL_CALL_START")
                .anyMatch(e -> e.type() == AGUIEvent.EventType.TOOL_CALL_START);

        // Reset mock invocation count so we can verify Phase 2 calls independently
        org.mockito.Mockito.clearInvocations(mockChatModel);

        // given: after form submission, model should return a verdict text (Phase 2)
        given(mockChatModel.call(any(Prompt.class))).willReturn(verdictResponse());
        given(mockChatModel.stream(any(Prompt.class))).willReturn(streamingVerdictResponse());

        // when: Phase 2 — submit form data with same threadId to resume graph
        var phase2Events = webTestClient.post()
                .uri("/langgraph4j/copilotkit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "threadId": "form-resume-thread-3",
                          "runId":    "form-resume-run-3b",
                          "messages": [
                            {"role": "user", "id": "msg-1", "content": "Chce zwrocic produkt"},
                            {"role": "tool", "id": "result-1", "toolCallId": "call-123", "name": "showReturnForm", "content": "{\\"productName\\":\\"Sukienka\\",\\"type\\":\\"return\\",\\"description\\":\\"Za duza\\"}"}
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

        // then: no errors
        assertThat(phase2Events)
                .as("Phase 2 must NOT produce RUN_ERROR")
                .noneMatch(e -> e.type() == AGUIEvent.EventType.RUN_ERROR);

        // and: Phase 2 must produce text message content (the verdict)
        assertThat(phase2Events)
                .as("Phase 2 must produce TEXT_MESSAGE_CONTENT — the model generated a verdict")
                .anyMatch(e -> e.type() == AGUIEvent.EventType.TEXT_MESSAGE_CONTENT);

        // and: the model must have been called at least once during Phase 2
        // (the graph resumes from the approval node, executes the tool, then calls the model for verdict)
        verify(mockChatModel, atLeast(1)).stream(any(Prompt.class));
    }
}
