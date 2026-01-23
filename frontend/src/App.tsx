import { useState, Fragment } from 'react';
import { IntakeForm } from './components/IntakeForm';
import { ChatInterface } from './components/ChatInterface';
import { Layout } from './components/Layout';
import './App.css';

export type SessionContext = {
  orderId: string;
  intent: string;
  description: string;
  image?: string | null;
};

function App() {
  const [session, setSession] = useState<SessionContext | null>(null);

  const steps = [
    { id: 1, label: 'Informacje', active: !session },
    { id: 2, label: 'Weryfikacja AI', active: !!session },
    { id: 3, label: 'Decyzja', active: false },
  ];

  return (
    <Layout>
      <div className="bg-white p-2 md:p-6 animate-fade-in">
        <div className="max-w-2xl mx-auto">
          <header className="mb-12 text-center">
            <h1 className="text-3xl font-black mb-4 tracking-tight">
              Automatyczny Asystent Zwrotów
            </h1>

            {/* Progress Chart / Steps */}
            <div className="flex justify-center items-center gap-4 mb-8">
              {steps.map((step, i) => (
                <Fragment key={step.id}>
                  <div className="flex flex-col items-center gap-2">
                    <div
                      className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all duration-500 ${step.active ? 'bg-black text-white scale-110 shadow-lg' : 'bg-gray-100 text-gray-400'}`}
                    >
                      {step.id}
                    </div>
                    <span
                      className={`text-[10px] font-bold uppercase tracking-widest ${step.active ? 'text-black' : 'text-gray-300'}`}
                    >
                      {step.label}
                    </span>
                  </div>
                  {i < steps.length - 1 && (
                    <div className="w-12 h-[2px] bg-gray-100 -translate-y-3">
                      <div
                        className={`h-full bg-black transition-all duration-1000 ${session ? 'w-full' : 'w-0'}`}
                      ></div>
                    </div>
                  )}
                </Fragment>
              ))}
            </div>

            <p className="text-gray-500 text-sm max-w-md mx-auto">
              {!session
                ? 'Wypełnij poniższe dane, aby rozpocząć proces analizy Twojego zgłoszenia przez sztuczną inteligencję.'
                : 'Nasza AI analizuje teraz Twój przypadek. Możesz doprecyzować szczegódy w czacie.'}
            </p>
          </header>

          <div className="relative">
            {!session ? (
              <IntakeForm onSubmit={setSession} />
            ) : (
              <ChatInterface session={session} />
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default App;
