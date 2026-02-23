# Product Requirements Document: Sinsay AI Verification PoC

**Version:** 1.0

**Status:** Accepted

**Date:** 2026-01-22

**Target Audience:** AI coding assistants

## 1. Introduction & Overview

This Proof of Concept (PoC) aims to validate the technical feasibility of using Multimodal AI (GPT-4o Vision) to automate the verification of product returns (*Zwrot*) and complaints (*Reklamacja*) for the Sinsay brand.

The system will guide users through a structured intake form and transition into an interactive AI chat session. The AI will analyze uploaded photos against specific business rules (e.g., return windows, visible wear, manufacturing defects) and provide an immediate, justified verdict. The goal is to determine if AI can accurately distinguish between valid and invalid claims before human intervention.

## 2. Goals

1. **Validate AI Vision Capabilities:** Determine if GPT-4o can reliably identify fabric defects (stains, pilling, tears) vs. mechanical damage.
2. **Test "Form-to-Chat" UX:** Evaluate a user flow that starts with structured data collection and evolves into a conversational explanation.
3. **Data Persistence:** Log all interaction data (photos, chat logs, verdicts) locally for post-analysis without complex cloud infrastructure.
4. **Architecture Verification:** Prove the viability of a Spring Boot + React Monorepo using Server-Sent Events (SSE) adapted for the Vercel AI SDK.

## 3. User Stories

1. **As a Customer**, I want to select whether I am making a "Return" or a "Complaint" so the system applies the correct rules (30 days vs. 2 years).
2. **As a Customer**, I want to upload a photo of my product and receipt so the verification happens instantly without waiting for an email.
3. **As a Customer**, I want to see a clear explanation of why my return was rejected (e.g., "The item appears worn") in Polish, even if the interface is in English.
4. **As a Developer**, I want all chat sessions and verdicts saved to a local SQLite database so I can manually review the AI's performance later.

## 4. Functional Requirements

### 4.1 Backend (Spring Boot 3.5.9 + Java 21)

1. **API Endpoint:** The system must expose a `POST /api/chat` endpoint.
2. **Streaming Protocol:** The endpoint must stream responses using **Server-Sent Events (SSE)**.
* **Crucial:** The stream format must strictly adhere to the **Vercel AI SDK Data Stream Protocol** (text chunks prefixed with `0:`, data chunks with `8:`).


3. **AI Orchestration:** Use **Spring AI (`spring-ai-starter-model-openai`)**.
* Construct prompts dynamically based on the "Intent" (Return vs. Complaint).
* Pass image data (Base64 or URL) to the `ChatClient` using `new Media(...)`.


4. **Persistence:** Use **Spring Data JPA** with **SQLite**.
* Store `RequestID`, `Intent`, `UserInputs`, `Verdict`, and `ChatTranscript`.
* Images can be stored as BLOBs or local file paths referenced in the DB.



### 4.2 Frontend (React 19 + TypeScript)

1. **Intake Form:**
* Fields: Order Number (String), Purchase Date (Date Picker), Type (Radio: Return/Complaint), Description (Text Area), Photo Upload (File Input).
* Validation: Use **Zod**. Reject dates older than 30 days for Returns immediately on the client side.


2. **Chat Interface:**
* Use **`assistant-ui`** library components (`<Thread />`, `<Composer />`).
* Use the **Vercel AI SDK** `useChat` hook to manage the connection to the Spring Boot backend.


3. **State Management:**
* Upon form submission, pass the form data as the "System Context" or initial "User Message" to the chat view.
* The Chat UI must replace the Form UI seamlessly (conditional rendering).



### 4.3 AI Logic (The "Agent")

1. **Language Strategy:** The System Prompt must instruct the AI to "Think in English (for logic) but Reply to the User in Polish."
2. **Policy Logic:**
* **Return:** Verify tags are present; verify no signs of usage (wrinkles, stains).
* **Complaint:** Identify defect type (e.g., seam slippage); assess if it looks like intentional damage (scissors) vs. failure.



## 5. Non-Goals (Out of Scope)

* **Production Deployment:** No Docker/K8s setup required. Running via `mvn spring-boot:run` and `npm run dev` is sufficient.
* **External Integration:** No connection to real Sinsay ERP/Order systems (Order IDs are not validated against a real database).
* **User Accounts:** No login/authentication required.
* **Email Notifications:** The verdict is displayed in-chat only.

## 6. Design Considerations

* **UI Language:** All labels and buttons in **English** (e.g., "Submit", "Upload Photo").
* **AI Persona:** Professional, helpful, compliant with Polish consumer law (*Ustawa o prawach konsumenta*).
* **Visual Style:** Clean, monochrome (Sinsay style). Use **Shadcn UI** components.
* **Layout:**
* **Step 1:** Centered Card with Form.
* **Step 2:** Full-height Chat Window with "Verdict" summary at the top.



## 7. Technical Considerations & Constraints

* **SQLite Dialect:** Spring Boot does not have a native SQLite dialect out-of-the-box in older versions. Ensure `org.xerial:sqlite-jdbc` and `org.hibernate.orm:hibernate-community-dialects` are configured correctly.
* **Image Optimization:** Resize images on the Frontend (Canvas API) to max 1024px before sending to Backend to save bandwidth/tokens.
* **Assistant-UI & React 19:** If peer dependency warnings occur for `assistant-ui` or `radix-ui` with React 19, use `npm install --legacy-peer-deps`.

## 8. Success Metrics

* **Accuracy:** The AI correctly identifies "Obvious Wear" in >90% of test images.
* **Latency:** The chat response begins streaming within 3 seconds of photo upload.
* **Reliability:** The form data and final chat logs are successfully identifiable in the SQLite `verification_logs.db` file.

## 9. Open Questions (To be resolved during Dev)

* *Does GPT-4o Vision require higher resolution for fabric texture analysis (e.g., pilling)?* (We will start with 1024px and adjust if detection fails).
* *How to handle "Retake Photo" requests?* (If AI says "Image blurry", can we re-trigger the upload input in `assistant-ui`? For MVP, we may just ask user to reload).
