package com.sinsay.repository;

import com.sinsay.model.Intent;
import com.sinsay.model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SessionRepositoryTests {

    @Autowired
    private SessionRepository sessionRepository;

    private Session testSession;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();

        testSession = Session.builder()
                .intent(Intent.RETURN)
                .orderNumber("ORD-12345")
                .productName("Blue Jeans")
                .description("Product arrived damaged")
                .build();
    }

    @Test
    void saveSession_shouldPersistAllFields() {
        // When
        Session saved = sessionRepository.save(testSession);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIntent()).isEqualTo(Intent.RETURN);
        assertThat(saved.getOrderNumber()).isEqualTo("ORD-12345");
        assertThat(saved.getProductName()).isEqualTo("Blue Jeans");
        assertThat(saved.getDescription()).isEqualTo("Product arrived damaged");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findById_shouldReturnSavedSession() {
        // Given
        Session saved = sessionRepository.save(testSession);
        UUID savedId = saved.getId();

        // When
        Optional<Session> found = sessionRepository.findById(savedId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedId);
        assertThat(found.get().getIntent()).isEqualTo(Intent.RETURN);
        assertThat(found.get().getOrderNumber()).isEqualTo("ORD-12345");
        assertThat(found.get().getProductName()).isEqualTo("Blue Jeans");
        assertThat(found.get().getDescription()).isEqualTo("Product arrived damaged");
    }

    @Test
    void findById_withNonExistentId_shouldReturnEmpty() {
        // When
        Optional<Session> found = sessionRepository.findById(UUID.randomUUID());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void saveSession_withComplaintIntent_shouldPersistCorrectly() {
        // Given
        Session complaintSession = Session.builder()
                .intent(Intent.COMPLAINT)
                .orderNumber("ORD-67890")
                .productName("T-Shirt")
                .description("Stains on fabric")
                .build();

        // When
        Session saved = sessionRepository.save(complaintSession);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIntent()).isEqualTo(Intent.COMPLAINT);
        assertThat(saved.getOrderNumber()).isEqualTo("ORD-67890");
        assertThat(saved.getProductName()).isEqualTo("T-Shirt");
        assertThat(saved.getDescription()).isEqualTo("Stains on fabric");
    }
}
