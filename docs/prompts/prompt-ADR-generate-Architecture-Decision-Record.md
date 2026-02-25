Generate an Architecture Decision Record document in ./doc folder, with technical implementation details for chat bot application based on OpenAI Java SDK on the backend and CopilotKit UI framework on the frontend (React). The application we are creating should meet the requirements of the PRD document, which is also in the same ./doc folder, and you should also analyze the functional requirements described in the PRD document, i.e., Product Requirement Document, when creating this ADR document. And transfer to architectural decisions, using these two tools I provided, which are the official OpenAI Java SDK and CopilotKit UI components, which will contain interactive elements for agents based on AG-UI protocol. And the application should allow sending photos, receiving these photos from the frontend, sending this image to the multi-modal model, handle several types of prompts also possible to choose depending on the user's decision on the frontend. And then conducting continuation of the conversation in the chat with the user. Analyze the functional requirements carefully in order to implement the backend.

For persistence on BE you can use SQLite to save data from the form, conversation history and logs.
Analyse what else should be implemented based on PRD document.

Analyse also @AGENTS.md and @src/AGENTS.md and @Frontend/AGENTS.md files to align ADR with instructions we provided for the agents.
All these documents (ADR, PRD and AGENTS.md files) should be aligned and consistent. Make sure we will not have any contradicting statements, instructions or decisions.

You should also start with making research about how to implement CopilotKit UI framework (based on AG-UI) together with OpenAI Java SDK, and if they can talk to each other, what data contract we should create for them to communicate. Or maybe they need some additional intermediary layer that would translate AG-UI protocol to our BE in Java?

Search if OpenAI Java SDK has some examples for integration with AG-UI. If not, maybe Spring AI framework has some integration or examples for AG-UI protocol and/or CopilotKit?
In this case we can use different Java framework or SDK on BE that will make it easier to integrate with CopilotKit UI.

Before you create ADR document, make sure you fully understand how this application should work, how BE will communicate with FE, how to integrate Java BE with CopilotKit UI and AG-UI to make sure we know how to make it work.

If you can't find any information or examples how to implement CopilotKit or AG-UI with Java BE, please do not try to implement this yourself. Inform me about this, and we should switch to simpler FE UI components, e.g. to Vercel AI SDK UI (you can also research if Vercel AI SDK has integration with CopilotKit and/or AG-UI, and maybe we can use some parts of this library, e.g. hooks, to make communication between BE and FE easier).

Documentation:

- https://github.com/CopilotKit/CopilotKit
- https://github.com/ag-ui-protocol/ag-ui
- https://github.com/openai/openai-java

Context7 docs IDs (handlers to use to get docs with Context7 MCP):

- websites/copilotkit_ai
- copilotkit/copilotkit
- openai/openai-java
