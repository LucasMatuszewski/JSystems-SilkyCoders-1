package com.silkycoders1.jsystemssilkycodders1.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silkycoders1.jsystemssilkycodders1.model.ChatMessage;
import com.silkycoders1.jsystemssilkycodders1.model.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for the chat endpoint.
 * Implements SSE streaming with Vercel AI SDK Data Stream Protocol.
 * 
 * <p>The Vercel Data Stream Protocol requires text chunks to be formatted as:
 * <ul>
 *   <li>{@code 0:"text_chunk"} - for text deltas</li>
 *   <li>{@code 8:[{...}]} - for data/tool calls (not implemented yet)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    /**
     * Handles chat requests and streams responses in Vercel Data Stream format.
     *
     * @param request The chat request containing messages and context
     * @return A Flux of SSE-formatted strings in Vercel Data Stream Protocol
     */
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {
        log.info("Received chat request with {} messages, intent: {}", 
                request.getMessages() != null ? request.getMessages().size() : 0,
                request.getIntent());

        // Convert frontend messages to Spring AI format
        List<Message> messages = convertToSpringAiMessages(request.getMessages());

        // Stream the response and transform to Vercel format
        return chatClient.prompt()
                .messages(messages)
                .stream()
                .content()
                .map(this::formatVercelTextChunk)
                .onErrorResume(e -> {
                    log.error("Error during chat streaming", e);
                    return Flux.just(formatVercelTextChunk("Error: " + e.getMessage()));
                });
    }

    /**
     * Converts frontend ChatMessage objects to Spring AI Message objects.
     *
     * @param chatMessages List of frontend chat messages
     * @return List of Spring AI Message objects
     */
    private List<Message> convertToSpringAiMessages(List<ChatMessage> chatMessages) {
        if (chatMessages == null || chatMessages.isEmpty()) {
            return new ArrayList<>();
        }

        List<Message> messages = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            Message message = switch (chatMessage.getRole().toLowerCase()) {
                case "system" -> new SystemMessage(chatMessage.getContent());
                case "assistant" -> new AssistantMessage(chatMessage.getContent());
                case "user" -> new UserMessage(chatMessage.getContent());
                default -> new UserMessage(chatMessage.getContent());
            };
            messages.add(message);
        }
        return messages;
    }

    /**
     * Formats a text chunk according to the Vercel AI SDK Data Stream Protocol.
     * 
     * <p>The format is: {@code 0:"escaped_json_string"\n}
     * 
     * <p>The {@code 0:} prefix indicates this is a text delta chunk.
     * The content must be a valid JSON-encoded string (with proper escaping).
     *
     * @param chunk The text chunk to format
     * @return Formatted string for SSE transmission
     */
    String formatVercelTextChunk(String chunk) {
        if (chunk == null) {
            return "";
        }
        try {
            // ObjectMapper.writeValueAsString properly escapes the string as JSON
            // This handles newlines, quotes, unicode, etc.
            String jsonEscaped = objectMapper.writeValueAsString(chunk);
            return "0:" + jsonEscaped + "\n";
        } catch (JsonProcessingException e) {
            log.error("Failed to format chunk as JSON", e);
            return "";
        }
    }
}
