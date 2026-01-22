package com.silkycoders1.jsystemssilkycodders1.controller;

import com.silkycoders1.jsystemssilkycodders1.dto.RequestType;
import com.silkycoders1.jsystemssilkycodders1.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final AiService aiService;
    
    public ChatController(AiService aiService) {
        this.aiService = aiService;
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Flux<String>> chat(
            @RequestParam("conversationId") String conversationId,
            @RequestParam(value = "requestType") String requestType,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        
        log.info("=== CHAT REQUEST RECEIVED ===");
        log.info("ConversationId: {}", conversationId);
        log.info("RequestType: {}", requestType);
        log.info("Message: {}", message != null ? message : "(default)");
        log.info("Images count: {}", images != null ? images.length : 0);
        
        RequestType type;
        try {
            type = RequestType.valueOf(requestType);
            log.info("Parsed RequestType: {}", type);
        } catch (IllegalArgumentException e) {
            log.error("Invalid RequestType: {}", requestType, e);
            return ResponseEntity.badRequest().build();
        }
        
        // Convert MultipartFile to Resource
        List<Resource> imageResources = new ArrayList<>();
        if (images != null) {
            log.info("Processing {} image(s)...", images.length);
            for (int i = 0; i < images.length; i++) {
                MultipartFile file = images[i];
                try {
                    log.debug("Processing image {}: {} (size: {} bytes)", i + 1, file.getOriginalFilename(), file.getSize());
                    ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    };
                    imageResources.add(resource);
                    log.debug("Successfully converted image {} to Resource", i + 1);
                } catch (IOException e) {
                    log.error("Failed to process image {}: {}", file.getOriginalFilename(), e.getMessage(), e);
                    return ResponseEntity.badRequest().build();
                }
            }
            log.info("Successfully processed {} image(s)", imageResources.size());
        } else {
            log.info("No images provided in request");
        }
        
        String userInput = message != null ? message : "Please analyze the uploaded images.";
        log.info("User input: {}", userInput);
        
        log.info("Starting AI analysis stream...");
        // Stream AI response and format to Vercel Protocol
        Flux<String> formattedStream = aiService.streamAnalysis(type, userInput, imageResources)
            .doOnSubscribe(subscription -> log.info("AI stream subscription started"))
            .doOnNext(chunk -> log.debug("Received chunk from AI service: {} characters", chunk.length()))
            .doOnError(error -> log.error("Error in AI stream: {}", error.getMessage(), error))
            .doOnComplete(() -> log.info("AI stream completed successfully"))
            .doOnCancel(() -> log.warn("AI stream was cancelled"))
            .map(chunk -> {
                // Properly escape the chunk: escape backslashes first, then quotes
                String escaped = chunk.replace("\\", "\\\\").replace("\"", "\\\"");
                // Format as Vercel protocol: 0:"text"\n\n
                // Note: Spring WebFlux automatically adds "data: " prefix for TEXT_EVENT_STREAM
                String formatted = "0:\"" + escaped + "\"\n\n";
                log.debug("Formatted chunk for Vercel protocol: {} characters, content preview: {}", 
                    formatted.length(), formatted.length() > 50 ? formatted.substring(0, 50) + "..." : formatted);
                return formatted;
            })
            .doOnNext(formatted -> log.debug("Emitting formatted chunk to client: {} chars", formatted.length()))
            .doOnComplete(() -> log.info("All formatted chunks emitted to client"))
            .doOnError(error -> log.error("Error emitting formatted chunks: {}", error.getMessage(), error));
        
        log.info("Returning ResponseEntity with streaming response");
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("X-Accel-Buffering", "no") // Disable nginx buffering if present
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(formattedStream);
    }
}
