# E2E Tests

## Prerequisites

**BEFORE running these tests, you MUST:**

1. Start the backend server:
   ```bash
   cd backend
   export OPENROUTER_API_KEY=sk-your-key  # or OPENAI_API_KEY
   ./mvnw spring-boot:run
   ```

2. Verify backend is running:
   - Open http://localhost:8080/actuator/health in browser
   - You should see: `{"status":"UP"}`

## Running Tests

The frontend dev server is started automatically by Playwright.

```bash
cd frontend
npx playwright test
```

## What These Tests Do

These tests verify the **FULL STACK**:
- Real browser (Chrome via Playwright)
- Real frontend (Vite dev server at :5173)
- Real backend (Spring Boot at :8080)
- Real database (SQLite)
- NO API mocking

## Test Coverage

### `form.spec.ts`
- Form renders with all 5 fields
- Empty submit shows 5 validation errors (real backend validation)
- PDF upload shows format error (real backend validation)
- >10MB file shows size error (real backend validation)
- Valid submit shows loading state → chat appears (real backend processing)
- Valid WebP upload works
- Visual validation screenshots

### `chat-flow.spec.ts`
- Form submit → chat view appears with AI message (real backend)
- Session ID stored in localStorage (real backend response)
- Chat input visible and user can type
- "Nowa sesja" button clears session and shows form
- Send message → response streams in (real backend streaming)
- Session resume on page reload
- Visual validation screenshots

## Screenshots

Screenshots are saved to `tests/e2e/screenshots/` for visual validation.

## Logs

Test logs are written to `logs/e2e-tests.log` in the repository root.

## Troubleshooting

### Backend not running
```
Error: Backend is not running or not accessible
```
**Solution:** Start the backend with `cd backend && ./mvnw spring-boot:run`

### Health check timeout
```
Error: Backend health check timed out
```
**Solution:** Verify backend is healthy at http://localhost:8080/actuator/health

### Tests fail with "chat view did not appear"
**Solution:** Check backend logs for errors. Verify OPENROUTER_API_KEY is set.

## Manual Testing BEFORE Running Tests

Before running automated tests, verify manually:

1. Open http://localhost:5173
2. Fill form with real data
3. Upload real image from `assets/example-images/`
4. Click submit
5. Verify chat appears with AI message
6. Send follow-up message
7. Verify response appears

**ONLY when manual test passes, run automated tests.**
