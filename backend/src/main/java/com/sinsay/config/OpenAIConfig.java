package com.sinsay.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api-key:}")
    private String openaiApiKey;

    @Value("${openrouter.api-key:}")
    private String openrouterApiKey;

    @Value("${openai.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${openai.model:openai/gpt-4o-mini}")
    private String model;

    @Bean
    public OpenAIClient openAIClient() {
        String apiKey = openaiApiKey;
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = openrouterApiKey;
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Neither OPENAI_API_KEY nor OPENROUTER_API_KEY is set. " +
                    "Please set one of these environment variables before starting the application. " +
                    "For OpenRouter: Get your key from https://openrouter.ai/keys and set OPENROUTER_API_KEY."
            );
        }
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public String openaiModel() {
        return model;
    }
}
