package com.silkycoders1.jsystemssilkycodders1.controller.dto;

import java.util.List;

public record ChatMessage(String role, String content, List<ImagePayload> images) {
}
