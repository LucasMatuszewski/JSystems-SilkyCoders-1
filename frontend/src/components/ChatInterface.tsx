// @ts-ignore
import { useChat } from '@ai-sdk/react';
import type { SessionContext } from '../App';
import { useRef, useEffect } from 'react';

export function ChatInterface({ session }: { session: SessionContext }) {
  // @ts-ignore
  const { messages, input, handleInputChange, handleSubmit, isLoading } = useChat({
    api: "/api/chat",
    body: session,
    initialMessages: [
      {
        id: 'init-1',
        role: 'user',
        content: session.description,
        experimental_attachments: session.image ? [
            { name: 'evidence.png', contentType: 'image/png', url: session.image }
        ] : undefined
      }
    ]
  } as any);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <div className="flex flex-col h-full max-h-[600px]">
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {/* @ts-ignore */}
        {messages.map((m: any) => (
          <div key={m.id} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-[80%] rounded-lg p-3 ${
              m.role === 'user' ? 'bg-black text-white' : 'bg-gray-100 text-black'
            }`}>
              <div className="whitespace-pre-wrap text-sm">{m.content}</div>
              {m.experimental_attachments?.map((att: any, i: number) => (
                  <img key={i} src={att.url} alt="attachment" className="mt-2 max-h-40 rounded border border-white/20" />
              ))}
            </div>
          </div>
        ))}
        {isLoading && (
           <div className="flex justify-start">
             <div className="bg-gray-100 rounded-lg p-3 text-sm animate-pulse">Checking policy...</div>
           </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <form onSubmit={handleSubmit} className="p-4 border-t flex gap-2 bg-gray-50">
        <input 
          value={input} 
          onChange={handleInputChange} 
          placeholder="Type your reply..." 
          className="flex-1 border rounded px-3 py-2 focus:ring-black focus:border-black outline-none" 
        />
        <button type="submit" disabled={isLoading} className="bg-black text-white px-6 py-2 rounded font-medium disabled:opacity-50">
          Send
        </button>
      </form>
    </div>
  );
}
