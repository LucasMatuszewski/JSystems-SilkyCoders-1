package com.silkycoders1.jsystemssilkycodders1.service;

import com.silkycoders1.jsystemssilkycodders1.dto.RequestType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiService {
    
    private final ChatModel chatModel;
    
    public AiService(ChatModel chatModel) {
        this.chatModel = chatModel;
        log.info("AiService initialized with ChatModel: {}", chatModel.getClass().getSimpleName());
    }
    
    public String buildSystemPrompt(RequestType requestType) {
        log.info("Building system prompt for RequestType: {}", requestType);
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a Senior Sinsay QA Specialist. Your role is to verify customer returns and complaints.\n\n");
        
        prompt.append("POLICY RULES:\n");
        if (requestType == RequestType.RETURN) {
            log.debug("Adding RETURN-specific policy rules");
            prompt.append("- Returns: 30-day window, item must be unused, receipt required\n");
            prompt.append("- Verify receipt authenticity and extract order/receipt ID and purchase date\n");
            prompt.append("- Match extracted information with user-provided data\n");
        } else {
            log.debug("Adding COMPLAINT-specific policy rules");
            prompt.append("- Complaints: 2-year statutory warranty, manufacturing defects only\n");
            prompt.append("- Analyze defect photos to classify defect type\n");
            prompt.append("- Distinguish between manufacturing defects and user-caused damage\n");
        }
        
        prompt.append("\nDEFECT TAXONOMY:\n");
        prompt.append("Valid defects (manufacturing): Seam slippage, pilling, color bleeding, barre, slub, manufacturing defects\n");
        prompt.append("Invalid (user damage): Scissors cut, bleach spot, wear-and-tear, user-caused damage\n");
        
        prompt.append("\nOUTPUT FORMAT:\n");
        prompt.append("Provide a structured JSON response: {\"status\": \"APPROVED\" or \"REJECTED\", \"reason\": \"...\"}\n");
        prompt.append("Then provide a conversational explanation in plain English.\n");
        prompt.append("If uncertain, reject with explanation of what's missing or unclear.\n");
        prompt.append("\nIMPORTANT: Start your response directly with the analysis. Do NOT add introductory phrases like \"Here's my analysis\" or \"Here's my response\". Begin immediately with the substantive content of your evaluation.\n");
        
        String promptText = prompt.toString();
        log.info("System prompt built successfully (length: {} characters)", promptText.length());
        log.debug("System prompt content: {}", promptText);
        return promptText;
    }
    
    public Flux<String> streamAnalysis(RequestType requestType, String userInput, List<Resource> imageResources) {
        log.info("=== STARTING STREAM ANALYSIS ===");
        log.info("RequestType: {}", requestType);
        log.info("UserInput: {}", userInput);
        log.info("ImageResources count: {}", imageResources != null ? imageResources.size() : 0);
        
        String systemPrompt = buildSystemPrompt(requestType);
        
        log.info("Creating message list...");
        List<Message> messages = new ArrayList<>();
        messages.add(new org.springframework.ai.chat.messages.SystemMessage(systemPrompt));
        log.debug("Added SystemMessage to messages list");
        
        // Convert Resources to Media objects
        List<Resource> safeImageResources = imageResources != null ? imageResources : new ArrayList<>();
        log.info("Converting {} image resource(s) to Media objects...", safeImageResources.size());
        List<Media> mediaList = safeImageResources.stream()
            .map(resource -> {
                try {
                    String filename = resource.getFilename();
                    log.debug("Converting resource to Media: {}", filename);
                    // Try to determine MIME type from filename, default to JPEG
                    org.springframework.util.MimeType mimeType = determineMimeType(resource);
                    log.debug("Determined MIME type for {}: {}", filename, mimeType);
                    Media media = new Media(mimeType, resource);
                    log.debug("Successfully created Media object for {}", filename);
                    return media;
                } catch (Exception e) {
                    log.error("Error converting resource {} to Media: {}", resource.getFilename(), e.getMessage(), e);
                    throw e;
                }
            })
            .collect(Collectors.toList());
        log.info("Successfully converted {} resource(s) to Media objects", mediaList.size());
        
        // Create UserMessage with text and media
        log.info("Creating UserMessage...");
        UserMessage userMessage;
        if (!mediaList.isEmpty()) {
            log.debug("Creating UserMessage with {} media attachment(s)", mediaList.size());
            userMessage = new UserMessage(userInput, mediaList);
        } else {
            log.debug("Creating UserMessage without media attachments");
            userMessage = new UserMessage(userInput);
        }
        messages.add(userMessage);
        log.info("UserMessage created and added to messages list (total messages: {})", messages.size());
        
        log.info("Creating Prompt object...");
        Prompt prompt = new Prompt(messages);
        log.info("Prompt created successfully");
        
        log.info("Starting chatModel.stream() call...");
        AtomicInteger chunkCounter = new AtomicInteger(0);
        AtomicInteger totalContentLength = new AtomicInteger(0);
        
        // Stream the response and split into chunks for better streaming
        return chatModel.stream(prompt)
            .doOnSubscribe(subscription -> log.info("ChatModel stream subscription started - waiting for model response..."))
            .doOnNext(response -> log.debug("Received ChatResponse from model"))
            .map(ChatResponse::getResult)
            .doOnNext(result -> log.debug("Extracted result from ChatResponse"))
            .map(result -> result.getOutput().getContent())
            .doOnNext(content -> {
                int length = content != null ? content.length() : 0;
                log.debug("Extracted content from output (length: {} characters)", length);
            })
            .filter(content -> {
                boolean isValid = content != null && !content.isEmpty();
                if (!isValid) {
                    log.debug("Filtered out empty or null content");
                }
                return isValid;
            })
            .doOnNext(content -> {
                int chunkNum = chunkCounter.incrementAndGet();
                totalContentLength.addAndGet(content.length());
                log.info("Processing content chunk #{} (length: {} chars, total so far: {} chars)", 
                    chunkNum, content.length(), totalContentLength.get());
            })
            .flatMap(content -> {
                log.debug("Splitting content into smaller chunks (chunk size: 10)");
                // Split content into smaller chunks for smoother streaming
                List<String> chunks = new ArrayList<>();
                int chunkSize = 10; // Characters per chunk
                for (int i = 0; i < content.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, content.length());
                    chunks.add(content.substring(i, end));
                }
                log.debug("Split content into {} smaller chunks", chunks.size());
                return Flux.fromIterable(chunks);
            })
            .doOnNext(chunk -> log.trace("Streaming chunk: '{}'", chunk))
            .doOnError(error -> {
                log.error("ERROR in chatModel stream: {}", error.getMessage(), error);
                log.error("Error class: {}", error.getClass().getName());
                if (error.getCause() != null) {
                    log.error("Error cause: {}", error.getCause().getMessage());
                }
            })
            .doOnComplete(() -> {
                log.info("=== STREAM ANALYSIS COMPLETED SUCCESSFULLY ===");
                log.info("Total chunks processed: {}", chunkCounter.get());
                log.info("Total content length: {} characters", totalContentLength.get());
            })
            .doOnCancel(() -> {
                log.warn("=== STREAM ANALYSIS CANCELLED ===");
                log.warn("Chunks processed before cancellation: {}", chunkCounter.get());
            });
    }
    
    /**
     * Determines MIME type from resource filename, defaults to JPEG
     */
    private org.springframework.util.MimeType determineMimeType(Resource resource) {
        String filename = resource.getFilename();
        log.debug("Determining MIME type for resource: {}", filename);
        
        if (filename == null) {
            log.debug("Filename is null, defaulting to JPEG");
            return MimeTypeUtils.IMAGE_JPEG;
        }
        
        String lowerFilename = filename.toLowerCase();
        org.springframework.util.MimeType mimeType;
        
        if (lowerFilename.endsWith(".png")) {
            mimeType = MimeTypeUtils.IMAGE_PNG;
            log.debug("Detected PNG MIME type");
        } else if (lowerFilename.endsWith(".gif")) {
            mimeType = MimeTypeUtils.IMAGE_GIF;
            log.debug("Detected GIF MIME type");
        } else if (lowerFilename.endsWith(".webp")) {
            mimeType = MimeTypeUtils.parseMimeType("image/webp");
            log.debug("Detected WEBP MIME type");
        } else {
            // Default to JPEG for jpg, jpeg, or unknown
            mimeType = MimeTypeUtils.IMAGE_JPEG;
            log.debug("Defaulting to JPEG MIME type");
        }
        
        log.debug("Final MIME type for {}: {}", filename, mimeType);
        return mimeType;
    }
}
