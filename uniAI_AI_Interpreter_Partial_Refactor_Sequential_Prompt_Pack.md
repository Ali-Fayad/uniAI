# uniAI AI Interpreter Partial Refactor — Sequential Prompt Pack

This file contains the complete implementation sequence for partially refactoring the graduate-knowledge interpretation and retrieval pipeline.

Use the prompts in order.

## Execution Rule

For every task:

1. Give the selected prompt to the coding model.
2. The model must first investigate and return the required review output.
3. Do not let it implement until the review decision is approved.
4. When the review result is `APPROVE` or `APPROVE WITH NOTES`, continue with the implementation section of the same task.
5. After implementation, review the changed files and validation results.
6. Only after the implementation review is approved should you continue to the next task.
7. Do not combine multiple tasks into one large implementation unless the model proves that they are inseparable.

Do not skip a task unless the current repository already contains the required architecture and the model proves it from the code.

---

# TASK 1 — PostgreSQL Dynamic Filter Safety

```text
TASK CODE: AI_INTERPRETER_REFACTOR_001_SQL_FILTER_SAFETY

Recommended Model:
GPT-5.6 Sol

Objective

Fix the production-blocking PostgreSQL parameter typing failures across the complete graduate-retrieval SQL layer.

The current implementation includes optional predicates such as:

- `(:city IS NULL OR ...)`
- `(:faculty IS NULL OR ...)`
- `(:department IS NULL OR ...)`
- `(:subjectPattern IS NULL OR ...)`

At runtime, PostgreSQL fails with errors such as:

- `could not determine data type of parameter $3`
- `could not determine data type of parameter $4`
- `could not determine data type of parameter $5`

The failure is not limited to one query. The investigation found that the nullable-filter strategy affects:

- program lookup
- tuition aggregation
- admission retrieval
- academic-structure retrieval
- comparison paths
- other SQL branches that bind absent optional values

This task must correct the SQL composition strategy safely and consistently.

Important Scope Boundary

This task should:

- audit every SQL query built by the graduate retrieval adapters
- identify every optional filter that can bind an untyped null
- replace unsafe always-present nullable predicates with safe dynamic SQL composition
- omit SQL clauses when filters are absent
- preserve parameterized SQL
- preserve existing supported behavior
- add PostgreSQL integration coverage where possible

This task must not:

- redesign the interpreter
- change the generalized query model
- change prompts
- change memory behavior
- add new retrieval capabilities
- alter database schema or seed data

Investigation First

Inspect at minimum:

- `SqlGraduateKnowledgeRetrievalAdapter`
- `SqlGraduateLocationRetrievalAdapter`
- all SQL constants and query builders
- all `MapSqlParameterSource` usage
- all optional filters
- all retrieval methods
- all retrieval tests
- PostgreSQL integration-test configuration

Search for patterns such as:

- `IS NULL OR`
- nullable named parameters
- duplicated guard/value parameters
- regex parameters
- optional city/faculty/department filters
- optional language/admission/tuition filters
- conditional `IN` clauses
- query fragments appended dynamically
- casts added only to selected paths

Required Investigation Output

Use exactly these sections:

1. Findings
2. Affected SQL Paths
3. Root Cause
4. Proposed SQL Composition Strategy
5. Reusable Components
6. Risks
7. Files Likely To Change
8. Validation Plan
9. Review Decision

The review decision must be one of:

- APPROVE
- APPROVE WITH NOTES
- REJECT AND REWORK

Do not implement until approved.

Implementation Constraints

- Keep SQL generation inside infrastructure.
- Keep application query objects persistence-agnostic.
- Do not concatenate user input directly into SQL.
- Use named parameters.
- Omit absent predicates rather than binding untyped nulls where practical.
- Use explicit SQL types only where dynamic omission is not appropriate.
- Do not create a generic SQL framework.
- Avoid duplicated query-fragment logic.
- Preserve bounded result limits.
- Preserve existing ordering and semantics.
- Do not introduce a new dependency.

Acceptance Criteria

- Program lookup no longer fails with PostgreSQL parameter type errors.
- Tuition aggregation no longer fails with PostgreSQL parameter type errors.
- Admission retrieval no longer fails for absent optional filters.
- Academic-structure retrieval no longer fails for absent optional filters.
- Comparison queries no longer fail for absent optional filters.
- Present filters remain parameterized.
- Absent filters do not appear as unsafe null-guard predicates.
- Existing retrieval behavior remains backward compatible.
- Tests cover absent, single, and multiple optional filters.
- PostgreSQL-backed tests execute generated SQL when Docker/Testcontainers is available.

Validation Commands

./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest test
./mvnw -q -Dtest=SqlGraduateLocationRetrievalAdapterTest test
./mvnw -q -Dtest=*Retrieval*IntegrationTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test
./mvnw -q -DskipTests compile

Required Implementation Output

1. Summary
2. Root Causes Fixed
3. SQL Composition Changes
4. Files Changed
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

fix(ai): make graduate retrieval filters PostgreSQL-safe
```

---

# TASK 2 — Entity Resolution Hardening

```text
TASK CODE: AI_INTERPRETER_REFACTOR_002_ENTITY_RESOLUTION

Recommended Model:
GPT-5.6 Sol

Prerequisite

AI_INTERPRETER_REFACTOR_001_SQL_FILTER_SAFETY must be implemented and approved.

Objective

Harden university, campus, faculty, department, program, language, and location resolution so common words and broad name fragments do not resolve unrelated entities.

Observed problems include:

- `Beirut` contributing to unrelated university matches
- `science` matching institutions because it appears inside a long official name
- explicit AUB questions resolving multiple universities
- broad token matching producing false positives
- current-message entities being merged with stale history entities

This task must make entity resolution precise, explainable, and safe.

Important Scope Boundary

This task should:

- audit all entity-resolution utilities
- separate exact aliases from weak tokens
- define safe matching precedence
- prevent generic words from becoming university references
- require stronger evidence for broad or partial matching
- preserve valid acronym, alias, and canonical-name resolution
- expose ambiguity rather than silently choosing unrelated entities

This task must not:

- redesign the full interpreter pipeline
- change SQL retrieval semantics
- redesign follow-up memory yet
- introduce embeddings or semantic-search libraries
- add new database columns

Investigation First

Inspect:

- `GraduateKnowledgeResolutionSupport`
- university alias and catalog resolution
- campus resolution
- faculty and department matching
- subject/program matching
- language matching
- deterministic interpreter entity extraction
- AI interpretation validator
- follow-up resolver merge behavior
- all related tests

Required Investigation Output

Use exactly these sections:

1. Findings
2. Current Matching Rules
3. False-Positive Sources
4. Proposed Matching Precedence
5. Ambiguity Policy
6. Reusable Components
7. Risks
8. Files Likely To Change
9. Validation Plan
10. Review Decision

Do not implement until approved.

Implementation Constraints

- Prefer exact acronym, exact alias, and normalized full-name matching.
- Treat generic tokens as insufficient evidence.
- Do not silently resolve multiple plausible entities.
- Keep normalization deterministic.
- Avoid fuzzy matching thresholds that cannot be explained.
- Preserve multilingual or alternate-name support already present.
- Do not store persistence concerns in the query model.
- Do not add a new dependency.

Acceptance Criteria

- `AUB` resolves only AUB.
- `LAU` resolves only LAU.
- `Beirut` is treated as a location, not as a university reference.
- `science` alone does not resolve unrelated universities.
- explicit current-message entities are not expanded by weak history matches.
- ambiguous aliases produce clarification or safe ambiguity.
- valid canonical names and acronyms continue to resolve.
- tests cover exact, alias, partial, generic-token, and ambiguous cases.

Validation Commands

./mvnw -q -Dtest=GraduateKnowledgeResolutionSupportTest test
./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest test
./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=GraduateFollowUpResolverTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Matching Rules Added or Changed
3. False Positives Prevented
4. Files Changed
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

fix(ai): harden graduate entity resolution
```

---

# TASK 3 — Normalized Interpretation Decision Model

```text
TASK CODE: AI_INTERPRETER_REFACTOR_003_NORMALIZED_QUERY_DECISIONS

Recommended Model:
GPT-5.6 Sol

Prerequisite

AI_INTERPRETER_REFACTOR_002_ENTITY_RESOLUTION must be implemented and approved.

Objective

Refactor interpretation decisions so the system represents requests through explicit, composable dimensions instead of relying primarily on overloaded intent values and downstream special cases.

The project already contains a generalized query-model foundation. This task must review and complete its use across the runtime interpretation pipeline.

The normalized interpretation must clearly represent:

- resource
- operation
- scope
- filters
- aggregation
- sorting
- limit
- requested detail
- follow-up metadata
- interpretation source
- ambiguity reason

The design must prevent a correctly recognized query from being overwritten later by generic guards.

Observed failures include:

- global counts recognized in one layer and rejected later
- `LOCATION_LOOKUP` carrying count, list, exists, and details semantics
- later validation rules overriding earlier correct decisions
- downstream routing depending on legacy intent assumptions

Important Scope Boundary

This task should:

- audit the generalized query model created in AI_RETRIEVAL_004
- identify remaining legacy-intent dependencies
- define one final normalized interpretation result
- make validation produce or reject that result consistently
- remove contradictory downstream reinterpretation
- preserve all currently supported retrieval capabilities

This task must not:

- add new product capabilities
- redesign conversation memory
- rewrite all prompts yet
- change database schema
- implement multi-filter search beyond what already exists

Investigation First

Inspect:

- `GraduateKnowledgeQuery`
- resource, operation, scope, aggregation, sort, and filter types
- `GraduateKnowledgeIntent`
- AI interpretation DTOs
- deterministic interpreter outputs
- validator
- resolution support
- follow-up resolver
- retrieval dispatch
- `ChatApplicationService`
- legacy intent mapping
- ambiguity guards
- related tests

Required Investigation Output

Use exactly these sections:

1. Findings
2. Current Normalized Model
3. Remaining Legacy Dependencies
4. Decision Overwrite Risks
5. Proposed Final Decision Flow
6. Backward-Compatibility Mapping
7. Risks
8. Files Likely To Change
9. Validation Plan
10. Review Decision

Do not implement until approved.

Implementation Constraints

- Do not introduce `Map<String, Object>`.
- Keep query dimensions typed.
- Keep the model immutable.
- Preserve backward compatibility where practical.
- One layer must own final consistency validation.
- No later layer may silently change resource, operation, or scope.
- Unsupported combinations must be rejected explicitly.
- Avoid one large conditional chain.
- Do not add a framework or dependency.

Acceptance Criteria

The normalized model can represent and preserve:

- global university count
- global campus count
- university count by city
- campus count by city
- university list by city
- campus list by university
- campus existence
- program details
- tuition aggregation
- admission details
- language queries
- academic-structure queries

The implementation must ensure:

- a correct global count cannot be rejected by a generic university-required guard
- count/list/exists/details remain distinct
- `LOCATION_LOOKUP` is no longer the sole carrier of unrelated operations
- unsupported combinations fail safely
- existing capabilities remain green

Validation Commands

./mvnw -q -Dtest=GraduateKnowledgeQueryTest test
./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest test
./mvnw -q -Dtest=GraduateFollowUpResolverTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Query-Decision Changes
3. Legacy Mapping Changes
4. Files Changed
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

refactor(ai): normalize graduate interpretation decisions
```

---

# TASK 4 — Deterministic-First Interpretation

```text
TASK CODE: AI_INTERPRETER_REFACTOR_004_DETERMINISTIC_FIRST

Recommended Model:
GPT-5.6 Sol

Prerequisite

AI_INTERPRETER_REFACTOR_003_NORMALIZED_QUERY_DECISIONS must be implemented and approved.

Objective

Introduce deterministic-first interpretation for high-confidence, common graduate-knowledge queries.

The AI provider should not be required for simple requests that can be recognized safely and cheaply.

High-confidence examples include:

- How many universities do we have?
- How many campuses do we have?
- How many universities are in Beirut?
- How many campuses are in Beirut?
- List AUB campuses.
- Does AUB have a campus in Beirut?
- Where is the Marine Research campus?
- What is the tuition at LAU?
- Admission requirements for USEK.
- What faculties does AUB have?

The AI interpreter should remain available for linguistically complex, vague, comparative, or genuinely ambiguous requests.

Proposed Flow

1. Normalize the current message.
2. Attempt deterministic high-confidence interpretation.
3. If complete and valid, skip the provider.
4. If incomplete or ambiguous, invoke the provider.
5. Normalize both paths into the same typed query model.
6. Run one final validator.
7. Use deterministic fallback if provider parsing or validation fails.

Investigation First

Inspect:

- current deterministic interpreter
- AI interpretation adapter
- interpreter orchestration
- provider invocation rules
- fallback behavior
- validation order
- metrics/logging
- tests for provider bypass

Required Investigation Output

Use exactly these sections:

1. Findings
2. Current Interpretation Order
3. High-Confidence Deterministic Coverage
4. Provider-Required Cases
5. Proposed Orchestration
6. Fallback Policy
7. Risks
8. Files Likely To Change
9. Validation Plan
10. Review Decision

Do not implement until approved.

Implementation Constraints

- Deterministic recognition must be conservative.
- Do not build a full natural-language parser.
- Provider bypass must be observable in logs.
- Both deterministic and AI paths must produce the same normalized model.
- The provider must still handle complex language.
- Do not duplicate validation rules.
- Do not silently downgrade ambiguous queries.
- Preserve context-budget behavior.

Acceptance Criteria

Provider invocation is skipped for high-confidence requests including:

- global university count
- global campus count
- city university count
- city campus count
- direct campus list by university
- direct campus existence
- direct campus-location lookup

Provider invocation remains available for:

- complex comparisons
- vague references
- multi-constraint requests
- ambiguous program names
- linguistically complex follow-ups

Tests must prove:

- provider bypass
- provider use when needed
- provider failure followed by deterministic fallback
- identical normalized output shape across both paths
- no regression in existing supported queries

Validation Commands

./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=AiGraduateQueryInterpretationAdapterTest test
./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Deterministic Coverage Added
3. Provider Boundary
4. Files Changed
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

refactor(ai): interpret common graduate queries deterministically first
```

---

# TASK 5 — Compact AI Interpretation Contract

```text
TASK CODE: AI_INTERPRETER_REFACTOR_005_COMPACT_AI_SCHEMA

Recommended Model:
GPT-5.6 Sol

Prerequisite

AI_INTERPRETER_REFACTOR_004_DETERMINISTIC_FIRST must be implemented and approved.

Objective

Reduce and harden the AI interpretation response contract.

The current AI interpretation response contains approximately 30 fields and frequently exceeds the configured 250-token output budget.

Observed runtime behavior includes:

- `finishReason=MAX_TOKENS`
- truncated JSON
- `Unexpected end-of-input`
- inconsistent fallback outcomes
- simple queries becoming ambiguous after provider truncation

This task must create a compact AI response schema that maps cleanly into the normalized query model.

Important Questions

Investigate whether the provider supports:

- structured JSON output
- response schemas
- constrained enum output
- explicit MIME type
- reliable max-output configuration

The response should contain only fields needed for interpretation. Catalog resolution and validation must remain in Java.

Investigation First

Inspect:

- interpretation DTOs
- serialization configuration
- interpreter prompt
- Gemini adapter
- response parsing
- provider configuration
- max-output token configuration
- fallback behavior
- tests for malformed and truncated output

Required Investigation Output

Use exactly these sections:

1. Findings
2. Current Response Schema
3. Token-Budget Failure Analysis
4. Proposed Compact Schema
5. Provider Structured-Output Options
6. Backward-Compatibility Plan
7. Risks
8. Files Likely To Change
9. Validation Plan
10. Review Decision

Do not implement until approved.

Implementation Constraints

- The AI must not return database IDs as authoritative values.
- The AI must not generate SQL.
- The AI must not answer the user.
- Prefer enums and nullable compact fields.
- Omit empty collections and explanations.
- Keep catalog resolution in Java.
- Do not rely only on increasing the token limit.
- Increase output tokens only if justified after schema reduction.
- Preserve safe provider fallback.

Acceptance Criteria

- The schema is materially smaller than the current contract.
- Typical responses fit safely within the configured output budget.
- Truncated or invalid JSON triggers deterministic fallback consistently.
- No simple high-confidence query becomes ambiguous solely because the provider failed.
- AI and deterministic outputs normalize into the same query model.
- Tests cover:
  - valid compact JSON
  - unknown enum
  - missing optional fields
  - malformed JSON
  - truncated JSON
  - extra fields
  - provider timeout
  - provider max-token completion

Validation Commands

./mvnw -q -Dtest=AiGraduateQueryInterpretationAdapterTest test
./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest test
./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Schema Reduction
3. Provider Configuration Changes
4. Files Changed
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

refactor(ai): compact graduate interpretation schema
```

---

# TASK 6 — Follow-Up and Memory Isolation

```text
TASK CODE: AI_INTERPRETER_REFACTOR_006_CONTEXT_ISOLATION

Recommended Model:
GPT-5.6 Sol

Prerequisite

AI_INTERPRETER_REFACTOR_005_COMPACT_AI_SCHEMA must be implemented and approved.

Objective

Prevent recent history and conversation memory from contaminating standalone or explicitly scoped queries.

Observed problems include:

- explicit AUB questions resolving multiple universities
- city questions inheriting universities from prior turns
- `followUpResolved=true` for standalone questions
- previous entities being appended instead of replaced
- assistant error messages influencing interpretation
- contradiction between prompt instructions and injected memory

Required Behavioral Rules

1. Current-message explicit entities take priority.
2. Explicit current scope clears incompatible inherited scope.
3. History is used only for genuinely referential language.
4. Memory is advisory, not authoritative catalog truth.
5. Assistant error messages must not become entity context.
6. New-topic queries must reset stale filters.
7. Ordinal references must resolve only against eligible prior results.
8. Ambiguous references must clarify safely.

Investigation First

Inspect:

- recent history preparation
- conversation memory format
- memory updater prompt
- `GraduateFollowUpResolver`
- history entity extraction
- AI adapter prompt assembly
- assistant-message inclusion
- topic-change detection
- explicit entity replacement
- ordinal resolution
- related tests

Required Investigation Output

Use exactly these sections:

1. Findings
2. Current History and Memory Flow
3. Contamination Sources
4. Proposed Inheritance Rules
5. Topic Reset Rules
6. Ordinal and Referential Resolution
7. Risks
8. Files Likely To Change
9. Validation Plan
10. Review Decision

Do not implement until approved.

Implementation Constraints

- Do not remove useful follow-up support.
- Do not inherit entities without referential evidence.
- Do not place raw database IDs in memory.
- Keep memory size bounded.
- Preserve context-budget behavior.
- Do not make assistant error text authoritative.
- Keep current-message semantics dominant.
- Clarify only when multiple plausible referents remain.

Acceptance Criteria

These sequences behave correctly:

Sequence 1:
- Tell me about AUB.
- How many campuses does it have?

Expected:
- AUB is inherited.

Sequence 2:
- Tell me about LAU.
- How many universities are in Beirut?

Expected:
- LAU is not inherited as the only scope.

Sequence 3:
- List AUB campuses.
- What is the tuition at LAU?

Expected:
- LAU replaces AUB.

Sequence 4:
- Compare AUB and LAU.
- Which one has more campuses?

Expected:
- both comparison targets are preserved.

Sequence 5:
- trigger an assistant error
- ask a standalone university question

Expected:
- the error message does not contaminate interpretation.

Tests must cover:

- explicit replacement
- standalone reset
- pronoun inheritance
- ordinal resolution
- ambiguous `it`
- stale-filter rejection
- assistant-error exclusion
- memory contradiction handling

Validation Commands

./mvnw -q -Dtest=GraduateFollowUpResolverTest test
./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Inheritance Rules Implemented
3. Topic Reset Behavior
4. Files Changed
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

fix(ai): isolate graduate follow-up and memory context
```

---

# TASK 7 — Aggregate Retrieval Semantics

```text
TASK CODE: AI_INTERPRETER_REFACTOR_007_AGGREGATE_RETRIEVAL

Recommended Model:
GPT-5.6 Sol

Prerequisite

AI_INTERPRETER_REFACTOR_006_CONTEXT_ISOLATION must be implemented and approved.

Objective

Ensure count, aggregate, and existence questions are answered through explicit database semantics rather than by asking the final AI model to infer results from partial row lists.

Observed weak behavior includes:

- `How many universities are in Beirut?`
  - interpreter resolves a small set of universities
  - retrieval returns a very small context
  - final model reports the retrieved row count rather than the actual aggregate

- `How many campuses are in Beirut?`
  - retrieval returns partial context
  - final model claims insufficient information

Required aggregate support includes:

- total universities
- total campuses
- universities by city
- campuses by city
- campuses by university
- programs by university
- faculties by university
- departments by scope
- existence checks
- already-supported tuition aggregates

Important Scope Boundary

This task should correct aggregate and existence execution for capabilities already present.

This task must not add unrelated new resources.

Investigation First

Inspect:

- normalized query operation and aggregation fields
- retrieval dispatch
- location adapter
- graduate retrieval adapter
- count SQL
- distinct semantics
- existence SQL
- result context formatting
- list limits
- related tests

Required Investigation Output

Use exactly these sections:

1. Findings
2. Current Aggregate Behavior
3. Count Semantics
4. Existence Semantics
5. Proposed Retrieval Mapping
6. Context Output Design
7. Risks
8. Files Likely To Change
9. Validation Plan
10. Review Decision

Do not implement until approved.

Implementation Constraints

- Execute counts in SQL.
- Use explicit `COUNT(DISTINCT ...)` where joins can duplicate rows.
- Do not infer totals from bounded lists.
- Existence checks should use efficient SQL semantics.
- Preserve entity names and scope in returned context.
- Keep result limits bounded.
- Do not return duplicate entities.
- Preserve existing tuition semantics.

Acceptance Criteria

These questions return explicit, grounded values:

- How many universities do we have?
- How many campuses do we have?
- How many universities are in Beirut?
- How many campuses are in Beirut?
- How many campuses does AUB have?
- Does AUB have a campus in Beirut?
- How many master's programs does AUB offer?
- How many faculties does BAU have?

Count context must include:

- resource
- operation
- scope
- filters
- total
- optional matched entity names where useful

Existence context must include:

- checked entity
- checked condition
- boolean result
- matching evidence when present

Validation Commands

./mvnw -q -Dtest=SqlGraduateLocationRetrievalAdapterTest test
./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q -Dtest=*Retrieval*IntegrationTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Aggregate Semantics Implemented
3. Existence Semantics Implemented
4. Files Changed
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

fix(ai): execute graduate counts and existence queries explicitly
```

---

# TASK 8 — Retrieval Context Quality

```text
TASK CODE: AI_INTERPRETER_REFACTOR_008_CONTEXT_QUALITY

Recommended Model:
GPT-5.6 Sol

Prerequisite

AI_INTERPRETER_REFACTOR_007_AGGREGATE_RETRIEVAL must be implemented and approved.

Objective

Improve retrieval context quality so successful answers are informative, structured, and grounded.

Observed weak answers include:

- `Yes, AUB has a campus in Beirut.`
- `The Marine Research campus is located in Batroun.`

These answers are correct but omit useful data already available in the database.

The context layer should preserve relevant fields such as:

- full university name
- acronym
- campus name
- city
- campus type
- faculty or department name
- program name
- degree type
- language
- tuition semantics
- admission source text
- official source URLs

Important Scope Boundary

This task should improve projections and context formatting.

This task must not:

- turn every answer into an exhaustive report
- add new retrieval capabilities
- expose internal IDs
- exceed context budgets unnecessarily
- change database schema

Investigation First

Inspect:

- retrieval projections
- context formatter methods
- context labels
- final chat system prompt
- location branches
- list/count/exists/details formatting
- missing-data markers
- context-budget interaction
- response tests

Required Investigation Output

Use exactly these sections:

1. Findings
2. Current Context Shapes
3. Data Lost Before Final Generation
4. Proposed Context Templates
5. Detail-Level Rules
6. Budget Risks
7. Files Likely To Change
8. Validation Plan
9. Review Decision

Do not implement until approved.

Implementation Constraints

- Keep context structured and compact.
- Preserve all relevant scope information.
- Avoid repeated URLs and duplicate rows.
- Use detail levels where available.
- Do not invent missing information.
- Keep empty-result context explicit.
- Preserve context-budget safeguards.
- Do not hardcode final prose in retrieval.

Acceptance Criteria

Examples of expected context quality:

For:
`Does AUB have a campus in Beirut?`

Context should include:

- American University of Beirut
- AUB
- matching Beirut campus names
- campus types
- boolean existence result

For:
`Where is the Marine Research campus?`

Context should include:

- campus name
- university
- city
- campus type

For:
`List AUB campuses.`

Context should include all bounded campus records with names, cities, and types.

For:
`Admission requirements for USEK.`

Context should preserve structured requirement and source information when available.

Tests must cover:

- details context
- list context
- count context
- exists context
- no-result context
- missing fields
- duplicate prevention
- context-budget limits

Validation Commands

./mvnw -q -Dtest=SqlGraduateLocationRetrievalAdapterTest test
./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Context Templates Added or Changed
3. Data Preservation Improvements
4. Files Changed
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

refactor(ai): improve graduate retrieval context quality
```

---

# TASK 9 — Full Text-to-SQL Integration Coverage

```text
TASK CODE: AI_INTERPRETER_REFACTOR_009_PIPELINE_TESTING

Recommended Model:
GPT-5.6 Sol

Prerequisite

AI_INTERPRETER_REFACTOR_008_CONTEXT_QUALITY must be implemented and approved.

Objective

Add integration coverage for the complete graduate-query path:

User text
→ deterministic or AI interpretation
→ normalization
→ entity resolution
→ validation
→ follow-up resolution
→ retrieval dispatch
→ generated SQL
→ PostgreSQL execution
→ context output

The current test suite primarily validates isolated components. This allowed unit tests to pass while runtime behavior still failed.

Important Scope Boundary

This task is focused on testing and limited testability improvements only.

Do not add new product capabilities.

Investigation First

Inspect:

- current unit tests
- current integration tests
- Testcontainers configuration
- database fixtures
- provider stubs
- chat-service tests
- logging and observability
- gaps identified by AI_INTERPRETER_INVESTIGATION_001

Required Investigation Output

Use exactly these sections:

1. Findings
2. Current Test Coverage
3. Missing Pipeline Coverage
4. Proposed Test Architecture
5. Required Fixtures and Stubs
6. Docker and Testcontainers Risks
7. Files Likely To Change
8. Validation Plan
9. Review Decision

Do not implement until approved.

Implementation Constraints

- Do not call live AI providers.
- Use deterministic provider stubs.
- Execute SQL against real PostgreSQL where possible.
- Keep fixtures minimal and explicit.
- Avoid brittle full-response string assertions.
- Assert normalized query, route, SQL result, and context semantics.
- Keep tests deterministic.
- Do not weaken existing tests.

Required Test Matrix

Standalone:

- How many universities do we have?
- How many campuses do we have?
- How many universities are in Beirut?
- How many campuses are in Beirut?
- List universities in Beirut.
- List AUB campuses.
- Does AUB have a campus in Beirut?
- Where is the Marine Research campus?
- Tell me about Computer Science at AUB.
- What is the tuition at LAU?
- Admission requirements for USEK.

Paraphrases:

- Total universities?
- Number of campuses?
- Count unis in Beirut.
- Show AUB locations.
- Is there an AUB branch in Beirut?

Context sequences:

Sequence 1:
- Tell me about AUB.
- How many campuses does it have?

Sequence 2:
- Tell me about LAU.
- How many universities are in Beirut?

Sequence 3:
- List AUB campuses.
- What is the tuition at LAU?

Sequence 4:
- provider returns truncated JSON
- deterministic fallback still returns a valid normalized query

SQL-specific:

- absent city filter
- present city filter
- absent faculty filter
- present faculty filter
- absent department filter
- present department filter
- program lookup
- tuition aggregation
- admission lookup
- count
- exists
- multiple optional filters

Acceptance Criteria

- The complete text-to-context path is exercised.
- PostgreSQL parameter typing failures are detected by tests.
- history contamination is detected.
- provider truncation is detected.
- deterministic bypass is verified.
- count semantics are verified.
- no-result semantics are verified.
- existing unit tests remain green.
- Testcontainers failures are reported clearly if Docker is unavailable.

Validation Commands

./mvnw -q -Dtest=*Graduate*IntegrationTest test
./mvnw -q -Dtest=*Retrieval*IntegrationTest test
./mvnw -q test
./mvnw -q -DskipTests compile

Required Implementation Output

1. Summary
2. Integration Test Architecture
3. Scenarios Covered
4. Files Changed
5. Validation Results
6. Docker/Testcontainers Notes
7. Remaining Coverage Gaps
8. Implementation Review Decision

Suggested Commit Message

test(ai): cover graduate interpretation to SQL pipeline
```

---

# TASK 10 — Final Architecture and Regression Review

```text
TASK CODE: AI_INTERPRETER_REFACTOR_010_FINAL_REVIEW

Recommended Model:
GPT-5.6 Sol

Prerequisite

AI_INTERPRETER_REFACTOR_009_PIPELINE_TESTING must be implemented and approved.

Objective

Perform the final architecture, regression, correctness, performance, and documentation review for the partial interpreter refactor.

Do not add new product capabilities in this task.

Review:

- normalized query model
- deterministic-first interpretation
- compact AI schema
- entity resolution
- validation ownership
- follow-up and memory isolation
- retrieval dispatch
- dynamic SQL safety
- aggregate semantics
- context quality
- fallback behavior
- observability
- integration coverage
- documentation

Required Review Output

Use exactly these sections:

1. Executive Summary
2. Architecture Review
3. SOLID Review
4. Interpretation Flow Review
5. Entity Resolution Review
6. Follow-Up and Memory Review
7. SQL Safety Review
8. Aggregate Semantics Review
9. Context Quality Review
10. Backward-Compatibility Review
11. Test Coverage Review
12. Performance and Budget Review
13. Documentation Gaps
14. Required Fixes
15. Final Review Decision

The final review decision must be one of:

- APPROVE
- APPROVE WITH NOTES
- REJECT AND REWORK

Required Validation

Run:

./mvnw -q test
./mvnw -q -DskipTests compile

Also report:

- total tests run
- failures
- skipped tests
- integration tests executed
- Testcontainers status
- any remaining unbounded SQL path
- any remaining nullable-parameter path
- any query that still changes meaning across layers
- any standalone query contaminated by history
- any provider response that can exceed its configured output budget
- any unsupported combination that still produces misleading output

Documentation Requirements

Update the relevant technical documentation with:

- interpretation flow
- deterministic-first rules
- AI-provider boundary
- normalized query dimensions
- entity-resolution precedence
- ambiguity policy
- follow-up inheritance rules
- topic-reset rules
- fallback behavior
- SQL composition rules
- aggregate semantics
- context templates
- supported example questions
- known limitations
- extension guide for future resources and operations

Suggested Commit Message

docs(ai): document partial graduate interpreter refactor
```

---

# Completion Checklist

Complete the sequence only when all tasks are approved:

- [ ] AI_INTERPRETER_REFACTOR_001_SQL_FILTER_SAFETY
- [ ] AI_INTERPRETER_REFACTOR_002_ENTITY_RESOLUTION
- [ ] AI_INTERPRETER_REFACTOR_003_NORMALIZED_QUERY_DECISIONS
- [ ] AI_INTERPRETER_REFACTOR_004_DETERMINISTIC_FIRST
- [ ] AI_INTERPRETER_REFACTOR_005_COMPACT_AI_SCHEMA
- [ ] AI_INTERPRETER_REFACTOR_006_CONTEXT_ISOLATION
- [ ] AI_INTERPRETER_REFACTOR_007_AGGREGATE_RETRIEVAL
- [ ] AI_INTERPRETER_REFACTOR_008_CONTEXT_QUALITY
- [ ] AI_INTERPRETER_REFACTOR_009_PIPELINE_TESTING
- [ ] AI_INTERPRETER_REFACTOR_010_FINAL_REVIEW

---

# Recommended Execution Order Summary

1. Fix PostgreSQL blockers first.
2. Tighten entity resolution.
3. Stabilize the normalized decision model.
4. Add deterministic-first interpretation.
5. Compact and harden the AI schema.
6. Isolate follow-up and memory context.
7. Correct aggregate and existence retrieval.
8. Improve retrieval context quality.
9. Add full text-to-SQL integration coverage.
10. Perform final architecture and regression review.

Do not merge the SQL blocker, interpreter redesign, memory redesign, and context-quality changes into one implementation task.
