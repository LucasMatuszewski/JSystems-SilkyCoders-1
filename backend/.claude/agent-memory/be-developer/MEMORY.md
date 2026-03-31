# BE Developer Memory

## Project Context

**Sinsay AI PoC** — Spring Boot 3.5.9 + Java 21 backend for multimodal AI assistant handling returns/complaints.

## Key File Locations

- **Entities**: `backend/src/main/java/com/sinsay/model/`
- **Repositories**: `backend/src/main/java/com/sinsay/repository/`
- **Services**: `backend/src/main/java/com/sinsay/service/`
- **Test classes**: `backend/src/test/java/com/sinsay/` (mirrored package, `*Tests` suffix)
- **Policy docs**: `docs/regulamin.md`, `docs/reklamacje.md`, `docs/zwrot-30-dni.md`
- **ADR docs**: `docs/ADR/000-main-architecture.md`, `docs/ADR/001-backend.md`

## Architecture Decisions

### Data Models
- `Session`: UUID PK (auto-generated), Intent enum, orderNumber, productName, description, createdAt
- `ChatMessage`: UUID PK, sessionId (FK), Role enum, content (TEXT), sequenceNumber, createdAt
- Enums: `Intent` (RETURN, COMPLAINT), `Role` (USER, ASSISTANT)
- JPA with SQLite + Hibernate Community Dialects

### System Prompt Structure (PolicyDocService)
The `getSystemPrompt(Intent)` method assembles 6 sections in order:
1. Role definition
2. Decision categories (zaakceptowane/odrzucone/niejasne)
3. Mandatory disclaimer (non-binding assessment)
4. Scope boundary (Sinsay policy questions only)
5. Language instruction (Polish only)
6. Policy document content (regulamin + intent-specific doc)

### Test Configuration
- **Profile**: `test` (activated via `@ActiveProfiles("test")`)
- **Database**: H2 in-memory for testing (NOT SQLite)
- **Config file**: `src/test/resources/application-test.properties`
- **Important**: In Spring Boot 3.x, do NOT set `spring.profiles.active` in profile-specific properties files

## TDD Workflow

1. Write tests first in `*Tests` class
2. Run tests — confirm they FAIL
3. Implement entities/services/repositories
4. Run tests — confirm they PASS
5. Verify: `./mvnw test && ./mvnw clean package`
6. Commit: `Area: short summary` format

## Commit Patterns

- `Backend: add JPA entities and repositories for Session and ChatMessage`
- `Backend: add PolicyDocService for intent-based system prompt assembly`

## Testing Patterns

### Repository Tests
- Use `@DataJpaTest` + `@ActiveProfiles("test")`
- Test save, findById, custom query methods
- Use `@BeforeEach` with `deleteAll()` for isolation

### Service Tests
- Use `@SpringBootTest` + `@ActiveProfiles("test")`
- Use `@TempDir` for file I/O tests
- Test both positive and negative cases
- Verify unique content markers for intent-specific behavior

### Technical Acceptance Criteria (TAC-BE)
- TAC-BE-01: RETURN prompt does NOT contain reklamacje content
- TAC-BE-02: COMPLAINT prompt does NOT contain zwrot content

## Common Issues & Solutions

### Issue: `InvalidConfigDataPropertyException: spring.profiles.active` in profile-specific file
**Solution**: Remove `spring.profiles.active=test` from `application-test.properties`. Profile is activated via `@ActiveProfiles("test")` annotation.

### Issue: Tests fail due to substring overlap in role definition
**Solution**: Use truly unique content markers from actual policy docs (e.g., "Zamówione produkty możesz reklamować" for reklamacje, "otrzymaniu przesyłki masz 30 dni" for zwrot).
