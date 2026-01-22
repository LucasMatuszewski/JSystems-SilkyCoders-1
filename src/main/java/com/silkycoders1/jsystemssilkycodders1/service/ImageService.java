package com.silkycoders1.jsystemssilkycodders1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ImageService {

   private final ImageValidationService imageValidationService;
   private final ChatGPTService chatGPTService;
   private final OllamaService ollamaService;
   private final ServicePromptService servicePromptService;

   @Value("${image.analysis.provider:openai}")
   private String provider;

   public String uploadImage(MultipartFile file, String productProblemDetails) {
      String mimeType = imageValidationService.validateImage(file);

      try {
         String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
         String prompt = servicePromptService.buildImageAnalysisPrompt(productProblemDetails);

         if ("ollama".equalsIgnoreCase(provider)) {
            return ollamaService.analyzeImage(base64Image, prompt, mimeType);
         }
         return chatGPTService.analyzeImage(base64Image, prompt, mimeType);
      } catch (IOException e) {
         throw new RuntimeException("Failed to process image", e);
      }
   }
}
