package com.sinsay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for encoding SSE events in the Vercel AI SDK v6 UI Message Stream format.
 * Each method returns a JSON string that can be sent via SseEmitter.send(SseEmitter.event().data(jsonString)).
 */
public class SseStreamEncoder {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private SseStreamEncoder() {
        // Utility class - prevent instantiation
    }

    /**
     * Encodes the start event that marks the beginning of a message stream.
     *
     * @param messageId The UUID identifying this message
     * @return JSON string: {"type":"start","messageId":"..."}
     */
    public static String encodeStart(String messageId) {
        try {
            return objectMapper.writeValueAsString(new EventStart(messageId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to encode start event", e);
        }
    }

    /**
     * Encodes the text-start event that marks the beginning of text content.
     *
     * @param messageId The UUID identifying this message (same as in start event)
     * @return JSON string: {"type":"text-start","id":"..."}
     */
    public static String encodeTextStart(String messageId) {
        try {
            return objectMapper.writeValueAsString(new EventTextStart(messageId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to encode text-start event", e);
        }
    }

    /**
     * Encodes a text-delta event containing a chunk of text content.
     *
     * @param messageId The UUID identifying this message (same as in start event)
     * @param delta     The text chunk to send
     * @return JSON string: {"type":"text-delta","id":"...","delta":"..."}
     */
    public static String encodeTextDelta(String messageId, String delta) {
        try {
            return objectMapper.writeValueAsString(new EventTextDelta(messageId, delta));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to encode text-delta event", e);
        }
    }

    /**
     * Encodes the text-end event that marks the end of text content.
     *
     * @param messageId The UUID identifying this message (same as in start event)
     * @return JSON string: {"type":"text-end","id":"..."}
     */
    public static String encodeTextEnd(String messageId) {
        try {
            return objectMapper.writeValueAsString(new EventTextEnd(messageId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to encode text-end event", e);
        }
    }

    // Internal record classes for JSON serialization

    private record EventStart(String type, String messageId) {
        EventStart(String messageId) {
            this("start", messageId);
        }
    }

    private record EventTextStart(String type, String id) {
        EventTextStart(String id) {
            this("text-start", id);
        }
    }

    private record EventTextDelta(String type, String id, String delta) {
        EventTextDelta(String id, String delta) {
            this("text-delta", id, delta);
        }
    }

    private record EventTextEnd(String type, String id) {
        EventTextEnd(String id) {
            this("text-end", id);
        }
    }
}
