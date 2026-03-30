import { z } from 'zod'

// Zod schema for form validation
export const formSchema = z.object({
  intent: z.enum(['RETURN', 'COMPLAINT'], {
    required_error: 'Proszę wybrać typ zgłoszenia',
  }),
  orderNumber: z.string().min(1, 'Numer zamówienia jest wymagany'),
  productName: z.string().min(1, 'Nazwa produktu jest wymagana'),
  description: z.string().min(1, 'Opis problemu jest wymagany'),
})

// TypeScript interfaces
export type FormData = z.infer<typeof formSchema>

export interface FormErrors {
  intent?: string
  orderNumber?: string
  productName?: string
  description?: string
  image?: string
  submit?: string
}

export interface ImageValidationError {
  valid: boolean
  error?: string
}

// Allowed MIME types for image upload
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']

// Maximum file size: 10 MB (10 * 1024 * 1024 bytes)
const MAX_FILE_SIZE = 10 * 1024 * 1024

/**
 * Validates an image file's MIME type and size.
 * Returns { valid: boolean, error?: string } with Polish error messages.
 */
export function validateImage(file: File): ImageValidationError {
  // Check MIME type first
  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    return {
      valid: false,
      error: 'Dozwolone formaty: JPEG, PNG, WebP, GIF',
    }
  }

  // Check file size
  if (file.size > MAX_FILE_SIZE) {
    return {
      valid: false,
      error: 'Maksymalny rozmiar pliku: 10 MB',
    }
  }

  return { valid: true }
}
