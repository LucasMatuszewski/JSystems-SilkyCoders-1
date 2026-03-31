import { test, expect, Page } from '@playwright/test'
import path from 'path'
import fs from 'fs'
import { fileURLToPath } from 'url'
import { log, verifyBackendRunning } from './helpers'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

// Real images from assets/example-images/
const cloth3Jpg = fs.readFileSync(
  path.resolve(__dirname, '../../../assets/example-images/cloth3.jpg')
)

async function uploadFileViaInput(page: Page, fileBuffer: Buffer, fileName: string, mimeType: string) {
  const fileInput = page.locator('input[type="file"]')
  await fileInput.setInputFiles({
    name: fileName,
    mimeType: mimeType,
    buffer: fileBuffer,
  })
}

async function fillAndSubmitForm(page: Page) {
  await page.getByRole('radio', { name: 'Zwrot' }).click()
  await page.getByLabel('Numer zamówienia').fill('ORD-001')
  await page.getByLabel('Nazwa produktu').fill('Koszulka')
  await page.getByLabel('Opis problemu').fill('Chcę zwrócić produkt')
  await uploadFileViaInput(page, cloth3Jpg, 'cloth3.jpg', 'image/jpeg')
  await page.getByRole('button', { name: 'Sprawdź' }).click()
}


function ensureScreenshotsDir(): void {
  const screenshotsDir = path.join(__dirname, 'screenshots')
  if (!fs.existsSync(screenshotsDir)) {
    fs.mkdirSync(screenshotsDir, { recursive: true })
  }
}

test.describe('ChatFlow', () => {
  test.beforeEach(async () => {
    await verifyBackendRunning()
  })

  test('Test 1: form submit → chat view appears with AI message', async ({ page }) => {
    const testName = 'ChatFlow - Test 1: form submit → chat view appears with AI message'
    log(testName, 'start', 'Test started')
    ensureScreenshotsDir()

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    await fillAndSubmitForm(page)
    log(testName, 'action', 'Filled form and submitted')

    // Summary bar with "Nowa sesja" button indicates chat view is rendered
    await expect(page.getByRole('button', { name: 'Nowa sesja' })).toBeVisible({ timeout: 30000 })
    log(testName, 'assert', 'Chat view appeared (Nowa sesja button visible)')

    await page.screenshot({ path: 'tests/e2e/screenshots/chat-initial-ai-message.png', fullPage: true })
    log(testName, 'screenshot', 'Saved chat-initial-ai-message.png')

    // The initial AI message should be visible and contain a decision from the LLM
    const chatMessages = page.locator('[data-testid="assistant-message"]')
    await expect(chatMessages.first()).toBeVisible()
    log(testName, 'assert', 'AI message visible in chat')

    // Verify LLM returned a non-empty decision (not a blank/error response)
    const initialResponse = await chatMessages.first().innerText()
    expect(initialResponse.trim().length).toBeGreaterThan(10)
    log(testName, 'assert', `LLM initial decision non-empty: "${initialResponse.substring(0, 80)}..."`)

    log(testName, 'end', 'Test passed')
  })

  test('Test 2: session ID stored in localStorage after form submit', async ({ page }) => {
    const testName = 'ChatFlow - Test 2: session ID stored in localStorage after form submit'
    log(testName, 'start', 'Test started')

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    await fillAndSubmitForm(page)
    log(testName, 'action', 'Filled form and submitted')

    await expect(page.getByRole('button', { name: 'Nowa sesja' })).toBeVisible({ timeout: 30000 })
    log(testName, 'assert', 'Chat view appeared')

    // Verify session ID was stored
    const storedId = await page.evaluate(() => localStorage.getItem('sinsay_session_id'))
    expect(storedId).toBeTruthy()
    expect(storedId?.length).toBeGreaterThan(0)
    log(testName, 'assert', `Session ID stored in localStorage: ${storedId}`)

    log(testName, 'end', 'Test passed')
  })

  test('Test 3: chat input visible and user can type', async ({ page }) => {
    const testName = 'ChatFlow - Test 3: chat input visible and user can type'
    log(testName, 'start', 'Test started')

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    await fillAndSubmitForm(page)
    log(testName, 'action', 'Filled form and submitted')

    await expect(page.getByRole('button', { name: 'Nowa sesja' })).toBeVisible({ timeout: 30000 })
    log(testName, 'assert', 'Chat view appeared')

    // Composer input is visible
    const composerInput = page.getByPlaceholder('Zadaj pytanie...')
    await expect(composerInput).toBeVisible()
    log(testName, 'assert', 'Chat input visible')

    // User can type in the composer
    await composerInput.fill('Kiedy dostanę zwrot?')
    await expect(composerInput).toHaveValue('Kiedy dostanę zwrot?')
    log(testName, 'assert', 'User can type in chat input')

    await page.screenshot({ path: 'tests/e2e/screenshots/chat-user-typing.png', fullPage: true })
    log(testName, 'screenshot', 'Saved chat-user-typing.png')

    log(testName, 'end', 'Test passed')
  })

  test('Test 4: "Nowa sesja" button clears session and shows form', async ({ page }) => {
    const testName = 'ChatFlow - Test 4: "Nowa sesja" button clears session and shows form'
    log(testName, 'start', 'Test started')

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    await fillAndSubmitForm(page)
    log(testName, 'action', 'Filled form and submitted')

    await expect(page.getByRole('button', { name: 'Nowa sesja' })).toBeVisible({ timeout: 30000 })
    log(testName, 'assert', 'Chat view appeared')

    // Get the session ID before clicking
    const storedIdBefore = await page.evaluate(() => localStorage.getItem('sinsay_session_id'))
    log(testName, 'info', `Session ID before Nowa sesja: ${storedIdBefore}`)

    await page.getByRole('button', { name: 'Nowa sesja' }).click()
    log(testName, 'action', 'Clicked Nowa sesja button')

    // Form view should be visible now
    await expect(page.getByRole('radio', { name: 'Zwrot' })).toBeVisible()
    log(testName, 'assert', 'Form view visible after Nowa sesja')

    // localStorage should be cleared
    const storedIdAfter = await page.evaluate(() => localStorage.getItem('sinsay_session_id'))
    expect(storedIdAfter).toBeNull()
    log(testName, 'assert', 'Session ID cleared from localStorage')

    await page.screenshot({ path: 'tests/e2e/screenshots/form-after-nowa-sesja.png', fullPage: true })
    log(testName, 'screenshot', 'Saved form-after-nowa-sesja.png')

    log(testName, 'end', 'Test passed')
  })

  test('Test 5: send message → response streams in', async ({ page }) => {
    const testName = 'ChatFlow - Test 5: send message → response streams in'
    log(testName, 'start', 'Test started')
    ensureScreenshotsDir()

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    await fillAndSubmitForm(page)
    log(testName, 'action', 'Filled form and submitted')

    await expect(page.getByRole('button', { name: 'Nowa sesja' })).toBeVisible({ timeout: 30000 })
    log(testName, 'assert', 'Chat view appeared')

    const composerInput = page.getByPlaceholder('Zadaj pytanie...')
    await composerInput.fill('Kiedy dostanę zwrot?')
    log(testName, 'action', 'Typed user message')

    await page.screenshot({ path: 'tests/e2e/screenshots/chat-before-send.png', fullPage: true })
    log(testName, 'screenshot', 'Saved chat-before-send.png')

    await page.keyboard.press('Enter')
    log(testName, 'action', 'Pressed Enter to send message')

    // Wait for user message to appear
    await expect(page.getByText('Kiedy dostanę zwrot?')).toBeVisible()
    log(testName, 'assert', 'User message visible in chat')

    // Wait for assistant response (streamed response should appear)
    const assistantMessages = page.locator('[data-testid="assistant-message"]')
    await expect(assistantMessages.nth(1)).toBeVisible({ timeout: 30000 })
    log(testName, 'assert', 'Assistant response appeared (streamed)')

    // Verify LLM actually responded with non-empty Polish content
    const responseText = await assistantMessages.nth(1).innerText()
    expect(responseText.trim().length).toBeGreaterThan(0)
    log(testName, 'assert', `LLM response non-empty: "${responseText.substring(0, 80)}..."`)

    await page.screenshot({ path: 'tests/e2e/screenshots/chat-after-response.png', fullPage: true })
    log(testName, 'screenshot', 'Saved chat-after-response.png')

    log(testName, 'end', 'Test passed')
  })

  test('Test 6: session resume on page reload', async ({ page }) => {
    const testName = 'ChatFlow - Test 6: session resume on page reload'
    log(testName, 'start', 'Test started')

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    await fillAndSubmitForm(page)
    log(testName, 'action', 'Filled form and submitted')

    await expect(page.getByRole('button', { name: 'Nowa sesja' })).toBeVisible({ timeout: 30000 })
    log(testName, 'assert', 'Chat view appeared')

    const storedId = await page.evaluate(() => localStorage.getItem('sinsay_session_id'))
    log(testName, 'info', `Session ID: ${storedId}`)

    await page.screenshot({ path: 'tests/e2e/screenshots/chat-before-reload.png', fullPage: true })
    log(testName, 'screenshot', 'Saved chat-before-reload.png')

    // Reload the page
    await page.reload()
    log(testName, 'action', 'Reloaded page')

    // Chat view should still be visible (session resumed)
    await expect(page.getByRole('button', { name: 'Nowa sesja' })).toBeVisible({ timeout: 30000 })
    log(testName, 'assert', 'Chat view restored after reload (session resumed)')

    await page.screenshot({ path: 'tests/e2e/screenshots/chat-after-reload.png', fullPage: true })
    log(testName, 'screenshot', 'Saved chat-after-reload.png')

    log(testName, 'end', 'Test passed')
  })

  test('Test 7 (Visual): take screenshot of chat view', async ({ page }) => {
    const testName = 'ChatFlow - Test 7 (Visual): take screenshot of chat view'
    log(testName, 'start', 'Test started')
    ensureScreenshotsDir()

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    await fillAndSubmitForm(page)
    log(testName, 'action', 'Filled form and submitted')

    await expect(page.getByRole('button', { name: 'Nowa sesja' })).toBeVisible({ timeout: 30000 })
    log(testName, 'assert', 'Chat view appeared')

    const screenshotsDir = path.join(__dirname, 'screenshots')
    const screenshotPath = path.join(screenshotsDir, 'chat-desktop.png')
    await page.screenshot({ path: screenshotPath, fullPage: false })
    log(testName, 'screenshot', `Saved chat-desktop.png — review at: ${screenshotPath}`)
    log(testName, 'visual', 'Compare screenshot against: docs/wireframe-decision+chat.png and assets/sinsay-homepage.png')

    // Structural checks: verify all key chat UI elements are present
    await expect(page.getByRole('button', { name: 'Nowa sesja' })).toBeVisible()
    await expect(page.getByPlaceholder('Zadaj pytanie...')).toBeVisible()
    await expect(page.locator('[data-testid="assistant-message"]').first()).toBeVisible()
    log(testName, 'assert', 'All key chat elements visible (summary bar, messages, composer)')

    log(testName, 'end', 'Test passed')
  })
})
