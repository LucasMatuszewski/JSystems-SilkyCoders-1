import { useEffect, useState } from 'react'
import { useSession } from '../hooks/useSession'
import { useChatRuntime, AssistantChatTransport } from '@assistant-ui/react-ai-sdk'
import { AssistantRuntimeProvider } from '@assistant-ui/react'
import { ThreadPrimitive, ComposerPrimitive, MessagePrimitive } from '@assistant-ui/react'
import type { UIMessage } from 'ai'

interface SessionInfo {
  intent: string
  productName: string
  orderNumber: string
}

interface MessageDto {
  id: string
  role: 'USER' | 'ASSISTANT'
  content: string
  sequenceNumber: number
}

interface SessionResponse {
  session: SessionInfo
  messages: MessageDto[]
}

export interface ChatViewProps {
  sessionId: string
  onSessionInvalid: () => void
}

interface ChatRuntimeViewProps {
  sessionId: string
  sessionInfo: SessionInfo
  initialMessages: UIMessage[]
  onNewSession: () => void
}

function isSessionResponse(data: unknown): data is SessionResponse {
  if (typeof data !== 'object' || data === null) return false
  const d = data as Record<string, unknown>
  return typeof d['session'] === 'object' && d['session'] !== null && Array.isArray(d['messages'])
}

function mapMessageDtoToUIMessage(msg: MessageDto): UIMessage {
  return {
    id: msg.id,
    role: msg.role === 'USER' ? 'user' : 'assistant',
    parts: [{ type: 'text', text: msg.content }],
  }
}

function ChatRuntimeView({
  sessionId,
  sessionInfo,
  initialMessages,
  onNewSession,
}: ChatRuntimeViewProps): React.JSX.Element {
  const runtime = useChatRuntime({
    transport: new AssistantChatTransport({
      api: `/api/sessions/${sessionId}/messages`,
    }),
    messages: initialMessages,
  })

  return (
    <AssistantRuntimeProvider runtime={runtime}>
      <div className="h-screen flex flex-col bg-background">
        {/* Logo bar */}
        <div className="flex justify-center px-4 py-3 border-b border-gray-200 bg-white">
          <img src="/logo.svg" alt="Sinsay" className="h-7" />
        </div>

        {/* Summary Bar */}
        <div className="flex items-center justify-between px-4 py-2.5 border-b border-gray-200 bg-white">
          <div className="flex items-center gap-2 text-sm min-w-0 mr-4">
            <span
              className="font-semibold shrink-0 px-2 py-0.5 text-xs uppercase tracking-wide"
              style={{ backgroundColor: '#fff8f0', color: '#e09243', border: '1px solid #e09243' }}
            >
              {sessionInfo.intent === 'RETURN' ? 'Zwrot' : 'Reklamacja'}
            </span>
            <span className="text-text-secondary truncate">{sessionInfo.orderNumber}</span>
            <span className="text-text-secondary shrink-0">–</span>
            <span className="text-text-secondary truncate">{sessionInfo.productName}</span>
          </div>
          <button
            onClick={onNewSession}
            className="text-sm font-semibold shrink-0 px-3 py-1.5 border transition-colors"
            style={{
              color: '#16181d',
              borderColor: '#16181d',
              borderRadius: 0,
              backgroundColor: '#ffffff',
            }}
          >
            Nowa sesja
          </button>
        </div>

        {/* Thread Area */}
        <ThreadPrimitive.Root className="flex-1 flex flex-col min-h-0">
          <ThreadPrimitive.Viewport className="flex-1 overflow-y-auto p-4">
            <ThreadPrimitive.Messages
              components={{
                UserMessage: () => (
                  <div className="flex justify-end mb-4">
                    <div
                      className="max-w-[80%] px-4 py-2.5 text-sm text-white"
                      style={{ backgroundColor: '#e09243', borderRadius: 0 }}
                    >
                      <MessagePrimitive.Root>
                        <MessagePrimitive.Parts />
                      </MessagePrimitive.Root>
                    </div>
                  </div>
                ),
                AssistantMessage: () => (
                  <div className="flex justify-start mb-4">
                    <div
                      className="max-w-[80%] px-4 py-2.5 text-sm"
                      style={{
                        backgroundColor: '#f3f4f6',
                        color: '#333333',
                        borderRadius: 0,
                      }}
                    >
                      <MessagePrimitive.Root>
                        <MessagePrimitive.Parts />
                      </MessagePrimitive.Root>
                    </div>
                  </div>
                ),
              }}
            />
          </ThreadPrimitive.Viewport>

          {/* Composer */}
          <ThreadPrimitive.ViewportFooter className="border-t border-gray-200 bg-white p-4">
            <ComposerPrimitive.Root
              className="flex w-full items-center gap-2 border border-gray-300 bg-white px-4 py-2"
              style={{ borderRadius: 0 }}
            >
              <ComposerPrimitive.Input
                placeholder="Zadaj pytanie..."
                className="flex-1 min-h-8 resize-none bg-transparent text-sm focus:outline-none"
                rows={1}
              />
              <ComposerPrimitive.Send
                className="flex items-center gap-1.5 px-4 py-2 text-sm font-semibold text-white disabled:opacity-30"
                style={{ backgroundColor: '#e09243', borderRadius: 0 }}
              >
                Wyślij
              </ComposerPrimitive.Send>
            </ComposerPrimitive.Root>
          </ThreadPrimitive.ViewportFooter>
        </ThreadPrimitive.Root>
      </div>
    </AssistantRuntimeProvider>
  )
}

export default function ChatView({
  sessionId,
  onSessionInvalid,
}: ChatViewProps): React.JSX.Element {
  const { clearSession } = useSession()
  const [sessionData, setSessionData] = useState<{
    info: SessionInfo
    messages: UIMessage[]
  } | null>(null)

  useEffect(() => {
    const fetchSessionInfo = async () => {
      try {
        const response = await fetch(`/api/sessions/${sessionId}`)
        if (!response.ok) {
          if (response.status === 404) {
            onSessionInvalid()
            clearSession()
          }
          return
        }

        const data: unknown = await response.json()
        if (!isSessionResponse(data)) {
          console.error('Unexpected session response shape', data)
          return
        }

        setSessionData({
          info: {
            intent: data.session.intent,
            productName: data.session.productName,
            orderNumber: data.session.orderNumber,
          },
          messages: data.messages.map(mapMessageDtoToUIMessage),
        })
      } catch (error) {
        console.error('Failed to fetch session info:', error)
      }
    }

    fetchSessionInfo()
  }, [sessionId, onSessionInvalid, clearSession])

  const handleNewSession = () => {
    clearSession()
    onSessionInvalid()
  }

  if (sessionData === null) {
    return <div className="h-screen flex items-center justify-center bg-background" />
  }

  return (
    <ChatRuntimeView
      sessionId={sessionId}
      sessionInfo={sessionData.info}
      initialMessages={sessionData.messages}
      onNewSession={handleNewSession}
    />
  )
}
