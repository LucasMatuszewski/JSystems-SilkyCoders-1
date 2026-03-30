package com.sinsay.repository;

import com.sinsay.model.ChatMessage;
import com.sinsay.model.Intent;
import com.sinsay.model.Role;
import com.sinsay.model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ChatMessageRepositoryTests {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private SessionRepository sessionRepository;

    private Session testSession;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        sessionRepository.deleteAll();

        testSession = Session.builder()
                .intent(Intent.RETURN)
                .orderNumber("ORD-12345")
                .productName("Blue Jeans")
                .description("Product arrived damaged")
                .build();
        testSession = sessionRepository.save(testSession);
    }

    @Test
    void saveChatMessage_shouldPersistAllFields() {
        // Given
        ChatMessage message = ChatMessage.builder()
                .sessionId(testSession.getId())
                .role(Role.USER)
                .content("I want to return this item")
                .sequenceNumber(0)
                .build();

        // When
        ChatMessage saved = chatMessageRepository.save(message);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSessionId()).isEqualTo(testSession.getId());
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getContent()).isEqualTo("I want to return this item");
        assertThat(saved.getSequenceNumber()).isEqualTo(0);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findBySessionIdOrderBySequenceNumberAsc_withMultipleMessages_shouldReturnInCorrectOrder() {
        // Given
        UUID sessionId = testSession.getId();

        ChatMessage msg1 = ChatMessage.builder()
                .sessionId(sessionId)
                .role(Role.USER)
                .content("First message")
                .sequenceNumber(0)
                .build();

        ChatMessage msg2 = ChatMessage.builder()
                .sessionId(sessionId)
                .role(Role.ASSISTANT)
                .content("Second message")
                .sequenceNumber(1)
                .build();

        ChatMessage msg3 = ChatMessage.builder()
                .sessionId(sessionId)
                .role(Role.USER)
                .content("Third message")
                .sequenceNumber(2)
                .build();

        chatMessageRepository.save(msg1);
        chatMessageRepository.save(msg2);
        chatMessageRepository.save(msg3);

        // When
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId);

        // Then
        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).getSequenceNumber()).isEqualTo(0);
        assertThat(messages.get(0).getRole()).isEqualTo(Role.USER);
        assertThat(messages.get(0).getContent()).isEqualTo("First message");

        assertThat(messages.get(1).getSequenceNumber()).isEqualTo(1);
        assertThat(messages.get(1).getRole()).isEqualTo(Role.ASSISTANT);
        assertThat(messages.get(1).getContent()).isEqualTo("Second message");

        assertThat(messages.get(2).getSequenceNumber()).isEqualTo(2);
        assertThat(messages.get(2).getRole()).isEqualTo(Role.USER);
        assertThat(messages.get(2).getContent()).isEqualTo("Third message");
    }

    @Test
    void findBySessionIdOrderBySequenceNumberAsc_withNoMessages_shouldReturnEmptyList() {
        // When
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderBySequenceNumberAsc(testSession.getId());

        // Then
        assertThat(messages).isEmpty();
    }

    @Test
    void saveMultipleMessagesForDifferentSessions_shouldOnlyReturnRelevantSession() {
        // Given
        Session otherSession = Session.builder()
                .intent(Intent.COMPLAINT)
                .orderNumber("ORD-999")
                .productName("Other Product")
                .description("Other issue")
                .build();
        otherSession = sessionRepository.save(otherSession);

        ChatMessage msg1 = ChatMessage.builder()
                .sessionId(testSession.getId())
                .role(Role.USER)
                .content("Message for session 1")
                .sequenceNumber(0)
                .build();

        ChatMessage msg2 = ChatMessage.builder()
                .sessionId(otherSession.getId())
                .role(Role.USER)
                .content("Message for session 2")
                .sequenceNumber(0)
                .build();

        chatMessageRepository.save(msg1);
        chatMessageRepository.save(msg2);

        // When
        List<ChatMessage> session1Messages = chatMessageRepository.findBySessionIdOrderBySequenceNumberAsc(testSession.getId());
        List<ChatMessage> session2Messages = chatMessageRepository.findBySessionIdOrderBySequenceNumberAsc(otherSession.getId());

        // Then
        assertThat(session1Messages).hasSize(1);
        assertThat(session1Messages.get(0).getContent()).isEqualTo("Message for session 1");

        assertThat(session2Messages).hasSize(1);
        assertThat(session2Messages.get(0).getContent()).isEqualTo("Message for session 2");
    }
}
