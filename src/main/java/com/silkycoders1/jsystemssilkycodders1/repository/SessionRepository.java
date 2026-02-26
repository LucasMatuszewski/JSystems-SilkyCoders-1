package com.silkycoders1.jsystemssilkycodders1.repository;

import com.silkycoders1.jsystemssilkycodders1.domain.Session;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface SessionRepository extends ReactiveCrudRepository<Session, UUID> {
}
