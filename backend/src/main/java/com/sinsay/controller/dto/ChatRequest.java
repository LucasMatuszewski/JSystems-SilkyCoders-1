package com.sinsay.controller.dto;

import java.util.List;

/**
 * Request DTO for chat message endpoint.
 * Matches the format sent by AssistantChatTransport from assistant-ui.
 *
 * @param messages Array of message objects with role and content
 */
public record ChatRequest(List<ChatMessageItem> messages) {

    /**
     * Represents a single message in the messages array.
     */
    public record ChatMessageItem(String role, String content) {
    }
}
