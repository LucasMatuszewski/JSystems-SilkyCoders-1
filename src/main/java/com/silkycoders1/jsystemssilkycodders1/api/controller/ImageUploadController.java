package com.silkycoders1.jsystemssilkycodders1.api.controller;

import com.silkycoders1.jsystemssilkycodders1.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productProblemDetails") String productProblemDetails) {
        String analysisResult = imageService.uploadImage(file, productProblemDetails);
        return ResponseEntity.ok(analysisResult);
    }
}
