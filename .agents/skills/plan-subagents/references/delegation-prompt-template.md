# Delegation Prompt Template

Use this for each task when the user wants prompt-ready delegation text for Codex CLI subagents.

```md
Task: [TASK-ID] - [title]

You are the [agent-role] working in `[path]`.

Goal:
- [one-sentence outcome]

Read first:
- `path/to/AGENTS.md`
- `path/to/spec.md`
- `path/to/current/file`

Current facts:
- [repo state the agent must know]
- [contracts already fixed]
- [known constraints]

Write scope:
- `allowed/path/one`
- `allowed/path/two`

Do not modify:
- `forbidden/path`
- [integration surface owned by another task]

TDD workflow:
1. Add or update tests first.
2. Run the new tests and confirm they fail for the expected reason.
3. Implement the minimum code needed.
4. Run the required verification commands.
5. Start the affected app/runtime if the task changes runtime behavior.
6. Commit with: `[commit message]`

Verification commands:
- `[command]`
- `[command]`

Task-specific requirements:
- [exact behavior to implement]
- [error cases]
- [acceptance points]

Coordination rules:
- You are not alone in the codebase.
- Do not revert changes you did not make.
- Stay inside the write scope.
- If you discover a contract mismatch with another task, stop and report it instead of improvising a conflicting change.
```

## Delegation rules for the main agent

- Send only the prompt section relevant to that task.
- Include only the files the subagent actually needs.
- Keep contract wording identical across dependent prompts.
- If two tasks meet at a shared interface, delegate the interface-defining task first and freeze the contract in later prompts.
