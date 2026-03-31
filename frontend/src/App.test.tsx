import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import App from './App'

// Mock the ChatView and IntakeForm components to verify props
vi.mock('./components/ChatView', () => ({
  default: function MockChatView({
    sessionId,
    onSessionInvalid,
  }: {
    sessionId: string
    onSessionInvalid: () => void
  }) {
    return (
      <div data-testid="chat-view">
        <div data-testid="session-id">{sessionId}</div>
        <button onClick={onSessionInvalid}>Session Invalid</button>
      </div>
    )
  },
}))

vi.mock('./components/IntakeForm', () => ({
  default: function MockIntakeForm({ onSuccess }: { onSuccess: (sessionId: string) => void }) {
    return (
      <div data-testid="intake-form">
        <button onClick={() => onSuccess('new-session-id')}>Form Success</button>
      </div>
    )
  },
}))

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('renders IntakeForm when no sessionId in localStorage', () => {
    render(<App />)

    expect(screen.getByTestId('intake-form')).toBeInTheDocument()
    expect(screen.queryByTestId('chat-view')).not.toBeInTheDocument()
  })

  it('renders ChatView when sessionId exists in localStorage on mount', () => {
    localStorage.setItem('sinsay_session_id', 'existing-session-id')

    render(<App />)

    expect(screen.getByTestId('chat-view')).toBeInTheDocument()
    expect(screen.queryByTestId('intake-form')).not.toBeInTheDocument()
    expect(screen.getByTestId('session-id')).toHaveTextContent('existing-session-id')
  })

  it('switches to ChatView when IntakeForm onSuccess is called', async () => {
    const user = userEvent.setup()
    render(<App />)

    // Initially shows form
    expect(screen.getByTestId('intake-form')).toBeInTheDocument()
    expect(screen.queryByTestId('chat-view')).not.toBeInTheDocument()

    // Simulate form success
    const formSuccessButton = screen.getByRole('button', { name: 'Form Success' })
    await user.click(formSuccessButton)

    // Should switch to chat view
    await waitFor(() => {
      expect(screen.getByTestId('chat-view')).toBeInTheDocument()
      expect(screen.queryByTestId('intake-form')).not.toBeInTheDocument()
    })

    // Should store sessionId in localStorage
    expect(localStorage.getItem('sinsay_session_id')).toBe('new-session-id')

    // ChatView should receive the correct sessionId
    expect(screen.getByTestId('session-id')).toHaveTextContent('new-session-id')
  })

  it('switches back to IntakeForm when ChatView onSessionInvalid is called', async () => {
    const user = userEvent.setup()
    localStorage.setItem('sinsay_session_id', 'test-session-id')

    render(<App />)

    // Initially shows chat view
    expect(screen.getByTestId('chat-view')).toBeInTheDocument()
    expect(screen.queryByTestId('intake-form')).not.toBeInTheDocument()

    // Simulate session invalid
    const sessionInvalidButton = screen.getByRole('button', { name: 'Session Invalid' })
    await user.click(sessionInvalidButton)

    // Should switch back to form
    await waitFor(() => {
      expect(screen.getByTestId('intake-form')).toBeInTheDocument()
      expect(screen.queryByTestId('chat-view')).not.toBeInTheDocument()
    })

    // Should clear sessionId from localStorage
    expect(localStorage.getItem('sinsay_session_id')).toBeNull()
  })

  it('passes correct sessionId prop to ChatView', () => {
    const testSessionId = 'specific-session-id-123'
    localStorage.setItem('sinsay_session_id', testSessionId)

    render(<App />)

    expect(screen.getByTestId('chat-view')).toBeInTheDocument()
    expect(screen.getByTestId('session-id')).toHaveTextContent(testSessionId)
  })

  it('displays correct page title', () => {
    render(<App />)
    expect(document.title).toBe('Sprawdź zwrot lub reklamację')
  })
})
