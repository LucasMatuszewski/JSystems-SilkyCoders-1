0a. Study `specs/*` and `docs/PRD-Sinsay-PoC.md` to learn the application specifications.
0b. Study @IMPLEMENTATION_PLAN.md.
0c. For reference, the application source code is in `src/`.

1. Your task is to implement functionality per the specifications. Follow @IMPLEMENTATION_PLAN.md and choose the most important item to address.
2. Before making changes, search the codebase (don't assume not implemented).
3. Implement functionality or resolve problems. 
4. After implementing, run the tests for that unit of code:
   - Backend: `./mvnw test`
   - Specific test: `./mvnw -Dtest=ClassName test`
5. If functionality is missing then it's your job to add it as per the application specifications. Ultrathink.
6. When you discover issues, immediately update @IMPLEMENTATION_PLAN.md with your findings.
7. When the tests pass, update @IMPLEMENTATION_PLAN.md, then `git add -A` then `git commit` with a message describing the changes. After the commit, `git push`.

99999. Important: When authoring documentation, capture the why — tests and implementation importance.
999999. Important: Single sources of truth. If tests unrelated to your work fail, resolve them as part of the increment.
9999999. As soon as there are no build or test errors, create a git tag. Start at 0.0.0 and increment patch by 1 (e.g. 0.0.1) if 0.0.0 does not exist. Also ensure code adheres to `docs/ADR-Sinsay-PoC.md`.
99999999. You may add extra logging if required to debug issues.
999999999. Keep @IMPLEMENTATION_PLAN.md current with learnings — future work depends on this to avoid duplicating efforts. Update especially after finishing your turn.
9999999999. When you learn something new about how to run the application, update @AGENTS.md but keep it brief.
99999999999. For any bugs you notice, resolve them or document them in @IMPLEMENTATION_PLAN.md.
999999999999. Implement functionality completely. Placeholders and stubs waste efforts.
9999999999999. When @IMPLEMENTATION_PLAN.md becomes large, periodically clean out the items that are completed from the file.
99999999999999. If you find inconsistencies in the specs/* then use deep reasoning ('ultrathink') to update the specs.
999999999999999. IMPORTANT: Keep @AGENTS.md operational only — status updates and progress notes belong in `IMPLEMENTATION_PLAN.md`. A bloated AGENTS.md pollutes every future loop's context.