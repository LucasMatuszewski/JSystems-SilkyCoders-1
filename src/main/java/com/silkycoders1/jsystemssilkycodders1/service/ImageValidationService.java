package com.silkycoders1.jsystemssilkycodders1.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class ImageValidationService {

    private final Tika tika = new Tika();
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/heic",
            "image/heif");

    public String validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {
            String detectedMimeType = tika.detect(file.getInputStream());
            if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
                throw new IllegalArgumentException(
                        "Invalid image format. Allowed: JPEG, PNG, GIF, WEBP, HEIC, HEIF. Detected: "
                                + detectedMimeType);
            }
            return detectedMimeType;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to process file validation", e);
        }
    }
}
