package com.sinsay.service;

import com.openai.client.OpenAIClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.chat.completions.ChatCompletionChunk;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.sinsay.model.ChatMessage;
import com.sinsay.model.Role;
import com.sinsay.model.Session;
import com.sinsay.repository.ChatMessageRepository;
import com.sinsay.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for handling chat streaming with OpenAI.
 * Streams responses using SSE in Vercel AI SDK v6 UI Message Stream format.
 */
@Service
@Slf4j
public class ChatService {

    private final OpenAIClient openAIClient;
    private final PolicyDocService policyDocService;
    private final ChatMessageRepository chatMessageRepository;
    private final SessionRepository sessionRepository;
    private final String model;

    public ChatService(
            OpenAIClient openAIClient,
            PolicyDocService policyDocService,
            ChatMessageRepository chatMessageRepository,
            SessionRepository sessionRepository,
            @Qualifier("openaiModel") String model) {
        this.openAIClient = openAIClient;
        this.policyDocService = policyDocService;
        this.chatMessageRepository = chatMessageRepository;
        this.sessionRepository = sessionRepository;
        this.model = model;
    }

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Streams a chat response from OpenAI and sends SSE events to the client.
     *
     * @param session      The session entity
     * @param history      Ordered list of previous chat messages
     * @param userContent  The new user message content
     * @param emitter      The SseEmitter to send events to
     */
    public void streamResponse(Session session, List<ChatMessage> history, String userContent, SseEmitter emitter) {
        executorService.submit(() -> {
            try {
                // Generate messageId UUID
                String messageId = UUID.randomUUID().toString();

                // Send start event
                emitter.send(SseEmitter.event().data(SseStreamEncoder.encodeStart(messageId)));

                // Send text-start event
                emitter.send(SseEmitter.event().data(SseStreamEncoder.encodeTextStart(messageId)));

                // Persist new USER message
                int nextSequenceNumber = history.size();
                ChatMessage userMessage = ChatMessage.builder()
                        .sessionId(session.getId())
                        .role(Role.USER)
                        .content(userContent)
                        .sequenceNumber(nextSequenceNumber)
                        .build();
                chatMessageRepository.save(userMessage);

                // Get system prompt from policy docs
                String systemPrompt = policyDocService.getSystemPrompt(session.getIntent());

                // Build ChatCompletionCreateParams
                ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                        .model(model)
                        .addDeveloperMessage(systemPrompt);

                // Add history as USER/ASSISTANT messages
                for (ChatMessage msg : history) {
                    if (msg.getRole() == Role.USER) {
                        paramsBuilder.addUserMessage(msg.getContent());
                    } else if (msg.getRole() == Role.ASSISTANT) {
                        paramsBuilder.addAssistantMessage(msg.getContent());
                    }
                }

                // Add new user message
                paramsBuilder.addUserMessage(userContent);

                ChatCompletionCreateParams params = paramsBuilder.build();

                // Call streaming API
                StringBuilder fullResponse = new StringBuilder();
                try (StreamResponse<ChatCompletionChunk> streamResponse =
                        openAIClient.chat().completions().createStreaming(params)) {

                    streamResponse.stream()
                            .filter(chunk -> !chunk.choices().isEmpty())
                            .forEach(chunk -> {
                                chunk.choices().forEach(choice -> {
                                    choice.delta().content().ifPresent(delta -> {
                                        try {
                                            fullResponse.append(delta);
                                            emitter.send(SseEmitter.event().data(
                                                    SseStreamEncoder.encodeTextDelta(messageId, delta)
                                            ));
                                        } catch (IOException e) {
                                            log.error("Failed to send text-delta event", e);
                                            throw new RuntimeException("Failed to send text-delta event", e);
                                        }
                                    });
                                });
                            });
                }

                // Send text-end event
                emitter.send(SseEmitter.event().data(SseStreamEncoder.encodeTextEnd(messageId)));

                // Persist ASSISTANT message with full content
                ChatMessage assistantMessage = ChatMessage.builder()
                        .sessionId(session.getId())
                        .role(Role.ASSISTANT)
                        .content(fullResponse.toString())
                        .sequenceNumber(nextSequenceNumber + 1)
                        .build();
                chatMessageRepository.save(assistantMessage);

                // Complete the emitter
                emitter.complete();
                log.info("Successfully streamed response for session {}", session.getId());

            } catch (Exception e) {
                log.error("Error streaming response for session {}", session.getId(), e);
                emitter.completeWithError(e);
            }
        });
    }
}
