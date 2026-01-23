import { useState } from 'react'
import { IntakeForm } from './components/IntakeForm'
import { ChatInterface } from './components/ChatInterface'

export type SessionContext = {
  orderId: string;
  intent: string;
  description: string;
  image?: string | null;
}

function App() {
  const [session, setSession] = useState<SessionContext | null>(null);

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="w-full max-w-2xl bg-white rounded-xl shadow-lg overflow-hidden min-h-[600px] flex flex-col">
        <header className="bg-black text-white p-4 text-center font-bold text-xl uppercase tracking-widest">
          Sinsay Returns AI
        </header>
        
        <main className="flex-1 p-6 relative">
          {!session ? (
            <IntakeForm onSubmit={setSession} />
          ) : (
            <ChatInterface session={session} />
          )}
        </main>
      </div>
    </div>
  )
}

export default App