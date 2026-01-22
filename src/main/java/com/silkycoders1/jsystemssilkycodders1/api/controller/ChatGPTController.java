package com.silkycoders1.jsystemssilkycodders1.api.controller;

import org.springframework.web.bind.annotation.*;

import com.silkycoders1.jsystemssilkycodders1.service.ChatGPTService;

import java.util.Map;

@RestController
@RequestMapping("/api/chatgpt")
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    public ChatGPTController(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String response = chatGPTService.chat(prompt);
        return Map.of("response", response);
    }
}
