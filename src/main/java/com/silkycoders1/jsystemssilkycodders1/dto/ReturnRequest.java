package com.silkycoders1.jsystemssilkycodders1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class ReturnRequest {
    @NotNull(message = "Request type is required")
    private RequestType requestType;
    
    @NotBlank(message = "Order/Receipt ID is required")
    private String orderReceiptId;
    
    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;
    
    // Only for RETURN
    private Boolean unused;
    
    // Only for COMPLAINT
    private String defectDescription;
    
    // Receipt for RETURN, defect photos for COMPLAINT
    private MultipartFile[] images;
}
