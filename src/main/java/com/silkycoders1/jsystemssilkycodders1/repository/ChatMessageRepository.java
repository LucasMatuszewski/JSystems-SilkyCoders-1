package com.silkycoders1.jsystemssilkycodders1.repository;

import com.silkycoders1.jsystemssilkycodders1.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionId(UUID sessionId);
}
