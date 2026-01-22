package com.silkycoders1.jsystemssilkycodders1.controller;

import com.silkycoders1.jsystemssilkycodders1.dto.ReturnRequest;
import com.silkycoders1.jsystemssilkycodders1.dto.SubmitResponse;
import com.silkycoders1.jsystemssilkycodders1.service.ReturnPolicyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
@CrossOrigin(origins = "*")
public class ReturnController {
    
    private final ReturnPolicyService policyService;
    
    public ReturnController(ReturnPolicyService policyService) {
        this.policyService = policyService;
    }
    
    @PostMapping("/submit")
    public ResponseEntity<SubmitResponse> submitRequest(
            @RequestParam("requestType") String requestType,
            @RequestParam("orderReceiptId") String orderReceiptId,
            @RequestParam("purchaseDate") String purchaseDate,
            @RequestParam(value = "unused", required = false) Boolean unused,
            @RequestParam(value = "defectDescription", required = false) String defectDescription,
            @RequestParam("images") MultipartFile[] images) {
        
        // Convert to DTO
        ReturnRequest request = new ReturnRequest();
        request.setRequestType(com.silkycoders1.jsystemssilkycodders1.dto.RequestType.valueOf(requestType));
        request.setOrderReceiptId(orderReceiptId);
        request.setPurchaseDate(java.time.LocalDate.parse(purchaseDate));
        request.setUnused(unused);
        request.setDefectDescription(defectDescription);
        request.setImages(images);
        
        // Validate policy
        var validationResult = policyService.validatePolicy(
            request.getRequestType(), 
            request.getPurchaseDate()
        );
        
        if (!validationResult.valid()) {
            return ResponseEntity.ok(new SubmitResponse(
                null,
                "REJECTED",
                validationResult.message()
            ));
        }
        
        // Generate conversation ID for valid requests
        String conversationId = UUID.randomUUID().toString();
        
        return ResponseEntity.ok(new SubmitResponse(
            conversationId,
            "VALID",
            "Request validated. Proceeding to verification."
        ));
    }
}
