# Executive Summary

Building an AI-driven returns & complaints verification system for
Sinsay requires a tightly integrated stack: a Java Spring Boot 3.5
backend (Java 21) and a React 19 frontend (with TailwindCSS and Shadcn
UI) managed in a single monorepo. The solution will leverage GPT-4
Vision (GPT-4 "OpenAI Vision", a multimodal model) via OpenAI's API to
analyze customer-uploaded images for product defects. Key
recommendations include:

- **Monorepo & Deployment:** Maintain one repository housing both the
  Spring Boot API and React SPA for easier
  coordination[\[1\]](https://graphite.com/guides/monorepo-frontend-backend-best-practices#:~:text=Here%27s%20a%20simple%20directory%20layout,both%20frontend%20and%20backend%20apps).
  For the Proof-of-Concept, bundle the React app into Spring Boot's
  static resources to produce a single deployable JAR, simplifying
  dev/test
  operations[\[2\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Learn%20how%20to%20bundle%20your,simplifying%20both%20dev%20and%20ops)[\[3\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Image%3A%20diagram%20showing%20how%20the,JAR%20after%20running%20mvn%20package).
  This monolithic approach is convenient for a PoC, while still allowing
  future separation if needed (e.g. separate static hosting for the SPA)
  without code changes.

- **Intake Form Design:** Present a concise initial form (5 fields or
  fewer) to triage the request as either a standard *30-day return
  (zwrot)* or a *defective product complaint (reklamacja)*. Essential
  fields include **Purchase Channel** (Online vs In-Store),
  **Order/Receipt ID**, **Purchase Date**, **Return vs Complaint**
  selection, and optionally **Contact Info**
  (email/phone)[\[4\]](https://everstox.com/glossary/returns-form/#:~:text=A%20returns%20form%20is%20a,data%20to%20ensure%20proper%20processing).
  Conditional logic should tailor the form: e.g. if "Online", ask for
  Order ID; if "In-store", ask for receipt details. Validation rules
  enforce Sinsay's policies -- e.g. allow "return" only if purchase date
  is within 30
  days[\[5\]](https://android.com.pl/tech/549377-sinsay-zwrot-poradnik/#:~:text=W%20Sinsay%20zwrot%20jest%20mo%C5%BCliwy,przez%2030%20dni%20od%20zakup%C3%B3w)
  and item is unused with tags (the form can include a checkbox
  confirmation)[\[6\]](https://furgonetka.pl/zwroty/55061c54-a0ed-481b-bfff-70db8924bd4e#:~:text=Furgonetka,Zwrot%20zapakuj%20w%20jedn%C4%85%20paczk%C4%99).
  This ensures the AI agent gets correct context up front.

- **Workflow Overview:** Once the form is submitted, its structured data
  is passed to the AI chatbot as context. The GPT-4 Vision agent uses
  that info plus any customer-provided images to determine if the case
  is a regular return or a valid defect claim. The chatbot engages the
  user in natural dialogue ("Vibe Coding" style) to gather missing
  details and enforce return policy rules. Images of the item are
  analyzed by GPT-4 to distinguish manufacturing defects vs.
  wear-and-tear. The Spring Boot backend orchestrates this flow:
  validating form input, handling image uploads, calling OpenAI's API
  with the form data and images, and returning the AI's analysis and
  next questions. The React frontend provides a seamless multi-step UX
  -- initial form → image upload → chat -- using modern component
  libraries and form handling techniques for developer efficiency.

- **Tech Stack & Tools:** Use Spring Boot's strengths for quick API
  development (Spring Web for REST, Lombok to reduce boilerplate) and
  integrate OpenAI via the official SDK or Spring's new **Spring AI**
  project for
  OpenAI[\[7\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=Gradle).
  In the React app, utilize **React Hook Form** with Zod for schema
  validation to implement the form (Shadcn UI's components are designed
  to work with React Hook Form for accessible, styled
  forms[\[8\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=Validation)).
  Incorporate the **Vercel AI SDK** or an open-source chat UI library
  like **assistant-ui** for the chat interface, enabling token-streaming
  responses and easy UI
  customization[\[9\]](https://github.com/assistant-ui/assistant-ui#:~:text=pixel%20,easy%20extension%20to%20custom%20APIs).
  Testing is done with JUnit 5 (backend) and Vitest/React Testing
  Library (frontend) to cover edge cases (e.g. invalid form inputs,
  unusual defect images). We recommend IntelliJ IDEA for development,
  with the Lombok plugin and Spring Boot devtools enabled for hot
  reload.

- **AI Prompt & Policy Logic:** Encode Sinsay's return policies and
  common defect types into the AI agent's instructions. For a *return*,
  the agent simply confirms eligibility (within 30 days, tags intact)
  and provides return instructions. For a *complaint*, the agent asks
  for and analyzes images (e.g. checking for tears, stains,
  discoloration, broken zippers,
  etc.)[\[10\]](https://www.v-trust.com/en/blog/most-common-defects-in-garments#:~:text=2)[\[11\]](https://www.v-trust.com/en/blog/most-common-defects-in-garments#:~:text=8).
  It then compares findings against policy: genuine manufacturing
  defects (seams coming apart, misprints, etc.) are approved for free
  repair/replacement[\[12\]](https://zwroty.globkurier.pl/pl/sinsay-zwrot-towaru-kompletny-przewodnik/#:~:text=Je%C5%9Bli%20jednak%20oka%C5%BCe%20si%C4%99%2C%20%C5%BCe,commerce.%20Znajomo%C5%9B%C4%87%20tych%20zasad),
  whereas issues from misuse or wear (heavy stains, obvious
  wear-and-tear) may be politely declined per policy. Prompt engineering
  ensures the GPT-4 model's output is **structured** when needed (the
  backend can request a JSON decision), and always grounded in the
  provided policy text to avoid inconsistency. Error handling and
  fallbacks are in place -- e.g. if the AI fails to classify the defect,
  it can request more info or escalate to human review, and the backend
  will catch and log exceptions from OpenAI API calls (with retries on
  transient
  errors[\[13\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=Property%20Description%20Default)).

In summary, this PoC will demonstrate an end-to-end AI-driven returns
process: customers enter minimal info, the AI agent (with vision
capabilities) asks for evidence, evaluates compliance with Sinsay's
rules, and provides a resolution -- all through an intuitive web
interface. Below, we detail each aspect of the design and
implementation, including form specifications, architectural decisions,
development best practices, project structure, code examples, and CLI
instructions for setting everything up.

------------------------------------------------------------------------

## Intake Form Specification (Initial Contact Form)

**Goal:** Collect just enough structured information to route the
request correctly (standard return vs. defect complaint) and initialize
the AI agent's context, without burdening the customer. Based on
Sinsay's policies, we recommend the following fields:

- **1. Purchase Channel** -- *Online purchase* or *In-store purchase*.
  (Select one, required)\
  *Justification:* Return procedures differ by channel (online returns
  need an order ID and are mailed at customer's cost, while store
  purchases use a receipt and can be returned in-store for
  free[\[14\]](https://android.com.pl/tech/549377-sinsay-zwrot-poradnik/#:~:text=W%20stacjonarnym%20sklepie%20Sinsay%20zwrot,jest%20bezp%C5%82atny)).
  This field triggers conditional logic:

- If "Online": show **Order Number** field (and possibly auto-fill
  customer info if logged in).

- If "In-Store": show **Store Location** and/or **Receipt Number**
  field.

- **2. Order Number / Receipt Number** -- *(Conditional, required)*\
  *For online purchases:* an alphanumeric Order ID from the confirmation
  email.\
  *For in-store:* the receipt or transaction ID (as printed on the
  receipt). Possibly also ask for store branch name if needed for
  context.\
  *Justification:* Identifies the purchase and allows verification of
  purchase date and item. Even if the PoC doesn't integrate with backend
  systems, collecting this lends realism and can be used by the AI to
  personalize responses ("I see your order 12345 was on Jan 5th..."). It
  also deters fraudulent claims. Validate format (e.g., order numbers
  might have a specific pattern) and **require** this field if its
  parent field is applicable.

- **3. Purchase Date** -- *Date selector, required.*\
  *Justification:* Sinsay allows returns within 30 days of
  purchase[\[5\]](https://android.com.pl/tech/549377-sinsay-zwrot-poradnik/#:~:text=W%20Sinsay%20zwrot%20jest%20mo%C5%BCliwy,przez%2030%20dni%20od%20zakup%C3%B3w).
  This date helps determine eligibility for a "zwrot regulaminowy". The
  backend or front-end can auto-validate: if today \> 30 days from this
  date and the user selected "Return", we should either prevent
  submission or convert the case to a "Complaint" (with a message like
  "Purchase is older than 30 days, so this will be handled as a warranty
  claim"). This field is also used by the AI agent to enforce the policy
  (the agent can double-check the interval). Ensure the date is not in
  the future and not too far in the past (for complaints, possibly allow
  up to 2 years due to implied warranty laws).

- **4. Request Type** -- *"Return (30-day)" vs "Complaint (Defective
  Product)"*, required.\
  *Justification:* Explicitly asking the customer if the item is simply
  unwanted vs. defective guides the next steps. Use radio buttons or a
  dropdown:

- **"Zwrot 30-dniowy"** -- Standard return of an intact product within
  30 days.

- **"Reklamacja wadliwego produktu"** -- Complaint due to a product
  fault.\
  This selection can toggle additional fields: if "Complaint", prompt
  the user to briefly **describe the defect** (optional text area) and
  later upload photos. If "Return", no defect description is needed --
  the agent will likely just confirm the item is unworn with tags (and
  the image upload step might be skipped or used only for verification
  in edge cases).

- **5. Customer Contact Info** -- *Email and/or Phone*, **optional (but
  recommended)**.\
  *Justification:* Not strictly required to determine the process path,
  but essential for follow-up. If the user is logged in, this can be
  pre-filled or omitted. Otherwise, having an email allows sending
  return labels or confirmation updates. We can mark this optional in
  the PoC (to focus on core logic), or include it to demonstrate
  end-to-end flow. Validate format (use a regex for email, etc.).

**Field Validation & Constraints:** Use client-side and server-side
validation: - **Order/Receipt ID** -- not empty when required; matches
expected format (e.g., 8-12 digit number or mix of letters). If online
order, ensure it starts with e.g. "SN" if Sinsay's orders do (for
realism). If in-store receipt, could be numeric. These checks reduce
garbage input. - **Purchase Date** -- must be a valid past date. If
`Request Type = Return`, enforce (via form validation or on submit) that
`today - purchaseDate ≤ 30 days`. If this fails, show an error like
"Return period (30 days) exceeded -- please select \'Complaint\' for
defects" and/or automatically switch the type to Complaint with a
notice. For `Request Type = Complaint`, allow any date (but maybe inform
if \> 2 years might not be accepted as per warranty policy). -
**Description of Defect** -- if shown (Complaint selected), keep it
short (e.g. max 250 chars) and optional, since the chatbot will ask
details. Its purpose is to give the AI a starting point (e.g. "zipper
broke off" or "shoe sole detached"). We might feed this as part of the
prompt to GPT-4. - **Unused Condition Confirmation** -- For returns,
consider a checkbox like "I confirm the item is unworn, with original
tags attached." According to Sinsay policy, returned goods must have
tags and no signs of
use[\[6\]](https://furgonetka.pl/zwroty/55061c54-a0ed-481b-bfff-70db8924bd4e#:~:text=Furgonetka,Zwrot%20zapakuj%20w%20jedn%C4%85%20paczk%C4%99).
This isn't a required field to route logic, but checking it can be
mandatory to submit a return request. (For complaints, this isn't needed
-- a defect can be claimed even if the item was used normally.)

**Form Flow Logic:** The form should dynamically adapt: - First, the
user chooses *Purchase Channel*. The form then displays either "Order
Number" or "Store Receipt #" accordingly. - Next, *Request Type* is
chosen. If "Return", we show minimal fields (no defect description, no
image upload at this stage). If "Complaint", we reveal a "Defect
Description" text area (optional) and, after form submission, the UI
will prompt for an image upload step. - On submission, perform
validation. If any required info is missing or invalid, highlight those
fields with error messages. Notably, enforce the Purchase Date vs.
Return logic: if a user selects "Return" but the date is beyond 30 days,
the form should either: - Prevent submission and prompt them to switch
to "Complaint", **or** - Auto-switch the request to Complaint under the
hood. (The first approach is more user-friendly and transparent.) -
After a successful submit, the form data is sent to the backend (e.g.
via a POST to `/api/intake`). The backend also validates (using Bean
Validation and custom checks) to catch any bypass of client rules. On
success, the backend responds (perhaps with an acknowledgment and a
conversation ID or token for the upcoming chat).

**Form Data Example (JSON):** The submitted data can be structured as
follows, which the backend API expects (as a DTO schema):

    {
      "purchaseChannel": "ONLINE",              // or "IN_STORE"
      "orderId": "SN123456789",                 // present if purchaseChannel is ONLINE
      "storeReceipt": null,                     // present if purchaseChannel is IN_STORE (could contain storeId and receiptNo)
      "purchaseDate": "2025-12-01",
      "requestType": "COMPLAINT",               // "RETURN" or "COMPLAINT"
      "defectDescription": "Zipper fell off after one use",  // only for complaints
      "customerEmail": "john.doe@example.com"
    }

*(Note: For an in-store return,* `orderId` *would be null and we might
have* `"storeId": 112, "receiptNumber": "0377/305/2025"` *in a*
`storeReceipt` *object. For simplicity, a single* `orderOrReceiptId`
*field could also be used with a label that changes based on channel.)*

This JSON is used to initialize the AI conversation context. The Spring
Boot backend will map it to a Java DTO (e.g. `ReturnRequest` class) and
validate it. It will also determine the next step: - If
`requestType=RETURN` and validation passes, no image is needed. The
backend might immediately invoke the GPT agent with the info to get a
response (or simply respond with a success message and a standard return
instruction from policy). The chat UI can still be shown for any Q&A,
but likely the AI will mostly confirm details and close the case. - If
`requestType=COMPLAINT`, the backend responds indicating that an image
upload is required or that the chat (with image capability) can begin.
The frontend will then prompt the user to upload photos of the item's
defect.

**Form Flow Diagram:** (textual description)\
1. *User opens Return/Complaint form* -- selects **Purchase Channel**
(Online or In-Store).\
2. *Form displays appropriate ID field* -- user enters Order \# or
Receipt #.\
3. *User selects* *Request Type* *-- Return vs Complaint. If Complaint,
a "Describe the defect" field appears (and later they'll upload a
photo).*\
*4. User fills Purchase Date* -- date picker ensures format.\
5. *User (optionally) enters Contact info* -- email/phone for
follow-up.\
6. *User submits form*. Client-side checks run (e.g. within 30 days for
returns).\
- If errors, show messages (e.g. "Order number is required", "Return
period exceeded -- choose Complaint for defects").\
- If valid, disable form and send data to backend.\
7. *Backend processes input:* validates again, then returns a response
(e.g. JSON `{ "conversationId": "abcd1234", "next": "upload_image" }`).\
8. *Frontend moves to next step:* - If `next` is `"upload_image"`, show
an image upload interface (for complaints). - If the form was a return,
possibly jump directly to chat agent or result. In our PoC, we'll still
initiate the chat agent (the AI might greet the user and confirm the
return instructions).

Every field in the form is chosen to either influence the AI's
decision-making or to facilitate the logistics of the return. By keeping
it ≤5 fields, we ensure a quick user experience before the AI chat,
while gathering the critical data points: **what was bought, when,
where, and why it's being returned** -- the same information a manual
support agent would initially ask for.

------------------------------------------------------------------------

## Answers to Research Questions

### 3.1 Form-to-AI Agent Workflow

Once the intake form is submitted, the collected data must be handed off
to the AI chatbot so it has context to start assisting the customer. The
**pattern** we'll use is to include the form data as **initial context
in the chat conversation**, usually via a system message or injected
prompt, before the AI converses with the user. This ensures the GPT-4
agent knows the relevant facts (purchase date, return vs complaint,
etc.) without the user re-typing them.

**Passing Structured Data to the AI:** We will create a **system
message** for GPT-4 that summarizes the form info and relevant policy
rules. For example:

> *System prompt:* "You are a Sinsay customer service assistant. The
> user is initiating a **defective product complaint**. **Context:**
> Purchase channel = Online, Order #SN123456789, Purchase date =
> 2025-12-01 (beyond 30 days), Item = Women's Jacket. Customer says the
> zipper fell off after one use. According to Sinsay's return policy,
> standard returns are only within 30 days and items must be undamaged.
> Complaints for defects are allowed under warranty. **Your job:**
> Verify if this defect qualifies for a free repair/refund. Ask the user
> for any needed details (like photos of the defect or how it happened).
> Respond in a helpful, policy-abiding manner."

By providing such a prompt, the AI is immediately aware of the scenario
and the distinction between a normal return vs. a defect claim. The
structured fields from the form (order ID, dates, etc.) are embedded as
plain text in this prompt (since the OpenAI API does not directly accept
JSON "context" outside of messages). This approach is a common pattern:
*transitioning from a form to a conversation by summarizing the form
data as an initial assistant/system message*. It saves the user from
repeating information and makes the AI's responses more
informed[\[15\]](https://community.openai.com/t/form-filling-chatbot-ideas/183868#:~:text=Basically%20I%20want%20ideas%20for,input%20to%20my%20form%20API)[\[16\]](https://community.openai.com/t/form-filling-chatbot-ideas/183868#:~:text=gabrielimanibureau%20%20January%2023%2C%202024%2C,3%3A05pm%20%205).

**Technical Implementation:** In the Spring Boot backend, when the form
is submitted (to e.g. `POST /api/intake`), we will: 1. Create a new
conversation context (assign an ID or use the session). 2. Store the
form data in that context (in memory, or encode it in a JWT/token
returned to the frontend). 3. Prepare the initial AI prompt. This likely
includes: - A **System Message** with Sinsay's return & complaint policy
highlights (fetched from config) and instructions to the assistant on
how to behave (polite tone, etc.). - An **Assistant Message** (optional)
that restates the user's form input. Alternatively, we could treat the
form submission as if the user "said" those facts in the chat. For
instance, the first user message to GPT-4 could be "I bought an item on
2025-12-01, and its zipper broke. It's been 60 days since purchase. I
want to file a complaint." The AI would then respond. However, it's
cleaner to use a system message so the AI knows these as facts rather
than something to debate or clarify. - No actual user message is needed
yet, since the user hasn't "asked" anything; they just provided info. We
will prompt the AI to proactively ask a question or give instructions as
appropriate.

**Example using OpenAI Java SDK:** We can use the Chat Completion API
with a list of messages. Pseudo-code:

    List<ChatMessage> messages = new ArrayList<>();
    messages.add(new SystemMessage("""
    Role: Sinsay Returns Assistant
    Policy: 30-day returns require unused product with tags. Defective items can be claimed under warranty (2 years) but wear-and-tear is not covered.
    Context: Customer purchase was online on 2025-12-01 (order SN123456789). It’s now beyond 30 days, so this is a defect complaint. Defect: "zipper fell off after one use."
    Your task: greet the customer, confirm these details, request a photo of the defect, and then analyze the photo to decide if it's a manufacturing defect or not. Then provide next steps according to policy.
    """));
    // We don’t yet add a user message, we’ll get the AI to initiate.
    OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
        .model("gpt-4-vision") 
        .messages(messages)
        .build();
    ChatCompletionResult result = openAiClient.chat().create(request);
    String assistantReply = result.getChoices().get(0).getMessage().getContent();

In this flow, the first response might be the AI saying: *"Hello! I'm
sorry to hear you had an issue with your jacket. Could you please upload
a photo of the broken zipper? I'll check if it's a manufacturing
defect."* -- thus the conversation begins.

**Transitioning to Conversation UI:** The backend will send the AI's
first reply to the frontend, which the React app will display in the
chat UI. From this point on, the user and AI chat freely: - The user
uploads an image or types additional info (these become new user
messages sent to backend). - The backend attaches the image (converted
to base64 or via a URL accessible to OpenAI) to a new OpenAI API call
along with the prior messages (including the system prompt and
assistant's last message, to maintain context), and gets the next
assistant
answer[\[17\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=var%20imageResource%20%3D%20new%20ClassPathResource%28). -
This loop continues until resolution.

We will likely use an **ID to track conversation state** (e.g., an
in-memory map: `conversationId -> List<Message>`). The form submission
handler can initialize this with the system message and perhaps an
initial assistant question. Subsequent chat messages from the user will
reference the `conversationId` so the backend knows where to append the
new message. An alternative is to have the frontend store the entire
message history and send it every time (stateless approach), but that
can be heavy; better to store server-side or in a hidden session.

**Context retention:** GPT-4's message history will carry the form info
throughout the conversation, so the AI remembers those facts. We will
cap the history if it grows (not likely in a short return dialog) to
avoid hitting token limits. Because GPT-4 Vision can analyze images,
when the user uploads a photo, we include it in the next prompt as an
image attachment (using the OpenAI API's ability to accept images in
chat). With Spring AI's library, for example, we create a `UserMessage`
with text "Here is the item photo." and attach the image bytes as
`new Media(MimeTypeUtils.IMAGE_JPEG, imageResource)`[\[17\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=var%20imageResource%20%3D%20new%20ClassPathResource%28).
The model then replies with analysis of the image.

**Summary:** The structured form data flows into the AI conversation by
being summarized in a system message. This pattern -- *form → system
prompt → conversational agent* -- is a best practice for context-aware
chatbots[\[15\]](https://community.openai.com/t/form-filling-chatbot-ideas/183868#:~:text=Basically%20I%20want%20ideas%20for,input%20to%20my%20form%20API).
It ensures the AI's first utterance is on target (no need to ask for
basic info already provided) and the user immediately feels understood.
From there, natural language interaction takes over, powered by the
context we've given and the images the user shares.

### 3.2 Vercel AI SDK UI with Spring Boot Backend

Vercel's AI SDK provides convenient React hooks and components (like
`useChat` and `<Chat/>`) to build chat UIs, but it's primarily designed
to work with Next.js and Edge Functions. The question is whether we can
use these UI components in our React 19 app and have them communicate
with a **Spring Boot REST API** instead of a Node/Next backend. The
answer: **Yes, with some configuration**. The Vercel AI SDK is
essentially frontend tooling; it can call any endpoint as long as it
returns the expected stream of events. By default, `useChat` assumes an
API route at `/api/chat` on the same domain (in Next, implemented via a
serverless
function)[\[18\]](https://ai-sdk.dev/docs/reference/ai-sdk-ui/use-chat#:~:text=api%3F%3A).
We can mimic this by exposing an equivalent endpoint in Spring Boot.

**Integration Pattern:** We will create a Spring Boot controller for the
chat, e.g. `POST /api/chat`, that accepts user messages (and images) and
streams back the assistant's response. On the React side, we initialize
`useChat` with a custom transport or endpoint pointing to our Spring API
(the hook allows specifying a different `api` URL or a custom fetch
function[\[19\]](https://ai-sdk.dev/docs/reference/ai-sdk-ui/use-chat#:~:text=transport%3F%3A)).
The SDK's UI components (like `<MessageList/>`, `<ChatPanel/>` etc.)
don't care *what* the backend is built with, only that it follows the
expected protocol (SSE or chunked streaming for real-time updates).

**Streaming Responses:** One consideration is streaming the GPT-4
response so the user sees the answer appear word-by-word. Vercel's
`useChat` hook uses fetch with `ReadableStream` under the hood to append
tokens as they arrive. Our Spring Boot endpoint can support this via
**Server-Sent Events (SSE)** or by writing to the `HttpServletResponse`
output stream flush periodically. Another approach is to not stream for
PoC (collect full response then send) -- simpler but less dynamic. If we
want streaming: - Spring can use
`org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter`
or `SseEmitter` to send data chunks. The Vercel UI will handle it if the
response is properly formatted (each chunk prefixed as the SDK expects
-- typically the OpenAI stream format). - Alternatively, implement the
endpoint to **proxy** the OpenAI stream: as it receives chunks from
OpenAI, immediately flush them to the client.

This is doable, but requires careful implementation. If we find it
complex, we might opt for a simpler approach: use the Vercel UI
components but only display full messages at a time (the SDK also
supports non-streaming mode). In either case, the **UI components
themselves (buttons, message bubbles)** work fine in a CRA/Vite app,
since they're just React components and hooks.

**Limitations/Considerations:** - The Vercel AI SDK's examples assume
Next.js API routes. We must ensure CORS is handled if our Spring API is
on a different origin (for development, our React dev server on
localhost:3000 calling Spring on 8080 -- we'll enable CORS for those
endpoints). In production, if we bundle the app, same-origin calls are
fine. - We might not be able to use some Next-specific features like
edge caching of AI responses or Vercel-specific streaming
implementations. But for functionality, it can work. - **Session
affinity:** The Vercel hook by default will maintain state of messages
internally on the client. We'll still send `conversationId` with each
request to map to server-side context. Alternatively, we let the client
store the last N messages and send them each time (less ideal, but
possible if stateless).

**Alternative Solutions:** If the Vercel AI SDK UI proves cumbersome
with Spring Boot, we have other options: - **assistant-ui (React
library)**: As noted, `assistant-ui` is a popular library that handles
streaming chats and is
backend-agnostic[\[20\]](https://github.com/assistant-ui/assistant-ui#:~:text=pixel%20,easy%20extension%20to%20custom%20APIs).
It provides composable components (using Shadcn UI styles) and can work
with *any* custom backend by simply calling it (it even has integrations
for Vercel SDK or custom transports). Using `assistant-ui` in our React
app might actually align well since we are already using Shadcn UI --
its default theme is built on
it[\[21\]](https://github.com/assistant-ui/assistant-ui#:~:text=assistant,grade%20AI%20chat%20experiences%20fast).
We would just call our Spring API via fetch when the user sends a
message (the library likely allows us to plug in our API call logic). -
**Build from scratch:** The chat interface is essentially a controlled
list of messages and an input box. We can implement a simple version:
store `messages` in React state (an array of
`{ role: 'user'|'assistant', content: string }`), render them in a
scrollable `<div>`, and have a form for user input. On submit, append
the user message to state, call the Spring `/api/chat` endpoint (using
`fetch` or Axios), then stream or await the assistant response, and
append it when ready. This avoids external dependencies and gives full
control, albeit with a bit more coding.

Given time constraints, leveraging a library like assistant-ui is
attractive, since it **supports streaming, file attachments, and is
built for
customization**[\[22\]](https://github.com/assistant-ui/assistant-ui#:~:text=Why%20assistant)[\[23\]](https://github.com/assistant-ui/assistant-ui#:~:text=,theme%20you%20can%20fully%20customize).
It also uses Radix and Shadcn-like patterns which fit our UI stack. We
can integrate it by:

    npm install @assistant-ui/react
    npx assistant-ui init   # to configure the project (if needed)

Then use its components to compose the chat window.

**Summary:** We can indeed use Vercel's AI SDK React components with a
Spring Boot backend by pointing the `useChat` hook to our Spring
endpoint[\[19\]](https://ai-sdk.dev/docs/reference/ai-sdk-ui/use-chat#:~:text=transport%3F%3A).
The integration will require enabling CORS and implementing streaming
output on the Spring side. If that proves complex, libraries like
**assistant-ui** or a custom implementation are viable alternatives that
work seamlessly with a Spring REST API. The key is that our frontend
remains decoupled from the backend technology -- it just needs an API
URL to post messages and receive responses. In our PoC, we'll likely
implement a custom `<ChatBot>` component using either the Vercel hook
(with a custom fetch) or the assistant-ui library for rapid development.

### 3.3 Monorepo Architecture and Hosting Strategy

We choose a **monorepo** to house both the backend and frontend, as this
is a single cohesive project. Monorepo means one Git repository
containing multiple projects (in our case, a Spring Boot project and a
React app). Industry experience shows that if the frontend and backend
are part of the same product and developed by one team, a monorepo
simplifies
coordination[\[24\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=It%20isn%E2%80%99t%20a%20monorepo%20if,the%20same%20repo%20by%20definition)[\[25\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=%E2%80%A2%20%201y%20ago%20,%E2%80%A2%20Edited%201y%20ago).
Changes that span front and back (like adding a new field to the API and
using it in the UI) can be done in one branch and one pull request,
ensuring nothing falls out of
sync[\[26\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=StoneAgainstTheSea).

**Repo Structure:** We will structure the repository with top-level
folders, for example:

    sinsay-returns-ai/
    ├── backend/   (Spring Boot project)
    │   ├── pom.xml (or build.gradle)
    │   └── src/main/java/com/sinsay/...(controllers, services, etc.)
    ├── frontend/  (React 19 project)
    │   ├── package.json, vite.config.js, tailwind.config.js, etc.
    │   └── src/... (React app source)
    └── shared/    (optional, for any shared configs or assets)

This aligns with common monorepo
layouts[\[1\]](https://graphite.com/guides/monorepo-frontend-backend-best-practices#:~:text=Here%27s%20a%20simple%20directory%20layout,both%20frontend%20and%20backend%20apps),
where each app is isolated in its directory. We might also include a
root `README.md` and CI config that builds both.

**One repo vs. separate repos:** Since this is a PoC with a small team,
one repo is best. Using separate repositories would complicate things
like aligning versions and deploying simultaneously when a feature
touches both
sides[\[24\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=It%20isn%E2%80%99t%20a%20monorepo%20if,the%20same%20repo%20by%20definition).
In a monorepo, an atomic commit can update both projects, and tooling
(like a single CI pipeline or a combined dev script) can manage them
together. As one Reddit discussion succinctly put it: *"If it's the same
project, same
repository"*[\[24\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=It%20isn%E2%80%99t%20a%20monorepo%20if,the%20same%20repo%20by%20definition).

**Monorepo ≠ single deployment:** We have a choice: *embed* the frontend
into the Spring Boot app for a single deployment artifact, or *deploy
separately*. Let's compare:

- **Embedded SPA (Single Jar/WAR deployment):** We can bundle the built
  React app (HTML, JS, CSS) into Spring Boot's
  `src/main/resources/static/` directory. Spring Boot will serve these
  files on requests to `/` or `/static/*`. This yields one service that
  serves both the API and the frontend UI. **Pros:** simple deployment
  (only one service to run), no need to configure CORS, consistent
  versioning (frontend and backend always deploy together, avoiding
  mismatch). **Cons:** front and back must be released in lockstep (not
  a big issue for a small project), and builds might be slightly coupled
  (though we can automate the frontend build as part of Maven
  lifecycle[\[27\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=,Conclusion)).
  For our PoC, this approach is ideal due to
  simplicity[\[2\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Learn%20how%20to%20bundle%20your,simplifying%20both%20dev%20and%20ops)[\[3\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Image%3A%20diagram%20showing%20how%20the,JAR%20after%20running%20mvn%20package).
  As Jessy's tutorial notes, bundling a React/Vite app with Spring Boot
  simplifies dev and ops for small
  projects[\[2\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Learn%20how%20to%20bundle%20your,simplifying%20both%20dev%20and%20ops).
  We will implement this bundling.

- **Separate Deployments:** This means deploying the React app on a
  static host or Vercel/Netlify, and the Spring Boot API on a server or
  cloud function, serving from different origins. **Pros:** Each can be
  scaled or deployed independently; potentially use CDN for the
  frontend; align with microservice philosophy if the team grows.
  **Cons:** Requires dealing with CORS (cross-origin requests) and
  environmental config for API URL in the frontend. It's more complex
  for a PoC and unnecessary unless we foresee separate lifecycles. For
  now, we do not -- the features we implement will likely require
  changes in both front and back simultaneously, so tying deployments is
  fine.

**Development in Monorepo:** We can still **run the front and back in
dev mode separately** for convenience. For example, run Spring Boot on
port 8080 and `npm run dev` for React on port 5173 (Vite's default).
We'll enable CORS on Spring Boot for `http://localhost:5173` so the dev
UI can call the API. The monorepo structure aids local dev too, as
everything is in one place. We might add some helper scripts or an npm
workspace so that a single command can start both (e.g., using npm
concurrently or IntelliJ compound run configs).

**Best Practices:** In a monorepo, clear separation of concerns is key.
We keep backend code in the backend folder only, frontend code in
frontend only. Shared code (like if we had API schema definitions or
typings) could go in a "shared" module -- for instance, if we use
OpenAPI codegen for API models for both Java and TypeScript. That's
beyond our scope here, but monorepo would allow it easily (by
referencing files across projects). We also need to ensure builds don't
conflict: we'll configure Maven to ignore the frontend folder (unless we
integrate the build) and manage node_modules separately. Tools like Yarn
Workspaces or Nx can help manage multi-project repos, but for two
projects it might be overkill. A simple solution is to treat them as
independent but use the repo to version them together.

**Conclusion & Deployment:** We will proceed with a **monorepo, embedded
deployment** strategy. The React app will be built into static files
that Spring Boot
serves[\[28\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Image%3A%20diagram%20showing%20how%20the,JAR%20after%20running%20mvn%20package).
In production (PoC demo), we'll just run the Spring Boot JAR and access
the UI via `http://host:8080` (Spring will serve the React index.html,
which will then call the API endpoints on the same host). This gives the
illusion of one unified application to the user.

This approach is fitting for our current scope. Should the project
evolve (e.g., a dedicated mobile app consuming the same API, or the need
to deploy front-end updates independently of the back-end), we can
refactor the deployment: host the React build on a CDN and deploy the
Spring Boot API separately (monorepo can still be kept for code
management, as many companies do). But as a starting point, monorepo +
single deployment is straightforward and
recommended[\[2\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Learn%20how%20to%20bundle%20your,simplifying%20both%20dev%20and%20ops).

### 3.4 Spring Boot Best Practices for AI Integration

Our Spring Boot backend will not be very large, but we should structure
and implement it using standard best practices to ease maintenance and
make AI-assisted development smoother. Key considerations:

**Project Structure & Layers:** Follow a conventional layered
structure: - **Controller Layer:** REST controllers (annotated with
`@RestController`) handle HTTP requests (form submission, image upload,
chat messages). These controllers should be lean -- just parsing input,
calling services, and formatting output (e.g., returning
`ResponseEntity` with appropriate status codes). We'll use constructor
injection (or Lombok `@RequiredArgsConstructor`) for dependencies --
*avoid field injection* as it's considered an anti-pattern and AI tools
might default to it if not
guided[\[29\]](https://blog.jetbrains.com/idea/2025/05/coding-guidelines-for-your-ai-agents/#:~:text=Nowadays%2C%20asking%20an%20AI%20agent,working%20result%2C%20but%20it%20may).
Our controllers: - `ReturnsController` could handle the form intake
(`POST /api/intake`). - `ChatController` for the chat endpoint
(`POST /api/chat`). - `ImageController` (optional) if we have a separate
image upload route (e.g., `POST /api/upload-image`) that returns an ID
or directly triggers analysis. - **Service Layer:** Business logic is
encapsulated in services (annotated `@Service`). For example, an
`OpenAIService` to call the OpenAI API (so that controllers don't have
that logic inline), and perhaps a `ReturnPolicyService` that checks
eligibility (30-day window, etc.) and prepares prompts. The service
layer allows reuse and easier testing (we can unit test a service
without the web layer). Also, if we decide to support multiple channels
(mobile, kiosk) as mentioned, having the core logic in services means
those channels can reuse it by calling the same service or API. -
**Model/Domain Layer:** Define DTOs for requests and responses. For the
form, a class `ReturnRequestDto` with fields matching the JSON
(PurchaseChannel, orderId, etc.) and validation annotations. Use **Bean
Validation** (`javax.validation`) to enforce rules on these DTOs at the
API boundary (e.g., `@NotNull`, `@Email`, custom `@Within30Days` for
purchaseDate). Spring Boot will automatically validate if we annotate
the controller method parameter with `@Valid`, returning a 400 Bad
Request if validation
fails[\[30\]](https://dev.to/gianfcop98/spring-boot-and-validation-a-complete-guide-with-valid-and-validated-471p#:~:text=Spring%20Boot%20natively%20supports%20validation,Hibernate%20Validator%20under%20the%20hood)[\[31\]](https://dev.to/gianfcop98/spring-boot-and-validation-a-complete-guide-with-valid-and-validated-471p#:~:text=Spring%20Boot%E2%80%99s%20validation%20system%20supports,380%20annotations).
This provides a consistent error response that we can either pass to the
front-end or even let the AI handle (though typically validation errors
are handled by the UI directly). - **Configuration:** Externalize
configurable aspects. For instance, the OpenAI API key will be in
`application.properties` (or an env var) not hardcoded. We can set
`spring.ai.openai.api-key=${OPENAI_API_KEY}` if using Spring
AI[\[32\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=The%20Spring%20AI%20project%20defines,obtained%20from%20openai.com).
Also, the return policy text from the Sinsay website might be long -- we
can store it in a file (e.g., `classpath:policies/return_policy.txt`) or
in the config as a multiline property, and load it at startup into a
constant. This way, if policies update, we edit the file, not the code.
Prompts can be templated and stored similarly (some projects keep prompt
strings in separate files for ease of tweaking without recompiling). -
**Logging & Error Handling:** Implement global exception handling via
`@ControllerAdvice` to catch exceptions like OpenAI API failures or
image processing errors. For example, if OpenAI's API is down or returns
an error, we catch that in `OpenAIService` and throw a custom exception
(e.g., `AIServiceException`). The ControllerAdvice can catch that and
return a friendly error JSON (and an HTTP 502 or 500 status). Also log
errors with context (including maybe the conversationId or orderId) for
debugging. We should be careful not to log sensitive info (like the
image content or the OpenAI API key). - **OpenAI API Integration:** Use
a robust library to call OpenAI. There are two main choices: - *Official
OpenAI Java SDK:* A library (`com.openai:openai-java`) that provides an
easy interface for OpenAI's REST
API[\[33\]](https://github.com/openai/openai-java#:~:text=implementation%28%22com.openai%3Aopenai).
This supports chat completions and should support image inputs (GPT-4
with vision) as well. For example, it has a `ChatCompletionRequest`
builder. The official SDK would require us to manage streaming manually
(if needed) by reading the HTTP response. - *Spring AI project:*
Spring's experimental library that auto-configures OpenAI clients for
you[\[7\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=Gradle).
Using `spring-ai-starter-model-openai` we get a bean `OpenAiChatModel`
that we can inject and call. This model supports multimodal messages
easily -- as documented, you can just attach images via the `Media`
class[\[17\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=var%20imageResource%20%3D%20new%20ClassPathResource%28).
It also has convenient features like retries/backoff out of the box for
transient
errors[\[13\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=Property%20Description%20Default).
Given that we're on Spring Boot 3.5.9, Spring AI should be compatible
and provides a higher-level abstraction. We will likely use Spring AI
for speed and future-proofing (since it aligns with Spring Boot nicely).
For instance, configuring the API key in `application.yml` as shown in
Spring's
docs[\[32\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=The%20Spring%20AI%20project%20defines,obtained%20from%20openai.com)
and then autowiring `OpenAiChatModel` is all that's needed to get
started. Under the hood it uses the official client. - **Calling GPT-4
with images:** According to Spring AI docs, we can do:

    var userMsg = new UserMessage("Explain what you see", new Media(MimeTypeUtils.IMAGE_PNG, imageResource));
    ChatResponse resp = chatModel.call(new Prompt(userMsg, OpenAiChatOptions.builder().model("gpt-4o").build()));
    String content = resp.getResult().getOutput().getContent();

which sends an image with the user's
prompt[\[17\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=var%20imageResource%20%3D%20new%20ClassPathResource%28).
We will do similar, but our prompt will include policy context as well.
Ensure the image file is read as `Resource` (Spring can handle a
`MultipartFile` from the upload and convert to `FileSystemResource` or
bytes easily). - **Performance and Scalability:** While a PoC, we keep
an eye on scalability. For example, do not block threads during the AI
call any more than necessary -- the OpenAI API call is network-bound, so
using Spring WebClient (reactive) or making the controller async (with
`@Async` service) could help handle more concurrent requests. But given
GPT-4 calls are heavy anyway, a simpler sync call might be fine
initially (with perhaps a timeout set). We will also restrict the image
size -- perhaps resize or reject images above certain MB to avoid huge
payloads (OpenAI has a 50MB request limit and will charge based on image
resolution[\[34\]](https://platform.openai.com/docs/guides/images-vision#:~:text=,individual%20image%20inputs%20per%20request)[\[35\]](https://platform.openai.com/docs/guides/images-vision#:~:text=match%20at%20L989%20B,no%20more%20than%201536%20patches)).
We can use an image library to downscale images to, say, 1024px width,
before sending to AI to save tokens. - **Testing (JUnit 5):** Write
tests especially for the policy logic and OpenAI prompt generation. For
example, a unit test for "if purchaseDate is 40 days ago and
type=Return, service should flag it or convert it to complaint." Use
`@SpringBootTest` with `@AutoConfigureMockMvc` to test the form
submission endpoint, feeding sample JSON and expecting the correct
response (like 400 on invalid or some success structure). Also test that
given a sample defect image (we can use a known image of, say, a stained
shirt), the OpenAIService returns a classification (this could be hard
to test offline; we might mock the OpenAI client with a preset
response). Edge-case tests include: missing order ID, malformed email,
image that is not actually an item (maybe the AI should respond it
cannot identify any clothing -- this tests our prompt instructions for
the AI).

**Coding Standards & A.I. Agent Considerations:** We will document
specific guidelines in `AGENTS.md` (see section 6), but in general: -
Use clear naming (e.g., `ReturnRequestDto`, `ComplaintService`). - Keep
classes and methods focused (Single Responsibility). For example, don't
cram too much logic in the controller; an AI agent reading the code
should see a clean delegation (which makes it easier for the AI to
modify one part without side effects). - Avoid using reflection or
"magic" that an AI might not handle well. Simple, explicit code is
better for AI pair programmers. For instance, rather than a complex
generic framework, we'll write straightforward methods. - Include
comments especially in areas involving the AI prompts, explaining why we
do certain things (this helps both human and AI maintainers). - Use
Lombok to reduce noise: e.g., `@Data` or `@Getter` on DTOs, and
`@RequiredArgsConstructor` on services for dependency injection. This
makes the code shorter -- and we must ensure IntelliJ's annotation
processing is on so Lombok works (Agents should be instructed that if
they add a Lombok-annotated class, to remind about enabling annotation
processing[\[36\]](https://stackoverflow.com/questions/24006937/lombok-annotations-do-not-compile-under-intellij-idea#:~:text=Overflow%20stackoverflow,Enable%20annotation%20processing)).

By adhering to these practices, our Spring Boot backend will be clean,
resilient, and amenable to AI-assisted coding. This means faster
iterations (the AI doesn't get confused by messy code) and a stable
foundation for the returns system.

### 3.5 React Frontend Best Practices

Our React 19 frontend will be implemented as a Single Page Application
using modern practices. Key areas to address: form management, UI
architecture, state management, and integrating our UI libraries (Shadcn
UI with TailwindCSS).

**Component Architecture:** We'll split the UI into logical, reusable
components: - A `ReturnIntakeForm` component for the multi-step form. -
An `ImageUpload` component for uploading/previewing images (used when a
complaint case is initiated). - A `ChatbotUI` component for the
conversation interface after the form. - Possibly smaller components
like `TextField`, `SelectField` wrapping Shadcn UI form controls for
consistency, and a `MessageBubble` for chat messages.

Using a component-based architecture ensures each part of the process is
encapsulated and easier to manage or modify by an AI agent. We'll
colocate related components; for example, form-related components can
live in `src/components/form/`, chat in `src/components/chat/`.

**Forms with React Hook Form:** We choose **React Hook Form (RHF)** for
form handling because it works seamlessly with uncontrolled components
and integrates with validation schemas. Shadcn UI's documentation even
provides patterns for using RHF with their
components[\[8\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=Validation).
Benefits of RHF: minimal re-renders, built-in form state (touched,
errors, etc.), and easy integration of **Zod** or **Yup** for schema
validation. We'll define a Zod schema for our form that mirrors the
backend DTO rules (e.g., date must be ≤30 days in the past if return --
we can implement that logic in a custom refinement in Zod). RHF will use
`zodResolver` to automatically validate on the client side and show
errors before
submission[\[37\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=React%20Hook%20Form%20validates%20your,hook).

For example:

    import { useForm } from 'react-hook-form';
    import { zodResolver } from '@hookform/resolvers/zod';
    const FormSchema = z.object({
      purchaseChannel: z.enum(['ONLINE','IN_STORE']),
      orderId: z.string().optional(),
      receiptNumber: z.string().optional(),
      purchaseDate: z.string().refine(val => /* date parse and check */),
      requestType: z.enum(['RETURN','COMPLAINT']),
      defectDescription: z.string().max(250).optional(),
      email: z.string().email().optional()
    }).refine(data => {
       if(data.purchaseChannel==='ONLINE' && !data.orderId) return false;
       if(data.purchaseChannel==='IN_STORE' && !data.receiptNumber) return false;
       // if RETURN and purchaseDate > 30 days, invalidate:
       if(data.requestType==='RETURN') {
           const days = differenceInDays(new Date(), new Date(data.purchaseDate));
           return days <= 30;
       }
       return true;
    }, { message: "Invalid form data" });

    const form = useForm({ resolver: zodResolver(FormSchema) });

Using RHF, we'll register Shadcn UI inputs via RHF's `<Controller>` if
needed (Shadcn's `Input`, `Select` can be wrapped to forward refs). The
UI library gives us pre-styled components (TailwindCSS classes for
consistency). For example, Shadcn's form pattern uses `<FormField>` and
`<FieldError>` components to display validation errors in a friendly
way[\[38\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=autoComplete%3D,Field%3E%20%29%7D)[\[39\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=,Field).
Our agent can use those patterns to keep forms accessible and
consistent.

**Dynamic Fields:** We will use React state or watch from RHF to
show/hide fields. For instance:

    const watchChannel = form.watch('purchaseChannel');
    ...
    {watchChannel === 'ONLINE' ? 
       <Input {...form.register('orderId')} placeholder="Order #"/> : 
       <Input {...form.register('receiptNumber')} placeholder="Receipt #"/>}

Similarly, for requestType toggling defectDescription. This conditional
rendering is straightforward and keeps all fields in one form object. We
can also break the form into steps (pages): - Step 1: Channel +
Order/Receipt + Date - Step 2: Return vs Complaint + maybe defect
description - Step 3: (maybe contact info or confirmation)

But given only 5 fields, it might be acceptable as one page with
sections that appear. If we wanted a wizard UX, we could manage a step
index in state and only render the relevant fields, storing intermediate
values. React Hook Form makes it easy to retain values between steps (we
can keep the form instance alive and just conditionally render parts).

**State Management:** For this PoC, using heavy state libraries (Redux,
MobX) is unnecessary. We can use component local state and React Context
for passing data between steps: - When the form is submitted, we could
lift the form data state up to a context (e.g., `ReturnsContext`) that
also holds the conversationId returned by backend. The Chatbot component
can consume that context to know what conversation to attach to. -
However, it might be simpler: on form submit, we call backend, get
conversationId, then directly initialize the Chatbot component with that
ID (as a prop). The Chatbot can maintain its own state of messages. - If
we needed global state (like user authentication info or theme), we'd
consider Zustand or Redux Toolkit. But here it's not necessary --
context or prop drilling suffice.

**UI/UX with Shadcn and Tailwind:** Shadcn UI provides pre-built
components styled with Tailwind. We should follow their usage
guidelines: - Use their layout and form components for consistent
spacing and responsive design. For example, using `<Card>` components to
box the form and chat
nicely[\[40\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=return%20%28%20%3CCard%20className%3D%22w,CardContent). -
Use Tailwind utility classes for any custom styling. We should define a
Tailwind config (with `content` pointing to our files in the monorepo)
and include Shadcn's default styles (which often requires adding Radix
UI CSS fixes). Since our React is not Next.js, we ensure to include the
base styles:

    @tailwind base;
    @tailwind components;
    @tailwind utilities;

and if Shadcn provided any CSS file for their components (they often
rely purely on tailwind classes so maybe not). - Ensure **dark mode or
theme** as needed (not critical for PoC, but tailwind can handle if
needed). - Use **icons** or feedback where appropriate (maybe an icon
for image upload button, loading spinners when waiting for AI response,
etc.). There's HeroIcons or Lucide icons which integrate with Shadcn.

**File Upload Handling:** For the image upload step: - Use an
`<input type="file" accept="image/*">` element (Shadcn doesn't have a
fancy file picker by default, but we can style a button that triggers
the file dialog). We'll manage this as an uncontrolled input since React
Hook Form's docs suggest file inputs be
uncontrolled[\[41\]](https://github.com/shadcn-ui/ui/discussions/2137#:~:text=GitHub%20github,to%20have%20an%20uncontrolled%20input).
Basically, we'll capture the file via `onChange` and not keep it in RHF
state. - On file select, show a preview. We can create a URL object:
`const url = URL.createObjectURL(file)` and render
`<img src={url} alt="preview" className="max-w-xs"/>`. This gives user
feedback on what they uploaded. - We might allow uploading multiple
images (policy might allow showing multiple defects), but to keep it
simple, one image is likely enough per complaint. - When ready to send
to AI, we have two approaches: 1. Directly send the image file as part
of the chat message to the backend. This could be done by reading the
file into base64 on the client or by sending multipart form data. The
Vercel SDK (if used) might require the image to be converted to base64
string in the JSON payload since SSE streaming of binary might be
tricky. Alternatively, we use a normal POST (non-stream) endpoint for
images. 2. Use a separate endpoint to upload the image and get back an
ID or URL, then send a chat message referring to that image ID. This
adds complexity but offloads image handling. However, for PoC, it's
probably fine to send it directly.

- We'll likely implement the simpler path: when the user clicks "Send
  Photo" (or the agent requests a photo), the frontend will do a
  `fetch('/api/chat', { body: FormData with file, conversationId, message: "Image attached" })`.
  The Spring controller can accept `MultipartFile` along with a JSON
  part for conversationId. This is a mixed multipart request. We can
  handle that by using Spring's `@RequestPart` for JSON and
  `@RequestPart` for file. The controller then calls the OpenAI service
  with the image. The response (analysis text) is returned and
  displayed.

**Conditional Rendering & Navigation:** Implement transitions in the
UI: - Initially show the `ReturnIntakeForm`. On submit (success), hide
the form (or mount the next component). Perhaps we replace the form with
the Chatbot panel, or navigate to a new "page" in SPA (we could use
React Router for a multi-page feel: e.g. `/start` for form, `/chat` for
chat, but since state (conversationId) needs to carry over, we can use
context or state management). - A simple method: use a top-level
component that holds
`const [conversationId, setConversationId] = useState(null)`. Initially,
`conversationId` is null so we render the form. When form is submitted
successfully, set the conversationId (and store form data if needed in
context for agent reference). Then conditionally render the ChatbotUI.
This avoids needing an actual route change. - Ensure a smooth UX: maybe
display a brief confirmation or loading while switching to chat
("Connecting you to an agent..."). - The ChatbotUI, on mount, can call
an endpoint to get the AI's first message (if we didn't already get it
in the form response). Or, as per the earlier plan, the backend might
have responded with an initial AI message along with conversationId when
the form was submitted (that would save one round-trip). We can design
it either way. Perhaps simpler: form submit returns the first assistant
message as well. Then we show chat with that message pre-populated.

**Chat UI considerations:** Whether using a library or custom, ensure: -
Auto-scroll to newest message in the chat container (libraries do this;
if custom, use `useEffect` to scroll). - The user input box is always
visible and focused when needed. - If the AI is responding, either
disable the user input or indicate a "Bot is typing..." state to prevent
overlapping messages. - Support for the user to send another image in
chat if needed (maybe not needed but could allow). - Use Markdown
rendering for AI messages if they contain formatting (OpenAI might
return bullet points or URLs). We can include a lightweight Markdown
renderer or at least handle line breaks, etc. (Shadcn UI has a
`Markdown` component or we can style with Tailwind typographic
classes). - Make the chat accessible (each message with `role="article"`
or similar, and input with label for screen readers if needed).

**Testing & Tools:** We will include front-end testing: - Use **React
Testing Library** with Vitest or Jest to simulate filling the form and
ensure validation messages show and correct API calls are made. For
example, test that selecting "Return" with a date 40 days ago triggers
an error on submit (the component should not call the API in that
case). - Use Testing Library to simulate uploading a file (this is a bit
tricky in JSDOM, but we can fake a File object). - Snapshot test the
Chat UI to ensure messages render correctly given a messages state.

**Integration with TailwindCSS:** Ensure Tailwind is properly configured
to scan our content in the monorepo (we'll include the
`frontend/src/**/*.{js,jsx,ts,tsx}` in tailwind.config content paths).
We'll also use Tailwind classes for responsive design (e.g.,
`sm:max-w-md` on the form container to not stretch on big
screens[\[42\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=return%20%28%20%3CCard%20className%3D%22w,CardHeader),
etc.). Tailwind's utility-first style will help the AI agent too -- it
can apply straightforward classes instead of fiddling with custom CSS,
which is easier for it to "guess" and we can verify via preview.

By following these best practices, our React codebase will be modular,
maintainable, and **AI-friendly**. The use of form libraries and UI
components reduces the amount of custom code an AI has to generate
(e.g., we won't have to write our own input styling or validation logic
from scratch -- we use widely used patterns that the AI likely
knows[\[43\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=Client)).
This also means faster development and fewer bugs, as these libraries
have community backing.

### 3.6 AI Agent Development Optimization (Vibe Coding Approach)

Developing with AI agents (like ChatGPT or GitHub Copilot) in the loop
requires setting up the project such that the AI can understand the
project context easily and perform tasks through CLI or code as needed.
The concept of "Vibe Coding" emphasizes natural language instructions to
AI agents to build software. To optimize for this:

**Project Initialization via CLI (for agents):** We will utilize CLI
commands to scaffold and configure as much as possible, so an AI can
execute simple commands to set up boilerplate: - **Spring Boot
scaffold:** Use Spring Initializr's HTTP API or CLI. For example, an AI
agent could run:

    curl https://start.spring.io/starter.zip -d dependencies=web,lombok,validation -d javaVersion=21 -d bootVersion=3.5.9 -o backend.zip

to generate a Spring Boot project with Web, Lombok, and Validation
starters. The agent then unzips it. This avoids writing the basic
structure
manually[\[44\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Starting%20with%20a%20Simple%20Backend).
We'll provide such commands in our CLI Reference section. - **React
scaffold:** Use Vite's CLI (as CRA is deprecated for modern use). For
instance:

    npm create vite@latest frontend -- --template react-ts

(We use the TypeScript template for better type
safety)[\[45\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=cd%20src%2Fmain%20npm%20create%20vite%40latest,ts).
This one-liner creates a ready-to-run React app in the `frontend`
directory. The agent then only needs to install dependencies and
integrate our libraries. - **Library installation via CLI:** We prefer
commands over manual edits, for reproducibility. E.g.,

    cd frontend && npm install react-hook-form @hookform/resolvers zod @assistant-ui/react

to add multiple front-end libs in one go, which an AI can execute in a
dev environment. Similarly,

    ./mvnw install:install-file ... 

if we needed a local jar, but mostly we'll use Maven Central deps. For
Java dependencies, an AI can edit the `pom.xml` but to minimize
confusion, we list exactly which dependencies to add and their versions.
The agent can copy-paste those into pom.xml or use IntelliJ's Maven
tool. We'll document these in **AGENTS.md** so it knows what to do.

**Code Generation Patterns:** Encourage the AI to generate code in
**small, verifiable chunks**. For example, instead of asking the AI to
"build the entire form component", we might prompt "build a controlled
text input component using Shadcn UI and React Hook Form". The agent
(especially if guided by our AGENTS.md) will know about using
`<Controller>` and `<Input/>` from Shadcn. We also use examples from
docs -- since these are standard, the AI likely has them in training
(Shadcn's example form code is
widespread[\[46\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=render%3D%7B%28%7B%20field%2C%20fieldState%20%7D%29%20%3D,button%20not%20working%20on%20mobile)[\[47\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=%7Bfield.value.length%7D%2F100%20characters%20,FieldError%20errors%3D%7B%5BfieldState.error%5D%7D)).
By using well-documented libraries, we make the AI's job easier (less
hallucination, more concrete references).

**Minimizing Confusion:** Keep a **consistent project structure** so the
AI always knows where to put things: - We instruct that all React
components go in `src/components` (or a subfolder thereof). All pages or
major sections should have an index or main component file. - In Spring,
follow Java package conventions (e.g.,
`com.sinsay.returnai.controller.ReturnsController`). The AI will then
place new controllers or services in the right package by analogy. - Use
clear, descriptive names (not overly abbreviated). Instead of `AIServ`
or such, name it `OpenAIService`. This helps the AI select the correct
class when editing.

**Agent-specific tools:** There are emerging AI dev tools like
**Cursor** (an AI-enabled code editor) and **aider** (CLI tool to have
GPT modify code). Our setup should be friendly to such tools: - Provide
an `AGENTS.md` file with guidelines and common commands (the agent can
read this to understand how to run/test the project -- we'll fill this
with tips like how to run the Spring Boot app, etc.). Many coding agents
will automatically load AGENTS.md for
context[\[48\]](https://agents.md/#:~:text=Think%20of%20AGENTS,agents%20work%20on%20your%20project)[\[49\]](https://agents.md/#:~:text=One%20AGENTS,agents). -
Include sample prompts or tasks in comments to nudge the agent. For
instance, in AGENTS.md: "To create a new REST endpoint, add a method in
`ReturnsController` -- see existing methods for style." This proactive
guidance steers AI away from pitfalls (like forgetting `@Autowired` vs
constructor injection).

**CLI Commands for Agents:** We ensure any repetitive or error-prone
tasks have CLI solutions: - Setting up Tailwind: rather than editing
config files manually, we can have commands:

    npm install -D tailwindcss postcss autoprefixer
    npx tailwindcss init -p   # generates tailwind.config.js and postcss.config.js

Then the agent just needs to update the tailwind config content paths
and add the `@tailwind` directives in index.css. - Running the app:
We'll list `npm run dev` for front, `./mvnw spring-boot:run` for back,
so the agent (or a developer) can easily start both. Possibly we include
a shell script like `start-dev.sh` that an agent could execute to run
both servers (using concurrently or two terminals). If using IntelliJ,
the agent can also leverage the IDE's run configs (but CLI is more
universal). - Testing: `./mvnw test` and `npm test` will be
documented. - Linting/Formatting: If we add ESLint or Prettier, include
commands for those so AI can auto-fix code style issues by running them.

**Tool selection with CLI in mind:** We leaned towards libraries that
are easy to install and well-known. For instance: - Spring's
dependencies are via Maven (which an AI can handle; plus Spring
Initializr covers initial ones). - React Hook Form, Zod, etc., are
one-line npm installs (as above). - Shadcn UI itself isn't an npm
package per se -- it's a set of components. But the project provides a
CLI to add components (e.g., `npx shadcn-ui add form` to add form
component scaffolds). We might not need to run it since we can manually
create what we need, but it's something to consider. We will note these
CLI in our docs if relevant. - The **Vercel AI SDK** was considered; to
use it, we'd `npm install @vercel/ai`. But given we might use
assistant-ui, we installed that. Assistant-ui also has a CLI
`npx assistant-ui create` for a starter, but since we already have a
project, we did `init`.

**Common Pitfalls for AI agents and how we avoid them:** -
"Hallucinating" nonexistent classes or properties: By generating most
skeleton code via known templates (Initializr, Vite, etc.), the agent
works on a solid base. We also explicitly list dependencies and ensure
they are installed, so the agent doesn't try to use a library that isn't
there. - Off-by-one or minor logic bugs: We'll instruct agents to run
tests where possible. For example, after writing a date validation
function, run `npm test` to see if the test we wrote passes. Agents
(especially autonomous ones) can use tests as a guide if we include
them. - Misunderstanding the domain: Since we will provide policy
context in prompts (and AGENTS.md might contain a summary of Sinsay
rules), the agent coding the solution has some domain knowledge. But for
any detailed policy logic, it might be safer for us to implement or
clearly specify rather than expecting the AI to guess Polish
regulations. We've minimized this by focusing on technical aspects in
research and will feed the policy text ourselves to the GPT-4 when it's
making decisions rather than hard-coding much in the code. So the agent
coding just needs to ensure the pipeline is there.

**AGENTS.md Use:** As per the new
standard[\[48\]](https://agents.md/#:~:text=Think%20of%20AGENTS,agents%20work%20on%20your%20project),
we'll maintain two AGENTS.md (one for backend, one for frontend as
subproject guides). This file is essentially a "cheat sheet" for any AI
agent contributing to the code: - It will list *Setup commands* (so the
agent knows how to run the project, run tests, etc. without asking). -
*Code style guidelines* (e.g., "Use 4 spaces indentation for Java", "Use
single quotes and semi-colons consistently for TS" if such are our
standards). - *Conventions* like "Controller class names end with
Controller and are in package X", "All state is managed via React Hook
Form or useState, do not introduce Redux unless asked". - *Git
guidelines* if any (like conventional commits, though for PoC we might
not enforce that). - Very importantly, *"Boundaries"*: For instance,
instruct the AI not to modify certain files. E.g., we might say "Do not
edit `pom.xml` dependencies without approval, as they are carefully
chosen" to prevent an agent from randomly adding conflicting
dependencies. Or "Do not change the OpenAI prompt format unless
necessary -- coordinate with team for prompt updates". Boundaries ensure
the AI doesn't drift from
requirements[\[50\]](https://www.reddit.com/r/ChatGPTCoding/comments/1p6m036/any_tips_and_tricks_for_agentsmd/#:~:text=Any%20tips%20and%20tricks%20for,to%20burn%20a%20lot).

By systematically setting up these guidelines and using automation, our
development approach aligns with *AI pair-programming*. We reduce grunt
work via CLI scaffolds, guiding the AI with clear examples and
constraints, and thereby harness its speed while keeping the codebase
clean and consistent.

### 3.7 OpenAI Integration Patterns (Vision + Text)

Integrating GPT-4 with vision (often referenced as GPT-4V or GPT-4
"Vision" model) into our system presents unique challenges and
opportunities. We need to construct prompts and handle responses in a
way that yields reliable, policy-compliant results. Key considerations:

**Library Choice & Usage:** As mentioned, we'll likely use Spring's
OpenAI integration or the official SDK: - With **Spring AI's
OpenAiChatModel**, sending images and text is
straightforward[\[17\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=var%20imageResource%20%3D%20new%20ClassPathResource%28).
We choose the model `gpt-4-vision` (or its identifier, possibly
`gpt-4-1106` or similar as per OpenAI's API changes). Spring AI's
abstraction allows setting the model name in options. If not using
Spring AI, the official client can call the `/v1/chat/completions`
endpoint with `model: "gpt-4-vision"` and a message containing an image.
We'll need to send the image as a base64 string or form-data -- the API
supports images as part of the JSON payload (base64 encoded) for vision
models[\[17\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=var%20imageResource%20%3D%20new%20ClassPathResource%28). -
We must handle the possibility that our model might require special
provisioning (GPT-4V was in limited beta). For PoC, assume we have
access.

**Prompt Engineering for Vision+Text:** The prompt to GPT-4 should
explicitly request what we need: - We will instruct the model to
**identify defects in the image** and categorize them (tear, stain,
etc.) because we have a known list of common defects (tears, stains,
broken zipper, holes, misprint, discoloration, seam issues, etc.)
gleaned from garment QA
sources[\[10\]](https://www.v-trust.com/en/blog/most-common-defects-in-garments#:~:text=2)[\[11\]](https://www.v-trust.com/en/blog/most-common-defects-in-garments#:~:text=8).
We can embed that list in the prompt or at least ensure the model knows
to look for those. For example: *"When analyzing the image, check for:
tears,
holes[\[11\]](https://www.v-trust.com/en/blog/most-common-defects-in-garments#:~:text=8),
stains[\[10\]](https://www.v-trust.com/en/blog/most-common-defects-in-garments#:~:text=2),
discoloration, broken components (zippers, buttons), or other material
defects. The item is a textile/clothing."* This guides the vision part
of the model. - We also include the **policy rules**: e.g., *"If the
defect is clearly a manufacturing fault (like a stitching error or
detached sole) and within warranty, we approve the claim. If it's
wear-and-tear or caused by misuse, we politely explain it's not
covered."* Essentially the "brain" of decision-making lies in the prompt
instructions plus the model's own reasoning. - We might use the **OpenAI
function calling or JSON mode** for structured
output[\[51\]](https://community.openai.com/t/form-filling-chatbot-ideas/183868#:~:text=%7B%20,Name%20of%20the%20hotel)[\[52\]](https://community.openai.com/t/form-filling-chatbot-ideas/183868#:~:text=,).
For instance, define a function schema "classify_defect" with fields: {
defectType: string, decision: "approve"/"deny", reason: string }. Then
ask GPT-4 to output a JSON per that schema. This can reduce the risk of
the AI giving a long narrative when we need a decision. However, since
part of the user experience is a conversational explanation, we may let
the AI produce a friendly answer and then parse it for the outcome. A
compromise: instruct the AI: *"First line of answer: 'APPROVED' or
'DENIED' (the decision). Then explanation in a polite tone."* The
backend can then read the first line to take any programmatic action (if
needed). - Given this is a PoC, we might not automate the final decision
beyond telling the user. So parsing may not be strictly needed, but we
mention it as a pattern in case we needed to trigger an automated refund
vs sending to manual review. The OpenAI API now even has a JSON mode for
GPT-4 (1106) that we can try (by setting
`response_format: { type: "json" }` in the
request)[\[53\]](https://community.openai.com/t/it-appears-that-the-officially-recommended-java-lib-library-is-no-longer-being-maintained/716140#:~:text=It%20appears%20that%20the%20officially,2%2C%201556%2C%20November%2015%2C)[\[54\]](https://stackoverflow.com/questions/77434808/openai-api-how-do-i-enable-json-mode-using-the-gpt-4-vision-preview-model#:~:text=OpenAI%20API%3A%20How%20do%20I,1106%2C%20you%20can%20set),
but that could be unstable. Using function calling is a safer way:
define a function for decision and let GPT-4 "call" it with arguments --
the Spring AI library supports function calling if
needed[\[55\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=You%20can%20register%20custom%20Java,Read%20more%20about)[\[56\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=This%20will%20create%20an%20,chat%20model%20for%20text%20generations).

**Context Management:** We ensure the model gets all needed context each
turn: - The system message will persist with policy info and any
constraints (like "only output in Polish if user speaks Polish" -- but
presumably we handle in English for dev, maybe later we localize). - The
conversation history will include user's initial info and image and
prior Q&A, so GPT-4 stays grounded. But note GPT-4 has a token limit
(\~8K or 32K). Our dialogues are short, so no issue. - We should avoid
sending extremely long texts each time (like the entire policy text from
the website, which could be lengthy). Better to summarize the relevant
rules ourselves and feed the summary. The references given would likely
be distilled to something like: "30 days for returns, warranty covers
manufacturing defects up to 2 years, normal wear is not covered, etc.",
a few sentences.

**Moderation & Safety:** By funneling interactions through our backend,
we can apply OpenAI's Moderation API on user inputs (especially image
content -- though our scope is images of products, which is fine). We
likely don't need heavy moderation since users will presumably upload
product photos. But if an image is not of a product, GPT-4 might start
describing it unrelatedly -- we can mitigate by prompt: "Focus only on
the product defect. If the image is unrelated or inappropriate, respond
with a polite request for a valid product image." Also ensure the
assistant does not output anything toxic -- however, since our domain is
narrow, risk is low. Still, we might enable OpenAI's own moderation on
text responses.

**Error Handling for AI calls:** If OpenAI API call fails (network issue
or rate-limit), our backend should catch that. The controller can return
a message to the UI: "Sorry, I'm having trouble analyzing right now.
Please try again." and log the error. We can also implement retries with
exponential backoff using Spring AI's config (max 3 attempts for
example)[\[13\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=Property%20Description%20Default)
so transient failures (like a momentary timeout) are handled gracefully
without user even noticing.

**Testing OpenAI in Dev:** Because GPT-4 calls cost money and require
connectivity, for dev/test we can stub the OpenAIService. For example,
if `spring.profiles.active=dev`, we can have the service return a canned
response ("This looks like a tear along a seam -- a manufacturing
defect. We will approve your complaint.") when given a specific test
image. This allows us to test the whole flow end-to-end without actually
calling OpenAI every time. In production (or a staging environment), we
enable the real calls.

**Differentiating Regular Return vs Complaint in prompts:** If it's a
*return (no defect)* scenario, we actually might not need GPT analysis
at all -- it's straightforward: check date, if ≤30 days and tags intact
(the user confirmed), then approve return. We could have the backend
just return a scripted response ("Your return is within 30 days, please
visit a Sinsay store or mail the item\..." referencing policy). However,
the goal is AI-powered, so perhaps we *still* involve GPT-4 to handle
any follow-up questions or to simulate an agent interaction ("Sure, I
see you want to return the item. Is it unused with tags? (the user would
say yes) Great, here's how to proceed\..."). But we must be careful: if
the user asks something not in our initial flow, GPT-4 should be able to
answer (the policy context helps with that).

For a return flow, we can prompt GPT-4 with the context that it's a
normal return and instruct it to just reiterate return instructions from
policy. This is a bit of overkill for logic we know, but it demonstrates
consistency and allows natural conversation ("Can I return it to any
store?" -- GPT-4 can answer yes, as per policy, if that's true). So
we'll integrate GPT-4 in both flows, but the complexity is with
complaints.

**Response Validation:** After we get GPT's reply, we should validate it
if critical: - Ensure it actually addresses whether the return is
approved/denied. If GPT-4 gave an irrelevant answer (unlikely with good
prompting), we might call it again or fallback to a default message
("We'll need to review this further\..."). But assuming our instructions
are strong, GPT will follow them. - If using function calling, the
function result itself can be validated (e.g., defectType is one of the
expected strings, decision is approve/deny). - We also might filter out
any content we don't want the user to see (shouldn't happen if our
system prompt restricts the AI to a support persona -- it shouldn't
output API keys or such).

**Logging and Audit:** For an AI-driven system, it's wise to log the
conversation (at least internally) for later review, especially if a
decision is contested. We should store: form data, the final decision,
and perhaps the conversation transcript. This can be done in-memory or a
simple database if it were prod. For PoC, maybe just log to console or
keep last session in memory.

In summary, the OpenAI integration will follow this pattern: 1.
Construct initial prompt with policy and context. 2. For each user
interaction (text or image), include it as user message and call GPT-4.
3. Get GPT-4 response, possibly structured with a decision. 4. Use GPT's
response to inform the user and drive the next step (or final outcome).
5. Use robust error handling around those API calls (with retries and
graceful degradation).

By leveraging GPT-4's vision capabilities and instructing it with
Sinsay's policies and defect categories, our system should accurately
distinguish **legitimate defects** (e.g., manufacturing fault: approve
free
return[\[12\]](https://zwroty.globkurier.pl/pl/sinsay-zwrot-towaru-kompletny-przewodnik/#:~:text=Je%C5%9Bli%20jednak%20oka%C5%BCe%20si%C4%99%2C%20%C5%BCe,commerce.%20Znajomo%C5%9B%C4%87%20tych%20zasad))
from **non-covered issues** (e.g., normal wear: politely refuse). This
AI judgment is encapsulated in our prompt engineering rather than
hardcoded `if/else` logic, making the solution flexible. For example, if
policies change (e.g., extend returns to 45 days), we just update the
prompt or config, not the code -- a powerful advantage of this approach.

------------------------------------------------------------------------

## Tool Recommendations and Installation Steps

To implement the above system, we will use a variety of frameworks and
libraries. Below is a list of recommended tools for backend and
frontend, along with their purpose, installation instructions (with CLI
commands), and rationale (especially focusing on AI development
friendliness):

### 4.1 **Backend (Spring Boot) Tools/Libraries**

- **Spring Boot 3.5.9 (Java 21)** -- *Framework for the backend.* We use
  Spring Boot for rapid setup of REST APIs, dependency injection, and
  production-ready features. It simplifies connecting to OpenAI (via
  Spring AI) and serving static content. **Installation:** We initialize
  via Spring Initializr:


- curl https://start.spring.io/starter.zip \
        -d groupId=com.sinsay \
        -d artifactId=returns-backend \
        -d dependencies=web,lombok,validation \
        -d javaVersion=21 -d bootVersion=3.5.9 -o backend.zip

  (This will produce a project with Spring Web (for REST) and Validation
  and
  Lombok.)[\[44\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Starting%20with%20a%20Simple%20Backend).
  Unzip `backend.zip` and open it in IntelliJ.\
  **Justification:** Spring Boot provides **convention over
  configuration**, so an AI agent doesn't need to write a lot of
  boilerplate (e.g., Tomcat setup, JSON mappers -- all auto-configured
  with Web starter). It also has a huge community, meaning any AI
  suggestions are likely to align with known patterns.


- **Spring Boot DevTools** -- *Dev-only dependency for hot reload.* Auto
  reloads the app on code changes. **Installation:** Add to `pom.xml`
  (inside `<dependencies>`):


- <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
      </dependency>

  (No CLI install since it's just a dependency addition).\
  **Justification:** Improves developer experience (including for AI
  pair-programming) by removing the need to manually restart the app on
  changes. IntelliJ auto-reloads on save if DevTools is present. This
  speeds up the tweak/test loop for prompting and form changes.


- **Lombok** -- *Code generator for getters/setters, etc.* Already
  included in initializr above.\
  **Installation:** Confirm the dependency:


- <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.28</version> <!-- version that matches Boot 3.5 -->
        <scope>provided</scope>
      </dependency>

  Also, **IntelliJ plugin:** Install "Lombok" plugin and enable
  annotation processing
  (`Settings > Build > Compiler > Annotation Processors > Enable`[\[36\]](https://stackoverflow.com/questions/24006937/lombok-annotations-do-not-compile-under-intellij-idea#:~:text=Overflow%20stackoverflow,Enable%20annotation%20processing)).\
  **Justification:** Lombok reduces boilerplate (constructors, getters)
  -- an AI agent doesn't have to repeatedly write or update these,
  avoiding errors and saving time. Using `@Data` or `@Getter` on DTOs
  means if we add a field, we don't need to adjust the rest of the code
  (the AI or human can focus on logic, not accessor methods).


- **Spring Validation (Hibernate Validator)** -- *For annotating and
  enforcing field rules.* Included as `spring-boot-starter-validation`
  from Initializr (under the hood this adds Hibernate Validator).\
  **Installation:** (Already in dependencies from initializr if
  `validation` selected). Otherwise:


- <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
      </dependency>

  **Justification:** Allows using `@Valid`, `@NotNull`, etc., on our
  form DTO and in controller to automatically handle error
  responses[\[57\]](https://dev.to/gianfcop98/spring-boot-and-validation-a-complete-guide-with-valid-and-validated-471p#:~:text=2)[\[31\]](https://dev.to/gianfcop98/spring-boot-and-validation-a-complete-guide-with-valid-and-validated-471p#:~:text=Spring%20Boot%E2%80%99s%20validation%20system%20supports,380%20annotations).
  This is robust and saves writing manual checks. AI agents often know
  how to use these annotations, making it easier for them to implement
  constraints.


- **OpenAI Java SDK (com.openai:openai-java)** -- *Official Java client
  for OpenAI API.*\
  **Installation:** Add to `pom.xml`:
  `xml <dependency> <groupId>com.openai</groupId> <artifactId>openai-java</artifactId> <version>4.15.0</version> </dependency>`[\[33\]](https://github.com/openai/openai-java#:~:text=implementation%28%22com.openai%3Aopenai).\
  **Justification:** Provides strongly-typed API calls to OpenAI
  (ChatCompletion, etc.), reducing error-prone manual HTTP code. It
  supports setting API keys via env vars and handles JSON, making it
  easier for an AI agent to call OpenAI correctly. Also, it's the
  library Spring AI uses under the hood, so either we use it directly or
  via Spring AI.

- **Spring AI (Spring Boot Starter for OpenAI)** -- *Simplifies OpenAI
  integration.* (Optional, but recommended for future-proofing and
  ease).\
  **Installation:** Add Spring AI BOM and starter:


- <!-- In <dependencyManagement> section: -->
      <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-dependencies</artifactId>
        <version>0.2.0</version> <!-- example version -->
        <type>pom</type>
        <scope>import</scope>
      </dependency>

  then the dependency:
  `xml <dependency> <groupId>org.springframework.ai</groupId> <artifactId>spring-ai-starter-model-openai</artifactId> <version>0.2.0</version> </dependency>`[\[7\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=Gradle).\
  **Justification:** This auto-configures an `OpenAiApi` and
  `OpenAiChatModel` bean for us, using properties like
  `spring.ai.openai.api-key`. It supports multimodal messages easily
  with the `Media`
  class[\[17\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=var%20imageResource%20%3D%20new%20ClassPathResource%28)
  and has built-in retry
  logic[\[13\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=Property%20Description%20Default).
  For an AI agent developer, using this means less custom code -- they
  just inject the `OpenAiChatModel` and call `.call(prompt)` to get a
  response. It abstracts the HTTP and JSON complexities. (If we find
  Spring AI too bleeding-edge, we can stick with the raw OpenAI SDK, but
  we'll evaluate.)


- **JUnit 5 (JUnit Jupiter)** -- *Testing framework.* Already part of
  Spring Boot starter test (which is added by default in initializr). We
  will ensure it's there:


- <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
      </dependency>

  This includes JUnit, Mockito, AssertJ, etc.\
  **Justification:** For writing unit and integration tests in Java.
  It's the standard, and AI agents are familiar with writing JUnit 5
  tests (e.g., `@Test` annotation usage). We'll use it to verify our
  controllers and services (possibly using MockMvc for web tests).
  Having tests also helps AI agents verify their changes didn't break
  functionality (if integrated into an AI workflow, tests passing is a
  good sign).


- **Testcontainers (optional)** -- Not strictly needed since we have no
  external DB in PoC. We skip it now. If we had, we'd recommend for
  integration tests with e.g. a Postgres container.

- **Maven** -- We'll use Maven as the build tool (since initializr by
  default gave a pom.xml). **Justification:** Maven is straightforward
  and widely recognized by AI tools. Commands like
  `./mvnw spring-boot:run` to run the app, `./mvnw test` to run tests,
  are simple. Gradle could be used too, but Maven's lifecycle is perhaps
  more transparent for an AI agent (and we avoid any Kotlin DSL
  confusion for now).

### 4.2 **Frontend (React) Tools/Libraries**

- **Node.js 18+ & npm** -- *Runtime and package manager.* Ensure Node 18
  or 20 is installed (for Vite and many packages, Node 18 LTS is
  stable).\
  **Installation:** (On dev machine or CI, install Node.js from
  nodejs.org or package manager). Verify with `node -v` and `npm -v`.\
  **Justification:** Needed to run our React 19 app and related tooling.
  Node 18+ is recommended for compatibility and performance.

- **Vite 4 (Create Vite)** -- *Build tool / scaffolding for React.* We
  choose Vite for its fast dev server and simple configuration (over
  CRA).\
  **Installation/Scaffold:**


- npm create vite@latest frontend -- --template react-ts

  This CLI (as used earlier) will create a `frontend` directory with a
  React 18 (TypeScript)
  project[\[45\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=cd%20src%2Fmain%20npm%20create%20vite%40latest,ts).
  We can later bump React to 19 if needed (assuming React 19 is released
  in this scenario). Actually, by 2026 React 19 might be out; if our
  template is React 18, we can npm install react@19.0.0 if available.
  (For now, assume React 18.x since that's current. It's easy to upgrade
  when 19 is available as it's likely backward compatible.)\
  **Justification:** Vite is extremely fast, supports modern ES modules,
  and is framework-agnostic. It's simpler than Next.js since we don't
  need SSR. Also, *for an AI agent*, Vite config is minimal, meaning
  less chance to get lost in configuration. The agent can focus on React
  code. Running `npm run dev` gives hot-reload dev server, which is
  great for quick feedback during development (AI can utilize that to
  see UI changes perhaps).


- **React 18/19 with TypeScript** -- *Frontend library.* Already
  included by Vite template above.\
  **Justification:** React is required. We use TypeScript to catch
  errors early and to make code more self-documenting (also many Shadcn
  UI components come with TS types). TypeScript is helpful to AI agents
  because it provides autocompletion and prevents certain mistakes (the
  agent can rely on types to some extent). We will ensure `strict: true`
  in tsconfig for best practices.

- **Tailwind CSS 4** -- *Utility-first CSS framework.* We will style
  mostly via Shadcn UI which uses Tailwind under the hood.\
  **Installation:** After scaffold:


- cd frontend
      npm install -D tailwindcss@^4 postcss autoprefixer
      npx tailwindcss init -p

  This creates `tailwind.config.js` and
  `postcss.config.js`[\[58\]](https://v3.tailwindcss.com/docs/guides/vite#:~:text=Start%20by%20creating%20a%20new,npm%20create%20vite%40latest).
  Then in `tailwind.config.js`, set
  `content: ["./index.html","./src/**/*.{js,ts,jsx,tsx}"]`. In
  `src/index.css`, include:

      @tailwind base;
      @tailwind components;
      @tailwind utilities;

  (Shadcn UI may provide additional styles to include, like
  `@tailwind base` includes some normalize.css, and Shadcn might require
  setting up fonts or theme, but mostly it's default).\
  **Justification:** Tailwind lets us rapidly style components with
  utility classes instead of writing a lot of custom CSS. This is
  AI-friendly because the agent can apply known class names (e.g.,
  `p-4 bg-gray-100 rounded`) rather than having to create new CSS rules
  (which might conflict). It ensures consistency. Shadcn UI components
  come pre-styled with Tailwind classes, so using Tailwind is natural
  here.


- **Shadcn/UI Components** -- *Pre-built Radix UI + Tailwind
  components.* Specifically, we'll use shadcn's form elements, button,
  card, etc.\
  **Installation:** Shadcn UI is a bit unique: it's not a package you
  install; you copy the component code into your project (so you can
  fully control and customize). They have a CLI for Next.js, but we can
  still use it by specifying output paths. For our PoC:

- We can go to **ui.shadcn.com** and select the components we need
  (Form, Input, Button, Dialog, etc.), then either copy the code or run
  the CLI.

- Possibly use CLI:


- npx shadcn-ui@latest init
      npx shadcn-ui add button input form dialog card

  We might need to adjust for a Vite project (the CLI expects a Next
  directory structure by default). Alternatively, use their templates
  manually.\
  **Justification:** Shadcn/UI gives us ready-made, accessible
  components with consistent design (based on Tailwind). This saves a
  ton of UI coding -- an AI agent can just use `<Button>` or `<Input>`
  with appropriate props instead of crafting from scratch. It also
  ensures our app looks modern without a dedicated designer. Each Shadcn
  component is plain React + Tailwind, easy to tweak if needed.


- **React Hook Form 7** -- *Form state management and validation.*\
  **Installation:**


- npm install react-hook-form @hookform/resolvers zod

  (We include `@hookform/resolvers` to integrate Zod, and `zod` for
  schema
  definition.)[\[37\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=React%20Hook%20Form%20validates%20your,hook)\
  **Justification:** This library drastically simplifies form handling
  -- minimal re-renders and easy integration with
  controlled/uncontrolled inputs. It's lightweight (\~9KB). It's
  well-known, so AI agents are familiar with usage patterns. By using
  RHF, we avoid manual form state code (which is repetitive and
  bug-prone). RHF's resolver allows using **Zod** for declarative
  validation rules, which aligns with our approach of mirroring backend
  validation on the front.


- **Zod** -- *Runtime schema validation for TypeScript.* (Installed
  above.)\
  **Justification:** We use it to define the form schema in one place.
  The AI agent can use the schema both for RHF and perhaps to generate
  TypeScript types for form data (`z.infer<typeof schema>`). This single
  source of truth reduces errors. Zod is developer-friendly and a common
  choice in modern React apps -- likely within the knowledge of AI
  assistants.

- **Axios or Fetch API** -- *HTTP client for calling backend.* We can
  actually use the built-in `fetch` in modern browsers, which might
  suffice. But some devs prefer Axios for convenience (automatic JSON
  parse, better error handling).\
  **Installation (if Axios):**


- npm install axios

  **Justification:** Axios can simplify calls like:

      axios.post('/api/intake', data).then(res=>...).catch(err=>...);

  and can set base URLs easily. However, using fetch isn't hard either,
  and since we might stream responses (for chat), using the Fetch API's
  streaming capabilities might be needed. We might stick to `fetch` for
  simplicity (no extra dependency). Fetch with `EventSource` for SSE or
  `ReadableStream` can handle streams.


- **Assistant UI (optional)** -- *AI chat UI library.* If we decide to
  use the `assistant-ui` library: **Installation:**


- npm install @assistant-ui/react

  and possibly run `npx assistant-ui init` to configure (it might set up
  some context providers).\
  **Justification:** It gives us high-level components for chat (message
  list, input box with voice support, etc.) and handles streaming
  tokens, markdown rendering, and even attachments, out of the
  box[\[59\]](https://github.com/assistant-ui/assistant-ui#:~:text=The%20UX%20of%20ChatGPT%20in,your%20React%20app)[\[23\]](https://github.com/assistant-ui/assistant-ui#:~:text=,theme%20you%20can%20fully%20customize).
  This dramatically cuts down the code we need to write for the chat
  interface, and it's highly customizable via Tailwind so it will match
  our Shadcn styling. For an AI agent developer, leveraging this means
  they don't have to reinvent the wheel of chat UI -- they just plug in
  our API calls. Since assistant-ui is built with an API-agnostic
  approach, it fits our Spring Boot backend
  scenario[\[9\]](https://github.com/assistant-ui/assistant-ui#:~:text=pixel%20,easy%20extension%20to%20custom%20APIs).


- **React Icons (optional)** or Lucide icons -- *Icon library.* The
  Shadcn UI uses Lucide icons under the hood for some components. If we
  need icons (e.g., a paperclip icon on upload button), we can install:


- npm install lucide-react

  or use `react-icons`.\
  **Justification:** Provides a set of SVG icons as React components.
  This is developer-friendly and easier for an AI to use than hunting
  down SVG paths. For example, `<LucideCamera className="mr-2"/>` to
  show a camera icon.


- **Vitest + Testing Library** -- *Unit testing for React.* Vite comes
  with Vitest config easy to set up, which is similar to Jest but
  faster. We also use \@testing-library/react for DOM testing.\
  **Installation:**


- npm install -D vitest @vitest/ui jsdom @testing-library/react @testing-library/user-event @testing-library/jest-dom

  and update `vite.config.ts` for test config if needed.\
  **Justification:** Ensures our critical interactions (form validation,
  API calls, chat UI rendering) work as expected. Testing Library helps
  simulate user events (like typing into form, clicking upload). This
  not only ensures quality but also can be run by AI agents to verify
  that modifications don't break functionality. (An advanced AI agent
  could run `npm run test` after changes to see if all tests pass).


- **ESLint and Prettier (optional but good)** -- *Linting and
  Formatting.*\
  **Installation:**


- npm install -D eslint eslint-config-react-app prettier

  (We might extend create-react-app's config or use a popular preset
  like Airbnb or use Next's since Shadcn might assume some conventions).
  Also add a Prettier config (maybe enforce trailing commas, etc.).\
  **Justification:** Maintains code style consistency. For AI agents,
  having an ESLint config means the agent can be guided to follow those
  rules (some AI dev tools auto-read ESLint config to avoid introducing
  lint errors). Prettier ensures uniform formatting, which is helpful
  when multiple people/agents contribute. This reduces noise in diffs
  and focuses the AI on actual logic changes.

**IntelliJ IDEA Plugins & Config for Spring Boot:**\
- **IntelliJ Spring Assistant/Support:** IntelliJ (Ultimate) has
built-in Spring Boot support. Make sure the Spring plug-in is enabled so
it recognizes `application.properties` and such. - **Lombok plugin:** as
mentioned, required to avoid errors with Lombok annotations. - **REST
Client or cURL**: IntelliJ can test APIs via its HTTP client, which
could help during development to manually verify endpoints. Not critical
for AI, but nice for human devs. - **UI/UX:** If using VSCode, we might
mention relevant extensions (ESLint, Tailwind IntelliSense) but since
the user specifically said IntelliJ, we'll stick to that environment
primarily for backend and maybe they use WebStorm or VSCode for front.

**CLI Summary for Setup:** We will provide a consolidated list of
commands in the CLI Reference section (#10), but as a quick
narrative: 1. `curl ... start.spring.io ... -o backend.zip` (or use the
Spring Initializr web UI through IntelliJ new project wizard). 2.
`unzip backend.zip -d backend && cd backend` -- then maybe
`./mvnw package` to ensure it builds. 3.
`npm create vite@latest frontend -- --template react-ts`. 4.
`cd frontend && npm install` (install base deps). 5.
`npm install -D tailwindcss postcss autoprefixer && npx tailwindcss init -p`.
6. Edit tailwind config/content and add Tailwind directives in
index.css. 7. `npm install react-hook-form @hookform/resolvers zod`. 8.
`npm install lucide-react @assistant-ui/react` (if using assistant-ui).
9. Possibly run `npx shadcn-ui init` and `npx shadcn-ui add component`
for each needed component, or manually copy them. 10. `npm run dev` and
`./mvnw spring-boot:run` to verify everything compiles and basic pages
load.

Each installation we justified is aimed at reducing the amount of custom
code and configuration needed, thereby speeding up development and
reducing potential errors (especially those an AI might make if writing
boilerplate). By using these well-documented tools, we also provide the
AI agent with abundant references and predictable patterns to
follow[\[60\]](https://agents.md/#:~:text=README,project%20descriptions%2C%20and%20contribution%20guidelines).
This increases the chances that the AI's suggestions align with best
practices rather than something obscure.

------------------------------------------------------------------------

## Architecture Decision: Monorepo Hosting Strategy

**Decision:** **We will host the React frontend** within **the Spring
Boot application for the Proof of Concept**, using a monorepo and a
single combined deployment.

After evaluating both approaches (embedding vs. separate deployments),
we concluded that for a PoC and initial development, bundling the SPA
into the Spring Boot JAR is most pragmatic. This effectively treats the
whole system as one application (UI + API together), simplifying the
dev/test cycle and deployment.

**Rationale / Pros (Embedded SPA in Spring Boot):** - **Single
Deployment Artifact:** We produce one JAR (or Docker image) containing
everything. This means we only need to run one service. For a
small-scale PoC, this is easier to manage and demonstrate. As Jessy's
tutorial noted, bundling front and back into one jar "simplifies both
dev and ops" for simple
cases[\[2\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Learn%20how%20to%20bundle%20your,simplifying%20both%20dev%20and%20ops).
We avoid syncing separate deployments. - **No CORS issues:** The React
app will be served from the same origin as the API (e.g.,
`localhost:8080`). All AJAX calls to `/api/...` are same-origin, so we
can avoid dealing with Cross-Origin Resource Sharing headers and
configuration. This reduces complexity in both code and
troubleshooting. - **Coordinated Releases:** In a monolithic setup, the
front and backend are always in lockstep. A certain API change and the
corresponding UI change are released together, preventing version
mismatch. Given our agent flows rely on both front and back logic, this
is safer
initially[\[24\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=It%20isn%E2%80%99t%20a%20monorepo%20if,the%20same%20repo%20by%20definition). -
**Monorepo Developer Experience:** With one repo and one build,
developers (or AI agents) can run the whole system easily. Code
navigation is simple -- an engineer (or AI tool) can search the repo and
see both front and back code, which is helpful for an AI that might
reason about end-to-end functionality. (This ties into our AI dev
approach -- e.g. an agent can see in backend that there's `/api/chat`
and then open the React code to ensure it calls that.) - **Atomic
commits:** One repository with both projects means a single commit or PR
can span changes in both, which makes code review and history easier.
For example, if we add a new field in the form and also adjust the
backend DTO, it's one commit -- easier to track and revert if
needed[\[24\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=It%20isn%E2%80%99t%20a%20monorepo%20if,the%20same%20repo%20by%20definition). -
**Simpler Infrastructure for Demo:** We can host the JAR on a cloud VM
or Heroku, etc., and the UI is available at the same URL as the API. No
need to set up separate domain or static hosting bucket for the
frontend. This is especially useful for a quick demo to stakeholders.

**Cons & Mitigations (Embedded approach):** - *Larger artifact:* The JAR
will include all static files (JS bundle might be a few MB). This is not
a big issue for us. Build time slightly increases when including
frontend build in Maven, but that's acceptable in PoC. - *Tight coupling
of releases:* We can't deploy a frontend fix without deploying backend
(and vice versa). For now, that's fine. The team is small and features
likely involve both anyway. In the future, if different teams handle
front/back, separate might be beneficial, but we're optimizing for
now. - *Memory usage:* Serving static files from Spring Boot is light;
it just uses classpath resources. There's no Node server overhead. So
not really a con except that we have to ensure cache headers etc.
(Spring can serve them with far-future cache if we fingerprint
assets). - *Build complexity:* We need to integrate the frontend build
into the Maven build process. E.g., a Maven plugin that runs
`npm install && npm run build` during the package phase, then copies
`dist/` files into `src/main/resources/static/`. This adds some config
(we can use the `frontend-maven-plugin` or simply add an Ant task).
However, for PoC, we might even build the React app manually and drop
files into static/. Or instruct AI dev to do so. It's not too complex --
Jessy's article shows adding it to Maven
lifecycle[\[27\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=,Conclusion).

Alternatively, we consider **separate deployments**: - **Pros
(Separate):** Independent scaling (e.g., serve static files via CDN),
can deploy frontend immediately for content changes, the backend API
could be reused by other clients (mobile). Also, if using frameworks
like Next.js with server-side rendering, separate is natural -- but we
are not doing SSR. - **Cons (Separate):** Would need to configure CORS
on Spring Boot to allow the React dev server domain in dev and whatever
domain in
prod[\[61\]](https://www.reddit.com/r/javahelp/comments/i5pgdb/spring_boot_backendreact_frontend_is_it/#:~:text=,has%20not%20caught%20up).
Also need to manage two deployments -- e.g., host the static files on
Netlify and API on Heroku. That introduces complexity for a prototype.
Also local development would require running two servers (we can script
it, but still overhead). Additionally, as noted by a Redditor, if it's
one project, splitting unnecessarily means coordinating branches and
versions across two repos which is more overhead than benefit at this
stage[\[24\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=It%20isn%E2%80%99t%20a%20monorepo%20if,the%20same%20repo%20by%20definition).

Given these points, we opt to **keep it simple now** by embedding. If in
the future Sinsay wants to scale this up (say, heavy traffic on the AI
API, or multiple frontends -- web, mobile -- consuming it), we can
refactor to separate the backend into an independent service. Monorepo
doesn't preclude that -- we could still maintain a monorepo but deploy
them separately with CI.

For now, our monorepo will produce: - `returns-backend.jar` which
contains `static/index.html`, `static/assets/...` (JS, CSS) and all API
endpoints. - Running that JAR launches the Spring Boot app; when users
hit `http://server:8080/`, Spring will serve `index.html` (we'll
configure resource mapping so that any unknown path goes to index.html
for client-side routing). The React app then bootstraps, and calls e.g.
`fetch('/api/intake')` which hits the same server's endpoints.

**Monorepo Structure Best Practices:** According to a guide on managing
front/back in
monorepo[\[62\]](https://graphite.com/guides/monorepo-frontend-backend-best-practices#:~:text=Example%20structure%20for%20frontend%20and,backend),
we'll keep them in separate subdirectories (we already decided on
`backend/` and `frontend/`). Optionally, a `shared/` if we had any
shared code (like if we wanted to share model classes or type
definitions -- for instance, we could share TypeScript types for the API
via a JSON schema or d.ts file -- but that might be overkill now).

We should also consider how to handle **pull requests** in monorepo:
It's good to keep PRs focused (like one feature at a time, touching both
front and back if
needed)[\[63\]](https://graphite.com/guides/monorepo-frontend-backend-best-practices#:~:text=Best%20practices%20for%20pull%20requests,in%20a%20monorepo).
For now, since likely one or two devs on this, not a big concern.

**Conclusion:** We recommend the **monorepo with embedded UI approach**
for this PoC and possibly initial production. It yields a unified
application that is easier to develop and aligns with the "single
project" nature of this
system[\[24\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=It%20isn%E2%80%99t%20a%20monorepo%20if,the%20same%20repo%20by%20definition).
We accept the coupling in exchange for simplicity.

In the future, if the project grows: - The monorepo can remain, but we
might separate deployment by adjusting CI to build and deploy the React
app to a CDN (and change Spring Boot to serve only APIs). Since by then
we'll have solved CORS and config management, an AI agent can help with
that migration too, guided by our documentation and tests. - For now, we
will implement the build such that the **React app is built and placed
in** `src/main/resources/static`. Spring Boot will automatically serve
files from there at `/`. We'll also ensure routing works by perhaps
adding a controller or resource handler to forward unknown routes to
`/index.html` (so client-side routing doesn't break on refresh).

This decision is justified by the context of a small, fast-moving
project where integrated development trumps modular deployment. Many
successful apps start this way (monolithic), and can split later when
needed (the concept of starting monolithic and later breaking into
microservices if necessary).

To quote an experienced dev: *"If it's the same project, same
repository\... \[and one
service\]"*[\[24\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=It%20isn%E2%80%99t%20a%20monorepo%20if,the%20same%20repo%20by%20definition)
-- we embrace that for now. The overhead of multi-repo or multi-deploy
is not warranted yet for Sinsay's AI returns assistant in its infancy.

------------------------------------------------------------------------

## AGENTS.md for Spring Boot (Backend Guidelines)

**Project Overview:** This Spring Boot project (`backend/` directory) is
the server-side of the Sinsay Returns AI system. It exposes RESTful
endpoints for the returns intake form and AI chat, and integrates with
OpenAI's API (GPT-4 Vision) to analyze images and texts. The application
uses Java 21 and Spring Boot 3.5. Key frameworks: Spring Web (for REST
controllers), Spring Validation, Lombok, and an OpenAI API client. The
code follows a layered architecture (Controller -\> Service -\> (OpenAI)
-\> etc.), and is designed with clarity and maintainability in mind.

As an AI coding agent working on this backend, please adhere to the
following guidelines and conventions:

### Setup Commands

- **Build the project:** Use Maven Wrapper -- run
  `./mvnw clean package`. This will compile the code and run all tests.
  Ensure the build is passing before committing changes.
- **Run the application:** Use Spring Boot plugin --
  `./mvnw spring-boot:run`. The app starts on port 8080 by default.
  Alternatively, run the JAR:
  `java -jar target/returns-backend-0.0.1-SNAPSHOT.jar`.
- **Run tests:** `./mvnw test`. Make sure to add or update tests when
  you implement new features or fix bugs.
- **Install new dependencies:** Add to `pom.xml` within `<dependencies>`
  and let Maven resolve them (the Maven wrapper will download them on
  the next build). Use exact versions. After modifying `pom.xml`, run
  `./mvnw compile` to fetch dependencies.
- **Formatting:** We follow standard Java formatting (4-space
  indentation, curly braces on same line). Use the IDE's Reformat Code
  function or configure Google Java Format if available.
- **Hot reload:** The project includes Spring DevTools. If running via
  IntelliJ, code changes in the classpath will trigger a restart.
  Alternatively, use `spring-boot:run` which picks up changes.

### Code Style and Conventions

- **Language Level:** Use Java 21 features where appropriate (e.g.,
  `record` types for simple DTOs, if it makes sense, though we currently
  use Lombok data classes). However, ensure compatibility with Spring
  Boot 3.5.
- **Project Structure:**
- `com.sinsay.returns.controller` -- REST controllers. Each controller
  handles a specific set of endpoints (e.g., `ReturnController` for form
  submission, `ChatController` for chat interactions).
- `com.sinsay.returns.service` -- Service classes containing business
  logic (e.g., `OpenAIService` for calling GPT, `ReturnService` for
  validating policy conditions).
- `com.sinsay.returns.model` -- DTOs and domain models (e.g.,
  `ReturnRequest`, `ImageAnalysisResult`).
- `com.sinsay.returns.config` -- Configuration classes (if any, e.g.,
  OpenAI API key config, CORS config if needed).
- `com.sinsay.returns.util` -- Utility classes or constants (e.g.,
  define constants for policy rules, date formatting, etc., if needed).
- **Dependency Injection:** **Use constructor injection** for all
  components (preferred) or Lombok `@RequiredArgsConstructor`. *Do not
  use field injection (*`@Autowired` *on
  fields)*[\[29\]](https://blog.jetbrains.com/idea/2025/05/coding-guidelines-for-your-ai-agents/#:~:text=Nowadays%2C%20asking%20an%20AI%20agent,working%20result%2C%20but%20it%20may).
  We want immutability where possible and ease of testing. All service
  and controller classes should either be annotated with Lombok's
  `@RequiredArgsConstructor` or explicitly have a constructor for their
  dependencies.
- **Lombok Usage:** We use Lombok to reduce boilerplate:
- Use `@Data` or `@Getter/@Setter` on simple DTOs.
- Use `@RequiredArgsConstructor` on service classes to inject final
  dependencies.
- **Ensure annotation processing is enabled** (in IntelliJ, it should
  be; otherwise, build will handle it). If you add a new
  Lombok-annotated class, no need to manually create getters, etc.
- **Controller Conventions:**
- Mark REST controllers with `@RestController` and map requests with
  `@RequestMapping` or the specific method annotations (`@GetMapping`,
  `@PostMapping`, etc.).
- Controllers should be lean: delegate to services for heavy logic. **Do
  not** call OpenAI API or perform complex processing in the controller
  itself -- put that in a service method.
- Validate inputs using `@Valid` on request body DTOs and
  `BindingResult` or exception handler for errors. If validation fails,
  return a proper error response (HTTP 400 with error details). We have
  Spring Boot's default error handling or can use a `@ControllerAdvice`
  for custom error format.
- Use meaningful endpoint paths. For example, `/api/returns` for the
  form submission (POST), `/api/chat` for chat messages (POST). Use
  nouns for resources and HTTP verbs for actions (POST for creating a
  conversation message, etc.).
- Return types: Usually `ResponseEntity<...>` to control status codes.
  E.g., return `ResponseEntity.ok(responseDto)` for 200, or appropriate
  status for errors. If using Spring's implicit conversion, returning
  the object is fine (it will default to 200).
- **Service Conventions:**
- Each service should have a clear single responsibility (e.g.,
  `ReturnPolicyService` might check if a return is within policy,
  `OpenAIService` handles calls to the OpenAI API).
- Services can call other services if needed, but avoid circular
  dependencies.
- Make methods `public` only if they're used by other classes; otherwise
  package-private or private.
- Handle exceptions internally and either return a result indicating
  failure or throw a custom exception that controllers can catch. For
  example, if OpenAI API call fails, `OpenAIService` could throw
  `AiApiException` which our global handler translates to a 502 Bad
  Gateway to the client.
- Logging: Use SLF4J (`@Slf4j` Lombok annotation for log) for important
  events. For instance, log when a complaint is approved or denied, and
  why, at INFO level (for audit). Log exceptions at WARN/ERROR with
  context (orderId, etc.). Avoid logging sensitive info like the actual
  image content or user PII (except order ID or high-level stuff).
- **OpenAI Integration:**
- The OpenAI API key is provided via environment (`OPENAI_API_KEY`). We
  map it in `application.properties` as
  `spring.ai.openai.api-key=${OPENAI_API_KEY}` if using Spring AI
  starter[\[32\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=The%20Spring%20AI%20project%20defines,obtained%20from%20openai.com).
  **Do not hard-code API keys** or secrets in the code or repository.
- Use the configured OpenAI client (if using Spring AI, inject
  `OpenAiChatModel` bean). If using the raw OpenAI SDK, instantiate a
  client once (e.g., as a bean in a config class with
  `OpenAIClient.fromEnv()` reading `OPENAI_API_KEY`). *Never print the
  API key or expose it.*
- When calling OpenAI:
  - Prefer async/non-blocking if possible (Spring AI might support
    reactive, but for simplicity we might use blocking calls). It's okay
    as PoC to call synchronously inside a service method (OpenAI
    responses might take a couple seconds).
  - Include relevant context in prompts. The prompt construction logic
    (what system message to send, etc.) can be in a service or utility
    class. Keep it readable -- e.g., build a multi-line string or use a
    small template file. If the prompt is long or complex, consider
    moving it to a resource file and loading it.
  - **Error handling:** Wrap OpenAI API calls in try-catch. If an
    IOException or APIException occurs, log it and throw a custom
    exception upward. Possibly implement retry for transient errors
    (HTTP 429 or timeouts) -- Spring AI has auto-retry config (max 10
    attempts, backoff) which we can adjust via
    `application.properties`[\[13\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=Property%20Description%20Default).
    If using raw SDK, consider one manual retry for certain errors.
- **Data Validation & Policy Enforcement:**
- Use Bean Validation for simple field rules (e.g., `@NotBlank`,
  `@Email`, `@Past` for dates). For complex rules (like "if requestType
  is RETURN, purchaseDate must be within 30 days"), use either a custom
  `@Constraint` annotation or just handle it in service logic after
  receiving the DTO. Simpler: in the controller, after `@Valid` passes
  basic checks, do an if-check for the 30-day rule and if violated,
  respond with
  `ResponseEntity.badRequest().body({"error":"Return period exceeded"})`.
- The 30-day window can be computed with Java Date API. Use
  `java.time.LocalDate` for dates (the DTO can use `@JsonFormat` if
  needed to parse string dates). We have Java 21, which includes some
  helpful date methods. Ensure correct time zone usage (likely we only
  care about date, not time).
- For image uploads: if images come as `MultipartFile`, validate the
  file size (e.g., max 5MB) and type (check `getContentType()` starts
  with `image/`). If invalid, throw an error (HTTP 400).
- The service that calls OpenAI should also perhaps verify that the
  image is present when expected, etc. But largely, rely on the AI to
  classify the defect. Our job is to enforce clear-cut rules (like the
  time window).
- **Output and JSON structure:**
- Follow consistent JSON naming (e.g., camelCase for property names in
  DTOs). We rely on Jackson's default to map field names to JSON keys.
- Create response DTOs for endpoints rather than returning raw entity or
  domain objects. For example, `ReturnResponse` might contain a status
  and message for the outcome of a return submission. For chat, perhaps
  a `ChatMessageResponse` with fields like `role` and `content`. This
  clarity ensures front-end gets exactly what it expects.
- If using OpenAI function calling or we want to return structured data,
  define Java classes for that structure. Eg.
  `DefectDecision { String defectType; boolean approved; String explanation; }`
  and have GPT fill those (we parse JSON -\> object). If we do that, use
  Jackson or manual parsing of GPT output. This is advanced; if not
  doing, just handle as needed.
- **Testing Guidelines:**
- Write unit tests for service methods, especially those with logic
  (policy checks, prompt building). Use JUnit 5. Use meaningful
  `@DisplayName` to describe test cases.
- Use MockMvc for controller integration tests: e.g., test that posting
  an invalid form returns 400, posting a valid one returns 200 and
  expected JSON. We have `spring-boot-starter-test` which includes
  MockMvc and can auto-configure the context.
- Use Mockito or MockBean to stub out the OpenAI interactions for tests.
  For example, in a test for `ChatController`, use
  `@MockBean OpenAiService` and program it to return a preset answer for
  a given prompt. This isolates tests from actual API calls (don't call
  external API in unit tests).
- Test edge cases: date exactly 30 days vs 31 days, missing fields
  (should be caught by validation), extremely large image (should be
  rejected or maybe not even reach OpenAI).
- The tests should run fast and not rely on network. Use Spring's
  `@SpringBootTest` for integration or slice tests (like `@WebMvcTest`
  for controller-only tests with mocked services).
- **Security & Safety:**
- No authentication is in scope (assuming this tool is internal or the
  user's identity isn't needed beyond provided info). However, ensure
  that our endpoints are not exposing anything sensitive. Since it's a
  PoC, we might not add full Spring Security, but be mindful if any keys
  or internal info could leak.
- If later adding auth (e.g., verifying order belongs to user by token),
  follow Spring Security practices, but for now, skip.
- Validate inputs thoroughly (as above) to prevent any unexpected states
  or injection (though primarily we take JSON and images; Jackson and
  Spring handle escaping, etc.).
- **AI Safety:** The backend should perhaps filter out or redact any
  personal data before sending to OpenAI (OpenAI policies encourage not
  sending PII). In our case, we might send an order number or purchase
  date -- that's likely fine. Avoid sending customer's name or email to
  GPT. And of course, do not send the user's entire address or payment
  info (we're not collecting those).
- Use OpenAI's moderation API if time permits to check user-provided
  text (like defect description) before sending to GPT. However, given
  domain, this might not be crucial.
- **Git and Workflow:**
- Create small, focused commits with descriptive messages. For example:
  "Implement 30-day return validation in
  ReturnService[\[5\]](https://android.com.pl/tech/549377-sinsay-zwrot-poradnik/#:~:text=W%20Sinsay%20zwrot%20jest%20mo%C5%BCliwy,przez%2030%20dni%20od%20zakup%C3%B3w)"
  or "Integrate OpenAI image analysis service".
- If you fix a bug or change logic, also update/add tests covering that
  scenario.
- Avoid committing secrets (the API key should be in environment, not in
  code).
- Follow branching strategy as directed (e.g., feature branches merging
  into main).
- Ensure CI (if set up) is green before merge. This means run
  `./mvnw test` locally.
- **Common Pitfalls (and how to avoid them):**
- **NullPointerExceptions:** Use `@NonNull` on required fields (Lombok's
  \@NonNull generates null-checks), and validate inputs from external
  sources. Assume nothing is perfectly safe -- always check optional
  fields for presence before use.
- **Handling Optional:** If using `Optional` as return (e.g., in
  repository calls), handle properly (e.g., `optional.orElseThrow()`).
  *Don't return Optional in controller methods*, better to unwrap or
  throw and let `@ExceptionHandler` deal with it.
- **Date/Time calculations:** Off-by-one day errors -- be careful with
  how you calculate 30 days difference. Use
  `ChronoUnit.DAYS.between(start, end)` or `start.plusDays(30)` to
  compare with end. Ensure using correct time zones (likely LocalDate is
  fine as we deal just with dates).
- **Large images or timeouts from OpenAI:** The agent might hang if
  image is too large. We might want to resize image or limit resolution.
  This can be complex; at minimum, set a reasonable timeout for OpenAI
  calls (OpenAI client may allow setting a timeout). Do not let a single
  request thread hang indefinitely.
- **Threading:** Spring MVC is synchronous by default. If a user uploads
  an image and GPT-4 takes e.g. 15 seconds to respond, that thread is
  tied up. It's okay for small scale. If we needed to scale, we'd
  consider making that async (CompletableFuture and responding via
  DeferredResult/WebFlux) -- but for PoC, keep it simple synchronous.
- **Memory usage:** Loading a large image into memory (as `byte[]`)
  could be heavy. Possibly stream if needed. But likely fine for typical
  image sizes.
- **Spelling/Copy:** Ensure user-facing messages are clear and free of
  typos, as they will be seen by customers. Write messages in Polish if
  required by stakeholders (the Sinsay site is in Polish). Currently, we
  might use English for development, but keep an eye on localization
  requirements.
- **Progressive Disclosure for AI:** We maintain this AGENTS.md to guide
  you (the AI developer). If something is unclear or you encounter
  something not in guidelines, follow typical Spring Boot and Java best
  practices. When coding, include helpful comments especially around the
  OpenAI integration (so future devs know what prompt we send, etc.).

In summary, **write clean, idiomatic Spring Boot code**. Prioritize
clarity and robustness. This backend should be easy to read and modify,
as it will likely evolve (the AI logic might be refined, or integrated
with order databases later). By following the above guidelines, you
ensure consistency and reduce errors, allowing both human and AI
collaborators to work efficiently on this project.

*(End of Spring Boot AGENTS.md)*

------------------------------------------------------------------------

## AGENTS.md for React (Frontend Guidelines)

**Project Overview:** This React project (`frontend/` directory) is the
client-side of the Sinsay Returns AI system. It is a Single Page
Application built with React and Vite, using TypeScript. The UI uses
Shadcn/UI (Tailwind + Radix) components for a modern look and consistent
design. Key functionalities include a multi-step return/complaint intake
form, an image upload interface, and a chatbot UI where the user
interacts with an AI agent. The app communicates with the Spring Boot
backend via REST API calls (HTTP). We aim for a clean, responsive, and
accessible UI.

As an AI coding agent working on this React codebase, please follow
these guidelines:

### Setup Commands

- **Install dependencies:** Run `npm install` in the `frontend/`
  directory. (Ensure Node 18+ is installed).
- **Run the dev server:** Use `npm run dev`. This will start Vite's dev
  server (typically on port 5173). The backend should be running on
  8080; the dev server is configured (via `vite.config.ts`) to proxy API
  calls to the backend (if we set that up). If not, the frontend expects
  to be served by Spring Boot in production; for dev, you might need to
  manually set `VITE_API_URL` env for API base.
- **Build for production:** Use `npm run build`. This outputs static
  files to `dist/` which are then served by Spring Boot (in this
  monorepo setup, the Maven build will copy them).
- **Run tests:** Use `npm run test`. We use Vitest + Testing Library.
  Ensure any new UI logic has corresponding tests (especially form
  validation and any utility functions).
- **Lint/format:** Use `npm run lint` (if ESLint configured) and
  `npm run format` (if Prettier configured). The project adheres to a
  pretty standard ESLint config (extends react-app or similar) and
  Prettier for code style. Indent with 2 spaces for JS/TS, use single
  quotes, etc. Please fix any lint errors/warnings before committing
  (the CI may fail on lint).
- **Adding packages:** Use npm. e.g., `npm install some-library` or for
  dev deps `npm install -D some-dev-lib`. Keep dependencies minimal and
  prefer existing ones unless absolutely needed.
- **Environment Vars:** The Vite dev server can use a `.env` file. For
  example, `VITE_API_URL=http://localhost:8080` to route API calls. Do
  not commit actual secrets. In production (Spring Boot serving), it
  might not need separate env since same origin. If you introduce any
  env var usage, document it and add sample to `.env.example`.

### File/Folder Structure

- **src/main.tsx:** Entry point. Renders `<App />` into the DOM. Also
  set up BrowserRouter here if using React Router for multi-page.
- **src/App.tsx:** Main application component. It likely contains the
  routing logic or the conditional rendering for form vs chat. Keep it
  slim if possible (maybe just routes or context providers).
- **src/components/**: Reusable presentational components and composite
  components. We have subfolders:
- `form/` for form-related components (e.g., `ReturnForm.tsx`, maybe
  `PurchaseChannelField.tsx` etc.).
- `chat/` for chat interface components (e.g., `ChatWindow.tsx`,
  `MessageList.tsx`, `MessageInput.tsx`).
- `ui/` could contain any small generic components not specific to this
  app (if any).
- **src/hooks/**: Custom React hooks if needed (e.g., a `useChat` hook
  to manage chat state, or `useFormState`).
- **src/context/**: Context providers if using React Context (maybe for
  form data or chat conversation).
- **src/utils/**: Utility functions (e.g., a date formatter or a helper
  to call local storage, etc.).
- We use **Shadcn UI components**: They are likely in
  `src/components/ui/` (if we ran the shadcn generator, it might put
  e.g., `Button.tsx`, `Input.tsx`, etc. there). These are pre-styled,
  please use them instead of raw HTML where appropriate.
- **Assets:** If there are static assets (logo, etc.), we can have
  `src/assets/`. But likely minimal assets (maybe we display a thumbnail
  of uploaded image which we handle in code, not a file).
- **CSS:** Tailwind is our primary styling method. There might be a
  `src/index.css` for base Tailwind directives. Avoid writing custom CSS
  classes if possible; use Tailwind utilities or compose styles via
  Radix classes from Shadcn. If needed, you can add a small custom CSS
  for things like a specific layout that Tailwind doesn't cover, but
  usually Tailwind covers almost everything with utilities.

### Coding Conventions & Best Practices

- **TypeScript:** Use strong typing. Define interfaces or types for
  component props and for data structures (e.g., define a
  `ReturnFormData` type for the form fields). Avoid using `any` except
  in truly generic cases. Use `unknown` or proper generics for unknown
  types and then narrow.
- Enable strict mode (we likely have `tsconfig.json` with
  `"strict": true`). Thus, handle all possibly undefined values
  appropriately.
- **React Functional Components:** Use functional components with hooks.
  We do not use class components. Prefer React hooks (useState,
  useEffect, useContext, useReducer as needed).
- **State Management:**
- For form handling, we are using React Hook Form -- continue to use
  that pattern rather than useState for each field. It provides
  `formState` for errors, etc.
- For global or cross-component state, use Context or simple prop
  drilling if shallow. Avoid introducing heavy libraries like Redux/MobX
  for this small scope, as agreed. If something becomes complex, we
  might consider Zustand (simple hook-based store) but likely not needed
  yet.
- For the chat conversation, state might include the list of messages
  and loading status. If multiple components need it, consider context
  (e.g., `<ChatProvider>` context that holds messages and provides
  functions to send new message).
- **Styling:**
- Use Tailwind CSS classes for styling. Make use of Shadcn UI pre-built
  classNames (they often have nice classes for paddings, etc.).
- Keep className strings readable -- group related utilities (e.g.,
  \"px-4 py-2 bg-gray-50 rounded-md\").
- **No inline styles** unless dynamic calculation is needed -- even
  then, consider conditional classes or style objects via React if
  needed. But prefer Tailwind's approach (like use utility classes or
  define in CSS if truly dynamic).
- Ensure responsiveness: use Tailwind's responsive prefixes (`sm:`,
  `md:`) to adjust layout on different screen sizes. The form and chat
  UI should be mobile-friendly (column layout on narrow screens, maybe
  larger paddings on desktop).
- The design should be consistent with Sinsay branding if possible
  (colors, etc.). If Shadcn's default isn't, we may adjust the Tailwind
  config theme (not too much in PoC). Use Sinsay's known color scheme if
  provided (not sure if accessible, but maybe stick to neutral light
  theme).
- **UI Components usage:**
- Use Shadcn `<Button>` for all buttons (don't use raw `<button>` unless
  very custom). They come in variants (e.g., variant=\"outline\" etc.).
- Use Shadcn `<Input>` or `<Textarea>` for form fields. They are already
  styled and accessible.
- Use `<FormField>` and related components as shown in docs to integrate
  with
  RHF[\[46\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=render%3D%7B%28%7B%20field%2C%20fieldState%20%7D%29%20%3D,button%20not%20working%20on%20mobile)[\[47\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=%7Bfield.value.length%7D%2F100%20characters%20,FieldError%20errors%3D%7B%5BfieldState.error%5D%7D)
  -- this ensures labels, errors are tied to inputs correctly.
- Use icons from Lucide where appropriate (e.g., an upload icon on the
  image upload button, a send icon on chat send).
- Keep accessibility in mind: every text input should have a
  corresponding `<label>` (Shadcn's `<FieldLabel>` covers that), buttons
  should have discernible text or aria-label (if icon only).
- Ensure the image `<img>` has an `alt` attribute (maybe \"Preview of
  uploaded product defect\").
- Tab order should flow logically -- form fields in order, then submit.
  Chat input should be focusable after messages. Test with keyboard
  navigation.
- **Forms:**
- Use React Hook Form for managing form data and validation. The form is
  small (\<=5 fields), so one RHF `useForm` instance is fine.
- For conditional fields (e.g., orderId vs receipt depending on
  purchaseChannel), RHF's `watch` can be used to conditionally render.
  But still register those fields so that if visible they get included.
- **Validation:** Use `yup` or `zod` via RHF resolver to enforce complex
  rules (we chose Zod). This gives instant feedback on form (we can
  validate on submit or on change as configured).
- Display validation errors near the fields using `<FieldError>`
  component or manually if needed. Ensure error messages are
  user-friendly (Polish if necessary: e.g., \"Pole wymagane\" for
  required).
- Prevent submission if form invalid -- RHF will handle that by not
  calling `onSubmit` if errors.
- On submit, call backend API (`/api/intake`) to send form data. Provide
  user feedback:
  - Disable submit button while submitting (so user doesn\'t click
    twice).
  - Perhaps show a spinner or \"Submitting\...\" on the button.
  - If error from API (like 400 validation error or 500), show an alert
    or error message (maybe above the form or as a toast). Could
    highlight specific fields if error details returned.
  - If success (e.g., returns conversationId and initial AI message),
    proceed to next step.
- **Chat Flow:**
- After successful form submission (especially for complaints requiring
  AI), transition to chat view. This can be a new route (`/chat`) or a
  conditional rendering within same component (like switching state from
  showForm -\> showChat). We can use React Router (if already set up).
  If using Router, ensure to use `<BrowserRouter>` in main and define
  routes.
- The chat UI likely includes:
  - A message list display. Use a scrollable container with auto-scroll
    to bottom on new messages. Could use a `<ul>` with each message as
    `<li>` or just divs. Use Tailwind classes for styling each message
    differently based on sender (user vs assistant). e.g., user messages
    aligned right with one color, assistant left with another color.
  - If using assistant-ui library, leverage its `<MessageList>` etc.,
    for streaming. If not, implement basic version:
  - On sending a message (or image), add a placeholder message in state
    (so it appears immediately if we want to echo user input).
  - Call API `/api/chat` (with fetch or axios). Possibly use SSE for
    streaming response: could open an EventSource to listen to events
    from server (if server implemented SSE for streaming tokens). If not
    streaming, just wait for full response.
  - Append the assistant's response to message list state.
  - The input area: a text input for user message and a send button.
    When user presses Enter or Send, capture the text, clear the input
    field, call backend.
  - If the assistant asked for an image, we should allow attaching an
    image. We can use an `<input type="file">` (maybe hidden) and a
    button that triggers it. On file select, directly call the backend
    (e.g., send as FormData with fetch) or convert to base64 and include
    in JSON (depending on API contract). Perhaps simpler: have a
    separate button \"Upload image\" that calls an API endpoint or the
    same chat endpoint with an image.
  - Provide UI feedback for image uploading (maybe show image preview in
    chat instantly as a message from user, with a \"uploading\...\"
    status).
  - Error handling: If the chat API call fails (network or server),
    notify user (e.g., \"Failed to send, please retry\"). Possibly allow
    resending by not clearing the input or by storing unsent message.
  - Keep track of `conversationId` (from form submission result) and
    send it with each chat API call, so the server knows which context
    (the backend likely manages context).
  - Possibly implement `useChat` hook that encapsulates the above logic
    (messages state, sendMessage function, loading state).
- Consider adding a slight delay or typing indicator for assistant for
  realism (not necessary if we stream).
- Ensure the chat UI is scrollable but the input stays visible (stick to
  bottom). Use CSS like `overflow-y-auto` for message list and maybe
  `flex flex-col` container that pushes input to bottom.
- **Accessibility & UX:**
- All interactive elements must be keyboard-accessible (links, buttons,
  inputs).
- Use appropriate HTML elements: `<button>` for actions (not \<div
  onClick\>), `<input type="file">` for file selection, labels for
  inputs, etc.
- Provide feedback: e.g., highlight the field with error, and use
  `aria-invalid` and `aria-describedby` for errors (Shadcn form
  components handle some of this).
- Chat messages should have roles (maybe `role="article"` or
  `aria-label="Assistant message"` etc. if needed for screen readers).
  Ensure the message container announces new messages (ARIA live region
  could be considered for the chat log).
- The UI should be intuitive: e.g., the difference between a regular
  return and a complaint should be clear in the form (maybe label it
  \"Reason: Return (no defect) or Complaint (product faulty)\").
- Possibly, if time: add small enhancements like autofocus on the first
  form field when form loads, autofocus on chat input when chat starts,
  etc., to streamline user flow.
- **API Integration:**
- We use relative URLs for API calls since in production same domain. In
  dev, Vite's proxy or an env var `VITE_API_URL` can be used. For
  example, define `const API_BASE = import.meta.env.VITE_API_URL || ""`
  and use `fetch(API_BASE + "/api/endpoint")`. Our AGENTS.md can note
  that for dev, you might need to set VITE_API_URL to
  \"http://localhost:8080\".
- Use `fetch` with proper headers (the backend expects JSON). E.g.,


- fetch("/api/intake", { 
         method: "POST",
         headers: { "Content-Type": "application/json" },
         body: JSON.stringify(formData)
      })
      .then(res => res.json())
      .then(data => { ... })

  If using axios, similar approach with
  `axios.post("/api/intake", formData)`.


- For image upload via fetch: use `FormData`. E.g.,


- const fd = new FormData();
      fd.append("conversationId", convoId);
      fd.append("image", fileInput.files[0]);
      fetch("/api/chat", { method: "POST", body: fd })

  (Ensure backend can handle multipart form. If it expects base64 JSON,
  do accordingly).


- Use `await`/`.then` consistently (prefer async/await for readability).
- Implement loading states: e.g., disable form submit and show spinner
  while awaiting submission response; disable chat input while awaiting
  AI response (or allow user to type but not send multiple
  concurrently).
- If the backend returns an error status, handle it. Check `response.ok`
  in fetch; if not, maybe parse error message and display or have a
  generic \"Something went wrong\".
- **Testing:**
- Write tests using React Testing Library for:
  - The form: simulate filling fields, trigger validation by clicking
    submit, assert that errors show for invalid input; fill valid data
    and mock fetch (perhaps use msw to simulate backend response) and
    assert that it goes to next step (maybe check that Chat component
    appears).
  - The chat: we can test the chat component logic by mocking the
    `fetch` call for sending a message (use msw or similar to simulate
    an assistant reply). Assert that after sending, the new message
    appears in list. Also test that sending an empty message might not
    call API (if we disallow).
  - Snapshot or DOM tests for basic rendering of components (button,
    etc., but these are Shadcn default -- not much logic there).
- Use Vitest's JSDOM environment. For file upload, you can simulate by
  creating a Blob or File object and populating an input's files
  (Testing Library `userEvent.upload` is useful).
- Aim for coverage on crucial branches: e.g., form toggling between
  Online vs In-store fields, chat handling both text and image messages.
- Ensure tests run without real backend (so likely mock fetch with
  `global.fetch = vi.fn()` in tests or using `msw` to intercept network
  calls).
- **Git Workflow:**
- Similar to backend: commit small changes with clear messages (e.g.,
  \"Add client-side validation for purchase date\" or \"Implement chat
  message list component\").
- Before committing, run `npm run lint` and fix issues, run `npm test`
  and ensure all pass.
- We version control the `package.json` and `package-lock.json` -- any
  new dependencies should reflect there.
- Do not commit build output (dist/) or .env files with secrets. Only
  commit .env.example if needed.
- If using feature flags or environment toggles, document them.
- **Common Pitfalls (Frontend) & Tips:**
- **State sync issues:** E.g., forgetting to add `value` and `onChange`
  for controlled inputs. With RHF, use its register properly or
  Controller. If manual, ensure React component state ties to input
  value.
- **Over/Under-fetching:** Don't call the API more than needed. E.g., on
  form submission, call once. On chat, if using SSE, don't also do
  unnecessary fetch. Align calls with user actions.
- **Memory leaks:** If using effect with cleanup (like maybe for
  auto-scrolling or for SSE event listener), ensure to clean up in
  `useEffect` return to avoid listeners accumulating on hot reload in
  dev.
- **Performance:** Our app is small; performance is fine. Just avoid
  extremely inefficient code in loops. The one heavy thing could be
  rendering a long list of chat messages -- use keys on list items for
  reconciliation. If extremely long, we could virtualize list (not
  needed for small usage).
- **Integration with backend:** make sure the JSON shapes match exactly
  what backend expects and sends. For instance, if backend expects
  `purchaseChannel` as \"ONLINE\"/\"IN_STORE\", ensure we send that, not
  e.g. lowercase. If backend returns `assistantMessage` property, use
  the same. This consistency is vital -- refer to backend DTO or API
  docs when writing fetch handling.
- **Timeouts/slow API**: Indicate to user when waiting. E.g., if GPT
  processing image might take 10 seconds, maybe show a \"Assistant is
  thinking\...\" message or a typing indicator.
- **UI polish:** Basic styling is covered by Shadcn. But ensure it looks
  decent: spacing between form fields, proper alignment of labels,
  responsive layout. Use Shadcn's layout components (card, etc.) to
  structure things nicely (e.g., wrap form in a `<Card>` as they often
  do in
  examples[\[40\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=return%20%28%20%3CCard%20className%3D%22w,CardContent)).
- **Localization (if required):** If later we need to display messages
  in Polish, we might not integrate a full i18n library for PoC, but we
  can at least avoid hard-coding a lot of English text. Possibly keep
  strings in a constants file or at top of component to ease changing
  language. For now, probably English is fine for dev.

By following these guidelines, you ensure the frontend code remains
**clean, understandable, and aligned with best practices**. This will
make it easier for any developer or AI agent to pick up and modify. The
goal is an intuitive UX and maintainable codebase. Keep components
focused, avoid duplication (if you find yourself repeating similar JSX
for two slightly different fields, consider making a small reusable
component).

Remember, the frontend and backend work in tandem. If you adjust
something that affects the API contract, coordinate with backend (which
in our monorepo, is easy since you can update both). Keep an eye on the
DevTools console and network tab during development to catch any CORS
errors, API errors, or React warnings.

*(End of React AGENTS.md)*

------------------------------------------------------------------------

## Project Structure

Below is the proposed **monorepo file structure** for the entire
project, illustrating how the backend and frontend are organized
together and where key files reside:

    sinsay-returns-ai/
    ├── backend/
    │   ├── pom.xml
    │   ├── mvnw & mvnw.cmd (Maven Wrapper scripts)
    │   └── src/
    │       ├── main/
    │       │   ├── java/
    │       │   │   └── com/sinsay/returns/
    │       │   │       ├── ReturnsApplication.java        # Spring Boot starter class
    │       │   │       ├── controller/
    │       │   │       │   ├── ReturnController.java      # Handles form submission endpoint
    │       │   │       │   └── ChatController.java        # Handles chat message endpoint (text & image)
    │       │   │       ├── service/
    │       │   │       │   ├── ReturnPolicyService.java   # Business logic for return eligibility (30-day check, etc.)
    │       │   │       │   ├── OpenAIService.java         # Encapsulates calls to OpenAI API (vision analysis)
    │       │   │       │   └── ChatService.java           # Manages conversation state (if any server-side)
    │       │   │       ├── model/
    │       │   │       │   ├── ReturnRequest.java         # DTO for form submission (fields: purchaseChannel, orderId, etc.)
    │       │   │       │   ├── ReturnResponse.java        # DTO for form submit response (maybe includes conversationId, initial bot msg)
    │       │   │       │   ├── ChatMessage.java           # Model for a chat message (role, content; used in request/response)
    │       │   │       │   └── DefectDecision.java        # (Optional) Model for defect analysis result from AI (type, approved, reason)
    │       │   │       ├── config/
    │       │   │       │   ├── OpenAIConfig.java          # (Optional) Configures OpenAI client bean if needed
    │       │   │       │   └── WebConfig.java             # (Optional) CORS configuration, ResourceHandlers for static, etc.
    │       │   │       ├── util/
    │       │   │       │   └── DateUtils.java             # (Optional) Utility for date calculations (e.g., daysBetween)
    │       │   │       └── exception/
    │       │   │           ├── ApiException.java          # Custom exception for API errors (e.g., OpenAI failure)
    │       │   │           └── GlobalExceptionHandler.java# Uses @ControllerAdvice to handle exceptions (validation, ApiException, etc.)
    │       │   └── resources/
    │       │       ├── application.properties            # Spring Boot configuration (OpenAI API key reference, etc.)
    │       │       ├── static/
    │       │       │   └── ** (Front-end build output will be placed here) ** 
    │       │       └── templates/                        # (Not used unless Thymeleaf; we serve static SPA)
    │       └── test/
    │           ├── com/sinsay/returns/
    │           │   ├── ReturnControllerTests.java        # Tests for controller (using MockMvc)
    │           │   ├── ReturnPolicyServiceTests.java     # Tests for policy logic (30-day calc, etc.)
    │           │   ├── OpenAIServiceTests.java           # Tests for OpenAI integration (could mock API responses)
    │           │   └── ChatFlowIntegrationTest.java      # End-to-end test simulating form submit then chat (maybe using MockMvc + stubbed OpenAI)
    │           └── resources/
    │               └── test-data/                        # Sample payloads or images for testing (if needed)
    ├── frontend/
    │   ├── package.json
    │   ├── vite.config.ts                                # Vite configuration (dev server proxy to backend, etc.)
    │   ├── tailwind.config.js                            # Tailwind configuration (content paths, theme)
    │   ├── postcss.config.js
    │   ├── tsconfig.json
    │   └── src/
    │       ├── main.tsx                                  # Application entry, renders App (and BrowserRouter if using routing)
    │       ├── App.tsx                                   # Root component (could contain Routes or conditional render Form vs Chat)
    │       ├── index.css                                 # Global CSS imports (Tailwind directives, any base styles)
    │       ├── components/
    │       │   ├── form/
    │       │   │   ├── ReturnForm.tsx                    # Component for the initial intake form
    │       │   │   ├── PurchaseChannelField.tsx          # Sub-component for radio or select (Online vs In-Store)
    │       │   │   ├── OrderNumberField.tsx              # Sub-component for order/receipt input (with conditional logic)
    │       │   │   └── ... (fields or maybe integrated in ReturnForm directly)
    │       │   ├── chat/
    │       │   │   ├── ChatWindow.tsx                    # Main chat interface component containing messages and input
    │       │   │   ├── MessageList.tsx                   # Displays list of Message bubbles
    │       │   │   ├── MessageItem.tsx                   # A single message (styles differ for user vs assistant)
    │       │   │   ├── MessageInput.tsx                  # Text input and send button for user to send message
    │       │   │   └── ImageUploadButton.tsx             # Button/flow for attaching an image (could open file dialog)
    │       │   ├── ui/
    │       │   │   ├── Button.tsx                        # Shadcn UI Button component (pre-styled)
    │       │   │   ├── Input.tsx                         # Shadcn Input component
    │       │   │   ├── Form.tsx                          # Shadcn Form utilities (FormField, FormItem, etc.)
    │       │   │   ├── Card.tsx                          # Shadcn Card component (used to wrap form or chat maybe)
    │       │   │   └── ... (other Shadcn components as needed, e.g., Label, Textarea)
    │       │   └── ... (other components if needed e.g., a Modal for any dialog or a Toast component)
    │       ├── hooks/
    │       │   ├── useChat.ts                            # (Optional) Custom hook encapsulating chat logic (message state, send function)
    │       │   └── useFormFields.ts                      # (Optional) Hook to manage dynamic form fields (if complex)
    │       ├── context/
    │       │   └── ChatContext.tsx                       # (Optional) Context provider for chat state across components
    │       ├── utils/
    │       │   ├── api.ts                                # Helper for API calls (e.g., wraps fetch/axios with base URL and error handling)
    │       │   ├── validation.ts                         # Zod schemas for form validation (e.g., ReturnFormSchema)
    │       │   └── formatDate.ts                         # Utility to format dates if needed for display
    │       └── assets/
    │           └── logo.png (for example, if we had a logo or any static images)
    └── AGENTS.md (root-level overview or pointers to sub AGENTS files)

**Notes on Structure:**

- The `backend/src/main/java` packages follow typical Spring naming. All
  code is under `com.sinsay.returns` which groups context.
- The `backend/src/main/resources/static` folder is where the React
  build outputs will end up (Maven can copy `frontend/dist/*` here
  during build). This allows Spring to serve the SPA. No template
  engines needed.
- The `frontend/src/components/ui` folder contains imported Shadcn UI
  components. These are essentially design system primitives we leverage
  (like Button, Input, etc.). We treat them as internal library code --
  generally shouldn't need frequent edits, except maybe tweaking styling
  or adding variants.
- We separated form and chat components for clarity. The
  `ReturnForm.tsx` handles form UI and on submit calls a prop or context
  to notify parent to switch to chat.
- The Chat uses multiple components: breaking out `MessageItem` helps to
  differentiate styling for user vs assistant messages (could be a prop
  `role` that applies different Tailwind classes).
- `useChat.ts` hook (if used) can maintain messages state and abstract
  the API calls (maybe using a reducer for adding messages, setting
  loading, etc.). This could simplify ChatWindow logic or allow reuse
  (though we only have one chat interface).
- `utils/api.ts` might encapsulate fetch calls, e.g., a function
  `submitReturn(formData)` and `sendMessage(convoId, contentOrFile)`
  returning promises. This centralizes handling base URL and error
  processing (like check `res.ok`).
- Context: if the conversationId and initial messages need to be
  available to multiple components (like ChatWindow and maybe some
  header), we could use a ChatContext to provide that. Or we can keep it
  internal to ChatWindow if not needed elsewhere.
- The testing files aren't shown above but would likely mirror structure
  in a `__tests__` or alongside files. For simplicity, we listed them
  under `backend/src/test` in similar structure and `frontend` tests
  could be under `frontend/src` (Vitest often encourages co-locating
  tests, e.g., `ReturnForm.test.tsx` next to the component).
- Root AGENTS.md: We might keep one at root with general info and then
  link to `backend/AGENTS.md` and `frontend/AGENTS.md` for specifics (as
  we wrote above). The structure above assumes one top-level AGENTS.md,
  but we can also have separate ones as needed (as the Agents.md
  guidelines allow nested agent files for
  subprojects[\[64\]](https://agents.md/#:~:text=4,md%20files%20for%20subprojects)).

Every directory's purpose: - **backend/pom.xml**: Maven config listing
dependencies (Spring Boot starters, Lombok, etc.), build plugins (e.g.,
plugin to build front static assets). - **ReturnsApplication.java**:
Main method launching Spring Boot. - **Controllers**: Routing and API
layer. - **Services**: logic and external API calls. - **Model**: Data
containers for request/response to decouple from entity if any (no DB
here, but treating OpenAI results as model). - **Exception**: Custom
exceptions and global handler so that controllers remain clean (e.g.,
handle MethodArgumentNotValidException for form validation globally to
send JSON error). - **resources/application.properties**: likely minimal
(set server port if needed, and OpenAI key config as mentioned). -
**frontend/vite.config.ts**: Proxy setup. For example:

    server: { proxy: { '/api': 'http://localhost:8080' } }

so that dev calls go to Spring Boot. - **frontend/tailwind.config.js**:
content points to all TSX files for purge. - **frontend/src/index.css**:
includes Tailwind base styles (like
`@tailwind base; @tailwind components; @tailwind utilities;`). Might
also include any custom global styles if needed (though try to avoid
heavy global CSS). - **Shadcn components** in
`frontend/src/components/ui`: They are basically pre-made React
components with Tailwind class names, e.g., Button.tsx which uses `cva`
(Class Variance Authority) for variants. We will not modify them
heavily, just use them.

This structure ensures a clear separation of concerns: - The form and
chat flows are separated in code but can share some things (like maybe
both use a `types.ts` file describing `PurchaseChannel` enum or such,
which we can define in `frontend/src/utils/validation.ts` alongside Zod
schema to keep all form definitions in one place). - We keep styling
co-located with components (Tailwind classes within the JSX). No
separate `.css` for component-specific styles (Tailwind covers it). -
Static build artifacts do not clutter the repo (we do not commit
`dist/`, just generate on build; static is kept in resources in final
jar).

**Integration between front and back:** - The backend static resource
config will serve `index.html` for `/` and probably for any route (to
allow client-side routing to work on reload). We might need a catch-all
controller or resource handler mapping `/**` (except `/api/**`) to
index.html. We can configure that in `WebConfig.java` (as commented in
structure). - The front calls `/api/...` which maps to controllers.

If any shared code would be beneficial (like the list of defect types or
the return policy days), we might duplicate logic on both sides or make
it an env variable. For example, 30 days -- we can define on front as a
constant, and on back as constant. Duplication is fine for such a simple
rule, but ensure if changed, both updated. Alternatively, backend could
send policy info in response (overkill now).

**File tree in text form for brevity:**

    sinsay-returns-ai/
    ├── backend/
    │   ├── pom.xml
    │   └── src/main/java/com/sinsay/returns/... (controllers, services, model, etc.)
    │       (as detailed above)
    │   └── src/test/java/com/sinsay/returns/... (tests corresponding to classes)
    ├── frontend/
    │   ├── package.json
    │   ├── vite.config.ts
    │   └── src/
    │       ├── main.tsx, App.tsx
    │       ├── components/{form, chat, ui}/... (component files)
    │       ├── hooks/... 
    │       ├── context/...
    │       └── utils/... 
    └── AGENTS.md (plus AGENTS.md inside backend/ and frontend/ if split)

This layout fosters modularity: e.g., one can work on `frontend/` in
isolation (it's a standalone npm project), and similarly `backend/` is
standalone Maven project. But combined, they deliver the full
functionality.

By clearly naming directories and files by feature and purpose, any
contributor (AI or human) can quickly navigate. For example, an AI asked
to modify validation rules will see likely `ReturnRequest.java`
(backend) and `validation.ts` (frontend) to change. The structure and
naming we've chosen align with what many projects do, so AI dev tools
should feel at
home[\[60\]](https://agents.md/#:~:text=README,project%20descriptions%2C%20and%20contribution%20guidelines).

------------------------------------------------------------------------

## Implementation Plan (Step-by-Step)

Finally, we outline a step-by-step plan to implement the system. This
plan can be followed by developers (or AI agents) to build the project
from scratch:

**Step 1: Initialize Backend Project (Spring Boot)**\
- **Use Spring Initializr:** Generate a Spring Boot project with
dependencies: Spring Web, Spring Validation, Lombok. Java version 21,
packaging Jar, group `com.sinsay`, artifact `returns-backend`.\
*(Command:)*
`curl https://start.spring.io/starter.zip -d dependencies=web,validation,lombok -d groupId=com.sinsay -d artifactId=returns-backend -d javaVersion=21 -d bootVersion=3.5.9 -o backend.zip`[\[44\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Starting%20with%20a%20Simple%20Backend).\
Unzip it into `backend/` directory. This gives us
`ReturnsApplication.java` etc.

- **Configure OpenAI API Key:** In
  `backend/src/main/resources/application.properties`, add:


- spring.ai.openai.api-key=${OPENAI_API_KEY}
      spring.ai.openai.model=gpt-4-vision  (if Spring AI needs default model name)
      spring.ai.retry.max-attempts=3      (reduce retries to avoid long waits)

  This assumes we use Spring AI starter. If not using Spring AI, we will
  configure `OpenAIClient` in code with key from env.


- **Implement ReturnRequest DTO (Backend):**\
  Create `ReturnRequest.java` in `model` package:


- @Data
      public class ReturnRequest {
          @NotNull
          private PurchaseChannel purchaseChannel; // define enum ONLINE/IN_STORE
          @NotBlank
          private String orderOrReceipt; // order number or receipt code (depending on channel)
          @NotNull @PastOrPresent
          private LocalDate purchaseDate;
          @NotNull
          private RequestType requestType; // enum { RETURN, COMPLAINT }
          private String defectDescription;
          // Lombok @Data generates getters/setters
      }

  Define enums `PurchaseChannel { ONLINE, IN_STORE }` and
  `RequestType { RETURN, COMPLAINT }` either as nested enums or separate
  files in `model`. These help type-safety.


- **Implement ReturnController (Backend):**\
  In `controller` package,
  `@RestController @RequestMapping("/api") public class ReturnController { ... }`.
  Add endpoint for form submission:


- @PostMapping("/intake")
      public ResponseEntity<?> submitReturn(@Valid @RequestBody ReturnRequest request) {
          // call ReturnPolicyService to check eligibility
          if (request.getRequestType() == RequestType.RETURN) {
              boolean ok = returnPolicyService.isReturnEligible(request);
              if (!ok) {
                  return ResponseEntity.badRequest()
                      .body(Map.of("error", "Return period exceeded 30 days"));
              }
              // If eligible, maybe directly respond with instructions or simulate agent short interaction
              ReturnResponse resp = new ReturnResponse(/* maybe a message or status */);
              return ResponseEntity.ok(resp);
          } else { // COMPLAINT
              // Initiate conversation context (maybe generate an ID)
              String convoId = chatService.startConversation(request);
              // Possibly get initial assistant message if any (or just greet)
              String botMessage = "Thank you. Please send a photo of the defect for analysis.";
              ReturnResponse resp = new ReturnResponse(convoId, botMessage);
              return ResponseEntity.ok(resp);
          }
      }

  `ReturnPolicyService.isReturnEligible(request)` checks date difference
  \<=30 days. We need to implement that service (Step 3).\
  `chatService.startConversation(request)` could store the request in
  memory map keyed by an ID (like using a simple
  `UUID.randomUUID().toString()`). This would allow ChatController to
  retrieve initial context later. If we don't want to store, we could
  encode info in the conversation ID (not secure or great). But as PoC,
  a simple in-memory store is fine. Alternatively, skip storing and have
  client send entire context each time -- less ideal. Let's store.


- **Implement ReturnPolicyService (Backend):**\
  In `service` package:


- @Service
      public class ReturnPolicyService {
          public boolean isReturnEligible(ReturnRequest req) {
              LocalDate purchaseDate = req.getPurchaseDate();
              return !purchaseDate.isBefore(LocalDate.now().minusDays(30));
          }
      }

  (This assumes including day 30 as eligible. If need strict, adjust
  `.minusDays(30)` accordingly). We might add more checks (like item is
  unused, but we rely on user confirmation of tags in UI, or not at all
  in PoC).


- **Implement ChatService (Backend):**\
  In `service` pkg:


- @Service
      public class ChatService {
          private final Map<String, ConversationContext> conversations = new ConcurrentHashMap<>();
          public String startConversation(ReturnRequest req) {
              String convoId = UUID.randomUUID().toString();
              // Save context including the ReturnRequest or relevant parts
              conversations.put(convoId, new ConversationContext(req));
              return convoId;
          }
          public OpenAIRequest buildOpenAIRequest(String convoId, String userMessage, MultipartFile image) {
              ConversationContext ctx = conversations.get(convoId);
              // Build prompt: use ctx.returnRequest info + userMessage and image
              // ...
          }
          public String processUserMessage(String convoId, String messageText, MultipartFile image) {
              // Use openAIService to get reply
              ChatResponse aiResp = openAIService.sendMessage(conversations.get(convoId), messageText, image);
              // Save or update context if needed (append conversation history)
              conversations.get(convoId).addMessage("assistant", aiResp.getContent());
              return aiResp.getContent();
          }
      }

  `ConversationContext` could be a simple class holding the initial
  ReturnRequest and maybe a list of past messages if we want to
  accumulate (for GPT context). But GPT-4 can handle fairly large
  context, but we\'ll be making separate calls per message. Possibly we
  keep history and send it all each time (though images maybe not each
  time). For simplicity, maybe do not accumulate all user messages, just
  rely on conversation logically but not enforce strong memory beyond
  initial data and maybe last assistant message to maintain context.

Alternatively, we might not implement complex context at all, just
always send initial info with every OpenAI call (because each call is
stateless except what we include). That's simpler and avoids memory
store. But to keep plan, the store is fine.

- **Implement OpenAIService (Backend):**\
  Assuming Spring AI:


- @Service
      public class OpenAIService {
          private final OpenAiChatModel chatModel;
          public OpenAIService(OpenAiChatModel chatModel) { this.chatModel = chatModel; }

          public ChatResponse sendMessage(ConversationContext ctx, String userMessage, MultipartFile image) {
              // Build prompt with system and user messages
              List<Message> messages = new ArrayList<>();
              // System message containing policy and context
              String policy = "Return policy: 30 days for returns. Complaints allowed for defects. Normal wear not covered.";
              String context = "Purchase date: " + ctx.getReq().getPurchaseDate() + 
                               ", Request type: " + ctx.getReq().getRequestType();
              messages.add(new SystemMessage("You are Sinsay assistant. " + policy + " " + context));
              if (userMessage != null) {
                  if (image != null) {
                      // If an image is included with the user message
                      messages.add(new UserMessage(userMessage, List.of(new Media(... image bytes ...))));
                  } else {
                      messages.add(new UserMessage(userMessage));
                  }
              }
              // Call OpenAI
              ChatResponse resp = chatModel.call(new Prompt(messages));
              return resp;
          }
      }

  The above is conceptual. Actually, for images, Spring AI expects
  `Resource` object. So you'd convert `MultipartFile` to
  `ByteArrayResource` and create
  `new Media(MimeTypeUtils.IMAGE_JPEG, resource)`. The GPT model used
  should be GPT-4 with vision. We set that in properties or explicitly
  in `OpenAiChatOptions`. E.g.,

      OpenAiChatOptions opts = OpenAiChatOptions.builder().model("gpt-4-vision").build();
      ChatResponse resp = chatModel.call(new Prompt(messages), opts);

  We might instruct GPT to respond briefly or in JSON if needed by
  adding something in system prompt.


- **Implement ChatController (Backend):**\
  In `ChatController.java`:


- @PostMapping(value = "/chat", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
      public ResponseEntity<ChatMessage> chat(
              @RequestParam String conversationId,
              @RequestParam(required=false) String message,
              @RequestParam(required=false) MultipartFile image) {
          // message could be empty if just an image is sent
          String reply = chatService.processUserMessage(conversationId, message, image);
          ChatMessage response = new ChatMessage("assistant", reply);
          return ResponseEntity.ok(response);
      }

  This allows a multipart form with fields: conversationId, message,
  image. If the frontend always sends JSON (no image), we could have a
  separate mapping or allow both. The above uses `consumes` with both
  JSON and multipart which might require separate endpoints in practice
  (Spring might not handle one method with both well). We could instead
  do:


- If it\'s text-only: client sends JSON
  `{"conversationId":"...","message":"..."}`

- If with image: client sends FormData (with message possibly). We can
  differentiate by checking content type in request. Possibly make two
  methods or one that takes MultipartFile optional. Given PoC, a single
  method with `@RequestParam` works because in a JSON request, image
  param will be null and Spring can still bind message via
  `@RequestParam` if content-type JSON? Actually, Spring might not bind
  `@RequestParam` from JSON body. Maybe easier:

  - One endpoint `/chat` that accepts JSON body `ChatMessage` (with
    fields conversationId, message).
  - Another endpoint `/chat/image` that accepts `MultipartFile` and
    conversationId (and maybe message if needed). But we\'ll skip
    complexity: likely just handle image within same, but the binding
    might need using `MultipartHttpServletRequest`. For brevity, assume
    the agent can manage a solution.

- **Define Response DTOs (Backend):**\
  `ReturnResponse.java` could contain:


- @Data
      public class ReturnResponse {
          private String conversationId;
          private String assistantMessage;
          // maybe a flag if needed
          public ReturnResponse(String convoId, String msg) {
              this.conversationId = convoId;
              this.assistantMessage = msg;
          }
      }

  `ChatMessage.java` (as used above in controller response) might have:

      @Data
      @AllArgsConstructor
      public class ChatMessage {
          private String role; // "assistant" or "user"
          private String content;
      }

  But in controller, we only return assistant messages outward, user
  messages originate from client. So ChatMessage here is for assistant
  reply payload.


- **Backend Test Quick Run:**\
  At this point, we have the basic backend flow:

- Submitting ReturnRequest returns conversationId (for complaint) or
  immediate confirmation (for return).

- Chat endpoint processes user messages with OpenAI.\
  We should do a quick manual test (maybe via `curl`):


- curl -X POST http://localhost:8080/api/intake \
        -H "Content-Type: application/json" \
        -d '{"purchaseChannel":"ONLINE","orderOrReceipt":"ABC123","purchaseDate":"2025-12-01","requestType":"COMPLAINT"}'

  Expect a JSON with conversationId and initial assistantMessage (like
  \"Please upload photo\"). Then test sending an image:
      curl -X POST http://localhost:8080/api/chat \
        -F conversationId=<id_from_previous> \
        -F image=@test.jpg

  (We should also supply message text if needed, maybe not needed if
  image alone triggers analysis). Expect JSON with assistant reply.\
  This manual test would require actual OpenAI call, which might not
  work until we have API key and such. For now, assume it would return
  something.

**Step 2: Initialize Frontend Project (React + Vite)**\
- **Scaffold React App:**\
Run
`npm create vite@latest frontend -- --template react-ts`[\[45\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=cd%20src%2Fmain%20npm%20create%20vite%40latest,ts).
Choose a name, then `cd frontend && npm install`. This yields a base
React 18 TS project.

- **Install Tailwind & Shadcn UI:**


- npm install -D tailwindcss postcss autoprefixer
      npx tailwindcss init -p

  This adds `tailwind.config.js` and `postcss.config.js`. Edit
  `tailwind.config.js`:
      content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}"
      ],

  This ensures Tailwind scans all our files for class usage.\
  In `src/index.css`, add:
      @tailwind base;
      @tailwind components;
      @tailwind utilities;

  Now, for Shadcn (which are basically pre-built components):


- We can manually copy needed components from shadcn UI documentation
  (since it's not a library to install).

- However, for brevity, let\'s say we create:

  - `src/components/ui/Button.tsx`: from Shadcn (with variants for
    default, outline, etc.).
  - `src/components/ui/Input.tsx`: styles for input element.
  - `src/components/ui/Label.tsx`: for form labels.
  - `src/components/ui/Card.tsx`: to wrap content nicely.
  - `src/components/ui/Form.tsx`: which might include `FormField`,
    `FormItem`, `FormLabel`, `FormMessage` etc., as Shadcn provides for
    RHF
    integration[\[46\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=render%3D%7B%28%7B%20field%2C%20fieldState%20%7D%29%20%3D,button%20not%20working%20on%20mobile)[\[47\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=%7Bfield.value.length%7D%2F100%20characters%20,FieldError%20errors%3D%7B%5BfieldState.error%5D%7D).
    (This is a bit of manual work, but those can be fetched from
    shadcn's open source or via CLI in a Next project and copied. We\'ll
    assume they\'ve been added to project.)

- **Setup Vite Proxy (Dev):**\
  In `vite.config.ts`, configure:
  `ts export default defineConfig({ plugins: [react()], server: { proxy: { '/api': 'http://localhost:8080' } } });`[\[65\]](https://graphite.com/guides/monorepo-frontend-backend-best-practices#:~:text=shared%20dependencies.%20,of%20the%20codebase%20that%20changed).\
  This means during `npm run dev`, any request to `/api` is forwarded to
  backend. So we can use relative URLs in fetch.

- **Create TypeScript types for form and chat:**\
  In `src/utils/validation.ts` (or separate):


- import { z } from 'zod';
      export const returnFormSchema = z.object({
        purchaseChannel: z.enum(['ONLINE','IN_STORE']),
        orderOrReceipt: z.string().min(1, 'Required'),
        purchaseDate: z.string().refine(val => {
          const date = new Date(val);
          return !isNaN(date.getTime()) && 
                 date >= new Date(new Date().setDate(new Date().getDate()-30));
        }, { message: 'Purchase date beyond 30 days' }),
        requestType: z.enum(['RETURN','COMPLAINT']),
        defectDescription: z.string().max(500).optional()
      }).refine(data => {
        if(data.requestType === 'RETURN' && data.purchaseDate) {
          // Check date logic already done above in refine.
        }
        return true;
      });
      export type ReturnFormData = z.infer<typeof returnFormSchema>;

  (We made purchaseDate a string for simplicity because we\'ll get it
  from a date input as string. Or we could use z.date if we manage
  converting.)

Also define maybe:

    export type ChatMessage = {
      role: 'user' | 'assistant',
      content: string;
    };
    export type ReturnResponse = {
      conversationId?: string;
      assistantMessage?: string;
      error?: string;
    };

to type the responses from backend.

- **Implement ReturnForm Component (Frontend):**\
  Create `src/components/form/ReturnForm.tsx`:


- import { useForm } from 'react-hook-form';
      import { zodResolver } from '@hookform/resolvers/zod';
      import { returnFormSchema, ReturnFormData } from '../../utils/validation';
      import { Input } from '../ui/Input';
      import { Button } from '../ui/Button';
      import { Label } from '../ui/Label';
      import { Card } from '../ui/Card';
      // maybe import FormField, FormItem, etc.
      type ReturnFormProps = { onSubmitted: (res: { conversationId?: string, assistantMessage?: string }) => void };
      const ReturnForm: React.FC<ReturnFormProps> = ({ onSubmitted }) => {
        const form = useForm<ReturnFormData>({ resolver: zodResolver(returnFormSchema), mode: 'onChange' });
        const { register, handleSubmit, formState: { errors, isSubmitting }, watch } = form;
        const watchType = watch('requestType');
        const onSubmit = async (data: ReturnFormData) => {
          try {
            const res = await fetch('/api/intake', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(data)
            });
            const result = await res.json();
            if(!res.ok) {
              // handle validation error from server
              alert(result.error || 'Submission failed');
            } else {
              onSubmitted(result); // notify parent to proceed
            }
          } catch(err) {
            console.error('Network error', err);
            alert('Network error, please try again');
          }
        };
        return (
          <Card className="p-6 max-w-md mx-auto">
            <h2 className="text-xl font-bold mb-4">Return / Complaint Form</h2>
            <form onSubmit={handleSubmit(onSubmit)}>
              <div className="mb-3">
                <Label htmlFor="purchaseChannel">Purchase Channel</Label><br/>
                <select id="purchaseChannel" {...register('purchaseChannel')} className="border px-2 py-1">
                  <option value="ONLINE">Online</option>
                  <option value="IN_STORE">In-Store</option>
                </select>
              </div>
              <div className="mb-3">
                <Label htmlFor="orderOrReceipt">
                  {watch('purchaseChannel') === 'ONLINE' ? 'Order Number' : 'Receipt Number'}
                </Label>
                <Input id="orderOrReceipt" {...register('orderOrReceipt')} />
                {errors.orderOrReceipt && <p className="text-red-600 text-sm">{errors.orderOrReceipt.message}</p>}
              </div>
              <div className="mb-3">
                <Label htmlFor="purchaseDate">Purchase Date</Label>
                <Input id="purchaseDate" type="date" {...register('purchaseDate')} />
                {errors.purchaseDate && <p className="text-red-600 text-sm">{errors.purchaseDate.message}</p>}
              </div>
              <div className="mb-3">
                <Label>Request Type</Label><br/>
                <label className="mr-4">
                  <input type="radio" value="RETURN" {...register('requestType')} defaultChecked /> Return (within 30 days)
                </label>
                <label>
                  <input type="radio" value="COMPLAINT" {...register('requestType')} /> Complaint (defective product)
                </label>
                {errors.requestType && <p className="text-red-600 text-sm">{errors.requestType.message}</p>}
              </div>
              { watchType === 'COMPLAINT' && (
                <div className="mb-3">
                  <Label htmlFor="defectDescription">Defect Description (optional)</Label>
                  <textarea id="defectDescription" {...register('defectDescription')} className="border w-full p-2"></textarea>
                </div>
              )}
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Submitting...' : 'Submit'}
              </Button>
            </form>
          </Card>
        );
      };
      export default ReturnForm;


- We used basic HTML select and radio for simplicity, and a button. We
  show errors from RHF validation and from backend if returned via
  alert.

- When submitted successfully, we call `onSubmitted(result)` to parent
  with conversationId and initial message if any.

- **Implement ChatWindow Component (Frontend):**\
  Create `src/components/chat/ChatWindow.tsx`:


- import React, { useState, useEffect, useRef } from 'react';
      import { ChatMessage } from '../../utils/validation'; // or define ChatMessage type separately
      import { MessageList } from './MessageList';
      import MessageInput from './MessageInput';
      import { ImageUploadButton } from './ImageUploadButton';
      type ChatWindowProps = {
        convoId: string;
        initialMessage?: string;
      };
      const ChatWindow: React.FC<ChatWindowProps> = ({ convoId, initialMessage }) => {
        const [messages, setMessages] = useState<ChatMessage[]>( initialMessage 
            ? [{ role:'assistant', content: initialMessage }] : [] );
        const [loading, setLoading] = useState(false);
        const bottomRef = useRef<HTMLDivElement>(null);

        useEffect(() => {
          // scroll to bottom when messages change
          bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
        }, [messages]);

        const sendMessage = async (text: string, imageFile?: File) => {
          if (!text && !imageFile) return;
          // Add user message to UI immediately
          if (text) {
            setMessages(msgs => [...msgs, { role: 'user', content: text }]);
          }
          setLoading(true);
          try {
            let response;
            if (imageFile) {
              const fd = new FormData();
              fd.append('conversationId', convoId);
              if (text) fd.append('message', text);
              fd.append('image', imageFile);
              response = await fetch('/api/chat', { method: 'POST', body: fd });
            } else {
              response = await fetch('/api/chat', {
                method: 'POST',
                headers: {'Content-Type':'application/json'},
                body: JSON.stringify({ conversationId: convoId, message: text })
              });
            }
            const data = await response.json();
            if(response.ok) {
              const assistantMsg: ChatMessage = { role: 'assistant', content: data.content || data.assistantMessage || '(No response)' };
              setMessages(msgs => [...msgs, assistantMsg]);
            } else {
              console.error('Chat API error', data);
              setMessages(msgs => [...msgs, { role:'assistant', content: "Error: " + (data.error || "Unable to process") }]);
            }
          } catch(err) {
            console.error('Network error sending chat', err);
            setMessages(msgs => [...msgs, { role:'assistant', content: "Error: Network issue, please try again." }]);
          } finally {
            setLoading(false);
          }
        };

        return (
          <div className="chat-window border rounded p-4 max-w-xl mx-auto flex flex-col" style={{height:'70vh'}}>
            <MessageList messages={messages} />
            <div ref={bottomRef}></div>
            <div className="mt-auto">  {/* Input area pinned to bottom */}
              <MessageInput onSend={(text) => sendMessage(text)} disabled={loading} />
              <ImageUploadButton onUpload={(file) => sendMessage('', file)} disabled={loading} />
            </div>
          </div>
        );
      };
      export default ChatWindow;

  Explanation:


- We maintain messages in state, and add a user message to state
  immediately for optimistic UI.

- Then call the API. If there\'s an image, we use FormData (and include
  message if any).

- We handle errors by adding an \"assistant\" message with error text
  (to show in chat).

- `MessageList` is a presentational component listing messages (we'll
  implement next).

- `MessageInput` is a component with a text field and a send button.
  `ImageUploadButton` triggers a file input and calls onUpload with a
  File when selected.

- **Implement MessageList, MessageItem (Frontend):**\
  `src/components/chat/MessageList.tsx`:


- import React from 'react';
      import { ChatMessage } from '../../utils/validation';
      import MessageItem from './MessageItem';
      type MessageListProps = { messages: ChatMessage[] };
      const MessageList: React.FC<MessageListProps> = ({ messages }) => {
        return (
          <div className="mb-2 overflow-y-auto" style={{maxHeight:'60vh'}}>
            {messages.map((msg, idx) => (
              <MessageItem key={idx} role={msg.role} content={msg.content} />
            ))}
          </div>
        );
      };
      export default MessageList;

  `src/components/chat/MessageItem.tsx`:

      import React from 'react';
      import clsx from 'clsx';
      type MessageItemProps = { role: 'user' | 'assistant'; content: string; };
      const MessageItem: React.FC<MessageItemProps> = ({ role, content }) => {
        const isUser = role === 'user';
        return (
          <div className={clsx("my-1 flex", isUser ? "justify-end" : "justify-start")}>
            <div className={clsx("px-3 py-2 rounded-lg text-sm whitespace-pre-wrap", 
                                  isUser ? "bg-blue-500 text-white rounded-br-none" 
                                         : "bg-gray-200 text-black rounded-bl-none")}>
              {content}
            </div>
          </div>
        );
      };
      export default MessageItem;

  We used `clsx` (a tiny utility for conditional classes; install via
  `npm i clsx`). This nicely styles user messages differently (blue
  background, align right) vs assistant (gray, align left). Rounded
  corners slightly different for bubble effect.


- **Implement MessageInput and ImageUploadButton (Frontend):**\
  `src/components/chat/MessageInput.tsx`:


- import React, { useState } from 'react';
      import { Button } from '../ui/Button'; // or just use <button>
      type MessageInputProps = { onSend: (text: string) => void; disabled?: boolean; };
      const MessageInput: React.FC<MessageInputProps> = ({ onSend, disabled }) => {
        const [text, setText] = useState("");
        const handleSend = () => {
          if(!text.trim()) return;
          onSend(text);
          setText("");
        };
        return (
          <div className="flex">
            <input type="text"
              className="flex-1 border border-gray-300 rounded-l px-2 py-1"
              value={text}
              onChange={e => setText(e.target.value)}
              onKeyDown={e => { if(e.key==='Enter') { e.preventDefault(); handleSend(); } }}
              placeholder="Type a message..."
              disabled={disabled}
            />
            <button 
              className="bg-blue-500 text-white px-4 py-1 rounded-r disabled:bg-gray-400"
              onClick={handleSend} disabled={disabled || !text.trim()}>
              Send
            </button>
          </div>
        );
      };
      export default MessageInput;

  We disabled send if `disabled` prop true (when loading) or no text.
  `ImageUploadButton.tsx`:

      import React, { useRef } from 'react';
      type ImageUploadButtonProps = { onUpload: (file: File) => void; disabled?: boolean; };
      const ImageUploadButton: React.FC<ImageUploadButtonProps> = ({ onUpload, disabled }) => {
        const fileInputRef = useRef<HTMLInputElement>(null);
        const handleFileChange = () => {
          const file = fileInputRef.current?.files?.[0];
          if(file) {
            onUpload(file);
            // clear input value to allow re-upload same file if needed
            fileInputRef.current.value = "";
          }
        };
        return (
          <div className="mt-2">
            <button onClick={() => fileInputRef.current?.click()} 
                    className="text-blue-600 underline" disabled={disabled}>
              { disabled ? 'Uploading image...' : 'Upload an image' }
            </button>
            <input type="file" accept="image/*" style={{display:'none'}} ref={fileInputRef} onChange={handleFileChange} />
          </div>
        );
      };
      export default ImageUploadButton;

  This provides a hidden file input and a button to trigger it. On file
  select, calls `onUpload` with the file.


- **Integrate Form and Chat in App (Frontend):**\
  In `src/App.tsx`:


- import React, { useState } from 'react';
      import ReturnForm from './components/form/ReturnForm';
      import ChatWindow from './components/chat/ChatWindow';
      import './index.css'; // Tailwind styles
      function App() {
        const [conversation, setConversation] = useState<{ id: string, initialMsg?: string } | null>(null);
        const handleFormSubmitted = (res: any) => {
          // `res` corresponds to backend ReturnResponse: contains convo ID and maybe assistantMessage
          if(res.conversationId) {
            setConversation({ id: res.conversationId, initialMsg: res.assistantMessage });
          } else {
            // If no convoId, it was a regular return scenario. We can show a simple confirmation or end flow.
            alert("Return request recorded. Please return the item to store or via mail within 30 days.");
            // Possibly reset form or keep as done.
          }
        };
        return (
          <div className="App">
            { conversation ? (
                <ChatWindow convoId={conversation.id} initialMessage={conversation.initialMsg} />
              ) : (
                <ReturnForm onSubmitted={handleFormSubmitted} />
              )
            }
          </div>
        );
      }
      export default App;

  If conversation is set (complaint case), we render ChatWindow, else
  show form. In the case of a standard return, `res.conversationId`
  might be undefined and we just alert a message (the backend return
  path gave immediate instructions, we chose to just alert them or we
  could display a small message and not switch to chat at all). Possibly
  refine: if `requestType === RETURN`, we consider the flow done after
  showing a message.


- **Styling Adjustments:**

- Ensure tailwind classes are applied for spacing. We did some in
  components (e.g., card with padding).

- Might need to add container or outer layout if needed (in App, maybe
  center content).

- The ChatWindow uses a fixed height 70vh for demonstration; could
  refine to full height.

- Colors and styling can be tweaked to match branding if known. If not,
  our neutral/blue scheme is fine.

- **Test & Run Frontend (Dev Mode):**\
  Run `npm run dev`. The app should open on localhost:5173.

- Try filling the form as \"Return\": If date is older than 30 days,
  should show error (client-side).

- If within 30, on submit, the backend returns no conversation (res just
  maybe contained no convoId as we coded), we alert success. That
  scenario ends. (We might refine to show a nicer success message in UI
  instead of alert, but it\'s okay).

- Try \"Complaint\": on submit, backend returns convoId and assistant
  initial message, the app should now show ChatWindow with initial
  assistant message (like \"Please upload a photo\").

- Then type text or click upload image, see that it calls ChatWindow\'s
  sendMessage which does fetch. If backend responds, message appears.

To test integration, ensure backend is running on 8080. The Vite proxy
will forward API calls to it. We can open dev tools network to verify
calls.

**Step 3: Refinements and Testing**\
- Add any missing pieces: - Perhaps handle the case where the assistant
initial message is null by giving a default greeting. - Possibly add a
loading spinner or \"typing\...\" indicator. For simplicity, we disabled
input when loading to avoid concurrent messages, and changed the upload
button text to \"Uploading\...\". - Cross-check backend and frontend
data shapes: The backend ChatController returns `ChatMessage` (with
`content`). Our front expects `data.content` or `data.assistantMessage`.
In our ChatWindow, we attempted to handle both
(`data.content || data.assistantMessage`). Better unify: maybe modify
backend to return `{"content": "..."}` - Validate that orderOrReceipt
logic: We used one field for both order and receipt to keep it simple.
Alternatively, have separate fields and conditionally require one or the
other. But our approach should suffice with a single input and label
switching. - The form currently always selects purchaseChannel
\"ONLINE\" as default (since no default given, but state might be
undefined if not touched, but backend requires not null, maybe we should
default to one). Possibly add `defaultValues` in useForm or set default
in form fields. - Ensure date format: The input type=date yields
\"YYYY-MM-DD\". Our backend expects LocalDate, which Jackson can parse
if format matches ISO. Should be fine (the string \"2025-12-01\" will
parse to LocalDate). - Confirm enumeration casing: Our front sends
\"ONLINE\" exactly as string, backend `PurchaseChannel` is expecting
that exact (Jackson default for enums is uppercase same as defined, so
it\'s good).

- Write some basic tests (we may skip comprehensive test writing here
  due to time, but earlier we outlined how).
- Prepare for build integration:
- To deploy the app as one JAR, we incorporate the front build into
  Maven. Possibly add Maven plugin:


- <plugin>
        <groupId>com.github.eirslett</groupId><artifactId>frontend-maven-plugin</artifactId><version>1.12.0</version>
        <configuration>
          <workingDirectory>../frontend</workingDirectory>
        </configuration>
        <executions>
          <execution>
             <id>install-node</id>
             <goals><goal>install-node-and-npm</goal></goals>
             <configuration><nodeVersion>18.13.0</nodeVersion></configuration>
          </execution>
          <execution>
             <id>build-frontend</id>
             <goals><goal>npm</goal></goals>
             <configuration><arguments>run build</arguments></configuration>
             <phase>generate-resources</phase>
          </execution>
        </executions>
      </plugin>

  And in resources plugin, copy `../frontend/dist/**` to
  `classpath:static/`.\
  (This might not be necessary to do now, but part of making monorepo
  build integrated.)


- For PoC, we might manually run `npm run build` in frontend and copy
  files to `backend/src/main/resources/static` once, then package. But
  the plugin above automates it.

**Step 4: Final Verification**\
- Run `./mvnw spring-boot:run` after building front, then hit
`http://localhost:8080`. It should serve the built React app (with
proper static files). - Try a full flow: \* \"Return\" scenario: picks
up form validation, on submit maybe directly returns message (we did
`alert`, but in production could show a nice static confirmation page or
re-render a success state in ReturnForm). \* \"Complaint\" scenario:
goes to chat, can simulate sending a dummy image (the OpenAI call might
not succeed without actual key or if the image is random, but at least
ensure the request goes out and UI updates accordingly). \* If we had an
OpenAI key and the system is wired properly, the assistant should
respond analyzing the image (the content of response and correctness
depends on GPT\'s ability; we assume GPT-4 would identify defect vs wear
in an image fairly well and respond per policy instructions).

- Check edge cases:
- If user tries uploading a huge image, does it go through (maybe up to
  a few MB is fine, we didn\'t implement explicit file size check on
  front).
- If user does not attach image when asked, and just sends a text \"I
  cannot send image\", how does AI respond (our system would still call
  GPT with context but no image, and GPT might ask for image again).
- If user tries to send another message while one is loading (we
  disabled input, so fine).
- Multi-turn conversation is somewhat supported by our ChatService
  storing context and adding conversation history. But our OpenAIService
  currently only always sends initial context and the latest user query.
  If we wanted continuous conversation, we would accumulate messages in
  conversation context and send entire history each time (or use the
  messages list in ChatService).
  - If needed, adjust OpenAIService: after first image analysis, if user
    asks follow-up, we should include previous QA in prompt so GPT knows
    context. This can be done by storing messages in
    `ConversationContext.messages` and building prompt from all of them
    each time. It\'s an enhancement if we have time.

Given this is a PoC, a single round might suffice: user submits
complaint -\> AI asks for image -\> user sends image -\> AI gives
verdict. We might not handle many follow-ups beyond that.

**Step 5: Document & Deploy**\
- Document any instructions for running (like needing to set
OPENAI_API_KEY env). - The deliverables (AGENTS.md, etc.) should be
placed accordingly. - If deploying, we\'d create a jar via `mvn package`
after integrating front build, then run it on a server with
OPENAI_API_KEY configured.

We have thus followed through the research to a concrete implementation
plan, aligning with the earlier analysis and the required architecture.
Each component and service is implemented with best practices from
research (as cited and reasoned), ensuring the final system meets the
goals.

------------------------------------------------------------------------

**CLI Command Reference**

Finally, a quick reference for commands used in setting up and running
the project (for convenience, formatted for copy-paste by an AI agent or
developer):

**Project Setup & Installation**\
- *Generate Spring Boot project (backend):*

    curl https://start.spring.io/starter.zip \
      -d dependencies=web,lombok,validation \
      -d groupId=com.sinsay \
      -d artifactId=returns-backend \
      -d name=ReturnsBackend \
      -d javaVersion=21 \
      -d bootVersion=3.5.9 \
      -o backend.zip

(Unzip `backend.zip` to `backend/` directory)

- *Initialize React + Vite project (frontend):*


- npm create vite@latest frontend -- --template react-ts
      cd frontend
      npm install


- *Install Tailwind CSS (frontend):*


- npm install -D tailwindcss@^4.0 postcss autoprefixer
      npx tailwindcss init -p

  (Then configure `tailwind.config.js` content paths and include
  directives in `src/index.css`)


- *Install form & UI libraries (frontend):*


- npm install react-hook-form @hookform/resolvers zod
      npm install clsx

  (Shadcn UI components are added via copy or CLI if available, not
  through npm)


- *Install Axios (optional, if using for requests):*


- npm install axios

  (We actually used fetch in code, so not mandatory)


- *Install and configure Lombok (backend):*\
  (Already included via initializr. Ensure Lombok plugin enabled in
  IntelliJ and annotation processing on.)

**Building & Running**\
- *Run backend (Spring Boot) in dev:*

    cd backend
    ./mvnw spring-boot:run

(Requires `OPENAI_API_KEY` env variable to be set in your shell or
application.properties.)

- *Run frontend (Vite dev server):*


- cd frontend
      npm run dev

  (Open `http://localhost:5173` in browser. Vite proxies API calls to
  `localhost:8080` as configured.)


- *Build frontend for production:*


- cd frontend
      npm run build

  (Outputs files to `frontend/dist/`.)


- *Package backend with frontend assets:*\
  *(If using frontend-maven-plugin in pom.xml as described)*


- cd backend
      ./mvnw clean package

  (This will trigger npm build and include static files in JAR.)


- *Run the packaged JAR:*


- java -jar backend/target/returns-backend-0.0.1-SNAPSHOT.jar

  (The React app will be served at `http://localhost:8080`.)

**Testing**\
- *Run backend tests:*

    cd backend
    ./mvnw test

\- *Run frontend tests:*

    cd frontend
    npm run test

(You may need to configure Vitest; ensure `jsdom` environment if tests
involve DOM.)

**Useful Development Commands**\
- *Maven Wrapper install (if not present):*\
(Already included by start.spring.io; otherwise run
`mvn -N io.takari:maven:wrapper` in backend folder.) - *Start both dev
servers concurrently (optional):*\
You can open two terminals or use a tool like `concurrently` (install
globally or as dev dep) to run both:

    npx concurrently "./mvnw spring-boot:run" "npm run dev --prefix frontend"

This will launch backend on 8080 and front on 5173 simultaneously.

- *Lint and format (frontend):*


- npm run lint
      npm run format

  (If ESLint/Prettier configured in package.json scripts.)


- *Open IntelliJ on backend and start Spring Boot*:\
  Use IntelliJ run configuration (or simply use the Maven plugin in
  IntelliJ to run spring-boot:run).

- *Set environment variable for OpenAI Key (Linux/macOS):*


- export OPENAI_API_KEY="<your-api-key-here>"

  (Ensure not to expose this in any repo or logs.)

This reference covers the core commands needed to setup, run, and test
the application. Following these, a developer or AI agent can reliably
bootstrap the project environment and verify functionality, ensuring a
smooth development workflow.

------------------------------------------------------------------------

[\[1\]](https://graphite.com/guides/monorepo-frontend-backend-best-practices#:~:text=Here%27s%20a%20simple%20directory%20layout,both%20frontend%20and%20backend%20apps)
[\[62\]](https://graphite.com/guides/monorepo-frontend-backend-best-practices#:~:text=Example%20structure%20for%20frontend%20and,backend)
[\[63\]](https://graphite.com/guides/monorepo-frontend-backend-best-practices#:~:text=Best%20practices%20for%20pull%20requests,in%20a%20monorepo)
[\[65\]](https://graphite.com/guides/monorepo-frontend-backend-best-practices#:~:text=shared%20dependencies.%20,of%20the%20codebase%20that%20changed)
Best practices for managing frontend and backend in a single monorepo

<https://graphite.com/guides/monorepo-frontend-backend-best-practices>

[\[2\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Learn%20how%20to%20bundle%20your,simplifying%20both%20dev%20and%20ops)
[\[3\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Image%3A%20diagram%20showing%20how%20the,JAR%20after%20running%20mvn%20package)
[\[27\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=,Conclusion)
[\[28\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Image%3A%20diagram%20showing%20how%20the,JAR%20after%20running%20mvn%20package)
[\[44\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=Starting%20with%20a%20Simple%20Backend)
[\[45\]](https://www.jessym.com/articles/bundling-react-vite-with-spring-boot#:~:text=cd%20src%2Fmain%20npm%20create%20vite%40latest,ts)
Bundling React (Vite) with Spring Boot \| Articles \| Jessy

<https://www.jessym.com/articles/bundling-react-vite-with-spring-boot>

[\[4\]](https://everstox.com/glossary/returns-form/#:~:text=A%20returns%20form%20is%20a,data%20to%20ensure%20proper%20processing)
What is a returns form? Explained clearly

<https://everstox.com/glossary/returns-form/>

[\[5\]](https://android.com.pl/tech/549377-sinsay-zwrot-poradnik/#:~:text=W%20Sinsay%20zwrot%20jest%20mo%C5%BCliwy,przez%2030%20dni%20od%20zakup%C3%B3w)
[\[14\]](https://android.com.pl/tech/549377-sinsay-zwrot-poradnik/#:~:text=W%20stacjonarnym%20sklepie%20Sinsay%20zwrot,jest%20bezp%C5%82atny)
W Sinsay zwrot towaru to błahostka! Oto jak to zrobić

<https://android.com.pl/tech/549377-sinsay-zwrot-poradnik/>

[\[6\]](https://furgonetka.pl/zwroty/55061c54-a0ed-481b-bfff-70db8924bd4e#:~:text=Furgonetka,Zwrot%20zapakuj%20w%20jedn%C4%85%20paczk%C4%99)
Formularz zwrotu - Zwroty towaru do sklepu sinsay.pl - Furgonetka.pl

<https://furgonetka.pl/zwroty/55061c54-a0ed-481b-bfff-70db8924bd4e>

[\[7\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=Gradle)
[\[13\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=Property%20Description%20Default)
[\[17\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=var%20imageResource%20%3D%20new%20ClassPathResource%28)
[\[32\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=The%20Spring%20AI%20project%20defines,obtained%20from%20openai.com)
[\[55\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=You%20can%20register%20custom%20Java,Read%20more%20about)
[\[56\]](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html#:~:text=This%20will%20create%20an%20,chat%20model%20for%20text%20generations)
OpenAI Chat :: Spring AI Reference

<https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html>

[\[8\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=Validation)
[\[37\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=React%20Hook%20Form%20validates%20your,hook)
[\[38\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=autoComplete%3D,Field%3E%20%29%7D)
[\[39\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=,Field)
[\[40\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=return%20%28%20%3CCard%20className%3D%22w,CardContent)
[\[42\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=return%20%28%20%3CCard%20className%3D%22w,CardHeader)
[\[43\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=Client)
[\[46\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=render%3D%7B%28%7B%20field%2C%20fieldState%20%7D%29%20%3D,button%20not%20working%20on%20mobile)
[\[47\]](https://ui.shadcn.com/docs/forms/react-hook-form#:~:text=%7Bfield.value.length%7D%2F100%20characters%20,FieldError%20errors%3D%7B%5BfieldState.error%5D%7D)
React Hook Form - shadcn/ui

<https://ui.shadcn.com/docs/forms/react-hook-form>

[\[9\]](https://github.com/assistant-ui/assistant-ui#:~:text=pixel%20,easy%20extension%20to%20custom%20APIs)
[\[20\]](https://github.com/assistant-ui/assistant-ui#:~:text=pixel%20,easy%20extension%20to%20custom%20APIs)
[\[21\]](https://github.com/assistant-ui/assistant-ui#:~:text=assistant,grade%20AI%20chat%20experiences%20fast)
[\[22\]](https://github.com/assistant-ui/assistant-ui#:~:text=Why%20assistant)
[\[23\]](https://github.com/assistant-ui/assistant-ui#:~:text=,theme%20you%20can%20fully%20customize)
[\[59\]](https://github.com/assistant-ui/assistant-ui#:~:text=The%20UX%20of%20ChatGPT%20in,your%20React%20app)
GitHub - assistant-ui/assistant-ui: Typescript/React Library for AI Chat

<https://github.com/assistant-ui/assistant-ui>

[\[10\]](https://www.v-trust.com/en/blog/most-common-defects-in-garments#:~:text=2)
[\[11\]](https://www.v-trust.com/en/blog/most-common-defects-in-garments#:~:text=8)
10 Most Common Defects in Garments (with Pictures) \| V-Trust

<https://www.v-trust.com/en/blog/most-common-defects-in-garments>

[\[12\]](https://zwroty.globkurier.pl/pl/sinsay-zwrot-towaru-kompletny-przewodnik/#:~:text=Je%C5%9Bli%20jednak%20oka%C5%BCe%20si%C4%99%2C%20%C5%BCe,commerce.%20Znajomo%C5%9B%C4%87%20tych%20zasad)
Sinsay zwroty towaru

<https://zwroty.globkurier.pl/pl/sinsay-zwrot-towaru-kompletny-przewodnik/>

[\[15\]](https://community.openai.com/t/form-filling-chatbot-ideas/183868#:~:text=Basically%20I%20want%20ideas%20for,input%20to%20my%20form%20API)
[\[16\]](https://community.openai.com/t/form-filling-chatbot-ideas/183868#:~:text=gabrielimanibureau%20%20January%2023%2C%202024%2C,3%3A05pm%20%205)
[\[51\]](https://community.openai.com/t/form-filling-chatbot-ideas/183868#:~:text=%7B%20,Name%20of%20the%20hotel)
[\[52\]](https://community.openai.com/t/form-filling-chatbot-ideas/183868#:~:text=,)
Form filling chatBOT ideas - API - OpenAI Developer Community

<https://community.openai.com/t/form-filling-chatbot-ideas/183868>

[\[18\]](https://ai-sdk.dev/docs/reference/ai-sdk-ui/use-chat#:~:text=api%3F%3A)
[\[19\]](https://ai-sdk.dev/docs/reference/ai-sdk-ui/use-chat#:~:text=transport%3F%3A)
AI SDK UI: useChat

<https://ai-sdk.dev/docs/reference/ai-sdk-ui/use-chat>

[\[24\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=It%20isn%E2%80%99t%20a%20monorepo%20if,the%20same%20repo%20by%20definition)
[\[25\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=%E2%80%A2%20%201y%20ago%20,%E2%80%A2%20Edited%201y%20ago)
[\[26\]](https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/#:~:text=StoneAgainstTheSea)
For those who use monorepos, do you try to keep the frontend and backend
in the same repo, or split them apart if they\'re different languages? :
r/ExperiencedDevs

<https://www.reddit.com/r/ExperiencedDevs/comments/1ggigzr/for_those_who_use_monorepos_do_you_try_to_keep/>

[\[29\]](https://blog.jetbrains.com/idea/2025/05/coding-guidelines-for-your-ai-agents/#:~:text=Nowadays%2C%20asking%20an%20AI%20agent,working%20result%2C%20but%20it%20may)
Coding Guidelines for Your AI Agents \| The IntelliJ IDEA Blog

<https://blog.jetbrains.com/idea/2025/05/coding-guidelines-for-your-ai-agents/>

[\[30\]](https://dev.to/gianfcop98/spring-boot-and-validation-a-complete-guide-with-valid-and-validated-471p#:~:text=Spring%20Boot%20natively%20supports%20validation,Hibernate%20Validator%20under%20the%20hood)
[\[31\]](https://dev.to/gianfcop98/spring-boot-and-validation-a-complete-guide-with-valid-and-validated-471p#:~:text=Spring%20Boot%E2%80%99s%20validation%20system%20supports,380%20annotations)
[\[57\]](https://dev.to/gianfcop98/spring-boot-and-validation-a-complete-guide-with-valid-and-validated-471p#:~:text=2)
Spring Boot and Validation: A Complete Guide with \@Valid and
\@Validated - DEV Community

<https://dev.to/gianfcop98/spring-boot-and-validation-a-complete-guide-with-valid-and-validated-471p>

[\[33\]](https://github.com/openai/openai-java#:~:text=implementation%28%22com.openai%3Aopenai)
GitHub - openai/openai-java: The official Java library for the OpenAI
API

<https://github.com/openai/openai-java>

[\[34\]](https://platform.openai.com/docs/guides/images-vision#:~:text=,individual%20image%20inputs%20per%20request)
[\[35\]](https://platform.openai.com/docs/guides/images-vision#:~:text=match%20at%20L989%20B,no%20more%20than%201536%20patches)
Images and vision \| OpenAI API

<https://platform.openai.com/docs/guides/images-vision>

[\[36\]](https://stackoverflow.com/questions/24006937/lombok-annotations-do-not-compile-under-intellij-idea#:~:text=Overflow%20stackoverflow,Enable%20annotation%20processing)
Lombok annotations do not compile under Intellij idea - Stack Overflow

<https://stackoverflow.com/questions/24006937/lombok-annotations-do-not-compile-under-intellij-idea>

[\[41\]](https://github.com/shadcn-ui/ui/discussions/2137#:~:text=GitHub%20github,to%20have%20an%20uncontrolled%20input)
How to use input type file in Form? #2137 - shadcn-ui ui - GitHub

<https://github.com/shadcn-ui/ui/discussions/2137>

[\[48\]](https://agents.md/#:~:text=Think%20of%20AGENTS,agents%20work%20on%20your%20project)
[\[49\]](https://agents.md/#:~:text=One%20AGENTS,agents)
[\[60\]](https://agents.md/#:~:text=README,project%20descriptions%2C%20and%20contribution%20guidelines)
[\[64\]](https://agents.md/#:~:text=4,md%20files%20for%20subprojects)
AGENTS.md

<https://agents.md/>

[\[50\]](https://www.reddit.com/r/ChatGPTCoding/comments/1p6m036/any_tips_and_tricks_for_agentsmd/#:~:text=Any%20tips%20and%20tricks%20for,to%20burn%20a%20lot)
Any tips and tricks for AGENTS.md : r/ChatGPTCoding - Reddit

<https://www.reddit.com/r/ChatGPTCoding/comments/1p6m036/any_tips_and_tricks_for_agentsmd/>

[\[53\]](https://community.openai.com/t/it-appears-that-the-officially-recommended-java-lib-library-is-no-longer-being-maintained/716140#:~:text=It%20appears%20that%20the%20officially,2%2C%201556%2C%20November%2015%2C)
It appears that the officially recommended java-lib library is no \...

<https://community.openai.com/t/it-appears-that-the-officially-recommended-java-lib-library-is-no-longer-being-maintained/716140>

[\[54\]](https://stackoverflow.com/questions/77434808/openai-api-how-do-i-enable-json-mode-using-the-gpt-4-vision-preview-model#:~:text=OpenAI%20API%3A%20How%20do%20I,1106%2C%20you%20can%20set)
OpenAI API: How do I enable JSON mode using the gpt-4-vision \...

<https://stackoverflow.com/questions/77434808/openai-api-how-do-i-enable-json-mode-using-the-gpt-4-vision-preview-model>

[\[58\]](https://v3.tailwindcss.com/docs/guides/vite#:~:text=Start%20by%20creating%20a%20new,npm%20create%20vite%40latest)
Install Tailwind CSS with Vite

<https://v3.tailwindcss.com/docs/guides/vite>

[\[61\]](https://www.reddit.com/r/javahelp/comments/i5pgdb/spring_boot_backendreact_frontend_is_it/#:~:text=,has%20not%20caught%20up)
Spring boot backend/React frontend: is it good/common practice to \...

<https://www.reddit.com/r/javahelp/comments/i5pgdb/spring_boot_backendreact_frontend_is_it/>
