package com.silkycoders1.jsystemssilkycodders1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silkycoders1.jsystemssilkycodders1.controller.MessageDto;
import com.silkycoders1.jsystemssilkycodders1.repository.ChatMessageRepository;
import com.silkycoders1.jsystemssilkycodders1.repository.VerificationSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private VerificationSessionRepository sessionRepo;
    @Mock
    private ChatMessageRepository messageRepo;
    @Mock
    private ObjectMapper objectMapper;

    private VerificationService verificationService;

    @BeforeEach
    void setUp() {
        verificationService = new VerificationService(chatClientBuilder, sessionRepo, messageRepo, objectMapper);
    }

    @Test
    void getSystemPrompt_Return_ContainsReturnPolicy() {
        String prompt = verificationService.getSystemPrompt("RETURN");
        assertThat(prompt).contains("POLICY: STANDARD RETURN");
        assertThat(prompt).contains("30 days");
        assertThat(prompt).contains("<thought>");
    }

    @Test
    void getSystemPrompt_Complaint_ContainsComplaintPolicy() {
        String prompt = verificationService.getSystemPrompt("COMPLAINT");
        assertThat(prompt).contains("POLICY: COMPLAINT");
        assertThat(prompt).contains("2 years warranty");
    }

    @Test
    void convertUserMessage_TextContent_ExtractsText() {
        MessageDto msg = new MessageDto("user", "Hello world", null);
        UserMessage userMessage = verificationService.convertUserMessage(msg);

        assertThat(userMessage.getText()).isEqualTo("Hello world");
    }

    @Test
    void convertUserMessage_ListContent_ExtractsText() {
        // Imitate [{"type": "text", "text": "Hello"}]
        Map<String, String> textPart = Map.of("type", "text", "text", "Complex content");
        MessageDto msg = new MessageDto("user", List.of(textPart), null);

        UserMessage userMessage = verificationService.convertUserMessage(msg);

        assertThat(userMessage.getText()).isEqualTo("Complex content");
    }

    @Test
    void convertUserMessage_WithImage_ExtractsMedia() {
        // 1x1 pixel red dot png base64
        String base64Png = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";
        Map<String, String> attachment = Map.of("url", base64Png);
        
        MessageDto msg = new MessageDto("user", "Check this", List.of(attachment));

        UserMessage userMessage = verificationService.convertUserMessage(msg);

        assertThat(userMessage.getText()).isEqualTo("Check this");
        Collection<Media> media = userMessage.getMedia();
        assertThat(media).hasSize(1);
        Media firstMedia = media.iterator().next();
        assertThat(firstMedia.getMimeType().toString()).isEqualTo("image/png");
    }
}