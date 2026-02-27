package org.bsc.langgraph4j.agui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.net.ConnectException;
import java.time.Duration;
import java.time.LocalTime;

@RestController
@RequestMapping("/langgraph4j")
public class AGUISSEController {

    private static final Logger log = LoggerFactory.getLogger(AGUISSEController.class);

    final AGUIAgent uiAgent;
    final ObjectMapper mapper = new ObjectMapper();

    public AGUISSEController( @Qualifier("AGUIAgent")   AGUIAgent uiAgent) {
        this.uiAgent = uiAgent;
    }

    @PostMapping(path = "/copilotkit",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
            )
    @SuppressWarnings("unchecked")
    public Flux<AGUIEvent> copilotKit(@RequestBody String runAgentInputPayload) throws Exception {

        // Log raw payload summary so we can confirm photo arrives from CopilotKit's LangGraphHttpAgent
        boolean payloadHasPhoto = runAgentInputPayload.contains("\"photo\"") &&
                                   runAgentInputPayload.contains("\"photoMimeType\"");
        boolean payloadHasToolRole = runAgentInputPayload.contains("\"role\":\"tool\"");
        log.info("SSE /copilotkit received: payloadLen={}, hasToolMessage={}, hasPhotoField={}",
                runAgentInputPayload.length(), payloadHasToolRole, payloadHasPhoto);

        var input = mapper.readValue(runAgentInputPayload, AGUIType.RunAgentInput.class);

        Flux<AGUIEvent> agentFlux = (Flux<AGUIEvent>) uiAgent.run(input);
        return agentFlux.onErrorResume(throwable -> {
            String causeMessage = throwable.getCause() != null
                    ? throwable.getCause().getMessage()
                    : "no cause";
            log.error("Agent execution failed: {} | cause: {}", throwable.getMessage(), causeMessage, throwable);
            return Flux.just(new AGUIEvent.RunErrorEvent(mapErrorToUserMessage(throwable), "AGENT_ERROR"));
        });
    }

    String mapErrorToUserMessage(Throwable throwable) {
        String msg = throwable.getMessage() != null ? throwable.getMessage().toLowerCase() : "";
        Throwable cause = throwable.getCause();
        String causeMsg = (cause != null && cause.getMessage() != null) ? cause.getMessage().toLowerCase() : "";

        if (throwable instanceof ConnectException
                || msg.contains("connection refused")
                || causeMsg.contains("connection refused")
                || msg.contains("connect to")
                || msg.contains("broken pipe")
                || causeMsg.contains("broken pipe")) {
            return "Nie można połączyć się z modelem AI. Sprawdź czy Ollama jest uruchomiona i model jest dostępny (port 11434).";
        }
        if (msg.contains("401") || msg.contains("unauthorized") || msg.contains("api key")
                || msg.contains("forbidden") || msg.contains("403")) {
            return "Błąd autoryzacji do modelu AI. Sprawdź poprawność klucza API.";
        }
        if (msg.contains("503") || msg.contains("service unavailable") || msg.contains("overloaded")) {
            return "Model AI jest chwilowo niedostępny. Spróbuj ponownie za chwilę.";
        }
        if (msg.contains("timeout") || msg.contains("timed out")) {
            return "Odpowiedź modelu AI trwała zbyt długo. Spróbuj ponownie.";
        }
        return "Wystąpił błąd podczas przetwarzania wiadomości. Spróbuj ponownie.";
    }

    /**
     * Endpoint to stream Server-Sent Events.
     * This example emits a message every second with the current time.
     *
     * @return A Flux of ServerSentEvent objects.
     */
    // @GetMapping(path = "/sse-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamEvents() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> ServerSentEvent.<String>builder()
                        .id(String.valueOf(sequence))
                        .event("periodic-event")
                        .data("SSE - " + LocalTime.now().toString())
                        .comment("This is a comment for event " + sequence)
                        .retry(Duration.ofSeconds(5)) // Client should retry after 5 seconds if connection is lost
                        .build());
    }

    /**
     * A more complex example demonstrating different event types and data structures.
     *
     * @return A Flux of ServerSentEvent objects.
     */
    // @GetMapping(path = "/sse-complex-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> streamComplexEvents() {
        // Simulate different types of events
        Flux<ServerSentEvent<Object>> heartBeat = Flux.interval(Duration.ofSeconds(10))
                .map(seq -> ServerSentEvent.builder()
                        .comment("keep-alive")
                        .build());

        Flux<ServerSentEvent<Object>> dataEvents = Flux.interval(Duration.ofSeconds(2))
                .take(5) // Emit 5 data events
                .map(sequence -> {
                    // Simulate different event names and data
                    if (sequence % 2 == 0) {
                        return ServerSentEvent.builder()
                                .id("data-" + sequence)
                                .event("data-update")
                                .data(new DataObject("Item " + sequence, (int) (sequence * 100)))
                                .build();
                    } else {
                        return ServerSentEvent.builder()
                                .id("alert-" + sequence)
                                .event("alert")
                                .data("Critical Alert at " + LocalTime.now())
                                .build();
                    }
                });

        Flux<ServerSentEvent<Object>> completionEvent = Flux.just(
                ServerSentEvent.builder()
                        .event("stream-completed")
                        .data("The event stream has finished.")
                        .build()
        ).delayElements(Duration.ofSeconds(11)); // Ensure it's sent after data events

        return Flux.merge(heartBeat, dataEvents, completionEvent);
    }

    // Example data object for complex events
    private record DataObject(String name, int value) {}

}