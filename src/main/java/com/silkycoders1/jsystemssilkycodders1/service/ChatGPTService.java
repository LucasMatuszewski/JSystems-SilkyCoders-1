package com.silkycoders1.jsystemssilkycodders1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.silkycoders1.jsystemssilkycodders1.api.response.ChatGPTResponse;
import com.silkycoders1.jsystemssilkycodders1.api.reuqest.ChatGPTRequest;

import java.util.List;

@Service
public class ChatGPTService {

    @Autowired
    private RestTemplate openaiRestTemplate;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    public String chat(String prompt) {
        ChatGPTRequest request = new ChatGPTRequest(
                model,
                List.of(new ChatGPTRequest.Message("user", prompt)));

        ChatGPTResponse response = openaiRestTemplate.postForObject(
                apiUrl,
                request,
                ChatGPTResponse.class);

        if (response != null && !response.choices().isEmpty()) {
            return response.choices().get(0).message().content();
        }

        return "No response from ChatGPT";
    }

    public String analyzeImage(String base64Image, String prompt, String mimeType) {
        String imageUrl = "data:" + mimeType + ";base64," + base64Image;
        ChatGPTRequest.ContentPart imagePart = new ChatGPTRequest.ContentPart(new ChatGPTRequest.ImageUrl(imageUrl));
        ChatGPTRequest.ContentPart textPart = new ChatGPTRequest.ContentPart(prompt);

        ChatGPTRequest request = new ChatGPTRequest(
                model,
                List.of(new ChatGPTRequest.Message("user", List.of(textPart, imagePart))));

        ChatGPTResponse response = openaiRestTemplate.postForObject(
                apiUrl,
                request,
                ChatGPTResponse.class);

        if (response != null && !response.choices().isEmpty()) {
            return response.choices().get(0).message().content();
        }

        return "No response from ChatGPT";
    }
}
