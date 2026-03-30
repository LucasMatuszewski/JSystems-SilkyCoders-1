package com.silkycoders1.jsystemssilkycodders1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class VercelStreamFormatter {

    private final ObjectMapper objectMapper;

    public VercelStreamFormatter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String formatTextChunk(String chunk) {
        if (chunk == null) {
            return "";
        }
        try {
            String jsonEscaped = objectMapper.writeValueAsString(chunk);
            return "0:" + jsonEscaped + "\n";
        } catch (JsonProcessingException exception) {
            return "";
        }
    }
}
