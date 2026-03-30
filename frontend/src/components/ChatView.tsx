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
        {/* Summary Bar */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-gray-200 bg-white">
          <div className="flex items-center gap-4 text-sm">
            <span className="font-medium text-text-primary">
              {sessionInfo.intent === 'RETURN' ? 'Zwrot' : 'Reklamacja'}
            </span>
            <span className="text-text-secondary">•</span>
            <span className="text-text-secondary">{sessionInfo.productName}</span>
            <span className="text-text-secondary">•</span>
            <span className="text-text-secondary">{sessionInfo.orderNumber}</span>
          </div>
          <button
            onClick={onNewSession}
            className="text-sm font-medium text-brand-accent hover:text-brand-accent/80"
          >
            Nowa sesja
          </button>
        </div>

        {/* Thread Area */}
        <ThreadPrimitive.Root className="flex-1 flex flex-col">
          <ThreadPrimitive.Viewport className="flex-1 overflow-y-auto p-4">
            <ThreadPrimitive.Messages
              components={{
                UserMessage: () => (
                  <div className="flex justify-end mb-4">
                    <div className="max-w-[80%] rounded-2xl bg-brand-accent px-4 py-2.5 text-sm text-white">
                      <MessagePrimitive.Root>
                        <MessagePrimitive.Parts />
                      </MessagePrimitive.Root>
                    </div>
                  </div>
                ),
                AssistantMessage: () => (
                  <div className="flex justify-start mb-4">
                    <div className="max-w-[80%] rounded-2xl bg-gray-100 px-4 py-2.5 text-sm text-text-primary">
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
            <ComposerPrimitive.Root className="flex w-full flex-col rounded-3xl border border-gray-300 bg-white">
              <ComposerPrimitive.Input
                placeholder="Napisz wiadomość..."
                className="min-h-10 w-full resize-none bg-transparent px-5 pt-3.5 pb-2.5 text-sm focus:outline-none"
                rows={1}
              />
              <div className="flex items-center justify-end px-2.5 pb-2.5">
                <ComposerPrimitive.Send className="flex size-8 items-center justify-center rounded-full bg-brand-accent text-white disabled:opacity-30">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className="size-4"
                  >
                    <path d="M5 12h14M12 5l7 7-7 7" />
                  </svg>
                </ComposerPrimitive.Send>
              </div>
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
