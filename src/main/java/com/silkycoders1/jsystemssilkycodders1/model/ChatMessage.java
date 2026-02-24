package com.silkycoders1.jsystemssilkycodders1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single message in the chat conversation.
 * Compatible with Vercel AI SDK message format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    
    /**
     * Role of the message sender: "user", "assistant", or "system"
     */
    private String role;
    
    /**
     * Text content of the message
     */
    private String content;
}
