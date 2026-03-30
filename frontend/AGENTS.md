# Frontend Guidelines

See root `AGENTS.md` for project overview. This file covers frontend-specific implementation rules.

## Tech Stack

React 19, TypeScript (strict), Vite, Tailwind CSS, Shadcn/ui, assistant-ui, Vercel AI SDK

## Component Structure

```
src/
  App.tsx                 Root; reads sessionId from localStorage; renders IntakeForm or ChatView
  components/
    IntakeForm.tsx        5-field form + submit → POST /api/sessions
    ChatView.tsx          Chat UI with assistant-ui + summary bar + "Nowa sesja" button
    ImageUpload.tsx       Drag-and-drop + click; MIME/size validation; thumbnail preview
  hooks/
    useSession.ts         Read/write sessionId to localStorage (key: sinsay_session_id)
  components/ui/          Shadcn/ui components
```

## Form Fields

| Field | Type | Validation |
|---|---|---|
| `intent` | radio — `RETURN` \| `COMPLAINT` | required |
| `orderNumber` | string | required |
| `productName` | string | required |
| `description` | string | required |
| `image` | File | required; JPEG/PNG/WebP/GIF only; max 10MB |

Validate with **Zod**. Show inline errors per field. Submit as `multipart/form-data` to `POST /api/sessions`.

## Chat Integration

Use `useChatRuntime` with `AssistantChatTransport` from `@assistant-ui/react-ai-sdk`:

```ts
import { useChatRuntime, AssistantChatTransport } from "@assistant-ui/react-ai-sdk";

const runtime = useChatRuntime({
  transport: new AssistantChatTransport({
    api: `/api/sessions/${sessionId}/messages`,
  }),
});
```

Pass `runtime` to `<AssistantRuntimeProvider>`. Do NOT use `useLocalRuntime`.

The `AssistantChatTransport` sends `{ messages, system, tools }` to the backend and expects SSE responses in Vercel AI SDK v6 UI Message Stream format (`x-vercel-ai-ui-message-stream: v1` header). The backend extracts only the last user message from `messages[]`.

For session resume, map messages from `GET /api/sessions/{id}` to the `UIMessage` format:
```ts
{ id: string, role: 'user' | 'assistant', parts: [{ type: 'text', text: string }] }
```

## Session Flow

**New session:** Form submit → loading state → success: `{sessionId, message}` → write `sessionId` to localStorage → `App` switches to `ChatView` → initial AI message rendered.

**Resume session:** App mounts → reads `sinsay_session_id` from localStorage → renders `ChatView` → `GET /api/sessions/{id}` → maps history to `Message[]` → passed as `initialMessages`.

**New session button:** Clear `sinsay_session_id` from localStorage → `App` switches back to `IntakeForm`.

## Image Handling

- Validate MIME type + file size on file selection (immediate inline error)
- Resize to max 1024px on the longest side before upload (canvas resize)
- Include as `image` field in multipart form

## Vite Configuration

- Dev proxy: `/api/*` → `http://localhost:8080`
- Build output: `../backend/src/main/resources/static/`
- All UI text must be in **Polish**

## Coding Conventions

**TypeScript:** Strict mode. Always annotate types. Prefer `interface` over `type`. No `any`, no `as`/`!` assertions. Use type guards for runtime narrowing. Functional components with TypeScript interfaces.

## Testing

**Unit/Integration:** Vitest + MSW for API mocks.
**E2E:** Playwright, REAL stack (no `page.route()` API mocking, do real END to END tests! No Mocks!).

## Verification
1. `npm test` — pass
2. `npm run lint` — no errors
3. `npm run format:check` — no violations
4. `npm run build` — succeeds
5. `npm run dev` — STARTS, no console errors
6. Manual test with real backend at least once per task

If app won't load → fix before commit.

## Completion Criteria

- All tests and ESLint pass
- Check the application visually, use puppeter to make a screen and align it with:
  - wireframe of the Form (view 1) `docs\wireframe-form.png`
  - wireframe of decision and chat (view 2) `docs\wireframe-decision+chat.png`
  - screen of Sinsay Home Page `assets\sinsay-homepage.png`
