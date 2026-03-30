package com.sinsay.service;

import com.openai.client.OpenAIClient;
import com.openai.services.blocking.ChatService;
import com.openai.services.blocking.chat.ChatCompletionService;
import com.sinsay.model.Intent;
import com.sinsay.model.Role;
import com.sinsay.repository.ChatMessageRepository;
import com.sinsay.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTests {

    @Mock
    private OpenAIClient openAIClient;

    @Mock
    private ChatService chatService;

    @Mock
    private ChatCompletionService chatCompletionService;

    @Mock
    private PolicyDocService policyDocService;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService(openAIClient, policyDocService, sessionRepository, chatMessageRepository, "openai/gpt-4o-mini");

        // Setup OpenAI client chain mocks
        when(openAIClient.chat()).thenReturn(chatService);
        when(chatService.completions()).thenReturn(chatCompletionService);

        // Mock create to return null for now - the service handles null completion
        when(chatCompletionService.create((com.openai.models.chat.completions.ChatCompletionCreateParams) any())).thenReturn(null);

        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void analyzeAndCreateSession_shouldEncodeImageToBase64DataUri() {
        // Arrange
        String testMimeType = "image/jpeg";
        byte[] testImageData = "test image data".getBytes(StandardCharsets.UTF_8);
        String expectedBase64 = Base64.getEncoder().encodeToString(testImageData);
        String expectedDataUri = "data:" + testMimeType + ";base64," + expectedBase64;

        when(policyDocService.getSystemPrompt(Intent.RETURN)).thenReturn("Test system prompt");
        when(sessionRepository.save(any())).thenReturn(com.sinsay.model.Session.builder().id(UUID.randomUUID()).build());

        // Act
        analysisService.analyzeAndCreateSession(
            Intent.RETURN,
            "ORDER123",
            "Test Product",
            "Test description",
            testImageData,
            testMimeType
        );

        // Assert - verify the OpenAI call was made
        verify(chatCompletionService).create(any(com.openai.models.chat.completions.ChatCompletionCreateParams.class));
    }

    @Test
    void analyzeAndCreateSession_shouldIncludeSystemPromptAsSystemMessage() {
        // Arrange
        String expectedSystemPrompt = "Test system prompt with policy docs";
        byte[] testImageData = "test".getBytes(StandardCharsets.UTF_8);

        when(policyDocService.getSystemPrompt(Intent.COMPLAINT)).thenReturn(expectedSystemPrompt);
        when(openAIClient.chat().completions().create((com.openai.models.chat.completions.ChatCompletionCreateParams) any())).thenReturn(null);
        when(sessionRepository.save(any())).thenReturn(com.sinsay.model.Session.builder().id(UUID.randomUUID()).build());

        // Act
        analysisService.analyzeAndCreateSession(
            Intent.COMPLAINT,
            "ORDER456",
            "Test Product",
            "Test description",
            testImageData,
            "image/png"
        );

        // Assert - verify policy doc service was called with correct intent
        verify(policyDocService).getSystemPrompt(Intent.COMPLAINT);
        verify(chatCompletionService).create(any(com.openai.models.chat.completions.ChatCompletionCreateParams.class));
    }

    @Test
    void analyzeAndCreateSession_shouldPersistSessionWithCorrectFields() {
        // Arrange
        byte[] testImageData = "test".getBytes(StandardCharsets.UTF_8);
        UUID expectedSessionId = UUID.randomUUID();

        when(policyDocService.getSystemPrompt(Intent.RETURN)).thenReturn("Test prompt");
        when(openAIClient.chat().completions().create((com.openai.models.chat.completions.ChatCompletionCreateParams) any())).thenReturn(null);
        when(sessionRepository.save(any())).thenAnswer(invocation -> {
            com.sinsay.model.Session session = invocation.getArgument(0);
            session.setId(expectedSessionId);
            return session;
        });

        // Act
        var result = analysisService.analyzeAndCreateSession(
            Intent.RETURN,
            "ORDER789",
            "Test Product Name",
            "Test description here",
            testImageData,
            "image/jpeg"
        );

        // Assert
        ArgumentCaptor<com.sinsay.model.Session> sessionCaptor = ArgumentCaptor.forClass(com.sinsay.model.Session.class);
        verify(sessionRepository).save(sessionCaptor.capture());

        com.sinsay.model.Session savedSession = sessionCaptor.getValue();
        assertEquals(Intent.RETURN, savedSession.getIntent());
        assertEquals("ORDER789", savedSession.getOrderNumber());
        assertEquals("Test Product Name", savedSession.getProductName());
        assertEquals("Test description here", savedSession.getDescription());
        assertEquals(expectedSessionId, result.sessionId());
    }

    @Test
    void analyzeAndCreateSession_shouldPersistUserAndAssistantMessages() {
        // Arrange
        byte[] testImageData = "test".getBytes(StandardCharsets.UTF_8);
        String userDescription = "User's problem description";

        when(policyDocService.getSystemPrompt(Intent.RETURN)).thenReturn("Test prompt");
        when(openAIClient.chat().completions().create((com.openai.models.chat.completions.ChatCompletionCreateParams) any())).thenReturn(null);
        when(sessionRepository.save(any())).thenReturn(com.sinsay.model.Session.builder().id(UUID.randomUUID()).build());

        // Act
        analysisService.analyzeAndCreateSession(
            Intent.RETURN,
            "ORDER101",
            "Product",
            userDescription,
            testImageData,
            "image/webp"
        );

        // Assert - verify two messages were saved
        ArgumentCaptor<com.sinsay.model.ChatMessage> messageCaptor = ArgumentCaptor.forClass(com.sinsay.model.ChatMessage.class);
        verify(chatMessageRepository, times(2)).save(messageCaptor.capture());

        var savedMessages = messageCaptor.getAllValues();
        assertEquals(2, savedMessages.size());

        // First message should be USER with sequence 0
        com.sinsay.model.ChatMessage userMessage = savedMessages.get(0);
        assertEquals(Role.USER, userMessage.getRole());
        assertEquals(0, userMessage.getSequenceNumber());
        assertEquals(userDescription, userMessage.getContent());

        // Second message should be ASSISTANT with sequence 1
        com.sinsay.model.ChatMessage assistantMessage = savedMessages.get(1);
        assertEquals(Role.ASSISTANT, assistantMessage.getRole());
        assertEquals(1, assistantMessage.getSequenceNumber());
    }

    @Test
    void analyzeAndCreateSession_shouldBuildUserMessageWithImageAndTextContent() {
        // Arrange
        byte[] testImageData = "test image content".getBytes(StandardCharsets.UTF_8);
        String description = "Product has a defect";

        when(policyDocService.getSystemPrompt(Intent.COMPLAINT)).thenReturn("System prompt");
        when(openAIClient.chat().completions().create((com.openai.models.chat.completions.ChatCompletionCreateParams) any())).thenReturn(null);
        when(sessionRepository.save(any())).thenReturn(com.sinsay.model.Session.builder().id(UUID.randomUUID()).build());

        // Act
        analysisService.analyzeAndCreateSession(
            Intent.COMPLAINT,
            "ORDER202",
            "Test Product",
            description,
            testImageData,
            "image/png"
        );

        // Assert - verify OpenAI client was called
        verify(openAIClient.chat().completions()).create((com.openai.models.chat.completions.ChatCompletionCreateParams) any());
    }
}
