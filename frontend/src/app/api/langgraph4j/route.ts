import {
  CopilotRuntime,
  ExperimentalEmptyAdapter,
  copilotRuntimeNextJSAppRouterEndpoint,
} from '@copilotkit/runtime';
import { LangGraphHttpAgent } from '@copilotkit/runtime/langgraph';
import { NextRequest } from 'next/server';

const serviceAdapter = new ExperimentalEmptyAdapter();

const runtime = new CopilotRuntime({
  agents: {
    default: new LangGraphHttpAgent({
      url:
        process.env.NEXT_PUBLIC_LANGGRAPH_URL ||
        'http://localhost:8085/langgraph4j/copilotkit',
    }),
  },
});

export const POST = async (req: NextRequest) => {
  const { handleRequest } = copilotRuntimeNextJSAppRouterEndpoint({
    runtime,
    serviceAdapter,
    endpoint: '/api/langgraph4j',
  });

  // Diagnostic: log whether this is a Phase 1 or Phase 2 request and image size.
  // Uses req.clone() so the original body is untouched for handleRequest.
  req.clone().json().then((body: Record<string, unknown>) => {
    const messages = (body?.messages as Array<Record<string, unknown>>) ?? [];
    const resultMessages = messages.filter((m) => m.role === 'tool');
    if (resultMessages.length > 0) {
      const content = String(resultMessages[0]?.content ?? '');
      const hasPhoto = content.includes('"photo"') && content.length > 200;
      console.log(
        `[langgraph4j] Phase 2 request — ResultMessage content: ${content.length} chars, hasPhoto=${hasPhoto}`
      );
    } else {
      console.log(`[langgraph4j] Phase 1 request — ${messages.length} messages`);
    }
  }).catch(() => { /* body not JSON or already consumed — ignore */ });

  return handleRequest(req);
};
