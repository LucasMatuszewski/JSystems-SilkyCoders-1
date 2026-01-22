# Backend Guidelines

## AI Integration

* **Framework:** Spring AI (`spring-ai-ollama-spring-boot-starter`)
* **Client:** Use `ChatModel` for all Ollama interactions
* **Media:** Use `Media` objects to send images to Ollama Gemma3
* **Model:** Configure Gemma3 for multimodal analysis

## Concurrency

* **Use Virtual Threads** (Java 21). Do not use `WebFlux` unless specifically required for streaming adapter logic.

## Controllers

### `ReturnController` - `/api/returns/submit`

* Accepts `ReturnRequest` or `ComplaintRequest` DTOs
* Validates policy (30 days for returns, 2 years for complaints)
* Returns: `{ conversationId: string, status: "VALID" | "REJECTED", message: string }`
* Generates unique `conversationId` for valid requests

### `ChatController` - `/api/chat`

* Accepts multipart form data with images and `conversationId`
* Returns `Flux<String>` (or `SseEmitter`) with strings manually formatted to Vercel Protocol
* Format example: `.map(chunk -> "0:\"" + chunk + "\"\n")`
* Must achieve TTFT < 2 seconds

## Services

### `ReturnPolicyService`

Implements business rules:
* Calculate days between purchase date and current date
* Auto-reject returns > 30 days
* Auto-reject complaints > 2 years
* Return clear rejection messages with actionable feedback

### `AiService`

Handles AI interactions:
* **Returns:** Receipt OCR verification (extract order ID, date, verify authenticity)
* **Complaints:** Defect analysis (classify defect type, distinguish manufacturing defect vs. user damage)
* System prompt must include role ("Senior Sinsay QA Specialist"), policy rules, and defect taxonomy
* Output structured JSON: `{"status": "APPROVED" | "REJECTED", "reason": "..."}` plus conversational explanation
* If uncertain, reject with explanation of missing/unclear information
