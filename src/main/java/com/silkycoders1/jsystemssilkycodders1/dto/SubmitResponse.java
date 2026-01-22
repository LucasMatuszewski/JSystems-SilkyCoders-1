package com.silkycoders1.jsystemssilkycodders1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitResponse {
    private String conversationId;
    private String status; // "VALID" or "REJECTED"
    private String message;
}
