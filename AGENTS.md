# AI Agent Instructions / Project Coding Guidelines

This document provides guidelines for developing and maintaining the `JSystems-SilkyCoders-1` project.

## 1. General Java Development (Backend)
- **Java Version:** Ensure you are using the Java version specified in `pom.xml`.
- **Lombok:** Use Lombok to reduce boilerplate.
    - Prefer `@RequiredArgsConstructor` for constructor injection.
    - Use `@Slf4j` for logging.
    - Use `@Getter`, `@Setter`, `@Builder`, and `@Data` as appropriate for DTOs and Entities.
- **Naming Conventions:**
    - Classes: `PascalCase`
    - Methods and Variables: `camelCase`
    - Constants: `SCREAMING_SNAKE_CASE`
- **Access Modifiers:**
    - Use the most restrictive access level possible. Controllers and their implementations can often be package-private if they are not needed outside their package.
- **Optional:** Use `Optional<T>` for return types where a value might be missing, rather than returning `null`.

## 2. REST API Development (Backend)
- **Interface-First approach (Target Goal):** Define the API using interfaces.
    - Use `@RequestMapping` on the interface to define the base path.
    - Use Spring Web annotations (`@GetMapping`, `@PostMapping` etc.) on interface methods.
    - Define path constants within the interface.
- **Controller Implementation:**
    - Annotate with `@RestController`.
    - Use `@RequiredArgsConstructor` for dependency injection.
    - Keep controllers thin; delegate business logic to Services.
    - **Current State:** Since this is a single-module app, keep interfaces and implementations in appropriate packages structure (e.g., `controller.api` vs `controller.impl` or similar separation if possible).
- **DTOs:** Use DTOs for request and response bodies. Record classes are preferred for simple data containers.

## 3. Service Layer (Backend)
- **Annotations:** Use `@Service` for service classes.
- **Business Logic:** All core business logic should reside in the Service layer.

## 4. Data Access (Backend)
> **Note:** Currently, no database is used. The following principles apply for future reference or if a DB is introduced.
- **Spring Data JPA:** Use Spring Data JPA interfaces for standard CRUD operations.
- **Entities:** Use JPA annotations (`@Entity`, `@Table`, etc.).

## 5. Testing (Backend)
- **Frameworks:** JUnit 5, Mockito, AssertJ, and Instancio.
- **Test Structure:** Use the `given-when-then` (or `given-and-expect`) pattern.
- **Mocking:**
    - Use `@ExtendWith(MockitoExtension.class)`.
    - Use `@Mock` for dependencies and `@InjectMocks` for the class under test.
    - Prefer `Mockito.when(...).thenReturn(...)` consistently.
- **Data Generation:** Use **Instancio** (`Instancio.create(...)`) to generate test data/DTOs to keep tests concise and resilient to schema changes.
- **Assertions:** Use **AssertJ** (`assertThat(...)`) for readable and powerful assertions.
- **Exception Testing:** Use `assertThrows(...)` or AssertJ's `assertThatThrownBy(...)`.
- **Coverage:** Aim for high coverage of business logic in Services and Mappers.

## 6. Error Handling (Backend)
- Use custom exceptions where appropriate.
- Leverage `BadRequestException` with `ErrorLevel` (or equivalent global exception handler mechanism).

## 7. Logging (Backend)
- **Debug:** `log.debug(...)` for development/troubleshooting.
- **Info:** `log.info(...)` for important lifecycle events (e.g., "Created a new record with ID {}").
- **Error:** `log.error(...)` for actual errors, usually accompanied by an exception.

---

## 8. Frontend Development (React + Vite)
- **Framework**: React 18+ with Vite.
- **Language**: JavaScript (JSX). *Prefer TypeScript if possible, but currently initialized as JS.*
- **Component Structure**:
    - Functional Components only.
    - Use Hooks (`useState`, `useEffect`, or custom hooks) for state and side effects.
    - One component per file, PascalCase naming (e.g., `UserProfile.jsx`).
- **Styling & UI Library**:
    - **Material UI (MUI)**: Use `@mui/material` components as the primary UI library.
    - Use `sx` prop or `styled` API for custom styling overrides.
    - Avoid direct CSS/SCSS unless necessary for global overrides.
    - *No TailwindCSS* (unless explicitly requested).
- **API Integration**:
    - Use `fetch` or `axios` (if added).
    - **Proxy**: API requests to `/api/...` are proxied to the backend via `vite.config.js`. Do not hardcode `localhost:8080` in component code.
- **State Management**:
    - Keep state local where possible.
    - Lift state up only when necessary or use Context API for global state.
- **Testing**:
    - (Future) Vitest + React Testing Library.
