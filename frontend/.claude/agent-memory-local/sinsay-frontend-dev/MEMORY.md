# Sinsay Frontend Agent Memory

## Critical Architecture Facts

### Verdict Detection in E2E Tests
- The verdict from the AI model comes back as **plain streaming text** in the CopilotKit chat UI.
- There are NO `data-testid="verdict-approved"` or `data-testid="verdict-rejected"` elements.
- The model returns text like "Zwrot możliwy", "Zwrot niemożliwy", "Reklamacja uzasadniona", "Reklamacja nieuzasadniona".
- E2E verdict assertion: `await expect(page.getByTestId('chat-messages')).toContainText(/Zwrot możliwy|Zwrot niemożliwy|Reklamacja uzasadniona|Reklamacja nieuzasadniona/i, { timeout: 120000 })`

### Model Latency (kimi-k2.5:cloud)
- Simple chat reply: up to 60s → use `{ timeout: 60000 }` for AI text assertions
- Form trigger (showReturnForm tool call): up to 60s → use `{ timeout: 60000 }` for return-form visibility
- Multimodal verdict (full flow with photo): up to 120s → use `{ timeout: 120000 }` for verdict text
- Playwright global `timeout` in `playwright.config.ts` must be >= 180000 to cover full flow tests

### data-testid Attributes (confirmed present)
- `return-form` — on the `<form>` element in ReturnForm.tsx
- `form-product-name` — on the product name `<input>`
- `form-description` — on the description `<textarea>`
- `form-photo-upload` — on the file `<input>`
- `form-submit-btn` — on the submit `<button>`
- `form-type` — on the type `<select>`
- `chat-input` — injected via MutationObserver in SinsayChat.tsx onto CopilotKit's textarea
- `chat-send-btn` — injected via MutationObserver in SinsayChat.tsx
- `chat-messages` — injected via MutationObserver in SinsayChat.tsx

### CopilotKit testid Injection
- SinsayChat.tsx uses a `MutationObserver` + `useEffect` to inject `data-testid` onto CopilotKit's
  internal DOM elements (chat-input, chat-send-btn, chat-messages).
- Always wait for `chat-messages` to be visible before asserting anything: `{ timeout: 15000 }`

## Key File Locations
- E2E tests: `frontend/src/e2e/visual-verification.spec.ts`
- E2E config: `frontend/playwright.config.ts`
- Chat component: `frontend/src/app/component/SinsayChat.tsx`
- Form component: `frontend/src/app/component/ReturnForm.tsx`
- Test images: `docs/example-images/cloth2.jpg` (~26KB), `docs/example-images/cloth3.jpg` (~1.4MB)

## Playwright Config Notes
- `screenshotsPath` is NOT a valid Playwright property — use `outputDir` for output location.
- Screenshots with `screenshot: 'on'` go into `outputDir` (`src/e2e/test-results/`).
