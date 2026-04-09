# Plan Template

Use this shape unless the repository needs a tighter variant.

```md
# [Project / Feature] - Implementation Plan

## Context

- Current state of the repo
- Final outcome to achieve
- Key source documents used for the plan
- Assumptions if any

## Agents

| Agent | Specialization | Primary write scope |
|---|---|---|
| `agent-name` | skills | paths |

## Workflow Rules

1. Test-first workflow.
2. Verify before commit.
3. One logical change per commit.
4. The main agent does not implement; it delegates every execution task to subagents.
5. Do not run parallel tasks with overlapping write scope.
6. Main agent integrates outputs between dependency levels.
7. Do not push unless explicitly requested.

## Dependency Matrix

Prefer both:

- ASCII dependency graph for quick scanning
- Optional table with `Task`, `Depends on`, `Blocks`, `Write scope`

Example:

```text
BE-1 ──────┬──> BE-2 ──┬──> BE-4 ──> BE-6 ──> BE-7 ──> BE-8 ──┐
           │           │                                        │
           └──> BE-3 ──┤                                        ├──> QA-1 ──> QA-2 ──> QA-3
                       │                                        │
                       └──> BE-5 ──────────────────┘            │
                                                                │
FE-1 ──┬──> FE-2 ──┬──> FE-4 ──┬──> FE-6 ──> FE-7 ────────────┘
       │           │            │
       └──> FE-3 ──┘            │
       │                        │
       └──> FE-5 ───────────────┘
```

## Parallelism Map

| Slot | Agent A | Agent B | Agent C |
|---|---|---|---|
| 1 | TASK-ID | TASK-ID | TASK-ID |

Detailed example:

| Step | Slot 1 (be-developer) | Slot 2 (fe-developer) |
|---|---|---|
| 1 | **BE-1** | **FE-1** |
| 2 | **BE-2** + **BE-3** (sequential) | **FE-2** + **FE-3** (sequential) |
| 3 | **BE-4** + **BE-5** (sequential) | **FE-4** + **FE-5** (sequential) |
| 4 | **BE-6** | **FE-6** |
| 5 | **BE-7** | **FE-7** |
| 6 | **BE-8** | — |
| 7 | — | **QA-1** (qa-engineer) |
| 8 | — | **QA-2** (qa-engineer) |
| 9 | — | **QA-3** (qa-engineer) |

## Phase [N]: [Name]

### [TASK-ID]: [Short title]
- Agent:
- Depends on:
- Parallel with:
- Goal:
- Write scope:
- Read scope:

**Context to provide:**
- Current implementation facts
- Constraints
- Contracts already fixed

**Spec references to include in prompt:**
- `path/to/doc`

**TDD steps:**
1. Add or update tests first.
2. Run the new tests and confirm failure for the expected reason.
3. Implement the minimum change.
4. Re-run scoped verification.

**Implementation boundaries:**
1. Files or modules allowed to change
2. Integration points that must stay stable
3. Things this task must not modify

**Validation:**
- Real commands to run

**Commit:**
- `Area: summary`

## Post-implementation Verification

- Backend checks
- Frontend checks
- E2E or smoke checks
- Manual checks if runtime behavior matters
```

## Planning heuristics

- Before planning tasks, ask at least 5 questions and confirm the usable subagent roster.
- Create a contract task before parallel feature tasks if API, DTO, schema, or component interfaces are still moving.
- Put cross-cutting refactors in their own slot before feature work.
- QA tasks should start only after the relevant runtime flow exists and can be manually exercised.
- If a task would require a subagent to ask follow-up questions, the task is too large or underspecified.
