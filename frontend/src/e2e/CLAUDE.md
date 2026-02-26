# Frontend E2E Test Standards (Playwright)

> This file covers Playwright end-to-end tests in `frontend/src/e2e/`.
> For Vitest unit/integration tests, see `frontend/src/test/CLAUDE.md`.
> For the TDD process and when to run tests, see `frontend/CLAUDE.md`.

---

## Tech Stack

| Tool | Purpose |
|------|---------|
| `@playwright/test` | E2E test runner and browser automation |
| Chromium | Default browser for E2E tests |

---

## Commands

```bash
cd frontend
pnpm e2e                        # run all Playwright tests
pnpm e2e --headed               # run with browser visible (useful for debugging)
pnpm e2e --project=chromium     # run on a specific browser
pnpm exec playwright show-report  # open last HTML report
```

---

## Prerequisites — Full Stack Must Be Running

E2E tests exercise the real application end-to-end. They **require**:

1. **Spring Boot backend** running on port 8085 — `cd /project-root && ./mvnw spring-boot:run`
2. **Ollama** running with the configured model — `ollama serve`
3. **Next.js frontend** running on port 3000 — `cd frontend && pnpm dev`

If any of these is not running, the tests MUST fail with a clear error. There is no silent skipping. A test that passes when the backend is down provides zero value — it cannot detect regressions.

```typescript
// Example: explicit health check that fails loudly
test.beforeAll(async ({ request }) => {
  const response = await request.get('http://localhost:8085/actuator/health').catch(() => null);
  if (!response || !response.ok()) {
    throw new Error('Backend is not running on port 8085. Start it with: ./mvnw spring-boot:run');
  }
});
```

---

## Element Selection — Required Rules

Use selectors in this priority order:

1. **`getByTestId`** — preferred for app-specific observable elements: `page.getByTestId('return-form')`
2. **`getByRole`** — for semantic elements: `page.getByRole('button', { name: /wyślij/i })`
3. **`getByLabel`** — for form fields: `page.getByLabel(/nazwa produktu/i)`

**Never** select by CSS class or DOM structure:
```typescript
// WRONG — breaks when CSS is refactored
page.locator('.copilotKitMessages')
page.locator('div > span.assistant-message')

// RIGHT — stable, behavior-based
page.getByTestId('chat-messages')
page.getByRole('region', { name: 'Chat messages' })
```

CSS class selectors create tests that break due to styling changes, not behavioral regressions. They create maintenance burden without providing coverage of actual functionality.

---

## Error Handling in Tests — Hard Rules

1. **Do NOT filter out network or backend errors.** If the backend is down, the test must fail.
2. **Do NOT use `try/catch` around assertions.** Let failures propagate.
3. **Do NOT use `|| ` (OR) in assertions.** `inputCleared || messageAppeared` passes even when both conditions are false.

```typescript
// WRONG — passes when backend is down
const criticalErrors = errors.filter(e =>
  !e.includes('ERR_CONNECTION_REFUSED') &&  // hiding real errors
  !e.includes('localhost:8085')
);

// RIGHT — all errors are failures
page.on('pageerror', (err) => {
  throw err;  // fail immediately on any page error
});
```

---

## Required Test Scenarios

The following scenarios MUST have Playwright tests. If any of these is missing, it is a test coverage gap:

### 1. App loads with correct branding
- Page loads without errors
- Sinsay logo is visible
- Chat interface is rendered
- Polish placeholder text is present in the chat input

### 2. Chat sends a message
- User types a message in `data-testid="chat-input"`
- Clicks send (`data-testid="chat-send-btn"`)
- AI response appears in the chat
- **This test must FAIL if the backend does not respond**

### 3. Return form appears on intent detection
- User types a message containing "zwrot" (return keyword in Polish)
- `data-testid="return-form"` becomes visible
- This verifies the full flow: chat input → backend → tool call → form render

### 4. Form submission produces a verdict
- User fills `data-testid="return-form"` with valid data including a photo
- Clicks `data-testid="form-submit-btn"`
- Either `data-testid="verdict-approved"` or `data-testid="verdict-rejected"` appears
- **This test requires Ollama running with a vision-capable model**

### 5. No JavaScript errors on load
- Page loads and the browser console has zero errors (no filtering)

---

## Test Pattern

```typescript
import { test, expect } from '@playwright/test';

test.describe('Sinsay return form flow', () => {
  test.beforeAll(async ({ request }) => {
    const health = await request.get('http://localhost:8085/actuator/health').catch(() => null);
    if (!health?.ok()) {
      throw new Error('Backend not running. Start with: ./mvnw spring-boot:run');
    }
  });

  test('return form appears when user mentions zwrot', async ({ page }) => {
    // Fail loudly on any page error
    page.on('pageerror', (err) => { throw err; });

    await page.goto('http://localhost:3000');
    await page.getByTestId('chat-input').fill('Chcę dokonać zwrotu');
    await page.getByTestId('chat-send-btn').click();

    // Form must appear — not optional
    await expect(page.getByTestId('return-form')).toBeVisible({ timeout: 30000 });
  });

  test('form submission produces a verdict', async ({ page }) => {
    page.on('pageerror', (err) => { throw err; });

    await page.goto('http://localhost:3000');
    // ... trigger form, fill it, upload test photo, submit
    await page.getByTestId('form-submit-btn').click();

    // Verdict must appear — use OR only to cover both possible outcomes
    const approved = page.getByTestId('verdict-approved');
    const rejected = page.getByTestId('verdict-rejected');
    await expect(approved.or(rejected)).toBeVisible({ timeout: 60000 });
  });
});
```

---

## Test Images

For tests that require a photo upload, use the small test image at:
`frontend/src/e2e/fixtures/test-product.jpg`

If this file does not exist, create a minimal JPEG (any 100×100px image will do). The image must be small enough that it does not exceed the model's context window — see the image size limit in `frontend/CLAUDE.md`.

---

## Playwright Configuration

Configuration lives in `frontend/playwright.config.ts`. Key settings:
- Base URL: `http://localhost:3000`
- Default timeout: 30 seconds (increase for AI response tests to 60s)
- Browser: Chromium
- Screenshots on failure: enabled
- Test retries: 0 (flaky tests must be fixed, not retried)
