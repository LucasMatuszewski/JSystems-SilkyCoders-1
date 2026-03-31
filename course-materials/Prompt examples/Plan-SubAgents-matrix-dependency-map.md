# Example prompt (dictated) to get detailed plan based on PRD and ADR

I want you to create very detailed plan of work to create fully working proof of concept of our application.
Implement both back end, front end, tests and follow all guidelines from your CLAUDE.md files.
I want you to follow instructions and detailed documentation from:
- Product Requirements Document for functional description,
- Architecture Decision Records for architecture and implementation technical decisions,
- Design guidelines to follow the design of our Silky brand

You should provide exact context to specialized agents and orchestrate work of multiple subagents.
You should not implement anything yourself. You are only orchestrator and manager.
You should only delegate to specialized agents, and provides for each agent everything what is required for this agent to, do its work without asking questions.
So every agent for every task should have very detailed instructions for this specific task.

You should not ask agents to implement everything at once.
So your next task is to create detailed plan, make phases, and you should plan work of multiple agents in many steps in small steps, with commiss after every step and with full test driven development process for every step.
You should, make matrix of dependencies between tasks and between agents, to coordinate their work in the way that will prevent agents interrupting other agents and to let them work together (partially in pararell when possible).
Plan the tasks and every step in this process with information which task need to be finished first before we go to the next stage.

Agent you should always use to delegate work to based on specialization:
@"fe-developer (agent)"
@"be-developer (agent)"
@"qa-engineer (agent)"

Files you should use to create detailed plan of implemention of our PoC application:
- @docs/PRD-Product-Requirements-Document.md
- @docs/ADR/001-backend.md
- @docs/ADR/002-frontend.md
- @docs/ADR/000-main-architecture.md
- @docs/design-guidelines.md

You should provide exact context needed to every task for every subagent.
But provide only required information for this specific task.
So don't blow the context window with everything about the application.
You should only provide to the specialized subagents information needed to complete the task at hand.

If enything is unclear, you should ask minimum 5 questions to make sure that you understand everything correct and that you don't have any doubts about what you should do and how this process should work.

---

Result of this prompt is here:
[day-3-PLAN-dependency-matrix-pararelizm-map.md](../03-2026/day-3-PLAN-dependency-matrix-pararelizm-map.md)
