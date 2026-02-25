# Create AGENTS.md with Testing and quality standards

## My original prompt:

Prepare a new version of AGENTS.md, analyzing the current version of
this file located in the main directory. However, we want to introduce
clear rules for agents which they must follow every time they create
new commits or perform any work accomplishing tasks that would give
clear quality guidelines and a clear procedure that agents must follow
before they make a commit or before they consider a task as completed,
among other things, he wants that the agent every time in the case of
Java code makes JUNIT tests, conducts tests of what he does, writes
tests thinking, test driven development first about tests how he can
test given functionality before he starts implementing it, for easy
feedback or for the agent so that he could easily check whether what
he did actually corresponds to the specification documentation and the
tests should exactly reflect this documentation and specifications as
well as functionality. And only after testing both with tests and
linking that there are no problems in the code, the agent can consider
that the given task is completed.

Additionally, create a new Frontend folder in the main main root
folder. In this new Frontend folder, also create an Agents MD file,
which will specifically relate to our frontend. This will be a RAT
application with Time Wind. The most important element at the moment,
which you should consider, is exactly a similar process approach to
commit approval and task approval by the agent, it should each time
first run tests, this time, it should be J Unit tests, sorry, it
should be unit tests, as well as integration tests on the most
critical and important functionalities. And additionally, it should
use the Prywright tool to open the page and see what the page looks
like, analyze its DOM, structure, do a code review of what it created
itself. And only after visually and functionally verifying that what
it created works and looks correct. The agent only after this can
approve and consider its work finished.

> *Please notice, how Transcription made a mistake in "Time Wind" and Prywright, but Prompt Cowboy corrected it to Tailwind and Playwright :)*

> If you use Tailwind, add it to Transcriber's (e.g.Handy) defined words: Advanced settings > Custom words & in Post Process add to our prompt: `Names/terms that may appear and must be preserved exactly (or be corrected if misspelled): Tailwind, Playwright, IntelliJ, ...`

---

## Prompt Cowboy version:

**Situation**

You are working on a greenfield software development project that uses AI agents (including Claude Code) to automate code creation and task completion. The project is being built with Spring Boot and Spring AI for the backend, and React with Tailwind CSS for the frontend, creating an AI bot application. Currently, there is an AGENTS.md file in the main directory that needs to be updated with stricter quality control procedures. The project requires agents to follow test-driven development principles and comprehensive verification processes before considering any task complete. This file will also be accessible to Claude Code through a symlink named CLAUDE.md.

**Task**

The assistant should perform four distinct tasks:

1. Analyze the existing AGENTS.md file in the main directory and create a new version that introduces mandatory quality guidelines and procedures that agents must follow before making commits or marking tasks as complete.

2. Create a symlink from AGENTS.md to CLAUDE.md in the main directory so Claude Code can access the same guidelines without maintaining duplicate files.

3. Analyze and document the available tools for Claude Code (Plugins, MCP servers, and Skills) within the AGENTS.md file, including guidance on how and when to use each tool type.

4. Create a new folder structure: `/Frontend/` in the main root directory, and within it create a new `AGENTS.md` file specifically for frontend development work.

**Objective**

Establish clear, enforceable quality standards and verification procedures for AI agents (including Claude Code) working on both backend and frontend code to ensure all work is thoroughly tested, validated, and meets specifications before being committed or marked complete. Ensure agents understand and utilize available tools appropriately to maximize development efficiency and code quality.

**Knowledge**

Backend Requirements (Spring Boot with Spring AI):
- Technology stack: Spring Boot framework with Spring AI for AI bot application
- Agents must design and implement testing setup according to current best practices for Spring Boot applications
- Agents must write JUnit tests for all Java code (use latest stable JUnit version)
- Test-driven development (TDD) approach is mandatory: agents must think about and write tests BEFORE implementing functionality
- Tests must directly reflect documentation, specifications, and required functionality
- Agents must run all tests and verify no errors exist before considering a task complete
- The testing process serves as feedback mechanism to verify implementation matches specifications
- This is a greenfield project, so testing infrastructure should align with newest standards

Frontend Requirements (React with Tailwind):
- Application stack: React with Tailwind CSS
- Agents must design and implement testing setup according to current best practices for React applications
- Agents must write unit tests for frontend components
- Integration tests are required for critical and important functionalities
- Agents must use Playwright tool to:
  - Open and visually inspect the rendered page
  - Analyze the DOM structure
  - Perform code review of their own created code
- Agents can only approve their work after visual and functional verification confirms the implementation works correctly and looks correct
- This is a greenfield project, so testing infrastructure should align with newest standards

Claude Code Tool Ecosystem:
- Claude Code has access to three types of tools: Plugins, MCP (Model Context Protocol) servers, and Skills
- Agents must be aware of available tools and use them appropriately to enhance development workflow
- The AGENTS.md file should document these tools and provide clear guidance on when and how to use them
- Tool usage should be integrated into the standard development workflow

General Requirements for Both:
- Clear commit approval process must be defined
- Clear task completion criteria must be established
- Agents must follow these procedures every time they create commits or perform work
- Quality guidelines must be explicit and actionable
- A symlink (CLAUDE.md) must point to AGENTS.md to ensure Claude Code can access the same guidelines
- Only one source file (AGENTS.md) should be maintained to avoid synchronization issues

**Instructions for AGENTS.md (Main Directory - Backend)**

The assistant should structure the updated AGENTS.md file to include:

1. A clear introduction explaining the purpose and mandatory nature of these guidelines, noting that this file is also accessible as CLAUDE.md via symlink

2. A "Claude Code Tools and Resources" section that:
   - Documents available Plugins and their use cases
   - Documents available MCP servers and their use cases
   - Documents available Skills and their use cases
   - Provides clear guidance on when to leverage each tool type
   - Explains how these tools integrate into the development workflow

3. A "Quality Standards" section defining what constitutes acceptable work for Spring Boot and Spring AI development

4. A "Testing Infrastructure Setup" section with:
   - Recommended testing frameworks and versions for Spring Boot (JUnit 5, Mockito, Spring Boot Test, etc.)
   - Configuration guidelines aligned with current best practices
   - Instructions for setting up the testing environment in a greenfield project

5. A "Test-Driven Development Process" section with step-by-step procedures:
   - Write tests first based on specifications and documentation
   - Ensure tests accurately reflect requirements
   - Implement functionality to pass tests
   - Verify all tests pass

6. A "Pre-Commit Checklist" that agents must complete before any commit

7. A "Task Completion Criteria" section defining when a task can be marked complete

8. Specific requirements for JUnit test creation and execution

9. Clear consequences or blocking conditions if procedures are not followed

10. Instructions for creating the symlink: `ln -s AGENTS.md CLAUDE.md` in the main directory

**Instructions for AGENTS.md (Frontend Folder)**

The assistant should create the Frontend folder structure and AGENTS.md file with:

1. Frontend-specific quality standards for React and Tailwind development

2. A "Testing Infrastructure Setup" section with:
   - Recommended testing frameworks for React (Jest, React Testing Library, Vitest, etc.)
   - Playwright configuration guidelines
   - Setup instructions aligned with current best practices for greenfield projects

3. A "Testing Requirements" section covering:
   - Unit test requirements for components
   - Integration test requirements for critical functionalities

4. A "Playwright Verification Process" section detailing:
   - Steps to open and inspect the page
   - DOM structure analysis requirements
   - Self-code review procedures

5. A "Visual and Functional Verification" section explaining:
   - What constitutes correct visual appearance
   - What constitutes correct functionality
   - How to verify both before approval

6. A "Pre-Commit Checklist" specific to frontend work

7. A "Task Completion Criteria" section that emphasizes visual and functional verification must be complete before approval

**Output Format**

The assistant should provide three complete deliverables:

1. The updated AGENTS.md content for the main directory (including Claude Code tools documentation and symlink creation instructions)
2. The command to create the symlink from AGENTS.md to CLAUDE.md
3. The new AGENTS.md content for the Frontend folder

Each file should be clearly labeled and formatted in proper markdown syntax with appropriate headers, lists, and emphasis. The files should be production-ready and immediately usable by AI agents (including Claude Code) working on the project. The symlink command should be clearly indicated and ready to execute.
