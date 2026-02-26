# Backend Agent Guidelines (Java / Spring Boot)

> **This file governs all AI agents working on the Spring Boot backend.**
> It supplements — and does not replace — the root `CLAUDE.md`.
> **All procedures defined here are mandatory. Non-compliance blocks commits and task completion.**

**Stack**: Spring Boot 3.5.9 · Java 21 · Maven · Spring WebFlux · LangGraph4j 1.6.2 · Spring AI 1.0.0 (OpenAI + Ollama) · Reactor · Lombok

---

## Key Dependencies (pom.xml)

| Dependency                              | Version          | Purpose                                     |
| --------------------------------------- | ---------------- | ------------------------------------------- |
| `spring-boot-starter-webflux`           | (Boot 3.5.9)     | Reactive web server (Netty), SSE streaming  |
| `langgraph4j-core`                      | 1.6.2            | Graph-based agent orchestration             |
| `langgraph4j-spring-ai`                 | 1.6.2            | Spring AI integration for LangGraph4j       |
| `langgraph4j-springai-agentexecutor`    | 1.6.2            | Agent executor with tool calling support    |
| `spring-ai-openai`                      | 1.0.0            | OpenAI / GitHub Models chat model adapter   |
| `spring-ai-ollama`                      | 1.0.0            | Ollama local LLM chat model adapter         |
| `lombok`                                | (Boot-managed)   | Boilerplate reduction annotations           |
| `spring-boot-starter-test`              | (Boot 3.5.9)     | JUnit 5, Mockito, AssertJ, Spring Test      |
| `reactor-test`                          | (Boot-managed)   | Reactor Flux/Mono testing utilities         |

---

## Directory Structure

```
src/
├── main/java/
│   ├── com/silkycoders1/jsystemssilkycodders1/
│   │   └── JSystemsSilkyCodders1Application.java   # Entry point + AGUIAgent bean definitions
│   └── org/bsc/langgraph4j/agui/                   # AG-UI protocol implementation
│       ├── AGUIAgent.java                           # Agent interface (returns Flux<AGUIEvent>)
│       ├── AGUIEvent.java                           # AG-UI event types (polymorphic records)
│       ├── AGUIType.java                            # AG-UI data types (RunAgentInput, Tool, etc.)
│       ├── AGUIMessage.java                         # AG-UI message types (TextMessage, ResultMessage)
│       ├── AGUILangGraphAgent.java                  # Abstract base: LangGraph4j → AG-UI event bridge
│       ├── AGUIAgentExecutor.java                   # Concrete agent with tool calling + model fallback
│       ├── AGUISampleAgent.java                     # Minimal "Hello World" test agent
│       └── AGUISSEController.java                   # REST controller: POST /langgraph4j/copilotkit
├── main/resources/
│   └── application.properties                       # Server port, model config, agent selection
└── test/java/
    ├── com/silkycoders1/jsystemssilkycodders1/      # App-level tests (bean wiring, context)
    └── org/bsc/langgraph4j/agui/                    # AG-UI unit tests (model resolution, etc.)
```

---

## Application Configuration (`application.properties`)

```properties
server.port=8085
spring.main.web-application-type=reactive

# Agent selection: "agentExecutor" (LangGraph4j with tools) or "sample" (echo agent)
ag-ui.agent=agentExecutor

# Primary AI model (enum name from AGUIAgentExecutor.AiModel)
# Fallback chain: configured model → OPENAI → GITHUB_MODELS → OLLAMA_QWEN2_5
# Available: OLLAMA_KIMI_K2_5_CLOUD, OLLAMA_QWEN2_5_7B, OLLAMA_QWEN3_14B,
#            OPENAI_GPT_4O_MINI (needs OPENAI_API_KEY), GITHUB_MODELS_GPT_4O_MINI (needs GITHUB_MODELS_TOKEN)
ag-ui.model=OLLAMA_KIMI_K2_5_CLOUD
```

---

## Architecture: AG-UI Protocol Flow

```
Frontend (CopilotKit)  →  POST /api/langgraph4j (Next.js route)
                       →  POST http://localhost:8085/langgraph4j/copilotkit (Spring Boot SSE)
                       →  AGUISSEController → AGUIAgent.run(input) → Flux<AGUIEvent>
                       →  LangGraph4j graph execution → streamed AG-UI events back to frontend
```

Key classes:
- `AGUISSEController` — REST endpoint, deserializes `RunAgentInput`, delegates to `AGUIAgent` bean
- `AGUIAgentExecutor` — builds LangGraph4j graph with tool support, resolves AI model with fallback chain
- `AGUILangGraphAgent` — abstract base that converts LangGraph4j `NodeOutput`/`StreamingOutput` to AG-UI events

---

## Backend Quality Standards

- All new Java classes must have corresponding JUnit 5 test classes
- Test coverage must reflect the full documented behavior (happy path + edge cases + error cases)
- AI model integration (Spring AI / OpenAI / Ollama) must be tested with mocked responses — never call the live API in tests
- No API keys or secrets may appear in any test or source file
- All tests must pass (`./mvnw test`) before committing
- Code must compile cleanly with no warnings (`./mvnw clean compile`)

---

## Backend Implementation Rules (Critical)

- **Reactive stack**: this app uses WebFlux (not Spring MVC). All controllers return `Flux`/`Mono`, not blocking types
- **Streaming format**: SSE via AG-UI protocol events (not Vercel Data Stream format). Events are typed JSON objects with `type` discriminator
- **Endpoint**: `POST /langgraph4j/copilotkit` returns `text/event-stream` with AG-UI events
- **AI models**: configured via `ag-ui.model` property in `application.properties`. Model fallback chain is in `AGUIAgentExecutor.resolveModel()`
- **Two packages**: app entry + beans in `com.silkycoders1.jsystemssilkycodders1`; AG-UI protocol code in `org.bsc.langgraph4j.agui`. Both are component-scanned

---

## Java Coding Style & Naming Conventions

- 4-space indentation; standard Spring Boot naming conventions
- Packages: `com.silkycoders1.jsystemssilkycodders1` (app), `org.bsc.langgraph4j.agui` (AG-UI protocol)
- Classes: UpperCamelCase; methods/fields: lowerCamelCase
- Tests: `*Tests` suffix, mirrored package structure under `src/test/`
- Lombok annotations used consistently; do not mix manual getters/setters with Lombok
- No unused imports, dead code, or commented-out blocks

---

## Testing

**For how to write tests, patterns, library details, and project-specific conventions, see [`src/test/CLAUDE.md`](../src/test/CLAUDE.md).**

TDD is mandatory for all backend work. See the root `CLAUDE.md` for the full TDD process.

### Test Commands

```bash
./mvnw test                          # run all tests
./mvnw test -Dtest=ClassName         # run a single test class
./mvnw test -Dtest=ClassName#method  # run a single test method
./mvnw clean compile                 # compile only, check for warnings
./mvnw clean verify                  # full build + all tests
```

Run tests after changing `.java` or `pom.xml` files only. Do NOT run after changing only `.md` or frontend files.

### Always Run the Full Test Suite Before Committing

```bash
./mvnw test
```

All tests must pass. If they don't, fix the implementation (not the test) unless the test itself was wrong.

---

## Build and Development Commands

```bash
./mvnw spring-boot:run              # run backend locally (port 8085)
./mvnw test                         # run all JUnit tests
./mvnw test -Dtest=ClassName        # run a single test class
./mvnw clean compile                # compile only, check for warnings
./mvnw clean package                # build JAR
./mvnw clean verify                 # full build + test validation
```

**When to compile/test**: Only run `./mvnw compile` or `./mvnw test` after changing compilable files (`.java`, `pom.xml`, `application.properties`). Do NOT compile or test after changing only non-compilable files (`.md`, `.txt`, frontend files, docs). This avoids wasting time on unnecessary builds.

---

## Pre-Commit Checklist (Backend)

**In addition to the universal checklist in root `CLAUDE.md`, verify:**

```
□ All new Java classes have corresponding test classes
□ All tests pass: ./mvnw test (zero failures, zero errors)
□ Code compiles cleanly: ./mvnw clean compile (no warnings)
□ No .db database files staged for commit
□ If touching the SSE controller: verified AG-UI event format is correct
□ If touching AI model calls: verified with mocked responses, not live API
□ If touching model config: verified fallback chain still works in tests
□ Context7 MCP used to check latest docs for Spring AI / Spring Boot / LangGraph4j APIs used
```

---

## Task Completion Criteria (Backend)

In addition to the universal criteria in root `CLAUDE.md`, a backend task requires:

- All new Java classes have test classes written before production code
- `./mvnw test` exits with zero failures and zero errors
- `./mvnw clean compile` produces no warnings
- SSE endpoint (if touched) verified against the AG-UI protocol event format
- AI model calls (if touched) tested with mocked responses only — never against the live API

---

## MCP and Skills available to use in backend

### Skills (reusable workflows)

#### `java-spring-boot` — Spring Boot Development

- **Invocation**: `Skill("java-spring-boot")`
- **Covers**: REST APIs with Spring MVC/WebFlux, Spring Security, Spring Data JPA, Actuator, production-ready configuration patterns.
- **When to use**: At the start of any task involving backend Spring Boot code.
- **Security note**: Has `Bash` tool access. Review any shell commands before execution.

#### `java-testing` — Java Testing

- **Invocation**: `Skill("java-testing")`
- **Covers**: JUnit 5, Mockito, AssertJ, Testcontainers, Spring Boot Test slices, MockMvc, TDD patterns, JaCoCo coverage.
- **When to use**: When writing any Java test class.
- **Security note**: Flagged as High Risk by Gen Agent Trust Hub — risk is from overly broad `Bash` permissions only, no malicious code found. Review Bash commands before running.

### MCP Servers (Model Context Protocol)

#### `jetbrains` — IntelliJ IDEA IDE Integration

- **What it does**: Bridges Claude Code with IntelliJ IDEA — open files, navigate symbols, run configurations, access project structure, read diagnostics.
- **When to use**: When navigating the Java codebase, running Spring Boot, or checking live compiler errors before committing.
- **Note**: Requires IntelliJ IDEA to be running with the MCP server plugin active.

#### `context7` — Live Library Documentation

Fetch these before implementing features that depend on them:

| Handler                                  | Use when                                              |
| ---------------------------------------- | ----------------------------------------------------- |
| `/websites/spring_io_projects_spring-ai` | Implementing AI model calls, streaming, prompts        |
| `/spring-projects/spring-boot`           | Spring Boot configuration, auto-configuration          |
| `/projectlombok/lombok`                  | Using Lombok annotations                               |
| `/openai/openai-java`                    | OpenAI Java SDK usage                                  |
| `/websites/platform_openai`              | OpenAI platform API details                            |
