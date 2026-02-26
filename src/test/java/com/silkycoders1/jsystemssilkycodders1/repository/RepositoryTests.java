package com.silkycoders1.jsystemssilkycodders1.repository;

import com.silkycoders1.jsystemssilkycodders1.domain.FormSubmission;
import com.silkycoders1.jsystemssilkycodders1.domain.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@DataR2dbcTest
@TestPropertySource(properties = {
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb-repos;DB_CLOSE_DELAY=-1",
    "spring.r2dbc.username=sa",
    "spring.r2dbc.password=",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:schema.sql"
})
class RepositoryTests {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private FormSubmissionRepository formSubmissionRepository;

    @Test
    void shouldSaveAndFindSession() {
        var session = Session.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        StepVerifier.create(
                sessionRepository.save(session)
                        .flatMap(saved -> sessionRepository.findById(saved.getId()))
        )
        .expectNextMatches(found -> found.getId() != null)
        .verifyComplete();
    }

    @Test
    void shouldSaveFormSubmission() {
        var session = Session.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        StepVerifier.create(
                sessionRepository.save(session)
                        .flatMap(savedSession -> {
                            var submission = FormSubmission.builder()
                                    .sessionId(savedSession.getId())
                                    .intent("return")
                                    .productName("Sukienka")
                                    .description("Nie pasuje rozmiar")
                                    .submittedAt(LocalDateTime.now())
                                    .build();
                            return formSubmissionRepository.save(submission);
                        })
                        .flatMap(saved -> formSubmissionRepository.findById(saved.getId()))
        )
        .expectNextMatches(found -> found.getProductName().equals("Sukienka"))
        .verifyComplete();
    }

    @Test
    void shouldFindFormSubmissionsBySessionId() {
        var session = Session.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        StepVerifier.create(
                sessionRepository.save(session)
                        .flatMapMany(savedSession -> {
                            var sub1 = FormSubmission.builder()
                                    .sessionId(savedSession.getId())
                                    .intent("return")
                                    .productName("Product 1")
                                    .description("Description one")
                                    .submittedAt(LocalDateTime.now())
                                    .build();
                            return formSubmissionRepository.save(sub1)
                                    .thenMany(formSubmissionRepository.findBySessionId(savedSession.getId()));
                        })
        )
        .expectNextCount(1)
        .verifyComplete();
    }

    @Test
    void shouldHandleNullVerdictInFormSubmission() {
        var session = Session.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        StepVerifier.create(
                sessionRepository.save(session)
                        .flatMap(savedSession -> {
                            var submission = FormSubmission.builder()
                                    .sessionId(savedSession.getId())
                                    .intent("complaint")
                                    .productName("Shirt")
                                    .description("Defective zipper")
                                    .submittedAt(LocalDateTime.now())
                                    .verdict(null)
                                    .build();
                            return formSubmissionRepository.save(submission);
                        })
                        .flatMap(saved -> formSubmissionRepository.findById(saved.getId()))
        )
        .expectNextMatches(found -> found.getVerdict() == null)
        .verifyComplete();
    }
}
