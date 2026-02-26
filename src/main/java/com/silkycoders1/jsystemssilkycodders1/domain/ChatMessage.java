package com.silkycoders1.jsystemssilkycodders1.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Table("chat_messages")
public class ChatMessage {
    @Id
    private UUID id;
    private UUID sessionId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
