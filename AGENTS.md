## Build & Run

Succinct rules for how to BUILD the project:

- **Run Backend:** `./mvnw spring-boot:run`
- **Build JAR:** `./mvnw clean package`
- **Java Version:** 21
- **Wrapper:** Use `./mvnw` (Linux/Mac) or `mvnw.cmd` (Windows)

## Validation

Run these after implementing to get immediate feedback:

- **All Tests:** `./mvnw test`
- **Specific Test:** `./mvnw -Dtest=TestClassName test`
- **Verify Package:** `./mvnw clean package -DskipTests=false`
- **Frontend Validation (Planned):** `npm test`, `npm run lint`, `npm run format:check`

## Critical Implementation Rules

### Backend (Spring Boot + Spring AI)
- **Streaming format**: SSE must emit Vercel Data Stream chunks (`0:"text"` and `8:[{...}]`), not raw JSON.
- **Endpoint**: `POST /api/chat` returns `text/event-stream` and maps the Spring AI stream to the Vercel format.
- **AI stack**: use `spring-ai-starter-model-openai` with chat model `gpt-4o` and `Media` attachments for images.
- **Prompt policy**: select system prompt based on `intent` (`return` vs `complaint`). Respond to users in Polish.
- **Persistence**: use SQLite with JPA; store request metadata, transcript, and verdicts. Never commit API keys.

### Frontend (React 19 + assistant-ui) - Planned
- **Form**: order number, purchase date, intent, description, image upload; validate with Zod.
- **Chat UI**: `assistant-ui` components and `useChat` from Vercel AI SDK targeting `/api/chat`.
- **Image handling**: resize images to max 1024px on the client before upload.
- **TypeScript**: Strict mode, interfaces over types, avoid `any` and `as/!`. PascalCase for components.

## Operational Notes & Standards

- **Database:** SQLite (local file, do not commit `.db` files).
- **Security:** API keys in env vars (e.g., `OPENAI_API_KEY`).
- **Docs:** Read `docs/PRD-Sinsay-PoC.md` and `docs/ADR-Sinsay-PoC.md` for full context.

### Codebase Patterns
- **Java:** 4-space indentation. Standard Spring Boot layering (Controller/Service/Repo).
- **Lombok:** Use for boilerplate (`@Data`, `@RequiredArgsConstructor`).
- **Tests:** `*Tests` suffix. Write tests *alongside* features (backend integration tests for SSE are critical).
- **Commits:** Follow `Area: short summary` (e.g., `Feature: add chat endpoint`).
- **Spring AI:** Version 1.1.2 uses `org.springframework.ai.content.Media` and `UserMessage.builder()`.
- **JDK:** Use JDK 17 for compilation.

## Documentation Sources (Context7 MCP)

Use these handlers to fetch up-to-date documentation:
- `/websites/spring_io_projects_spring-ai`
- `/spring-projects/spring-boot`
- `/projectlombok/lombok`
- `/openai/openai-java`
- `/websites/platform_openai`
- `/vercel/ai`
- `/assistant-ui/assistant-ui`
- `/reactjs/react.dev`
- `/tailwindlabs/tailwindcss.com`
- `/shadcn-ui/ui`
