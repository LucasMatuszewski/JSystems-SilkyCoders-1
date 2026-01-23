package com.silkycoders1.jsystemssilkycodders1.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID sessionId;

    private String role; // "user", "assistant", "system"
    
    @Column(length = 10000)
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();
    
    public ChatMessage(UUID sessionId, String role, String content) {
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
    }
}
