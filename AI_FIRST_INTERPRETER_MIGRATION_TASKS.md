# uniAI — AI-First Interpreter Migration Tasks

## Goal

Move the graduate knowledge pipeline toward an AI-first interpretation contract while preserving deterministic safety, catalog resolution, follow-up behavior, fallback, and retrieval correctness.

Target pipeline:

```text
User message + bounded history
→ AI structured interpreter
→ CanonicalGraduateQueryDraft
→ Draft validation
→ Deterministic entity resolution
→ GraduateKnowledgeQuery factory
→ Executable-query safety validation
→ Follow-up resolution
→ Retrieval
```

The AI decides **what the user wants**. The backend decides **whether it is valid, which entities it refers to, and how to execute it safely**.

## Migration rules

- Do not expose database IDs, SQL, repositories, adapters, Java classes, or method names to the AI.
- Keep `GraduateKnowledgeQuery` internal.
- Keep entity resolution deterministic and server-owned.
- Keep a final safety boundary before retrieval.
- Preserve the deterministic path until the new contract is proven stable.
- Do not redesign SQL retrieval in these tasks.
- Complete and validate each task before starting the next.

---

# Task 1 — Align the Existing AI Contract

## Task code

```text
AI_INTERPRETER_REFACTOR_014_CONTRACT_ALIGNMENT
```

## Objective

Fix the mismatch between the prompt, compact interpretation DTO, adapter, validator, and tests.

The current compact prompt requests typed routing fields without requiring legacy `intent`, while `CompactGraduateQueryInterpretation.toLegacyInterpretation()` sets `intent` to `null` and the validator rejects null intent.

## Required work

1. Trace the exact provider schema.
2. Trace the deserialized DTO.
3. Confirm which fields validation requires.
4. Select one temporary contract.
5. Align prompt, DTO, adapter, validator, and tests.
6. Prevent deterministic fallback from masking invalid provider output.
7. Add direct tests for valid compact JSON, missing fields, unsupported enums, malformed JSON, and typed routing without legacy intent if supported.

## Constraints

- Do not introduce the final architecture yet.
- Do not remove deterministic interpretation.
- Do not modify retrieval SQL.
- Do not add university aliases.
- Do not perform the large validator extraction.

## Acceptance criteria

- Valid provider output succeeds without deterministic fallback.
- Invalid provider output gets the correct failure category.
- Prompt and DTO fields match.
- Provider-path tests run independently.
- Existing deterministic behavior remains unchanged.

---

# Task 2 — Introduce CanonicalGraduateQueryDraft

## Task code

```text
AI_INTERPRETER_REFACTOR_015_CANONICAL_QUERY_DRAFT
```

## Objective

Introduce a provider-facing draft that expresses the requested domain operation without internal database IDs.

Example:

```json
{
  "schemaVersion": 2,
  "resource": "PROGRAM",
  "operation": "AGGREGATE",
  "aggregation": {
    "function": "AVG",
    "field": "TUITION"
  },
  "filters": {
    "universities": ["University of Balamand"],
    "degreeTypes": ["MASTER"],
    "city": null,
    "faculty": null,
    "department": null,
    "programName": null,
    "languages": [],
    "admissionRequirementTypes": []
  },
  "comparison": null,
  "sort": null,
  "limit": null,
  "clarificationRequired": false,
  "unsupportedConstraints": []
}
```

## Required work

1. Add an immutable provider-facing model such as `CanonicalGraduateQueryDraft`.
2. Include only stable domain concepts: resource, operation, filters, aggregation, sorting, comparison, detail level, limit, clarification, and unsupported constraints.
3. Exclude database IDs, resolved entities, SQL, repositories, handlers, and Java methods.
4. Update the prompt to return only this schema.
5. Supply supported enum values in the prompt.
6. Optionally supply a compact authoritative university name/acronym list.
7. Add draft validation for schema version, required fields, enum allow-lists, sizes, lengths, numeric bounds, and malformed combinations.
8. Keep the old path behind a compatibility boundary during migration.

## Constraints

- Do not deserialize into `GraduateKnowledgeQuery`.
- Do not resolve IDs inside the AI adapter.
- Do not remove deterministic interpretation.
- Do not redesign follow-up handling or SQL adapters.

## Acceptance criteria

- The adapter returns a validated canonical draft.
- The provider contract contains no internal IDs.
- Tests cover tuition aggregation, program lists, comparisons, locations, and academic structure.
- Invalid draft values fail before entity resolution.
- Existing behavior remains available through compatibility handling.

---

# Task 3 — Extract Entity Resolution and Query Construction

## Task code

```text
AI_INTERPRETER_REFACTOR_016_RESOLVER_QUERY_FACTORY
```

## Objective

Move catalog resolution and final query construction out of `GraduateQueryInterpretationValidator`.

Target:

```text
CanonicalGraduateQueryDraft
→ Entity resolver
→ Query factory
→ Final safety validator
```

## Required work

### Entity resolver

Introduce a dedicated component such as `GraduateKnowledgeEntityResolver` that:

- maps canonical names and acronyms to `ResolvedUniversity`,
- applies server-owned aliases,
- detects unknown and ambiguous entities,
- preserves explicit scope,
- never silently broadens unresolved scoped queries.

It may reuse `GraduateKnowledgeResolutionSupport`.

### Alias support

Introduce reusable alias handling rather than Balamand-specific Java branches.

Initial validated forms should include:

```text
University of Balamand
UOB
Balamand
Balamand University
Balamand uni
```

Typo handling such as `Balamnd` must require:

- a unique candidate,
- a defined confidence threshold,
- no competing candidate,
- otherwise clarification.

### Query factory

Introduce a deterministic factory such as `GraduateKnowledgeQueryFactory` to construct:

- `GraduateKnowledgeFilters`,
- aggregation,
- sort,
- comparison metadata,
- detail level,
- follow-up metadata,
- `GraduateKnowledgeQuery`.

### Final safety validator

Keep only domain invariants:

- resource/operation compatibility,
- aggregation validity,
- threshold validity,
- scope compatibility,
- comparison compatibility,
- required resolved scope,
- valid limit,
- unsupported undergraduate constraints,
- executable-query state.

## Constraints

- No AI calls inside the resolver.
- No SQL inside the validator or factory.
- No Balamand-only condition.
- Preserve LAU, AUB, LU, and UOB behavior.
- Keep deterministic fallback.
- Do not redesign retrieval adapters.

## Acceptance criteria

- Entity resolution is outside the validator.
- Query construction is outside the validator.
- Canonical and alias forms resolve deterministically.
- Unknown and ambiguous universities have explicit outcomes.
- UOB aliases resolve to university ID 22.
- `Balamnd` resolves conservatively or asks for clarification.
- Retrieval tests continue to pass.

---

# Task 4 — Simplify Orchestration and Move Toward One Interpreter

## Task code

```text
AI_INTERPRETER_REFACTOR_017_SINGLE_INTERPRETER_ORCHESTRATION
```

## Objective

Make the AI structured interpreter the primary interpretation path while retaining deterministic interpretation as an optimization or fallback until reliability is proven.

Do not immediately delete the deterministic interpreter.

## Required work

1. Define one authoritative interpretation result for orchestration.
2. Remove duplicated decisions between deterministic and AI interpretation.
3. Decide whether deterministic interpretation remains as:
   - pre-provider optimization,
   - provider-failure fallback,
   - shadow comparison,
   - temporary migration path.
4. Preserve provider independence.
5. Preserve bounded history and token budgeting.
6. Keep follow-up state server-owned.
7. Support follow-ups such as:
   - `for AUB?`
   - `LU?`
   - `Balamand instead`
   - `what about UOB?`
8. Introduce explicit retrieval outcomes:
   - `SUCCESS`
   - `RESOLVED_NO_DATA`
   - `UNRESOLVED_ENTITY`
   - `AMBIGUOUS_ENTITY`
   - `UNSUPPORTED`
   - `INVALID_INTERPRETATION`
9. Ensure unresolved entities are never presented as confirmed no-data results.
10. Add metrics for interpretation success, fallback use, invalid contracts, unresolved entities, clarification, provider failure, and AI/deterministic disagreement.

## Constraints

- Do not remove fallback until tests and metrics justify it.
- Do not let the AI choose Java methods or SQL.
- Do not expose internal IDs.
- Do not merge unresolved and no-data outcomes.
- Preserve security and context-budget controls.

## Acceptance criteria

- Orchestration consumes one canonical interpretation model.
- Duplicate interpretation logic is reduced.
- Provider failures still have controlled fallback.
- Follow-up replacement queries work.
- Unresolved entities produce clarification.
- Resolved zero-row results produce valid no-data responses.
- Metrics support a later decision on removing deterministic interpretation.

---

# Recommended order

```text
Task 1
→ Task 2
→ Task 3
→ Task 4
```

Do not run Tasks 2–4 in parallel because each changes the contract required by the next.

## Risk estimate

| Approach | Estimated risk |
|---|---:|
| One large implementation task | 65–75% |
| Four sequential tasks with tests | 20–30% |
| Remove deterministic interpreter immediately | 55–70% |
| Keep deterministic fallback during migration | 15–25% |

## Final boundary

The AI may choose:

```text
resource
operation
filters
aggregation
sorting
comparison
detail level
clarification need
```

The AI must not choose:

```text
database ID
repository
adapter
SQL query
Java method
transaction boundary
authorization rule
retrieval implementation
```

The backend remains authoritative for:

```text
contract validation
entity identity
aliases
ambiguity
query construction
business invariants
security
execution
retrieval-result semantics
```

## Completion definition

The migration is complete only when:

- prompt and provider schema are aligned,
- the provider returns a canonical draft,
- entity resolution is outside the validator,
- query construction is outside the validator,
- final validation remains deterministic,
- follow-up scope is preserved,
- unresolved and no-data states are distinct,
- regression tests pass for LAU, AUB, LU, UOB, Balamand aliases, and malformed provider output,
- removal of deterministic fallback is treated as a separate metrics-based decision.
