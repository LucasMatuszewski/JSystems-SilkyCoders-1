package org.bsc.langgraph4j.agui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.ai.content.Media;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

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
                        .ollamaApi(OllamaApi.builder()
                                .baseUrl("http://localhost:11434")
                                .webClientBuilder(WebClient.builder()
                                        .clientConnector(new ReactorClientHttpConnector(
                                                HttpClient.create().responseTimeout(Duration.ofSeconds(30))
                                        )))
                                .build())
                        .defaultOptions(OllamaOptions.builder()
                                .model("qwen2.5:7b")
                                .temperature(0.1)
                                .build())
                        .build()),
        OLLAMA_QWEN3_14B( () ->
                OllamaChatModel.builder()
                        .ollamaApi(OllamaApi.builder()
                                .baseUrl("http://localhost:11434")
                                .webClientBuilder(WebClient.builder()
                                        .clientConnector(new ReactorClientHttpConnector(
                                                HttpClient.create().responseTimeout(Duration.ofSeconds(30))
                                        )))
                                .build())
                        .defaultOptions(OllamaOptions.builder()
                                .model("qwen3:14b")
                                .temperature(0.1)
                                .build())
                        .build()),
        OLLAMA_KIMI_K2_5_CLOUD( () ->
                OllamaChatModel.builder()
                        .ollamaApi(OllamaApi.builder()
                                .baseUrl("http://localhost:11434")
                                .webClientBuilder(WebClient.builder()
                                        .clientConnector(new ReactorClientHttpConnector(
                                                HttpClient.create().responseTimeout(Duration.ofSeconds(30))
                                        )))
                                .build())
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

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parsed form submission data from a showReturnForm tool result.
     */
    record FormSubmissionData(String productName, String type, String description, String photo, String photoMimeType) {}

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

    /**
     * Truncates a value that may contain a large base64 payload for safe logging.
     * Values longer than 80 characters are replaced with the first 80 chars followed
     * by a size annotation, e.g. {@code "/9j/4AAQ...[45231 chars]"}.
     * This prevents multi-kilobyte base64 image data from polluting log output.
     *
     * @param value the string to truncate; may be null
     * @return the original value if {@code null} or <= 80 chars; a truncated form otherwise
     */
    static String truncateBase64(String value) {
        if (value == null) return null;
        int len = value.length();
        if (len <= 80) return value;
        return value.substring(0, 80) + "...[" + len + " chars]";
    }

    /**
     * Resizes an image to at most 800×800 pixels (maintaining aspect ratio) to prevent
     * exceeding the Ollama model context window. If the image is already within the
     * threshold, or if decoding fails, the original bytes are returned unchanged.
     *
     * @param imageBytes raw bytes of the image (JPEG or PNG)
     * @param mimeType   MIME type string used to choose the output format
     * @return resized image bytes, or the original bytes if resize was not needed or failed
     */
    static byte[] resizeImageIfNeeded(byte[] imageBytes, String mimeType) {
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (original == null) {
                log.warn("Could not decode image for resize — using original bytes");
                return imageBytes;
            }
            int w = original.getWidth();
            int h = original.getHeight();
            int maxDim = 800;
            if (w <= maxDim && h <= maxDim) {
                return imageBytes;
            }
            double scale = (double) maxDim / Math.max(w, h);
            int newW = (int) (w * scale);
            int newH = (int) (h * scale);
            log.debug("Resizing image from {}x{} to {}x{} before sending to model", w, h, newW, newH);

            BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
            var g = resized.createGraphics();
            g.drawImage(original.getScaledInstance(newW, newH, Image.SCALE_SMOOTH), 0, 0, null);
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String format = mimeType.contains("png") ? "png" : "jpg";
            ImageIO.write(resized, format, out);
            return out.toByteArray();
        } catch (Exception e) {
            log.warn("Image resize failed, using original: {}", e.getMessage());
            return imageBytes;
        }
    }

    String getEnv(String name) {
        return System.getenv(name);
    }

    /**
     * Builds a FallbackChatModel chain: primary model first, then OpenAI (if key present),
     * then GitHub Models (if token present), then Ollama qwen2.5 as last resort.
     *
     * FallbackChatModel retries transient errors (broken pipe, connection refused, timeout)
     * once after 1 second, then falls through to the next model in the chain.
     * This handles Ollama cold-start failures and intermittent network issues at RUNTIME,
     * not just at startup.
     */
    ChatModel resolveModel() {
        var models = new java.util.ArrayList<ChatModel>();

        // 1. Configured primary model
        if (primaryModel != null) {
            try {
                var model = primaryModel.model.get();
                log.info("Primary model configured: {}", primaryModel.name());
                models.add(model);
            } catch (Exception e) {
                log.warn("Primary model {} failed to initialize: {}", primaryModel.name(), e.getMessage());
            }
        }

        // 2. OpenAI if API key present
        if (getEnv("OPENAI_API_KEY") != null) {
            try {
                models.add(AiModel.OPENAI_GPT_4O_MINI.model.get());
                log.info("OpenAI fallback added to chain");
            } catch (Exception e) {
                log.warn("OpenAI fallback could not be added: {}", e.getMessage());
            }
        }

        // 3. GitHub Models if token present
        if (getEnv("GITHUB_MODELS_TOKEN") != null) {
            try {
                models.add(AiModel.GITHUB_MODELS_GPT_4O_MINI.model.get());
                log.info("GitHub Models fallback added to chain");
            } catch (Exception e) {
                log.warn("GitHub Models fallback could not be added: {}", e.getMessage());
            }
        }

        // 4. Ollama qwen2.5 as last resort (always available when Ollama is running)
        models.add(AiModel.OLLAMA_QWEN2_5_7B.model.get());
        log.info("Ollama qwen2.5 added as final fallback. Fallback chain length: {}", models.size());

        return new FallbackChatModel(models);
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

        log.debug("LAST USER MESSAGE: {}", truncateBase64(lastUserMessage));

        // Check if any messages contain a form submission result (JSON with photo)
        Optional<FormSubmissionData> formSubmission = extractFormSubmission(input);

        // Use intent from form data if available, otherwise detect from keywords
        String intent = formSubmission.map(FormSubmissionData::type)
                .orElseGet(() -> detectIntent(lastUserMessage));
        var systemMessage = new SystemMessage(buildSystemPrompt(intent));

        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);

        // If we have a form submission with a photo, add the image as a multimodal message
        formSubmission.ifPresent(data -> {
            if (data.photo() != null && !data.photo().isBlank()) {
                try {
                    var rawBytes = Base64.getDecoder().decode(data.photo());
                    var imageBytes = resizeImageIfNeeded(rawBytes, data.photoMimeType());
                    var media = new Media(
                            MimeType.valueOf(data.photoMimeType()),
                            new ByteArrayResource(imageBytes)
                    );
                    var analysisPrompt = String.format(
                            "Przeanalizuj zdjęcie produktu w kontekście %s. " +
                            "Produkt: %s. Opis klienta: %s. " +
                            "Sprawdź czy zdjęcie jest wyraźne i czy widać stan produktu. " +
                            "Jeśli zdjęcie jest niewyraźne, poproś o lepsze zdjęcie.",
                            data.type().equals("return") ? "zwrotu" : "reklamacji",
                            data.productName(),
                            data.description()
                    );
                    messages.add(UserMessage.builder()
                            .text(analysisPrompt)
                            .media(media)
                            .build());
                    log.info("Added multimodal image message for {} analysis", data.type());
                } catch (IllegalArgumentException e) {
                    log.warn("Could not decode base64 photo for multimodal analysis: {}", e.getMessage());
                }
            }
        });

        messages.add(new UserMessage(lastUserMessage));
        return Map.of("messages", messages);
    }

    /**
     * Extracts form submission data from ResultMessage entries in the input.
     * Looks for a ResultMessage from the showReturnForm tool that contains
     * form JSON with productName, type, description, and optionally photo fields.
     *
     * @param input the AG-UI run input containing all messages
     * @return parsed form submission data if found, empty otherwise
     */
    Optional<FormSubmissionData> extractFormSubmission(AGUIType.RunAgentInput input) {
        return input.messages().stream()
                .filter(msg -> msg instanceof AGUIMessage.ResultMessage)
                .map(msg -> (AGUIMessage.ResultMessage) msg)
                .filter(msg -> msg.result() != null && msg.result().startsWith("{"))
                .flatMap(msg -> {
                    try {
                        JsonNode node = objectMapper.readTree(msg.result());
                        if (node.has("productName")) {
                            return Stream.of(new FormSubmissionData(
                                    node.path("productName").asText(),
                                    node.path("type").asText("return"),
                                    node.path("description").asText(),
                                    node.has("photo") ? node.path("photo").asText() : null,
                                    node.path("photoMimeType").asText("image/jpeg")
                            ));
                        }
                    } catch (Exception e) {
                        log.debug("Message content is not form JSON: {}", e.getMessage());
                    }
                    return Stream.empty();
                })
                .findFirst();
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
