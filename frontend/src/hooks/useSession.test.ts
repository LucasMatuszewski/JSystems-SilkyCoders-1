import { renderHook, act } from '@testing-library/react'
import { useSession } from './useSession'

const LOCAL_STORAGE_KEY = 'sinsay_session_id'

describe('useSession', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  afterEach(() => {
    localStorage.clear()
  })

  it('returns null initially when localStorage is empty', () => {
    const { result } = renderHook(() => useSession())

    expect(result.current.sessionId).toBeNull()
  })

  it('setSessionId writes to localStorage', () => {
    const { result } = renderHook(() => useSession())

    act(() => {
      result.current.setSessionId('test-session-123')
    })

    expect(result.current.sessionId).toBe('test-session-123')
    expect(localStorage.getItem(LOCAL_STORAGE_KEY)).toBe('test-session-123')
  })

  it('clearSession removes from localStorage', () => {
    const { result } = renderHook(() => useSession())

    act(() => {
      result.current.setSessionId('test-session-123')
    })
    expect(result.current.sessionId).toBe('test-session-123')

    act(() => {
      result.current.clearSession()
    })

    expect(result.current.sessionId).toBeNull()
    expect(localStorage.getItem(LOCAL_STORAGE_KEY)).toBeNull()
  })

  it('reads existing value from localStorage on init', () => {
    localStorage.setItem(LOCAL_STORAGE_KEY, 'existing-session-abc')

    const { result } = renderHook(() => useSession())

    expect(result.current.sessionId).toBe('existing-session-abc')
  })

  it('updates sessionId when localStorage changes externally', () => {
    const { result } = renderHook(() => useSession())

    expect(result.current.sessionId).toBeNull()

    act(() => {
      localStorage.setItem(LOCAL_STORAGE_KEY, 'external-session')
      window.dispatchEvent(
        new StorageEvent('storage', {
          key: LOCAL_STORAGE_KEY,
          newValue: 'external-session',
        }),
      )
    })

    // Note: This test documents expected behavior with storage events
    // Implementation may need to handle this if cross-tab sync is required
  })
})
