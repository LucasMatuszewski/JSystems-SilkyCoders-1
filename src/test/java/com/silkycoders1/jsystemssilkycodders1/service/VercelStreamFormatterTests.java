package com.silkycoders1.jsystemssilkycodders1.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class VercelStreamFormatterTests {

    private final VercelStreamFormatter formatter = new VercelStreamFormatter(new ObjectMapper());

    @Test
    void formatTextChunkAddsPrefixAndNewline() {
        String formatted = formatter.formatTextChunk("Cześć");

        assertThat(formatted).isEqualTo("0:\"Cześć\"\n");
    }

    @Test
    void formatTextChunkEscapesJsonCharacters() throws Exception {
        String chunk = "Line 1\nLine \"2\"";
        String expectedJson = new ObjectMapper().writeValueAsString(chunk);

        assertThat(formatter.formatTextChunk(chunk)).isEqualTo("0:" + expectedJson + "\n");
    }

    @Test
    void formatTextChunkHandlesNull() {
        assertThat(formatter.formatTextChunk(null)).isEmpty();
    }
}
