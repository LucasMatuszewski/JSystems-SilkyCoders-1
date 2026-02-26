import { test, expect } from '@playwright/test';
import path from 'path';

const SCREENSHOT_DIR = path.resolve(__dirname, '../../../docs/assets');

test.describe('Sinsay AI Assistant — Visual and Functional Verification', () => {
  test('app loads with correct Sinsay branding', async ({ page }) => {
    // Navigate to the app
    await page.goto('http://localhost:3000');

    // Wait for the CopilotKit chat messages container to be visible
    await page.waitForSelector('.copilotKitMessages', { timeout: 15000 });

    // Take screenshot and save to docs/assets
    const screenshotPath = path.join(SCREENSHOT_DIR, 'screenshot-app-loaded.png');
    await page.screenshot({ path: screenshotPath, fullPage: false });

    // Verify Sinsay branded header is present
    const sinsayHeader = page.locator('header[aria-label="Sinsay AI Assistant"]');
    await expect(sinsayHeader).toBeVisible();

    // Verify logo SVG is inside header
    const logoSvg = sinsayHeader.locator('svg');
    await expect(logoSvg).toBeVisible();

    // Verify "AI Assistant" label text
    const assistantLabel = sinsayHeader.locator('span');
    await expect(assistantLabel).toContainText('AI Assistant');

    // Verify chat input with Polish placeholder
    const input = page.locator('textarea').first();
    await expect(input).toBeVisible();
    const placeholder = await input.getAttribute('placeholder');
    expect(placeholder).toBe('Napisz wiadomość...');

    // Verify the initial AI message in Polish
    const messages = page.locator('.copilotKitAssistantMessage');
    const messageCount = await messages.count();
    expect(messageCount).toBeGreaterThan(0);
    const firstMessage = await messages.first().textContent();
    expect(firstMessage).toContain('Sinsay');
  });

  test('chat input accepts text and shows send button', async ({ page }) => {
    await page.goto('http://localhost:3000');
    await page.waitForSelector('.copilotKitMessages', { timeout: 15000 });

    // Find the textarea input
    const input = page.locator('textarea').first();
    await expect(input).toBeVisible();

    // Verify send button is initially present
    const sendButton = page.locator('button[aria-label="Send"]');
    await expect(sendButton).toBeVisible();

    // Type a greeting in Polish
    await input.fill('Cześć');

    // Take screenshot with typed message
    const beforePath = path.join(SCREENSHOT_DIR, 'screenshot-before-send.png');
    await page.screenshot({ path: beforePath, fullPage: false });

    // Verify the text was entered correctly
    const inputValue = await input.inputValue();
    expect(inputValue).toBe('Cześć');

    // Press Enter to send
    await input.press('Enter');

    // Wait briefly for UI to react (user message may appear then be cleared if backend is down)
    await page.waitForTimeout(2000);

    // Take screenshot after sending attempt
    const afterPath = path.join(SCREENSHOT_DIR, 'screenshot-after-send.png');
    await page.screenshot({ path: afterPath, fullPage: false });

    // Input should be cleared after send
    const inputValueAfter = await input.inputValue();
    expect(inputValueAfter).toBe('');
  });

  test('CSS custom properties are applied correctly', async ({ page }) => {
    await page.goto('http://localhost:3000');
    await page.waitForSelector('.copilotKitMessages', { timeout: 15000 });

    // Check that CopilotKit primary color override is applied (compare case-insensitively)
    const primaryColor = await page.evaluate(() => {
      return getComputedStyle(document.documentElement)
        .getPropertyValue('--copilot-kit-primary-color')
        .trim()
        .toLowerCase();
    });
    expect(primaryColor).toBe('#16181d');

    // Check background color
    const bgColor = await page.evaluate(() => {
      return getComputedStyle(document.documentElement)
        .getPropertyValue('--copilot-kit-background-color')
        .trim()
        .toLowerCase();
    });
    expect(['#fff', '#ffffff']).toContain(bgColor);

    // Check contrast color (white text on dark backgrounds)
    const contrastColor = await page.evaluate(() => {
      return getComputedStyle(document.documentElement)
        .getPropertyValue('--copilot-kit-contrast-color')
        .trim()
        .toLowerCase();
    });
    expect(['#fff', '#ffffff']).toContain(contrastColor);

    // Check Sinsay header background is dark (#16181D)
    const headerBg = await page.evaluate(() => {
      const header = document.querySelector('header[aria-label="Sinsay AI Assistant"]');
      if (!header) return null;
      return getComputedStyle(header).backgroundColor;
    });
    // rgb(22,24,29) is #16181D
    expect(headerBg).toBe('rgb(22, 24, 29)');
  });

  test('no critical JavaScript errors on page load', async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', (err) => {
      errors.push(err.message);
    });
    page.on('console', (msg) => {
      if (msg.type() === 'error') {
        errors.push(`Console error: ${msg.text()}`);
      }
    });

    await page.goto('http://localhost:3000');
    await page.waitForSelector('.copilotKitMessages', { timeout: 15000 });
    await page.waitForTimeout(1000);

    // Filter out non-critical errors (network errors when backend is down are expected)
    const criticalErrors = errors.filter(e =>
      !e.includes('Failed to fetch') &&
      !e.includes('ERR_CONNECTION_REFUSED') &&
      !e.includes('NetworkError') &&
      !e.includes('AbortError') &&
      !e.includes('fetch') &&
      !e.includes('localhost:8085') &&
      !e.includes('localhost:3000/api/langgraph4j')
    );

    if (criticalErrors.length > 0) {
      console.log('Critical errors found:', criticalErrors);
    }
    expect(criticalErrors).toHaveLength(0);
  });

  test('layout fills viewport without horizontal scrollbars', async ({ page }) => {
    await page.goto('http://localhost:3000');
    await page.waitForSelector('.copilotKitMessages', { timeout: 15000 });

    // Check that no horizontal scrollbar exists
    const hasHorizontalScrollbar = await page.evaluate(() => {
      return document.documentElement.scrollWidth > document.documentElement.clientWidth;
    });
    expect(hasHorizontalScrollbar).toBe(false);

    // Check body overflow is hidden
    const bodyOverflow = await page.evaluate(() => {
      return getComputedStyle(document.body).overflow;
    });
    expect(bodyOverflow).toBe('hidden');
  });
});
