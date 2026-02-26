import { test, expect } from '@playwright/test';

const BACKEND_HEALTH_URL = 'http://localhost:8085/langgraph4j/copilotkit';
const APP_URL = 'http://localhost:3000';
// Test fixture for photo upload tests (form submission / verdict):
//   path.join(__dirname, 'fixtures', 'test-product.jpg')

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
  // Test 4 — No JavaScript errors on page load
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
