package com.sinsay.service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionContentPart;
import com.openai.models.chat.completions.ChatCompletionContentPartImage;
import com.openai.models.chat.completions.ChatCompletionContentPartText;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.sinsay.model.ChatMessage;
import com.sinsay.model.Intent;
import com.sinsay.model.Role;
import com.sinsay.model.Session;
import com.sinsay.repository.ChatMessageRepository;
import com.sinsay.repository.SessionRepository;
import com.sinsay.service.dto.AnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AnalysisService {

    private final OpenAIClient openAIClient;
    private final PolicyDocService policyDocService;
    private final SessionRepository sessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final String model;

    public AnalysisService(
            OpenAIClient openAIClient,
            PolicyDocService policyDocService,
            SessionRepository sessionRepository,
            ChatMessageRepository chatMessageRepository,
            @Qualifier("openaiModel") String model) {
        this.openAIClient = openAIClient;
        this.policyDocService = policyDocService;
        this.sessionRepository = sessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.model = model;
    }

    @Transactional
    public AnalysisResponse analyzeAndCreateSession(
            Intent intent,
            String orderNumber,
            String productName,
            String description,
            byte[] imageBytes,
            String mimeType
    ) {
        log.info("Analyzing session: intent={}, order={}, product={}", intent, orderNumber, productName);

        // Convert image to base64 data URI
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String dataUri = "data:" + mimeType + ";base64," + base64Image;

        // Get system prompt
        String systemPrompt = policyDocService.getSystemPrompt(intent);

        // Build multimodal user message with 2 content parts: image + text
        ChatCompletionContentPart imagePart = ChatCompletionContentPart.ofImageUrl(
                ChatCompletionContentPartImage.builder()
                        .imageUrl(ChatCompletionContentPartImage.ImageUrl.builder()
                                .url(dataUri)
                                .build())
                        .build()
        );
        ChatCompletionContentPart textPart = ChatCompletionContentPart.ofText(
                ChatCompletionContentPartText.builder()
                        .text(description)
                        .build()
        );

        // Build chat completion request with multimodal content
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(model)
                .addSystemMessage(systemPrompt)
                .addUserMessageOfArrayOfContentParts(List.of(imagePart, textPart))
                .build();

        // Call OpenAI API (synchronous, non-streaming)
        ChatCompletion completion = openAIClient.chat().completions().create(params);

        // Extract assistant response
        String assistantMessage = "";
        if (completion != null && !completion.choices().isEmpty()) {
            assistantMessage = completion.choices().get(0).message().content().orElse("");
        }

        // Create and persist session
        Session session = Session.builder()
                .intent(intent)
                .orderNumber(orderNumber)
                .productName(productName)
                .description(description)
                .build();
        session = sessionRepository.save(session);
        UUID sessionId = session.getId();

        // Persist USER message (sequence 0)
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role(Role.USER)
                .content(description)
                .sequenceNumber(0)
                .build();
        chatMessageRepository.save(userMessage);

        // Persist ASSISTANT message (sequence 1)
        ChatMessage assistantMsgEntity = ChatMessage.builder()
                .sessionId(sessionId)
                .role(Role.ASSISTANT)
                .content(assistantMessage)
                .sequenceNumber(1)
                .build();
        chatMessageRepository.save(assistantMsgEntity);

        log.info("Analysis complete: sessionId={}", sessionId);
        return new AnalysisResponse(sessionId, assistantMessage);
    }
}
