# E2E Test Guidelines

**Framework:** Playwright

## E2E = Real Stack, No API Mocks

Tests run against:
- Real backend at `localhost:8080`
- Real frontend at `localhost:5173`
- Real browser (Chrome)
- Real database (SQLite)

**NO `page.route()` mocking of `/api/*` endpoints.**

## QA Workflow: Quality FIRST, Tests SECOND

### Phase 1: Manual Smoke Test (MANDATORY)
1. Start backend: `cd backend && ./mvnw spring-boot:run`
2. Start frontend: `cd frontend && npm run dev`
3. Open `http://localhost:5173`
4. Fill form with REAL data + image from `assets/example-images/`
5. Submit → verify chat appears
6. Send follow-up → verify response streams
7. "Nowa sesja" → verify form shown
8. Take screenshots

**If any step fails → document bug, don't write tests yet.**

### Phase 2: Write Tests
Codify working behavior with Playwright. Use REAL images from `assets/example-images/`.

## Images
- `cloth1.webp` — WebP format
- `cloth2.jpg`, `cloth3.jpg`, `cloth4.jpg` — JPEG format

Load with `fs.readFileSync(path.resolve('../../assets/example-images/cloth2.jpg'))`.

## Screenshots (REQUIRED)
Save to `tests/e2e/screenshots/<descriptive-name>.png`.

## Logging (REQUIRED)
Log to `logs/e2e-tests.log` (repo root). Format: `[timestamp] [test] [step] message`.

## What You're NOT Responsible For
- Unit tests (fe/be-developer)
- Fixing bugs (fe/be-developer)
- Writing code (fe/be-developer)

Your job: Ensure app ACTUALLY WORKS before it ships.
