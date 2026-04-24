# Sinsay Frontend Dev — Persistent Memory

## Key Project Facts
- Frontend: `frontend/` — Next.js 16.1.6, React 19, TypeScript strict, Tailwind CSS 4, CopilotKit 1.51, pnpm
- Backend target: `POST /api/langgraph4j` (Next.js API route → Spring Boot at `localhost:8085`)
- Test runner: Vitest 4.x + React Testing Library + MSW
- E2E: Playwright 1.58, config at `frontend/playwright.config.ts`, tests in `frontend/src/e2e/`
- ESLint: custom flat config at `frontend/eslint.config.mjs` (Next.js 16 removed `next lint`)

## Critical Patterns

### Next.js 16 — No `next lint` Command
- `next lint` was removed in Next.js 16. Use `eslint src --ext .ts,.tsx` instead.

### CSS Import Order in layout.tsx — CRITICAL
- `@copilotkit/react-ui/styles.css` must be imported BEFORE `./globals.css`
- Reason: CopilotKit uses `!important` on `.poweredBy { display: block !important }` etc.
- When globals.css loads AFTER CopilotKit, same-specificity `!important` in globals.css WINS
  (later cascade position = wins tie-breaking for equal !important weight)
- Correct order in layout.tsx: `import '@copilotkit/react-ui/styles.css'` THEN `import './globals.css'`

### CopilotKit "Powered by CopilotKit" Removal
- `.poweredBy` has BOTH CSS class rules AND inline styles set by JS runtime
- CSS fix: correct import order (globals.css after CopilotKit) + `display: none !important`
- JS fix: MutationObserver in SinsayChat sets `el.style.setProperty('display','none','important')`
- Both layers needed: CSS kills it before paint; JS handles any re-injection

### CopilotKit Developer Inspector ("N" button) — cpk-web-inspector
- `cpk-web-inspector` is a custom element (web component) added by CopilotKit in dev mode
- CSS `cpk-web-inspector { display: none !important }` works for the host element
- JS: MutationObserver calls `el.remove()` — observe `document.body` with `subtree: true`
- Its internals render in shadow DOM, unreachable by global CSS

### Next.js Dev Overlay Button (the "N" circle in corner)
- This is NOT cpk-web-inspector — it's Next.js's own devtools overlay
- It renders inside `<nextjs-portal>`'s shadow DOM — global CSS cannot pierce it
- Fix: `next.config.ts` → `devIndicators: false`

### CopilotKit CSS Variable Overrides — Specificity Issue
- Also use `html:root` selector (higher specificity than `:root`) for CSS custom properties
- Combined with correct import order, this ensures all overrides take effect

### CopilotKit v1.51 — No Header Rendered by `CopilotChat`
- `CopilotChat` does NOT render a `.copilotKitHeader` element by default.
- Add a custom branded `<header>` element above `<CopilotChat>`.

### CopilotKit Real CSS Variable Names (v1.51)
- primary-color, contrast-color, background-color, input-background-color
- secondary-color, secondary-contrast-color, separator-color, muted-color
- NOT real: response-button-*, scrollbar-color

### Vitest — Exclude Playwright E2E tests
- Add `exclude: ['**/e2e/**', '**/node_modules/**']` to vitest.config.ts.

### Vitest — Mock `new Image()` constructor
- Use a real constructor function: `(global as any).Image = function MockImage() { capturedInstance = this; };`

### ReturnForm — Label vs. Radiogroup Conflict in Tests
- Using `<label htmlFor="type">` for hidden `<select>` AND `aria-labelledby` on radiogroup
  pointing to the same text causes `getByLabelText(/typ zgłoszenia/i)` to find two elements.
- Fix: `<label htmlFor="type">` for hidden select + radiogroup `aria-label="Zwrot lub reklamacja"`
  (different text, no conflict with test query)

### Playwright Screenshot Reliability
- `playwright screenshot URL file` races with JS init — use chromium.launch() pattern instead
- Wait for `[data-testid="chat-messages"]` before screenshot
- For cpk-web-inspector removal: `waitForFunction(() => !document.querySelector('cpk-web-inspector'))`

## File Locations
- Design system: `docs/sinsay-design-system.md`
- Wireframes: `docs/assets/sinsay-wireframe-1-step.png`, `docs/assets/sinsay-wireframe-2-step.png`
- Logo: `docs/assets/sinsay-logo.svg` (dark #16181D, viewBox 0 0 84 31)
- Logo white: `docs/assets/sinsay-logo-white.svg`
- Frontend AGENTS rules: `frontend/CLAUDE.md`
- Next.js config: `frontend/next.config.ts`

## Sinsay Design Tokens
- Brand dark: `#16181D`, Accent orange: `#E09243`, Input border: `#C7C8C9`
- Error: `#FF0023`, Success: `#0DB209`, Subtle bg: `#F1F2F4`, Muted text: `#7B7D80`
- Dark 20 (subtle): `#C8C9CC`, Dark 10 (separator): `#E3E4E5`, Dark 30 (placeholder): `#AFB0B2`

## CopilotKit CSS Overrides (globals.css via `html:root`)
- primary: `#16181D`, contrast: `#FFFFFF`, bg: `#FFFFFF`
- input-bg: `#F1F2F4`, separator: `#E3E4E5`, muted: `#AFB0B2`

## Component Map
- `ReturnForm` — `frontend/src/app/component/ReturnForm.tsx`
  - Pill submit button (rounded-full), chip type selector (radiogroup + sr-only select),
    dashed photo upload zone, inline error icons, character counter, frozen-state banner
- `SinsayChat` — `frontend/src/app/component/SinsayChat.tsx`
  - Gradient header + "ASYSTENT ONLINE" strip + MutationObserver removes CopilotKit chrome
- `VerdictMessage` — `frontend/src/app/component/VerdictMessage.tsx`
  - Banner with icon + green/red colour, justification body
- `imageResize` — `frontend/src/app/lib/imageResize.ts`
- `schemas` — `frontend/src/app/lib/schemas.ts`

## Layout Height Chain
- `body.h-screen.overflow-hidden.flex.flex-col` → `CopilotKit (context)` →
  `main.flex-1.flex-col.min-h-0` → `SinsayChat div.flex.flex-1.flex-col.min-h-0` →
  `header.shrink-0` + `status-strip.shrink-0` + `CopilotChat.flex-1.min-h-0`
