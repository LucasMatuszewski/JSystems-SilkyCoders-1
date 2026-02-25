# Product Requirements Document: Sinsay AI Assistant PoC — v2.0

**Version:** 2.0
**Status:** Active
**Date:** 2026-02-25
**Supersedes:** `PRD-Sinsay-PoC.md` (v1.0 — form-first flow)
**Target Audience:** AI coding assistants and developers

---

## 1. Introduction & Overview

This document describes the **v2.0 redesign** of the Sinsay AI Assistant PoC. The core change from v1.0 is the UX paradigm shift: instead of a form-first flow where the user must fill a structured form before accessing AI chat, the application now opens **directly as a chat**.

The user arrives at a chat window and can have a natural conversation — asking about the Sinsay offer, return windows, complaint procedures, sizing, store locations, or any general customer service question. The AI responds as a knowledgeable Sinsay assistant.

When the conversation reveals that the user wants to **initiate a return (*Zwrot*)** or file a **complaint (*Reklamacja*)**, the AI agent automatically injects a **structured form component directly into the chat window** (as an interactive chat message). The user fills the form in-context, submits it, and the AI analyzes the data and photo to issue a verdict — all without leaving the chat.

**Key distinction from v1.0:**
- v1.0: Form → Chat (linear, form is the entry point)
- v2.0: Chat → Form-in-Chat → Verdict (form appears contextually, mid-conversation, as a chat component)

---

## 2. Goals

1. **Natural entry point:** Users start a conversation without any barrier — no form to fill before they can ask a question.
2. **Contextual form injection:** The structured data collection form appears only when needed, triggered by the AI detecting return/complaint intent.
3. **Validate AI tool-use UX:** Test the `assistant-ui` tool call rendering pattern to display interactive React components inside the chat thread.
4. **Validate AI verdict quality:** Confirm that GPT-4o can analyze a product photo + description and cross-reference it against Sinsay's return/complaint regulations to produce a justified verdict in Polish.
5. **Data persistence:** Log all sessions, form submissions, photos, and verdicts to a local SQLite database for post-analysis.

---

## 3. User Stories

### General Chat
1. **As a Customer**, I want to ask the AI about Sinsay's return policy so I know my options before committing to a return.
2. **As a Customer**, I want to ask general questions about the Sinsay offer, store locations, or current promotions.
3. **As a Customer**, I want the AI to respond in Polish so the experience feels natural.

### Return/Complaint Flow
4. **As a Customer**, when I say I want to return a product, I want a form to appear in the chat — not on a separate page — so I can provide the details without losing my conversation context.
5. **As a Customer**, I want the return/complaint type to be pre-selected in the form based on what I told the AI, but I want to be able to change it in case the AI misunderstood.
6. **As a Customer**, I want all form fields to be required so I don't accidentally submit incomplete information.
7. **As a Customer**, I want to upload a photo of the product (or defect) directly in the form so the AI can see the actual condition of the item.
8. **As a Customer**, after submitting the form, I want the AI to give me a clear verdict — justified or not — explained in Polish, with references to the applicable policy.
9. **As a Customer**, I want to be able to continue the conversation after receiving a verdict, to ask follow-up questions.

### Developer
10. **As a Developer**, I want form submissions and verdicts stored in SQLite with timestamps so I can audit the AI's accuracy.

---

## 4. Application Flow

```
[User opens chat]
       │
       ▼
[General conversation with AI]
  - Questions about offer, returns policy, complaints, etc.
  - AI responds in Polish as a Sinsay assistant
       │
       ├─ [User expresses return/complaint intent]
       │         │
       │         ▼
       │  [AI calls show_form tool]
       │  AI pre-fills the "type" field (Return or Complaint)
       │  based on the conversation context
       │         │
       │         ▼
       │  [Form component renders IN the chat thread]
       │  User sees the form as an interactive chat message
       │  User fills all fields, can change the pre-selected type
       │  User submits the form
       │         │
       │         ▼
       │  [Form data + photo sent to /api/chat]
       │  Agent analyzes: product name, type, description, photo
       │  Agent cross-references: Sinsay regulations (zwrot, reklamacje, regulamin)
       │         │
       │         ▼
       │  [AI streams verdict as a chat message in Polish]
       │  Verdict: justified/not justified + reasoning
       │         │
       │         ▼
       └─ [Conversation continues — user can ask follow-up questions]
```

---

## 5. Functional Requirements

### 5.1 Chat Interface

- The application opens directly as a full-screen chat window — no landing form, no onboarding screen.
- The AI assistant introduces itself briefly on first load (a welcome message, hardcoded or streamed).
- The user can type freely and receive responses streamed via SSE.
- The chat history must persist for the duration of the browser session (no page-reload wipe).
- The chat must display all message types: plain text AI messages, plain text user messages, and **in-chat form components** (see §5.2).

### 5.2 In-Chat Form Component (Critical)

This is the central new feature of v2.0. When the AI detects return/complaint intent:

**Trigger mechanism:** The backend AI calls a tool named `show_return_form`. The tool call result is rendered by the frontend as an interactive React component embedded in the chat thread, appearing as a distinct "card" in the message list.

**Form fields (all mandatory):**

| Field | Type | Details |
|---|---|---|
| `productName` | Text input | Name of the product being returned or complained about |
| `type` | Select dropdown | Options: `Return` (Zwrot), `Complaint` (Reklamacja). Pre-populated by the AI via the tool call parameters, but fully editable by the user |
| `defectDescription` | Textarea | Description of the defect (for complaints) or reason for return (for returns). Min 20 characters |
| `photo` | File input | Photo of the product or defect. Accepted: JPEG, PNG, WebP. Max display: 1 file. Mandatory |
| — | Submit button | "Send to Assistant" — sends the completed form back as a user message |

**Form behaviour:**
- All fields validate on submit attempt; errors display inline below each field.
- The submit button is disabled until all fields are filled and a photo is attached.
- After successful submission, the form card becomes read-only (frozen state showing submitted values) — the user cannot re-submit the same form.
- The photo is resized on the client to max 1024px (longest edge) before upload using the Canvas API.
- The form card must be visually distinct from regular chat messages (e.g., bordered card with a light background).

**Tool call data contract** (backend → frontend via SSE tool call chunk):
```json
{
  "tool": "show_return_form",
  "parameters": {
    "preselectedType": "return" | "complaint",
    "contextSummary": "Short sentence from AI explaining why the form is being shown"
  }
}
```

**Form submission data contract** (frontend → backend, as a user message payload):
```json
{
  "formData": {
    "productName": "string",
    "type": "return" | "complaint",
    "defectDescription": "string",
    "photo": "<base64-encoded image, resized to max 1024px>"
  }
}
```

### 5.3 Backend API

1. **Endpoint:** `POST /api/chat`
2. **Streaming:** Server-Sent Events (SSE), strictly following the **Vercel AI SDK Data Stream Protocol**:
   - Text chunks: `0:"token"`
   - Tool call chunks: `9:{...}` (tool call invocation)
   - Tool result chunks: `a:{...}` (tool result / parameters for the frontend to render)
   - Finish chunks: `8:[{"finishReason":"stop",...}]`
3. **Request body:** Vercel AI SDK message array format — includes conversation history and, when submitting a form, a user message with `formData` embedded in the content.
4. **AI model:** `gpt-4o` via `spring-ai-starter-model-openai`.
5. **Image handling:** The backend receives the base64 photo and passes it to the ChatClient using Spring AI's `Media` object for multimodal analysis.

### 5.4 AI Agent Behaviour

#### General conversation mode
- The agent acts as a knowledgeable Sinsay customer service assistant.
- All responses must be in **Polish**.
- The agent may answer questions about: return windows, complaint procedures, product categories, store locations, payment methods, loyalty points, etc. Ground answers in the content of `docs/01-2026-Sinsay-PoC-Returns-Bot/sinsay-documents/`.
- The agent must NOT fabricate Sinsay policies. If it doesn't know, it should say so and direct the user to official channels.

#### Intent detection
- When the user's message indicates they want to return a product or file a complaint, the agent must call the `show_return_form` tool.
- Signals for **return** intent: "chcę zwrócić", "zwrot", "oddać produkt", "nie pasuje", "nie spełnia oczekiwań", "want to return", etc.
- Signals for **complaint** intent: "reklamacja", "wadliwy", "uszkodzony", "defekt", "zepsuty", "nie działa", "complaint", etc.
- When intent is ambiguous, the agent asks one clarifying question before calling the tool.
- The `preselectedType` parameter in the tool call must reflect the agent's best judgment from the conversation.

#### Post-form-submission verdict
After the user submits the form, the agent receives the `formData` (including the photo) and must:

1. **Identify the type:** `return` or `complaint`.
2. **For returns (`type: return`):**
   - Check if return is within 30 days (the agent cannot verify the purchase date from the photo alone — it notes this limitation and asks the user to confirm the date if not already provided in conversation).
   - Assess the photo: are original tags present? Does the item show signs of use (wear marks, stains, washing)?
   - Cross-reference: `sinsay-documents/zwrot-30-dni.md` — item must have no signs of use and must be originally tagged.
   - Verdict: **"Zwrot możliwy" / "Zwrot niemożliwy"** + Polish explanation of reasoning.
3. **For complaints (`type: complaint`):**
   - Analyze the photo: identify the visible defect type (seam slippage, fabric tear, stain, pilling, etc.).
   - Assess whether the defect appears to be a manufacturing/material defect vs. user-caused damage (e.g., scissors cut, obvious misuse).
   - Note: complaint window is 2 years from receipt date.
   - Cross-reference: `sinsay-documents/reklamacje.md` and `sinsay-documents/regulamin.md`.
   - Verdict: **"Reklamacja uzasadniona" / "Reklamacja nieuzasadniona"** + Polish explanation of reasoning.
4. The verdict message must include:
   - The verdict conclusion (bold, prominent).
   - A 2–4 sentence justification referencing the relevant policy rule.
   - Next steps the user should take (e.g., how to physically return the item).
5. After the verdict, the agent remains in the chat and can answer follow-up questions.

**System prompt structure (per intent mode):**
```
You are a helpful Sinsay customer service assistant.
Always respond in Polish.
You have access to Sinsay's return and complaint policies.
[policy document content injected here]

When the user wants to return a product or file a complaint:
- Call the show_return_form tool with the appropriate preselectedType.
- Do not ask the user to fill in information you will collect via the form.

When you receive formData in a user message:
- Analyze the photo and text fields carefully.
- Issue a verdict based solely on Sinsay policies.
- Never approve a claim that violates policy; never reject a claim without policy justification.
```

### 5.5 Persistence

- Every chat session is stored in SQLite via Spring Data JPA.
- Schema: `session_id` (UUID), `created_at`, `intent` (null until form submitted), `product_name`, `defect_description`, `photo_path` (local file path), `verdict`, `full_transcript` (JSON array of messages).
- Photos are saved to a local `/uploads` directory; only the file path is stored in the DB.
- No data is sent to any external service beyond the OpenAI API call.

---

## 6. Non-Goals (Out of Scope)

- **Authentication:** No login required. The PoC is open access.
- **Production deployment:** Running via `./mvnw spring-boot:run` + `npm run dev` is sufficient.
- **Order number validation:** The agent does not verify order numbers against a real Sinsay database.
- **Multiple photos per submission:** The form accepts one photo. Multiple photo support is a v3 consideration.
- **Email notifications:** Verdicts are displayed in-chat only.
- **Multi-language UI:** The chat responses are in Polish; UI labels may be in English.
- **Returning/re-opening previous sessions:** Each page load starts a fresh session.

---

## 7. Design Considerations

### Chat window
- Full-height, full-width layout.
- User messages: right-aligned, Sinsay brand colour fill.
- AI messages: left-aligned, white/light grey fill.
- The in-chat form card: left-aligned (appears as an AI message), distinct bordered card, wider than standard messages to accommodate all form fields comfortably.

### In-chat form card visual anatomy
```
┌─────────────────────────────────────────────┐
│  📋  Formularz Zwrotu / Reklamacji           │
│  [contextSummary from AI]                   │
│                                             │
│  Nazwa produktu *                           │
│  [________________________]                 │
│                                             │
│  Typ zgłoszenia *                           │
│  [Return ▼]  (or [Complaint ▼])             │
│                                             │
│  Opis problemu *                            │
│  [________________________]                 │
│  [________________________]                 │
│                                             │
│  Zdjęcie produktu *                         │
│  [ + Dodaj zdjęcie ]  filename.jpg ✓        │
│                                             │
│         [ Wyślij do asystenta ]             │
└─────────────────────────────────────────────┘
```

### Visual style
- Sinsay brand: monochrome, clean, minimal. Use Shadcn UI components.
- Form card: white background, 1px border, subtle shadow, 8px border-radius.
- Submit button: full-width, black fill, white text — consistent with Sinsay style.
- Verdict message: the verdict line (e.g., "Reklamacja uzasadniona ✓") should be visually prominent — larger font or green/red accent colour.

---

## 8. Technical Considerations & Constraints

### Tool call rendering in assistant-ui
- Use `assistant-ui`'s `makeAssistantToolUI` (or equivalent API) to register a custom renderer for the `show_return_form` tool call.
- The rendered component must be a controlled React form with Zod validation.
- On submit, the form calls the `useChat` append function with a user message containing the serialized `formData` (including base64 photo).

### Photo handling
- Client-side resize to max 1024px (longest edge) using Canvas API before encoding to base64.
- Base64 string is embedded in the user message JSON payload: `{ formData: { ..., photo: "data:image/jpeg;base64,..." } }`.
- The backend extracts the base64, saves the file to `/uploads/{sessionId}.jpg`, and passes a `Media` object to the Spring AI ChatClient.

### SSE and Vercel AI SDK compatibility
- The Spring Boot backend must produce SSE chunks in the exact Vercel AI SDK Data Stream Protocol format.
- Tool call chunks (`9:`) and tool result chunks (`a:`) must be emitted before text chunks for the same turn.
- The frontend's `useChat` hook must be configured with `streamProtocol: "data"`.

### assistant-ui and React 19 peer dependencies
- If `npm install` produces peer dependency warnings for `assistant-ui` or `@radix-ui` with React 19, use `--legacy-peer-deps`.

### SQLite
- Use `org.xerial:sqlite-jdbc` + `org.hibernate.orm:hibernate-community-dialects`.
- DB file: `sinsay-assistant.db` in the project root. Add to `.gitignore`.

---

## 9. Success Metrics

| Metric | Target |
|---|---|
| Intent detection accuracy | Agent correctly triggers the form (correct `preselectedType`) in ≥90% of clear return/complaint messages |
| Form usability | User completes and submits the form without error in ≤2 attempts |
| Verdict accuracy | Agent correctly identifies "obvious wear" or "manufacturing defect" in ≥85% of test images |
| Streaming latency | First token appears within 3 seconds of form submission |
| Persistence | 100% of form submissions with photos are recoverable from SQLite + `/uploads` |

---

## 10. Open Questions

1. **Tool call streaming:** Does the Vercel AI SDK's `useChat` hook correctly handle `9:` tool call chunks mid-stream for triggering form rendering? Needs validation with actual `assistant-ui` version.
2. **Form re-trigger:** If the user wants to submit a second complaint in the same session, can the agent call `show_return_form` a second time in the same thread? The component must support multiple instances in the chat history.
3. **Blurry photo handling:** If the photo is too low-quality for GPT-4o to analyse, the agent should ask for a retake. How does the user submit a new photo without re-filling the whole form? For MVP: agent asks user to send a new, clearer photo as a follow-up image attachment in the chat composer (no full form re-render required).
4. **Policy grounding accuracy:** Does injecting the full content of `zwrot-30-dni.md`, `reklamacje.md`, and `regulamin.md` into the system prompt stay within the GPT-4o context window comfortably alongside the conversation history and image?
