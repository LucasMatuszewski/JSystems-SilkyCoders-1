# Project Implementation Plan: AI-Driven Returns & Complaints Verification System

## 1. Executive Summary

### Project Overview and Objectives
This project involves building a Proof of Concept (PoC) for Sinsay, a fast-fashion retailer, to automate the triage and verification of customer returns and complaints. The objective is to deploy an AI-driven system that can intelligently distinguish between a standard **30-day return (*Zwrot*)** and a **statutory product complaint (*Reklamacja*)**. 

The system will leverage **local Ollama with Gemma3 model** to analyze customer-uploaded images (receipts and product defects) to enforce policy compliance. By automating this initial verification, Sinsay aims to reduce manual customer service workload, minimize fraudulent returns, and provide instant resolution feedback to customers.

### Key Technology Decisions Summary
* **Architecture:** Monorepo with Embedded Deployment (Spring Boot serving React static assets).
* **AI Integration:** **Spring AI** framework using **Ollama Gemma3** for multimodal analysis (text + vision).
* **Backend:** **Java 21** with **Spring Boot 3.5.9**.
* **Frontend:** **React 19** with **TypeScript**, **TailwindCSS**, and **Shadcn UI**.
* **Streaming:** **Server-Sent Events (SSE)** utilizing the Vercel AI SDK Data Stream Protocol.

### Critical Success Factors
1.  **Accurate Defect Classification:** The AI must differentiate between manufacturing defects (valid complaint) and user wear-and-tear (invalid).
2.  **Regulatory Compliance:** Strict adherence to the 2-year statutory warranty vs. 30-day contractual return policy.
3.  **Seamless Integration:** Flawless streaming of AI responses from the Java backend to the React frontend.
4.  **User Experience:** A friction-free, mobile-responsive intake form that requires fewer than 5 initial inputs.

---

## 2. Project Requirements Analysis

### Functional Requirements
* **Intake Triage:** Users must select their intent: "Return" (*Zwrot*) or "Complaint" (*Reklamacja*).
* **Dynamic Data Collection:**
    * **Returns:** Order/Receipt ID, Purchase Date, Confirmation of "Unused" condition.
    * **Complaints:** Order/Receipt ID, Purchase Date, Defect Description, Defect Photos.
* **Automated Verification:**
    * *Returns:* Verify date $\le$ 30 days. Verify receipt validity via OCR.
    * *Complaints:* Verify date $\le$ 2 years. Analyze images for defect type (e.g., seam slippage, pilling, color bleeding).
* **Policy Enforcement:** Auto-reject returns > 30 days. Escalate or approve complaints based on visual evidence.

### AI/ML Requirements
* **Capabilities:** Multimodal LLM (Text + Vision) for OCR and forensic image analysis.
* **Accuracy:** High recall for detecting defects; conservative approach to rejection (escalate ambiguous cases).
* **Data:** No training data needed for PoC (Zero/Few-shot prompting).

### Non-Functional Requirements
* **Performance:** Streaming responses (Time to First Token < 2s) to mitigate model latency.
* **Scalability:** Stateless architecture allowing horizontal scaling.
* **Security:** API keys secured on backend; no PII storage in PoC.

### Compliance and Regulatory Requirements
* **Polish Civil Code:** Adherence to statutory warranty (*rękojmia*) rules.
* **Sinsay Policy:** Enforcement of the 30-day contractual return window.

---

## 3. Architecture Overview

### High-Level System Architecture
The system utilizes a **Monolithic Repository (Monorepo)** structure. The React frontend is built as a static resource and embedded within the Spring Boot backend JAR, creating a single deployable artifact.

### Component Breakdown
1.  **Frontend (React 19):**
    * **Intake Form:** Managed by `react-hook-form` and `zod` for validation.
    * **Chat Interface:** Uses Vercel AI SDK (`useChat`) to render streaming responses.
    * **Styling:** Shadcn UI + TailwindCSS.
2.  **Backend (Spring Boot 3.5):**
    * **Controllers:** `ReturnController` (Form API) and `ChatController` (Streaming API).
    * **Services:** `ReturnPolicyService` (Rules engine) and `AiService` (AI interaction).
    * **Data Model:** DTOs for `ReturnRequest` and `ChatRequest`.
3.  **AI Layer (Spring AI):**
    * **Client:** `ChatModel` abstraction wrapping the Ollama API.
    * **Model:** Gemma3 for processing text and image payloads.

### Data Flow Diagram (Textual)
1.  **Ingest:** User submits Intake Form $\rightarrow$ React Validates $\rightarrow$ POST to Spring Boot.
2.  **Triage:** Spring Boot checks dates/policy. If valid/complaint $\rightarrow$ returns `conversationId`.
3.  **Interaction:** User uploads image in Chat $\rightarrow$ POST to Spring Boot (Multipart/JSON).
4.  **Analysis:** Spring Boot constructs prompt with **System Rules** + **User Image** $\rightarrow$ Calls Ollama.
5.  **Streaming:** Ollama streams tokens $\rightarrow$ Spring Boot adapts to Vercel Protocol (`0:"text"`) $\rightarrow$ React renders via SSE.

---

## 4. Technology Stack Decisions

### 4.1 AI/ML Framework
* **Recommended:** **Spring AI** (`spring-ai-ollama-spring-boot-starter`)
* **Alternatives Considered:** Direct Ollama HTTP client, LangChain4j.
* **Justification:**
    * **Ecosystem Fit:** Spring AI provides native Spring Boot auto-configuration, reducing boilerplate.
    * **Abstraction:** The `ChatModel` interface allows future model swapping without code changes.
    * **Capabilities:** Full support for Multimodal (Vision) inputs and Reactive streaming (Flux), which are core requirements.

### 4.2 Programming Language
* **Recommended:** **Java 21**
* **Alternatives Considered:** Kotlin, Node.js.
* **Justification:**
    * **Concurrency:** Java 21's Virtual Threads (Project Loom) efficiently handle I/O-bound AI operations without complex reactive code.
    * **Stability:** Enterprise standard, type safety, and massive ecosystem support.

### 4.3 Backend Framework
* **Recommended:** **Spring Boot 3.5.9**
* **Alternatives Considered:** Quarkus, Micronaut.
* **Justification:**
    * **Synergy:** Direct support for Spring AI.
    * **Speed:** Spring Initializr and "Starters" allow rapid PoC setup.
    * **Deployment:** Easy bundling of frontend assets into a robust Fat JAR.

### 4.4 Database
* **Recommended:** **None (In-Memory)** for PoC.
* **Justification:** The PoC focuses on logic and AI interaction. Conversation state can be transient or passed via client context. Adding a database adds unnecessary complexity for this phase.

### 4.5 Frontend Framework
* **Recommended:** **React 19**
* **Alternatives Considered:** Vue, Angular.
* **Justification:**
    * **Modern Features:** React 19's Actions and `useActionState` simplify form handling.
    * **AI Tooling:** The **Vercel AI SDK** is optimized for React, making the implementation of streaming chat interfaces trivial.
    * **UI Library:** Shadcn UI (React-based) dramatically speeds up UI development with accessible, pre-styled components.

### 4.6 Streaming Protocol
* **Recommended:** **Server-Sent Events (SSE)**
* **Alternatives Considered:** WebSockets.
* **Justification:**
    * **Simplicity:** Unidirectional (Server $\rightarrow$ Client) streaming is perfect for AI responses.
    * **Compatibility:** Works over standard HTTP, friendly to enterprise firewalls (unlike WebSockets).
    * **Native Support:** Built-in to Spring Boot (`SseEmitter`) and Vercel AI SDK.

---

## 5. AI/ML Pipeline Design

### Prompt Engineering Strategy
Since we utilize a pre-trained model, the "pipeline" is the **System Prompt**.
* **Role:** "Senior Sinsay QA Specialist".
* **Context:** Inject policy rules (30 days vs 2 years).
* **Taxonomy:** Define defect types (e.g., *Slub*, *Barre*, *Seam Slippage*) vs user damage (e.g., *Scissors cut*, *Bleach spot*).
* **Output:** Structured JSON decision (`{"status": "APPROVED", "reason": "..."}`) + Conversational explanation.

### Data Strategy
* **Input:** User-uploaded images resized client-side to max 1024px (cost/latency optimization).
* **Validation:** Few-shot prompting with examples of valid/invalid defects to ground the model.

---

## 6. Development Approach

### Methodology: AI-Assisted "Vibe Coding"
* **Agent Guidance:** Use `AGENTS.md` files to provide context and architectural constraints to AI coding assistants (Cursor/Copilot).
* **Structure:** Monorepo ensures the AI agent sees the full context (Frontend DTOs matching Backend DTOs).

### Testing Strategy
* **Backend:** JUnit 5 for logic; `MockChatModel` to simulate Ollama responses (for testing).
* **Frontend:** Vitest for form logic and validation state.
* **Integration:** Manual end-to-end testing of the "Happy Path" (Return & Complaint flows).

### CI/CD
* **Build:** Maven Wrapper (`mvnw`) triggers `frontend-maven-plugin` $\rightarrow$ Builds React $\rightarrow$ Copies to `static/` $\rightarrow$ Packages JAR.

---

## 7. Implementation Phases

### Phase 1: Foundation (Days 1-2)
* **Objective:** Project scaffolding.
* **Deliverables:** Monorepo setup, Spring Boot init, React+Vite init, `AGENTS.md` creation.

### Phase 2: Core Features (Days 3-5)
* **Objective:** Functional intake.
* **Deliverables:** React Intake Form (Zod validation), Spring Boot Form Controller, basic SSE endpoint.

### Phase 3: Intelligence (Days 6-8)
* **Objective:** AI integration.
* **Deliverables:** Spring AI ChatClient setup, Vision (Media) integration, System Prompt refinement, Streaming protocol adapter.

### Phase 4: Polish (Days 9-10)
* **Objective:** UX and Testing.
* **Deliverables:** Tailwind styling (Sinsay branding), Error handling, Unit tests, Final JAR build.

---

## 8. Risk Assessment

| Risk | Mitigation |
| :--- | :--- |
| **Hallucinations** | Robust System Prompts with "Chain of Thought"; Human escalation path. |
| **Latency** | SSE Streaming to show progress; Optimistic UI states. |
| **Cost** | Client-side image resizing; Use "Low Detail" mode for receipt OCR. |
| **Image Quality** | Prompt AI to reject blurry images and request retakes. |

---

## 9. Success Metrics

* **System:** Successful streaming response (TTFT < 2s).
* **Accuracy:** 100% rejection of returns > 30 days (logic); Qualitative accuracy on defect images.
* **Delivery:** Single JAR file containing full stack app.

---

## 10. Next Steps

1.  **Environment:** Verify Java 21, Node 20+, and Ollama installation with Gemma3 model.
2.  **Scaffold:** Run Spring Initializr and Vite create commands.
3.  **Config:** Place `AGENTS.md` in root. Add `spring-ai-ollama-spring-boot-starter` dependency.
4.  **Execute:** Begin Phase 1 (Foundation).