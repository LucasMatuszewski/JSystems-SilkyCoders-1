import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import IntakeForm from './IntakeForm'
import { setMockSessionError } from '../mocks/handlers'

describe('IntakeForm', () => {
  const mockOnSuccess = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    // Reset mock error state
    setMockSessionError(null)
  })

  it('shows error message on API failure', async () => {
    // Set mock to return server error BEFORE rendering
    setMockSessionError('server')
    await new Promise((resolve) => setTimeout(resolve, 50))

    const user = userEvent.setup({ delay: null })
    render(<IntakeForm onSuccess={mockOnSuccess} />)

    // Fill form with valid data
    await user.click(screen.getByLabelText('Zwrot'))

    const orderInput = screen.getByLabelText('Numer zamówienia')
    await user.clear(orderInput)
    await user.type(orderInput, 'ERROR-TEST')

    const productInput = screen.getByLabelText('Nazwa produktu')
    await user.clear(productInput)
    await user.type(productInput, 'Test')

    const descInput = screen.getByLabelText('Opis problemu')
    await user.clear(descInput)
    await user.type(descInput, 'Test')

    const fileInput = screen.getByLabelText(/Wybierz zdjęcie produktu/i)
    const imageFile = new File(['test content'], 'test.jpg', { type: 'image/jpeg' })
    await user.upload(fileInput, imageFile)

    // Wait for image upload to complete
    await new Promise((resolve) => setTimeout(resolve, 200))

    // Submit form
    const submitButton = screen.getByRole('button', { name: 'Sprawdź' })
    await user.click(submitButton)

    // Check for error message
    await waitFor(
      () => {
        expect(screen.getByText(/Błąd podczas przesyłania formularza/i)).toBeInTheDocument()
      },
      { timeout: 5000 },
    )

    // Verify onSuccess was NOT called
    expect(mockOnSuccess).not.toHaveBeenCalled()

    // Reset mock error state for other tests
    setMockSessionError(null)
  })

  it('renders all 5 fields with Polish labels', () => {
    render(<IntakeForm onSuccess={mockOnSuccess} />)

    // Check heading
    expect(screen.getByText('Zgłoszenie zwrotu lub reklamacji')).toBeInTheDocument()

    // Check intent radio buttons
    expect(screen.getByLabelText('Zwrot')).toBeInTheDocument()
    expect(screen.getByLabelText('Reklamacja')).toBeInTheDocument()

    // Check text inputs
    expect(screen.getByLabelText('Numer zamówienia')).toBeInTheDocument()
    expect(screen.getByLabelText('Nazwa produktu')).toBeInTheDocument()
    expect(screen.getByLabelText('Opis problemu')).toBeInTheDocument()

    // Check image upload
    expect(screen.getByText('Zdjęcie produktu')).toBeInTheDocument()

    // Check submit button
    expect(screen.getByRole('button', { name: 'Sprawdź' })).toBeInTheDocument()
  })

  it('shows 5 validation errors when submitting empty form', async () => {
    const user = userEvent.setup()
    render(<IntakeForm onSuccess={mockOnSuccess} />)

    const submitButton = screen.getByRole('button', { name: 'Sprawdź' })
    await user.click(submitButton)

    // Check for all 5 validation errors
    await waitFor(() => {
      expect(screen.getByText('Proszę wybrać typ zgłoszenia')).toBeInTheDocument()
      expect(screen.getByText('Numer zamówienia jest wymagany')).toBeInTheDocument()
      expect(screen.getByText('Nazwa produktu jest wymagana')).toBeInTheDocument()
      expect(screen.getByText('Opis problemu jest wymagany')).toBeInTheDocument()
      expect(screen.getByText('Zdjęcie produktu jest wymagane')).toBeInTheDocument()
    })

    // Verify onSuccess was NOT called
    expect(mockOnSuccess).not.toHaveBeenCalled()
  })

  it('submits valid data and calls onSuccess callback', async () => {
    const user = userEvent.setup({ delay: null })
    render(<IntakeForm onSuccess={mockOnSuccess} />)

    // Select intent
    const returnRadio = screen.getByLabelText('Zwrot')
    await user.click(returnRadio)

    // Fill in order number
    const orderInput = screen.getByLabelText('Numer zamówienia')
    await user.clear(orderInput)
    await user.type(orderInput, 'PL123456789')

    // Fill in product name
    const productInput = screen.getByLabelText('Nazwa produktu')
    await user.clear(productInput)
    await user.type(productInput, 'Sukienka midi')

    // Fill in description
    const descriptionInput = screen.getByLabelText('Opis problemu')
    await user.clear(descriptionInput)
    await user.type(descriptionInput, 'Produkt nie pasuje do opisu')

    // Create a test image file (small enough to pass validation)
    const imageFile = new File(['test content'], 'test-image.jpg', { type: 'image/jpeg' })

    // Find and interact with the file input
    const fileInput = screen.getByLabelText(/Wybierz zdjęcie produktu/i)
    await user.upload(fileInput, imageFile)

    // Wait for the image upload to complete (resize operation)
    await new Promise((resolve) => setTimeout(resolve, 200))

    // Submit the form
    const submitButton = screen.getByRole('button', { name: 'Sprawdź' })
    await user.click(submitButton)

    // Wait for the API call and onSuccess callback
    await waitFor(
      () => {
        expect(mockOnSuccess).toHaveBeenCalled()
      },
      { timeout: 5000 },
    )

    // Verify the callback was called with correct arguments
    expect(mockOnSuccess).toHaveBeenCalledWith('test-session-123')

    // Verify sessionId was stored in localStorage
    expect(localStorage.getItem('sinsay_session_id')).toBe('test-session-123')
  })

  it('shows loading state during submission', async () => {
    const user = userEvent.setup({ delay: null })
    render(<IntakeForm onSuccess={mockOnSuccess} />)

    // Fill form with valid data
    await user.click(screen.getByLabelText('Zwrot'))
    const orderInput = screen.getByLabelText('Numer zamówienia')
    await user.clear(orderInput)
    await user.type(orderInput, 'PL123456789')

    const productInput = screen.getByLabelText('Nazwa produktu')
    await user.clear(productInput)
    await user.type(productInput, 'Sukienka midi')

    const descInput = screen.getByLabelText('Opis problemu')
    await user.clear(descInput)
    await user.type(descInput, 'Test')

    const fileInput = screen.getByLabelText(/Wybierz zdjęcie produktu/i)
    const imageFile = new File(['test content'], 'test.jpg', { type: 'image/jpeg' })
    await user.upload(fileInput, imageFile)

    // Wait for image upload to complete
    await new Promise((resolve) => setTimeout(resolve, 200))

    // Submit form
    const submitButton = screen.getByRole('button', { name: 'Sprawdź' })
    await user.click(submitButton)

    // Check for loading state immediately
    await waitFor(
      () => {
        expect(screen.getByRole('button', { name: 'Analizuję...' })).toBeInTheDocument()
      },
      { timeout: 2000 },
    )

    // Button should be disabled - use the specific button
    const loadingButton = screen.getByRole('button', { name: 'Analizuję...' })
    expect(loadingButton).toBeDisabled()
  })

  it('integrates ImageUpload component and validates image', async () => {
    const user = userEvent.setup()
    render(<IntakeForm onSuccess={mockOnSuccess} />)

    // Try to submit without image
    await user.click(screen.getByLabelText('Zwrot'))

    const orderInput = screen.getByLabelText('Numer zamówienia')
    await user.clear(orderInput)
    await user.type(orderInput, 'PL123456789')

    const productInput = screen.getByLabelText('Nazwa produktu')
    await user.clear(productInput)
    await user.type(productInput, 'Test')

    const descInput = screen.getByLabelText('Opis problemu')
    await user.clear(descInput)
    await user.type(descInput, 'Test')

    const submitButton = screen.getByRole('button', { name: 'Sprawdź' })
    await user.click(submitButton)

    // Should show image error
    await waitFor(() => {
      expect(screen.getByText('Zdjęcie produktu jest wymagane')).toBeInTheDocument()
    })
  })
})
