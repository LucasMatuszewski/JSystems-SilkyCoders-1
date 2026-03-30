import { http, HttpResponse, delay } from 'msw'

// Global variable to control mock responses (for testing error scenarios)
export let mockSessionError: 'server' | 'validation' | null = null

export function setMockSessionError(error: 'server' | 'validation' | null) {
  mockSessionError = error
}

export const handlers = [
  // POST /api/sessions - Create a new session
  http.post('/api/sessions', async () => {
    // Check if we should return an error
    if (mockSessionError === 'server') {
      return HttpResponse.json({ error: 'Błąd serwera' }, { status: 500 })
    }

    if (mockSessionError === 'validation') {
      return HttpResponse.json({ error: 'Nieprawidłowe dane' }, { status: 400 })
    }

    // Return successful response with a small delay to simulate network
    await delay(100)
    return HttpResponse.json({
      sessionId: 'test-session-123',
      message: 'Dziękuję za zgłoszenie. Analizuję przypadek...',
    })
  }),

  // GET /api/sessions/:id - Get session history
  http.get('/api/sessions/:id', ({ params }) => {
    const sessionId = params.id as string

    // Simulate 404 for invalid session
    if (sessionId === 'invalid-session-id') {
      return HttpResponse.json({ error: 'Session not found' }, { status: 404 })
    }

    // Return mock session history
    return HttpResponse.json({
      id: sessionId,
      messages: [
        {
          id: 'msg-1',
          role: 'user',
          content: 'Chcę zwrócić sukienkę',
          createdAt: '2026-03-30T10:00:00Z',
        },
        {
          id: 'msg-2',
          role: 'assistant',
          content: 'Dziękuję za zgłoszenie. Analizuję przypadek...',
          createdAt: '2026-03-30T10:00:01Z',
        },
      ],
    })
  }),
]
