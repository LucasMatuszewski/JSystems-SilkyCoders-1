# Backend Agent Guidelines (Java / Spring Boot)

> **This file governs all AI agents working on the Spring Boot backend.**
> It supplements — and does not replace — the root `AGENTS.md` (also accessible as `CLAUDE.md`).
> **All procedures defined here are mandatory. Non-compliance blocks commits and task completion.**

**Stack**: Spring Boot 3.5.9 · Java 21 · Maven · Spring AI · Spring Data JPA · SQLite · OpenAI GPT-4o

---

## Directory Structure

```
src/
├── main/java/com/silkycoders1/jsystemssilkycodders1/   # Spring Boot entry point and source
│   ├── config/       # Spring configuration classes
│   ├── controller/   # REST controllers
│   ├── service/      # Business logic
│   └── model/        # JPA entities and DTOs
├── main/resources/
│   ├── application.properties
│   ├── static/       # Frontend build output (bundled here)
│   └── templates/
└── test/java/com/silkycoders1/jsystemssilkycodders1/   # JUnit test classes (mirrored structure)
```

---

## Backend Quality Standards

- All new Java classes must have corresponding JUnit 5 test classes
- Test coverage must reflect the full documented behavior (happy path + edge cases + error cases)
- The SSE `/api/chat` endpoint must be covered by integration tests verifying stream format compliance with the Vercel AI SDK Data Stream Protocol
- AI model integration (Spring AI / OpenAI) must be tested with mocked responses — never call the live API in tests
- Persistence layer (SQLite + JPA) must be tested with `@DataJpaTest` and an in-memory database
- No API keys or secrets may appear in any test or source file
- All tests must pass (`./mvnw test`) before committing
- Code must compile cleanly with no warnings (`./mvnw clean compile`)

---

## Backend Implementation Rules (Critical)

- **Streaming format**: SSE must emit Vercel Data Stream chunks (`0:"text"` and `8:[{...}]`), not raw JSON
- **Endpoint**: `POST /api/chat` returns `text/event-stream` and maps the Spring AI stream to the Vercel format
- **AI stack**: use `spring-ai-starter-model-openai` with chat model `gpt-4o` and `Media` attachments for images
- **Prompt policy**: select system prompt based on `intent` (`return` vs `complaint`). Respond to users in Polish
- **Persistence**: use SQLite with JPA; store request metadata, transcript, and verdicts. Never commit API keys

---

## Java Coding Style & Naming Conventions

- 4-space indentation; standard Spring Boot naming conventions
- Packages: lowercase; keep package structure consistent with `com.silkycoders1...` or migrate to `com.sinsay` only if the ADR is implemented
- Classes: UpperCamelCase; methods/fields: lowerCamelCase
- Tests: `*Tests` suffix, mirrored package structure under `src/test/`
- Lombok annotations used consistently; do not mix manual getters/setters with Lombok
- No unused imports, dead code, or commented-out blocks

---

## Testing Infrastructure

> Spring Boot: **3.5.9** | Java: **21** | Build: Maven

### Current Test Dependencies (already in `pom.xml`)

`spring-boot-starter-test` is included in `test` scope and transitively provides:

- **JUnit 5** (Jupiter) — test engine
- **Mockito** — mocking framework
- **AssertJ** — fluent assertions
- **Spring Test** — `MockMvc`, `@SpringBootTest`, test slices
- **Hamcrest** — matcher library (use AssertJ preferentially)
- **JSONassert** and **JsonPath** — JSON response assertions

### Additional Dependencies to Add as Needed

When the task requires them, add to `pom.xml`:

```xml
<!-- Testcontainers (for integration tests with real DBs) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- SQLite in-memory for @DataJpaTest (if switching from file-based) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Awaitility (for async/SSE stream testing) -->
<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <scope>test</scope>
</dependency>
```

### Test Class Naming and Location

- Test classes: `src/test/java/...` mirroring the main source package structure
- Naming: `*Tests` suffix (e.g., `ChatControllerTests`, `OrderServiceTests`)
- Unit tests: no Spring context — `@ExtendWith(MockitoExtension.class)`
- API/slice tests: `@WebMvcTest(ControllerClass.class)`
- Full integration tests: `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- Persistence tests: `@DataJpaTest`

---

## TDD Process (Backend)

Follow the universal TDD process from the root `AGENTS.md`. Backend-specific commands:

### Step 4 — Confirm Tests Fail

```bash
./mvnw test -Dtest=YourTestClass
```

Tests must fail at this point (no functionality implemented yet). If they pass, the test is not testing anything.

### Step 6 — Confirm All Tests Pass

```bash
./mvnw test
```

All tests must pass. If they don't, fix the implementation (not the test) unless the test itself was wrong.

### Test Style

Use BDD-style descriptive names and `given / when / then` structure with Mockito `BDDMockito`:

```java
@Test
void shouldReturnStreamedVerdictForValidReturnRequest() {
    // given
    given(service.processRequest(any())).willReturn(mockFlux);

    // when
    var result = controller.chat(request);

    // then
    assertThat(result).isNotNull();
}
```

---

## Build and Development Commands

```bash
./mvnw spring-boot:run              # run backend locally
./mvnw test                         # run all JUnit tests
./mvnw test -Dtest=ClassName        # run a single test class
./mvnw clean compile                # compile only, check for warnings
./mvnw clean package                # build JAR
./mvnw clean verify                 # full build + test validation
```

---

## Pre-Commit Checklist (Backend)

**In addition to the universal checklist in root `AGENTS.md`, verify:**

```
□ All new Java classes have corresponding test classes
□ All tests pass: ./mvnw test (zero failures, zero errors)
□ Code compiles cleanly: ./mvnw clean compile (no warnings)
□ No .db database files staged for commit
□ If touching the SSE adapter: verified stream format matches Vercel AI SDK Data Stream Protocol
□ If touching AI model calls: verified with mocked responses, not live API
□ Context7 MCP used to check latest docs for Spring AI / Spring Boot APIs used
```

---

## Task Completion Criteria (Backend)

In addition to the universal criteria in root `AGENTS.md`, a backend task requires:

- All new Java classes have test classes written before production code
- `./mvnw test` exits with zero failures and zero errors
- `./mvnw clean compile` produces no warnings
- SSE endpoint (if touched) verified against the Vercel AI SDK Data Stream Protocol
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

| Handler                                  | Use when                                        |
| ---------------------------------------- | ----------------------------------------------- |
| `/websites/spring_io_projects_spring-ai` | Implementing AI model calls, streaming, prompts |
| `/spring-projects/spring-boot`           | Spring Boot configuration, auto-configuration   |
| `/projectlombok/lombok`                  | Using Lombok annotations                        |
| `/openai/openai-java`                    | OpenAI Java SDK usage                           |
| `/websites/platform_openai`              | OpenAI platform API details                     |
