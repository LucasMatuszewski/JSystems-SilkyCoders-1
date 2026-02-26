import { test, expect } from '@playwright/test';
import path from 'path';

const BACKEND_HEALTH_URL = 'http://localhost:8085/langgraph4j/copilotkit';
const APP_URL = 'http://localhost:3000';

// Real product images from docs/example-images/ — vary in size to exercise image resize logic:
//   cloth2.jpg:  ~26 KB  (small, no resize needed)
//   cloth3.jpg:  ~1.4 MB (large, resize to 800px should trigger)
const EXAMPLE_IMAGES_DIR = path.join(__dirname, '../../../docs/example-images');
const SMALL_PHOTO = path.join(EXAMPLE_IMAGES_DIR, 'cloth2.jpg');   // ~26 KB
const LARGE_PHOTO = path.join(EXAMPLE_IMAGES_DIR, 'cloth3.jpg');   // ~1.4 MB

/**
 * Every test suite in this file requires a live backend.
 * The beforeAll health check throws so that tests never silently pass
 * when the backend is down.
 */
test.describe('Sinsay AI Assistant — E2E verification', () => {
  test.beforeAll(async ({ request }) => {
    // The backend does not have an /actuator/health endpoint; we probe the
    // CopilotKit endpoint with an OPTIONS / HEAD request. A connection
    // refused means the backend is not running and we must abort.
    const response = await request
      .head(BACKEND_HEALTH_URL)
      .catch(() => null);

    if (!response) {
      throw new Error(
        'Backend is not running on port 8085. ' +
        'Start it with: ./mvnw spring-boot:run'
      );
    }
  });

  // ─────────────────────────────────────────────────────────────────────────
  // Test 1 — App loads with correct branding
  // ─────────────────────────────────────────────────────────────────────────
  test('app loads with correct Sinsay branding', async ({ page }) => {
    // Any uncaught JS error on the page must fail this test immediately.
    page.on('pageerror', (err) => {
      throw err;
    });

    await page.goto(APP_URL);

    // chat-messages is injected by a MutationObserver in SinsayChat — wait for it
    await expect(page.getByTestId('chat-messages')).toBeVisible({ timeout: 15000 });

    // Sinsay branded header must be present and contain a logo
    const header = page.getByRole('banner', { name: 'Sinsay AI Assistant' });
    await expect(header).toBeVisible();

    // Logo SVG is embedded inside the header
    await expect(header.locator('svg[aria-label="Sinsay"]')).toBeVisible();

    // Chat input must carry Polish placeholder text
    const chatInput = page.getByTestId('chat-input');
    await expect(chatInput).toBeVisible({ timeout: 15000 });
    await expect(chatInput).toHaveAttribute('placeholder', 'Napisz wiadomość...');
  });

  // ─────────────────────────────────────────────────────────────────────────
  // Test 2 — Chat sends a message and backend responds
  // This test MUST FAIL if the backend is down.
  // ─────────────────────────────────────────────────────────────────────────
  test('chat sends a message and backend responds with AI reply', async ({ page }) => {
    page.on('pageerror', (err) => {
      throw err;
    });

    await page.goto(APP_URL);

    // Wait until CopilotKit has injected the testid attributes
    const chatInput = page.getByTestId('chat-input');
    await expect(chatInput).toBeVisible({ timeout: 15000 });

    await chatInput.fill('Cześć');
    await page.getByTestId('chat-send-btn').click();

    // The input must be cleared after the message is sent
    await expect(chatInput).toHaveValue('', { timeout: 10000 });

    // An AI response must appear in chat — this will time out and fail if the
    // backend is unreachable or not responding, which is the desired behavior.
    const messages = page.getByTestId('chat-messages');
    await expect(messages).toContainText(/Sinsay|asystent|pomóc/i, { timeout: 30000 });
  });

  // ─────────────────────────────────────────────────────────────────────────
  // Test 3 — Return form appears when "zwrot" keyword is sent
  // ─────────────────────────────────────────────────────────────────────────
  test('return form appears on "zwrot" intent keyword', async ({ page }) => {
    page.on('pageerror', (err) => {
      throw err;
    });

    await page.goto(APP_URL);

    const chatInput = page.getByTestId('chat-input');
    await expect(chatInput).toBeVisible({ timeout: 15000 });

    await chatInput.fill('Chcę dokonać zwrotu');
    await page.getByTestId('chat-send-btn').click();

    // The agent must call showReturnForm which renders the form in-chat.
    // This verifies the full round-trip: chat → backend → tool call → form.
    await expect(page.getByTestId('return-form')).toBeVisible({ timeout: 30000 });

    // Key form fields must be present and visible
    await expect(page.getByTestId('form-product-name')).toBeVisible();
    await expect(page.getByTestId('form-description')).toBeVisible();
  });

  // ─────────────────────────────────────────────────────────────────────────
  // Test 4a — COMPLETE FLOW with small photo (cloth2.jpg ~26KB, no resize needed)
  // This test exercises the full round-trip: chat → form → photo → verdict.
  // It MUST FAIL if the backend does not produce a verdict.
  // If the backend returns an error, data-testid="error-toast" appears immediately
  // and the test fails fast (instead of timing out after 60s).
  // ─────────────────────────────────────────────────────────────────────────
  test('complete flow: small photo (26KB) produces an AI verdict', async ({ page }) => {
    page.on('pageerror', (err) => {
      throw err;
    });

    await page.goto(APP_URL);

    const chatInput = page.getByTestId('chat-input');
    await expect(chatInput).toBeVisible({ timeout: 15000 });

    // Step 1: trigger the return form
    await chatInput.fill('Chcę dokonać zwrotu towaru');
    await page.getByTestId('chat-send-btn').click();

    // Step 2: wait for form to appear
    await expect(page.getByTestId('return-form')).toBeVisible({ timeout: 30000 });

    // Step 3: fill form fields
    await page.getByTestId('form-product-name').fill('Kurtka zimowa XL');
    await page.getByTestId('form-description').fill(
      'Kurtka posiada wadę fabryczną — rozerwany szew na lewym rękawie po pierwszym praniu.'
    );

    // Step 4: upload small test photo (~26KB — no resize should trigger)
    await page.getByTestId('form-photo-upload').setInputFiles(SMALL_PHOTO);

    // Step 5: submit the form
    await page.getByTestId('form-submit-btn').click();

    // Step 6: a verdict MUST appear. If an error banner appears first, fail immediately
    // instead of waiting the full 60s timeout.
    // The CopilotKit error banner contains Polish text starting with "Błąd" — select by text.
    const approved = page.getByTestId('verdict-approved');
    const rejected = page.getByTestId('verdict-rejected');
    const errorBanner = page.getByText(/Błąd/, { exact: false });

    const result = await Promise.race([
      approved.waitFor({ timeout: 60000 }).then(() => 'approved' as const),
      rejected.waitFor({ timeout: 60000 }).then(() => 'rejected' as const),
      errorBanner.waitFor({ timeout: 60000 }).then(() => 'error' as const),
    ]);

    if (result === 'error') {
      const errorText = await errorBanner.first().textContent();
      throw new Error(`Backend returned an error instead of a verdict: "${errorText}"`);
    }

    expect(['approved', 'rejected']).toContain(result);
  });

  // ─────────────────────────────────────────────────────────────────────────
  // Test 4b — COMPLETE FLOW with large photo (cloth3.jpg ~1.4MB)
  // Verifies the image resize logic: large images must be downscaled before
  // sending to the model, otherwise Ollama returns "broken pipe".
  // ─────────────────────────────────────────────────────────────────────────
  test('complete flow: large photo (1.4MB) triggers resize and produces a verdict', async ({ page }) => {
    page.on('pageerror', (err) => {
      throw err;
    });

    await page.goto(APP_URL);

    const chatInput = page.getByTestId('chat-input');
    await expect(chatInput).toBeVisible({ timeout: 15000 });

    await chatInput.fill('Mam reklamację do produktu');
    await page.getByTestId('chat-send-btn').click();

    await expect(page.getByTestId('return-form')).toBeVisible({ timeout: 30000 });

    await page.getByTestId('form-product-name').fill('Płaszcz wiosenny M');
    await page.getByTestId('form-description').fill(
      'Produkt ma wyraźną wadę fabryczną — odklejona podszewka przy kołnierzu.'
    );

    // Upload large photo (~1.4MB — must be resized to ≤800px before sending)
    await page.getByTestId('form-photo-upload').setInputFiles(LARGE_PHOTO);

    await page.getByTestId('form-submit-btn').click();

    const approved = page.getByTestId('verdict-approved');
    const rejected = page.getByTestId('verdict-rejected');
    const errorBanner = page.getByText(/Błąd/, { exact: false });

    const result = await Promise.race([
      approved.waitFor({ timeout: 60000 }).then(() => 'approved' as const),
      rejected.waitFor({ timeout: 60000 }).then(() => 'rejected' as const),
      errorBanner.waitFor({ timeout: 60000 }).then(() => 'error' as const),
    ]);

    if (result === 'error') {
      const errorText = await errorBanner.first().textContent();
      throw new Error(`Backend returned an error instead of a verdict: "${errorText}"`);
    }

    expect(['approved', 'rejected']).toContain(result);
  });

  // ─────────────────────────────────────────────────────────────────────────
  // Test 5 — No JavaScript errors on page load
  // NO filtering — every error is a failure.
  // ─────────────────────────────────────────────────────────────────────────
  test('no JavaScript errors on page load', async ({ page }) => {
    const errors: string[] = [];

    // Collect all uncaught page errors — no filtering whatsoever
    page.on('pageerror', (err) => {
      errors.push(err.message);
    });

    await page.goto(APP_URL);

    // Wait for the UI to fully initialise
    await expect(page.getByTestId('chat-messages')).toBeVisible({ timeout: 15000 });

    // Give the page one extra second for any deferred script errors to surface
    await page.waitForTimeout(1000);

    expect(errors).toHaveLength(0);
  });

  // ─────────────────────────────────────────────────────────────────────────
  // Test 5 — Layout fills viewport without horizontal scrollbar
  // ─────────────────────────────────────────────────────────────────────────
  test('layout fills viewport without horizontal scrollbar', async ({ page }) => {
    page.on('pageerror', (err) => {
      throw err;
    });

    await page.goto(APP_URL);
    await expect(page.getByTestId('chat-messages')).toBeVisible({ timeout: 15000 });

    const hasHorizontalScrollbar = await page.evaluate(() => {
      return document.documentElement.scrollWidth > document.documentElement.clientWidth;
    });

    expect(hasHorizontalScrollbar).toBe(false);
  });
});
