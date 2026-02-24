package com.silkycoders1.jsystemssilkycodders1.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI components.
 */
@Configuration
public class AiConfig {

    /**
     * Creates the default ChatClient bean using the auto-configured ChatModel.
     * 
     * <p>Spring AI auto-configures the OpenAI ChatModel based on properties:
     * <ul>
     *   <li>{@code spring.ai.openai.api-key} - API key (from env var)</li>
     *   <li>{@code spring.ai.openai.base-url} - Base URL (for OpenRouter)</li>
     *   <li>{@code spring.ai.openai.chat.options.model} - Model name</li>
     * </ul>
     *
     * @param builder The auto-configured ChatClient.Builder
     * @return Configured ChatClient instance
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
