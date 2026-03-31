# QA Engineer Memory

## Project: Sinsay AI PoC

### Playwright Setup
- Config: `frontend/playwright.config.ts` — Chromium only, baseURL `http://localhost:5173`, `reuseExistingServer: true`
- Tests: `frontend/tests/e2e/` — **REAL stack only, NO `page.route()` mocking of `/api/*` endpoints**
- Real backend at `localhost:8080`, real frontend at `localhost:5173`, real SQLite DB, real OpenRouter LLM calls
- Run: `cd frontend && npx playwright test --project=chromium`

### QA Workflow (MANDATORY)

#### Phase 1: Manual Smoke Test (use Playwright MCP / browser automation)
1. Start backend: `cd backend && ./mvnw spring-boot:run`
2. Start frontend: `cd frontend && npm run dev`
3. Use Playwright MCP to open `http://localhost:5173`
4. Fill form: pick intent (Zwrot or Reklamacja), order number, product name, description
5. Upload real image from `assets/example-images/` (use cloth2.jpg or cloth1.webp)
6. Submit — screenshot the loading state, then the chat view
7. Send a follow-up message → screenshot streamed response
8. Click "Nowa sesja" → screenshot form returned
9. **Analyze all screenshots** — compare to wireframes (`docs/wireframe-form.png`, `docs/wireframe-decision+chat.png`) and `assets/sinsay-homepage.png`
10. If any step fails → document the bug; do NOT write automated tests yet

#### Phase 2: Automated Playwright Tests
Codify the verified working behavior. All tests use real backend + real images.

### Key Patterns

**ESM / __dirname workaround** (project uses `"type": "module"`):
```ts
import { fileURLToPath } from 'url'
const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
```

**Real image loading** — use `__dirname`-based path, not CWD-relative:
```ts
const cloth2Jpg = fs.readFileSync(
  path.resolve(__dirname, '../../../assets/example-images/cloth2.jpg')
)
const cloth1Webp = fs.readFileSync(
  path.resolve(__dirname, '../../../assets/example-images/cloth1.webp')
)
```

Available images: `cloth1.webp` (WebP edge case), `cloth2.jpg`, `cloth3.jpg`, `cloth4.jpg` (JPEG).

**File upload via hidden input** — ImageUpload uses a hidden `<input type="file">`:
```ts
await page.locator('input[type="file"]').setInputFiles({ name, mimeType, buffer })
```

**Strict mode: avoid ambiguous text locators** — `getByText()` fails if text appears in multiple elements.
Use `getByRole('alert').filter({ hasText: '...' })` to target only error messages when static hint text shares the same string.

**Validation error messages (Polish)**:
- intent: `'Proszę wybrać typ zgłoszenia'`
- orderNumber: `'Numer zamówienia jest wymagany'`
- productName: `'Nazwa produktu jest wymagana'`
- description: `'Opis problemu jest wymagany'`
- image (missing): `'Zdjęcie produktu jest wymagane'`
- image (bad format): `'Dozwolone formaty: JPEG, PNG, WebP, GIF'`
- image (too large): `'Maksymalny rozmiar pliku: 10 MB'`

**Loading state text**: submit button shows `'Analizuję...'` and is disabled during submit.

**LLM response assertions** — assert actual content, not just element presence:
- Response must be non-empty
- Should contain one of: `'Prawdopodobnie zaakceptowane'`, `'Prawdopodobnie odrzucone'`, `'Niejasne'`
- Language must be Polish

### Screenshot Analysis (REQUIRED)
After taking screenshots in automated tests or manual Phase 1:
- Use vision to **actually read and describe** what the screenshot shows
- Compare against wireframes and Sinsay homepage design
- Document discrepancies as bugs
- Do NOT hardcode observations as console.log strings — those are never re-evaluated

### Backend Health Check
```ts
import { verifyBackendRunning } from './helpers'
// Call in test.beforeEach — throws if backend unreachable
```
