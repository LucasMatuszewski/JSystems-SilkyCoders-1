package com.sinsay.controller.dto;

import com.sinsay.model.ChatMessage;
import com.sinsay.model.Session;

import java.util.List;

/**
 * Response DTO for session load endpoint.
 *
 * @param session  The session entity
 * @param messages Ordered list of chat messages for the session
 */
public record SessionResponse(Session session, List<ChatMessage> messages) {
}
