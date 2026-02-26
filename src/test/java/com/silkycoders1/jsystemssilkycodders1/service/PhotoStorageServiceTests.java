package com.silkycoders1.jsystemssilkycodders1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.test.StepVerifier;

import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

class PhotoStorageServiceTests {

    @TempDir
    Path tempDir;

    private PhotoStorageService sut;

    @BeforeEach
    void setUp() {
        sut = new PhotoStorageService(tempDir.toString());
    }

    @Test
    void shouldSavePhotoAndReturnRelativePath() {
        var sessionId = UUID.randomUUID();
        var submissionId = UUID.randomUUID();
        var base64 = Base64.getEncoder().encodeToString("fake-image-bytes".getBytes());

        StepVerifier.create(sut.savePhoto(sessionId, submissionId, base64, "image/jpeg"))
                .expectNextMatches(path -> path.endsWith(".jpg"))
                .verifyComplete();
    }

    @Test
    void shouldSavePhotoPngExtension() {
        var sessionId = UUID.randomUUID();
        var submissionId = UUID.randomUUID();
        var base64 = Base64.getEncoder().encodeToString("fake-png-bytes".getBytes());

        StepVerifier.create(sut.savePhoto(sessionId, submissionId, base64, "image/png"))
                .expectNextMatches(path -> path.endsWith(".png"))
                .verifyComplete();
    }

    @Test
    void shouldCreateDirectoryStructure() {
        var sessionId = UUID.randomUUID();
        var submissionId = UUID.randomUUID();
        var base64 = Base64.getEncoder().encodeToString("fake-bytes".getBytes());

        StepVerifier.create(sut.savePhoto(sessionId, submissionId, base64, "image/jpeg"))
                .expectNextMatches(path -> {
                    var file = tempDir.resolve(path).toFile();
                    return file.exists() && file.length() > 0;
                })
                .verifyComplete();
    }
}
