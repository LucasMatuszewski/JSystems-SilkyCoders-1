package com.silkycoders1.jsystemssilkycodders1.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Table("form_submissions")
public class FormSubmission {
    @Id
    private UUID id;
    private UUID sessionId;
    private String intent;
    private String productName;
    private String description;
    private String photoPath;
    private String verdict;
    private LocalDateTime submittedAt;
    private LocalDateTime verdictAt;
}
