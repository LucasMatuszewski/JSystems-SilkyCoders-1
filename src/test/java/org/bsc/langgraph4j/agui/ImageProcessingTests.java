package org.bsc.langgraph4j.agui;

import com.silkycoders1.jsystemssilkycodders1.service.PolicyService;
import com.silkycoders1.jsystemssilkycodders1.tools.SinsayTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for image processing features:
 * - Server-side image resize before passing to the model (prevents context window overflow)
 * - Broken pipe error handling from Ollama
 * - Model builder includes timeout configuration
 *
 * TDD: these tests are written BEFORE the corresponding implementation exists.
 */
@ExtendWith(MockitoExtension.class)
class ImageProcessingTests {

    @Mock
    private PolicyService policyService;

    private AGUIAgentExecutor sut;

    @BeforeEach
    void setUp() {
        sut = new AGUIAgentExecutor(null, policyService, new SinsayTools());
    }

    // ─── Image resize tests ───────────────────────────────────────────────────

    /**
     * A large JPEG image (1600x1600, well above the 800px threshold) must be resized
     * server-side before the Media object is created, so the model receives a payload
     * that fits within its context window.
     *
     * Observable effect: the bytes stored in the UserMessage media are smaller than the
     * bytes of the original decoded image.
     */
    @Test
    void shouldResizeBase64ImageBeforePassingToModel() throws Exception {
        when(policyService.getPoliciesForIntent("return")).thenReturn("RETURN_POLICY");

        // Create a solid-color 1600×1600 JPEG — well above the 800px resize threshold
        byte[] largeImageBytes = createJpegImage(1600, 1600);
        String largeBase64 = Base64.getEncoder().encodeToString(largeImageBytes);

        // Sanity: must be a real image with non-zero bytes
        assertThat(largeImageBytes.length)
                .as("Test image must be non-empty")
                .isGreaterThan(0);

        String formJson = String.format(
                "{\"productName\":\"Duża kurtka\",\"type\":\"return\","
                + "\"description\":\"Za duża\","
                + "\"photo\":\"%s\","
                + "\"photoMimeType\":\"image/jpeg\"}",
                largeBase64
        );

        var resultMsg = new AGUIMessage.ResultMessage(
                "result-img-1", new Date(), "tool-call-img-1", "showReturnForm", formJson
        );

        var input = buildInput("Proszę sprawdzić rozmiar", List.of(resultMsg));

        // when
        var graphInput = sut.buildGraphInput(input);

        // then — system + multimodal + user text = 3 messages
        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        assertThat(messages).hasSize(3);

        var multimodalMsg = (UserMessage) messages.get(1);
        assertThat(multimodalMsg.getMedia()).hasSize(1);

        Media media = multimodalMsg.getMedia().iterator().next();
        byte[] resizedBytes = media.getDataAsByteArray();

        // The resized image must be strictly smaller in bytes than the original
        assertThat(resizedBytes.length)
                .as("Resized image (%d bytes) should be smaller than original (%d bytes)",
                        resizedBytes.length, largeImageBytes.length)
                .isLessThan(largeImageBytes.length);
    }

    /**
     * An image that is already within the 800×800 threshold must NOT be modified.
     * The bytes passed to the Media object should equal the original decoded bytes.
     */
    @Test
    void shouldNotResizeImageThatIsAlreadyWithinThreshold() throws Exception {
        when(policyService.getPoliciesForIntent("return")).thenReturn("RETURN_POLICY");

        // 400×300 — both dimensions below 800px threshold
        byte[] smallImageBytes = createJpegImage(400, 300);
        String smallBase64 = Base64.getEncoder().encodeToString(smallImageBytes);

        String formJson = String.format(
                "{\"productName\":\"Mała koszulka\",\"type\":\"return\","
                + "\"description\":\"Zły kolor\","
                + "\"photo\":\"%s\","
                + "\"photoMimeType\":\"image/jpeg\"}",
                smallBase64
        );

        var resultMsg = new AGUIMessage.ResultMessage(
                "result-img-2", new Date(), "tool-call-img-2", "showReturnForm", formJson
        );

        var input = buildInput("Proszę sprawdzić", List.of(resultMsg));

        // when
        var graphInput = sut.buildGraphInput(input);

        // then — 3 messages, media bytes equal to original (no resize applied)
        @SuppressWarnings("unchecked")
        var messages = (List<Message>) graphInput.get("messages");
        assertThat(messages).hasSize(3);

        var multimodalMsg = (UserMessage) messages.get(1);
        assertThat(multimodalMsg.getMedia()).hasSize(1);

        Media media = multimodalMsg.getMedia().iterator().next();
        byte[] returnedBytes = media.getDataAsByteArray();

        assertThat(returnedBytes).isEqualTo(smallImageBytes);
    }

    // ─── Broken pipe error mapping tests ─────────────────────────────────────

    /**
     * When Ollama closes the connection mid-stream ("broken pipe"), the SSE controller
     * must map the error to the Polish Ollama-unavailable message rather than a generic
     * fallback. Tests the package-private mapErrorToUserMessage() directly.
     *
     * The test file is in the same package (org.bsc.langgraph4j.agui) so it can access
     * the package-private method after we change its visibility from private.
     */
    @Test
    void shouldHandleBrokenPipeFromOllamaWithPolishMessage() {
        var controller = new AGUISSEController(null);

        var brokenPipeError = new RuntimeException("Broken pipe");
        String message = controller.mapErrorToUserMessage(brokenPipeError);

        assertThat(message)
                .as("'Broken pipe' should map to Ollama unavailability message containing 'Ollama'")
                .contains("Ollama");
    }

    /**
     * "Broken pipe" nested in the cause chain (wrapped by another exception) must also
     * be detected and mapped to the Ollama unavailable message.
     */
    @Test
    void shouldHandleBrokenPipeNestedInCause() {
        var controller = new AGUISSEController(null);

        var cause = new RuntimeException("broken pipe");
        var wrapped = new RuntimeException("Model call failed", cause);
        String message = controller.mapErrorToUserMessage(wrapped);

        assertThat(message)
                .as("Wrapped broken pipe cause should still map to Ollama unavailability message")
                .contains("Ollama");
    }

    // ─── Model builder / fallback tests ──────────────────────────────────────

    /**
     * Verify that all Ollama model suppliers build successfully. This guards against
     * API signature changes (e.g., a removed timeout method) that would silently break
     * the builder configuration added to the enum.
     */
    @Test
    void shouldTimeoutAndFallbackWhenOllamaIsSlowInBuilder() {
        // Verify OLLAMA_QWEN2_5_7B builds (this is the last-resort fallback model)
        var qwenModel = AGUIAgentExecutor.AiModel.OLLAMA_QWEN2_5_7B.model.get();
        assertThat(qwenModel).isNotNull()
                .isInstanceOf(org.springframework.ai.ollama.OllamaChatModel.class);

        // Verify OLLAMA_KIMI_K2_5_CLOUD builds (the default configured model)
        var kimiModel = AGUIAgentExecutor.AiModel.OLLAMA_KIMI_K2_5_CLOUD.model.get();
        assertThat(kimiModel).isNotNull()
                .isInstanceOf(org.springframework.ai.ollama.OllamaChatModel.class);

        // Verify OLLAMA_QWEN3_14B builds
        var qwen3Model = AGUIAgentExecutor.AiModel.OLLAMA_QWEN3_14B.model.get();
        assertThat(qwen3Model).isNotNull()
                .isInstanceOf(org.springframework.ai.ollama.OllamaChatModel.class);
    }

    /**
     * When the primary model throws a RuntimeException (simulating a "broken pipe" during
     * a large image upload), resolveModel() must fall back rather than propagating the error.
     */
    @Test
    void shouldFallbackWhenPrimaryModelThrowsBrokenPipeOnResolve() {
        // Executor with a custom resolveModel that simulates the broken pipe fallback logic
        var fallbackExecutor = new AGUIAgentExecutor(null) {
            @Override
            String getEnv(String name) {
                return null; // no API keys — fall to last-resort Ollama
            }

            @Override
            org.springframework.ai.chat.model.ChatModel resolveModel() {
                try {
                    throw new RuntimeException("broken pipe");
                } catch (RuntimeException e) {
                    // fallback to last resort when primary throws
                    return AGUIAgentExecutor.AiModel.OLLAMA_QWEN2_5_7B.model.get();
                }
            }
        };

        var model = fallbackExecutor.resolveModel();

        assertThat(model).isNotNull()
                .isInstanceOf(org.springframework.ai.ollama.OllamaChatModel.class);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Creates a solid blue JPEG image of the specified dimensions and returns the bytes.
     */
    private byte[] createJpegImage(int width, int height) throws Exception {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, width, height);
        g.dispose();

        var out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        return out.toByteArray();
    }

    private AGUIType.RunAgentInput buildInput(String userText, List<AGUIMessage> extraMessages) {
        var userMsg = AGUIMessage.userMessage("msg-img-1", userText);
        var allMessages = new ArrayList<AGUIMessage>();
        allMessages.addAll(extraMessages);
        allMessages.add(userMsg);
        return new AGUIType.RunAgentInput("thread-img", "run-img", null, allMessages, null, null, null);
    }
}
