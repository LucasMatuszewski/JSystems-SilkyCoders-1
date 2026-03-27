package com.silkycoders1.jsystemssilkycodders1.controller;

import com.silkycoders1.jsystemssilkycodders1.controller.dto.ChatMessage;
import com.silkycoders1.jsystemssilkycodders1.controller.dto.ChatRequest;
import com.silkycoders1.jsystemssilkycodders1.controller.dto.ImagePayload;
import com.silkycoders1.jsystemssilkycodders1.service.VercelStreamFormatter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.media.Media;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final String RETURN_INTENT = "return";
    private static final String COMPLAINT_INTENT = "complaint";

    private final ChatClient chatClient;
    private final VercelStreamFormatter streamFormatter;

    public ChatController(ChatClient chatClient, VercelStreamFormatter streamFormatter) {
        this.chatClient = chatClient;
        this.streamFormatter = streamFormatter;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(buildSystemPrompt(request)));
        messages.addAll(convertMessages(request.messages()));

        return chatClient.prompt()
                .messages(messages)
                .stream()
                .content()
                .map(streamFormatter::formatTextChunk);
    }

    private String buildSystemPrompt(ChatRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Jesteś asystentem weryfikacji zwrotów i reklamacji. ");
        prompt.append("Myśl po angielsku, ale odpowiadaj po polsku. ");
        prompt.append("Intencja: ").append(resolveIntentLabel(request.intent())).append(". ");

        if (request.orderNumber() != null && !request.orderNumber().isBlank()) {
            prompt.append("Numer zamówienia: ").append(request.orderNumber()).append(". ");
        }
        if (request.purchaseDate() != null) {
            String formattedDate = request.purchaseDate().format(DateTimeFormatter.ISO_DATE);
            prompt.append("Data zakupu: ").append(formattedDate).append(". ");
        }
        if (request.description() != null && !request.description().isBlank()) {
            prompt.append("Opis klienta: ").append(request.description()).append(". ");
        }
        prompt.append("Uzasadnij werdykt zgodnie z zasadami Sinsay.");
        return prompt.toString();
    }

    private String resolveIntentLabel(String intent) {
        if (intent == null) {
            return "nieznana";
        }
        String normalized = intent.trim().toLowerCase();
        if (RETURN_INTENT.equals(normalized)) {
            return "zwrot";
        }
        if (COMPLAINT_INTENT.equals(normalized)) {
            return "reklamacja";
        }
        return normalized;
    }

    private List<Message> convertMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        List<Message> converted = new ArrayList<>();
        for (ChatMessage message : messages) {
            converted.add(convertMessage(message));
        }
        return converted;
    }

    private Message convertMessage(ChatMessage message) {
        String role = message.role() == null ? "user" : message.role().trim().toLowerCase();
        String content = message.content() == null ? "" : message.content();
        List<ImagePayload> images = message.images() == null ? Collections.emptyList() : message.images();

        return switch (role) {
            case "assistant" -> new AssistantMessage(content);
            case "system" -> new SystemMessage(content);
            default -> images.isEmpty()
                    ? new UserMessage(content)
                    : new UserMessage(content, toMedia(images));
        };
    }

    private List<Media> toMedia(List<ImagePayload> images) {
        List<Media> mediaList = new ArrayList<>();
        for (ImagePayload image : images) {
            if (image == null || image.base64Data() == null) {
                continue;
            }
            String mimeType = image.mimeType() == null ? "image/jpeg" : image.mimeType();
            byte[] decoded = Base64.getDecoder().decode(image.base64Data());
            mediaList.add(new Media(MimeTypeUtils.parseMimeType(mimeType), decoded));
        }
        return mediaList;
    }
}
