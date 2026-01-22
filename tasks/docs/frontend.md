# Frontend Guidelines

## Compatibility

* **Shadcn UI:** Always use `--legacy-peer-deps` flag when installing components to resolve React 19 peer dependency conflicts

## Form Validation

* **Library:** Use **Zod** schemas for all form validation
* **Return Form Fields:**
  * Order/Receipt ID (required)
  * Purchase Date (required, not future)
  * Unused checkbox (required)
* **Complaint Form Fields:**
  * Order/Receipt ID (required)
  * Purchase Date (required, not future)
  * Defect Description (required, min 10 chars)
  * Defect Photos (required, 1-5 images, JPG/PNG/WebP, max 5MB each)

## Image Handling

* **Resize:** All images must be resized to max **1024px** on longest side *before* upload (reduces token usage and API costs)
* **Validation:** Validate file type and size client-side before submission

## Intake Form

* Dynamic form fields based on request type selection ("Return" or "Complaint")
* Progressive disclosure: show only relevant fields
* Mobile-responsive design required
* Fewer than 5 initial inputs to reduce friction

## Chat Interface

* Use Vercel AI SDK `useChat` hook for streaming responses
* Display streaming text as it arrives in real-time
* Allow multiple image uploads within same conversation
* Show clear loading states and error messages

## Rejection Handling

* Display clear rejection messages with actionable feedback
* Provide "Start New Request" button to return to intake form for resubmission
