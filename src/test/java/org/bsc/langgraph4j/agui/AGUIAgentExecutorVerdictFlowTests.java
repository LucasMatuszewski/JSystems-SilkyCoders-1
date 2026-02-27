package org.bsc.langgraph4j.agui;

import com.silkycoders1.jsystemssilkycodders1.service.PolicyService;
import com.silkycoders1.jsystemssilkycodders1.tools.SinsayTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AGUIAgentExecutorVerdictFlowTests {

    @Mock
    private PolicyService policyService;

    private AGUIAgentExecutor sut;

    @BeforeEach
    void setUp() {
        sut = new AGUIAgentExecutor(null, policyService, new SinsayTools());
    }

    // --- Intent detection in buildGraphInput ---

    @Test
    void shouldBuildGraphInputWithSystemAndUserMessages() {
        when(policyService.getPoliciesForIntent("")).thenReturn("");

        var input = buildInput("Hello there", List.of());
        var graphInput = sut.buildGraphInput(input);

        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        assertThat(messages).isNotNull();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).isInstanceOf(SystemMessage.class);
        assertThat(messages.get(1)).isInstanceOf(UserMessage.class);
    }

    @Test
    void shouldDetectReturnIntentAndLoadReturnPolicy() {
        when(policyService.getPoliciesForIntent("return")).thenReturn("RETURN_POLICY");

        var input = buildInput("Chcę zwrócić produkt", List.of());
        var graphInput = sut.buildGraphInput(input);

        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        var systemMsg = (SystemMessage) messages.get(0);
        assertThat(systemMsg.getText()).contains("RETURN_POLICY");
    }

    @Test
    void shouldDetectComplaintIntentAndLoadComplaintPolicy() {
        when(policyService.getPoliciesForIntent("complaint")).thenReturn("COMPLAINT_POLICY");

        var input = buildInput("Mam reklamację na wadliwy produkt", List.of());
        var graphInput = sut.buildGraphInput(input);

        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        var systemMsg = (SystemMessage) messages.get(0);
        assertThat(systemMsg.getText()).contains("COMPLAINT_POLICY");
    }

    // --- Form submission detection ---

    @Test
    void shouldDetectFormSubmissionInResultMessages() {
        when(policyService.getPoliciesForIntent("return")).thenReturn("RETURN_POLICY");

        String fakeBase64 = Base64.getEncoder().encodeToString("fake-image-bytes".getBytes());
        String formJson = String.format(
                "{\"productName\":\"Sukienka letnia\",\"type\":\"return\",\"description\":\"Nie pasuje rozmiarem\",\"photo\":\"%s\",\"photoMimeType\":\"image/jpeg\"}",
                fakeBase64
        );

        var resultMsg = new AGUIMessage.ResultMessage(
                "result-1", new Date(), "tool-call-1", "showReturnForm", formJson
        );

        var input = buildInput("Wysłałem formularz zwrotu", List.of(resultMsg));
        var graphInput = sut.buildGraphInput(input);

        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        // Should have: system + multimodal image message + user message = 3
        assertThat(messages).hasSize(3);
        assertThat(messages.get(0)).isInstanceOf(SystemMessage.class);
        assertThat(messages.get(1)).isInstanceOf(UserMessage.class); // multimodal with Media
        assertThat(messages.get(2)).isInstanceOf(UserMessage.class); // user text
    }

    @Test
    void shouldInjectMultimodalMessageWithPhotoMedia() {
        when(policyService.getPoliciesForIntent("return")).thenReturn("RETURN_POLICY");

        String fakeBase64 = Base64.getEncoder().encodeToString("fake-image-bytes".getBytes());
        String formJson = String.format(
                "{\"productName\":\"Sukienka\",\"type\":\"return\",\"description\":\"Za duża\",\"photo\":\"%s\",\"photoMimeType\":\"image/jpeg\"}",
                fakeBase64
        );

        var resultMsg = new AGUIMessage.ResultMessage(
                "result-1", new Date(), "tool-call-1", "showReturnForm", formJson
        );

        var input = buildInput("Formularz wysłany", List.of(resultMsg));
        var graphInput = sut.buildGraphInput(input);

        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        var multimodalMsg = (UserMessage) messages.get(1);
        // The multimodal message should reference the product and intent
        assertThat(multimodalMsg.getText()).containsIgnoringCase("Sukienka");
        assertThat(multimodalMsg.getText()).containsIgnoringCase("Za duża");
        // Should have media content (the photo)
        assertThat(multimodalMsg.getMedia()).hasSize(1);
    }

    @Test
    void shouldUseIntentFromFormDataInsteadOfKeywordDetection() {
        // The form JSON has type:"complaint" but user message has no complaint keywords
        when(policyService.getPoliciesForIntent("complaint")).thenReturn("COMPLAINT_POLICY");

        String fakeBase64 = Base64.getEncoder().encodeToString("fake-image".getBytes());
        String formJson = String.format(
                "{\"productName\":\"Koszulka\",\"type\":\"complaint\",\"description\":\"Zdefektowany guzik\",\"photo\":\"%s\",\"photoMimeType\":\"image/png\"}",
                fakeBase64
        );

        var resultMsg = new AGUIMessage.ResultMessage(
                "result-2", new Date(), "tool-call-2", "showReturnForm", formJson
        );

        var input = buildInput("Proszę sprawdzić", List.of(resultMsg));
        var graphInput = sut.buildGraphInput(input);

        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        var systemMsg = (SystemMessage) messages.get(0);
        // Should load complaint policy because form type is "complaint"
        assertThat(systemMsg.getText()).contains("COMPLAINT_POLICY");
    }

    @Test
    void shouldGracefullyHandleInvalidBase64Photo() {
        when(policyService.getPoliciesForIntent("return")).thenReturn("RETURN_POLICY");

        String formJson = "{\"productName\":\"Shirt\",\"type\":\"return\",\"description\":\"Wrong size\",\"photo\":\"NOT_VALID_BASE64!!!\",\"photoMimeType\":\"image/jpeg\"}";

        var resultMsg = new AGUIMessage.ResultMessage(
                "result-3", new Date(), "tool-call-3", "showReturnForm", formJson
        );

        var input = buildInput("Check this", List.of(resultMsg));
        var graphInput = sut.buildGraphInput(input);

        // Should not throw; gracefully degrades to no multimodal message
        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        // Only system + user = 2 (no multimodal message due to decode failure)
        assertThat(messages).hasSize(2);
    }

    @Test
    void shouldIgnoreNonFormResultMessages() {
        when(policyService.getPoliciesForIntent("")).thenReturn("");

        // Result message with plain text (not JSON) - e.g., a simple tool response
        var resultMsg = new AGUIMessage.ResultMessage(
                "result-4", new Date(), "tool-call-4", "someOtherTool", "plain text result"
        );

        var input = buildInput("Next question", List.of(resultMsg));
        var graphInput = sut.buildGraphInput(input);

        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        // Should have only system + user = 2 (no multimodal injection)
        assertThat(messages).hasSize(2);
    }

    @Test
    void shouldIgnoreFormJsonWithoutPhoto() {
        when(policyService.getPoliciesForIntent("return")).thenReturn("RETURN_POLICY");

        // Form JSON without photo field
        String formJson = "{\"productName\":\"Spodnie\",\"type\":\"return\",\"description\":\"Złe rozmiar\"}";

        var resultMsg = new AGUIMessage.ResultMessage(
                "result-5", new Date(), "tool-call-5", "showReturnForm", formJson
        );

        var input = buildInput("Wysyłam zwrot", List.of(resultMsg));
        var graphInput = sut.buildGraphInput(input);

        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        // No multimodal message since no photo, but intent still detected from form
        assertThat(messages).hasSize(2);
    }

    @Test
    void shouldHandleFormJsonWithEmptyPhoto() {
        when(policyService.getPoliciesForIntent("return")).thenReturn("RETURN_POLICY");

        String formJson = "{\"productName\":\"Bluza\",\"type\":\"return\",\"description\":\"Za mała\",\"photo\":\"\",\"photoMimeType\":\"image/jpeg\"}";

        var resultMsg = new AGUIMessage.ResultMessage(
                "result-6", new Date(), "tool-call-6", "showReturnForm", formJson
        );

        var input = buildInput("Proszę o weryfikację", List.of(resultMsg));
        var graphInput = sut.buildGraphInput(input);

        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        // Empty photo means no multimodal message
        assertThat(messages).hasSize(2);
    }

    @Test
    void shouldSetCorrectMimeTypeFromFormData() {
        when(policyService.getPoliciesForIntent("return")).thenReturn("RETURN_POLICY");

        String fakeBase64 = Base64.getEncoder().encodeToString("png-image-data".getBytes());
        String formJson = String.format(
                "{\"productName\":\"Buty\",\"type\":\"return\",\"description\":\"Odbarwione\",\"photo\":\"%s\",\"photoMimeType\":\"image/png\"}",
                fakeBase64
        );

        var resultMsg = new AGUIMessage.ResultMessage(
                "result-7", new Date(), "tool-call-7", "showReturnForm", formJson
        );

        var input = buildInput("Sprawdź proszę", List.of(resultMsg));
        var graphInput = sut.buildGraphInput(input);

        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        assertThat(messages).hasSize(3);
        var multimodalMsg = (UserMessage) messages.get(1);
        assertThat(multimodalMsg.getMedia()).hasSize(1);
        assertThat(multimodalMsg.getMedia().iterator().next().getMimeType().toString()).isEqualTo("image/png");
    }

    // --- Helper methods ---

    private AGUIType.RunAgentInput buildInput(String userText, List<AGUIMessage> extraMessages) {
        var userMsg = AGUIMessage.userMessage("msg-1", userText);
        var allMessages = new ArrayList<AGUIMessage>();
        allMessages.addAll(extraMessages);
        allMessages.add(userMsg);
        return new AGUIType.RunAgentInput("thread-1", "run-1", null, allMessages, null, null, null);
    }
}
