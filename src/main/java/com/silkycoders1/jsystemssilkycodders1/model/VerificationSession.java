package com.silkycoders1.jsystemssilkycodders1.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class VerificationSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String orderId;
    private String intent; // "RETURN" or "COMPLAINT"
    
    @Column(length = 5000)
    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();
    
    private String verdict;
    
    public VerificationSession(String orderId, String intent, String description) {
        this.orderId = orderId;
        this.intent = intent;
        this.description = description;
    }
}
