package com.sinsay;

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
import com.sinsay.service.AnalysisService;
import com.sinsay.service.ChatService;
import com.sinsay.service.dto.AnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-flow integration test for the session lifecycle.
 *
 * Steps (ordered, share instance via PER_CLASS lifecycle):
 *  1. POST /api/sessions  — initial form submission with image
 *  2. GET  /api/sessions/{id} — verify session and initial messages
 *  3. POST /api/sessions/{id}/messages — chat continuation (SSE streaming)
 *  4. GET  /api/sessions/{id} — verify all 4 messages persisted
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Full-flow integration: session lifecycle")
class FullFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @MockBean
    private AnalysisService analysisService;

    @MockBean
    private ChatService chatService;

    /** Shared across ordered steps within the same test instance (PER_CLASS lifecycle). */
    private String savedSessionId;
    private UUID savedSessionUuid;

    // Minimal valid JPEG bytes (SOI + EOI markers — enough to pass content-type check)
    private static final byte[] MINIMAL_JPEG = new byte[]{
        (byte) 0xFF, (byte) 0xD8, // SOI
        (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, // APP0 marker + length
        0x4A, 0x46, 0x49, 0x46, 0x00, // "JFIF\0"
        0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, // JFIF header
        (byte) 0xFF, (byte) 0xD9  // EOI
    };

    @BeforeEach
    void resetMocks() {
        // Mockito resets @MockBean stubs between methods automatically;
        // this method is here to allow per-step setup when needed.
    }

    // -------------------------------------------------------------------------
    // Step 1: POST /api/sessions
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    @DisplayName("Step 1: POST /api/sessions returns 200 with sessionId and message")
    void step1_postSession_shouldReturn200WithSessionIdAndMessage() throws Exception {
        // Arrange: create a real session in DB so analysisService can return its ID
        Session session = sessionRepository.save(Session.builder()
                .intent(Intent.RETURN)
                .orderNumber("ORD-001")
                .productName("Test Shirt")
                .description("Produkt uszkodzony")
                .build());
        savedSessionUuid = session.getId();

        // Persist initial USER message (seq 0)
        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(savedSessionUuid)
                .role(Role.USER)
                .content("Produkt uszkodzony")
                .sequenceNumber(0)
                .build());

        // Persist initial ASSISTANT message (seq 1)
        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(savedSessionUuid)
                .role(Role.ASSISTANT)
                .content("Twoje zamówienie prawdopodobnie zostanie zaakceptowane.")
                .sequenceNumber(1)
                .build());

        // Mock AnalysisService to return the session we just created
        String analysisMessage = "Twoje zamówienie prawdopodobnie zostanie zaakceptowane.";
        when(analysisService.analyzeAndCreateSession(
                eq(Intent.RETURN),
                eq("ORD-001"),
                eq("Test Shirt"),
                eq("Produkt uszkodzony"),
                any(byte[].class),
                eq("image/jpeg")
        )).thenReturn(new AnalysisResponse(savedSessionUuid, analysisMessage));

        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "product.jpg", "image/jpeg", MINIMAL_JPEG
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(multipart("/api/sessions")
                        .file(imageFile)
                        .param("intent", "RETURN")
                        .param("orderNumber", "ORD-001")
                        .param("productName", "Test Shirt")
                        .param("description", "Produkt uszkodzony"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sessionId").value(not(emptyString())))
                .andExpect(jsonPath("$.message").value(not(emptyString())))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        savedSessionId = objectMapper.readTree(body).get("sessionId").asText();
        assertThat(savedSessionId).isEqualTo(savedSessionUuid.toString());
    }

    // -------------------------------------------------------------------------
    // Step 2: GET /api/sessions/{id}
    // -------------------------------------------------------------------------

    @Test
    @Order(2)
    @DisplayName("Step 2: GET /api/sessions/{id} returns session data and 2 messages")
    void step2_getSession_shouldReturnSessionWithTwoMessages() throws Exception {
        assertThat(savedSessionId).as("sessionId must be set by step 1").isNotBlank();

        mockMvc.perform(get("/api/sessions/{id}", savedSessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.session.intent").value("RETURN"))
                .andExpect(jsonPath("$.session.orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.session.productName").value("Test Shirt"))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[0].role").value("USER"))
                .andExpect(jsonPath("$.messages[0].sequenceNumber").value(0))
                .andExpect(jsonPath("$.messages[1].role").value("ASSISTANT"))
                .andExpect(jsonPath("$.messages[1].sequenceNumber").value(1));
    }

    // -------------------------------------------------------------------------
    // Step 3: POST /api/sessions/{id}/messages — SSE streaming
    // -------------------------------------------------------------------------

    @Test
    @Order(3)
    @DisplayName("Step 3: POST /api/sessions/{id}/messages returns SSE stream with text-delta event")
    void step3_postMessage_shouldReturnSseStreamWithTextDelta() throws Exception {
        assertThat(savedSessionId).as("sessionId must be set by step 1").isNotBlank();

        // Arrange: mock ChatService to stream a text-delta SSE event and persist messages
        doAnswer(invocation -> {
            SseEmitter emitter = invocation.getArgument(3, SseEmitter.class);
            UUID messageId = UUID.randomUUID();

            // Emit the required SSE events
            emitter.send(SseEmitter.event().data("{\"type\":\"start\",\"messageId\":\"" + messageId + "\"}"));
            emitter.send(SseEmitter.event().data("{\"type\":\"text-start\",\"id\":\"" + messageId + "\"}"));
            emitter.send(SseEmitter.event().data("{\"type\":\"text-delta\",\"id\":\"" + messageId + "\",\"delta\":\"Odpowiem na pytanie.\"}"));
            emitter.send(SseEmitter.event().data("{\"type\":\"text-end\",\"id\":\"" + messageId + "\"}"));

            // Persist USER message (seq 2)
            chatMessageRepository.save(ChatMessage.builder()
                    .sessionId(savedSessionUuid)
                    .role(Role.USER)
                    .content("Kiedy dostanę zwrot?")
                    .sequenceNumber(2)
                    .build());

            // Persist ASSISTANT message (seq 3)
            chatMessageRepository.save(ChatMessage.builder()
                    .sessionId(savedSessionUuid)
                    .role(Role.ASSISTANT)
                    .content("Odpowiem na pytanie.")
                    .sequenceNumber(3)
                    .build());

            emitter.complete();
            return null;
        }).when(chatService).streamResponse(
                any(Session.class),
                anyList(),
                eq("Kiedy dostanę zwrot?"),
                any(SseEmitter.class)
        );

        ChatRequest request = new ChatRequest(
                List.of(new ChatMessageItem("user", new TextNode("Kiedy dostanę zwrot?")))
        );

        // Act: perform the SSE request
        MvcResult asyncResult = mockMvc.perform(post("/api/sessions/{id}/messages", savedSessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("x-vercel-ai-ui-message-stream", "v1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn();

        // Wait for async SseEmitter to complete
        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk());

        // Assert the accumulated SSE body contains a text-delta event
        String sseBody = asyncResult.getResponse().getContentAsString();
        assertThat(sseBody).contains("\"type\":\"text-delta\"");
    }

    // -------------------------------------------------------------------------
    // Step 4: GET /api/sessions/{id} — verify 4 messages
    // -------------------------------------------------------------------------

    @Test
    @Order(4)
    @DisplayName("Step 4: GET /api/sessions/{id} returns 4 messages after chat continuation")
    void step4_getSession_shouldReturnFourMessages() throws Exception {
        assertThat(savedSessionId).as("sessionId must be set by step 1").isNotBlank();

        mockMvc.perform(get("/api/sessions/{id}", savedSessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages.length()").value(4));
    }
}
