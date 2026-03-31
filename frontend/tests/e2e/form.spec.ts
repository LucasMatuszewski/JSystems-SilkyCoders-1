import { test, expect, Page } from '@playwright/test'
import path from 'path'
import fs from 'fs'
import { fileURLToPath } from 'url'
import { log, verifyBackendRunning } from './helpers'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

// Real images from assets/example-images/
const cloth2Jpg = fs.readFileSync(
  path.resolve(__dirname, '../../../assets/example-images/cloth2.jpg')
)
const cloth1Webp = fs.readFileSync(
  path.resolve(__dirname, '../../../assets/example-images/cloth1.webp')
)

const screenshotsDir = path.join(__dirname, 'screenshots')

async function uploadFileViaInput(page: Page, fileBuffer: Buffer, fileName: string, mimeType: string) {
  const fileInput = page.locator('input[type="file"]')
  await fileInput.setInputFiles({
    name: fileName,
    mimeType: mimeType,
    buffer: fileBuffer,
  })
}

test.describe('IntakeForm', () => {
  test.beforeEach(async () => {
    await verifyBackendRunning()
  })

  test('Test 1: form renders with all 5 fields', async ({ page }) => {
    const testName = 'IntakeForm - Test 1: form renders with all 5 fields'
    log(testName, 'start', 'Test started')
    if (!fs.existsSync(screenshotsDir)) {
      fs.mkdirSync(screenshotsDir, { recursive: true })
    }

    log(testName, 'navigate', 'Navigating to /')
    await page.goto('/')

    await page.screenshot({ path: 'tests/e2e/screenshots/form-initial-load.png', fullPage: true })
    log(testName, 'screenshot', 'Saved form-initial-load.png')

    // Intent radio group
    await expect(page.getByRole('radio', { name: 'Zwrot' })).toBeVisible()
    await expect(page.getByRole('radio', { name: 'Reklamacja' })).toBeVisible()
    log(testName, 'assert', 'Intent radio buttons visible')

    // Order number input
    await expect(page.getByLabel('Numer zamówienia')).toBeVisible()
    log(testName, 'assert', 'Order number input visible')

    // Product name input
    await expect(page.getByLabel('Nazwa produktu')).toBeVisible()
    log(testName, 'assert', 'Product name input visible')

    // Description textarea
    await expect(page.getByLabel('Opis problemu')).toBeVisible()
    log(testName, 'assert', 'Description textarea visible')

    // Image upload area
    await expect(page.getByRole('button', { name: /Wybierz zdjęcie produktu|Przeciągnij i upuść/ })).toBeVisible()
    log(testName, 'assert', 'Image upload button visible')

    log(testName, 'end', 'Test passed')
  })

  test('Test 2: empty submit shows 5 validation errors', async ({ page }) => {
    const testName = 'IntakeForm - Test 2: empty submit shows 5 validation errors'
    log(testName, 'start', 'Test started')
    if (!fs.existsSync(screenshotsDir)) {
      fs.mkdirSync(screenshotsDir, { recursive: true })
    }

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    await page.getByRole('button', { name: 'Sprawdź' }).click()
    log(testName, 'action', 'Clicked submit with empty fields')

    await page.screenshot({ path: 'tests/e2e/screenshots/form-after-empty-submit.png', fullPage: true })
    log(testName, 'screenshot', 'Saved form-after-empty-submit.png')

    const alerts = page.getByRole('alert')
    await expect(alerts).toHaveCount(5)
    log(testName, 'assert', '5 validation errors shown')

    await expect(page.getByText('Proszę wybrać typ zgłoszenia')).toBeVisible()
    await expect(page.getByText('Numer zamówienia jest wymagany')).toBeVisible()
    await expect(page.getByText('Nazwa produktu jest wymagana')).toBeVisible()
    await expect(page.getByText('Opis problemu jest wymagany')).toBeVisible()
    await expect(page.getByText('Zdjęcie produktu jest wymagane')).toBeVisible()
    log(testName, 'assert', 'All 5 error messages verified')

    log(testName, 'end', 'Test passed')
  })

  test('Test 3: PDF upload shows format error', async ({ page }) => {
    const testName = 'IntakeForm - Test 3: PDF upload shows format error'
    log(testName, 'start', 'Test started')
    if (!fs.existsSync(screenshotsDir)) {
      fs.mkdirSync(screenshotsDir, { recursive: true })
    }

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    const pdfBuffer = Buffer.from('%PDF-1.0')
    await uploadFileViaInput(page, pdfBuffer, 'test.pdf', 'application/pdf')
    log(testName, 'action', 'Uploaded PDF file')

    await page.screenshot({ path: 'tests/e2e/screenshots/form-pdf-error.png', fullPage: true })
    log(testName, 'screenshot', 'Saved form-pdf-error.png')

    await expect(page.getByRole('alert').filter({ hasText: 'Dozwolone formaty: JPEG, PNG, WebP, GIF' })).toBeVisible()
    log(testName, 'assert', 'Format error message visible')

    log(testName, 'end', 'Test passed')
  })

  test('Test 4: >10MB file shows size error', async ({ page }) => {
    const testName = 'IntakeForm - Test 4: >10MB file shows size error'
    log(testName, 'start', 'Test started')
    if (!fs.existsSync(screenshotsDir)) {
      fs.mkdirSync(screenshotsDir, { recursive: true })
    }

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    // 11 MB buffer filled with zeros, typed as JPEG so it passes MIME check but fails size check
    const largeBuffer = Buffer.alloc(11 * 1024 * 1024, 0)
    await uploadFileViaInput(page, largeBuffer, 'large.jpg', 'image/jpeg')
    log(testName, 'action', 'Uploaded 11MB JPEG file')

    await page.screenshot({ path: 'tests/e2e/screenshots/form-size-error.png', fullPage: true })
    log(testName, 'screenshot', 'Saved form-size-error.png')

    await expect(page.getByRole('alert').filter({ hasText: 'Maksymalny rozmiar pliku: 10 MB' })).toBeVisible()
    log(testName, 'assert', 'Size error message visible')

    log(testName, 'end', 'Test passed')
  })

  test('Test 5: valid submit shows loading state then chat view', async ({ page }) => {
    const testName = 'IntakeForm - Test 5: valid submit shows loading state then chat view'
    log(testName, 'start', 'Test started')
    if (!fs.existsSync(screenshotsDir)) {
      fs.mkdirSync(screenshotsDir, { recursive: true })
    }

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    // Fill all fields with real data
    await page.getByRole('radio', { name: 'Zwrot' }).click()
    await page.getByLabel('Numer zamówienia').fill('PL123456789')
    await page.getByLabel('Nazwa produktu').fill('Sukienka midi')
    await page.getByLabel('Opis problemu').fill('Produkt uszkodzony przy odbiorze')
    await uploadFileViaInput(page, cloth2Jpg, 'cloth2.jpg', 'image/jpeg')
    log(testName, 'action', 'Filled all form fields with real data')

    await page.screenshot({ path: 'tests/e2e/screenshots/form-filled.png', fullPage: true })
    log(testName, 'screenshot', 'Saved form-filled.png')

    // Submit and check for loading state
    const submitButton = page.getByRole('button', { name: 'Sprawdź' })
    await submitButton.click()
    log(testName, 'action', 'Clicked submit button')

    // Assert loading state
    await expect(page.getByRole('button', { name: 'Analizuję...' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Analizuję...' })).toBeDisabled()
    log(testName, 'assert', 'Loading state visible and button disabled')

    await page.screenshot({ path: 'tests/e2e/screenshots/form-loading-state.png', fullPage: true })
    log(testName, 'screenshot', 'Saved form-loading-state.png')

    // Wait for chat view to appear (backend processing completes)
    await expect(page.getByRole('button', { name: 'Nowa sesja' })).toBeVisible({ timeout: 30000 })
    log(testName, 'assert', 'Chat view appeared after backend processing')

    await page.screenshot({ path: 'tests/e2e/screenshots/chat-after-submit.png', fullPage: true })
    log(testName, 'screenshot', 'Saved chat-after-submit.png')

    log(testName, 'end', 'Test passed')
  })

  test('Test 6: screenshot of form (visual validation)', async ({ page }) => {
    const testName = 'IntakeForm - Test 6: screenshot of form (visual validation)'
    log(testName, 'start', 'Test started')
    if (!fs.existsSync(screenshotsDir)) {
      fs.mkdirSync(screenshotsDir, { recursive: true })
    }

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    const screenshotPath = path.join(screenshotsDir, 'form-desktop.png')
    await page.screenshot({ path: screenshotPath, fullPage: false })
    log(testName, 'screenshot', 'Saved form-desktop.png')

    console.log(`Screenshot saved to: ${screenshotPath}`)
    console.log('--- Visual Validation Observations ---')
    console.log('Layout: Form is centered on page with max-width container (max-w-lg)')
    console.log('Logo: Sinsay logo (logo.svg) displayed at top, centered, h-8 height')
    console.log('Heading: "Sprawdź zwrot lub reklamację" centered below logo')
    console.log('Subtitle: "Asystent AI zwrotów i reklamacji Sinsay" centered in gray')
    console.log('Fields (top to bottom):')
    console.log('  1. Radio group "Rodzaj zgłoszenia" — Zwrot | Reklamacja buttons')
    console.log('  2. Text input "Numer zamówienia"')
    console.log('  3. Text input "Nazwa produktu"')
    console.log('  4. Textarea "Opis problemu" (4 rows)')
    console.log('  5. Drag-and-drop image upload area "Zdjęcie produktu"')
    console.log('Submit button: Full-width orange ("Sprawdź"), square corners, brand color #e09243')
    console.log('--- End of Observations ---')

    // Non-blocking visual check: just verify the page has key elements rendered
    await expect(page.getByRole('heading', { name: 'Sprawdź zwrot lub reklamację' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Sprawdź' })).toBeVisible()
    log(testName, 'assert', 'Key form elements visible')

    log(testName, 'end', 'Test passed')
  })

  test('Test 7: valid WebP upload works', async ({ page }) => {
    const testName = 'IntakeForm - Test 7: valid WebP upload works'
    log(testName, 'start', 'Test started')
    if (!fs.existsSync(screenshotsDir)) {
      fs.mkdirSync(screenshotsDir, { recursive: true })
    }

    await page.goto('/')
    log(testName, 'navigate', 'Navigated to /')

    // Fill all fields with WebP image
    await page.getByRole('radio', { name: 'Reklamacja' }).click()
    await page.getByLabel('Numer zamówienia').fill('PL987654321')
    await page.getByLabel('Nazwa produktu').fill('Spodnie jeansowe')
    await page.getByLabel('Opis problemu').fill('Nieoczekiwane przerwanie')
    await uploadFileViaInput(page, cloth1Webp, 'cloth1.webp', 'image/webp')
    log(testName, 'action', 'Filled all form fields with WebP image')

    await page.screenshot({ path: 'tests/e2e/screenshots/form-webp-filled.png', fullPage: true })
    log(testName, 'screenshot', 'Saved form-webp-filled.png')

    // Submit
    const submitButton = page.getByRole('button', { name: 'Sprawdź' })
    await submitButton.click()
    log(testName, 'action', 'Clicked submit button')

    // Assert loading state
    await expect(page.getByRole('button', { name: 'Analizuję...' })).toBeVisible()
    log(testName, 'assert', 'Loading state visible')

    // Wait for chat view
    await expect(page.getByRole('button', { name: 'Nowa sesja' })).toBeVisible({ timeout: 30000 })
    log(testName, 'assert', 'Chat view appeared after WebP upload')

    await page.screenshot({ path: 'tests/e2e/screenshots/chat-webp-submit.png', fullPage: true })
    log(testName, 'screenshot', 'Saved chat-webp-submit.png')

    log(testName, 'end', 'Test passed')
  })
})
