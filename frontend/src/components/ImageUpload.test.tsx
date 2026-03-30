import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import ImageUpload from './ImageUpload'

describe('ImageUpload', () => {
  it('renders drop zone with format and size guidance text', () => {
    render(<ImageUpload value={null} onChange={vi.fn()} />)

    expect(screen.getByText(/drag & drop/i)).toBeInTheDocument()
    expect(screen.getByText(/Dozwolone formaty/i)).toBeInTheDocument()
    expect(screen.getByText(/JPEG, PNG, WebP, GIF/i)).toBeInTheDocument()
    expect(screen.getByText(/Maksymalny rozmiar/i)).toBeInTheDocument()
  })

  it('shows thumbnail and filename on valid file selection', async () => {
    const handleChange = vi.fn()
    const file = new File([''], 'test-image.jpg', { type: 'image/jpeg' })

    // Mock URL.createObjectURL
    global.URL.createObjectURL = vi.fn(() => 'blob:mock-url')

    render(<ImageUpload value={file} onChange={handleChange} />)

    expect(screen.getByText('test-image.jpg')).toBeInTheDocument()
    expect(screen.getByRole('img')).toHaveAttribute('src', 'blob:mock-url')
  })

  it('shows error for PDF file type', () => {
    const handleChange = vi.fn()
    const file = new File([''], 'test.pdf', { type: 'application/pdf' })

    render(<ImageUpload value={file} onChange={handleChange} />)

    expect(
      screen.getByText('Dozwolone formaty: JPEG, PNG, WebP, GIF'),
    ).toBeInTheDocument()
  })

  it('shows error for file larger than 10MB', () => {
    const handleChange = vi.fn()
    const tenMBPlusOne = 10 * 1024 * 1024 + 1
    const blob = new Blob([new ArrayBuffer(tenMBPlusOne)], {
      type: 'image/jpeg',
    })
    const file = new File([blob], 'large.jpg', { type: 'image/jpeg' })

    render(<ImageUpload value={file} onChange={handleChange} />)

    expect(screen.getByText(/10 MB/)).toBeInTheDocument()
  })

  it('calls onChange with null when Usuń button is clicked', () => {
    const handleChange = vi.fn()
    const file = new File([''], 'test-image.jpg', { type: 'image/jpeg' })
    global.URL.createObjectURL = vi.fn(() => 'blob:mock-url')
    global.URL.revokeObjectURL = vi.fn()

    render(<ImageUpload value={file} onChange={handleChange} />)

    const removeButton = screen.getByRole('button', { name: /Usuń/i })
    fireEvent.click(removeButton)

    expect(handleChange).toHaveBeenCalledWith(null)
  })

  it('calls onChange with File when valid file is selected', async () => {
    const handleChange = vi.fn()
    render(<ImageUpload value={null} onChange={handleChange} />)

    const input = screen.getByRole('presentation').querySelector('input[type="file"]')
    expect(input).not.toBeNull()

    const file = new File(['content'], 'test.jpg', { type: 'image/jpeg' })
    fireEvent.change(input!, { target: { files: [file] } })

    await waitFor(() => {
      expect(handleChange).toHaveBeenCalled()
      const calledFile = handleChange.mock.calls[0][0]
      expect(calledFile).toBeInstanceOf(File)
      expect(calledFile.name).toBe('test.jpg')
    })
  })

  it('does not show error when file is null (initial state)', () => {
    render(<ImageUpload value={null} onChange={vi.fn()} />)

    expect(
      screen.queryByText('Dozwolone formaty: JPEG, PNG, WebP, GIF'),
    ).not.toBeInTheDocument()
    expect(screen.queryByText(/10 MB/)).not.toBeInTheDocument()
  })

  it('shows error when error prop is provided', () => {
    render(
      <ImageUpload value={null} onChange={vi.fn()} error="Custom error message" />,
    )

    expect(screen.getByText('Custom error message')).toBeInTheDocument()
  })
})
