# Hallucination detection and reliability

## OpenAI SDK Capabilities

OpenAI's primary mechanism for confidence scoring is **logprobs** (log probabilities), available in the Chat Completions API via the `logprobs` and `top_logprobs` parameters. When enabled, the API returns the log probability of each output token, where a value of `0.0` represents 100% probability and any negative number represents lower confidence. Developers can aggregate these per-token values (using mean, sum, or weighted distribution) to produce a sentence- or response-level confidence score.[^1_1][^1_2][^1_3]

**Important caveat for GPT-5:** As of late 2025, logprobs appear to have been deprecated for GPT-5 models, leaving a gap for confidence scoring on the latest OpenAI models. This is a significant limitation for production systems relying on this mechanism going forward.[^1_4]

The official **openai-java** SDK (currently v4.6.1) exposes the full OpenAI REST API, including `ChatCompletionCreateParams`. However, there is no dedicated convenience method wrapping logprob-based confidence scoring — developers must manually add `.logprobs(true)` and `.topLogprobs(N)` to their request params and extract the `logprobs` field from each `Choice` in the response. There is no built-in hallucination detection in the SDK.[^1_5]

***

## Java-Specific Libraries

### Official OpenAI Java SDK

The official `com.openai:openai-java` SDK (v4.6.1) maps closely to the REST API but provides **no built-in confidence or hallucination detection utilities**. You must manually configure logprob parameters and parse the response tree.[^1_5]

A Java implementation pattern would look like:

```java
ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
    .addUserMessage("Is Paris the capital of France?")
    .model(ChatModel.GPT_4_1)
    .logprobs(true)
    .topLogprobs(5)    // up to 5 candidate tokens per position
    .build();

ChatCompletion completion = client.chat().completions().create(params);

// Extract logprobs from the choice
completion.choices().forEach(choice -> {
    choice.logprobs().ifPresent(logprobs -> {
        logprobs.content().ifPresent(contentTokens -> {
            contentTokens.forEach(token -> {
                double prob = Math.exp(token.logprob()); // convert to [0,1]
                System.out.printf("Token: %s | Confidence: %.2f%%%n",
                    token.token(), prob * 100);
            });
        });
    });
});
```

There is no `.confidenceScore()` method — you compute it yourself from the raw logprob values.

### Spring AI

Spring AI (1.0.x) fully exposes the OpenAI logprobs structure through its API layer. The relevant classes are:[^1_6][^1_7]

- `OpenAiApi.LogProbs` — record holding `content` (a `List<LogProbs.Content>`) and `refusal` tokens[^1_6]
- `OpenAiApi.LogProbs.Content.TopLogProbs` — record with fields `token` (String) and `logprob` (double)[^1_7]

Spring AI does **not** add any higher-level confidence scoring or hallucination detection abstraction on top of these raw structures. It simply relays what the OpenAI API returns. There is no `HallucinationDetector`, `ConfidenceScorer`, or similar component in Spring AI's current release. The `spring-ai-bench` project benchmarks agent execution performance but does not include confidence scoring utilities.[^1_8][^1_9]

### TheoKanning/openai-java (Community Fork)

The legacy community library `TheoKanning/openai-java` lacked logprobs support for chat completions. A community fork at `panghy/openai-java` added logprobs support for the Chat Completions endpoint. This library is now largely superseded by the official OpenAI Java SDK.[^1_10]

***

## LLM Provider Native Features

Here is a breakdown of what each major provider offers natively:


| Provider | Logprobs Support | Notes |
| :-- | :-- | :-- |
| **OpenAI** | ✅ Yes (`logprobs`, `top_logprobs`) | Deprecated for GPT-5 as of late 2025 [^1_4] |
| **Google (Gemini / Vertex AI)** | ✅ Yes (`response_logprobs`, `logprobs`) | Returns `avgLogprobs` by default; `logprobsCandidates` for alternates [^1_11][^1_12] |
| **Anthropic (Claude)** | ❌ No | No logprobs endpoint; no native confidence scoring API [^1_13] |
| **Cohere** | ⚠️ Partial | Log-likelihood for some generation endpoints, not uniformly exposed |
| **Mistral** | ⚠️ Partial | Logprobs available on some endpoints, similar to OpenAI structure [^1_14] |

**Google Gemini on Vertex AI** is notably well-supported: you can set `response_logprobs=True` in `generation_config` to receive per-token log probabilities and use `avgLogprobs` (returned by default) as a quick quality signal. The Vertex AI docs show patterns like flagging ambiguous classification results when the difference between top-1 and top-2 logprob candidates is below a defined margin.[^1_11][^1_15]

**Anthropic** provides no logprobs in the API. The recommended workaround is either asking Claude to self-report its confidence in the prompt (unreliable ) or using black-box consistency techniques described below.[^1_16][^1_15]

***

## Technical Implementation Details

There are three main technical approaches to confidence scoring in LLM systems:

### 1. Logprobs / Token Probabilities

The most direct method. The model returns the log of the softmax probability for each generated token. Common aggregation strategies:[^1_2][^1_1]

- **Mean log-probability** over all tokens: a rough proxy for sentence-level confidence
- **Minimum token probability**: a lower bound — if any token is very low-confidence, the response may be unreliable[^1_17]
- **Weighted top-K probability** (e.g., Azure OpenAI pattern with `TopLogProbabilityCount=5`): averages the top 5 candidate tokens by probability mass to get a calibrated score, compensating for cases where the top token has only ~43% probability but other plausible tokens exist[^1_18]
- **Brier Scores**: measure the probabilistic forecasting accuracy of individual logprob predictions over time[^1_18]


### 2. BSDetector / Consistency Sampling

For models without logprob access (e.g., Anthropic), the **BSDetector** method (published at ACL 2024) works on any black-box API:[^1_19][^1_20]

1. Call the LLM multiple times with the same prompt at varying temperatures
2. Compare consistency of responses using semantic similarity
3. Lower consistency → lower confidence score

This is computationally expensive (multiple API calls per query) but model-agnostic.[^1_19]

### 3. Prompt-Elicited Self-Assessment

Ask the model to rate its own confidence (e.g., "Rate your confidence 1-10"). This is **unreliable** — LLMs are not well-calibrated self-evaluators and can confidently state incorrect information. The LinkedIn post from January 2026 explicitly warns: "Stop asking LLMs for a confidence score. They are lying to you."[^1_15]

***

## Third-Party Solutions

### DeepEval (Python-first, with API bridge)

DeepEval provides a `HallucinationMetric` that uses LLM-as-a-judge to compare model outputs against provided context. It is Python-native but can be integrated into Java pipelines via subprocess or HTTP calls.[^1_21]

### BSDetector (Research Library)

Available as a research implementation; can wrap any LLM API. Not Java-native.[^1_20][^1_19]

### LangChain4j

LangChain4j (the Java equivalent of LangChain) does not have built-in confidence scoring, but community patterns exist for computing weighted confidence from OpenAI logprobs metadata. The Python LangChain community has explored `ConfidenceCalibrator` patterns, but these are not ported to LangChain4j.[^1_22][^1_23]

### Azure OpenAI LogProbs Examples (C\#/.NET)

The `bartczernicki/AzureOpenAILogProbs` repository provides the most thorough reference implementation of logprob-based confidence scoring patterns (First Token Probability, Weighted PMF, Confidence Intervals via bootstrapping). While in C\#, the algorithmic patterns are directly applicable to Java with the official OpenAI Java SDK.[^1_18]

***

## Limitations and Considerations

- **Logprobs ≠ factual accuracy.** A model can be highly confident (logprob ≈ 0) about a factually wrong token. Token-level probability reflects linguistic fluency and training data distribution, not ground-truth correctness. High confidence does not mean no hallucination.[^1_24][^1_17]
- **GPT-5 deprecation of logprobs** is a serious production concern. As of late 2025, this removes the primary confidence mechanism for the most capable OpenAI model.[^1_4]
- **Reasoning models (o1, o3)** don't expose chain-of-thought logprobs, making confidence estimation even harder for these architectures.
- **Anthropic Claude has no logprobs at all**, forcing reliance on black-box consistency methods like BSDetector, which require 3–10× the API call budget.
- **Individual hallucination detection metrics perform marginally better than random chance** in some benchmarks — one 2025 paper found only 56.6% ROC-AUC for code-change-to-NL hallucination detection.[^1_25]
- **Logprob-based confidence is poorly calibrated for structured outputs.** When using JSON mode or structured output schemas in the OpenAI SDK, logprob metadata is harder to access and the probability distribution is constrained by the schema, making raw logprob values less meaningful.[^1_22]
- The most reliable production strategy combines logprobs for quick filtering with retrieval-grounded verification (RAG with source citation) and human-in-the-loop for edge cases below a confidence threshold.[^1_26]
<span style="display:none">[^1_27][^1_28][^1_29][^1_30][^1_31][^1_32][^1_33][^1_34][^1_35][^1_36][^1_37][^1_38][^1_39][^1_40]</span>

<div align="center">⁂</div>

[^1_1]: https://developers.openai.com/cookbook/examples/using_logprobs/

[^1_2]: https://ericjinks.com/blog/2025/logprobs/

[^1_3]: https://developers.openai.com/cookbook/examples/using_logprobs

[^1_4]: https://community.openai.com/t/logprobs-deprecated-for-gpt-5-models/1355427

[^1_5]: https://github.com/openai/openai-java

[^1_6]: https://docs.spring.io/spring-ai/docs/current/api/org/springframework/ai/openai/api/OpenAiApi.LogProbs.html

[^1_7]: https://docs.spring.io/spring-ai/docs/current/api/org/springframework/ai/openai/api/OpenAiApi.LogProbs.Content.TopLogProbs.html

[^1_8]: https://github.com/spring-ai-community/spring-ai-bench

[^1_9]: https://spring.io/blog/2025/04/14/spring-ai-prompt-engineering-patterns

[^1_10]: https://github.com/TheoKanning/openai-java/issues/487

[^1_11]: https://developers.googleblog.com/unlock-gemini-reasoning-with-logprobs-on-vertex-ai/

[^1_12]: https://github.com/vercel/ai/issues/5418

[^1_13]: https://www.anthropic.com/transparency

[^1_14]: https://www.baeldung.com/spring-ai-mistral-api-function-calling

[^1_15]: https://www.linkedin.com/pulse/stop-asking-llms-confidence-score-lying-you-use-logprobs-molina-kuz6c

[^1_16]: https://openreview.net/pdf?id=QTImFg6MHU

[^1_17]: https://nanonets.com/blog/how-to-tell-if-your-llm-is-hallucinating/

[^1_18]: https://github.com/bartczernicki/AzureOpenAILogProbs

[^1_19]: https://ar5iv.labs.arxiv.org/html/2308.16175

[^1_20]: https://aclanthology.org/2024.acl-long.283/

[^1_21]: https://deepeval.com/docs/metrics-hallucination

[^1_22]: https://github.com/langchain-ai/langchain/discussions/30491

[^1_23]: https://sparkco.ai/blog/advanced-techniques-for-hallucination-detection-in-ai

[^1_24]: https://www.linkedin.com/pulse/harnessing-logprobs-gpt-confidence-score-mitigating-ashish-bhatia-1zgme

[^1_25]: https://aclanthology.org/2025.ijcnlp-long.137.pdf

[^1_26]: https://www.linkedin.com/posts/gaurav-shelke-2604_ai-engineering-rag-activity-7420118060554145795-dyxX

[^1_27]: https://community.openai.com/t/evaluate-confidence-on-gpt-5-response/1364636

[^1_28]: https://www.reddit.com/r/LocalLLaMA/comments/185c0r0/automatic_hallucination_detection_using/

[^1_29]: https://github.com/vezlo/ai-validator

[^1_30]: https://community.openai.com/t/thought-answer-pattern-while-evaluating-confidence-from-logprobs/777129

[^1_31]: https://www.getmaxim.ai/articles/top-5-tools-to-detect-hallucination-in-2025/

[^1_32]: https://testfort.com/blog/ai-hallucination-testing-guide

[^1_33]: https://skywork.ai/blog/claude-haiku-4-5-vertex-ai-vs-anthropic-api-2025-comparison/

[^1_34]: https://hallucinations.cloud

[^1_35]: https://deepwiki.com/openai/openai-cookbook/8.2-using-logprobs

[^1_36]: https://www.anthropic.com/research/anthropic-economic-index-september-2025-report

[^1_37]: https://superprompt.com/blog/best-ai-hallucination-detection-tools-enterprise-2025

[^1_38]: https://github.com/spring-ai-community/awesome-spring-ai

[^1_39]: https://spring-ai-community.github.io/spring-ai-bench/

[^1_40]: https://community.openai.com/t/logprobs-in-chatcompletion/329471
