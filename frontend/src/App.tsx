import { useState } from 'react';
import { IntakeForm } from './components/IntakeForm';
import { ChatInterface } from './components/ChatInterface';
import type { IntakeFormData } from './lib/schemas';

type AppState = 'form' | 'chat' | 'rejected';

function App() {
  const [state, setState] = useState<AppState>('form');
  const [conversationId, setConversationId] = useState<string>('');
  const [requestType, setRequestType] = useState<'RETURN' | 'COMPLAINT'>('RETURN');
  const [initialImages, setInitialImages] = useState<File[]>([]);
  const [error, setError] = useState<string | null>(null);

  const handleFormSubmit = (data: IntakeFormData, id: string) => {
    setConversationId(id);
    setRequestType(data.requestType);
    setInitialImages(data.images);
    setState('chat');
    setError(null);
  };

  const handleFormError = (message: string) => {
    setError(message);
    setState('rejected');
  };

  const handleNewRequest = () => {
    setState('form');
    setConversationId('');
    setError(null);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8 max-w-4xl">
        <header className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">
            Sinsay Returns & Complaints Verification
          </h1>
          <p className="text-gray-600 mt-2">
            Submit your return or complaint request for AI-powered verification
          </p>
        </header>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
            <p className="font-semibold">Request Rejected</p>
            <p>{error}</p>
            <button
              onClick={handleNewRequest}
              className="mt-3 bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
            >
              Start New Request
            </button>
          </div>
        )}

        {state === 'form' && (
          <div className="bg-white rounded-lg shadow p-6">
            <IntakeForm
              onSubmit={handleFormSubmit}
              onError={handleFormError}
            />
          </div>
        )}

        {state === 'chat' && conversationId && (
          <div className="bg-white rounded-lg shadow p-6 h-[600px] flex flex-col">
            <ChatInterface
              conversationId={conversationId}
              requestType={requestType}
              initialImages={initialImages}
              onNewRequest={handleNewRequest}
            />
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
