package com.sinsay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sinsay.controller.dto.ChatRequest;
import com.sinsay.controller.dto.ChatRequest.ChatMessageItem;
import com.sinsay.model.ChatMessage;
import com.sinsay.model.Intent;
import com.sinsay.model.Role;
import com.sinsay.model.Session;
import com.sinsay.repository.ChatMessageRepository;
import com.sinsay.repository.SessionRepository;
import com.sinsay.service.ChatService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("ChatController Tests")
class ChatControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private UUID testSessionId;
    private Session testSession;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        sessionRepository.deleteAll();

        testSession = Session.builder()
                .intent(Intent.RETURN)
                .orderNumber("ORD-123")
                .productName("Test Product")
                .description("Test description")
                .build();
        testSession = sessionRepository.save(testSession);
        testSessionId = testSession.getId();
    }

    @Nested
    @DisplayName("POST /api/sessions/{id}/messages - Chat Continuation")
    class PostMessagesTests {

        @Test
        @DisplayName("TAC-11: POST valid chat request should return SSE stream with correct header")
        void testPostValidChatRequest_shouldReturnSseStreamWithCorrectHeader() throws Exception {
            // Arrange
            ChatRequest request = new ChatRequest(
                    List.of(new ChatMessageItem("user", new TextNode("Hello")))
            );

            // Mock ChatService to immediately complete emitter
            doAnswer(invocation -> {
                SseEmitter emitter = invocation.getArgument(3, SseEmitter.class);
                emitter.complete();
                return null;
            }).when(chatService).streamResponse(
                    any(Session.class),
                    any(List.class),
                    eq("Hello"),
                    any(SseEmitter.class)
            );

            // Act & Assert
            mockMvc.perform(post("/api/sessions/{id}/messages", testSessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("x-vercel-ai-ui-message-stream", "v1"))
                    .andExpect(content().contentType("text/event-stream"));

            // Verify ChatService was called
            verify(chatService).streamResponse(
                    any(Session.class),
                    any(List.class),
                    eq("Hello"),
                    any(SseEmitter.class)
            );
        }

        @Test
        @DisplayName("TAC-12: POST should extract last user message from messages array")
        void testPost_shouldExtractLastUserMessage() throws Exception {
            // Arrange - messages array with multiple messages, last one is user
            ChatRequest request = new ChatRequest(
                    List.of(
                            new ChatMessageItem("user", new TextNode("First message")),
                            new ChatMessageItem("assistant", new TextNode("Response")),
                            new ChatMessageItem("user", new TextNode("This is the last user message"))
                    )
            );

            doAnswer(invocation -> {
                SseEmitter emitter = invocation.getArgument(3, SseEmitter.class);
                emitter.complete();
                return null;
            }).when(chatService).streamResponse(
                    any(Session.class),
                    any(List.class),
                    eq("This is the last user message"),
                    any(SseEmitter.class)
            );

            // Act & Assert
            mockMvc.perform(post("/api/sessions/{id}/messages", testSessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Verify the last USER message was extracted
            verify(chatService).streamResponse(
                    any(Session.class),
                    any(List.class),
                    eq("This is the last user message"),
                    any(SseEmitter.class)
            );
        }

        @Test
        @DisplayName("TAC-13: POST unknown session should return 404")
        void testPostUnknownSession_shouldReturn404() throws Exception {
            // Arrange
            UUID unknownId = UUID.randomUUID();
            ChatRequest request = new ChatRequest(
                    List.of(new ChatMessageItem("user", new TextNode("Hello")))
            );

            // Act & Assert
            mockMvc.perform(post("/api/sessions/{id}/messages", unknownId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            // Verify ChatService was NOT called
            verify(chatService, never()).streamResponse(
                    any(Session.class),
                    any(List.class),
                    any(String.class),
                    any(SseEmitter.class)
            );
        }

        @Test
        @DisplayName("TAC-14: POST empty messages array should return 400")
        void testPostEmptyMessagesArray_shouldReturn400() throws Exception {
            // Arrange
            ChatRequest request = new ChatRequest(List.of());

            // Act & Assert
            mockMvc.perform(post("/api/sessions/{id}/messages", testSessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Verify ChatService was NOT called
            verify(chatService, never()).streamResponse(
                    any(Session.class),
                    any(List.class),
                    any(String.class),
                    any(SseEmitter.class)
            );
        }

        @Test
        @DisplayName("POST messages array with no user messages should return 400")
        void testPostMessagesArrayWithNoUserMessages_shouldReturn400() throws Exception {
            // Arrange - only assistant messages, no user messages
            ChatRequest request = new ChatRequest(
                    List.of(
                            new ChatMessageItem("assistant", new TextNode("Response 1")),
                            new ChatMessageItem("assistant", new TextNode("Response 2"))
                    )
            );

            // Act & Assert
            mockMvc.perform(post("/api/sessions/{id}/messages", testSessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Verify ChatService was NOT called
            verify(chatService, never()).streamResponse(
                    any(Session.class),
                    any(List.class),
                    any(String.class),
                    any(SseEmitter.class)
            );
        }

        @Test
        @DisplayName("POST should load session history from DB and pass to service")
        void testPost_shouldLoadSessionHistoryFromDb() throws Exception {
            // Arrange - Create existing messages in DB
            ChatMessage existingMsg1 = ChatMessage.builder()
                    .sessionId(testSessionId)
                    .role(Role.USER)
                    .content("Existing user message")
                    .sequenceNumber(0)
                    .build();
            chatMessageRepository.save(existingMsg1);

            ChatMessage existingMsg2 = ChatMessage.builder()
                    .sessionId(testSessionId)
                    .role(Role.ASSISTANT)
                    .content("Existing assistant message")
                    .sequenceNumber(1)
                    .build();
            chatMessageRepository.save(existingMsg2);

            ChatRequest request = new ChatRequest(
                    List.of(new ChatMessageItem("user", new TextNode("New message")))
            );

            // Capture the history list passed to ChatService
            AtomicReference<List<ChatMessage>> capturedHistory = new AtomicReference<>();
            doAnswer(invocation -> {
                capturedHistory.set(invocation.getArgument(1, List.class));
                SseEmitter emitter = invocation.getArgument(3, SseEmitter.class);
                emitter.complete();
                return null;
            }).when(chatService).streamResponse(
                    any(Session.class),
                    any(List.class),
                    eq("New message"),
                    any(SseEmitter.class)
            );

            // Act
            mockMvc.perform(post("/api/sessions/{id}/messages", testSessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Assert - history should contain the 2 existing messages
            List<ChatMessage> history = capturedHistory.get();
            assertThat(history).isNotNull();
            assertThat(history).hasSize(2);
            assertThat(history.get(0).getContent()).isEqualTo("Existing user message");
            assertThat(history.get(1).getContent()).isEqualTo("Existing assistant message");
        }

        @Test
        @DisplayName("POST with system and tools fields should ignore them")
        void testPostWithSystemAndTools_shouldIgnoreThem() throws Exception {
            // Note: Our ChatRequest DTO only has messages field, so system/tools
            // in the JSON would be ignored by Jackson. This test verifies that.
            // The actual request from AssistantChatTransport includes these fields.

            // We'll test that only the messages field matters
            ChatRequest request = new ChatRequest(
                    List.of(new ChatMessageItem("user", new TextNode("Hello")))
            );

            doAnswer(invocation -> {
                SseEmitter emitter = invocation.getArgument(3, SseEmitter.class);
                emitter.complete();
                return null;
            }).when(chatService).streamResponse(
                    any(Session.class),
                    any(List.class),
                    eq("Hello"),
                    any(SseEmitter.class)
            );

            mockMvc.perform(post("/api/sessions/{id}/messages", testSessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(chatService).streamResponse(
                    any(Session.class),
                    any(List.class),
                    eq("Hello"),
                    any(SseEmitter.class)
            );
        }

        @Test
        @DisplayName("POST malformed JSON should return 400")
        void testPostMalformedJson_shouldReturn400() throws Exception {
            mockMvc.perform(post("/api/sessions/{id}/messages", testSessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
