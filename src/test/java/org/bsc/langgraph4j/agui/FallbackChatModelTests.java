package org.bsc.langgraph4j.agui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * TDD tests for FallbackChatModel — a ChatModel wrapper that retries transient errors
 * (broken pipe, connection refused, timeout) once with the same model, then falls
 * through to the next model in the chain.
 *
 * Tests written BEFORE the FallbackChatModel class exists.
 *
 * Mock data based on real Ollama/Spring AI response structures captured from production logs:
 * - ChatResponse contains a list of Generation objects
 * - Each Generation wraps an AssistantMessage with text content
 * - Streaming returns Flux<ChatResponse> with one Generation per chunk
 */
@ExtendWith(MockitoExtension.class)
class FallbackChatModelTests {

    @Mock
    private ChatModel primaryModel;

    @Mock
    private ChatModel fallbackModel;

    @Mock
    private ChatModel thirdModel;

    private Prompt testPrompt;

    @BeforeEach
    void setUp() {
        testPrompt = new Prompt("Cześć, chcę zwrócić kurtkę");
    }

    // -- Helpers matching real Spring AI response structure --

    private static ChatResponse batchResponseOf(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }

    private static Flux<ChatResponse> streamingResponseOf(String... chunks) {
        return Flux.fromArray(chunks)
                .map(chunk -> new ChatResponse(
                        List.of(new Generation(new AssistantMessage(chunk)))
                ));
    }

    // =========================================================================
    // stream() tests
    // =========================================================================

    @Nested
    @DisplayName("stream(): transient error handling with retry and fallback")
    class StreamTests {

        @Test
        @DisplayName("Primary model succeeds on first try - returns its stream directly")
        void shouldReturnPrimaryStreamWhenItSucceeds() {
            // given
            given(primaryModel.stream(any(Prompt.class)))
                    .willReturn(streamingResponseOf("Cześć", "! Jak", " mogę pomóc?"));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when & then
            StepVerifier.create(sut.stream(testPrompt))
                    .expectNextCount(3)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Primary throws broken pipe - retry once after ~1s - retry succeeds")
        void shouldRetryOnceOnBrokenPipeThenSucceed() {
            // given: first call throws "Broken pipe" (Ollama drops connection mid-stream),
            // second call (retry) succeeds
            given(primaryModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new RuntimeException("Broken pipe")))
                    .willReturn(streamingResponseOf("Cześć", "! Odpowiedź po retry"));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when & then: should succeed via retry, with ~1s delay
            StepVerifier.create(sut.stream(testPrompt))
                    .expectNextCount(2)
                    .verifyComplete();

            // verify: primary was called exactly 2 times (original + 1 retry)
            verify(primaryModel, times(2)).stream(any(Prompt.class));
        }

        @Test
        @DisplayName("Primary throws broken pipe - retry fails - fallback model used")
        void shouldFallbackToNextModelWhenRetryAlsoFails() {
            // given: primary always throws broken pipe (Ollama truly down)
            given(primaryModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new RuntimeException("Broken pipe")));

            given(fallbackModel.stream(any(Prompt.class)))
                    .willReturn(streamingResponseOf("Odpowiedź", " z fallback modelu"));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when & then
            StepVerifier.create(sut.stream(testPrompt))
                    .expectNextCount(2)
                    .verifyComplete();

            // primary called twice (original + retry), fallback called once
            verify(primaryModel, times(2)).stream(any(Prompt.class));
            verify(fallbackModel, times(1)).stream(any(Prompt.class));
        }

        @Test
        @DisplayName("Primary throws connection refused - retry fails - fallback model used")
        void shouldFallbackOnConnectionRefused() {
            // given: Ollama service not running
            given(primaryModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new ConnectException("Connection refused")));

            given(fallbackModel.stream(any(Prompt.class)))
                    .willReturn(streamingResponseOf("Odpowiedź z fallback"));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when & then
            StepVerifier.create(sut.stream(testPrompt))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Primary throws timeout - retry fails - fallback model used")
        void shouldFallbackOnTimeout() {
            // given: model took too long
            given(primaryModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new RuntimeException("Read timed out")));

            given(fallbackModel.stream(any(Prompt.class)))
                    .willReturn(streamingResponseOf("Odpowiedź po timeout"));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when & then
            StepVerifier.create(sut.stream(testPrompt))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        @Test
        @DisplayName("All models fail with transient errors - last error propagates")
        void shouldPropagateErrorWhenAllModelsFail() {
            // given: every model in the chain fails
            given(primaryModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new RuntimeException("Broken pipe")));

            given(fallbackModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new ConnectException("Connection refused")));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when & then: the last error in the chain propagates
            StepVerifier.create(sut.stream(testPrompt))
                    .expectErrorMatches(e ->
                            e instanceof ConnectException
                                    && e.getMessage().contains("Connection refused"))
                    .verify(Duration.ofSeconds(10));
        }

        @Test
        @DisplayName("Non-transient error (e.g. 401 Unauthorized) is NOT retried - propagates immediately")
        void shouldNotRetryNonTransientError() {
            // given: auth error should not be retried (it will never self-heal)
            given(primaryModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new RuntimeException("401 Unauthorized")));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when & then: error propagates without retry or fallback
            StepVerifier.create(sut.stream(testPrompt))
                    .expectErrorMatches(e -> e.getMessage().contains("401 Unauthorized"))
                    .verify(Duration.ofSeconds(5));

            // primary called only once (no retry for non-transient)
            verify(primaryModel, times(1)).stream(any(Prompt.class));
        }

        @Test
        @DisplayName("Three-model chain: primary and second fail, third succeeds")
        void shouldTryAllModelsInChainBeforeGivingUp() {
            // given
            given(primaryModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new RuntimeException("Broken pipe")));

            given(fallbackModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new RuntimeException("Broken pipe")));

            given(thirdModel.stream(any(Prompt.class)))
                    .willReturn(streamingResponseOf("Odpowiedź", " z trzeciego modelu"));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel, thirdModel));

            // when & then
            StepVerifier.create(sut.stream(testPrompt))
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Retry delay is approximately 1 second")
        void shouldDelayRetryByApproximatelyOneSecond() {
            // given: primary fails first, then succeeds on retry
            given(primaryModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new RuntimeException("Broken pipe")))
                    .willReturn(streamingResponseOf("OK"));

            var sut = new FallbackChatModel(List.of(primaryModel));

            // when & then: use StepVerifier's virtual time to verify the ~1s delay
            StepVerifier.withVirtualTime(() -> sut.stream(testPrompt))
                    .expectSubscription()
                    .expectNoEvent(Duration.ofMillis(900))
                    .thenAwait(Duration.ofMillis(200))
                    .expectNextCount(1)
                    .verifyComplete();
        }
    }

    // =========================================================================
    // call() tests
    // =========================================================================

    @Nested
    @DisplayName("call(): transient error handling with retry and fallback")
    class CallTests {

        @Test
        @DisplayName("Primary model succeeds on first try - returns its response directly")
        void shouldReturnPrimaryResponseWhenItSucceeds() {
            // given
            given(primaryModel.call(any(Prompt.class)))
                    .willReturn(batchResponseOf("Cześć! Jak mogę Ci pomóc?"));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when
            ChatResponse response = sut.call(testPrompt);

            // then
            assertThat(response.getResult().getOutput().getText())
                    .isEqualTo("Cześć! Jak mogę Ci pomóc?");
        }

        @Test
        @DisplayName("Primary throws broken pipe - retry once - retry succeeds")
        void shouldRetryOnceOnBrokenPipeThenSucceedForCall() {
            // given: first call throws, second succeeds
            given(primaryModel.call(any(Prompt.class)))
                    .willThrow(new RuntimeException("Broken pipe"))
                    .willReturn(batchResponseOf("Odpowiedź po retry"));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when
            ChatResponse response = sut.call(testPrompt);

            // then
            assertThat(response.getResult().getOutput().getText())
                    .isEqualTo("Odpowiedź po retry");

            verify(primaryModel, times(2)).call(any(Prompt.class));
        }

        @Test
        @DisplayName("Primary throws broken pipe - retry fails - fallback model used")
        void shouldFallbackToNextModelWhenRetryAlsoFailsForCall() {
            // given: primary always throws
            given(primaryModel.call(any(Prompt.class)))
                    .willThrow(new RuntimeException("Broken pipe"));

            given(fallbackModel.call(any(Prompt.class)))
                    .willReturn(batchResponseOf("Odpowiedź z fallback"));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when
            ChatResponse response = sut.call(testPrompt);

            // then
            assertThat(response.getResult().getOutput().getText())
                    .isEqualTo("Odpowiedź z fallback");

            verify(primaryModel, times(2)).call(any(Prompt.class));
            verify(fallbackModel, times(1)).call(any(Prompt.class));
        }

        @Test
        @DisplayName("All models fail - last error propagates")
        void shouldPropagateErrorWhenAllModelsFailForCall() {
            // given
            given(primaryModel.call(any(Prompt.class)))
                    .willThrow(new RuntimeException("Broken pipe"));

            given(fallbackModel.call(any(Prompt.class)))
                    .willThrow(new RuntimeException("Connection refused"));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when & then
            assertThatThrownBy(() -> sut.call(testPrompt))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Connection refused");
        }

        @Test
        @DisplayName("Non-transient error is NOT retried for call()")
        void shouldNotRetryNonTransientErrorForCall() {
            // given
            given(primaryModel.call(any(Prompt.class)))
                    .willThrow(new RuntimeException("401 Unauthorized"));

            var sut = new FallbackChatModel(List.of(primaryModel, fallbackModel));

            // when & then
            assertThatThrownBy(() -> sut.call(testPrompt))
                    .hasMessageContaining("401 Unauthorized");

            verify(primaryModel, times(1)).call(any(Prompt.class));
        }
    }

    // =========================================================================
    // Constructor validation
    // =========================================================================

    @Nested
    @DisplayName("Constructor and edge cases")
    class ConstructorTests {

        @Test
        @DisplayName("Empty model list throws IllegalArgumentException")
        void shouldRejectEmptyModelList() {
            assertThatThrownBy(() -> new FallbackChatModel(List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Null model list throws NullPointerException")
        void shouldRejectNullModelList() {
            assertThatThrownBy(() -> new FallbackChatModel(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Single model in list: retry once on transient failure, then propagate error")
        void shouldRetryOnceWithSingleModelThenFail() {
            // given
            given(primaryModel.stream(any(Prompt.class)))
                    .willReturn(Flux.error(new RuntimeException("Broken pipe")));

            var sut = new FallbackChatModel(List.of(primaryModel));

            // when & then: retry once + no fallback = error propagates
            StepVerifier.create(sut.stream(testPrompt))
                    .expectErrorMatches(e -> e.getMessage().contains("Broken pipe"))
                    .verify(Duration.ofSeconds(10));

            verify(primaryModel, times(2)).stream(any(Prompt.class));
        }
    }
}
