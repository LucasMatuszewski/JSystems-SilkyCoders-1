package com.sinsay.controller.dto;

import com.sinsay.model.Role;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for chat message in API responses.
 */
public record ChatMessageDto(
        UUID id,
        Role role,
        String content,
        Integer sequenceNumber,
        LocalDateTime createdAt
) {
}
