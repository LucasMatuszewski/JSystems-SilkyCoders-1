# Repository Guidelines

**Primary references**: see `docs/PRD-Sinsay-PoC.md` and `docs/ADR-Sinsay-PoC.md` before making changes.

## Project Overview
This repo hosts a Proof of Concept for Sinsay returns/complaints verification using multimodal AI. The target flow is a form-to-chat experience where users submit order context and images, then receive a streamed verdict in Polish. The backend is Spring Boot + Spring AI; the frontend is React 19 + assistant-ui. Streaming must follow the Vercel AI SDK Data Stream Protocol.

## Current vs. Planned Structure
- **Current (in repo)**: single Spring Boot app under `src/` with Maven wrapper and minimal tests.
- **Planned (ADR)**: a monorepo with separate `backend/` and `frontend/` directories and a build that bundles the frontend into the backend `static/` folder. If you introduce the monorepo layout, keep the existing root `pom.xml` aligned or migrate intentionally.

## Project Structure & Module Organization
- `src/main/java/com/silkycoders1/jsystemssilkycodders1/`: Spring Boot entry point.
- `src/main/resources/`: `application.properties`, plus `static/` and `templates/` (frontend build output targets `static/`).
- `src/test/java/com/silkycoders1/jsystemssilkycodders1/`: JUnit tests.
- `docs/`: PRD, ADR, and research notes used as requirements.
- `docs/sinay/`: terms of returns and complaints from Sinsay as input for AI Agent system prompt / knowledge base. 

### Target Modules (from ADR)
If/when split into modules, use the following layout and names:
- `backend/src/main/java/com/sinsay/`: `config/`, `controller/`, `service/`, `model/`.
- `frontend/src/`: `app/` (screens), `components/ui/` (Shadcn), form + chat components.

## Build, Test, and Development Commands
- `./mvnw spring-boot:run`: run backend locally.
- `./mvnw test`: run JUnit tests.
- `./mvnw clean package`: build JAR.

Planned commands once frontend exists:
- `cd frontend && npm run dev`: run React app.
- `cd frontend && npm run build`: build frontend into `backend/src/main/resources/static`.

## Backend Implementation Rules (Critical)
- **Streaming format**: SSE must emit Vercel Data Stream chunks (`0:"text"` and `8:[{...}]`), not raw JSON.
- **Endpoint**: `POST /api/chat` returns `text/event-stream` and maps the Spring AI stream to the Vercel format.
- **AI stack**: use `spring-ai-starter-openai` with chat model `gpt-4o` and `Media` attachments for images.
- **Prompt policy**: select system prompt based on `intent` (`return` vs `complaint`). Respond to users in Polish.
- **Persistence**: use SQLite with JPA; store request metadata, transcript, and verdicts. Never commit API keys.

## Frontend Implementation Rules (Planned)
- **Form**: order number, purchase date, intent, description, image upload; validate with Zod.
- **Chat UI**: `assistant-ui` components and `useChat` from Vercel AI SDK targeting `/api/chat`.
- **Image handling**: resize images to max 1024px on the client before upload.

## Coding Style & Naming Conventions
- Java: 4-space indentation; standard Spring Boot conventions.
- Packages: lowercase; keep package structure consistent with `com.silkycoders1...` or migrate to `com.sinsay` only if the ADR is implemented.
- Classes: UpperCamelCase; methods/fields: lowerCamelCase.
- Tests: `*Tests` suffix, mirrored package structure.
- TypeScript/React (planned): use explicit types, PascalCase components, and file names matching component names.

# TypeScript Usage
- Use TypeScript for all FE code; prefer interfaces over types
- Avoid using the "any" type. Instead, prefer strict typing
- Import types from external npm packages when possible
- Create custom interfaces (preferred) or types for our custom code
- Avoid type assertions with `as` or `!` when possible
- Use functional components with TypeScript interfaces
- Use strict mode in TypeScript for better type safety
- Use Type Guards for additional safety in execution time!

## Testing Guidelines
- Backend: `spring-boot-starter-test` (JUnit 5). Add tests for the `/api/chat` SSE adapter and persistence mapping.
- Frontend: use Vitest + Testing Library from the start; write both unit and integration tests for form validation, chat streaming, and UI state transitions.
- Treat `npm test` (Vitest), `npm run lint` (ESLint), and `npm run format` / `npm run format:check` (Prettier) as the primary validation loop for frontend changes.
- Always write tests alongside new components, classes or flows, not after the feature is complete. You should use them to validate if your changes work as expected.

## Commit & Pull Request Guidelines
- Current commit history uses prefixes like `Docs:`. Follow `Area: short summary` (e.g., `Docs:`, `Feature:`, `Fix:`).
- PRs should include: goal, scope of changes, and any required setup notes (env vars, database files, API keys).

## Security & Configuration
- Configure keys in environment variables (e.g., `OPENAI_API_KEY`).
- SQLite DB file should be local-only; avoid committing `.db` files.

## Agent Workflow Expectations
- Start by reading `docs/PRD-Sinsay-PoC.md` and `docs/ADR-Sinsay-PoC.md`.
- Keep changes aligned with the PoC scope (no auth, no production deployment).
- When adding new structure (backend/frontend), update this guide accordingly.

## Documentation from Context7 MCP Tools

To get newest documentation for tools we use in this project, you may use below Context7 MCP libraries (handlers to use to fetch documentation):

- /websites/spring_io_projects_spring-ai
- /spring-projects/spring-boot
- /projectlombok/lombok
- /openai/openai-java
- /websites/platform_openai
- /vercel/ai
- /assistant-ui/assistant-ui
- /reactjs/react.dev
- /tailwindlabs/tailwindcss.com
- /shadcn-ui/ui
