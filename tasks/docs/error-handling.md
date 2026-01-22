# Error Handling

## Policy Validation Errors

* Return immediate rejection with clear message
* Example: "Return request rejected: Purchase date is more than 30 days ago"

## AI API Failures

* Show generic error message to user
* Do NOT expose Ollama service errors directly

## Image Quality Issues

* AI should reject blurry/unreadable images with request for retake

## Form Validation Errors

* Display inline validation errors using Zod schema messages
