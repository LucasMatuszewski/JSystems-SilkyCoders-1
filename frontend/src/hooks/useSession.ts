import { useState } from 'react'

const LOCAL_STORAGE_KEY = 'sinsay_session_id'

export interface UseSessionReturn {
  sessionId: string | null
  setSessionId: (id: string) => void
  clearSession: () => void
}

export function useSession(): UseSessionReturn {
  const [sessionId, setSessionIdState] = useState<string | null>(() => {
    // Read from localStorage on init
    if (typeof window !== 'undefined') {
      return localStorage.getItem(LOCAL_STORAGE_KEY)
    }
    return null
  })

  const setSessionId = (id: string) => {
    localStorage.setItem(LOCAL_STORAGE_KEY, id)
    setSessionIdState(id)
  }

  const clearSession = () => {
    localStorage.removeItem(LOCAL_STORAGE_KEY)
    setSessionIdState(null)
  }

  return { sessionId, setSessionId, clearSession }
}
