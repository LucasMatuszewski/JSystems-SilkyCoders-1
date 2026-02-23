# ADR-001: Architecture & Technology Stack for Sinsay AI PoC

**Status:** ACCEPTED

**Date:** 2026-01-22

**Context:** Sinsay AI Verification System (Returns & Complaints)

**Target Audience:** AI Coding Agents & Developers

## 1. Context and Problem Statement

We are building a Proof of Concept (PoC) to automate the verification of e-commerce returns (*Zwrot*) and complaints (*Reklamacja*) using Multimodal AI. The system requires a seamless integration between a Java Spring Boot backend (logic, policy enforcement) and a React frontend (rich chat interface).

**Key Challenges:**

1. Bridging the Java Backend with the Vercel AI SDK (standard for React Chat UIs).
2. Handling image uploads and multimodal context efficiently.
3. Deploying as a single artifact for simplicity.
4. Persisting interaction data locally without heavy infrastructure.

## 2. Decision Record

### 2.1 Backend Framework: Spring Boot with Spring AI

* **Decision:** Use **Spring Boot 3.5.9** (Java 21) with **Spring AI (`spring-ai-starter-model-openai`)**.
* **Rationale:**
* Java 21 Virtual Threads (`Project Loom`) handle high-concurrency I/O (AI streaming) efficiently.
* `spring-ai-starter-model-openai` provides a stable abstraction over the standard OpenAI Chat Completions API.
* **Constraint:** We explicitly avoid the newer "Responses API" or the "Official SDK Wrapper" variant to ensure broader compatibility (e.g., OpenRouter) and cleaner Spring integration.



### 2.2 Frontend Library: React 19 + Assistant-UI

* **Decision:** Use **React 19** with **`assistant-ui`** and **Vercel AI SDK (Core)**.
* **Rationale:**
* `assistant-ui` provides pre-built, accessible, and polished chat components (Thread, Composer, Attachments) that adhere to Sinsay's minimalist aesthetic.
* Vercel AI SDK (`useChat`) manages the complex state of streaming (appending chunks, optimistic updates) automatically.
* **Constraint:** Must run in "Embedded Mode" (Single JAR).



### 2.3 Communication Protocol: SSE with Vercel Protocol Adapter

* **Decision:** **Server-Sent Events (SSE)** using the **Vercel Data Stream Protocol**.
* **Rationale:**
* SSE is firewall-friendly (standard HTTP) and simpler than WebSockets for unidirectional AI responses.
* **Critical:** Standard SSE (`data: hello`) breaks Vercel AI SDK. We MUST format streams as:
* `0:"text_chunk"` (Text deltas)
* `8:[{"data":"..."}]` (Data/Tool calls)





### 2.4 Persistence: SQLite

* **Decision:** **SQLite** (`jdbc:sqlite:sinsay_poc.db`).
* **Rationale:** Zero-config, file-based storage perfect for PoC. Allows easy transfer of "logs" by simply copying the `.db` file.

---

## 3. Detailed Implementation Reference

### 3.1 Project Structure (Monorepo)

The project utilizes the **Maven Frontend Plugin** pattern.

```text
sinsay-poc/
├── pom.xml                     # Root Aggregator
├── backend/
│   ├── pom.xml                 # Spring Boot + Frontend Plugin
│   ├── src/main/java/com/sinsay/
│   │   ├── config/             # WebConfig (CORS), AiConfig
│   │   ├── controller/         # ChatController.java (SSE Adapter)
│   │   ├── model/              # Entities: RequestLog, ChatMessage
│   │   └── service/            # VerificationService.java (Prompt Logic)
│   └── src/main/resources/
│       ├── application.properties
│       └── static/             # <-- Frontend builds into here
└── frontend/
    ├── package.json            # React 19, assistant-ui, ai-sdk
    ├── vite.config.ts          # Build output dir -> ../backend/src/main/resources/static
    └── src/
        ├── components/ui/      # Shadcn components
        └── app/                # Main Logic

```

### 3.2 Backend Implementation (The "Vercel Adapter")

**Dependency (`backend/pom.xml`):**

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
    <version>1.0.0-M5</version> </dependency>
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-community-dialects</artifactId>
</dependency>

```

**Controller Adapter (`ChatController.java`):**
*Crucial:* This transforms Spring AI's text stream into Vercel's protocol.

```java
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {
        
        // 1. Convert Frontend Messages to Spring AI Prompt
        List<Message> messages = convertToSpringAiMessages(request.messages());
        
        // 2. Add System Prompt with Policy Context (Return vs Complaint)
        // ... (Logic to inject System Prompt based on request metadata)

        // 3. Stream & Transform to Vercel Format (0:"content")
        return chatClient.prompt()
                .messages(messages)
                .stream()
                .content()
                .map(this::formatVercelMessage);
    }

    /**
     * Vercel AI SDK expects text chunks prefixed with '0:' and JSON encoded strings.
     * Example: 0:"Hello"
     */
    private String formatVercelMessage(String chunk) {
        if (chunk == null) return "";
        try {
            // Escape special JSON characters (newlines, quotes)
            String jsonEscaped = new ObjectMapper().writeValueAsString(chunk);
            return "0:" + jsonEscaped + "\n";
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}

```

### 3.3 Frontend Implementation (Assistant-UI)

**Installation (`frontend/package.json`):**

```bash
npm install @assistant-ui/react @assistant-ui/react-markdown ai
npm install lucide-react class-variance-authority clsx tailwind-merge
# Note: Use --legacy-peer-deps if React 19 conflicts occur

```

**Chat Component (`Chat.tsx`):**
Uses standard `useChat` but points to our Spring Boot endpoint.

```tsx
import { useChat } from "ai/react";
import { Thread } from "@assistant-ui/react";

export function SinsayChat() {
  const { messages, input, handleInputChange, handleSubmit } = useChat({
    api: "/api/chat", // Points to Spring Boot
    body: { 
       // Pass extra context from the Form here
       intent: "return", 
       orderId: "12345" 
    },
  });

  return (
    <div className="h-[600px] w-full border rounded-md">
      <Thread 
        messages={messages} 
        input={input} 
        handleInputChange={handleInputChange}
        handleSubmit={handleSubmit}
        assistantAvatar={{ src: "/sinsay-logo.png" }}
        welcome={{
            message: "Hello. I am the Sinsay Verification Assistant. Please upload your photo."
        }}
      />
    </div>
  );
}

```

### 3.4 Data & Persistence Configuration

**`application.properties`**

```properties
# Database
spring.datasource.url=jdbc:sqlite:sinsay_poc.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update

# OpenAI
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o
spring.ai.openai.chat.options.temperature=0.2

# Server
server.port=8080
spring.threads.virtual.enabled=true

```

---

## 4. Execution Plan (Commands for Agents)

### Phase 1: Initialization

```bash
# Backend Setup
curl https://start.spring.io/starter.zip \
    -d dependencies=web,webflux,data-jpa,lombok \
    -d javaVersion=21 \
    -d type=maven-project \
    -o backend.zip
unzip backend.zip -d backend

# Frontend Setup
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
npm install @assistant-ui/react ai zod react-hook-form

```

### Phase 2: Development Loop

* **Terminal 1 (Backend):** `cd backend && ./mvnw spring-boot:run`
* **Terminal 2 (Frontend):** `cd frontend && npm run dev`
* *Note:* Ensure `vite.config.ts` has a proxy to forward `/api` to `http://localhost:8080`.



### Phase 3: Build & Package (Single Artifact)

```bash
# From Root
cd backend
./mvnw clean package
# Result: target/sinsay-poc-0.0.1.jar (Contains React App)
java -jar target/sinsay-poc-0.0.1.jar

```

## 5. Critical Constraints for AI Implementation

1. **Do NOT** use `Flux<ChatResponse>` directly in the Controller return without mapping. It will stream raw JSON that `assistant-ui` cannot parse. You **MUST** map to the `0:"..."` string format.
2. **Do NOT** use the Image Generation API (`DALL-E`). Use `gpt-4o` Chat Model with `Media` attachments for vision.
3. **Strictly** enforce the `intent` field (Return vs Complaint) to select the correct System Prompt logic in Java.
4. **Security:** Never commit API keys. Use `.env` file or IDE environment variables.
