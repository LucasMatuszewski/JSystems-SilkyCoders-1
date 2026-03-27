---
name: create-adr
description: Create Architecture Decision Records (ADR) based on an existing PRD or user-provided feature description. Covers technical decisions, system design, data models, API contracts, diagrams, and testing strategy. Use when the user wants to document architecture, plan technical implementation, or prepare a codebase for AI-agent-driven development.
---

This skill creates one or more ADR documents that — together with the PRD — give an AI development agent everything it needs to implement the feature or application without ambiguity. All technical decisions, reasoning, diagrams, data structures, and test plans are defined here so the implementing agent does not have to make architectural guesses.

Technical implementation details and testing strategy intentionally excluded from PRD belong here.

---

## Process

### Step 1 — Read the PRD (if it exists)
Check for `docs/PRD.md`. If found, read it fully before proceeding. If not found, ask the user to provide a description of the feature or application: what it does, who uses it, what the main flows are.

### Step 2 — Ask clarifying questions (REQUIRED — minimum 5)
Do not write any ADR until you have answers. Ask about:

- Which frameworks, languages, and runtime environments are being used or preferred?
- Are there specific libraries or tools already decided? If yes, list their Context7 handles if known (see Step 3).
- What are the deployment constraints — local dev only, Docker, cloud, serverless?
- What is the persistence strategy — which database/storage, why?
- Are there external APIs or services being integrated? What are their constraints?
- What are the performance or scale expectations for this PoC/MVP?
- Are there security requirements (auth, secrets management, data privacy)?
- Any testing requirements — unit only, integration, e2e? TDD approach expected?
- Are there any existing architectural patterns in the codebase that must be followed?

Walk through each decision branch. Resolve dependencies between decisions one by one before writing.

### Step 3 — Resolve Context7 library references
For every library, framework, or SDK that will be used:

1. Ask the user if they have the Context7 handle (e.g., `/vercel/ai`, `/spring-projects/spring-boot`).
2. If not provided, use `mcp__context7__resolve-library-id` to find the correct handle.
3. Use `mcp__context7__query-docs` to fetch relevant documentation sections needed to make informed decisions.
4. Store all resolved handles in the ADR under the **Context7 References** section so future agents can fetch docs directly without searching again.

Do not make technology decisions based on training knowledge alone — always verify with current docs via Context7 when available.

### Step 4 — Determine ADR structure
Choose based on complexity:

**Simple** (use a single file):
- Single technical layer, or straightforward feature addition
- Fewer than 3 distinct integrated components
- Save as: `docs/ADR/000-[short-description].md`

**Complex** (use a folder with multiple files):
- Multiple distinct technical layers (e.g., frontend + backend + database + AI/LLM)
- Multiple integration points or external services
- Large scope requiring per-area ownership
- Save as:
  - `docs/ADR/000-main-architecture.md` — overall system, component map, main decisions
  - `docs/ADR/001-[area].md`, `002-[area].md`, etc. — one file per technical area

File naming: use zero-padded numbers + short kebab-case description. Do NOT include "ADR" in the filename — the folder provides that context.
Examples: `000-main-architecture.md`, `001-backend-api.md`, `002-frontend.md`, `003-database.md`

### Step 5 — Write the ADR(s)
Use the templates below. Save to `docs/ADR/`.

---

## Rules

- **Language**: Always write in English, regardless of the language the user communicates in.
- **No code snippets**: Do not include implementation code. The implementing agent will use Context7 to get exact API usage. Describe what, not how.
- **No vague statements**: Every constraint, decision, or requirement must be concrete and verifiable — same standard as Acceptance Criteria in the PRD.
- **Diagrams are mandatory**: Include architecture diagrams, data flow diagrams, and sequence diagrams for all flows where applicable. Only skip a diagram type if it genuinely cannot be expressed that way. More detail is better.
- **Testing is mandatory**: Every ADR must include a testing strategy. TDD is the primary self-validation tool for implementing agents.
- **Context7 handles must be stored**: Any library referenced in the ADR must have its Context7 handle recorded so future agents can fetch docs without searching.
- **Purpose**: This document, together with the PRD, must give a developer agent a complete, unambiguous picture of the system. If something is unclear, resolve it with the user before writing.

---

## ADR-000 Template — Main Architecture (or Single ADR)

```markdown
# ADR: [Product / Feature Name] — Main Architecture

**Date:** [today]
**Status:** Accepted
**PRD:** [link to docs/PRD.md if exists]

---

## 1. Overview

What is being built. What problem it solves. How this ADR relates to the PRD.

---

## 2. Context7 Library References

All libraries, frameworks, and SDKs used in this project. Implementing agents must use these handles to fetch docs — do not search for them again.

| Library | Context7 Handle | Used for |
|---|---|---|
| [Library name] | `/org/repo` | [purpose] |

---

## 3. System Architecture

### Architecture pattern
[e.g., monolith, monorepo, microservices, SPA + REST API, etc.]

### Repository structure
Describe the repo layout: folders, modules, build artifacts, how frontend and backend relate.

### Technology stack
List each layer with the chosen technology and one-sentence justification.

| Layer | Technology | Reason |
|---|---|---|
| Backend | | |
| Frontend | | |
| Database | | |
| AI/LLM | | |
| ... | | |

---

## 4. Module Structure & Dependencies

List all modules/packages/services. For each:
- What it is responsible for
- What it depends on
- What depends on it

Describe the dependency direction explicitly. No circular dependencies.

---

## 5. Data Models

Describe each entity/model conceptually:
- Name and purpose
- Key fields and their types (conceptual, not schema code)
- Relationships to other models
- Persistence (where it is stored, how long)

---

## 6. API / Interface Contracts

For each endpoint or interface boundary:
- Name / path
- Input (fields, types, constraints)
- Output (fields, types)
- Error cases
- Notes (auth required, rate limit, streaming, etc.)

No code. Conceptual contracts only.

---

## 7. Environment Variables

All required environment variables the application needs to run.

| Variable | Purpose | Required | Example value |
|---|---|---|---|
| | | Yes/No | |

---

## 8. Technical Decisions

One record per significant decision. Use the format below.

### [short title]
**Status:** Accepted | Proposed | Superseded
**Date:** [today]
**Context:** Why this decision was needed. What problem or constraint triggered it. (2-3 sentences)
**Decision:** What was decided. Brief reasoning — why this option over others.
**Rejected alternatives:**
- [Alternative A]: [1-2 sentences why rejected]
- [Alternative B]: [1-2 sentences why rejected]
**Consequences:**
- (+) [positive consequence]
- (-) [negative consequence or trade-off]
**Review trigger:** [Specific condition that would require revisiting this decision. E.g., "If concurrent sessions exceed 100/day", "If we add authentication", "If we move to production"]

---

## 9. Diagrams

### 9.1 Architecture / Component Diagram
[Mermaid diagram showing components and their relationships]

```mermaid
...
```

### 9.2 Data Flow Diagram
[Mermaid diagram showing how data moves through the system]

```mermaid
...
```

### 9.3 Sequence Diagrams
One diagram per main flow. Cover: happy path, error path, and any async/streaming flows.

#### [Flow name — e.g., Form submission and AI analysis]
```mermaid
sequenceDiagram
...
```

#### [Flow name — e.g., Session resume]
```mermaid
sequenceDiagram
...
```

---

## 10. Testing Strategy

### Philosophy
Describe the testing approach. TDD is recommended — tests should be written before or alongside implementation and serve as the agent's primary self-validation mechanism.

### Test layers

| Layer | Type | Scope | Tools |
|---|---|---|---|
| Unit | | | |
| Integration | | | |
| E2E | | | |

### Key test scenarios

List the most important scenarios to cover. For each:
- Scenario name
- What is being tested
- Input conditions
- Expected output / behavior
- Edge cases to cover

Include both happy path and failure/edge cases.

### Technical acceptance criteria

Measurable, verifiable criteria that confirm the implementation is correct from a technical standpoint (complement the business ACs in the PRD).

- TAC-01: [concrete, testable statement]
- TAC-02: ...
```

---

## Granular ADR Template (ADR-001+)

Use this for each focused technical area in a complex project.

```markdown
# ADR-[NNN]: [Technical Area Name]

**Date:** [today]
**Status:** Accepted
**Relates to:** `docs/ADR/000-main-architecture.md`

---

## 1. Scope

What technical area this ADR covers. What it does NOT cover (leave to other ADRs).

---

## 2. Context7 References

Libraries specific to this area.

| Library | Context7 Handle | Used for |
|---|---|---|

---

## 3. Component Design

Describe the internal design of this area:
- Layers / classes / services and their responsibilities
- Key interfaces and contracts between them
- State management (if any)

---

## 4. Data Structures

Models, DTOs, request/response shapes specific to this area. Conceptual descriptions, no code.

---

## 5. Interface Contracts

Endpoints, events, or method signatures this area exposes or consumes. Input/output/error for each.

---

## 6. Technical Decisions

[Same ADR record format as in the main architecture template]

---

## 7. Diagrams

### Component / Class Diagram
```mermaid
...
```

### Sequence Diagrams
```mermaid
sequenceDiagram
...
```

---

## 8. Testing Strategy

### Test scenarios for this area

| Scenario | Type | Input | Expected output | Edge cases |
|---|---|---|---|---|

### Technical acceptance criteria

- TAC-[NNN]-01: [concrete, testable statement]
```
