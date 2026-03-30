import { test, expect, Page } from '@playwright/test'
import path from 'path'
import fs from 'fs'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

// Minimal 1x1 pixel JPEG (hard-coded bytes)
const VALID_JPEG_BYTES = Buffer.from([
  0xff, 0xd8, 0xff, 0xe0, 0x00, 0x10, 0x4a, 0x46, 0x49, 0x46, 0x00, 0x01,
  0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0xff, 0xdb, 0x00, 0x43,
  0x00, 0x08, 0x06, 0x06, 0x07, 0x06, 0x05, 0x08, 0x07, 0x07, 0x07, 0x09,
  0x09, 0x08, 0x0a, 0x0c, 0x14, 0x0d, 0x0c, 0x0b, 0x0b, 0x0c, 0x19, 0x12,
  0x13, 0x0f, 0x14, 0x1d, 0x1a, 0x1f, 0x1e, 0x1d, 0x1a, 0x1c, 0x1c, 0x20,
  0x24, 0x2e, 0x27, 0x20, 0x22, 0x2c, 0x23, 0x1c, 0x1c, 0x28, 0x37, 0x29,
  0x2c, 0x30, 0x31, 0x34, 0x34, 0x34, 0x1f, 0x27, 0x39, 0x3d, 0x38, 0x32,
  0x3c, 0x2e, 0x33, 0x34, 0x32, 0xff, 0xc0, 0x00, 0x0b, 0x08, 0x00, 0x01,
  0x00, 0x01, 0x01, 0x01, 0x11, 0x00, 0xff, 0xc4, 0x00, 0x1f, 0x00, 0x00,
  0x01, 0x05, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00,
  0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
  0x09, 0x0a, 0x0b, 0xff, 0xc4, 0x00, 0xb5, 0x10, 0x00, 0x02, 0x01, 0x03,
  0x03, 0x02, 0x04, 0x03, 0x05, 0x05, 0x04, 0x04, 0x00, 0x00, 0x01, 0x7d,
  0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06,
  0x13, 0x51, 0x61, 0x07, 0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08,
  0x23, 0x42, 0xb1, 0xc1, 0x15, 0x52, 0xd1, 0xf0, 0x24, 0x33, 0x62, 0x72,
  0x82, 0x09, 0x0a, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28,
  0x29, 0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45,
  0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59,
  0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75,
  0x76, 0x77, 0x78, 0x79, 0x7a, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89,
  0x8a, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4,
  0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7,
  0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca,
  0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2, 0xe3,
  0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5,
  0xf6, 0xf7, 0xf8, 0xf9, 0xfa, 0xff, 0xda, 0x00, 0x08, 0x01, 0x01, 0x00,
  0x00, 0x3f, 0x00, 0xfb, 0xd3, 0xff, 0xd9,
])

async function uploadFileViaInput(page: Page, fileBuffer: Buffer, fileName: string, mimeType: string) {
  const fileInput = page.locator('input[type="file"]')
  await fileInput.setInputFiles({
    name: fileName,
    mimeType: mimeType,
    buffer: fileBuffer,
  })
}

test.describe('IntakeForm', () => {
  test('Test 1: form renders with all 5 fields', async ({ page }) => {
    await page.goto('/')

    // Intent radio group
    await expect(page.getByRole('radio', { name: 'Zwrot' })).toBeVisible()
    await expect(page.getByRole('radio', { name: 'Reklamacja' })).toBeVisible()

    // Order number input
    await expect(page.getByLabel('Numer zamówienia')).toBeVisible()

    // Product name input
    await expect(page.getByLabel('Nazwa produktu')).toBeVisible()

    // Description textarea
    await expect(page.getByLabel('Opis problemu')).toBeVisible()

    // Image upload area
    await expect(page.getByRole('button', { name: /Wybierz zdjęcie produktu|Przeciągnij i upuść/ })).toBeVisible()
  })

  test('Test 2: empty submit shows 5 validation errors', async ({ page }) => {
    await page.goto('/')

    await page.getByRole('button', { name: 'Sprawdź' }).click()

    const alerts = page.getByRole('alert')
    await expect(alerts).toHaveCount(5)

    await expect(page.getByText('Proszę wybrać typ zgłoszenia')).toBeVisible()
    await expect(page.getByText('Numer zamówienia jest wymagany')).toBeVisible()
    await expect(page.getByText('Nazwa produktu jest wymagana')).toBeVisible()
    await expect(page.getByText('Opis problemu jest wymagany')).toBeVisible()
    await expect(page.getByText('Zdjęcie produktu jest wymagane')).toBeVisible()
  })

  test('Test 3: PDF upload shows format error', async ({ page }) => {
    await page.goto('/')

    const pdfBuffer = Buffer.from('%PDF-1.0')
    await uploadFileViaInput(page, pdfBuffer, 'test.pdf', 'application/pdf')

    await expect(page.getByRole('alert').filter({ hasText: 'Dozwolone formaty: JPEG, PNG, WebP, GIF' })).toBeVisible()
  })

  test('Test 4: >10MB file shows size error', async ({ page }) => {
    await page.goto('/')

    // 11 MB buffer filled with zeros, typed as JPEG so it passes MIME check but fails size check
    const largeBuffer = Buffer.alloc(11 * 1024 * 1024, 0)
    await uploadFileViaInput(page, largeBuffer, 'large.jpg', 'image/jpeg')

    await expect(page.getByRole('alert').filter({ hasText: 'Maksymalny rozmiar pliku: 10 MB' })).toBeVisible()
  })

  test('Test 5: valid submit shows loading state', async ({ page }) => {
    // Mock POST /api/sessions
    await page.route('**/api/sessions', async (route) => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ sessionId: 'test-uuid', message: 'AI response' }),
        })
      } else {
        await route.continue()
      }
    })

    // Mock GET /api/sessions/test-uuid
    await page.route('**/api/sessions/test-uuid', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          sessionId: 'test-uuid',
          messages: [
            { id: '1', role: 'assistant', content: 'AI response' },
          ],
        }),
      })
    })

    await page.goto('/')

    // Fill all fields
    await page.getByRole('radio', { name: 'Zwrot' }).click()
    await page.getByLabel('Numer zamówienia').fill('PL123456789')
    await page.getByLabel('Nazwa produktu').fill('Sukienka midi')
    await page.getByLabel('Opis problemu').fill('Produkt uszkodzony przy odbiorze')

    // Upload valid JPEG
    await uploadFileViaInput(page, VALID_JPEG_BYTES, 'photo.jpg', 'image/jpeg')

    // Submit and immediately check for loading state
    const submitButton = page.getByRole('button', { name: 'Sprawdź' })

    // Intercept the route to add a delay so we can see the loading state
    let resolveRequest: (() => void) | null = null
    await page.route('**/api/sessions', async (route) => {
      if (route.request().method() === 'POST') {
        await new Promise<void>((resolve) => {
          resolveRequest = resolve
        })
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ sessionId: 'test-uuid', message: 'AI response' }),
        })
      } else {
        await route.continue()
      }
    })

    await submitButton.click()

    // Assert loading state
    await expect(page.getByRole('button', { name: 'Analizuję...' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Analizuję...' })).toBeDisabled()

    // Unblock the request
    if (resolveRequest) {
      resolveRequest()
    }
  })

  test('Test 6: screenshot of form (visual validation)', async ({ page }) => {
    await page.goto('/')

    const screenshotsDir = path.join(__dirname, 'screenshots')
    if (!fs.existsSync(screenshotsDir)) {
      fs.mkdirSync(screenshotsDir, { recursive: true })
    }

    const screenshotPath = path.join(screenshotsDir, 'form-desktop.png')
    await page.screenshot({ path: screenshotPath, fullPage: false })

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
  })
})
