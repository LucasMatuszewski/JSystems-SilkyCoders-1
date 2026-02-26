# Frontend Unit & Integration Test Standards

> This file covers Vitest + React Testing Library tests in `frontend/src/test/`.
> For Playwright E2E tests, see `frontend/src/e2e/CLAUDE.md`.
> For the TDD process and when to run tests, see `frontend/CLAUDE.md`.
> For test quality philosophy, see `.claude/agents/java-test-writer.md`.

---

## Tech Stack

| Tool | Purpose |
|------|---------|
| Vitest | Test runner — fast, Vite-native |
| React Testing Library (`@testing-library/react`) | Component rendering and querying |
| `@testing-library/user-event` | Realistic user interaction simulation |
| `@testing-library/jest-dom` | Extended DOM matchers (`toBeInTheDocument`, `toBeDisabled`, etc.) |
| jsdom | DOM environment for Vitest (configured in `vitest.config.ts`) |
| MSW (Mock Service Worker) | Mock API calls including SSE streams |

Setup file: `frontend/src/test/setup.ts` — imports `@testing-library/jest-dom` globally.

---

## Commands

```bash
cd frontend
pnpm test          # run all Vitest tests once
pnpm test:watch    # watch mode
pnpm test:ui       # Vitest UI
```

---

## File Conventions

- Test files live in `frontend/src/test/` mirroring the `app/` structure
- Naming: `*.test.tsx` for components, `*.test.ts` for utilities and hooks
- Examples: `src/test/component/ReturnForm.test.tsx`, `src/test/lib/schemas.test.ts`

---

## Querying Elements — Priority Order

Use queries in this priority order (most preferred first):

1. `getByRole` — semantic HTML roles: `getByRole('button', { name: /wyślij/i })`
2. `getByLabelText` — form fields with associated labels: `getByLabelText(/nazwa produktu/i)`
3. `getByText` — visible text: `getByText(/zwrot możliwy/i)`
4. `getByTestId` — when no semantic query applies: `getByTestId('verdict-approved')`

**Never** query by CSS class (`.copilotKitMessages`) or DOM structure (`div > span`). These break when the implementation changes, not when the behavior changes.

---

## What to Test in Components

| Category | What to assert |
|----------|----------------|
| Rendering | Key elements are present; Polish labels are visible |
| Props | Different prop values produce correct output (e.g., `status: 'complete'` disables inputs) |
| User interactions | Typing, clicking, form submission trigger the correct behavior |
| Validation | Invalid input shows the correct Polish error message |
| Disabled states | Submit button is disabled when required fields are empty |
| Conditional rendering | Conditional sections appear/disappear based on props or state |

---

## Component Test Pattern

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ReturnForm } from '../../app/component/ReturnForm';

describe('ReturnForm', () => {
  const mockRespond = vi.fn();
  const defaultProps = {
    args: { type: 'return' },
    status: 'inProgress' as const,
    respond: mockRespond,
  };

  beforeEach(() => {
    mockRespond.mockClear();
  });

  it('renders all required fields with Polish labels', () => {
    render(<ReturnForm {...defaultProps} />);
    expect(screen.getByLabelText(/nazwa produktu/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/opis problemu/i)).toBeInTheDocument();
  });

  it('shows Polish validation error when productName is empty on submit', async () => {
    const user = userEvent.setup();
    render(<ReturnForm {...defaultProps} />);

    await user.click(screen.getByRole('button', { name: /wyślij/i }));

    await waitFor(() => {
      expect(screen.getByText(/nazwa produktu jest wymagana/i)).toBeInTheDocument();
    });
  });
});
```

---

## What NOT to Test in Unit/Integration Tests

- The CopilotKit library internals — these are tested by CopilotKit's own suite
- CSS styling — visual correctness is verified by Playwright screenshots
- The actual SSE stream processing — test with MSW mocks or Playwright E2E

---

## Mocking API Calls (MSW)

Use MSW to intercept calls to `/api/langgraph4j` in integration tests. This allows testing the full component tree without requiring a running backend.

```typescript
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';

const server = setupServer(
  http.post('/api/langgraph4j', () => {
    // Return a canned SSE stream response
    return HttpResponse.text('data: {"type":"RUN_STARTED"}\n\n', {
      headers: { 'Content-Type': 'text/event-stream' },
    });
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

MSW mock responses should be based on real payloads captured from the running backend — do not invent event structures.

---

## data-testid Requirements

Every interactive and observable element must have a `data-testid`. The complete list of required testids:

**SinsayChat.tsx**: `chat-input`, `chat-send-btn`
**ReturnForm.tsx**: `return-form`, `form-product-name`, `form-type`, `form-description`, `form-photo-upload`, `form-submit-btn`
**VerdictMessage.tsx**: `verdict-approved`, `verdict-rejected`

If you add a new observable element that tests need to select, add a `data-testid` to it immediately and update this list.
