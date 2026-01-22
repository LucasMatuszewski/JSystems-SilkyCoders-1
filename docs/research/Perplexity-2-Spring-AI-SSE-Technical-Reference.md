# Spring AI + SSE Technical Reference Guide

Quick implementation guide for Spring Boot + Spring AI + SSE streaming for Sinsay PoC

---

## 1. Maven Dependencies

```xml
<!-- pom.xml -->

<!-- Spring Boot Starter Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.2.0</version>
</dependency>

<!-- Spring Boot Starter WebFlux (for reactive streaming) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
    <version>3.2.0</version>
</dependency>

<!-- Spring AI - OpenAI Integration (NOT the official SDK) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-openai</artifactId>
    <version>1.0.1</version>
</dependency>

<!-- Lombok (optional, for cleaner code) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>

<!-- Jackson for JSON (included with Spring Boot) -->
```

---

## 2. Configuration (application.properties)

```properties
# Spring AI - OpenAI Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o
spring.ai.openai.chat.options.temperature=0.3
spring.ai.openai.chat.options.max-tokens=2000

# Retry Configuration
spring.ai.retry.max-attempts=3
spring.ai.retry.backoff.initial-interval=2s
spring.ai.retry.backoff.multiplier=5
spring.ai.retry.backoff.max-interval=3m

# Server Configuration
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.force=true

# Optional: Logging
logging.level.org.springframework.ai=DEBUG
```

**In `.env` file**:
```bash
OPENAI_API_KEY=sk-...your-key-here...
```

---

## 3. Core Service: ChatService with Streaming

```java
package com.sinsay.returns.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    
    private final ChatClient chatClient;
    
    /**
     * Stream chat response via SSE
     * @param userMessage The user's chat message
     * @param systemPrompt System prompt for context
     * @param emitter SseEmitter to send events
     */
    public void streamChatResponse(
            String userMessage, 
            String systemPrompt,
            SseEmitter emitter) {
        
        // Use a thread pool for non-blocking execution
        Thread.ofVirtual().start(() -> {
            try {
                // Build and execute streaming prompt
                Flux<ChatResponse> responseFlux = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .stream()
                    .chatResponse();
                
                // Subscribe to streaming response
                responseFlux.subscribe(
                    // On each chunk received
                    response -> {
                        try {
                            String chunk = response.getResult().getOutput().getContent();
                            
                            // Send via SSE
                            emitter.send(SseEmitter.event()
                                .id(String.valueOf(System.nanoTime()))
                                .name("message")
                                .data(chunk)
                                .build());
                            
                            log.debug("Sent chunk: {}", chunk.substring(0, Math.min(20, chunk.length())));
                        } catch (IOException e) {
                            log.error("Error sending SSE event", e);
                            completeWithError(emitter, e);
                        }
                    },
                    // On error
                    error -> {
                        log.error("Error in chat stream", error);
                        completeWithError(emitter, error);
                    },
                    // On complete
                    () -> {
                        try {
                            // Send final completion event
                            emitter.send(SseEmitter.event()
                                .id(String.valueOf(System.nanoTime()))
                                .name("complete")
                                .data("true")
                                .build());
                            
                            emitter.complete();
                            log.info("Chat streaming completed successfully");
                        } catch (IOException e) {
                            log.error("Error completing SSE stream", e);
                        }
                    }
                );
                
            } catch (Exception e) {
                log.error("Unexpected error in chat streaming", e);
                completeWithError(emitter, e);
            }
        });
    }
    
    /**
     * Stream chat response with image analysis
     */
    public void streamChatWithImageAnalysis(
            String userMessage,
            byte[] imageData,
            String imageMediaType,
            String systemPrompt,
            SseEmitter emitter) {
        
        Thread.ofVirtual().start(() -> {
            try {
                // Create user message with image
                var userMsg = new UserMessage(
                    userMessage,
                    List.of(new org.springframework.ai.model.Media(
                        org.springframework.util.MimeTypeUtils.parseMimeType(imageMediaType),
                        imageData
                    ))
                );
                
                // Stream response
                Flux<ChatResponse> responseFlux = chatClient.prompt()
                    .system(systemPrompt)
                    .messages(List.of(userMsg))
                    .stream()
                    .chatResponse();
                
                responseFlux.subscribe(
                    response -> {
                        try {
                            String chunk = response.getResult().getOutput().getContent();
                            emitter.send(SseEmitter.event()
                                .data(chunk)
                                .build());
                        } catch (IOException e) {
                            completeWithError(emitter, e);
                        }
                    },
                    error -> completeWithError(emitter, error),
                    () -> {
                        try {
                            emitter.complete();
                        } catch (IOException e) {
                            log.error("Error completing SSE", e);
                        }
                    }
                );
                
            } catch (Exception e) {
                completeWithError(emitter, e);
            }
        });
    }
    
    /**
     * Helper: Complete SSE emitter with error
     */
    private void completeWithError(SseEmitter emitter, Exception error) {
        try {
            emitter.send(SseEmitter.event()
                .name("error")
                .data(error.getMessage())
                .build());
            emitter.complete();
        } catch (IOException e) {
            log.error("Failed to send error event", e);
        }
    }
}
```

---

## 4. Controller: HTTP Endpoints

```java
package com.sinsay.returns.controller;

import com.sinsay.returns.dto.ChatRequest;
import com.sinsay.returns.service.ChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.sinsay.returns.config.PromptTemplates.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "https://sinsay.com"})
public class ChatController {
    
    private final ChatService chatService;
    
    /**
     * POST /api/chat
     * Stream chat response via SSE
     * 
     * Request body:
     * {
     *   "orderNumber": "ORD12345",
     *   "issueType": "defect_complaint",
     *   "message": "The fabric has a tear"
     * }
     */
    @PostMapping
    public SseEmitter chat(@RequestBody ChatRequest request) {
        log.info("Chat request received - Order: {}, Issue: {}", 
                 request.getOrderNumber(), request.getIssueType());
        
        // Create SSE emitter with 5-minute timeout
        SseEmitter emitter = new SseEmitter(300000L);
        
        // Get system prompt based on issue type
        String systemPrompt = getSystemPrompt(request.getIssueType());
        
        // Stream response in background thread
        chatService.streamChatResponse(
            request.getMessage(),
            systemPrompt,
            emitter
        );
        
        return emitter;
    }
    
    /**
     * POST /api/chat/image-analysis
     * Stream image analysis via SSE
     * 
     * Request: multipart form
     * - image: file upload
     * - orderNumber: string
     * - message: string describing issue
     */
    @PostMapping("/image-analysis")
    public SseEmitter analyzeImage(
            @RequestParam("image") org.springframework.web.multipart.MultipartFile image,
            @RequestParam("orderNumber") String orderNumber,
            @RequestParam("message") String message) {
        
        log.info("Image analysis request - Order: {}", orderNumber);
        
        SseEmitter emitter = new SseEmitter(300000L);
        
        try {
            byte[] imageData = image.getBytes();
            String mimeType = image.getContentType();
            String systemPrompt = SYSTEM_PROMPT_IMAGE_ANALYSIS;
            
            chatService.streamChatWithImageAnalysis(
                message,
                imageData,
                mimeType,
                systemPrompt,
                emitter
            );
        } catch (Exception e) {
            log.error("Error processing image", e);
            try {
                emitter.completeWithError(e);
            } catch (Exception ex) {
                log.error("Error sending error event", ex);
            }
        }
        
        return emitter;
    }
    
    /**
     * Helper: Select system prompt
     */
    private String getSystemPrompt(String issueType) {
        return switch (issueType) {
            case "defect_complaint" -> SYSTEM_PROMPT_DEFECT;
            case "sizing_issue" -> SYSTEM_PROMPT_SIZING;
            case "color_issue" -> SYSTEM_PROMPT_COLOR;
            case "fit_complaint" -> SYSTEM_PROMPT_FIT;
            default -> SYSTEM_PROMPT_GENERAL;
        };
    }
}
```

---

## 5. DTOs

```java
package com.sinsay.returns.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatRequest {
    private String orderNumber;
    private String issueType;
    private String message;
}

@Data
@NoArgsConstructor
public class ChatResponse {
    private String response;
    private String status; // "completed", "error"
    private String errorMessage;
}
```

---

## 6. System Prompts Configuration

```java
package com.sinsay.returns.config;

public class PromptTemplates {
    
    public static final String SYSTEM_PROMPT_GENERAL = """
        You are a helpful customer service representative for Sinsay, a clothing retailer.
        You're assisting with product returns and complaints.
        
        Guidelines:
        - Be empathetic and professional
        - Acknowledge the customer's concern
        - Ask clarifying questions if needed
        - Provide solutions when possible
        - Keep responses concise (2-3 paragraphs max)
        - Always offer escalation to human support if needed
        """;
    
    public static final String SYSTEM_PROMPT_DEFECT = """
        You are a quality control specialist for Sinsay.
        The customer has reported a defect in their garment.
        
        Guidelines:
        - Ask about the specific nature of the defect
        - Assess severity (minor, moderate, severe)
        - Offer replacement, refund, or discount based on severity
        - If possible, guide them on how to care for the item
        - Provide RMA (Return Merchandise Authorization) number
        """;
    
    public static final String SYSTEM_PROMPT_SIZING = """
        You are a sizing specialist for Sinsay.
        The customer has issues with sizing/fit.
        
        Guidelines:
        - Ask about their usual size and how this item fits
        - Reference Sinsay's sizing chart
        - Offer size exchange at no cost
        - Provide tips for finding the right fit
        """;
    
    public static final String SYSTEM_PROMPT_COLOR = """
        You are a product specialist for Sinsay.
        The customer has concerns about color accuracy.
        
        Guidelines:
        - Ask if color is faded, discolored, or different from product photos
        - Assess if it's a manufacturing issue or user error
        - Offer replacement or refund if manufacturing defect
        """;
    
    public static final String SYSTEM_PROMPT_FIT = """
        You are a style consultant for Sinsay.
        The customer has fit/comfort concerns.
        
        Guidelines:
        - Ask specific questions about what feels wrong
        - Suggest alterations if applicable
        - Offer size exchange or return
        """;
    
    public static final String SYSTEM_PROMPT_IMAGE_ANALYSIS = """
        You are a quality inspector analyzing customer-provided images.
        Examine the image of the garment and provide a professional assessment.
        
        Analysis requirements:
        1. Identify the type of issue (defect, discoloration, wear, etc.)
        2. Assess severity (minor, moderate, severe)
        3. Determine if this is a manufacturing defect or usage issue
        4. Recommend action (replace, refund, discount, or reject claim)
        5. Provide explanation suitable for customer communication
        
        Output as: Issue | Severity | Root Cause | Recommendation | Explanation
        """;
}
```

---

## 7. Frontend React Component (TypeScript)

```typescript
// components/ChatBot.tsx

import React, { useState, useRef, useEffect } from 'react';
import './ChatBot.css';

interface Message {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

interface ChatRequest {
  orderNumber: string;
  issueType: string;
  message: string;
}

export const ChatBot: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [loading, setLoading] = useState(false);
  const [orderNumber, setOrderNumber] = useState('');
  const [issueType, setIssueType] = useState('defect_complaint');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to latest message
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = async () => {
    if (!inputValue.trim() || !orderNumber.trim()) {
      alert('Please enter order number and message');
      return;
    }

    // Add user message immediately
    const userMessage: Message = {
      role: 'user',
      content: inputValue,
      timestamp: new Date(),
    };

    setMessages(prev => [...prev, userMessage]);
    setInputValue('');
    setLoading(true);

    try {
      // Build request
      const request: ChatRequest = {
        orderNumber,
        issueType,
        message: inputValue,
      };

      // Send POST request
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      });

      if (!response.body) {
        throw new Error('No response body');
      }

      // Read SSE stream
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let assistantMessage = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        // Decode chunk
        const text = decoder.decode(value, { stream: true });
        assistantMessage += text;

        // Update message in real-time
        setMessages(prev => {
          const updated = [...prev];
          const lastMsg = updated[updated.length - 1];

          if (lastMsg?.role === 'assistant') {
            updated[updated.length - 1] = {
              ...lastMsg,
              content: assistantMessage,
            };
          } else {
            updated.push({
              role: 'assistant',
              content: assistantMessage,
              timestamp: new Date(),
            });
          }

          return updated;
        });
      }
    } catch (error) {
      console.error('Chat error:', error);
      setMessages(prev => [
        ...prev,
        {
          role: 'assistant',
          content: 'Sorry, an error occurred. Please try again.',
          timestamp: new Date(),
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleImageAnalysis = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('image', file);
    formData.append('orderNumber', orderNumber);
    formData.append('message', 'Please analyze this image for defects');

    setLoading(true);

    try {
      const response = await fetch('/api/chat/image-analysis', {
        method: 'POST',
        body: formData,
      });

      if (!response.body) throw new Error('No response body');

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let analysis = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const text = decoder.decode(value, { stream: true });
        analysis += text;

        setMessages(prev => {
          const updated = [...prev];
          const lastMsg = updated[updated.length - 1];

          if (lastMsg?.role === 'assistant') {
            updated[updated.length - 1] = {
              ...lastMsg,
              content: analysis,
            };
          } else {
            updated.push({
              role: 'assistant',
              content: analysis,
              timestamp: new Date(),
            });
          }

          return updated;
        });
      }
    } catch (error) {
      console.error('Image analysis error:', error);
      setMessages(prev => [
        ...prev,
        {
          role: 'assistant',
          content: 'Image analysis failed. Please try again.',
          timestamp: new Date(),
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="chatbot-container">
      <div className="chatbot-header">
        <h2>Sinsay Returns & Complaints Support</h2>
      </div>

      <div className="chatbot-messages">
        {messages.map((msg, idx) => (
          <div key={idx} className={`message message-${msg.role}`}>
            <div className="message-content">{msg.content}</div>
            <div className="message-time">
              {msg.timestamp.toLocaleTimeString()}
            </div>
          </div>
        ))}
        {loading && <div className="message message-assistant">Thinking...</div>}
        <div ref={messagesEndRef} />
      </div>

      <div className="chatbot-controls">
        <input
          type="text"
          placeholder="Order Number (e.g., ORD12345)"
          value={orderNumber}
          onChange={e => setOrderNumber(e.target.value)}
          className="input-field"
        />

        <select
          value={issueType}
          onChange={e => setIssueType(e.target.value)}
          className="select-field"
        >
          <option value="defect_complaint">Defect/Quality Issue</option>
          <option value="sizing_issue">Sizing Issue</option>
          <option value="color_issue">Color Issue</option>
          <option value="fit_complaint">Fit Complaint</option>
        </select>

        <div className="input-row">
          <input
            type="text"
            placeholder="Describe your issue..."
            value={inputValue}
            onChange={e => setInputValue(e.target.value)}
            onKeyPress={e => e.key === 'Enter' && handleSendMessage()}
            className="input-field"
            disabled={loading}
          />
          <button
            onClick={handleSendMessage}
            disabled={loading}
            className="btn btn-primary"
          >
            Send
          </button>
        </div>

        <div className="image-upload">
          <label>
            📷 Upload Image (Optional)
            <input
              type="file"
              accept="image/*"
              onChange={handleImageAnalysis}
              disabled={loading}
            />
          </label>
        </div>
      </div>
    </div>
  );
};
```

---

## 8. Testing the Implementation

### Test with cURL

```bash
# Test basic chat endpoint
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "orderNumber": "ORD12345",
    "issueType": "defect_complaint",
    "message": "The seam on my shirt is coming apart"
  }'

# Expected output: SSE stream with response chunks
```

### Test with HTTPie

```bash
# Streaming GET test
http --stream POST http://localhost:8080/api/chat \
  orderNumber=ORD12345 \
  issueType=defect_complaint \
  message="Is this a defect or wear?"
```

### Test with JavaScript/Fetch

```javascript
fetch('/api/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    orderNumber: 'ORD12345',
    issueType: 'defect_complaint',
    message: 'Help me with my issue'
  })
})
.then(response => response.body.getReader())
.then(reader => {
  const decoder = new TextDecoder();
  
  const read = async () => {
    const { done, value } = await reader.read();
    if (done) return;
    
    console.log(decoder.decode(value));
    await read();
  };
  
  return read();
})
.catch(err => console.error(err));
```

---

## 9. Performance & Optimization

### Thread Pool Configuration

```java
// Optional: Spring configuration for async processing

@Configuration
public class AsyncConfiguration {
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("chat-");
        executor.initialize();
        return executor;
    }
}
```

### Timeout Configuration

```java
// In ChatService, adjust emitter timeout based on response size
SseEmitter emitter = new SseEmitter(300000L); // 5 minutes

// Or configure via properties
spring.mvc.async.request-timeout=300000
```

### Connection Limits

```properties
# application.properties - tune for expected load

# Tomcat settings
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
server.tomcat.accept-count=100
server.tomcat.max-connections=10000

# Connection timeout
server.tomcat.connection-timeout=20000
```

---

## 10. Error Handling & Logging

```java
// In ChatService

@Slf4j
public class ChatService {
    
    public void streamChatResponse(String message, String systemPrompt, SseEmitter emitter) {
        Thread.ofVirtual().start(() -> {
            try {
                // ... streaming logic ...
                
            } catch (IllegalStateException e) {
                // Client disconnected
                log.warn("Client disconnected: {}", e.getMessage());
                
            } catch (IOException e) {
                // Network error
                log.error("IO error during streaming: {}", e.getMessage());
                
            } catch (org.springframework.ai.retry.NonTransientAiException e) {
                // Non-retryable error from AI provider
                log.error("Non-retryable AI error: {}", e.getMessage());
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("AI service error: " + e.getMessage())
                        .build());
                } catch (IOException ex) {
                    log.error("Failed to send error", ex);
                }
                
            } catch (org.springframework.ai.retry.TransientAiException e) {
                // Retryable error - will be handled by Spring Retry
                log.warn("Transient AI error (will retry): {}", e.getMessage());
                
            } catch (Exception e) {
                // Unexpected error
                log.error("Unexpected error in chat streaming", e);
                try {
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    log.error("Failed to complete with error", ex);
                }
            }
        });
    }
}
```

---

## 11. Security Considerations

```java
// CORS Configuration
@Configuration
public class SecurityConfig {
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/chat/**")
                    .allowedOrigins("https://sinsay.com", "https://app.sinsay.com")
                    .allowedMethods("POST", "OPTIONS")
                    .allowedHeaders("Content-Type", "Authorization")
                    .maxAge(3600);
            }
        };
    }
}

// Rate Limiting (with Spring Cloud Resilience4j)
@Configuration
@EnableConfigurationProperties(RateLimiterProperties.class)
public class RateLimiterConfig {
    
    @Bean
    public TimeLimiter timeLimiter() {
        return TimeLimiter.of(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(60))
            .build());
    }
    
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.of(RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .limitForPeriod(100) // 100 requests per minute
            .build());
    }
}

// Apply to controller
@PostMapping
@io.github.resilience4j.ratelimiter.annotation.RateLimiter(name = "rateLimiter")
public SseEmitter chat(@RequestBody ChatRequest request) {
    // ...
}
```

---

## 12. Monitoring & Metrics

```java
// Add Micrometer metrics

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    
    private final MeterRegistry meterRegistry;
    
    public void streamChatResponse(String message, String systemPrompt, SseEmitter emitter) {
        var startTime = System.currentTimeMillis();
        var messageCounter = Counter.builder("chat.messages.sent")
            .description("Number of chat messages sent")
            .register(meterRegistry);
        
        Thread.ofVirtual().start(() -> {
            try {
                // ... streaming logic ...
                messageCounter.increment();
                
                var duration = System.currentTimeMillis() - startTime;
                Timer.builder("chat.response.time")
                    .description("Time to generate chat response")
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(meterRegistry)
                    .record(duration, TimeUnit.MILLISECONDS);
                    
            } catch (Exception e) {
                Counter.builder("chat.errors")
                    .tag("type", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            }
        });
    }
}
```

---

## Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| **API Key not found** | Check `OPENAI_API_KEY` env var or `spring.ai.openai.api-key` property |
| **SSE not streaming** | Ensure `SseEmitter.send()` is called in background thread |
| **Timeout errors** | Increase `SseEmitter(timeout)` value, check `spring.mvc.async.request-timeout` |
| **CORS errors** | Configure `@CrossOrigin` on controller or `WebMvcConfigurer` bean |
| **Client disconnect handling** | Catch `IOException` when sending to emitter |
| **Memory leaks** | Use Virtual Threads (`Thread.ofVirtual()`) instead of creating platform threads |
| **Slow responses** | Check OpenAI API status, monitor temperature setting |

---

**Last Updated**: January 2026  
**Spring AI Version**: 1.0.1+  
**Spring Boot**: 3.2.0+  
**Java**: 21 LTS (required for Virtual Threads)
