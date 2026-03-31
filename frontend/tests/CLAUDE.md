# Frontend Test Guidelines

## Framework

**Vitest** + **React Testing Library** for unit and component tests.
**MSW** (Mock Service Worker) for HTTP mocking — never call the real backend.

## Validation Loop

Run before every commit:
```bash
npm test          # Vitest
npm run lint      # ESLint
npm run format:check  # Prettier
```

## Key Test Scenarios

**IntakeForm:**
- Shows inline error for each required field when empty
- Shows error for invalid image MIME type
- Shows error for image exceeding 10MB
- Shows loading state during submit
- Writes `sinsay_session_id` to localStorage on successful submit

**ChatView:**
- Loads session history on mount (via GET /api/sessions/{id})
- Renders streamed assistant messages
- "Nowa sesja" button clears localStorage and returns to IntakeForm

**useSession hook:**
- Reads/writes/clears `sinsay_session_id` from localStorage correctly

**ImageUpload:**
- Accepts valid MIME types
- Rejects oversized files with error message
- Shows thumbnail preview after valid selection
