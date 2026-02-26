package com.silkycoders1.jsystemssilkycodders1.repository;

import com.silkycoders1.jsystemssilkycodders1.domain.FormSubmission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface FormSubmissionRepository extends ReactiveCrudRepository<FormSubmission, UUID> {
    Flux<FormSubmission> findBySessionId(UUID sessionId);
}
