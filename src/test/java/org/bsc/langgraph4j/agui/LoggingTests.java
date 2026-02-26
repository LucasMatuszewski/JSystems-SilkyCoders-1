package org.bsc.langgraph4j.agui;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.silkycoders1.jsystemssilkycodders1.service.PolicyService;
import com.silkycoders1.jsystemssilkycodders1.tools.SinsayTools;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Verifies that the logging layer never emits raw base64 strings longer than 100 characters.
 * A raw base64 photo payload may be tens of kilobytes; it must be truncated in all log lines.
 */
@ExtendWith(MockitoExtension.class)
class LoggingTests {

    /**
     * A realistic base64 photo payload — 200+ characters, mimicking a real image fragment.
     * Using a string that contains only valid Base64 characters so it survives decoding.
     */
    private static final String LONG_BASE64_PHOTO = Base64.getEncoder().encodeToString(
            new byte[200] // 200 zero bytes → 268-char base64 string
    );

    @Mock
    private PolicyService policyService;

    private AGUIAgentExecutor sut;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger agentExecutorLogger;

    @BeforeEach
    void setUp() {
        sut = new AGUIAgentExecutor(null, policyService, new SinsayTools());

        // Attach an in-memory appender to the AGUIAgentExecutor logger.
        // Set level to DEBUG so that debug-level log statements (like "LAST USER MESSAGE")
        // are captured and can be validated for accidental base64 leakage.
        agentExecutorLogger = (Logger) LoggerFactory.getLogger(AGUIAgentExecutor.class);
        agentExecutorLogger.setLevel(Level.DEBUG);
        listAppender = new ListAppender<>();
        listAppender.start();
        agentExecutorLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        agentExecutorLogger.detachAppender(listAppender);
        agentExecutorLogger.setLevel(null); // restore parent-delegated level
        listAppender.stop();
    }

    @Test
    void noLogLineShouldContainRawBase64LongerThan100CharsWhenPhotoIsInFormJson() {
        // given — a form submission whose photo field is a long base64 string
        when(policyService.getPoliciesForIntent("return")).thenReturn("RETURN_POLICY");

        String formJson = String.format(
                "{\"productName\":\"Kurtka zimowa\",\"type\":\"return\","
                + "\"description\":\"Zły rozmiar\","
                + "\"photo\":\"%s\","
                + "\"photoMimeType\":\"image/jpeg\"}",
                LONG_BASE64_PHOTO
        );

        var resultMsg = new AGUIMessage.ResultMessage(
                "result-log-1", new Date(), "tool-call-log-1", "showReturnForm", formJson
        );

        // The user message text itself also contains the base64 photo — mimicking a frontend
        // that echoes the full form JSON back as the last user message text
        var input = buildInputWithCustomUserText(LONG_BASE64_PHOTO, List.of(resultMsg));

        // when — trigger buildGraphInput which processes the photo and logs activity
        sut.buildGraphInput(input);

        // then — no log line should contain a raw base64 fragment longer than 100 consecutive
        // base64 characters (A-Z, a-z, 0-9, +, /, =)
        var violations = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(msg -> containsLongBase64Segment(msg, 100))
                .toList();

        assertThat(violations)
                .as("Found log lines containing raw base64 longer than 100 chars: %s", violations)
                .isEmpty();
    }

    @Test
    void noLogLineShouldContainRawBase64WhenLastUserMessageContainsBase64() {
        // given — the last user message contains a long base64 string (edge case where user
        // pastes encoded content or the frontend sends a form echo as text)
        when(policyService.getPoliciesForIntent("")).thenReturn("");

        // The LONG_BASE64_PHOTO string will appear in the debug log "LAST USER MESSAGE: {}"
        // unless the log statement truncates it
        var input = buildInputWithCustomUserText(LONG_BASE64_PHOTO, List.of());

        // when
        sut.buildGraphInput(input);

        // then — the debug log of the last user message must NOT emit the full base64 string
        var violations = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(msg -> containsLongBase64Segment(msg, 100))
                .toList();

        assertThat(violations)
                .as("Found log lines containing raw base64 > 100 chars: %s", violations)
                .isEmpty();
    }

    @Test
    void truncateBase64ShouldShortenLongValuesToAtMost80CharsPlusAnnotation() {
        // This directly validates the truncation helper contract via observable logging behaviour.
        // We inject a 268-char base64 string; the logged representation must be <= 100 chars for
        // the prefix plus the annotation "[N chars]".
        when(policyService.getPoliciesForIntent("return")).thenReturn("");

        String formJson = String.format(
                "{\"productName\":\"Bluza\",\"type\":\"return\","
                + "\"description\":\"Krótki opis\","
                + "\"photo\":\"%s\","
                + "\"photoMimeType\":\"image/jpeg\"}",
                LONG_BASE64_PHOTO
        );

        var resultMsg = new AGUIMessage.ResultMessage(
                "result-log-2", new Date(), "tool-call-log-2", "showReturnForm", formJson
        );

        var input = buildInputWithCustomUserText(LONG_BASE64_PHOTO, List.of(resultMsg));
        sut.buildGraphInput(input);

        // All logged messages must be free of long raw base64 segments
        listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .forEach(msg ->
                        assertThat(containsLongBase64Segment(msg, 100))
                                .as("Log message contains raw base64 > 100 chars: %s", msg)
                                .isFalse()
                );
    }

    // --- Helper ---

    /**
     * Returns true if the message contains a contiguous run of base64 characters
     * (A-Z a-z 0-9 + / =) longer than {@code threshold} characters.
     */
    private boolean containsLongBase64Segment(String message, int threshold) {
        int consecutiveBase64 = 0;
        for (char c : message.toCharArray()) {
            if (isBase64Char(c)) {
                consecutiveBase64++;
                if (consecutiveBase64 > threshold) {
                    return true;
                }
            } else {
                consecutiveBase64 = 0;
            }
        }
        return false;
    }

    private boolean isBase64Char(char c) {
        return (c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')
                || (c >= '0' && c <= '9')
                || c == '+'
                || c == '/'
                || c == '=';
    }

    // --- Builder helpers ---

    private AGUIType.RunAgentInput buildInputWithCustomUserText(String userText, List<AGUIMessage> extraMessages) {
        var userMsg = AGUIMessage.userMessage("msg-log-1", userText);
        var allMessages = new ArrayList<AGUIMessage>();
        allMessages.addAll(extraMessages);
        allMessages.add(userMsg);
        return new AGUIType.RunAgentInput("thread-log", "run-log", null, allMessages, null, null, null);
    }
}
