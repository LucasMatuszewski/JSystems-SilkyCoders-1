package org.bsc.langgraph4j.agui;

import com.silkycoders1.jsystemssilkycodders1.service.PolicyService;
import com.silkycoders1.jsystemssilkycodders1.tools.SinsayTools;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.action.InterruptionMetadata;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.spring.ai.agentexecutor.AgentExecutorEx;
import org.bsc.langgraph4j.spring.ai.util.MessageUtil;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static org.bsc.langgraph4j.utils.CollectionsUtils.lastOf;

//@org.springframework.stereotype.Component("AGUIAgent")
public class AGUIAgentExecutor extends AGUILangGraphAgent {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AGUIAgentExecutor.class);

    public enum AiModel {

        OPENAI_GPT_4O_MINI( () ->
                OpenAiChatModel.builder()
                        .openAiApi(OpenAiApi.builder()
                                .baseUrl("https://api.openai.com")
                                .apiKey( System.getenv("OPENAI_API_KEY"))
                                .build())
                        .defaultOptions(OpenAiChatOptions.builder()
                                .model("gpt-4o-mini")
                                .logprobs(false)
                                .temperature(0.1)
                                .build())
                        .build()),
        GITHUB_MODELS_GPT_4O_MINI( () ->
                OpenAiChatModel.builder()
                        .openAiApi(OpenAiApi.builder()
                                .baseUrl("https://models.github.ai/inference") // GITHUB MODELS
                                .apiKey(System.getenv("GITHUB_MODELS_TOKEN"))
                                .build())
                        .defaultOptions(OpenAiChatOptions.builder()
                                .model("gpt-4o-mini")
                                .logprobs(false)
                                .temperature(0.1)
                                .build())
                        .build()),
        OLLAMA_QWEN2_5_7B( () ->
                OllamaChatModel.builder()
                        .ollamaApi( OllamaApi.builder().baseUrl("http://localhost:11434").build() )
                        .defaultOptions(OllamaOptions.builder()
                                .model("qwen2.5:7b")
                                .temperature(0.1)
                                .build())
                        .build()),
        OLLAMA_QWEN3_14B( () ->
                OllamaChatModel.builder()
                .ollamaApi( OllamaApi.builder().baseUrl("http://localhost:11434").build() )
                .defaultOptions(OllamaOptions.builder()
                                .model("qwen3:14b")
                                .temperature(0.1)
                                .build())
                .build()),
        OLLAMA_KIMI_K2_5_CLOUD( () ->
                OllamaChatModel.builder()
                .ollamaApi( OllamaApi.builder().baseUrl("http://localhost:11434").build() )
                .defaultOptions(OllamaOptions.builder()
                                .model("kimi-k2.5:cloud")
                                .temperature(0.1)
                                .build())
                .build());
        ;

        public final Supplier<ChatModel> model;

        AiModel(  Supplier<ChatModel> model ) {
            this.model = model;
        }
    }

    private static final String BASE_SYSTEM_PROMPT = """
            You are a helpful and friendly customer service assistant for Sinsay, a fashion brand.

            Your responsibilities:
            - Answer questions about returns, complaints, products, store locations, payment options
            - Detect when a user wants to return a product or file a complaint
            - Use the show_return_form tool when you detect clear return or complaint intent
            - Analyze submitted form data and product photos against Sinsay's policies
            - Respond in the user's language (Polish if they write in Polish, English if they write in English)

            On the VERY FIRST interaction: Send a short, friendly welcome message in Polish:
            "Cześć! Jestem Twoim wirtualnym asystentem Sinsay. W czym mogę Ci dziś pomóc?"

            Intent detection:
            - Call show_return_form(type: "return") when user says: "chcę zwrócić", "zwrot", "oddać", "nie pasuje", "want to return", "return item", etc.
            - Call show_return_form(type: "complaint") when user says: "reklamacja", "reklamować", "wadliwy", "uszkodzony", "defekt", "zepsuty", "complaint", "defective", etc.
            - When intent is ambiguous, ask ONE clarifying question before showing the form.
            - Do NOT show the form until you are confident about the intent.

            After receiving form submission with a photo:
            - Analyze the photo and form data carefully against Sinsay's policies below
            - For returns: check for original tags, signs of use, verify the 30-day return window is applicable
            - For complaints: identify the defect type, assess whether it is manufacturing vs user-caused damage, check 2-year complaint window
            - Verdict format:
              1. Start with a prominent conclusion line: "Zwrot możliwy ✓" / "Zwrot niemożliwy ✗" / "Reklamacja uzasadniona ✓" / "Reklamacja nieuzasadniona ✗"
              2. Then 2-4 sentences justifying the decision, citing the specific applicable policy rule
              3. Then one recommended next step for the customer
            - If the photo is blurry, too dark, or cannot be properly analyzed: politely ask the user to send a clearer image before giving a verdict

            IMPORTANT: Ground all policy answers in the provided policy documents. Never fabricate policies not found in the documents.
            """;

    private final MemorySaver saver = new MemorySaver();
    private final AiModel primaryModel;
    private final PolicyService policyService;
    private final SinsayTools sinsayTools;

    public AGUIAgentExecutor() {
        this(null, null, null);
    }

    public AGUIAgentExecutor(AiModel primaryModel) {
        this(primaryModel, null, null);
    }

    public AGUIAgentExecutor(AiModel primaryModel, PolicyService policyService, SinsayTools sinsayTools) {
        this.primaryModel = primaryModel;
        this.policyService = policyService;
        this.sinsayTools = sinsayTools;
    }

    /**
     * Builds the system prompt by combining the base instructions with relevant policy documents.
     *
     * @param intent "return", "complaint", or "" for general conversation
     * @return assembled system prompt string
     */
    String buildSystemPrompt(String intent) {
        String policies = policyService != null ? policyService.getPoliciesForIntent(intent) : "";
        if (policies.isBlank()) {
            return BASE_SYSTEM_PROMPT;
        }
        return BASE_SYSTEM_PROMPT + """

                --- POLICY DOCUMENTS ---
                The following Sinsay policy documents apply to this conversation. Use them to ground your answers:

                """ + policies;
    }

    /**
     * Simple keyword-based intent detection from the user's latest message.
     * Used to pre-load relevant policy documents into the system prompt.
     */
    private String detectIntent(String userMessage) {
        String lower = userMessage.toLowerCase();
        if (lower.contains("zwrot") || lower.contains("zwrócić") || lower.contains("oddać")
                || lower.contains("return") || lower.contains("nie pasuje")) {
            return "return";
        }
        if (lower.contains("reklamacja") || lower.contains("reklamować") || lower.contains("wadliwy")
                || lower.contains("uszkodzony") || lower.contains("defekt") || lower.contains("zepsuty")
                || lower.contains("complaint") || lower.contains("defective")) {
            return "complaint";
        }
        return "";
    }

    String getEnv(String name) {
        return System.getenv(name);
    }

    ChatModel resolveModel() {
        // 1. Try configured primary model
        if (primaryModel != null) {
            try {
                var model = primaryModel.model.get();
                log.info("Using configured primary model: {}", primaryModel.name());
                return model;
            } catch (Exception e) {
                log.warn("Primary model {} failed to initialize: {}, falling back...", primaryModel.name(), e.getMessage());
            }
        }

        // 2. Fallback: OpenAI if API key present
        if (getEnv("OPENAI_API_KEY") != null) {
            try {
                log.info("Falling back to OPENAI_GPT_4O_MINI");
                return AiModel.OPENAI_GPT_4O_MINI.model.get();
            } catch (Exception e) {
                log.warn("OpenAI fallback failed: {}", e.getMessage());
            }
        }

        // 3. Fallback: GitHub Models if token present
        if (getEnv("GITHUB_MODELS_TOKEN") != null) {
            try {
                log.info("Falling back to GITHUB_MODELS_GPT_4O_MINI");
                return AiModel.GITHUB_MODELS_GPT_4O_MINI.model.get();
            } catch (Exception e) {
                log.warn("GitHub Models fallback failed: {}", e.getMessage());
            }
        }

        // 4. Last resort: Ollama qwen2.5
        log.info("Falling back to OLLAMA_QWEN2_5_7B");
        return AiModel.OLLAMA_QWEN2_5_7B.model.get();
    }

    @Override
    protected GraphData buildStateGraph() throws GraphStateException {

        var model = resolveModel();
        var tools = sinsayTools != null ? sinsayTools : new SinsayTools();

        var agent = AgentExecutorEx.builder()
                .chatModel(model, true)
                .toolsFromObject(tools)
                .approvalOn("showReturnForm",
                        (nodeId, state) ->
                                InterruptionMetadata.builder(nodeId, state)
                                        .build()
                )
                .build();

        log.info("REPRESENTATION:\n{}",
                agent.getGraph(GraphRepresentation.Type.PLANTUML, "Agent Executor", false).content()
        );

        var compileConfig = CompileConfig.builder().checkpointSaver(saver).build();

        return new GraphData(agent.compile(compileConfig));
    }

    @Override
    protected Map<String, Object> buildGraphInput(AGUIType.RunAgentInput input) {

        var lastUserMessage = input.lastUserMessage()
                .map(AGUIMessage.TextMessage::content)
                .orElseThrow(() -> new IllegalStateException("last user message not found"));

        log.debug("LAST USER MESSAGE: {}", lastUserMessage);

        // Detect intent from the latest message to pre-load relevant policies
        String intent = detectIntent(lastUserMessage);
        var systemMessage = new SystemMessage(buildSystemPrompt(intent));

        return Map.of("messages", List.of(systemMessage, new UserMessage(lastUserMessage)));
    }

    @Override
    protected <State extends AgentState> List<Approval> onInterruption(AGUIType.RunAgentInput input, InterruptionMetadata<State> state ) {

        var messages = state.state().<List<Message>>value("messages")
                .orElseThrow( () -> new IllegalStateException("messages not found into given state"));

        return lastOf(messages)
                .flatMap(MessageUtil::asAssistantMessage)
                .filter(AssistantMessage::hasToolCalls)
                .map(AssistantMessage::getToolCalls)
                .map( toolCalls ->
                    toolCalls.stream().map( toolCall -> {
                        var id = toolCall.id().isBlank() ?
                                UUID.randomUUID().toString() :
                                toolCall.id();
                        return new Approval( id, toolCall.name(), toolCall.arguments() );
                    }).toList()
                )
                .orElseGet(List::of);

    }

    @Override
    protected Optional<String> nodeOutputToText(NodeOutput<? extends AgentState> output) {
        return Optional.empty();

        // TODO: support other than START or END in different way? (original code below throw error after Tool call)
        // if( output.isEND() | output.isSTART() ) {
        //     return Optional.empty();
        // }

        // throw new UnsupportedOperationException("not implemented yet");
    }


    public static void main( String[] argv ) throws Exception {

        var agent = new AGUIAgentExecutor();

        var input = new AGUIType.RunAgentInput(
                "thread1",
                "run1",
                null,
                List.of( AGUIMessage.userMessage( "msg1", """
                    Send a mail to bartolomeo.sorrentino@gmail.com with subjet AG-UI test and an empty body
                """ )),
                null,
                null,
                null );

        agent.run(input).subscribe( event -> log.trace( "{}", event) );

    }
}
