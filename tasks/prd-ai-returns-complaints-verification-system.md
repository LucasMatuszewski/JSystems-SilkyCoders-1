# Product Requirements Document: AI-Driven Returns & Complaints Verification System

## Introduction/Overview

This document describes a Proof of Concept (PoC) system for Sinsay, a fast-fashion retailer, designed to automate the initial triage and verification of customer returns and complaints. The system uses AI-powered image analysis to distinguish between standard 30-day returns (*Zwrot*) and statutory product complaints (*Reklamacja*) under Polish Civil Code regulations.

**Problem Statement:** Currently, customer service representatives manually review return and complaint requests, which is time-consuming and prone to inconsistencies. The system aims to reduce manual workload, minimize fraudulent returns, and provide instant feedback to customers.

**Goal:** Build a standalone PoC that demonstrates automated verification of returns and complaints using local Ollama with Gemma3 model to analyze customer-uploaded images (receipts and product defects) and enforce policy compliance.

---

## Goals

1. **Automate Initial Verification:** Automatically verify return eligibility (30-day window) and complaint validity (2-year statutory warranty) using AI-powered analysis.
2. **Reduce Manual Workload:** Demonstrate potential reduction in customer service manual review time by handling clear-cut cases automatically.
3. **Enforce Policy Compliance:** Strictly enforce Sinsay's 30-day return policy and Polish Civil Code's 2-year statutory warranty rules.
4. **Provide Instant Feedback:** Deliver real-time, streaming AI responses to users explaining approval or rejection decisions.
5. **Demonstrate Technical Feasibility:** Prove that a monorepo architecture (Spring Boot + React) can seamlessly integrate with local Ollama (Gemma3) and stream responses effectively.

---

## User Stories

### As a Customer
- **US-1:** As a customer, I want to submit a return request by uploading my receipt and confirming the item is unused, so that I can get an instant decision on my return eligibility.
- **US-2:** As a customer, I want to submit a complaint with photos of product defects, so that the system can analyze whether my complaint is valid under the 2-year warranty.
- **US-3:** As a customer, I want to see real-time streaming responses from the AI explaining why my request was approved or rejected, so that I understand the decision and can take appropriate action.
- **US-4:** As a customer, I want to resubmit my request if it was rejected, so that I can correct any issues (e.g., upload clearer images, provide additional information).

### As a System Administrator
- **US-5:** As a system administrator, I want the system to be accessible without authentication for PoC testing, so that we can quickly demonstrate the feature to stakeholders.
- **US-6:** As a system administrator, I want the system to process requests without storing data, so that we can test functionality without privacy concerns during the PoC phase.

---

## Functional Requirements

### FR-1: Intake Form - Request Type Selection
The system must present users with a form that allows them to select their request type:
- **FR-1.1:** The form must display two options: "Return" (30-day policy) and "Complaint" (2-year statutory warranty).
- **FR-1.2:** The form must dynamically show different input fields based on the selected request type.

### FR-2: Intake Form - Return Request Data Collection
For Return requests, the system must collect:
- **FR-2.1:** Order/Receipt ID (text input, required).
- **FR-2.2:** Purchase Date (date picker, required).
- **FR-2.3:** Confirmation checkbox that the item is "Unused" (required).

### FR-3: Intake Form - Complaint Request Data Collection
For Complaint requests, the system must collect:
- **FR-3.1:** Order/Receipt ID (text input, required).
- **FR-3.2:** Purchase Date (date picker, required).
- **FR-3.3:** Defect Description (text area, required, minimum 10 characters).
- **FR-3.4:** Defect Photos (file upload, required, minimum 1 image, maximum 5 images, accepted formats: JPG, PNG, WebP).

### FR-4: Form Validation
The system must validate all form inputs before submission:
- **FR-4.1:** All required fields must be validated client-side using Zod schema validation.
- **FR-4.2:** Date inputs must be validated to ensure they are valid dates and not in the future.
- **FR-4.3:** Image uploads must be validated for file type and size (max 5MB per image).
- **FR-4.4:** Images must be automatically resized client-side to a maximum of 1024px on the longest side before upload.

### FR-5: Backend Policy Validation - Returns
The system must validate return requests against the 30-day policy:
- **FR-5.1:** The system must calculate the number of days between the purchase date and the current date.
- **FR-5.2:** If the purchase date is more than 30 days ago, the system must automatically reject the return request.
- **FR-5.3:** If the purchase date is within 30 days, the system must proceed to receipt verification via AI.

### FR-6: Backend Policy Validation - Complaints
The system must validate complaint requests against the 2-year statutory warranty:
- **FR-6.1:** The system must calculate the number of days between the purchase date and the current date.
- **FR-6.2:** If the purchase date is more than 2 years ago, the system must automatically reject the complaint request.
- **FR-6.3:** If the purchase date is within 2 years, the system must proceed to defect analysis via AI.

### FR-7: Receipt Verification via AI (Returns)
For valid return requests (within 30 days), the system must verify the receipt:
- **FR-7.1:** The system must send the uploaded receipt image to GPT-4o with Vision for OCR analysis.
- **FR-7.2:** The AI must extract the order/receipt ID and purchase date from the receipt image.
- **FR-7.3:** The AI must verify that the extracted information matches the user-provided information.
- **FR-7.4:** The AI must determine if the receipt is valid and authentic (not fraudulent or altered).

### FR-8: Defect Analysis via AI (Complaints)
For valid complaint requests (within 2 years), the system must analyze product defects:
- **FR-8.1:** The system must send the uploaded defect images and description to Ollama Gemma3 for analysis.
- **FR-8.2:** The AI must classify the defect type (e.g., seam slippage, pilling, color bleeding, manufacturing defect vs. user damage).
- **FR-8.3:** The AI must determine if the defect is a valid manufacturing defect (eligible for complaint) or user-caused damage (not eligible).
- **FR-8.4:** The AI must provide a detailed explanation of its analysis.

### FR-9: AI Decision Making
The AI must make clear approval or rejection decisions:
- **FR-9.1:** The AI must output a structured JSON decision with status ("APPROVED" or "REJECTED") and a reason.
- **FR-9.2:** The AI must provide a conversational explanation of the decision in plain English.
- **FR-9.3:** If the AI cannot determine validity with confidence, it must reject the request with an explanation of what information is missing or unclear.

### FR-10: Streaming Response Delivery
The system must stream AI responses to the frontend in real-time:
- **FR-10.1:** The backend must use Server-Sent Events (SSE) to stream responses.
- **FR-10.2:** The backend must adapt Ollama's streaming format to the Vercel AI SDK Data Stream Protocol format (`0:"text"`).
- **FR-10.3:** The frontend must display streaming text as it arrives, showing the AI's analysis in real-time.
- **FR-10.4:** The system must achieve Time to First Token (TTFT) of less than 2 seconds.

### FR-11: Chat Interface for Image Upload
After initial form submission, the system must provide a chat interface:
- **FR-11.1:** The system must generate a unique `conversationId` for each valid request and return it to the frontend.
- **FR-11.2:** The chat interface must allow users to upload additional images if needed.
- **FR-11.3:** The chat interface must display the streaming AI response as it analyzes uploaded images.
- **FR-11.4:** The chat interface must support multiple image uploads within the same conversation.

### FR-12: Rejection Handling
When a request is rejected, the system must:
- **FR-12.1:** Display a clear rejection message explaining why the request was rejected.
- **FR-12.2:** Provide actionable feedback (e.g., "Receipt is not readable, please upload a clearer image").
- **FR-12.3:** Allow users to start a new request (return to the intake form) to resubmit with corrected information.

### FR-13: No Data Persistence
The system must not persist any data:
- **FR-13.1:** All requests must be processed in-memory only.
- **FR-13.2:** No database or file storage of user submissions, images, or AI responses.
- **FR-13.3:** Conversation state must be transient and only maintained during the active session.

### FR-14: Public Access (No Authentication)
The system must be accessible without authentication:
- **FR-14.1:** No login or registration required to access the system.
- **FR-14.2:** No user accounts or session management beyond transient conversation state.
- **FR-14.3:** The system must be accessible to anyone with the URL.

### FR-15: English Language Interface
The system must be presented in English:
- **FR-15.1:** All UI text, labels, buttons, and messages must be in English.
- **FR-15.2:** All AI responses and explanations must be in English.
- **FR-15.3:** Date formats must follow English conventions (MM/DD/YYYY or DD/MM/YYYY with clear labeling).

---

## Non-Goals (Out of Scope)

1. **User Authentication:** No login, registration, or user account management. The PoC is publicly accessible.
2. **Integration with Existing Systems:** No integration with Sinsay's order management system, customer database, or ticketing system. Order/receipt validation is manual (user-provided data only).
3. **Data Persistence:** No database, file storage, or logging of user submissions. All data is processed and discarded.
4. **Multi-language Support:** Only English is supported for the PoC. Polish and other languages are out of scope.
5. **Payment Processing:** No integration with payment systems or refund processing.
6. **Email Notifications:** No email notifications to users about request status.
7. **Admin Dashboard:** No administrative interface for reviewing or managing requests.
8. **Audit Logging:** No persistent logging or audit trails of system activity.
9. **Advanced Analytics:** No analytics, reporting, or metrics collection beyond basic success metrics.
10. **Mobile App:** Only web-based interface. Native mobile apps are out of scope.
11. **Human Escalation Queue:** No integration with customer service tools or escalation workflows. Rejected requests simply provide feedback for resubmission.

---

## Design Considerations

### UI/UX Requirements
- **Mobile-Responsive Design:** The intake form and chat interface must work seamlessly on mobile devices (phones and tablets).
- **Minimal Input Requirements:** The intake form should require fewer than 5 initial inputs to reduce friction.
- **Progressive Disclosure:** Show only relevant fields based on request type selection.
- **Visual Feedback:** Clear loading states, success messages, and error messages with actionable guidance.
- **Accessibility:** Follow WCAG 2.1 Level AA guidelines using Shadcn UI components (which are built with accessibility in mind).

### Component Library
- **Shadcn UI:** Use Shadcn UI components for consistent, accessible UI elements (forms, buttons, dialogs, etc.).
- **TailwindCSS:** Use TailwindCSS for styling and Sinsay-branded color schemes (if available).

### User Flow
1. User lands on homepage → sees intake form.
2. User selects "Return" or "Complaint" → form fields update dynamically.
3. User fills out form and uploads images → submits form.
4. Backend validates policy (date checks) → if invalid, immediate rejection; if valid, proceed to AI.
5. System generates `conversationId` → redirects to chat interface.
6. User sees streaming AI response analyzing their submission.
7. AI provides approval/rejection decision with explanation.
8. If rejected, user can return to form to resubmit.

---

## Technical Considerations

### Architecture
- **Monorepo Structure:** Single repository containing both frontend (React) and backend (Spring Boot) code.
- **Embedded Deployment:** React frontend is built as static assets and embedded within the Spring Boot JAR file, creating a single deployable artifact.
- **Stateless Design:** Backend is stateless to allow horizontal scaling (though scaling is not a PoC requirement).

### Technology Stack
- **Backend:** Java 21 with Spring Boot 3.5.9
- **Frontend:** React 19 with TypeScript, Vite, TailwindCSS, Shadcn UI
- **AI Integration:** Spring AI framework (`spring-ai-ollama-spring-boot-starter`) with Ollama Gemma3 model
- **Streaming:** Server-Sent Events (SSE) using Spring Boot's `SseEmitter` and Vercel AI SDK Data Stream Protocol
- **Form Validation:** `react-hook-form` with `zod` schema validation
- **Build Tool:** Maven with `frontend-maven-plugin` to build React and package into JAR

### API Endpoints
- **POST `/api/returns/submit`:** Submit intake form (returns or complaints)
  - Request body: `ReturnRequest` or `ComplaintRequest` DTO
  - Response: `{ conversationId: string, status: "VALID" | "REJECTED", message: string }`
- **POST `/api/chat`:** Upload images and receive streaming AI response
  - Request: Multipart form data with images and `conversationId`
  - Response: SSE stream with Vercel AI SDK format

### Dependencies
- **Spring AI Ollama Starter:** For Ollama Gemma3 integration
- **Spring Web:** For REST controllers and SSE support
- **Vercel AI SDK:** For React chat interface (`useChat` hook)
- **React Hook Form + Zod:** For form validation

### Security Considerations
- **Ollama Configuration:** Ollama base URL must be configured in `application.yml`. For distributed setups, ensure Ollama server is accessible and Gemma3 model is available.
- **No PII Storage:** Since this is a PoC with no persistence, no personal information is stored.
- **Input Validation:** All user inputs must be validated and sanitized to prevent injection attacks.
- **Image Processing:** Images are resized client-side and validated for type/size before upload.

### Performance Requirements
- **Time to First Token (TTFT):** Less than 2 seconds for streaming responses.
- **Image Optimization:** Client-side image resizing to max 1024px to reduce upload time and API costs.
- **Streaming:** Real-time token streaming to provide immediate feedback and improve perceived performance.

---

## Success Metrics

1. **Functional Success:**
   - System successfully distinguishes between returns and complaints.
   - 100% accuracy in rejecting returns that are more than 30 days old (date-based logic).
   - 100% accuracy in rejecting complaints that are more than 2 years old (date-based logic).
   - AI provides clear, understandable explanations for all decisions.

2. **Technical Success:**
   - Successful streaming of AI responses with TTFT < 2 seconds.
   - Single JAR file deployment containing both frontend and backend.
   - System processes requests without errors in the happy path (valid return and valid complaint flows).

3. **User Experience Success:**
   - Intake form requires fewer than 5 initial inputs.
   - Mobile-responsive design works on common mobile devices.
   - Users can understand AI rejection reasons and successfully resubmit with corrections.

4. **PoC Demonstration Success:**
   - System can be demonstrated to stakeholders showing automated verification in action.
   - Clear separation between return and complaint workflows is visible.
   - AI analysis of images (receipts and defects) produces reasonable results.

---

## Open Questions

1. **Image Quality Thresholds:** What level of image quality/blurriness should trigger a rejection with a request for retake? Should this be configurable?

2. **Receipt Format Support:** Should the system support specific receipt formats (e.g., Sinsay's standard receipt format) or be generic enough to handle any receipt format?

3. **Defect Classification Granularity:** How detailed should the defect classification be? Should we categorize specific defect types (seam slippage, pilling, etc.) or just distinguish between "manufacturing defect" vs. "user damage"?

4. **Conversation Persistence:** Since we're not using a database, how long should the `conversationId` remain valid? Should it expire after a certain time or number of messages?

5. **Error Handling for AI Failures:** What should happen if the OpenAI API is unavailable or returns an error? Should we show a generic error message or attempt retry logic?

6. **Performance Optimization:** Should we implement rate limiting or request throttling to manage Ollama resource usage during PoC testing?

---

## Target Audience

This PRD is written for **junior developers** who will implement the system. Requirements are explicit and avoid unnecessary jargon. Technical terms are explained where needed, and the document provides enough detail for developers to understand the feature's purpose, user flows, and core logic without requiring extensive domain knowledge.

---

## Appendix: Key Terms

- **Zwrot:** Polish term for "return" - refers to the 30-day contractual return policy.
- **Reklamacja:** Polish term for "complaint" - refers to the 2-year statutory warranty under Polish Civil Code.
- **Rękojmia:** Polish legal term for statutory warranty.
- **SSE:** Server-Sent Events - a web standard for streaming data from server to client.
- **TTFT:** Time to First Token - the time it takes for the first token of an AI response to arrive.
- **PoC:** Proof of Concept - a demonstration of feasibility, not a production system.
