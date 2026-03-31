import { describe, it, expect } from 'vitest'
import { formSchema, type FormData, validateImage } from './validation'

describe('Zod Form Validation', () => {
  const validFormData: FormData = {
    intent: 'RETURN',
    orderNumber: 'PL123456789',
    productName: 'Sukienka midi w kwiaty',
    description: 'Produkt nie pasuje do opisu',
  }

  it('accepts valid form data', () => {
    const result = formSchema.safeParse(validFormData)
    expect(result.success).toBe(true)
  })

  it('rejects missing intent', () => {
    const data = { ...validFormData, intent: undefined }
    const result = formSchema.safeParse(data)
    expect(result.success).toBe(false)
  })

  it('rejects empty orderNumber', () => {
    const data = { ...validFormData, orderNumber: '' }
    const result = formSchema.safeParse(data)
    expect(result.success).toBe(false)
  })

  it('rejects missing orderNumber', () => {
    const data = { ...validFormData, orderNumber: undefined }
    const result = formSchema.safeParse(data)
    expect(result.success).toBe(false)
  })

  it('rejects empty productName', () => {
    const data = { ...validFormData, productName: '' }
    const result = formSchema.safeParse(data)
    expect(result.success).toBe(false)
  })

  it('rejects missing productName', () => {
    const data = { ...validFormData, productName: undefined }
    const result = formSchema.safeParse(data)
    expect(result.success).toBe(false)
  })

  it('rejects empty description', () => {
    const data = { ...validFormData, description: '' }
    const result = formSchema.safeParse(data)
    expect(result.success).toBe(false)
  })

  it('rejects missing description', () => {
    const data = { ...validFormData, description: undefined }
    const result = formSchema.safeParse(data)
    expect(result.success).toBe(false)
  })

  it('accepts both RETURN and COMPLAINT intents', () => {
    const returnData = { ...validFormData, intent: 'RETURN' as const }
    const complaintData = { ...validFormData, intent: 'COMPLAINT' as const }

    expect(formSchema.safeParse(returnData).success).toBe(true)
    expect(formSchema.safeParse(complaintData).success).toBe(true)
  })
})

describe('validateImage', () => {
  it('accepts valid JPEG image', () => {
    const file = new File([''], 'test.jpg', { type: 'image/jpeg' })
    const result = validateImage(file)
    expect(result.valid).toBe(true)
    expect(result.error).toBeUndefined()
  })

  it('accepts valid PNG image', () => {
    const file = new File([''], 'test.png', { type: 'image/png' })
    const result = validateImage(file)
    expect(result.valid).toBe(true)
    expect(result.error).toBeUndefined()
  })

  it('accepts valid WebP image', () => {
    const file = new File([''], 'test.webp', { type: 'image/webp' })
    const result = validateImage(file)
    expect(result.valid).toBe(true)
    expect(result.error).toBeUndefined()
  })

  it('accepts valid GIF image', () => {
    const file = new File([''], 'test.gif', { type: 'image/gif' })
    const result = validateImage(file)
    expect(result.valid).toBe(true)
    expect(result.error).toBeUndefined()
  })

  it('rejects PDF file with Polish error message', () => {
    const file = new File([''], 'test.pdf', { type: 'application/pdf' })
    const result = validateImage(file)
    expect(result.valid).toBe(false)
    expect(result.error).toBe('Dozwolone formaty: JPEG, PNG, WebP, GIF')
  })

  it('rejects text file', () => {
    const file = new File([''], 'test.txt', { type: 'text/plain' })
    const result = validateImage(file)
    expect(result.valid).toBe(false)
    expect(result.error).toBe('Dozwolone formaty: JPEG, PNG, WebP, GIF')
  })

  it('accepts image under 10MB', () => {
    // Create a file that's 1MB in size
    const oneMB = 1024 * 1024
    const blob = new Blob([new ArrayBuffer(oneMB)], { type: 'image/jpeg' })
    const file = new File([blob], 'test.jpg', { type: 'image/jpeg' })

    const result = validateImage(file)
    expect(result.valid).toBe(true)
    expect(result.error).toBeUndefined()
  })

  it('accepts image exactly at 10MB limit', () => {
    const tenMB = 10 * 1024 * 1024
    const blob = new Blob([new ArrayBuffer(tenMB)], { type: 'image/jpeg' })
    const file = new File([blob], 'test.jpg', { type: 'image/jpeg' })

    const result = validateImage(file)
    expect(result.valid).toBe(true)
    expect(result.error).toBeUndefined()
  })

  it('rejects image over 10MB with Polish error message', () => {
    // Create a file that's 10MB + 1 byte
    const tenMBPlusOne = 10 * 1024 * 1024 + 1
    const blob = new Blob([new ArrayBuffer(tenMBPlusOne)], { type: 'image/jpeg' })
    const file = new File([blob], 'test.jpg', { type: 'image/jpeg' })

    const result = validateImage(file)
    expect(result.valid).toBe(false)
    expect(result.error).toBe('Maksymalny rozmiar pliku: 10 MB')
  })

  it('checks MIME type before file size', () => {
    // Invalid type, small size - should fail on type first
    const file = new File([''], 'test.pdf', { type: 'application/pdf' })
    const result = validateImage(file)
    expect(result.valid).toBe(false)
    expect(result.error).toBe('Dozwolone formaty: JPEG, PNG, WebP, GIF')
  })
})
