package com.silkycoders1.jsystemssilkycodders1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silkycoders1.jsystemssilkycodders1.model.ChatMessage;
import com.silkycoders1.jsystemssilkycodders1.model.ChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.ChatClient.StreamResponseSpec;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ChatController.
 * Tests the Vercel Data Stream Protocol formatting and message conversion.
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTests {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.PromptUserSpec promptUserSpec;

    @Mock
    private StreamResponseSpec streamResponseSpec;

    private ChatController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new ChatController(chatClient, objectMapper);
    }

    @Nested
    @DisplayName("Vercel Format Tests")
    class VercelFormatTests {

        @Test
        @DisplayName("Should format simple text chunk correctly")
        void shouldFormatSimpleTextChunk() {
            String result = controller.formatVercelTextChunk("Hello");
            
            assertThat(result).isEqualTo("0:\"Hello\"\n");
        }

        @Test
        @DisplayName("Should escape quotes in text chunks")
        void shouldEscapeQuotes() {
            String result = controller.formatVercelTextChunk("He said \"hello\"");
            
            assertThat(result).isEqualTo("0:\"He said \\\"hello\\\"\"\n");
        }

        @Test
        @DisplayName("Should escape newlines in text chunks")
        void shouldEscapeNewlines() {
            String result = controller.formatVercelTextChunk("Line1\nLine2");
            
            assertThat(result).isEqualTo("0:\"Line1\\nLine2\"\n");
        }

        @Test
        @DisplayName("Should escape tabs in text chunks")
        void shouldEscapeTabs() {
            String result = controller.formatVercelTextChunk("Col1\tCol2");
            
            assertThat(result).isEqualTo("0:\"Col1\\tCol2\"\n");
        }

        @Test
        @DisplayName("Should escape backslashes in text chunks")
        void shouldEscapeBackslashes() {
            String result = controller.formatVercelTextChunk("path\\to\\file");
            
            assertThat(result).isEqualTo("0:\"path\\\\to\\\\file\"\n");
        }

        @Test
        @DisplayName("Should handle Polish characters correctly")
        void shouldHandlePolishCharacters() {
            String result = controller.formatVercelTextChunk("Zwrot towaru możliwy");
            
            assertThat(result).isEqualTo("0:\"Zwrot towaru możliwy\"\n");
        }

        @Test
        @DisplayName("Should handle null chunk by returning empty string")
        void shouldHandleNullChunk() {
            String result = controller.formatVercelTextChunk(null);
            
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty string")
        void shouldHandleEmptyString() {
            String result = controller.formatVercelTextChunk("");
            
            assertThat(result).isEqualTo("0:\"\"\n");
        }

        @Test
        @DisplayName("Should handle unicode emoji")
        void shouldHandleEmoji() {
            String result = controller.formatVercelTextChunk("Status: ✅");
            
            assertThat(result).isEqualTo("0:\"Status: ✅\"\n");
        }
    }

    @Nested
    @DisplayName("Chat Streaming Tests")
    class ChatStreamingTests {

        @Test
        @DisplayName("Should stream chat responses in Vercel format")
        void shouldStreamChatResponses() {
            // Given
            ChatRequest request = ChatRequest.builder()
                    .messages(List.of(
                            ChatMessage.builder()
                                    .role("user")
                                    .content("Hello")
                                    .build()
                    ))
                    .intent("return")
                    .build();

            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.messages(anyList())).thenReturn(requestSpec);
            when(requestSpec.stream()).thenReturn(streamResponseSpec);
            when(streamResponseSpec.content()).thenReturn(Flux.just("Hello", " World", "!"));

            // When
            Flux<String> result = controller.chat(request);

            // Then
            StepVerifier.create(result)
                    .expectNext("0:\"Hello\"\n")
                    .expectNext("0:\" World\"\n")
                    .expectNext("0:\"!\"\n")
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle empty message list")
        void shouldHandleEmptyMessageList() {
            // Given
            ChatRequest request = ChatRequest.builder()
                    .messages(List.of())
                    .intent("return")
                    .build();

            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.messages(anyList())).thenReturn(requestSpec);
            when(requestSpec.stream()).thenReturn(streamResponseSpec);
            when(streamResponseSpec.content()).thenReturn(Flux.just("Response"));

            // When
            Flux<String> result = controller.chat(request);

            // Then
            StepVerifier.create(result)
                    .expectNext("0:\"Response\"\n")
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle null message list")
        void shouldHandleNullMessageList() {
            // Given
            ChatRequest request = ChatRequest.builder()
                    .messages(null)
                    .intent("return")
                    .build();

            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.messages(anyList())).thenReturn(requestSpec);
            when(requestSpec.stream()).thenReturn(streamResponseSpec);
            when(streamResponseSpec.content()).thenReturn(Flux.just("Response"));

            // When
            Flux<String> result = controller.chat(request);

            // Then
            StepVerifier.create(result)
                    .expectNext("0:\"Response\"\n")
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle streaming error gracefully")
        void shouldHandleStreamingError() {
            // Given
            ChatRequest request = ChatRequest.builder()
                    .messages(List.of(
                            ChatMessage.builder()
                                    .role("user")
                                    .content("Hello")
                                    .build()
                    ))
                    .build();

            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.messages(anyList())).thenReturn(requestSpec);
            when(requestSpec.stream()).thenReturn(streamResponseSpec);
            when(streamResponseSpec.content()).thenReturn(
                    Flux.error(new RuntimeException("API Error"))
            );

            // When
            Flux<String> result = controller.chat(request);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(chunk -> chunk.startsWith("0:\"Error:"))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Message Conversion Tests")
    class MessageConversionTests {

        @Test
        @DisplayName("Should convert user messages correctly")
        void shouldConvertUserMessages() {
            // Given
            ChatRequest request = ChatRequest.builder()
                    .messages(List.of(
                            ChatMessage.builder()
                                    .role("user")
                                    .content("Test message")
                                    .build()
                    ))
                    .build();

            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.messages(anyList())).thenReturn(requestSpec);
            when(requestSpec.stream()).thenReturn(streamResponseSpec);
            when(streamResponseSpec.content()).thenReturn(Flux.just("OK"));

            // When
            Flux<String> result = controller.chat(request);

            // Then
            StepVerifier.create(result)
                    .expectNext("0:\"OK\"\n")
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle mixed role messages")
        void shouldHandleMixedRoleMessages() {
            // Given
            ChatRequest request = ChatRequest.builder()
                    .messages(List.of(
                            ChatMessage.builder()
                                    .role("system")
                                    .content("You are a helpful assistant")
                                    .build(),
                            ChatMessage.builder()
                                    .role("user")
                                    .content("Hello")
                                    .build(),
                            ChatMessage.builder()
                                    .role("assistant")
                                    .content("Hi there!")
                                    .build(),
                            ChatMessage.builder()
                                    .role("user")
                                    .content("How are you?")
                                    .build()
                    ))
                    .build();

            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.messages(anyList())).thenReturn(requestSpec);
            when(requestSpec.stream()).thenReturn(streamResponseSpec);
            when(streamResponseSpec.content()).thenReturn(Flux.just("I'm doing great!"));

            // When
            Flux<String> result = controller.chat(request);

            // Then
            StepVerifier.create(result)
                    .expectNext("0:\"I'm doing great!\"\n")
                    .verifyComplete();
        }
    }
}
