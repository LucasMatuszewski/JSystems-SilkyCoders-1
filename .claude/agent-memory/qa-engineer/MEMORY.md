# QA Engineer Memory

## Project: Sinsay AI PoC

### Playwright Setup
- Config: `frontend/playwright.config.ts` — Chromium only, baseURL `http://localhost:5173`, `reuseExistingServer: true`
- Tests: `frontend/tests/e2e/` — use `page.route()` to mock all API calls, no real backend needed
- Run: `cd frontend && npx playwright test --project=chromium`

### Key Patterns

**ESM / __dirname workaround** (project uses `"type": "module"`):
```ts
import { fileURLToPath } from 'url'
const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
```

**Strict mode: avoid ambiguous text locators** — `getByText()` fails if text appears in multiple elements.
Use `getByRole('alert').filter({ hasText: '...' })` to target only error messages when static hint text shares the same string.

**File upload via hidden input** — ImageUpload uses a hidden `<input type="file">`:
```ts
await page.locator('input[type="file"]').setInputFiles({ name, mimeType, buffer })
```

**Valid 1x1 JPEG** — hard-coded minimal JPEG bytes buffer in test file (no file system dependency).

**Validation error messages (Polish)**:
- intent: `'Proszę wybrać typ zgłoszenia'`
- orderNumber: `'Numer zamówienia jest wymagany'`
- productName: `'Nazwa produktu jest wymagana'`
- description: `'Opis problemu jest wymagany'`
- image (missing): `'Zdjęcie produktu jest wymagane'`
- image (bad format): `'Dozwolone formaty: JPEG, PNG, WebP, GIF'`
- image (too large): `'Maksymalny rozmiar pliku: 10 MB'`

**Loading state text**: submit button shows `'Analizuję...'` and is disabled during submit.
