package com.silkycoders1.jsystemssilkycodders1.service;

import com.silkycoders1.jsystemssilkycodders1.domain.ChatMessage;
import com.silkycoders1.jsystemssilkycodders1.domain.FormSubmission;
import com.silkycoders1.jsystemssilkycodders1.domain.Session;
import com.silkycoders1.jsystemssilkycodders1.repository.ChatMessageRepository;
import com.silkycoders1.jsystemssilkycodders1.repository.FormSubmissionRepository;
import com.silkycoders1.jsystemssilkycodders1.repository.SessionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final FormSubmissionRepository formSubmissionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public SessionService(SessionRepository sessionRepository,
                          FormSubmissionRepository formSubmissionRepository,
                          ChatMessageRepository chatMessageRepository) {
        this.sessionRepository = sessionRepository;
        this.formSubmissionRepository = formSubmissionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public Mono<Session> createSession() {
        var session = Session.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return sessionRepository.save(session);
    }

    public Mono<Session> getSession(UUID id) {
        return sessionRepository.findById(id);
    }

    public Mono<ChatMessage> saveMessage(UUID sessionId, String role, String content) {
        var message = ChatMessage.builder()
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        return chatMessageRepository.save(message);
    }

    public Mono<FormSubmission> saveFormSubmission(UUID sessionId, String intent,
                                                    String productName, String description) {
        var submission = FormSubmission.builder()
                .sessionId(sessionId)
                .intent(intent)
                .productName(productName)
                .description(description)
                .submittedAt(LocalDateTime.now())
                .build();
        return formSubmissionRepository.save(submission);
    }

    public Mono<FormSubmission> updateVerdict(UUID submissionId, String verdict) {
        return formSubmissionRepository.findById(submissionId)
                .flatMap(submission -> {
                    submission.setVerdict(verdict);
                    submission.setVerdictAt(LocalDateTime.now());
                    return formSubmissionRepository.save(submission);
                });
    }

    public Flux<ChatMessage> getTranscript(UUID sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAt(sessionId);
    }
}
