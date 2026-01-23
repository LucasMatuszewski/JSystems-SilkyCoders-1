package com.silkycoders1.jsystemssilkycodders1.controller;

import java.util.List;

public record ChatRequest(
    List<MessageDto> messages,
    String orderId,
    String intent,
    String description
) {}
