package com.sinsay.service.dto;

import com.sinsay.model.Intent;

import java.util.UUID;

/**
 * Response DTO for initial analysis.
 *
 * @param sessionId The ID of the created session
 * @param message   The AI's initial response message
 */
public record AnalysisResponse(UUID sessionId, String message) {
}
