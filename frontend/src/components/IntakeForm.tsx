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

    if (!intent) {
      newErrors.intent = 'Proszę wybrać typ zgłoszenia'
    }

    if (!orderNumber.trim()) {
      newErrors.orderNumber = 'Numer zamówienia jest wymagany'
    }

    if (!productName.trim()) {
      newErrors.productName = 'Nazwa produktu jest wymagana'
    }

    if (!description.trim()) {
      newErrors.description = 'Opis problemu jest wymagany'
    }

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

    setSubmitError(null)

    const validationErrors = validateForm()
    setErrors(validationErrors)

    if (Object.keys(validationErrors).length > 0) {
      return
    }

    setIsSubmitting(true)

    try {
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

      setSessionId(data.sessionId)
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
    if (file) {
      setErrors((prev) => ({ ...prev, image: undefined }))
    }
  }

  return (
    <div className="min-h-screen bg-background flex items-start justify-center px-4 py-8">
      <div className="w-full max-w-lg">
        {/* Logo */}
        <div className="flex justify-center mb-8">
          <img src="/logo.svg" alt="Sinsay" className="h-8" />
        </div>

        {/* Heading */}
        <h1
          className="text-2xl font-semibold text-center mb-2"
          style={{ color: '#16181d', fontWeight: 600 }}
        >
          Sprawdź zwrot lub reklamację
        </h1>
        <p className="text-center text-sm mb-8" style={{ color: '#777777' }}>
          Asystent AI zwrotów i reklamacji Sinsay
        </p>

        {/* Form Card */}
        <div className="bg-white border border-gray-200 p-6 sm:p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Intent Selection */}
            <div>
              <p className="block text-sm font-medium mb-3" style={{ color: '#333333' }}>
                Rodzaj zgłoszenia
              </p>
              <div className="flex gap-3 flex-wrap">
                <label
                  className="flex items-center gap-2 cursor-pointer px-4 py-2 border transition-colors"
                  style={{
                    borderColor: intent === 'RETURN' ? '#e09243' : '#d1d5db',
                    backgroundColor: intent === 'RETURN' ? '#fff8f0' : '#ffffff',
                  }}
                >
                  <input
                    type="radio"
                    name="intent"
                    value="RETURN"
                    checked={intent === 'RETURN'}
                    onChange={() => setIntent('RETURN')}
                    className="w-4 h-4 accent-[#e09243]"
                    aria-label="Zwrot"
                  />
                  <span style={{ color: '#333333' }}>Zwrot</span>
                </label>
                <label
                  className="flex items-center gap-2 cursor-pointer px-4 py-2 border transition-colors"
                  style={{
                    borderColor: intent === 'COMPLAINT' ? '#e09243' : '#d1d5db',
                    backgroundColor: intent === 'COMPLAINT' ? '#fff8f0' : '#ffffff',
                  }}
                >
                  <input
                    type="radio"
                    name="intent"
                    value="COMPLAINT"
                    checked={intent === 'COMPLAINT'}
                    onChange={() => setIntent('COMPLAINT')}
                    className="w-4 h-4 accent-[#e09243]"
                    aria-label="Reklamacja"
                  />
                  <span style={{ color: '#333333' }}>Reklamacja</span>
                </label>
              </div>
              {errors.intent && (
                <p className="text-sm mt-1" style={{ color: '#e90000' }} role="alert">
                  {errors.intent}
                </p>
              )}
            </div>

            {/* Order Number */}
            <div>
              <label
                htmlFor="orderNumber"
                className="block text-sm font-medium mb-2"
                style={{ color: '#333333' }}
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
                className="w-full px-4 py-3 border focus:outline-none"
                style={{ borderColor: errors.orderNumber ? '#e90000' : '#d1d5db', borderRadius: 0 }}
              />
              {errors.orderNumber && (
                <p className="text-sm mt-1" style={{ color: '#e90000' }} role="alert">
                  {errors.orderNumber}
                </p>
              )}
            </div>

            {/* Product Name */}
            <div>
              <label
                htmlFor="productName"
                className="block text-sm font-medium mb-2"
                style={{ color: '#333333' }}
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
                className="w-full px-4 py-3 border focus:outline-none"
                style={{ borderColor: errors.productName ? '#e90000' : '#d1d5db', borderRadius: 0 }}
              />
              {errors.productName && (
                <p className="text-sm mt-1" style={{ color: '#e90000' }} role="alert">
                  {errors.productName}
                </p>
              )}
            </div>

            {/* Description */}
            <div>
              <label
                htmlFor="description"
                className="block text-sm font-medium mb-2"
                style={{ color: '#333333' }}
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
                className="w-full px-4 py-3 border focus:outline-none resize-none"
                style={{ borderColor: errors.description ? '#e90000' : '#d1d5db', borderRadius: 0 }}
              />
              {errors.description && (
                <p className="text-sm mt-1" style={{ color: '#e90000' }} role="alert">
                  {errors.description}
                </p>
              )}
            </div>

            {/* Image Upload */}
            <ImageUpload value={image} onChange={handleImageChange} error={errors.image} />

            {/* Submit Error */}
            {submitError && (
              <p className="text-sm text-center" style={{ color: '#e90000' }} role="alert">
                {submitError}
              </p>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full font-semibold text-base transition-opacity disabled:opacity-50 disabled:cursor-not-allowed"
              style={{
                backgroundColor: isSubmitting ? '#d1d5db' : '#e09243',
                color: '#ffffff',
                border: '1.6px solid',
                borderColor: isSubmitting ? '#d1d5db' : '#e09243',
                borderRadius: 0,
                padding: '12px 32px',
                fontSize: '16px',
                fontWeight: 600,
                lineHeight: '24px',
                cursor: isSubmitting ? 'not-allowed' : 'pointer',
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
