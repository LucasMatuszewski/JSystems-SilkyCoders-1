package com.silkycoders1.jsystemssilkycodders1.controller;

import com.silkycoders1.jsystemssilkycodders1.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow dev server
public class ChatController {

    private final VerificationService verificationService;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {
        return verificationService.verify(request);
    }
}
