# Frontend Agent Guidelines

> **This file governs all AI agents working on the Next.js + CopilotKit frontend.**
> It supplements — and does not replace — the root `CLAUDE.md`.
> **All procedures defined here are mandatory. Non-compliance blocks commits and task completion.**

**Stack**: Next.js 16 · React 19 · TypeScript (strict) · Tailwind CSS 4 · CopilotKit 1.51 · pnpm
**Backend target**: `POST /api/langgraph4j` (Next.js API route → Spring Boot SSE at `localhost:8085`)

---

## Key Dependencies (package.json)

| Dependency                  | Version   | Purpose                                         |
| --------------------------- | --------- | ----------------------------------------------- |
| `next`                      | 16.1.6    | React framework (App Router, Turbopack)          |
| `react` / `react-dom`       | ^19.2.4   | UI library                                       |
| `@copilotkit/react-core`    | ^1.51.4   | CopilotKit React provider and hooks             |
| `@copilotkit/react-ui`      | ^1.51.4   | CopilotKit chat UI components                   |
| `@copilotkit/runtime`       | ^1.51.4   | CopilotKit server runtime + adapters            |
| `@copilotkit/shared`        | ^1.51.4   | CopilotKit shared types                         |
| `tailwindcss`               | ^4        | Utility-first CSS framework                      |
| `@tailwindcss/postcss`      | ^4        | PostCSS plugin for Tailwind                      |
| `typescript`                | ^5        | Type safety                                      |

---

## Directory Structure

```
frontend/
├── src/app/                       # Next.js App Router
│   ├── api/langgraph4j/
│   │   └── route.ts               # API route: proxies CopilotKit → Spring Boot backend
│   ├── component/
│   │   ├── chat.tsx                # CopilotKit chat component
│   │   └── chatApproval.tsx        # Tool call approval UI (human-in-the-loop)
│   ├── layout.tsx                  # Root layout
│   ├── page.tsx                    # Home page
│   ├── globals.css                 # Tailwind globals
│   └── favicon.ico
├── public/                         # Static assets
├── package.json                    # pnpm project config
├── pnpm-lock.yaml                  # Lockfile
├── next.config.ts                  # Next.js configuration
├── tsconfig.json                   # TypeScript config (strict mode)
├── postcss.config.mjs              # PostCSS + Tailwind plugin
└── CLAUDE.md                       # This file
```

---

## Architecture: Frontend ↔ Backend Flow

```
User → CopilotKit Chat UI → POST /api/langgraph4j (Next.js API route)
     → CopilotRuntime + LangGraphHttpAgent → POST http://localhost:8085/langgraph4j/copilotkit
     → Spring Boot streams AG-UI events (SSE) ← LangGraph4j agent execution
     → CopilotKit renders streamed response in chat UI
```

Key files:
- `src/app/api/langgraph4j/route.ts` — CopilotKit runtime endpoint, creates `LangGraphHttpAgent` pointing to Spring Boot
- `src/app/component/chat.tsx` — CopilotKit chat UI wrapper
- `src/app/component/chatApproval.tsx` — Tool call approval component (human-in-the-loop for agent actions)

The `NEXT_PUBLIC_LANGGRAPH_URL` env var overrides the backend URL (defaults to `http://localhost:8085/langgraph4j/copilotkit`).

---

## Frontend Quality Standards

The following standards define acceptable work for all frontend code. Every item must be met before a commit or task completion.

- All new React components must have unit tests
- Critical and important user flows must have integration tests
- Every visual or layout change must be verified with Playwright (visual + functional)
- No `any` in TypeScript — use strict typing throughout
- No inline styles — use Tailwind utility classes exclusively
- Form validation must use Zod schemas
- Images must be resized to max 1024px on the client before upload
- All components must be functional — no class components
- Interfaces preferred over type aliases for object shapes
- Type Guards must be used wherever runtime type narrowing is needed

---

## Testing Infrastructure Setup (Frontend — Greenfield)

> This is a new project. Set up the testing stack from scratch using current best practices.

### Recommended Stack

| Tool                            | Purpose                                         | Version target                               |
| ------------------------------- | ----------------------------------------------- | -------------------------------------------- |
| **Vitest**                      | Unit and integration test runner                | latest stable                                |
| **React Testing Library**       | Component testing with user-centric queries     | latest stable                                |
| **@testing-library/user-event** | Realistic user interaction simulation           | latest stable                                |
| **jsdom**                       | DOM environment for Vitest                      | via Vitest config                            |
| **MSW (Mock Service Worker)**   | Mock API calls in tests (including SSE streams) | v2+                                          |
| **Playwright**                  | End-to-end and visual verification              | latest stable (already available via plugin) |

### Initial Setup Commands

```bash
# Inside the frontend/ directory — use pnpm (not npm)
pnpm add -D vitest @vitest/ui jsdom
pnpm add -D @testing-library/react @testing-library/user-event @testing-library/jest-dom
pnpm add -D msw

# Playwright (for E2E and visual verification)
pnpm add -D @playwright/test
pnpm exec playwright install chromium
```

### Vitest Configuration

Add to `next.config.ts` or create `vitest.config.ts`:

```typescript
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
});
```

### NPM Scripts (add to `package.json`)

```json
{
  "scripts": {
    "dev": "next dev --turbopack",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "test": "vitest run",
    "test:watch": "vitest",
    "test:ui": "vitest --ui",
    "e2e": "playwright test"
  }
}
```

---

## Build and Development Commands

```bash
cd frontend
pnpm install                        # install dependencies
pnpm dev                            # start dev server (Turbopack, ~localhost:3000)
pnpm build                          # production build
pnpm lint                           # run Next.js linter
pnpm test                           # run unit tests (after test infra setup)
pnpm e2e                            # run Playwright E2E tests (after setup)
```

**Note**: The backend must be running on port 8085 for the frontend to work end-to-end.

---

## Test-Driven Development Process (Mandatory)

Follow the universal TDD process defined in the root `CLAUDE.md` (steps 1–7). Frontend-specific commands and patterns:

### Step 3 — Write Component Tests First

Write failing test files before creating the component:

```typescript
// ReturnForm.test.tsx — written BEFORE ReturnForm.tsx exists
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ReturnForm } from './ReturnForm'

describe('ReturnForm', () => {
  it('renders all required fields', () => {
    render(<ReturnForm onSubmit={vi.fn()} />)
    expect(screen.getByLabelText(/order number/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/purchase date/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /submit/i })).toBeInTheDocument()
  })
})
```

### Step 4 — Confirm Tests Fail

```bash
pnpm test
```

Tests must be red at this stage.

### Step 6 — Run All Tests Green

```bash
pnpm test
```

All tests must pass. Fix the implementation, not the tests (unless the test was wrong).

### Step 7 — Playwright Verification (Mandatory)

After tests pass, perform visual and functional verification using Playwright tools. See the **Playwright Verification Process** section below.

---

## Testing Requirements

### Unit Tests — Required for All Components

Every React component must have a test file covering:

| Category          | What to test                                                    |
| ----------------- | --------------------------------------------------------------- |
| Rendering         | Component renders without crashing; key elements are present    |
| Props             | Different prop values produce correct output                    |
| User interactions | Clicks, input changes, form submission trigger correct behavior |
| Validation        | Invalid input shows correct error messages                      |
| Loading states    | Spinner/skeleton appears during async operations                |
| Error states      | API errors are displayed correctly                              |
| Accessibility     | Interactive elements have accessible names (ARIA)               |

### Integration Tests — Required for Critical Flows

The following flows **must** have integration tests:

1. **Return form submission flow** — user fills form → submits → SSE stream starts → verdict appears
2. **Image upload and resize** — user selects image → client resizes to ≤1024px → correct payload sent
3. **Chat streaming** — SSE chunks arrive → chat UI updates incrementally → stream ends correctly
4. **Form validation with Zod** — invalid inputs trigger correct field-level errors
5. **Tool approval flow** — agent requests tool call → approval UI renders → user approves/rejects

Integration tests use MSW to mock the `/api/langgraph4j` endpoint — never call the live backend in tests.

---

## Playwright Verification Process

**This process is mandatory after every visual change.** Use the Playwright plugin tools already available in Claude Code.

### Step 1 — Start the Development Server

```bash
cd frontend && pnpm dev
```

Confirm the server is running (typically `http://localhost:3000`).

### Step 2 — Open the Page

Use the `browser_navigate` tool to open the application URL.

### Step 3 — Capture Accessibility Snapshot

Use `browser_snapshot` to capture the full accessibility tree. Verify:

- All form labels are present and correctly associated
- Interactive elements have accessible roles and names
- Required ARIA attributes are present
- No orphaned or empty elements

### Step 4 — Take a Screenshot

Use `browser_take_screenshot` to capture the visual state. Verify:

- Layout matches the design specification from the PRD
- Tailwind classes produce the expected visual result
- No broken layouts, overflow, or missing elements
- Responsive behavior at the target viewport size

### Step 5 — Interact and Verify Behavior

Use `browser_click`, `browser_type`, and `browser_fill_form` to simulate real user interactions. Verify:

- Chat messages appear correctly
- Tool approval UI renders when agent requests tool calls
- Loading states appear and disappear as expected
- Error states display correctly for error scenarios

### Step 6 — Inspect Network Requests (for interactive features)

If the change involves API calls, use `browser_network_requests` after triggering the interaction. Verify:

- The correct endpoint is called (`/api/langgraph4j`)
- The request payload matches the expected format
- The SSE stream is received and rendered correctly

### Step 7 — DOM Structure Analysis

Use `browser_evaluate` to inspect specific elements when the snapshot is insufficient:

```javascript
// Example: verify image resize happened
() => document.querySelector('img')?.naturalWidth;
```

### Step 8 — Self Code Review

Review the rendered output against your implementation:

- Does the DOM structure match what you intended to build?
- Are CSS classes applied correctly?
- Is the component tree structured correctly?
- Are there any console errors? (use `browser_console_messages`)

---

## Visual and Functional Verification

### What Constitutes Correct Visual Appearance

- Layout matches the wireframe/description in the PRD
- Tailwind spacing, color, and typography tokens are used correctly (no magic numbers, no inline styles)
- Components are responsive (test at 1280px desktop and 375px mobile)
- No visible overflow, clipping, or z-index issues
- Loading and error states are visually distinct and clear
- Polish text is rendered correctly (UTF-8, no encoding artifacts)

### What Constitutes Correct Functionality

- All form fields accept and validate input correctly
- Form submission calls the correct API endpoint with the correct payload
- SSE stream updates the chat UI in real time as chunks arrive
- Image resize happens client-side before upload
- Errors from the API are caught and displayed to the user
- No unhandled promise rejections or console errors in the browser

### Approval Criteria

A visual change is approved **only when**:

1. All unit and integration tests pass (`pnpm test`)
2. Playwright screenshot confirms the layout looks correct
3. Playwright interaction confirms the behavior works correctly
4. No console errors (`browser_console_messages` returns no errors)
5. DOM snapshot shows correct accessibility structure

---

## Pre-Commit Checklist (Frontend)

**Complete the universal checklist in root `CLAUDE.md`, then verify these frontend-specific items:**

```
□ I wrote tests BEFORE implementing the component (TDD)
□ All new components have unit test files
□ Critical flows have integration tests with MSW mocks
□ All tests pass: pnpm test (zero failures)
□ TypeScript compiles cleanly: pnpm exec tsc --noEmit (no errors)
□ Linting passes: pnpm lint (no errors)
□ No `any` type used — strict typing enforced
□ No inline styles — only Tailwind utility classes
□ Playwright verification completed:
    □ Page opens without errors
    □ Screenshot taken and layout confirmed correct
    □ DOM snapshot reviewed for accessibility issues
    □ Interactive behavior verified (chat, tool approval)
    □ No console errors in browser
□ Context7 MCP used for any new library API (CopilotKit, Next.js, Tailwind)
```

---

## Task Completion Criteria (Frontend)

All universal criteria from root `CLAUDE.md` apply. In addition, a frontend task requires:

1. **All tests pass** — `pnpm test` exits with zero failures
2. **TypeScript clean** — `pnpm exec tsc --noEmit` produces no errors
3. **Linting clean** — `pnpm lint` produces no errors
4. **Playwright verification complete** — page opened, screenshot taken, DOM inspected, behavior confirmed
5. **Visual appearance correct** — screenshot matches specification, no layout issues
6. **Functional behavior correct** — interactions work as specified in the PRD
7. **No console errors** — browser developer console is clean
8. **Accessibility verified** — DOM snapshot confirms labels, roles, and ARIA attributes are correct

**If any criterion is not met, the task is NOT complete.** Do not move to the next task.

---

## MCP Servers (Model Context Protocol)

### `context7` — Live Library Documentation

- **What it does**: Fetches up-to-date documentation for project libraries on demand. Avoids relying on potentially outdated training data.
- **When to use**: Whenever implementing features using a library below. Always fetch current docs before writing new integration code.

#### Relevant Context7 MCP Documentation

Fetch these before implementing features that depend on them:

| Handler                         | Use when                                                  |
| ------------------------------- | --------------------------------------------------------- |
| `/copilotkit/copilotkit`        | CopilotKit hooks, components, runtime, AG-UI integration  |
| `/vercel/next.js`               | Next.js App Router, API routes, server components         |
| `/reactjs/react.dev`            | React 19 patterns (use(), Suspense, transitions)          |
| `/tailwindlabs/tailwindcss.com` | Tailwind utility classes and configuration                |

### `playwright` — Browser Automation and Visual Verification

- **What it does**: Provides browser automation tools (navigate, click, snapshot, screenshot, evaluate, network inspection) powered by Playwright MCP integration.
- **When to use**:
  - Frontend work: mandatory visual verification of every rendered page or component change
  - API testing: verify the running application responds correctly end-to-end
  - Self-code-review: open the running app and visually confirm the implementation before committing
- **Integration into workflow**: After any frontend change, use Playwright tools to open the page, take a screenshot, inspect the DOM, and confirm correctness before committing.
