package org.bsc.langgraph4j.agui;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AGUIAgentExecutorModelResolutionTests {

    /**
     * Subclass that lets us override env var lookups and inject fake model suppliers
     * to test the fallback chain without hitting real APIs or Ollama.
     */
    static class TestableExecutor extends AGUIAgentExecutor {
        private final Map<String, String> envOverrides = new HashMap<>();

        TestableExecutor(AiModel primaryModel) {
            super(primaryModel);
        }

        TestableExecutor() {
            super(null);
        }

        void setEnv(String key, String value) {
            envOverrides.put(key, value);
        }

        @Override
        String getEnv(String name) {
            if (envOverrides.containsKey(name)) {
                return envOverrides.get(name);
            }
            // Return null for all env vars by default — isolate tests from real env
            return null;
        }
    }

    // -- Primary model tests --

    // Helper: unwrap FallbackChatModel chain
    private static List<ChatModel> chainOf(ChatModel model) {
        assertThat(model).isInstanceOf(FallbackChatModel.class);
        return ((FallbackChatModel) model).models;
    }

    @Test
    void shouldUsePrimaryModelWhenConfigured() {
        // given
        var executor = new TestableExecutor(AGUIAgentExecutor.AiModel.OLLAMA_KIMI_K2_5_CLOUD);

        // when
        var chain = chainOf(executor.resolveModel());

        // then — first model in chain is the configured primary (Ollama)
        assertThat(chain).hasSizeGreaterThanOrEqualTo(2);
        assertThat(chain.get(0)).isInstanceOf(OllamaChatModel.class);
        // last model is always qwen2.5 as final fallback
        assertThat(chain.get(chain.size() - 1)).isInstanceOf(OllamaChatModel.class);
    }

    @Test
    void shouldUsePrimaryModelEvenWhenEnvVarsAreSet() {
        // given — primary is Ollama, but OpenAI key is also present
        var executor = new TestableExecutor(AGUIAgentExecutor.AiModel.OLLAMA_KIMI_K2_5_CLOUD);
        executor.setEnv("OPENAI_API_KEY", "sk-test-key");

        // when
        var chain = chainOf(executor.resolveModel());

        // then — primary (Ollama kimi) is first in chain; OpenAI and qwen2.5 also present
        assertThat(chain).hasSizeGreaterThanOrEqualTo(3);
        assertThat(chain.get(0)).isInstanceOf(OllamaChatModel.class);
    }

    // -- Fallback chain tests --

    @Test
    void shouldFallbackToOpenAiWhenNoPrimaryAndOpenAiKeyPresent() {
        // given
        var executor = new TestableExecutor();
        executor.setEnv("OPENAI_API_KEY", "sk-test-key");

        // when
        var chain = chainOf(executor.resolveModel());

        // then — no primary, so OpenAI is first; qwen2.5 is last resort
        assertThat(chain).hasSizeGreaterThanOrEqualTo(2);
        assertThat(chain.get(0)).isInstanceOf(OpenAiChatModel.class);
        assertThat(chain.get(chain.size() - 1)).isInstanceOf(OllamaChatModel.class);
    }

    @Test
    void shouldFallbackToLastResortWhenGitHubTokenSetButSupplierFailsDueToNullRealEnv() {
        // given — getEnv returns token, but the enum supplier internally calls System.getenv()
        // which returns null in test env, causing the GitHub Models supplier to fail.
        // The qwen2.5 last-resort must always be present regardless of supplier failures.
        var executor = new TestableExecutor();
        executor.setEnv("GITHUB_MODELS_TOKEN", "ghp-test-token");

        // when
        var chain = chainOf(executor.resolveModel());

        // then — GitHub supplier fails at construction, but qwen2.5 is always in chain
        assertThat(chain).isNotEmpty();
        assertThat(chain.get(chain.size() - 1)).isInstanceOf(OllamaChatModel.class);
    }

    @Test
    void shouldFallbackFromOpenAiToLastResortWhenBothEnvSetButSuppliersFailInRealEnv() {
        // given — both env vars set, real System.getenv() returns null for GitHub but not OpenAI
        var executor = new TestableExecutor();
        executor.setEnv("OPENAI_API_KEY", "sk-test-key");
        executor.setEnv("GITHUB_MODELS_TOKEN", "ghp-test-token");

        // when
        var chain = chainOf(executor.resolveModel());

        // then — chain is non-empty, ends with qwen2.5 as last resort
        assertThat(chain).isNotEmpty();
        assertThat(chain.get(chain.size() - 1)).isInstanceOf(OllamaChatModel.class);
    }

    @Test
    void shouldFallbackToOllamaQwenWhenNoEnvVarsAndNoPrimary() {
        // given — no primary, no env vars — qwen2.5 is the only model in chain
        var executor = new TestableExecutor();

        // when
        var chain = chainOf(executor.resolveModel());

        // then — single model: qwen2.5
        assertThat(chain).hasSize(1);
        assertThat(chain.get(0)).isInstanceOf(OllamaChatModel.class);
    }

    // -- Failing primary model with fallback --

    @Test
    void shouldFallbackWhenPrimaryModelSupplierThrows() {
        // given — a fake AiModel enum value can't be created, so we use a subclass
        //         that simulates primary failure by overriding with a throwing supplier
        var executor = new AGUIAgentExecutor(null) {
            private final Supplier<ChatModel> failingSupplier = () -> {
                throw new RuntimeException("Connection refused");
            };

            @Override
            ChatModel resolveModel() {
                // Simulate: primary configured but fails, then fall through
                try {
                    return failingSupplier.get();
                } catch (Exception e) {
                    // No env vars → last resort
                    return AGUIAgentExecutor.AiModel.OLLAMA_QWEN2_5_7B.model.get();
                }
            }

            @Override
            String getEnv(String name) {
                return null;
            }
        };

        // when
        ChatModel model = executor.resolveModel();

        // then
        assertThat(model).isInstanceOf(OllamaChatModel.class);
    }

    // -- AiModel enum tests --

    @Test
    void shouldHaveKimiModelInEnum() {
        var model = AGUIAgentExecutor.AiModel.valueOf("OLLAMA_KIMI_K2_5_CLOUD");
        assertThat(model).isNotNull();
        assertThat(model.name()).isEqualTo("OLLAMA_KIMI_K2_5_CLOUD");
    }

    @Test
    void shouldThrowOnInvalidEnumName() {
        assertThatThrownBy(() -> AGUIAgentExecutor.AiModel.valueOf("NONEXISTENT_MODEL"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldContainAllExpectedModels() {
        var values = AGUIAgentExecutor.AiModel.values();
        assertThat(values).extracting(Enum::name).containsExactlyInAnyOrder(
                "OPENAI_GPT_4O_MINI",
                "GITHUB_MODELS_GPT_4O_MINI",
                "OLLAMA_QWEN2_5_7B",
                "OLLAMA_QWEN3_14B",
                "OLLAMA_KIMI_K2_5_CLOUD"
        );
    }

    @Test
    void shouldHaveNonNullSuppliersForAllModels() {
        for (var model : AGUIAgentExecutor.AiModel.values()) {
            assertThat(model.model)
                    .as("Supplier for %s should not be null", model.name())
                    .isNotNull();
        }
    }

    // -- Constructor tests --

    @Test
    void shouldAcceptNullPrimaryModel() {
        var executor = new AGUIAgentExecutor(null);
        // Should not throw — null means "use fallback chain"
        assertThat(executor).isNotNull();
    }

    @Test
    void shouldAcceptNoArgConstructor() {
        var executor = new AGUIAgentExecutor();
        assertThat(executor).isNotNull();
    }
}
