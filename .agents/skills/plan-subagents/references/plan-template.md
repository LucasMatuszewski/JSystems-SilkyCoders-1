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
4. Do not run parallel tasks with overlapping write scope.
5. Main agent integrates outputs between dependency levels.
6. Do not push unless explicitly requested.

## Dependency Matrix

Use either:

- ASCII dependency graph for quick scanning
- Table with `Task`, `Depends on`, `Blocks`, `Write scope`

## Parallelism Map

| Slot | Agent A | Agent B | Agent C |
|---|---|---|---|
| 1 | TASK-ID | TASK-ID | TASK-ID |

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

- Create a contract task before parallel feature tasks if API, DTO, schema, or component interfaces are still moving.
- Put cross-cutting refactors in their own slot before feature work.
- QA tasks should start only after the relevant runtime flow exists and can be manually exercised.
- If a task would require a subagent to ask follow-up questions, the task is too large or underspecified.
