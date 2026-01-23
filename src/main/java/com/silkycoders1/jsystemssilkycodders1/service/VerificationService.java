package com.silkycoders1.jsystemssilkycodders1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silkycoders1.jsystemssilkycodders1.controller.ChatRequest;
import com.silkycoders1.jsystemssilkycodders1.controller.MessageDto;
import com.silkycoders1.jsystemssilkycodders1.model.VerificationSession;
import com.silkycoders1.jsystemssilkycodders1.repository.ChatMessageRepository;
import com.silkycoders1.jsystemssilkycodders1.repository.VerificationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final ChatClient.Builder chatClientBuilder;
    private final VerificationSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final ObjectMapper objectMapper;

    public Flux<String> verify(ChatRequest request) {
        ChatClient chatClient = chatClientBuilder.build();

        if (request.messages().size() == 1) { 
            VerificationSession session = new VerificationSession(request.orderId(), request.intent(), request.description());
            sessionRepo.save(session);
        }

        List<Message> messages = new ArrayList<>();
        
        String systemText = getSystemPrompt(request.intent());
        messages.add(new SystemMessage(systemText));

        for (MessageDto msg : request.messages()) {
            if ("user".equalsIgnoreCase(msg.role())) {
                messages.add(convertUserMessage(msg));
            } else if ("assistant".equalsIgnoreCase(msg.role())) {
                messages.add(new AssistantMessage(msg.content().toString()));
            }
        }

        return chatClient.prompt()
                .messages(messages)
                .stream()
                .content()
                .map(this::formatVercelMessage);
    }

    String getSystemPrompt(String intent) {
        String basePrompt = """
            You are a professional Sinsay Brand Assistant helping a customer with a verification process.
            Your goal is to analyze the customer's request and the provided photo evidence to determine if it meets our policy.
            
            CORE INSTRUCTIONS:
            1. Analyze the image carefully (look for defects, wear, tags, damage).
            2. Compare findings against the POLICY below.
            3. FIRST, think step-by-step in ENGLISH inside <thought> tags to form your logic.
            4. THEN, reply to the user exclusively in POLISH (Polski).
            5. Be polite, professional, and empathetic, but firm on the policy.
            
            OUTPUT FORMAT:
            <thought>
            [Analyze the image visuals]
            [Compare with policy]
            [Formulate verdict]
            </thought>
            
            [Your response in Polish]
            """;

        String policy = "";
        if ("RETURN".equalsIgnoreCase(intent)) {
            policy = """
                POLICY: STANDARD RETURN (ZWROT)
                - Timeframe: Must be within 30 days of purchase.
                - Condition: Item must be UNWORN, UNWASHED, and have ORIGINAL TAGS attached.
                - Rejection Criteria: Visible signs of use (wrinkles from wearing, stains, smells), missing tags, mechanical damage caused by user.
                - Acceptance Criteria: Item looks brand new, ready for resale.
                """;
        } else {
            // COMPLAINT / REKLAMACJA
            policy = """
                POLICY: COMPLAINT (REKLAMACJA)
                - Timeframe: 2 years warranty for manufacturing defects.
                - Scope: Covers material failure, seam slippage, discoloration, broken zippers (if not forced).
                - Rejection Criteria: Mechanical damage (cuts, tears from snagging), improper washing (shrinking), normal wear and tear over time.
                - Acceptance Criteria: Clear manufacturing fault visible.
                """;
        }

        return basePrompt + "\n" + policy;
    }

    UserMessage convertUserMessage(MessageDto msg) {
        Object content = msg.content();
        StringBuilder text = new StringBuilder();
        
        if (content instanceof String str) {
            text.append(str);
        } else if (content instanceof List<?> list) {
             for (Object item : list) {
                if (item instanceof Map<?,?> map) {
                    String type = (String) map.get("type");
                    if ("text".equals(type)) {
                        text.append(map.get("text"));
                    }
                }
             }
        }

        List<Media> mediaList = new ArrayList<>();
        if (msg.experimental_attachments() != null) {
            for (Object attachment : msg.experimental_attachments()) {
                if (attachment instanceof Map<?,?> map) {
                    String url = (String) map.get("url"); // Expecting "data:image/png;base64,..."
                    if (url != null && url.startsWith("data:")) {
                        try {
                            String[] parts = url.split(",");
                            String mimeType = parts[0].split(":")[1].split(";")[0];
                            String base64Data = parts[1];
                            byte[] data = Base64.getDecoder().decode(base64Data);
                            
                            mediaList.add(new Media(MimeType.valueOf(mimeType), new ByteArrayResource(data)));
                        } catch (Exception e) {
                            // Log or ignore invalid attachments
                            System.err.println("Failed to process attachment: " + e.getMessage());
                        }
                    }
                }
            }
        }

        return UserMessage.builder().text(text.toString()).media(mediaList).build();
    }

    private String formatVercelMessage(String chunk) {
        if (chunk == null) return "";
        try {
            String jsonEscaped = objectMapper.writeValueAsString(chunk);
            return "0:" + jsonEscaped + "\n";
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}