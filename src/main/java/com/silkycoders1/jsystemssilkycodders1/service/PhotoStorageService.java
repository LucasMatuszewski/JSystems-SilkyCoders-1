package com.silkycoders1.jsystemssilkycodders1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class PhotoStorageService {

    private static final Logger log = LoggerFactory.getLogger(PhotoStorageService.class);
    private static final Map<String, String> MIME_TO_EXT = Map.of(
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final String baseDir;

    public PhotoStorageService(@Value("${sinsay.photo.dir:data/photos}") String baseDir) {
        this.baseDir = baseDir;
    }

    public Mono<String> savePhoto(UUID sessionId, UUID submissionId, String base64, String mimeType) {
        return Mono.fromCallable(() -> {
            String ext = MIME_TO_EXT.getOrDefault(mimeType.toLowerCase(), "jpg");
            String relativePath = sessionId + "/" + submissionId + "." + ext;
            Path fullPath = Path.of(baseDir, relativePath);
            Files.createDirectories(fullPath.getParent());
            byte[] bytes = Base64.getDecoder().decode(base64);
            Files.write(fullPath, bytes);
            log.debug("Saved photo to: {}", fullPath);
            return relativePath;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
