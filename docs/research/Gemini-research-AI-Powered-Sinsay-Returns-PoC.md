# **Architectural Specification: AI-Powered Verification System for Post-Purchase Logic (Sinsay Brand)**

## **1\. Executive Summary and Strategic Alignment**

The rapid acceleration of e-commerce within the fast fashion sector has precipitated a commensurate rise in reverse logistics volume. For brands like Sinsay, maintaining a streamlined, customer-centric return process is not merely an operational necessity but a critical component of brand loyalty and financial stewardship. This comprehensive research report outlines the architectural blueprint for a Proof of Concept (PoC) designed to automate the initial triage of returns and complaints. By leveraging the multimodal capabilities of GPT-4o Vision, orchestrated through a robust Java Spring Boot backend and a reactive React 19 frontend, this system aims to enforce policy compliance while reducing the manual burden on customer service agents.

The core technical challenge addressed in this document is the synthesis of disparate ecosystems: the enterprise-grade stability of the Java Virtual Machine (JVM) and the rapid innovation of the React/Vercel AI ecosystem. Traditionally, the Vercel AI SDK—the de facto standard for building streaming AI user interfaces—has been optimized for Node.js environments. This report presents a novel implementation strategy to bridge this gap, detailing the precise wire protocols required to stream generative AI responses from a Spring Boot 3.5.9 application to a React 19 client. Furthermore, the architecture is encapsulated within a monolithic repository (monorepo) structure, simplifying the deployment pipeline and ensuring tight coupling between the backend business logic and the frontend presentation layer.

This document serves as the definitive implementation guide for engineering teams, providing deep analysis of Sinsay’s regulatory environment, granular architectural decisions, and the necessary context configuration (AGENTS.md) to align AI coding assistants with project standards.

## ---

**2\. Domain Analysis: Policy Engineering and Regulatory Compliance**

To architect an effective verification system, one must first rigorously define the business rules it is tasked to enforce. In the context of Sinsay operations in Poland, the distinction between a "Return" (*Zwrot*) and a "Complaint" (*Reklamacja*) is foundational. These are not merely different user intents; they are governed by entirely different legal frameworks and operational workflows.

### **2.1 The Dichotomy of Post-Purchase Interactions**

The system must function as a rigid Policy Enforcement Point (PEP), distinguishing between voluntary returns based on store policy and statutory complaints based on product non-conformity.

#### **2.1.1 Voluntary Returns (*Zwrot*)**

Sinsay’s return policy is a contractual privilege extended to the consumer, exceeding the statutory requirements for in-store purchases. The policy dictates a strict 30-day window from the date of purchase.1 The overarching principle of a return is that the item is "unwanted but perfect." The item must be in a resalable condition, defined specifically as having original tags intact and showing no signs of use.2

From an architectural perspective, this path requires the AI to perform "Negative Verification"—it is looking for the *absence* of defects and the *presence* of identifying markers (tags). The documentation requirement is strict: the original fiscal receipt is mandatory.2 This provides a clear OCR (Optical Character Recognition) target for the AI: locate the date on the receipt and calculate the delta between the transaction date and the current date. If $\\Delta t \> 30 \\text{ days}$, the return is algorithmically rejected before a human ever sees it.

#### **2.1.2 Product Complaints (*Reklamacja*)**

In contrast, a complaint is an assertion of a defect. This process is governed by the Polish Civil Code regarding the warranty for defects (*rękojmia*). The timeframe extends to two years from the date of delivery or purchase.3 Here, the condition of "unused" does not apply; a customer may return a washed shirt if the seams disintegrated after the first wash, provided the damage is due to a manufacturing flaw and not improper use.

The data requirements shift significantly for complaints. While a receipt is preferred, regulations allow for "Proof of Purchase" in broader terms, such as a bank transfer confirmation or an order confirmation email.2 The AI's role shifts from "Negative Verification" to "Positive Forensic Analysis." It must analyze images of the alleged defect, classify the damage against known textile defect taxonomies (e.g., distinguishing between pilling, which is often wear-and-tear, and seam slippage, which is a construction fault), and assess the likelihood of user-induced damage.

### **2.2 Comparative Data Requirements**

The following table synthesizes the divergent data requirements that must be enforced by the Intake Form validation logic.

| Feature | Return (Zwrot) | Complaint (Reklamacja) |
| :---- | :---- | :---- |
| **Legal Basis** | Store Policy (Contractual) | Statutory Warranty (Civil Code) |
| **Time Limit** | 30 Days strict 1 | 2 Years statutory 3 |
| **Asset State** | Pristine, Tags Attached, Unworn. | Defective, potentially used/washed. |
| **Proof of Purchase** | Original Fiscal Receipt (mandatory). | Any proof (Receipt, Invoice, Bank confirmation). |
| **AI Vision Task** | **Verification:** Detect tags, read receipt date, ensure no stains. | **Forensics:** Classify defect type, assess manufacturing vs. user error. |
| **Financial Outcome** | Instant refund to original payment method. | 14-day review period; repair, replacement, or refund. |
| **Form Fields** | Receipt Photo, Item Photo (Tags). | Defect Photo, Defect Description, Order ID. |

### **2.3 Implications for AI Agent Design**

The system must employ a "Router Agent" pattern. Upon initial user selection, the context window of the LLM must be primed with the specific regulatory framework relevant to the user's intent. Using a generic "Check if this is okay" prompt will lead to hallucinations where the AI might reject a valid complaint because the item "looks used," or approve a late return because the item "looks new." The architecture must strictly segregate these workflows to ensure high-fidelity compliance.

## ---

**3\. System Architecture: The Monorepo Paradigm**

The requirement to deliver a unified system comprising a Java backend and a React frontend necessitates a deliberate strategy for code organization. While microservices and poly-repo approaches offer isolation, they introduce significant overhead in DevOps complexity—versioning synchronization, separate CI/CD pipelines, and contract testing. For a Proof of Concept (PoC) that aims for rapid iteration and ease of deployment, a Monorepo architecture is the superior choice.

### **3.1 Architecture Decision: Embedded Maven Strategy**

Two primary Monorepo patterns exist for Java/React hybrids: the "Separate" approach (using Docker Compose to orchestrate distinct containers) and the "Embedded" approach (where the frontend is treated as a build artifact of the backend).

This report recommends the **Embedded Maven Architecture**. In this model, the React application is located within the Maven project structure (or adjacent to it) and is compiled into static assets (HTML/JS/CSS) that are copied into the Spring Boot src/main/resources/static directory during the build process.4

**Justification:**

1. **Single Artifact Deployment:** The build process produces a single JAR file (sinsay-poc.jar). This JAR contains the optimized backend and the production-grade frontend. Deployment becomes trivial: java \-jar sinsay-poc.jar. There is no need for a separate Node.js server in production, reducing the attack surface and resource consumption.5  
2. **Unified Versioning:** Since the frontend relies on specific backend API contracts (e.g., the streaming protocol), keeping them in lock-step versioning prevents "drift" where a frontend expects version 2 of the API while the backend is still serving version 1\.  
3. **Tooling Maturity:** The frontend-maven-plugin 6 is a mature, robust tool that bridges the gap between Maven and Node/Yarn. It handles the installation of Node.js and Yarn locally within the build directory, ensuring that the build environment is hermetic and does not depend on the host machine's global Node version.

### **3.2 Directory Structure Specification**

The proposed structure aligns with standard Maven multi-module conventions but simplifies them for the PoC into a root-level aggregation.

sinsay-verification-poc/  
├── AGENTS.md \# AI Coding Assistant Context  
├── pom.xml \# Root POM (Spring Boot Parent)  
├──.gitignore  
├── README.md  
├── backend/ \# Spring Boot Application Module  
│ ├── pom.xml  
│ ├── src/  
│ │ ├── main/  
│ │ │ ├── java/com/sinsay/poc/  
│ │ │ │ ├── SinsayPocApplication.java  
│ │ │ │ ├── config/ \# AI & WebFlux Config  
│ │ │ │ ├── controller/ \# ChatController (Streaming)  
│ │ │ │ ├── service/ \# AIService (Prompt Mgmt)  
│ │ │ │ └── model/ \# DTOs & Domain Objects  
│ │ │ └── resources/  
│ │ │ ├── application.yml  
│ │ │ └── prompts/ \# System Prompts (\*.st)  
│ └── target/  
└── frontend/ \# React 19 Application Module  
├── package.json  
├── vite.config.ts  
├── tsconfig.json  
├── tailwind.config.js  
├── components.json \# Shadcn Configuration  
├── src/  
│ ├── components/ \# Shadcn UI Components  
│ ├── hooks/ \# Custom Hooks (useVerification)  
│ ├── lib/ \# Utilities (utils.ts)  
│ └── App.tsx  
└── public/

### **3.3 Build Lifecycle Integration**

The integration logic within the backend/pom.xml is critical. It must orchestrate the build such that the frontend is compiled *before* the backend packaging phase.

XML

\<plugin\>  
    \<groupId\>com.github.eirslett\</groupId\>  
    \<artifactId\>frontend-maven-plugin\</artifactId\>  
    \<version\>1.15.0\</version\>  
    \<configuration\>  
        \<workingDirectory\>../frontend\</workingDirectory\>  
        \<installDirectory\>target\</installDirectory\>  
    \</configuration\>  
    \<executions\>  
        \<execution\>  
            \<id\>install node and npm\</id\>  
            \<goals\>\<goal\>install-node-and-npm\</goal\>\</goals\>  
            \<configuration\>  
                \<nodeVersion\>v20.10.0\</nodeVersion\>  
            \</configuration\>  
        \</execution\>  
        \<execution\>  
            \<id\>npm run build\</id\>  
            \<goals\>\<goal\>npm\</goal\>\</goals\>  
            \<configuration\>  
                \<arguments\>run build\</arguments\>  
            \</configuration\>  
        \</execution\>  
    \</executions\>  
\</plugin\>

*Note:* The maven-resources-plugin is then configured to copy the contents of ../frontend/dist into target/classes/static, allowing Spring Boot to serve the Single Page Application (SPA).4

## ---

**4\. Frontend Engineering: React 19 and Modern UX**

The frontend requirement specifies **React 19**, **Shadcn UI**, and **TailwindCSS**. This combination represents the "bleeding edge" of React development, introducing new paradigms for state management and form handling while also presenting specific compatibility challenges.

### **4.1 React 19: The Shift to Actions**

React 19 introduces a fundamental shift in how forms and asynchronous operations are handled, moving away from useEffect and manual state tracking toward **Actions** and the useActionState hook.7

For the Sinsay Intake Form, this allows for a cleaner architecture. Instead of manually handling onSubmit events, managing isLoading states, and catching errors, we can leverage the native pending states of Actions.

Implementation Insight:  
While react-hook-form has been the standard for years, React 19's useActionState (formerly useFormState) provides a native primitive for handling form submission results. However, given the requirement for client-side validation (max 5 fields, image previews), a hybrid approach using Zod for schema validation coupled with React 19 Actions is optimal.9

### **4.2 Shadcn UI & React 19 Compatibility**

A significant challenge identified in the research is the peer dependency conflict between Shadcn UI (which relies on Radix UI primitives) and React 19\. Radix UI components often specify peerDependencies of \`react@^16 |

| ^17 |  
| ^18, causing npm install\` to fail when React 19 is present.10  
Resolution Strategy:  
The implementation plan must explicitly utilize the \--legacy-peer-deps flag during the initialization of Shadcn and the installation of its components. This bypasses the strict version check, allowing the React 19 RC (Release Candidate) to function with the existing Radix primitives, which are generally compatible at the runtime level despite the metadata mismatch.10

### **4.3 Intake Form Design Specification**

The intake form is the primary interface for data collection. To strictly adhere to the "Max 5 Fields" requirement while supporting complex branching logic, a **Dynamic Field** pattern is required.

**Field 1: Intent Selector (Branching Logic)**

* **Type:** Radio Group (Shadcn RadioGroup).  
* **Label:** "Rodzaj Zgłoszenia" (Type of Request).  
* **Options:** "Zwrot towaru" (Return \- 30 days) vs. "Reklamacja" (Complaint \- Defect).  
* **Impact:** This selection completely reconfigures the subsequent fields and the backend AI prompt.

**Field 2: Product Identification**

* **Type:** Input Text.  
* **Label:** "Numer Zamówienia / Nazwa Produktu" (Order Number / Product Name).  
* **Validation:** Regex validator for Sinsay order format (typically alphanumeric).

**Field 3: Proof of Purchase**

* **Type:** File Input (Image).  
* **Label:**  
  * *If Return:* "Zdjęcie Paragonu" (Photo of Receipt) \- Critical for date verification.  
  * *If Complaint:* "Dowód Zakupu" (Proof of Purchase) \- Receipt, invoice, or bank transfer PDF.

**Field 4: Visual Evidence (The AI Payload)**

* **Type:** File Input (Multi-image, max 3).  
* **Label:**  
  * *If Return:* "Zdjęcie Produktu z Metkami" (Photo of Product with Tags). The prompt will instruct: "Ensure tags are clearly visible."  
  * *If Complaint:* "Zdjęcie Wady" (Photo of Defect). The prompt will instruct: "Close-up of the damage."

**Field 5: Description (Conditional)**

* **Type:** Textarea.  
* **Label:** "Opis Problemu" (Description of Issue).  
* **Logic:** Optional for Returns (implicit "changed mind"), Mandatory for Complaints (required by law to state the defect).3

## ---

**5\. Backend Engineering: Spring Boot 3.5 & Spring AI**

The backend serves as the secure orchestrator, managing the sensitive OpenAI API keys and performing the heavy lifting of image processing and protocol adaptation. The choice of **Spring Boot 3.5.9** implies a forward-looking architecture that fully embraces Java 21 features.

### **5.1 Project Loom and Virtual Threads**

Spring Boot 3.2+ introduced support for Virtual Threads (Project Loom), and by version 3.5, this will be the default concurrency model. For an AI application, this is transformative. Calls to the OpenAI API are I/O bound—the application spends most of its time waiting for the LLM to generate tokens.

In a traditional thread-per-request model, waiting for a 30-second GPT-4o analysis would block a platform thread, limiting scalability. With Virtual Threads, the JVM can handle thousands of concurrent AI verification requests with a minimal memory footprint, as the platform thread is released to do other work while the virtual thread "parks" during the I/O wait. This eliminates the need for complex reactive code (WebFlux) strictly for performance reasons, although WebFlux remains valuable for the *streaming* aspect of the response.12

### **5.2 Spring AI vs. Official OpenAI SDK**

The research highlights a choice between the official openai-java SDK and the spring-ai framework.

Recommendation: Spring AI  
Spring AI is recommended for this architecture due to its abstraction layer ChatClient. This interface decouples the application code from the specific model provider. While the PoC uses OpenAI (GPT-4o), an enterprise might later switch to Azure OpenAI (for compliance) or Bedrock without rewriting the controller logic.14 Furthermore, Spring AI’s PromptTemplate and Media classes provide a cleaner, more "Spring-native" way to handle multimodal inputs compared to constructing raw JSON payloads in the official SDK.

### **5.3 Controller Implementation: The Streaming Bridge**

The most complex technical requirement is bridging the Spring Boot backend with the Vercel AI SDK frontend. Vercel's useChat hook expects a proprietary **Data Stream Protocol** over HTTP, which differs from standard Server-Sent Events (SSE).

The Protocol Mismatch:  
Standard Spring SSE emits events like:  
data: {"content": "Hello"}  
Vercel AI SDK (Data Stream Protocol v1) expects:  
0:"Hello"  
0:" world"  
8:\[{"some":"data"}\]

* **0:** Represents a text chunk from the LLM.  
* **8:** Represents a data part (e.g., verification status JSON).  
* **e:** Represents an error.

Implementation Strategy:  
The Controller must manually format the stream. We cannot simply return a Flux\<ChatResponse\>. We must map the response stream to a Flux\<String\> where each string is formatted according to the Vercel protocol.

Java

@PostMapping(value \= "/verify", produces \= MediaType.TEXT\_EVENT\_STREAM\_VALUE)  
public Flux\<String\> verifyReturn(@RequestBody IntakeRequest request) {  
    // 1\. Construct the AI Prompt with User Text and Images  
    UserMessage userMessage \= new UserMessage(request.getDescription(),   
        List.of(new Media(MimeTypeUtils.IMAGE\_JPEG, request.getImageUrl())));  
      
    // 2\. Stream from OpenAI  
    return chatClient.prompt()  
       .user(userMessage)  
       .stream()  
       .content()  
        // 3\. Adapter Layer: Transform to Vercel Protocol  
       .map(chunk \-\> {  
            // Escape special JSON characters  
            String sanitized \= chunk.replace("\\"", "\\\\\\"").replace("\\n", "\\\\n");  
            return "0:\\"" \+ sanitized \+ "\\"\\n";   
        });  
}

This explicit adaptation is critical. Without it, the frontend useChat hook will fail to parse the stream, resulting in connection errors or empty responses.15

## ---

**6\. AI Integration Strategy: Vision and Verification**

The core value proposition of this system is the AI's ability to act as a qualified textile expert. This requires sophisticated prompt engineering and the utilization of GPT-4o's vision capabilities.

### **6.1 Vision Token Usage and Optimization**

GPT-4o processes images by dividing them into 512x512 pixel tiles. A high-resolution photo from a modern smartphone can consume thousands of tokens. To optimize costs and latency:

* **Resize at the Edge:** The React frontend should resize images to a maximum of 1024px on the longest side before upload.  
* **Detail Setting:** Set the detail parameter to high only for the "Defect" photo in complaints. For the "Receipt" photo in returns, low detail (512x512) is often sufficient for OCR tasks, significantly reducing cost.

### **6.2 Prompt Engineering: The Textile Expert**

The system prompt must inject domain-specific knowledge to prevent generic responses. We leverage the **ASTM D3990 Standard Terminology for Fabric Defects**.17

**Complaint Analysis Prompt Structure:**

"You are a Senior Quality Assurance Specialist for Sinsay. Analyze the attached image for textile defects.  
Reference ASTM D3990 standards. Look specifically for:

1. **Barre:** Unintentional repetitive horizontal patterns (knitting defect).  
2. **Slub:** Abrupt thickening of yarn (spinning defect).  
3. **Seam Slippage:** Fabric yarns pulling out of the seam (construction defect).  
4. **Pilling:** Small fuzz balls. Note: Slight pilling is normal wear; excessive pilling may be a defect.

**Assessment Logic:**

* If the defect is mechanical (e.g., a straight cut with clean edges), flag as likely 'User Damage \- Scissors'.  
* If the defect is chemical (e.g., bleached spot on dark fabric), flag as 'User Damage \- Bleach'.  
* If the defect is structural (e.g., seam unraveling), flag as 'Manufacturing Defect'.

Return your finding as a JSON object in the data stream."

### **6.3 Receipt Verification Logic**

For returns, the AI must extract the date.

"Locate the date on this receipt. Today is. Calculate the number of days elapsed.  
If elapsed \> 30, status is REJECTED.  
If elapsed \<= 30, look for the item name '' on the list.  
If item found and date valid, status is APPROVED."

## ---

**7\. Developer Experience: The AGENTS.md Standard**

To facilitate rapid development using AI coding assistants (like GitHub Copilot, Cursor, or Windsurf), the project includes an AGENTS.md file. This file acts as a context anchor, providing the AI tools with the architectural rules of the road.18

### **7.1 AGENTS.md Content**

# **AGENTS.md \- Sinsay Verification PoC Context**

## **Project Identity**

* **Name:** Sinsay AI Verification PoC  
* **Stack:** Java 21, Spring Boot 3.5, React 19, Shadcn UI, TailwindCSS.  
* **Architecture:** Monorepo (Maven wrapper).

## **Architectural Invariants (DO NOT VIOLATE)**

1. **Streaming:** All Chat/AI interactions MUST use the Vercel Data Stream Protocol.  
   * Text prefix: 0:  
   * Data prefix: 8:  
   * DO NOT use standard SSE (data:...).  
2. **Frontend-Backend Contract:**  
   * Frontend runs on port 5173 (Vite).  
   * Backend runs on port 8080\.  
   * Vite proxy is configured; frontend requests to /api/... are forwarded.  
3. **State Management:**  
   * Use useActionState for forms.  
   * Use useChat for AI interaction.  
   * Avoid Redux or Context for local form state.

## **Coding Standards**

* **Java:** Use var for local variables. Use Records (public record Dto(...)) for all data transfer objects. Use constructor injection.  
* **React:** Functional components only. Use zod for validation schemas. Tailwind utility classes for styling (no CSS files).  
* **Shadcn:** Components are in frontend/src/components/ui. Do not modify them unless necessary for React 19 compatibility.

## **Domain Dictionary**

* **Zwrot (Return):** Voluntary, 30 days, unused, tags attached.  
* **Reklamacja (Complaint):** Statutory, 2 years, defect based.  
* **Protocol:** The wire format for the chat stream.

## ---

**8\. Implementation Roadmap**

### **Phase 1: Foundation (Days 1-2)**

1. **Scaffold Monorepo:** Initialize the Maven parent project.  
2. **Frontend Setup:** Initialize Vite \+ React 19\. Install Shadcn UI using the \--legacy-peer-deps flag to resolve React 19 conflicts.10  
3. **Backend Setup:** Initialize Spring Boot with spring-ai-openai and webflux dependencies.  
4. **Build Integration:** Configure frontend-maven-plugin to ensure mvn clean install builds the full stack.

### **Phase 2: Core Features (Days 3-5)**

1. **Intake Form:** Build the 5-field dynamic form in React. Implement zod validation.  
2. **Controller Implementation:** Create the ChatController in Spring Boot. Implement the Flux.map() logic to adapt OpenAI chunks to the Vercel Protocol 0:"..." format.  
3. **Vision Integration:** Implement the Media object handling in Spring AI to upload images to GPT-4o.

### **Phase 3: Policy Intelligence (Days 6-8)**

1. **Prompt Engineering:** Refine the "Textile Expert" and "Receipt Validator" prompts. Test with real images of receipts and damaged clothes.  
2. **Data Channel:** Implement the 8:{...} protocol to stream structured decisions (Approved/Rejected) alongside the text explanation.  
3. **UI Feedback:** Update the React UI to disable the "Proceed" button if the AI returns a "Rejected" status in the data stream.

### **Phase 4: Polish & Delivery (Days 9-10)**

1. **Styling:** Apply Sinsay branding (monochrome, stark typography) using Tailwind.  
2. **Testing:** Write JUnit tests for the protocol adapter (mocking the AI response).  
3. **Documentation:** Finalize README.md and AGENTS.md.

## ---

**9\. CLI Command Reference**

The following commands are essential for the development lifecycle of this specific architecture.

**1\. Project Initialization (Backend)**

Bash

\# Generate Spring Boot 3.5 Skeleton  
curl https://start.spring.io/starter.zip \\  
    \-d dependencies=web,webflux,spring-ai-openai,lombok \\  
    \-d javaVersion=21 \\  
    \-d bootVersion=3.5.0-SNAPSHOT \\  
    \-d type\=maven-project \\  
    \-o backend.zip && unzip backend.zip \-d backend

**2\. Project Initialization (Frontend \- React 19\)**

Bash

\# Initialize Vite with React-TS  
npm create vite@latest frontend \-- \--template react-ts

\# Install Dependencies (Critical: Legacy Peer Deps for Shadcn)  
cd frontend  
npm install  
npx shadcn@latest init \--legacy-peer-deps  
\# Select: Style=New York, Base Color=Zinc, CSS Variables=Yes

**3\. Running the Development Environment**

* **Terminal 1 (Backend):** ./mvnw spring-boot:run  
* **Terminal 2 (Frontend):** cd frontend && npm run dev

**4\. Building for Production**

Bash

\# From the root directory  
./mvnw clean install  
\# The resulting artifact is at backend/target/backend-0.0.1-SNAPSHOT.jar

**5\. Docker Build (Optional)**

Bash

docker build \-t sinsay-poc.  
docker run \-p 8080:8080 \-e OPENAI\_API\_KEY=sk-... sinsay-poc

## ---

**10\. Conclusion**

This research report provides a robust architectural foundation for Sinsay’s AI-powered verification system. By combining the enterprise reliability of **Spring Boot** with the cutting-edge user experience of **React 19** and the cognitive capabilities of **GPT-4o Vision**, the proposed PoC addresses the critical business need for automated, policy-compliant return processing.

The key innovation identified—and solved—in this report is the protocol adaptation required to connect the Java backend with the Vercel AI SDK. This allows Sinsay to leverage the rich ecosystem of React AI UI components without abandoning their investment in the Java ecosystem. With the inclusion of the AGENTS.md context and the detailed implementation plan, the engineering team is equipped to move immediately from concept to code.

#### **Works cited**

1. 30 dni na zwrot \- Sinsay, accessed January 20, 2026, [https://www.sinsay.com/pl/pl/30-dni-na-zwrot-help](https://www.sinsay.com/pl/pl/30-dni-na-zwrot-help)  
2. Regulamin zwrotów i wymian towarów w sklepach Sinsay, accessed January 20, 2026, [https://www.sinsay.com/pl/pl/help-regulamin-zwrotow-i-wymian-towarow-w-sklepach-sinsay](https://www.sinsay.com/pl/pl/help-regulamin-zwrotow-i-wymian-towarow-w-sklepach-sinsay)  
3. Jak złożyć reklamację? \- Sinsay, accessed January 20, 2026, [https://www.sinsay.com/pl/pl/help-jak-zlozyc-reklamacje](https://www.sinsay.com/pl/pl/help-jak-zlozyc-reklamacje)  
4. Including React in your Spring Boot maven build | by Geoff Bourne | Medium, accessed January 20, 2026, [https://medium.com/@itzgeoff/including-react-in-your-spring-boot-maven-build-ae3b8f8826e](https://medium.com/@itzgeoff/including-react-in-your-spring-boot-maven-build-ae3b8f8826e)  
5. Spring Boot with ReactJS using Maven plugins \- YouTube, accessed January 20, 2026, [https://www.youtube.com/watch?v=7XxH-G9ckeU](https://www.youtube.com/watch?v=7XxH-G9ckeU)  
6. Allow overriding URL for musl-based NodeJS separately · Issue \#965 · eirslett/frontend-maven-plugin \- GitHub, accessed January 20, 2026, [https://github.com/eirslett/frontend-maven-plugin/issues/965](https://github.com/eirslett/frontend-maven-plugin/issues/965)  
7. React v19 Discussion · react-hook-form · Discussion \#11832 \- GitHub, accessed January 20, 2026, [https://github.com/orgs/react-hook-form/discussions/11832](https://github.com/orgs/react-hook-form/discussions/11832)  
8. React v19, accessed January 20, 2026, [https://react.dev/blog/2024/12/05/react-19](https://react.dev/blog/2024/12/05/react-19)  
9. Using react-hook-form with React 19, useActionState, and Next.js 15 App Router, accessed January 20, 2026, [https://markus.oberlehner.net/blog/using-react-hook-form-with-react-19-use-action-state-and-next-js-15-app-router](https://markus.oberlehner.net/blog/using-react-hook-form-with-react-19-use-action-state-and-next-js-15-app-router)  
10. Next.js 15 \+ React 19 \- shadcn/ui, accessed January 20, 2026, [https://ui.shadcn.com/docs/react-19](https://ui.shadcn.com/docs/react-19)  
11. Issue Installing shadcn on a new Next.js 15 Project : r/nextjs \- Reddit, accessed January 20, 2026, [https://www.reddit.com/r/nextjs/comments/1ge4nwt/issue\_installing\_shadcn\_on\_a\_new\_nextjs\_15\_project/](https://www.reddit.com/r/nextjs/comments/1ge4nwt/issue_installing_shadcn_on_a_new_nextjs_15_project/)  
12. Azure Open AI \- very different results on gpt4o between Python and Java API, accessed January 20, 2026, [https://stackoverflow.com/questions/78691202/azure-open-ai-very-different-results-on-gpt4o-between-python-and-java-api](https://stackoverflow.com/questions/78691202/azure-open-ai-very-different-results-on-gpt4o-between-python-and-java-api)  
13. Server-Sent Events in Spring \- Baeldung, accessed January 20, 2026, [https://www.baeldung.com/spring-server-sent-events](https://www.baeldung.com/spring-server-sent-events)  
14. OpenAI SDK Chat (Official) :: Spring AI Reference, accessed January 20, 2026, [https://docs.spring.io/spring-ai/reference/api/chat/openai-sdk-chat.html](https://docs.spring.io/spring-ai/reference/api/chat/openai-sdk-chat.html)  
15. Troubleshooting: useChat/useCompletion stream output contains 0:... instead of text, accessed January 20, 2026, [https://ai-sdk.dev/docs/troubleshooting/strange-stream-output](https://ai-sdk.dev/docs/troubleshooting/strange-stream-output)  
16. Stream Protocols \- AI SDK UI, accessed January 20, 2026, [https://ai-sdk.dev/docs/ai-sdk-ui/stream-protocol](https://ai-sdk.dev/docs/ai-sdk-ui/stream-protocol)  
17. ASTM D-3990-Fabric Defects | PDF | Textiles | Knitting \- Scribd, accessed January 20, 2026, [https://www.scribd.com/document/631825038/ASTM-D-3990-Fabric-Defects](https://www.scribd.com/document/631825038/ASTM-D-3990-Fabric-Defects)  
18. How I Write My AGENTS.md Files \- Best Practices, accessed January 20, 2026, [https://www.youtube.com/watch?v=6w88NVf2\_lY](https://www.youtube.com/watch?v=6w88NVf2_lY)  
19. AGENTS.md, accessed January 20, 2026, [https://agents.md/](https://agents.md/)