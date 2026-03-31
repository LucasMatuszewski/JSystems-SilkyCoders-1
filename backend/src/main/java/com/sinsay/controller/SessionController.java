package com.sinsay.controller;

import com.sinsay.controller.dto.SessionResponse;
import com.sinsay.model.ChatMessage;
import com.sinsay.model.Intent;
import com.sinsay.model.Session;
import com.sinsay.repository.ChatMessageRepository;
import com.sinsay.repository.SessionRepository;
import com.sinsay.service.AnalysisService;
import com.sinsay.service.dto.AnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Controller for session creation and loading.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final AnalysisService analysisService;
    private final SessionRepository sessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    // Allowed image MIME types
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    // Maximum image size: 10MB
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    /**
     * Create a new session by analyzing the form data and image.
     *
     * @param intent       The intent (RETURN or COMPLAINT)
     * @param orderNumber  The order number
     * @param productName  The product name
     * @param description  The description
     * @param image        The product image
     * @return AnalysisResponse with sessionId and message
     */
    @PostMapping
    public ResponseEntity<?> createSession(
            @RequestParam("intent") String intent,
            @RequestParam("orderNumber") String orderNumber,
            @RequestParam("productName") String productName,
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile image
    ) {
        log.info("Creating session: intent={}, order={}, product={}", intent, orderNumber, productName);

        // Validate intent
        if (intent == null || intent.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("intent is required");
        }

        try {
            Intent parsedIntent = Intent.valueOf(intent.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("intent must be either RETURN or COMPLAINT");
        }

        // Validate required fields
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("orderNumber is required");
        }

        if (productName == null || productName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("productName is required");
        }

        if (description == null || description.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("description is required");
        }

        // Validate image
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body("image is required");
        }

        // Validate image MIME type
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return ResponseEntity.badRequest().body(
                    "image must be one of: JPEG, PNG, WebP, or GIF"
            );
        }

        // Validate image size
        if (image.getSize() > MAX_IMAGE_SIZE) {
            return ResponseEntity.badRequest().body(
                    "image size must not exceed 10MB"
            );
        }

        try {
            // Parse intent (we know it's valid after the check above)
            Intent parsedIntent = Intent.valueOf(intent.toUpperCase());

            // Call analysis service
            AnalysisResponse response = analysisService.analyzeAndCreateSession(
                    parsedIntent,
                    orderNumber,
                    productName,
                    description,
                    image.getBytes(),
                    contentType
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating session", e);
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    /**
     * Load a session by ID with its message history.
     *
     * @param id The session ID
     * @return SessionResponse with session and messages
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSession(@PathVariable UUID id) {
        log.info("Loading session: {}", id);

        return sessionRepository.findById(id)
                .map(session -> {
                    List<ChatMessage> messages = chatMessageRepository
                            .findBySessionIdOrderBySequenceNumberAsc(id);
                    SessionResponse response = new SessionResponse(session, messages);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
