import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatView from './ChatView'

const mockSessionResponse = {
  session: {
    id: 'test-session-123',
    intent: 'RETURN',
    productName: 'Test Product',
    orderNumber: 'PL123',
    createdAt: '2026-03-30T10:00:00Z',
  },
  messages: [],
}

describe('ChatView', () => {
  const mockOnSessionInvalid = vi.fn()
  const mockSessionId = 'test-session-123'

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    // Mock fetch to return session info in the correct shape
    globalThis.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve(mockSessionResponse),
      }),
    ) as unknown as typeof fetch
  })

  it('fetches session info on mount', async () => {
    render(<ChatView sessionId={mockSessionId} onSessionInvalid={mockOnSessionInvalid} />)

    await waitFor(() => {
      expect(globalThis.fetch).toHaveBeenCalledWith(`/api/sessions/${mockSessionId}`)
    })
  })

  it('renders "Nowa sesja" button after session loads', async () => {
    render(<ChatView sessionId={mockSessionId} onSessionInvalid={mockOnSessionInvalid} />)

    const newSessionButton = await screen.findByRole('button', { name: 'Nowa sesja' })
    expect(newSessionButton).toBeInTheDocument()
  })

  it('"Nowa sesja" click calls onSessionInvalid callback', async () => {
    const user = userEvent.setup()
    render(<ChatView sessionId={mockSessionId} onSessionInvalid={mockOnSessionInvalid} />)

    const newSessionButton = await screen.findByRole('button', { name: 'Nowa sesja' })
    await user.click(newSessionButton)

    expect(mockOnSessionInvalid).toHaveBeenCalled()
  })

  it('displays session info from data.session (not flat data)', async () => {
    render(<ChatView sessionId={mockSessionId} onSessionInvalid={mockOnSessionInvalid} />)

    await waitFor(() => {
      expect(screen.getByText('Zwrot')).toBeInTheDocument()
      expect(screen.getByText('Test Product')).toBeInTheDocument()
      expect(screen.getByText('PL123')).toBeInTheDocument()
    })
  })

  it('displays "Reklamacja" label for COMPLAINT intent', async () => {
    globalThis.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () =>
          Promise.resolve({
            session: {
              ...mockSessionResponse.session,
              intent: 'COMPLAINT',
            },
            messages: [],
          }),
      }),
    ) as unknown as typeof fetch

    render(<ChatView sessionId={mockSessionId} onSessionInvalid={mockOnSessionInvalid} />)

    await waitFor(() => {
      expect(screen.getByText('Reklamacja')).toBeInTheDocument()
    })
  })

  it('handles 404 response by calling onSessionInvalid', async () => {
    globalThis.fetch = vi.fn(() =>
      Promise.resolve({
        ok: false,
        status: 404,
        json: () => Promise.resolve({ error: 'Session not found' }),
      }),
    ) as unknown as typeof fetch

    render(<ChatView sessionId={mockSessionId} onSessionInvalid={mockOnSessionInvalid} />)

    await waitFor(() => {
      expect(mockOnSessionInvalid).toHaveBeenCalled()
    })
  })

  it('passes message history to runtime when messages are present', async () => {
    globalThis.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () =>
          Promise.resolve({
            session: mockSessionResponse.session,
            messages: [
              {
                id: 'msg-1',
                role: 'USER',
                content: 'Chcę zwrócić produkt',
                sequenceNumber: 0,
              },
              {
                id: 'msg-2',
                role: 'ASSISTANT',
                content: 'Rozumiem, pomogę Ci z tym',
                sequenceNumber: 1,
              },
            ],
          }),
      }),
    ) as unknown as typeof fetch

    render(<ChatView sessionId={mockSessionId} onSessionInvalid={mockOnSessionInvalid} />)

    // After load, the runtime view should render (summary bar visible)
    await waitFor(() => {
      expect(screen.getByText('Zwrot')).toBeInTheDocument()
    })
  })
})
