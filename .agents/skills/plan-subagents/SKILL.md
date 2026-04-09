---
name: plan-subagents
description: Create repository-specific implementation plans for Codex CLI that split work across multiple subagents with clear phases, dependency matrices, parallel work slots, write-scope boundaries, and ready-to-send delegation prompts. Use when the user wants a detailed execution plan, orchestration plan, dependency map, or conflict-safe multi-agent workflow before implementation.
---

Create a plan document that lets a main Codex CLI agent coordinate specialists without doing the implementation itself.

Keep the plan actionable. Every planned task must give a future main agent enough structure to delegate work without re-planning from scratch.

## Workflow

### 1. Gather only the needed context

Read only the files required for the requested scope:

- Root `AGENTS.md` and the area-specific `AGENTS.md` files the plan touches
- The specific PRD, ADR, design, API, or policy docs that define the work
- Existing project structure and current implementation state
- Any example prompt/result files the user explicitly points to

If the request is ambiguous in a way that would materially change task boundaries, ask concise questions. Otherwise proceed with reasonable assumptions and state them in the plan.

### 2. Define planning boundaries before writing tasks

Decide these first:

- What the final deliverable is
- Which specialist roles are needed
- Which directories each role should own
- Which verification commands each role must run
- Which tasks are safe in parallel and which are not

For Codex CLI, optimize for isolated write scopes. Parallel tasks must not edit the same files or tightly-coupled modules in the same slot.

### 3. Build the task graph from write ownership

Split work into small tasks that each have:

- A stable ID such as `BE-1`, `FE-2`, `QA-1`
- One primary owner agent
- A narrow write scope
- Explicit dependencies
- A concrete verification step
- A focused commit message

Prefer sequencing over parallelism when two tasks touch:

- The same file
- The same package/module entry point
- Shared test fixtures
- Shared generated artifacts
- The same database schema or API contract in incompatible ways

### 4. Encode parallelism deliberately

Produce both:

- A dependency matrix or graph showing strict ordering
- A parallelism map showing which tasks can run in the same execution slot

Only place tasks in the same slot when all of these are true:

- Write scopes are disjoint
- Neither task depends on outputs of the other
- The future integration surface is already fixed or documented
- Verification can run independently

If parallel work is possible only after a contract is fixed, create a contract-defining task first and make later tasks depend on it.

### 5. Write delegation-ready task briefs

For each task, include:

- Agent role
- Depends on
- Parallel with
- Goal
- Write scope
- Read scope
- Exact context to provide
- Spec references to include in the prompt
- TDD steps
- Implementation boundaries
- Verification commands
- Commit message

Do not dump full repository context into every task. Give each subagent only the minimum files, facts, and constraints needed for that task.

### 6. Add main-agent orchestration rules

Every plan must tell the future main agent how to run the work safely:

- Never delegate implementation without first checking dependency readiness
- Never assign overlapping write scopes in parallel
- Reuse contract language verbatim across dependent tasks
- Wait for a task only when it blocks the critical path
- Integrate results between slots before launching dependent work
- Preserve unrelated user changes; do not revert them
- Require test-first workflow, scoped verification, runtime smoke checks, and focused commits

### 7. Format the result as an execution artifact

Use the template in [plan-template.md](D:\DEV\COURSES\JSystems-SilkyCoders-1\.agents\skills\plan-subagents\references\plan-template.md).

Use the prompt structure in [delegation-prompt-template.md](D:\DEV\COURSES\JSystems-SilkyCoders-1\.agents\skills\plan-subagents\references\delegation-prompt-template.md) when the user wants ready-made prompts for subagents.

## Rules

- Optimize for Codex CLI orchestration, not generic PM language.
- Make tasks small enough that a subagent can finish them in one bounded pass.
- Prefer write-scope isolation over theoretical maximum parallelism.
- State assumptions explicitly when the repo or docs leave gaps.
- Use repository paths and real commands, not placeholders, whenever the repo already defines them.
- Include verification before every commit.
- If the user asked only for a plan, do not implement code.

## Output requirements

The plan should usually contain:

- Context
- Agent roster
- Workflow rules
- Dependency matrix
- Parallelism map
- Phased task list
- Per-task delegation briefs
- Final verification checklist

Add or remove sections only if that makes the plan clearer for the current repo.
