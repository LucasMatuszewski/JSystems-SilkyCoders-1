package com.sinsay.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Request DTO for chat message endpoint.
 * Matches the format sent by AssistantChatTransport from assistant-ui.
 * The Vercel AI SDK sends messages with content as either a string or an array of parts.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatRequest(List<ChatMessageItem> messages) {

    /**
     * Represents a single message in the messages array.
     * Content can be a string (simple text) or a JsonNode (array of parts).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatMessageItem(
            String role,
            JsonNode content  // Accept both string and array of parts
    ) {
        /**
         * Extract text content from either a string or an array of parts.
         * Format 1: "plain text"
         * Format 2: [{"type": "text", "text": "..."}]
         */
        public String getTextContent() {
            if (content == null) {
                return "";
            }
            if (content.isTextual()) {
                return content.asText();
            }
            // Handle array of parts format
            if (content.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode part : content) {
                    JsonNode textNode = part.get("text");
                    if (textNode != null && textNode.isTextual()) {
                        sb.append(textNode.asText());
                    }
                }
                return sb.toString();
            }
            return "";
        }
    }
}
