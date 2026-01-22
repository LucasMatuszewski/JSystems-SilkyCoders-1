# API Specification

## POST `/api/returns/submit`

Submit intake form (returns or complaints).

### Request Body

```json
{
  "requestType": "RETURN" | "COMPLAINT",
  "orderReceiptId": "string",
  "purchaseDate": "YYYY-MM-DD",
  "unused": boolean,  // Only for RETURN
  "defectDescription": "string",  // Only for COMPLAINT
  "images": File[]  // Receipt for RETURN, defect photos for COMPLAINT
}
```

### Response

```json
{
  "conversationId": "uuid-string",
  "status": "VALID" | "REJECTED",
  "message": "string"
}
```

### Behavior

* Validate policy (date checks)
* If rejected: return immediately with rejection message
* If valid: generate `conversationId`, return it, proceed to chat interface

## POST `/api/chat`

Upload images and receive streaming AI response.

### Request

Multipart form data:
* `conversationId`: string (required)
* `images`: File[] (optional, for additional uploads)
* `message`: string (optional, for text messages)

### Response

SSE stream with Vercel AI SDK Data Stream Protocol format:
* Text chunks: `0:"chunk text"`
* Must stream in real-time with TTFT < 2 seconds
