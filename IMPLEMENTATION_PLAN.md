# Implementation Plan - Sinsay AI Verification PoC

**Goal:** Build a Proof of Concept for Sinsay Returns/Complaints using Spring Boot (Backend) and React (Frontend) with GPT-4o Vision.

## Phase 1: AI Logic & Vision (Backend) - **HIGHEST PRIORITY**
The current backend is "blind" and "generic". It needs to see the photos and know the business rules.
- [x] **System Prompt Engineering** (`VerificationService.java`):
    - [x] Implement `getSystemPrompt(intent)` to return specific instructions based on "RETURN" vs "COMPLAINT".
    - [x] Include "Think in English, Reply in Polish" instruction.
    - [x] Include specific criteria (Returns: <30 days, no wear; Complaints: <2 years, defect analysis).
- [x] **Vision Support** (`VerificationService.java`):
    - [x] Update `convertUserMessage` to parse incoming JSON for image data (base64/url).
    - [x] Construct Spring AI `UserMessage` with `Media` attachments.

## Phase 2: Persistence (Backend)
We need to save the history of what happened.
- [ ] **Chat Transcript Storage**:
    - [ ] Inject `ChatMessageRepository` into `VerificationService`.
    - [ ] Save the *User's* message to DB upon receipt.
    - [ ] Save the *AI's* full response to DB after streaming completes (or incrementally). *Note: Capturing the full stream content for saving might require a custom `doOnNext` or `reduce` in the Flux.*

## Phase 3: Frontend Polish & Optimization
- [ ] **Image Resizing** (`IntakeForm.tsx`):
    - [ ] Implement client-side canvas resizing to max 1024px (width or height) before setting state. This is a critical PRD requirement to save tokens/bandwidth.
- [ ] **UX Polish**:
    - [ ] Ensure loading states are clear.
    - [ ] Handle error cases (e.g., backend offline).

## Phase 4: Verification & Testing
- [ ] **Manual End-to-End Test**:
    1. Start Backend (`./mvnw spring-boot:run`).
    2. Start Frontend (`cd frontend && npm run dev`).
    3. Fill form (Intent: Return).
    4. Upload "damaged" photo.
    5. Verify AI rejects it in Polish.
    6. Check SQLite DB for records.

## Done / Foundation (Completed)
- [x] **Project Structure**: Monorepo setup (Spring Boot + Vite).
- [x] **Dependencies**: Spring AI, SQLite, React 19, Tailwind, Vercel AI SDK.
- [x] **Basic API**: `POST /api/chat` endpoint with SSE streaming.
- [x] **Frontend UI**: Intake Form and Chat Interface components.
- [x] **Database Schema**: `VerificationSession` and `ChatMessage` entities created.
