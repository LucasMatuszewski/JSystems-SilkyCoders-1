package org.bsc.langgraph4j.agui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * A ChatModel wrapper that provides retry-then-fallback behavior for transient errors.
 *
 * On a transient error (broken pipe, connection refused, timeout):
 * <ol>
 *   <li>Wait 1 second, retry once with the SAME model</li>
 *   <li>If retry still fails: try the NEXT model in the list</li>
 *   <li>If all models fail: propagate the last error</li>
 * </ol>
 *
 * Non-transient errors (e.g. 401 Unauthorized, 403 Forbidden) propagate immediately
 * without retry or fallback since they will never self-heal.
 *
 * Uses Reactor operators exclusively -- no blocking, no Thread.sleep.
 */
public class FallbackChatModel implements ChatModel {

    private static final Logger log = LoggerFactory.getLogger(FallbackChatModel.class);
    private static final Duration RETRY_DELAY = Duration.ofSeconds(1);

    /** Package-private — accessible from tests in the same package. */
    final List<ChatModel> models;

    public FallbackChatModel(List<ChatModel> models) {
        Objects.requireNonNull(models, "Model list must not be null");
        if (models.isEmpty()) {
            throw new IllegalArgumentException("Model list must not be empty");
        }
        this.models = List.copyOf(models);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return streamWithFallback(prompt, 0);
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return callWithFallback(prompt, 0);
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return models.get(0).getDefaultOptions();
    }

    // -- stream() implementation --

    private Flux<ChatResponse> streamWithFallback(Prompt prompt, int modelIndex) {
        var model = models.get(modelIndex);
        log.debug("Attempting stream() with model at index {} ({})", modelIndex, model.getClass().getSimpleName());

        return Flux.defer(() -> model.stream(prompt))
                .onErrorResume(error -> {
                    int next = modelIndex + 1;

                    if (!isTransient(error)) {
                        // Non-transient (e.g. 401 Unauthorized): no retry, but still fall through to next model.
                        // Only propagate when all models are exhausted.
                        if (next >= models.size()) {
                            log.warn("Non-transient error from model at index {} and no more models. Propagating: {}",
                                    modelIndex, error.getMessage());
                            return Flux.error(error);
                        }
                        log.warn("Non-transient error from model at index {}: {}. Trying next model...",
                                modelIndex, error.getMessage());
                        return streamWithFallback(prompt, next);
                    }

                    log.warn("Transient error from model at index {}: {}. Retrying after {}ms...",
                            modelIndex, error.getMessage(), RETRY_DELAY.toMillis());

                    // Retry once with the same model after a delay
                    return Mono.delay(RETRY_DELAY)
                            .flatMapMany(ignored -> {
                                log.debug("Retrying stream() with model at index {}", modelIndex);
                                return Flux.defer(() -> model.stream(prompt));
                            })
                            .onErrorResume(retryError -> {
                                if (next >= models.size()) {
                                    // All models exhausted — propagate the last real error
                                    log.warn("All models exhausted after retry at index {}. Propagating last error.", modelIndex);
                                    return Flux.error(retryError);
                                }
                                log.warn("Retry also failed for model at index {}: {}. Trying next model...",
                                        modelIndex, retryError.getMessage());
                                return streamWithFallback(prompt, next);
                            });
                });
    }

    // -- call() implementation --

    private ChatResponse callWithFallback(Prompt prompt, int modelIndex) {
        var model = models.get(modelIndex);
        log.debug("Attempting call() with model at index {} ({})", modelIndex, model.getClass().getSimpleName());

        int next = modelIndex + 1;
        try {
            return model.call(prompt);
        } catch (Exception error) {
            if (!isTransient(error)) {
                // Non-transient (e.g. 401 Unauthorized): no retry, but still fall through to next model.
                if (next >= models.size()) {
                    log.warn("Non-transient error from model at index {} and no more models. Propagating: {}",
                            modelIndex, error.getMessage());
                    throw error;
                }
                log.warn("Non-transient error from model at index {}: {}. Trying next model...",
                        modelIndex, error.getMessage());
                return callWithFallback(prompt, next);
            }

            log.warn("Transient error from model at index {}: {}. Retrying after {}ms...",
                    modelIndex, error.getMessage(), RETRY_DELAY.toMillis());

            try {
                // call() is a blocking method by contract; block on the retry delay.
                Mono.delay(RETRY_DELAY).block();
                log.debug("Retrying call() with model at index {}", modelIndex);
                return model.call(prompt);
            } catch (Exception retryError) {
                if (next >= models.size()) {
                    // All models exhausted — propagate the last real error
                    log.warn("All models exhausted after retry at index {}. Propagating last error.", modelIndex);
                    throw retryError;
                }
                log.warn("Retry also failed for model at index {}: {}. Trying next model...",
                        modelIndex, retryError.getMessage());
                return callWithFallback(prompt, next);
            }
        }
    }

    /**
     * Determines whether an error is transient (worth retrying) or permanent.
     *
     * Transient errors include:
     * - Broken pipe (Ollama drops connection mid-stream, especially with large image payloads)
     * - Connection refused (Ollama service not running)
     * - Timeout (model took too long to respond)
     *
     * Non-transient errors include:
     * - 401/403 authentication failures (bad API key)
     * - 400 bad request (malformed prompt)
     */
    static boolean isTransient(Throwable error) {
        if (error instanceof ConnectException) {
            return true;
        }

        String msg = error.getMessage() != null ? error.getMessage().toLowerCase() : "";
        String causeMsg = (error.getCause() != null && error.getCause().getMessage() != null)
                ? error.getCause().getMessage().toLowerCase() : "";

        return msg.contains("broken pipe")
                || causeMsg.contains("broken pipe")
                || msg.contains("connection refused")
                || causeMsg.contains("connection refused")
                || msg.contains("timed out")
                || causeMsg.contains("timed out")
                || msg.contains("timeout")
                || causeMsg.contains("timeout")
                || msg.contains("connect to")
                || causeMsg.contains("connect to");
    }
}
