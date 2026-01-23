package com.silkycoders1.jsystemssilkycodders1.repository;

import com.silkycoders1.jsystemssilkycodders1.model.VerificationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface VerificationSessionRepository extends JpaRepository<VerificationSession, UUID> {
    VerificationSession findTopByOrderIdOrderByCreatedAtDesc(String orderId);
}
