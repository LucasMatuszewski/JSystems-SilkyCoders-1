package com.sinsay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SseStreamEncoder")
class SseStreamEncoderTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("encodeStart method")
    class EncodeStartTests {

        @Test
        @DisplayName("should produce valid JSON with type=start and messageId")
        void encodeStart_producesCorrectJson() throws JsonProcessingException {
            // Arrange
            String messageId = "550e8400-e29b-41d4-a716-446655440000";

            // Act
            String result = SseStreamEncoder.encodeStart(messageId);

            // Assert
            JsonNode json = objectMapper.readTree(result);
            assertEquals("start", json.get("type").asText());
            assertEquals(messageId, json.get("messageId").asText());
            assertEquals(2, json.size()); // Only type and messageId
        }

        @Test
        @DisplayName("should escape special characters in messageId")
        void encodeStart_escapesSpecialCharacters() throws JsonProcessingException {
            // Arrange
            String messageId = "test\"quotes\"and\\slashes";

            // Act
            String result = SseStreamEncoder.encodeStart(messageId);

            // Assert - should be valid JSON
            JsonNode json = objectMapper.readTree(result);
            assertEquals(messageId, json.get("messageId").asText());
        }
    }

    @Nested
    @DisplayName("encodeTextStart method")
    class EncodeTextStartTests {

        @Test
        @DisplayName("should produce valid JSON with type=text-start and id")
        void encodeTextStart_producesCorrectJson() throws JsonProcessingException {
            // Arrange
            String messageId = "550e8400-e29b-41d4-a716-446655440000";

            // Act
            String result = SseStreamEncoder.encodeTextStart(messageId);

            // Assert
            JsonNode json = objectMapper.readTree(result);
            assertEquals("text-start", json.get("type").asText());
            assertEquals(messageId, json.get("id").asText());
            assertEquals(2, json.size()); // Only type and id
        }

        @Test
        @DisplayName("should escape special characters in messageId")
        void encodeTextStart_escapesSpecialCharacters() throws JsonProcessingException {
            // Arrange
            String messageId = "test\"quotes\"and\\slashes";

            // Act
            String result = SseStreamEncoder.encodeTextStart(messageId);

            // Assert
            JsonNode json = objectMapper.readTree(result);
            assertEquals(messageId, json.get("id").asText());
        }
    }

    @Nested
    @DisplayName("encodeTextDelta method")
    class EncodeTextDeltaTests {

        @Test
        @DisplayName("should produce valid JSON with type=text-delta, id, and delta")
        void encodeTextDelta_producesCorrectJson() throws JsonProcessingException {
            // Arrange
            String messageId = "550e8400-e29b-41d4-a716-446655440000";
            String delta = "Hello";

            // Act
            String result = SseStreamEncoder.encodeTextDelta(messageId, delta);

            // Assert
            JsonNode json = objectMapper.readTree(result);
            assertEquals("text-delta", json.get("type").asText());
            assertEquals(messageId, json.get("id").asText());
            assertEquals(delta, json.get("delta").asText());
            assertEquals(3, json.size()); // type, id, and delta
        }

        @Test
        @DisplayName("should escape special characters in delta")
        void encodeTextDelta_escapesSpecialCharacters() throws JsonProcessingException {
            // Arrange
            String messageId = "550e8400-e29b-41d4-a716-446655440000";
            String delta = "Text with \"quotes\" and\nnewlines\ttabs";

            // Act
            String result = SseStreamEncoder.encodeTextDelta(messageId, delta);

            // Assert
            JsonNode json = objectMapper.readTree(result);
            assertEquals(delta, json.get("delta").asText());
        }

        @Test
        @DisplayName("should handle empty delta")
        void encodeTextDelta_handlesEmptyDelta() throws JsonProcessingException {
            // Arrange
            String messageId = "550e8400-e29b-41d4-a716-446655440000";
            String delta = "";

            // Act
            String result = SseStreamEncoder.encodeTextDelta(messageId, delta);

            // Assert
            JsonNode json = objectMapper.readTree(result);
            assertEquals("", json.get("delta").asText());
        }

        @Test
        @DisplayName("should handle Polish characters")
        void encodeTextDelta_handlesPolishCharacters() throws JsonProcessingException {
            // Arrange
            String messageId = "550e8400-e29b-41d4-a716-446655440000";
            String delta = "Prawdopodobnie zaakceptowane - reklamacja zwrot";

            // Act
            String result = SseStreamEncoder.encodeTextDelta(messageId, delta);

            // Assert
            JsonNode json = objectMapper.readTree(result);
            assertEquals(delta, json.get("delta").asText());
        }
    }

    @Nested
    @DisplayName("encodeTextEnd method")
    class EncodeTextEndTests {

        @Test
        @DisplayName("should produce valid JSON with type=text-end and id")
        void encodeTextEnd_producesCorrectJson() throws JsonProcessingException {
            // Arrange
            String messageId = "550e8400-e29b-41d4-a716-446655440000";

            // Act
            String result = SseStreamEncoder.encodeTextEnd(messageId);

            // Assert
            JsonNode json = objectMapper.readTree(result);
            assertEquals("text-end", json.get("type").asText());
            assertEquals(messageId, json.get("id").asText());
            assertEquals(2, json.size()); // Only type and id
        }

        @Test
        @DisplayName("should escape special characters in messageId")
        void encodeTextEnd_escapesSpecialCharacters() throws JsonProcessingException {
            // Arrange
            String messageId = "test\"quotes\"and\\slashes";

            // Act
            String result = SseStreamEncoder.encodeTextEnd(messageId);

            // Assert
            JsonNode json = objectMapper.readTree(result);
            assertEquals(messageId, json.get("id").asText());
        }
    }

    @Nested
    @DisplayName("All methods return valid JSON")
    class ValidJsonTests {

        @Test
        @DisplayName("encodeStart should return valid JSON")
        void encodeStart_returnsValidJson() {
            // Arrange
            String messageId = "550e8400-e29b-41d4-a716-446655440000";

            // Act
            String result = SseStreamEncoder.encodeStart(messageId);

            // Assert - should not throw
            assertDoesNotThrow(() -> objectMapper.readTree(result));
        }

        @Test
        @DisplayName("encodeTextStart should return valid JSON")
        void encodeTextStart_returnsValidJson() {
            // Arrange
            String messageId = "550e8400-e29b-41d4-a716-446655440000";

            // Act
            String result = SseStreamEncoder.encodeTextStart(messageId);

            // Assert
            assertDoesNotThrow(() -> objectMapper.readTree(result));
        }

        @Test
        @DisplayName("encodeTextDelta should return valid JSON")
        void encodeTextDelta_returnsValidJson() {
            // Arrange
            String messageId = "550e8400-e29b-41d4-a716-446655440000";
            String delta = "Hello world";

            // Act
            String result = SseStreamEncoder.encodeTextDelta(messageId, delta);

            // Assert
            assertDoesNotThrow(() -> objectMapper.readTree(result));
        }

        @Test
        @DisplayName("encodeTextEnd should return valid JSON")
        void encodeTextEnd_returnsValidJson() {
            // Arrange
            String messageId = "550e8400-e29b-41d4-a716-446655440000";

            // Act
            String result = SseStreamEncoder.encodeTextEnd(messageId);

            // Assert
            assertDoesNotThrow(() -> objectMapper.readTree(result));
        }
    }
}
