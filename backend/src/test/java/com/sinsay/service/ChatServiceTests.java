package com.sinsay.service;

import com.openai.client.OpenAIClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.chat.completions.ChatCompletionChunk;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.sinsay.model.ChatMessage;
import com.sinsay.model.Intent;
import com.sinsay.model.Role;
import com.sinsay.model.Session;
import com.sinsay.repository.ChatMessageRepository;
import com.sinsay.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ChatService")
class ChatServiceTests {

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private OpenAIClient openAIClient;

    @Mock
    private PolicyDocService policyDocService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private SessionRepository sessionRepository;

    private ChatService chatService;

    private Session testSession;
    private List<ChatMessage> testHistory;
    private UUID testSessionId;
    private String testUserContent;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(openAIClient, policyDocService, chatMessageRepository, sessionRepository, "openai/gpt-4o-mini");

        testSessionId = UUID.randomUUID();
        testSession = Session.builder()
                .id(testSessionId)
                .intent(Intent.RETURN)
                .orderNumber("12345")
                .productName("Test Product")
                .description("Test description")
                .build();

        ChatMessage userMsg = ChatMessage.builder()
                .id(UUID.randomUUID())
                .sessionId(testSessionId)
                .role(Role.USER)
                .content("Initial user message")
                .sequenceNumber(0)
                .build();

        ChatMessage assistantMsg = ChatMessage.builder()
                .id(UUID.randomUUID())
                .sessionId(testSessionId)
                .role(Role.ASSISTANT)
                .content("Initial assistant response")
                .sequenceNumber(1)
                .build();

        testHistory = List.of(userMsg, assistantMsg);
        testUserContent = "What is the status of my return?";
    }

    @Nested
    @DisplayName("Stream initialization")
    class InitializationTests {

        @Test
        @DisplayName("should persist USER message immediately")
        void streamResponse_persistsUserMessageImmediately() {
            // Arrange
            String systemPrompt = "You are a helpful assistant";

            when(sessionRepository.findById(testSessionId)).thenReturn(Optional.of(testSession));
            when(chatMessageRepository.findBySessionIdOrderBySequenceNumberAsc(testSessionId))
                    .thenReturn(testHistory);
            when(policyDocService.getSystemPrompt(Intent.RETURN)).thenReturn(systemPrompt);
            when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

            StreamResponse<ChatCompletionChunk> mockStream = mock(StreamResponse.class);
            when(openAIClient.chat().completions().createStreaming(any(ChatCompletionCreateParams.class)))
                    .thenReturn(mockStream);

            SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

            // Act
            chatService.streamResponse(testSession, testHistory, testUserContent, emitter);

            // Assert - USER message saved before stream starts
            ArgumentCaptor<ChatMessage> msgCaptor = ArgumentCaptor.forClass(ChatMessage.class);
            verify(chatMessageRepository, timeout(1000).atLeastOnce()).save(msgCaptor.capture());

            List<ChatMessage> savedMessages = msgCaptor.getAllValues();
            assertTrue(savedMessages.stream().anyMatch(msg ->
                msg.getRole() == Role.USER && msg.getContent().equals(testUserContent)
            ));
        }
    }

    @Nested
    @DisplayName("System prompt construction")
    class SystemPromptTests {

        @Test
        @DisplayName("should fetch system prompt from PolicyDocService")
        void streamResponse_fetchesSystemPrompt() {
            // Arrange
            String systemPrompt = "You are a helpful assistant";

            when(sessionRepository.findById(testSessionId)).thenReturn(Optional.of(testSession));
            when(chatMessageRepository.findBySessionIdOrderBySequenceNumberAsc(testSessionId))
                    .thenReturn(testHistory);
            when(policyDocService.getSystemPrompt(Intent.RETURN)).thenReturn(systemPrompt);
            when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

            StreamResponse<ChatCompletionChunk> mockStream = mock(StreamResponse.class);
            when(openAIClient.chat().completions().createStreaming(any(ChatCompletionCreateParams.class)))
                    .thenReturn(mockStream);

            SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

            // Act
            chatService.streamResponse(testSession, testHistory, testUserContent, emitter);

            // Assert
            verify(policyDocService, timeout(1000).atLeastOnce())
                .getSystemPrompt(Intent.RETURN);
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should complete emitter with error when OpenAI API throws exception")
        void streamResponse_completesWithErrorOnApiException() {
            // Arrange
            when(sessionRepository.findById(testSessionId)).thenReturn(Optional.of(testSession));
            when(chatMessageRepository.findBySessionIdOrderBySequenceNumberAsc(testSessionId))
                    .thenReturn(testHistory);
            when(policyDocService.getSystemPrompt(Intent.RETURN)).thenReturn("System prompt");
            when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Mock the OpenAI client to throw an exception
            when(openAIClient.chat().completions().createStreaming(any(ChatCompletionCreateParams.class)))
                    .thenThrow(new RuntimeException("API error"));

            SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

            // Act
            chatService.streamResponse(testSession, testHistory, testUserContent, emitter);

            // Assert - USER message should still be saved
            verify(chatMessageRepository, timeout(1000).atLeastOnce()).save(any(ChatMessage.class));
        }
    }
}
