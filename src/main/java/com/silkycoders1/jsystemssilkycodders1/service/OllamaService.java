package com.silkycoders1.jsystemssilkycodders1.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

@Slf4j
@Service
public class OllamaService {

    @Autowired
    private ChatModel chatModel;

    public String analyzeImage(String base64Image, String promptText, String mimeType) {
        log.info("Analyzing image with Spring AI Ollama using mime type: {}", mimeType);

        try {
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes);

            UserMessage userMessage = new UserMessage(promptText,
                    new Media(MimeType.valueOf(mimeType), imageResource));

            ChatResponse response = chatModel.call(new Prompt(userMessage, ChatOptions.builder()
                    .model("gemma3:12b")
                    .temperature(0.7)
                    .build()));

            log.info("Response received from Spring AI Ollama");

            if (response != null && response.getResult() != null) {
                return response.getResult().getOutput().getText();
            }
        } catch (Exception e) {
            log.error("Error during image analysis with Spring AI Ollama", e);
            return "Error: " + e.getMessage();
        }

        return "No response from Ollama";
    }
}
