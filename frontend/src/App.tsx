import { useEffect } from 'react'
import { useSession } from './hooks/useSession'
import IntakeForm from './components/IntakeForm'
import ChatView from './components/ChatView'

function App(): React.JSX.Element {
  const { sessionId, setSessionId, clearSession } = useSession()

  // Set page title
  useEffect(() => {
    document.title = 'Sprawdź zwrot lub reklamację'
  }, [])

  // Derive view from sessionId - no separate state needed
  const view = sessionId ? 'chat' : 'form'

  const handleFormSuccess = (newSessionId: string) => {
    setSessionId(newSessionId)
  }

  const handleSessionInvalid = () => {
    clearSession()
  }

  return (
    <main className="min-h-screen bg-background">
      {view === 'form' && <IntakeForm onSuccess={handleFormSuccess} />}
      {view === 'chat' && sessionId && (
        <ChatView sessionId={sessionId} onSessionInvalid={handleSessionInvalid} />
      )}
    </main>
  )
}

export default App
