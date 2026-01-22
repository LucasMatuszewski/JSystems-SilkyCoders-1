package com.silkycoders1.jsystemssilkycodders1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload for the chat endpoint.
 * Compatible with Vercel AI SDK useChat hook format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {
    
    /**
     * List of conversation messages
     */
    private List<ChatMessage> messages;
    
    /**
     * Intent type: "return" (zwrot) or "complaint" (reklamacja)
     */
    private String intent;
    
    /**
     * Order ID for verification context
     */
    private String orderId;
}
