import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import ImageUpload from './ImageUpload'

describe('ImageUpload', () => {
  beforeEach(() => {
    // Reset URL mocks before each test
    vi.stubGlobal('URL', {
      createObjectURL: vi.fn((url: string | URL) => `blob:${url}`),
      revokeObjectURL: vi.fn(),
    })
  })

  it('renders drop zone with format and size guidance text', () => {
    render(<ImageUpload value={null} onChange={vi.fn()} />)

    expect(screen.getByText(/przeciągnij i upuść/i)).toBeInTheDocument()
    expect(screen.getByText(/Dozwolone formaty/i)).toBeInTheDocument()
    expect(screen.getByText(/JPEG, PNG, WebP, GIF/i)).toBeInTheDocument()
    expect(screen.getByText(/Maksymalny rozmiar/i)).toBeInTheDocument()
  })

  it('shows thumbnail and filename on valid file selection', async () => {
    const handleChange = vi.fn()
    const file = new File([''], 'test-image.jpg', { type: 'image/jpeg' })

    render(<ImageUpload value={file} onChange={handleChange} />)

    expect(screen.getByText('test-image.jpg')).toBeInTheDocument()
    expect(screen.getByRole('img')).toBeInTheDocument()
  })

  it('shows error for PDF file type', () => {
    const handleChange = vi.fn()
    const file = new File([''], 'test.pdf', { type: 'application/pdf' })

    render(<ImageUpload value={file} onChange={handleChange} />)

    // Check for error message with role="alert"
    const errorMessage = screen.getByRole('alert')
    expect(errorMessage).toHaveTextContent('Dozwolone formaty: JPEG, PNG, WebP, GIF')
  })

  it('shows error for file larger than 10MB', () => {
    const handleChange = vi.fn()
    const tenMBPlusOne = 10 * 1024 * 1024 + 1
    const blob = new Blob([new ArrayBuffer(tenMBPlusOne)], {
      type: 'image/jpeg',
    })
    const file = new File([blob], 'large.jpg', { type: 'image/jpeg' })

    render(<ImageUpload value={file} onChange={handleChange} />)

    // Check for error message with role="alert"
    const errorMessage = screen.getByRole('alert')
    expect(errorMessage).toHaveTextContent('10 MB')
  })

  it('calls onChange with null when Usuń button is clicked', () => {
    const handleChange = vi.fn()
    const file = new File([''], 'test-image.jpg', { type: 'image/jpeg' })

    render(<ImageUpload value={file} onChange={handleChange} />)

    const removeButton = screen.getByRole('button', { name: /Usuń/i })
    fireEvent.click(removeButton)

    expect(handleChange).toHaveBeenCalledWith(null)
  })

  it('calls onChange when valid file is selected', async () => {
    const handleChange = vi.fn()
    render(<ImageUpload value={null} onChange={handleChange} />)

    const input = screen.getByLabelText(/Wybierz zdjęcie produktu/i)
    expect(input).toBeInTheDocument()

    const file = new File(['content'], 'test.jpg', { type: 'image/jpeg' })
    fireEvent.change(input, { target: { files: [file] } })

    // Wait for the async resize operation to complete
    await new Promise((resolve) => setTimeout(resolve, 150))

    // The onChange should be called after resize timeout
    expect(handleChange).toHaveBeenCalled()
  })

  it('does not show validation error when file is null (initial state)', () => {
    render(<ImageUpload value={null} onChange={vi.fn()} />)

    // Check that there's no error message with role="alert"
    const errorMessage = screen.queryByRole('alert')
    expect(errorMessage).not.toBeInTheDocument()
  })

  it('shows error when error prop is provided', () => {
    render(<ImageUpload value={null} onChange={vi.fn()} error="Custom error message" />)

    expect(screen.getByText('Custom error message')).toBeInTheDocument()
  })
})
