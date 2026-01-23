import type { SessionContext } from '../App';
import { useRef, useEffect, useState, useCallback } from 'react';

interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
}

const MessageBubble = ({
  m,
  session,
}: {
  m: Message;
  session: SessionContext;
}) => {
  const isAssistant = m.role === 'assistant';

  // Extract thought and actual content
  const thoughtMatch = m.content.match(/<thought>([\s\S]*?)<\/thought>/);
  const thought = thoughtMatch ? thoughtMatch[1].trim() : null;
  const displayContent = m.content
    .replace(/<thought>[\s\S]*?<\/thought>/, '')
    .trim();

  return (
    <div
      className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}
    >
      <div
        className={`max-w-[85%] animate-in fade-in slide-in-from-bottom-2 duration-300 ${
          m.role === 'user'
            ? 'bg-black text-white px-5 py-3 rounded-2xl rounded-tr-none shadow-sm'
            : 'bg-white border border-gray-100 text-black px-5 py-3 rounded-2xl rounded-tl-none shadow-sm'
        }`}
      >
        {isAssistant && thought && (
          <div className="mb-3 pb-3 border-b border-gray-50">
            <div className="flex items-center gap-2 mb-2">
              <div className="w-1.5 h-1.5 rounded-full bg-blue-500"></div>
              <span className="text-[10px] font-bold uppercase tracking-widest text-gray-400">
                Analiza zgłoszenia
              </span>
            </div>
            <div className="text-[11px] text-gray-400 italic leading-relaxed space-y-1">
              {thought.split('\n').map((line, i) => (
                <div key={i}>{line.replace(/^\[|\]$/g, '')}</div>
              ))}
            </div>
          </div>
        )}

        <div className="whitespace-pre-wrap text-sm leading-relaxed">
          {displayContent}
        </div>

        {m.id === 'init-1' && session.image && (
          <div className="mt-4 relative group">
            <img
              src={session.image}
              alt="attachment"
              className="max-h-48 rounded-lg border border-black/10 transition-transform group-hover:scale-[1.02]"
            />
            <div className="absolute top-2 left-2 bg-black/60 text-[8px] text-white px-2 py-1 rounded-full uppercase font-bold tracking-tighter backdrop-blur-sm">
              Załącznik
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export function ChatInterface({ session }: { session: SessionContext }) {
  const [messages, setMessages] = useState<Message[]>([
    { id: 'init-1', role: 'user', content: session.description },
  ]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const hasSentInitialRef = useRef(false);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = useCallback(
    async (userMessages: Message[], isInitial = false) => {
      setIsLoading(true);
      setError(null);

      try {
        const response = await fetch('/api/chat', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            orderId: session.orderId,
            intent: session.intent,
            description: session.description,
            messages: userMessages.map((m, i) => ({
              role: m.role,
              content: m.content,
              ...(i === 0 && isInitial && session.image
                ? {
                    experimental_attachments: [{ url: session.image }],
                  }
                : {}),
            })),
          }),
        });

        if (!response.ok) throw new Error(`API error: ${response.status}`);
        if (!response.body) throw new Error('No response body');

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let assistantContent = '';
        const assistantId = `assistant-${Date.now()}`;
        let buffer = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop() || '';

          for (const line of lines) {
            if (line.startsWith('data:0:')) {
              try {
                const text = JSON.parse(line.slice(7));
                assistantContent += text;

                setMessages((prev) => {
                  const existing = prev.find((m) => m.id === assistantId);
                  if (existing) {
                    return prev.map((m) =>
                      m.id === assistantId
                        ? { ...m, content: assistantContent }
                        : m,
                    );
                  }
                  return [
                    ...prev,
                    {
                      id: assistantId,
                      role: 'assistant' as const,
                      content: assistantContent,
                    },
                  ];
                });
              } catch (e) {
                // Ignore parsing errors
              }
            }
          }
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
      } finally {
        setIsLoading(false);
      }
    },
    [session],
  );

  useEffect(() => {
    if (!hasSentInitialRef.current) {
      hasSentInitialRef.current = true;
      sendMessage(
        [{ id: 'init-1', role: 'user', content: session.description }],
        true,
      );
    }
  }, [sendMessage, session.description]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMsg: Message = {
      id: `user-${Date.now()}`,
      role: 'user',
      content: input,
    };
    const newMessages = [...messages, userMsg];
    setMessages(newMessages);
    setInput('');
    await sendMessage(newMessages);
  };

  return (
    <div className="flex flex-col h-[600px] border border-gray-100 bg-white shadow-2xl rounded-3xl overflow-hidden animate-scale-in">
      <div className="bg-white border-b border-gray-50 p-6 flex justify-between items-center">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-black rounded-full flex items-center justify-center text-white font-bold text-xs ring-4 ring-gray-50">
            S
          </div>
          <div>
            <h3 className="text-xs font-black uppercase tracking-widest text-black">
              Asystent Sinsay
            </h3>
            <p className="text-[10px] text-green-500 font-bold uppercase tracking-tighter">
              Online • Weryfikacja AI
            </p>
          </div>
        </div>
        <div className="flex gap-1.5">
          <div className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></div>
          <div className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse delay-75"></div>
          <div className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse delay-150"></div>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-6 space-y-6 bg-[radial-gradient(#e5e7eb_1px,transparent_1px)] [background-size:20px_20px]">
        {messages.map((m) => (
          <MessageBubble key={m.id} m={m} session={session} />
        ))}
        {isLoading &&
          messages.filter((m) => m.role === 'assistant').length === 0 && (
            <div className="flex justify-start">
              <div className="bg-white border border-gray-100 px-5 py-3 rounded-2xl rounded-tl-none text-xs text-gray-400 italic animate-pulse shadow-sm">
                Analizuję zgłoszenie i zdjęcia...
              </div>
            </div>
          )}
        {error && (
          <div className="flex justify-center">
            <div className="bg-red-50 text-red-600 border border-red-100 px-4 py-2 rounded-full text-[10px] uppercase font-bold tracking-widest">
              Błąd: {error}
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <form
        onSubmit={handleSubmit}
        className="p-6 border-t border-gray-50 flex gap-3 bg-white"
      >
        <div className="flex-1 relative">
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Napisz odpowiedź..."
            className="sinsay-input pr-12 !bg-gray-50 border-transparent focus:!bg-white focus:!border-black transition-all"
          />
          {!input.trim() && (
            <div className="absolute right-4 top-1/2 -translate-y-1/2">
              <svg
                className="w-5 h-5 text-gray-300"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"
                />
              </svg>
            </div>
          )}
        </div>
        <button
          type="submit"
          disabled={isLoading || !input.trim()}
          className="bg-black text-white w-12 h-12 flex items-center justify-center rounded-full disabled:opacity-30 disabled:grayscale transition-all hover:scale-110 active:scale-95 shadow-xl shadow-black/20"
        >
          <svg
            className="w-5 h-5 translate-x-0.5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M14 5l7 7m0 0l-7 7m7-7H3"
            />
          </svg>
        </button>
      </form>
    </div>
  );
}
