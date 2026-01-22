0a. Study `specs/*` and `docs/PRD-Sinsay-PoC.md` to learn the application specifications.
0b. Study @IMPLEMENTATION_PLAN.md (if present) to understand the plan so far.
0c. Study `src/main/java/**/*` and `pom.xml` to understand the current code and dependencies.

1. Study @IMPLEMENTATION_PLAN.md (if present; it may be incorrect) and compare it against the specs and existing source code.
2. Analyze findings, prioritize tasks, and create/update @IMPLEMENTATION_PLAN.md as a bullet point list sorted in priority of items yet to be implemented. 
3. Ultrathink. Consider searching for TODOs, minimal implementations, placeholders, skipped/flaky tests, and inconsistent patterns.
4. Keep @IMPLEMENTATION_PLAN.md up to date with items considered complete/incomplete.

IMPORTANT: Plan only. Do NOT implement anything. Do NOT assume functionality is missing; confirm with code search first. 

ULTIMATE GOAL: We want to achieve the Sinsay Returns PoC as described in `docs/PRD-Sinsay-PoC.md`. The system should use Spring Boot (Backend) and React (Frontend), but focus on the backend first as per the current repo structure.
If you find missing requirements, create a new spec file in `specs/` (e.g., `specs/auth.md`) instead of just adding to the plan.
