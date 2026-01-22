# AGENTS.md

**Project:** Monolithic PoC for Sinsay's AI-driven returns & complaints verification system. Spring Boot 3.5.9 (Java 21) backend + React 19 frontend, embedded deployment via Maven. Uses local Ollama with Gemma3 model for image analysis.

**Reference:** See `prd-ai-returns-complaints-verification-system.md` for complete requirements.

## Architectural Invariants

These constraints apply to **every task**. Do not violate them.

* **Deployment:** Frontend built into `src/main/resources/static`, served by Spring Boot. Single JAR deployment. No separate pipelines.
* **Streaming Protocol:** **MUST** use Vercel AI SDK Data Stream Protocol (`0:"text"`, `8:[data]`, `e:{error}`). Do NOT use standard SSE format.
* **State Management:** `react-hook-form` + `zod` for forms; `useChat` (Vercel SDK) for AI chat. No Redux.
* **Data Persistence:** None. All processing in-memory only. No database, file storage, or logging.
* **Authentication:** Public access only. No login, registration, or user accounts.

## Build & Development

* **Build Tool:** Maven (`./mvnw`). Frontend built via `frontend-maven-plugin` and embedded in JAR.
* **Backend:** `./mvnw spring-boot:run` (port 8080)
* **Frontend Dev:** `cd frontend && npm run dev` (port 5173, proxies `/api` to 8080)
* **Full Build:** `./mvnw clean install`
* **Configuration:** Ensure Ollama is running and Gemma3 model is available. Configure `spring.ai.ollama.base-url` in `application.yml` (default: `http://localhost:11434`)

## Detailed Guidelines

- [Backend Guidelines](./docs/backend.md) - Spring Boot, Spring AI, controllers, services
- [Frontend Guidelines](./docs/frontend.md) - React, forms, image handling, UI
- [API Specification](./docs/api.md) - Endpoint contracts and request/response formats
- [AI & Prompting](./docs/ai-prompting.md) - System prompts, AI service logic, defect taxonomy
- [Domain Dictionary](./docs/domain.md) - Business terms and concepts
- [Error Handling](./docs/error-handling.md) - Error patterns and user messaging
- [Success Criteria](./docs/success-criteria.md) - Testing and validation requirements
- [Out of Scope](./docs/out-of-scope.md) - Explicitly excluded features
