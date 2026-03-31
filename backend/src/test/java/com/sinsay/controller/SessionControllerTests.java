package com.sinsay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinsay.model.ChatMessage;
import com.sinsay.model.Intent;
import com.sinsay.model.Role;
import com.sinsay.model.Session;
import com.sinsay.repository.ChatMessageRepository;
import com.sinsay.repository.SessionRepository;
import com.sinsay.service.AnalysisService;
import com.sinsay.service.dto.AnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("SessionController Tests")
class SessionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnalysisService analysisService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private UUID testSessionId;
    private Session testSession;

    @BeforeEach
    @Transactional
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
    @DisplayName("POST /api/sessions - Form Submission")
    class PostSessionsTests {

        @Test
        @DisplayName("TAC-01: POST valid multipart should return 200 with sessionId and message")
        void testPostValidMultipartFile_shouldReturn200WithSessionIdAndMessage() throws Exception {
            // Arrange
            UUID expectedSessionId = UUID.randomUUID();
            String expectedMessage = "Analysis result: Product likely accepted for return.";
            AnalysisResponse mockResponse = new AnalysisResponse(expectedSessionId, expectedMessage);

            when(analysisService.analyzeAndCreateSession(
                    eq(Intent.RETURN),
                    eq("ORD-123"),
                    eq("Test T-shirt"),
                    eq("The product has a stain"),
                    any(byte[].class),
                    eq("image/jpeg")
            )).thenReturn(mockResponse);

            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "product.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            // Act & Assert
            mockMvc.perform(multipart("/api/sessions")
                            .file(imageFile)
                            .param("intent", "RETURN")
                            .param("orderNumber", "ORD-123")
                            .param("productName", "Test T-shirt")
                            .param("description", "The product has a stain"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.sessionId").value(expectedSessionId.toString()))
                    .andExpect(jsonPath("$.message").value(expectedMessage));

            // Verify service was called
            verify(analysisService).analyzeAndCreateSession(
                    eq(Intent.RETURN),
                    eq("ORD-123"),
                    eq("Test T-shirt"),
                    eq("The product has a stain"),
                    any(byte[].class),
                    eq("image/jpeg")
            );
        }

        @Test
        @DisplayName("TAC-02: POST missing intent should return 400")
        void testPostMissingIntent_shouldReturn400() throws Exception {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "product.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            mockMvc.perform(multipart("/api/sessions")
                            .file(imageFile)
                            .param("orderNumber", "ORD-123")
                            .param("productName", "Test T-shirt")
                            .param("description", "The product has a stain"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TAC-03: POST invalid intent should return 400")
        void testPostInvalidIntent_shouldReturn400() throws Exception {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "product.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            mockMvc.perform(multipart("/api/sessions")
                            .file(imageFile)
                            .param("intent", "INVALID_INTENT")
                            .param("orderNumber", "ORD-123")
                            .param("productName", "Test T-shirt")
                            .param("description", "The product has a stain"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TAC-04: POST missing image should return 400")
        void testPostMissingImage_shouldReturn400() throws Exception {
            mockMvc.perform(multipart("/api/sessions")
                            .param("intent", "RETURN")
                            .param("orderNumber", "ORD-123")
                            .param("productName", "Test T-shirt")
                            .param("description", "The product has a stain"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TAC-05: POST empty image should return 400")
        void testPostEmptyImage_shouldReturn400() throws Exception {
            MockMultipartFile emptyImageFile = new MockMultipartFile(
                    "image",
                    "product.jpg",
                    "image/jpeg",
                    new byte[0]
            );

            mockMvc.perform(multipart("/api/sessions")
                            .file(emptyImageFile)
                            .param("intent", "RETURN")
                            .param("orderNumber", "ORD-123")
                            .param("productName", "Test T-shirt")
                            .param("description", "The product has a stain"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TAC-06: POST image > 10MB should return 400")
        void testPostImageTooLarge_shouldReturn400() throws Exception {
            // Create image larger than 10MB (10MB + 1 byte)
            byte[] largeImage = new byte[10 * 1024 * 1024 + 1];

            MockMultipartFile largeImageFile = new MockMultipartFile(
                    "image",
                    "product.jpg",
                    "image/jpeg",
                    largeImage
            );

            mockMvc.perform(multipart("/api/sessions")
                            .file(largeImageFile)
                            .param("intent", "RETURN")
                            .param("orderNumber", "ORD-123")
                            .param("productName", "Test T-shirt")
                            .param("description", "The product has a stain"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TAC-07: POST image with invalid MIME type should return 400 - PDF")
        void testPostInvalidMimeTypePdf_shouldReturn400() throws Exception {
            MockMultipartFile pdfFile = new MockMultipartFile(
                    "image",
                    "document.pdf",
                    "application/pdf",
                    "test pdf content".getBytes()
            );

            mockMvc.perform(multipart("/api/sessions")
                            .file(pdfFile)
                            .param("intent", "RETURN")
                            .param("orderNumber", "ORD-123")
                            .param("productName", "Test T-shirt")
                            .param("description", "The product has a stain"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TAC-08: POST image with invalid MIME type should return 400 - plain text")
        void testPostInvalidMimeTypeText_shouldReturn400() throws Exception {
            MockMultipartFile textFile = new MockMultipartFile(
                    "image",
                    "document.txt",
                    "text/plain",
                    "test text content".getBytes()
            );

            mockMvc.perform(multipart("/api/sessions")
                            .file(textFile)
                            .param("intent", "RETURN")
                            .param("orderNumber", "ORD-123")
                            .param("productName", "Test T-shirt")
                            .param("description", "The product has a stain"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Valid MIME types: jpeg, png, webp, gif should be accepted")
        void testPostValidMimeTypes_shouldReturn200() throws Exception {
            String[] validMimeTypes = {"image/jpeg", "image/png", "image/webp", "image/gif"};

            for (String mimeType : validMimeTypes) {
                MockMultipartFile imageFile = new MockMultipartFile(
                        "image",
                        "product." + mimeType.split("/")[1],
                        mimeType,
                        "test image content".getBytes()
                );

                UUID expectedSessionId = UUID.randomUUID();
                AnalysisResponse mockResponse = new AnalysisResponse(expectedSessionId, "Response");

                when(analysisService.analyzeAndCreateSession(
                        any(Intent.class),
                        anyString(),
                        anyString(),
                        anyString(),
                        any(byte[].class),
                        eq(mimeType)
                )).thenReturn(mockResponse);

                mockMvc.perform(multipart("/api/sessions")
                                .file(imageFile)
                                .param("intent", "RETURN")
                                .param("orderNumber", "ORD-123")
                                .param("productName", "Test T-shirt")
                                .param("description", "The product has a stain"))
                        .andExpect(status().isOk());
            }
        }

        @Test
        @DisplayName("POST with missing orderNumber should return 400")
        void testPostMissingOrderNumber_shouldReturn400() throws Exception {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "product.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            mockMvc.perform(multipart("/api/sessions")
                            .file(imageFile)
                            .param("intent", "RETURN")
                            .param("productName", "Test T-shirt")
                            .param("description", "The product has a stain"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST with missing productName should return 400")
        void testPostMissingProductName_shouldReturn400() throws Exception {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "product.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            mockMvc.perform(multipart("/api/sessions")
                            .file(imageFile)
                            .param("intent", "RETURN")
                            .param("orderNumber", "ORD-123")
                            .param("description", "The product has a stain"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST with missing description should return 400")
        void testPostMissingDescription_shouldReturn400() throws Exception {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "product.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            mockMvc.perform(multipart("/api/sessions")
                            .file(imageFile)
                            .param("intent", "RETURN")
                            .param("orderNumber", "ORD-123")
                            .param("productName", "Test T-shirt"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST with COMPLAINT intent should return 200")
        void testPostComplaintIntent_shouldReturn200() throws Exception {
            UUID expectedSessionId = UUID.randomUUID();
            AnalysisResponse mockResponse = new AnalysisResponse(expectedSessionId, "Complaint analysis");

            when(analysisService.analyzeAndCreateSession(
                    eq(Intent.COMPLAINT),
                    anyString(),
                    anyString(),
                    anyString(),
                    any(byte[].class),
                    eq("image/jpeg")
            )).thenReturn(mockResponse);

            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "product.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            mockMvc.perform(multipart("/api/sessions")
                            .file(imageFile)
                            .param("intent", "COMPLAINT")
                            .param("orderNumber", "ORD-456")
                            .param("productName", "Defective Product")
                            .param("description", "Product arrived damaged"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value(expectedSessionId.toString()));
        }
    }

    @Nested
    @DisplayName("GET /api/sessions/{id} - Load Session")
    class GetSessionTests {

        @Test
        @DisplayName("TAC-09: GET valid session ID should return 200 with session and messages")
        void testGetValidSessionId_shouldReturn200WithSessionAndMessages() throws Exception {
            // Arrange - Create test messages
            ChatMessage userMessage = ChatMessage.builder()
                    .sessionId(testSessionId)
                    .role(Role.USER)
                    .content("User message")
                    .sequenceNumber(0)
                    .build();
            chatMessageRepository.save(userMessage);

            ChatMessage assistantMessage = ChatMessage.builder()
                    .sessionId(testSessionId)
                    .role(Role.ASSISTANT)
                    .content("Assistant response")
                    .sequenceNumber(1)
                    .build();
            chatMessageRepository.save(assistantMessage);

            // Act & Assert
            mockMvc.perform(get("/api/sessions/{id}", testSessionId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.session.id").value(testSessionId.toString()))
                    .andExpect(jsonPath("$.session.intent").value("RETURN"))
                    .andExpect(jsonPath("$.session.orderNumber").value("ORD-123"))
                    .andExpect(jsonPath("$.session.productName").value("Test Product"))
                    .andExpect(jsonPath("$.session.description").value("Test description"))
                    .andExpect(jsonPath("$.session.createdAt").exists())
                    .andExpect(jsonPath("$.messages").isArray())
                    .andExpect(jsonPath("$.messages.length()").value(2))
                    .andExpect(jsonPath("$.messages[0].role").value("USER"))
                    .andExpect(jsonPath("$.messages[0].content").value("User message"))
                    .andExpect(jsonPath("$.messages[0].sequenceNumber").value(0))
                    .andExpect(jsonPath("$.messages[1].role").value("ASSISTANT"))
                    .andExpect(jsonPath("$.messages[1].content").value("Assistant response"))
                    .andExpect(jsonPath("$.messages[1].sequenceNumber").value(1));
        }

        @Test
        @DisplayName("TAC-10: GET unknown session ID should return 404")
        void testGetUnknownSessionId_shouldReturn404() throws Exception {
            UUID unknownId = UUID.randomUUID();

            mockMvc.perform(get("/api/sessions/{id}", unknownId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET session with no messages should return empty messages array")
        void testGetSessionWithNoMessages_shouldReturnEmptyMessagesArray() throws Exception {
            mockMvc.perform(get("/api/sessions/{id}", testSessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.messages").isArray())
                    .andExpect(jsonPath("$.messages.length()").value(0));
        }

        @Test
        @DisplayName("GET session messages should be ordered by sequenceNumber")
        void testGetSessionMessages_shouldBeOrderedBySequenceNumber() throws Exception {
            // Create messages out of order
            ChatMessage msg2 = ChatMessage.builder()
                    .sessionId(testSessionId)
                    .role(Role.ASSISTANT)
                    .content("Second")
                    .sequenceNumber(2)
                    .build();
            chatMessageRepository.save(msg2);

            ChatMessage msg0 = ChatMessage.builder()
                    .sessionId(testSessionId)
                    .role(Role.USER)
                    .content("First")
                    .sequenceNumber(0)
                    .build();
            chatMessageRepository.save(msg0);

            ChatMessage msg1 = ChatMessage.builder()
                    .sessionId(testSessionId)
                    .role(Role.ASSISTANT)
                    .content("Middle")
                    .sequenceNumber(1)
                    .build();
            chatMessageRepository.save(msg1);

            mockMvc.perform(get("/api/sessions/{id}", testSessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.messages[0].sequenceNumber").value(0))
                    .andExpect(jsonPath("$.messages[1].sequenceNumber").value(1))
                    .andExpect(jsonPath("$.messages[2].sequenceNumber").value(2));
        }
    }
}
