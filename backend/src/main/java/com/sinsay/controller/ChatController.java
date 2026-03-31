package com.sinsay.controller;

import com.sinsay.controller.dto.ChatRequest;
import com.sinsay.model.ChatMessage;
import com.sinsay.model.Session;
import com.sinsay.repository.ChatMessageRepository;
import com.sinsay.repository.SessionRepository;
import com.sinsay.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for chat message streaming.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SessionRepository sessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * Stream a chat response for a session.
     * Accepts a request body from AssistantChatTransport and returns SSE events.
     *
     * @param id      The session ID
     * @param request The chat request with messages array
     * @return SseEmitter that streams the response
     */
    @PostMapping("/{id}/messages")
    public ResponseEntity<SseEmitter> chat(
            @PathVariable UUID id,
            @RequestBody ChatRequest request
    ) {
        log.info("Chat request for session: {}", id);

        // Validate request
        if (request.messages() == null || request.messages().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Extract the last user message from the messages array
        final String lastUserMessage = extractLastUserMessage(request.messages());
        if (lastUserMessage == null) {
            return ResponseEntity.badRequest().build();
        }

        // Load session
        return sessionRepository.findById(id)
                .map(session -> {
                    // Load message history from DB
                    List<ChatMessage> history = chatMessageRepository
                            .findBySessionIdOrderBySequenceNumberAsc(id);

                    // Create SSE emitter
                    SseEmitter emitter = new SseEmitter(60000L); // 60 second timeout

                    // Call ChatService to stream response (async)
                    chatService.streamResponse(session, history, lastUserMessage, emitter);

                    // Return emitter immediately
                    return ResponseEntity.ok()
                            .contentType(MediaType.TEXT_EVENT_STREAM)
                            .header("x-vercel-ai-ui-message-stream", "v1")
                            .body(emitter);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Extract the last user message from the messages array.
     *
     * @param messages List of chat message items
     * @return The content of the last user message, or null if none found
     */
    private String extractLastUserMessage(List<ChatRequest.ChatMessageItem> messages) {
        // Iterate in reverse to find the last user message
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatRequest.ChatMessageItem item = messages.get(i);
            if ("user".equalsIgnoreCase(item.role())) {
                return item.getTextContent();
            }
        }
        return null;
    }
}
