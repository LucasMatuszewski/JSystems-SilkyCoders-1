import { useState } from 'react'
import { useSession } from '../hooks/useSession'
import { validateImage } from '../lib/validation'
import type { FormErrors } from '../lib/validation'
import ImageUpload from './ImageUpload'

export interface IntakeFormProps {
  onSuccess: (sessionId: string) => void
}

export default function IntakeForm({ onSuccess }: IntakeFormProps): React.JSX.Element {
  const { setSessionId } = useSession()

  const [intent, setIntent] = useState<'RETURN' | 'COMPLAINT' | null>(null)
  const [orderNumber, setOrderNumber] = useState('')
  const [productName, setProductName] = useState('')
  const [description, setDescription] = useState('')
  const [image, setImage] = useState<File | null>(null)
  const [errors, setErrors] = useState<FormErrors>({})
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const validateForm = (): FormErrors => {
    const newErrors: FormErrors = {}

    // Validate intent
    if (!intent) {
      newErrors.intent = 'Proszę wybrać typ zgłoszenia'
    }

    // Validate order number
    if (!orderNumber.trim()) {
      newErrors.orderNumber = 'Numer zamówienia jest wymagany'
    }

    // Validate product name
    if (!productName.trim()) {
      newErrors.productName = 'Nazwa produktu jest wymagana'
    }

    // Validate description
    if (!description.trim()) {
      newErrors.description = 'Opis problemu jest wymagany'
    }

    // Validate image
    if (!image) {
      newErrors.image = 'Zdjęcie produktu jest wymagane'
    } else {
      const imageValidation = validateImage(image)
      if (!imageValidation.valid) {
        newErrors.image = imageValidation.error
      }
    }

    return newErrors
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    // Clear previous submit error
    setSubmitError(null)

    // Validate form
    const validationErrors = validateForm()
    setErrors(validationErrors)

    if (Object.keys(validationErrors).length > 0) {
      return
    }

    setIsSubmitting(true)

    try {
      // Create FormData for multipart upload
      const formData = new FormData()
      formData.append('intent', intent!)
      formData.append('orderNumber', orderNumber)
      formData.append('productName', productName)
      formData.append('description', description)
      if (image) {
        formData.append('image', image)
      }

      const response = await fetch('/api/sessions', {
        method: 'POST',
        body: formData,
      })

      if (!response.ok) {
        throw new Error('Błąd podczas przesyłania formularza')
      }

      const data = await response.json()

      // Store session ID in localStorage
      setSessionId(data.sessionId)

      // Call onSuccess callback
      onSuccess(data.sessionId)
    } catch (error) {
      setSubmitError('Błąd podczas przesyłania formularza. Spróbuj ponownie.')
      console.error('Form submission error:', error)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleImageChange = (file: File | null) => {
    setImage(file)
    // Clear image error when file changes
    if (file) {
      setErrors((prev) => ({ ...prev, image: undefined }))
    }
  }

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4">
      <div className="w-full max-w-2xl">
        {/* Logo */}
        <div className="flex justify-center mb-8">
          <img src="/logo.svg" alt="Sinsay" className="h-8" />
        </div>

        {/* Form Card */}
        <div className="bg-background border border-gray-200 p-8">
          <h1 className="text-2xl font-semibold text-brand-primary text-center mb-6">
            Zgłoszenie zwrotu lub reklamacji
          </h1>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Intent Selection */}
            <div>
              <label className="block text-sm font-medium text-text-primary mb-3">
                Typ zgłoszenia
              </label>
              <div className="flex gap-4">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="intent"
                    value="RETURN"
                    checked={intent === 'RETURN'}
                    onChange={(e) =>
                      setIntent(e.target.value === 'RETURN' ? 'RETURN' : 'COMPLAINT')
                    }
                    className="w-4 h-4"
                  />
                  <span className="text-text-primary">Zwrot</span>
                </label>
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="intent"
                    value="COMPLAINT"
                    checked={intent === 'COMPLAINT'}
                    onChange={(e) =>
                      setIntent(e.target.value === 'COMPLAINT' ? 'COMPLAINT' : 'RETURN')
                    }
                    className="w-4 h-4"
                  />
                  <span className="text-text-primary">Reklamacja</span>
                </label>
              </div>
              {errors.intent && (
                <p className="text-sm text-brand-error mt-1" role="alert">
                  {errors.intent}
                </p>
              )}
            </div>

            {/* Order Number */}
            <div>
              <label
                htmlFor="orderNumber"
                className="block text-sm font-medium text-text-primary mb-2"
              >
                Numer zamówienia
              </label>
              <input
                type="text"
                id="orderNumber"
                value={orderNumber}
                onChange={(e) => {
                  setOrderNumber(e.target.value)
                  setErrors((prev) => ({ ...prev, orderNumber: undefined }))
                }}
                placeholder="np. PL123456789"
                className="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-brand-accent"
              />
              {errors.orderNumber && (
                <p className="text-sm text-brand-error mt-1" role="alert">
                  {errors.orderNumber}
                </p>
              )}
            </div>

            {/* Product Name */}
            <div>
              <label
                htmlFor="productName"
                className="block text-sm font-medium text-text-primary mb-2"
              >
                Nazwa produktu
              </label>
              <input
                type="text"
                id="productName"
                value={productName}
                onChange={(e) => {
                  setProductName(e.target.value)
                  setErrors((prev) => ({ ...prev, productName: undefined }))
                }}
                placeholder="np. Sukienka midi w kwiaty"
                className="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-brand-accent"
              />
              {errors.productName && (
                <p className="text-sm text-brand-error mt-1" role="alert">
                  {errors.productName}
                </p>
              )}
            </div>

            {/* Description */}
            <div>
              <label
                htmlFor="description"
                className="block text-sm font-medium text-text-primary mb-2"
              >
                Opis problemu
              </label>
              <textarea
                id="description"
                value={description}
                onChange={(e) => {
                  setDescription(e.target.value)
                  setErrors((prev) => ({ ...prev, description: undefined }))
                }}
                placeholder="Opisz stan produktu i powód zwrotu lub reklamacji"
                rows={4}
                className="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-brand-accent resize-none"
              />
              {errors.description && (
                <p className="text-sm text-brand-error mt-1" role="alert">
                  {errors.description}
                </p>
              )}
            </div>

            {/* Image Upload */}
            <ImageUpload value={image} onChange={handleImageChange} error={errors.image} />

            {/* Submit Error */}
            {submitError && (
              <p className="text-sm text-brand-error text-center" role="alert">
                {submitError}
              </p>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-3 px-8 bg-brand-accent text-white font-semibold text-base hover:bg-brand-accent/90 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
              style={{
                backgroundColor: 'var(--color-brand-accent)',
                color: 'var(--color-text-on-dark)',
                border: 'none',
              }}
            >
              {isSubmitting ? 'Analizuję...' : 'Sprawdź'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
