### Key Recommendations
- **Intake Form Design**: Research suggests limiting fields to essentials like order number, purchase date, channel, type (return vs. complaint), and description with image upload to streamline user experience while complying with Sinsay's 30-day return and 2-year complaint policies.
- **AI Integration**: Use Spring AI for seamless OpenAI GPT-4o vision in Java, as it handles prompts and context better for defect analysis than the official SDK alone; evidence leans toward high accuracy in distinguishing factory defects from wear.
- **Frontend Tools**: React Hook Form pairs well with Shadcn UI and TailwindCSS for validation; it seems likely to reduce errors in multi-step flows like form to chatbot transitions.
- **Architecture**: For PoC, embed React in Spring Boot monorepo via Maven frontend plugin; this simplifies deployment but may add complexity—balanced views indicate it's efficient for small teams.
- **Development Optimization**: Structure prompts with clear context from forms; AI agents perform best with modular code and CLI tools like Spring Initializr.

#### Core Functionality Overview
The system should start with a simple form to categorize returns vs. complaints, then use GPT-4o vision for image-based defect verification, enforcing Sinsay rules like 30-day limits for returns and defect proofs for complaints.

#### Potential Challenges
It appears that Vercel AI SDK UI works via REST with Spring Boot, though some integration patterns require custom streaming; always validate AI outputs as vision models may misclassify subtle defects like stains on patterned fabrics.

#### Implementation Tips
Prioritize React Hook Form for frontend and Spring AI for backend; use monorepo for shared configs, but separate deployments if scaling.

---

### 1. Executive Summary (key recommendations)
This research outlines a PoC for an AI-powered returns and complaints system for Sinsay, integrating GPT-4o vision with Spring Boot 3.5.9 backend and React 19 frontend in a monorepo. Key recommendations include:
- **Intake Form**: 4 fields (Order Number, Purchase Date, Channel, Type/Reason) plus image upload, derived from Sinsay policies to route to return (30 days, unused) or complaint (2 years, defects like tears, stains).
- **Tools**: Spring AI for OpenAI integration, React Hook Form with Shadcn UI, Nx for monorepo.
- **Architecture**: Embedded React in Spring Boot for simplicity in PoC.
- **AI Optimization**: Structured prompts combining form data and policy rules for defect detection (e.g., tears, stains, seams).
- **Implementation**: Step-by-step CLI setup, code examples for form, API, AI service.

### 2. Intake Form Specification
#### Recommended Fields
Based on Sinsay regulations analysis, limit to 4 fields to minimize friction:
1. Order Number/Receipt ID (text, required)
2. Purchase Date (date, required)
3. Purchase Channel (dropdown: Online/Store, required)
4. Type/Reason (radio: Return/Complaint, with conditional description textarea and image upload)

#### Field Types, Validation, Conditional Logic
- Order Number: String, validate format (e.g., alphanumeric), required for all.
- Purchase Date: Date picker, validate within 30 days for returns, 2 years for complaints.
- Channel: Dropdown, triggers conditional fields (e.g., invoice for online).
- Type: Radio, if "Complaint", show defect description and require images; validate against policy (e.g., no returns for used items).
Conditional: Online channel shows return methods; Complaint adds defect types dropdown (tears, stains, etc.).

#### Form Flow Diagram
```
Start -> Enter Fields (1-4) -> If Type=Return: Check 30 days & unused -> Route to Policy Enforcement
      -> If Type=Complaint: Upload Images & Describe Defect -> AI Analysis -> Decision
End: Submit to AI Chatbot with Context
```

#### Regulatory Justification
Fields align with Sinsay needs: Receipt/ID and date for verification (30 days return, 2 years complaint); channel differentiates processes (store/online); type routes to zwrot (unused) vs. reklamacja (defects like tears, stains, zippers).

#### Example JSON Schema
```json
{
  "type": "object",
  "properties": {
    "orderNumber": {"type": "string", "minLength": 1},
    "purchaseDate": {"type": "string", "format": "date"},
    "channel": {"enum": ["online", "store"]},
    "type": {"enum": ["return", "complaint"]},
    "description": {"type": "string"},
    "images": {"type": "array", "items": {"type": "string"}}
  },
  "required": ["orderNumber", "purchaseDate", "channel", "type"]
}
```

### 3. Answers to Research Questions
1. **Initial Intake Form Design**: Optimal 4 fields as above; essential for path: date/channel for eligibility, type for routing. Conditional: Online shows courier options. Differentiate by asking if defect-related. Validations: Date ranges per policy.

2. **Form-to-AI Workflow**: Pass JSON via API to AI service; pre-populate context (e.g., "User purchased online on [date], complaining about [defect]"). Patterns: Hybrid structured-to-conversational, using agents for follow-ups.

3. **Vercel AI SDK Compatibility**: UI components (React) work with Spring Boot via REST; limitations: No native Java, use custom API for streaming. Alternatives: Custom React chat with useChat hook.

4. **Monorepo Architecture**: Embed React in Spring for PoC simplicity; best practices: Nx for build orchestration, shared configs. Pros: Single repo; cons: Language mismatches.

5. **Spring Boot Best Practices**: Structure: Controllers/Services for workflows. Use Spring AI over official SDK for prompts/configs. API: POST /submit-form, /upload-image. Error: Fallback prompts. Validation: Bean Validation.

6. **React Best Practices**: Multi-step: useSteps hook. Form: React Hook Form + Zod. State: Zustand for persistence. Integration: Shadcn components. Upload: Dropzone.

7. **AI Agent Development Optimization**: CLI: spring initializr, npx create-react-app. Libraries: Spring AI, Radix UI. Patterns: Modular services. Structure: Clear folders to avoid confusion.

8. **OpenAI Integration Patterns**: Spring AI preferred; prompts: "Analyze image for defects like [list], using policy [rules]". Context: Append form data. Parse: JSON responses.

| Defect Type | Description | AI Detection Notes |
|-------------|-------------|--------------------|
| Tears | Rips in fabric | Edge detection |
| Stains | Spots/marks | Color anomaly |
| Zipper Damage | Broken zips | Shape analysis |
| Etc. | ... | ... |

### 4. Tool Recommendations
- **Spring Boot Libraries**: spring-ai-openai-spring-boot-starter (mvn install). Justified: LLM knowledge, version 1.0+.
  - CLI: `mvn dependency:add groupId=org.springframework.ai artifactId=spring-ai-openai-spring-boot-starter`
- **React Form Libraries**: React Hook Form (`npm i react-hook-form`). Compatible with Shadcn, known to LLMs.
- **React Packages**: shadcn-ui (`npx shadcn@latest init`), tailwindcss (`npm i tailwindcss`).
- **IntelliJ Plugins**: Spring Boot, Lombok support.
- **Testing**: JUnit 5 (`mvn dependency:add`), Vitest (`npm i vitest`).
Justifications based on AI usability and versions.

### 5. Architecture Decision
Recommendation: Embedded React in Spring Boot for PoC (Maven builds frontend).
Pros: Single deployment, simplified; Cons: Build time increase. Deployment: JAR file.

### 6. Complete AGENTS.md Files
#### Spring Boot AGENTS.md
```
# AI Agent Guidance for Spring Boot
## Coding Standards
- Use Java 21 features.
- Lombok for boilerplate.
- Modular services.

## Patterns
- MVC: Controllers for APIs.
- AI: Use Spring AI ChatClient.

## Pitfalls
- Avoid tight coupling with OpenAI; use abstractions.

## Guidance
- Generate prompts with form context.
- Validate AI responses.
```

#### React AGENTS.md
```
# AI Agent Guidance for React
## Coding Standards
- Functional components, hooks.
- TypeScript.

## Patterns
- Component per feature.
- State: Zustand.

## Pitfalls
- Overuse global state.

## Guidance
- Use React Hook Form for validation.
- Pass form data to API.
```

### 7. Project Structure
```
monorepo/
├── backend/ (Spring Boot)
│   ├── src/main/java/com/sinsay/
│   │   ├── controller/ (FormController.java)
│   │   ├── service/ (AiService.java)
│   ├── pom.xml
├── frontend/ (React)
│   ├── src/components/ (IntakeForm.tsx)
│   ├── package.json
├── nx.json (for monorepo)
```
Explanations: Backend for APIs/AI; frontend for UI; nx for builds.

### 8. Implementation Plan
1. Setup: `spring initializr`, `npx create-react-app frontend --template typescript`.
2. Code Examples:
   - React Form:
     ```tsx
     import { useForm } from 'react-hook-form';
     // ...
     const { register, handleSubmit } = useForm();
     // Form fields with validation
     ```
   - Spring Controller:
     ```java
     @RestController
     public class FormController {
         @PostMapping("/submit")
         public Response submit(@RequestBody FormData data) { /* process */ }
     }
     ```
   - AI Service:
     ```java
     @Service
     public class AiService {
         private final ChatClient chatClient;
         // Prompt: "Analyze image for defects: " + policy
     }
     ```
   Workflow: Form submit -> API -> AI analysis -> Chatbot.

### 9. CLI Command Reference
- Init: `mvn archetype:generate ...`
- Deps: `mvn install`
- Build: `mvn clean package`
- Test: `mvn test`
- React: `npm start`

### 10. Additional Considerations
Security: API keys in env, GDPR for data. Scalability: Cloud deployment. Monitoring: Logs for AI errors. Privacy: Anonymize images.

#### Key Citations
- [Sinsay Returns Regulations](https://www.sinsay.com/pl/pl/help-regulamin-zwrotow-i-wymian-towarow-w-sklepach-sinsay)
- [Sinsay 30-Day Return](https://www.sinsay.com/pl/pl/30-dni-na-zwrot-help)
- [Sinsay Complaints Guide](https://www.sinsay.com/pl/pl/help-jak-zlozyc-reklamacje)
- [Ecommerce Returns Best Practices](https://www.gorgias.com/blog/ecommerce-returns-best-practices)
- [Client Intake Best Practices](https://www.cognitoforms.com/blog/644/intake-forms)
- [Vercel AI SDK](https://vercel.com/docs/ai-sdk)
- [Monorepo with Spring/React](https://medium.com/@bhargavkanjarla01/how-to-combine-a-java-spring-boot-back-end-with-a-reactjs-front-end-app-ed8d8ca65285)
- [OpenAI in Spring](https://docs.spring.io/spring-ai/reference/api/chat/openai-sdk-chat.html)
- [Prompt for Defects](https://www.leewayhertz.com/gpt-4-vision)
- [React UI Libraries](https://hashbyt.com/blog/19-best-react-ui-component-libraries)
- [AI Agents in Spring](https://dev.to/yuriybezsonov/a-practical-guide-to-building-ai-agents-with-java-and-spring-ai-part-1-create-an-ai-agent-4f4a)
- [Clothing Defects](https://pmc.ncbi.nlm.nih.gov/articles/PMC10900799)
