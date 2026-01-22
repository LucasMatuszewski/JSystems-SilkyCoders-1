package com.silkycoders1.jsystemssilkycodders1.service;

import com.silkycoders1.jsystemssilkycodders1.dto.RequestType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ReturnPolicyService {
    
    private static final int RETURN_DAYS_LIMIT = 30;
    private static final int COMPLAINT_DAYS_LIMIT = 730; // 2 years
    
    public PolicyValidationResult validatePolicy(RequestType requestType, LocalDate purchaseDate) {
        if (purchaseDate == null) {
            return new PolicyValidationResult(false, "Purchase date is required");
        }
        
        LocalDate now = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(purchaseDate, now);
        
        if (daysBetween < 0) {
            return new PolicyValidationResult(false, "Purchase date cannot be in the future");
        }
        
        if (requestType == RequestType.RETURN) {
            if (daysBetween > RETURN_DAYS_LIMIT) {
                return new PolicyValidationResult(
                    false, 
                    String.format("Return request rejected: Purchase date is more than %d days ago. The return window has expired.", RETURN_DAYS_LIMIT)
                );
            }
        } else if (requestType == RequestType.COMPLAINT) {
            if (daysBetween > COMPLAINT_DAYS_LIMIT) {
                return new PolicyValidationResult(
                    false,
                    String.format("Complaint request rejected: Purchase date is more than %d days (2 years) ago. The statutory warranty period has expired.", COMPLAINT_DAYS_LIMIT)
                );
            }
        }
        
        return new PolicyValidationResult(true, "Policy validation passed");
    }
    
    public record PolicyValidationResult(boolean valid, String message) {}
}
