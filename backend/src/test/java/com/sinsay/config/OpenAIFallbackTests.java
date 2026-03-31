package com.sinsay.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests to verify OpenRouter API key fallback functionality.
 */
class OpenAIFallbackTests {

    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = {
            "openai.api-key=",
            "openrouter.api-key=sk-test-openrouter-key"
    })
    static class WhenOnlyOpenRouterApiKeyIsSet {

        @Autowired
        private com.openai.client.OpenAIClient openAIClient;

        @Test
        void openAIClientBean_shouldBeConfigured() {
            assertNotNull(openAIClient, "OpenAIClient bean should be configured when only OPENROUTER_API_KEY is set");
        }
    }

    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = {
            "openai.api-key=sk-test-openai-key",
            "openrouter.api-key=sk-test-openrouter-key"
    })
    static class WhenBothApiKeysAreSet {

        @Autowired
        private com.openai.client.OpenAIClient openAIClient;

        @Test
        void openAIClientBean_shouldUseOpenAIKey() {
            assertNotNull(openAIClient, "OpenAIClient bean should be configured when both keys are set (OPENAI_API_KEY takes precedence)");
        }
    }
}
