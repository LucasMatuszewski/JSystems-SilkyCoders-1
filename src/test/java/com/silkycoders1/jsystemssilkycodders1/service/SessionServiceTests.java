package com.silkycoders1.jsystemssilkycodders1.service;

import com.silkycoders1.jsystemssilkycodders1.domain.ChatMessage;
import com.silkycoders1.jsystemssilkycodders1.domain.FormSubmission;
import com.silkycoders1.jsystemssilkycodders1.domain.Session;
import com.silkycoders1.jsystemssilkycodders1.repository.ChatMessageRepository;
import com.silkycoders1.jsystemssilkycodders1.repository.FormSubmissionRepository;
import com.silkycoders1.jsystemssilkycodders1.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTests {

    @Mock private SessionRepository sessionRepository;
    @Mock private FormSubmissionRepository formSubmissionRepository;
    @Mock private ChatMessageRepository chatMessageRepository;

    private SessionService sut;

    @BeforeEach
    void setUp() {
        sut = new SessionService(sessionRepository, formSubmissionRepository, chatMessageRepository);
    }

    @Test
    void shouldCreateSession() {
        var session = Session.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(sessionRepository.save(any())).thenReturn(Mono.just(session));

        StepVerifier.create(sut.createSession())
                .expectNextMatches(s -> s.getId() != null)
                .verifyComplete();
    }

    @Test
    void shouldSaveChatMessage() {
        var sessionId = UUID.randomUUID();
        var message = ChatMessage.builder()
                .id(UUID.randomUUID())
                .sessionId(sessionId)
                .role("user")
                .content("Hello")
                .createdAt(LocalDateTime.now())
                .build();
        when(chatMessageRepository.save(any())).thenReturn(Mono.just(message));

        StepVerifier.create(sut.saveMessage(sessionId, "user", "Hello"))
                .expectNextMatches(m -> m.getContent().equals("Hello") && m.getRole().equals("user"))
                .verifyComplete();
    }

    @Test
    void shouldUpdateVerdict() {
        var submissionId = UUID.randomUUID();
        var submission = FormSubmission.builder()
                .id(submissionId)
                .sessionId(UUID.randomUUID())
                .intent("return")
                .productName("Shirt")
                .description("Does not fit")
                .submittedAt(LocalDateTime.now())
                .build();
        var updatedSubmission = FormSubmission.builder()
                .id(submissionId)
                .sessionId(submission.getSessionId())
                .intent("return")
                .productName("Shirt")
                .description("Does not fit")
                .submittedAt(submission.getSubmittedAt())
                .verdict("Zwrot mozliwy")
                .verdictAt(LocalDateTime.now())
                .build();
        when(formSubmissionRepository.findById(submissionId)).thenReturn(Mono.just(submission));
        when(formSubmissionRepository.save(any())).thenReturn(Mono.just(updatedSubmission));

        StepVerifier.create(sut.updateVerdict(submissionId, "Zwrot mozliwy"))
                .expectNextMatches(s -> "Zwrot mozliwy".equals(s.getVerdict()))
                .verifyComplete();
    }
}
