package com.silkycoders1.jsystemssilkycodders1;

import org.bsc.langgraph4j.agui.AGUIAgent;
import org.bsc.langgraph4j.agui.AGUIEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.net.ConnectException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb-sse;DB_CLOSE_DELAY=-1",
    "spring.r2dbc.username=sa",
    "spring.r2dbc.password=",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:schema.sql",
    "ag-ui.agent=agentExecutor",
    "ag-ui.model=OLLAMA_QWEN2_5_7B"
})
class AGUISSEControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean(name = "AGUIAgent")
    private AGUIAgent uiAgent;

    private static final String MINIMAL_RUN_INPUT = """
            {
              "threadId": "thread-1",
              "runId":    "run-1",
              "messages": [
                {"role": "user", "id": "msg-1", "content": "Cześć"}
              ]
            }
            """;

    @Test
    void shouldStreamRunErrorEventWhenAgentThrowsConnectionRefusedException() {
        given(uiAgent.run(any())).willReturn(
                Flux.error(new ConnectException("Connection refused: localhost/127.0.0.1:11434"))
        );

        var events = webTestClient.post()
                .uri("/langgraph4j/copilotkit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(MINIMAL_RUN_INPUT)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(AGUIEvent.RunErrorEvent.class)
                .getResponseBody()
                .collectList()
                .block();

        assertThat(events).hasSize(1);
        var errorEvent = events.get(0);
        assertThat(errorEvent.type()).isEqualTo(AGUIEvent.EventType.RUN_ERROR);
        assertThat(errorEvent.message()).contains("Ollama");
    }

    @Test
    void shouldStreamRunErrorEventWithGenericMessageForUnknownErrors() {
        given(uiAgent.run(any())).willReturn(
                Flux.error(new RuntimeException("Some unexpected internal error"))
        );

        var events = webTestClient.post()
                .uri("/langgraph4j/copilotkit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(MINIMAL_RUN_INPUT)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(AGUIEvent.RunErrorEvent.class)
                .getResponseBody()
                .collectList()
                .block();

        assertThat(events).hasSize(1);
        var errorEvent = events.get(0);
        assertThat(errorEvent.type()).isEqualTo(AGUIEvent.EventType.RUN_ERROR);
        assertThat(errorEvent.message()).isNotBlank();
    }

    @Test
    void shouldStreamRunErrorEventWhenAgentThrowsUnauthorizedException() {
        given(uiAgent.run(any())).willReturn(
                Flux.error(new RuntimeException("401 Unauthorized: invalid API key"))
        );

        var events = webTestClient.post()
                .uri("/langgraph4j/copilotkit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(MINIMAL_RUN_INPUT)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(AGUIEvent.RunErrorEvent.class)
                .getResponseBody()
                .collectList()
                .block();

        assertThat(events).hasSize(1);
        var errorEvent = events.get(0);
        assertThat(errorEvent.message()).contains("autoryzacji");
    }

    @Test
    void shouldReturnStreamOfEventsNormallyWhenAgentSucceeds() {
        Flux<AGUIEvent> happyFlux = Flux.just(
                new AGUIEvent.RunStartedEvent("thread-1", "run-1"),
                new AGUIEvent.RunFinishedEvent("thread-1", "run-1")
        );
        given(uiAgent.run(any())).willAnswer(inv -> happyFlux);

        webTestClient.post()
                .uri("/langgraph4j/copilotkit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(MINIMAL_RUN_INPUT)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
    }
}
