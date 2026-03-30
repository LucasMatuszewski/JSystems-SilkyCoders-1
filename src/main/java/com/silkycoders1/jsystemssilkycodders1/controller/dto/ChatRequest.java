package com.silkycoders1.jsystemssilkycodders1.controller.dto;

import java.time.LocalDate;
import java.util.List;

public record ChatRequest(
        List<ChatMessage> messages,
        String intent,
        String orderNumber,
        LocalDate purchaseDate,
        String description
) {
}
