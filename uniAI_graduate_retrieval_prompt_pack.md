# uniAI Graduate Retrieval Expansion — Sequential Prompt Pack

This file contains the complete implementation sequence for expanding the graduate-knowledge backend.

Use the prompts in order.

## Execution Rule

For every task:

1. Give the selected prompt to the coding model.
2. The model must first investigate and return the required review output.
3. Do not let it implement until the review decision is approved.
4. When the review result is `APPROVE`, continue with the implementation section of the same task.
5. After implementation, review the changed files and validation results.
6. Only after the implementation review is approved should you continue to the next task.

Do not skip tasks unless the current repository already contains the required architecture and the model proves it from the code.

---

# TASK 1 — Generalized Query Model Foundation

**Status: ✅ Completed with implementation review approved.**

```text
TASK CODE: AI_RETRIEVAL_004_QUERY_MODEL_FOUNDATION

Recommended Model:
GPT-5.6 Luna

Objective

Refactor the graduate-knowledge interpretation contract into a generalized, typed query model that can support future university, campus, program, faculty, department, location, language, admission, tuition, aggregation, comparison, and follow-up capabilities.

This task creates the reusable foundation only.

Do not implement all new retrieval capabilities in this task.

Current Problem

The current backend is centered around these intents:

- GENERAL_CHAT
- PROGRAM_LOOKUP
- TUITION_AGGREGATION
- GRADUATE_OVERVIEW
- UNKNOWN_OR_AMBIGUOUS

The current query object cannot cleanly represent:

- campus queries
- city or region filters
- faculty or department queries
- count or existence operations
- min, max, or range aggregations
- sorting or limits
- language filters
- admission queries
- generic comparisons

The system must not grow into one intent per noun and operation.

Avoid designs such as:

- CAMPUS_LIST
- CAMPUS_COUNT
- UNIVERSITY_LIST
- UNIVERSITY_COUNT
- PROGRAM_COUNT
- FACULTY_LOOKUP
- FACULTY_COUNT

Preferred Direction

Represent a request as composable dimensions.

Candidate concepts:

Resource:
- UNIVERSITY
- CAMPUS
- PROGRAM
- FACULTY
- DEPARTMENT

Operation:
- LIST
- COUNT
- EXISTS
- DETAILS
- AGGREGATE
- COMPARE

Aggregation:
- COUNT
- AVG
- MIN
- MAX
- RANGE

Filters:
- university IDs
- city
- region
- degree types
- program subject or name
- faculty
- department
- language
- admission-related field
- tuition minimum
- tuition maximum
- currency
- billing basis
- academic year

Result controls:
- sort field
- sort direction
- result limit

Follow-up context:
- referenced university
- referenced result ordinal
- inherited resource
- inherited operation
- inherited filters

Important Scope Boundary

This task should:

- design and implement the generalized typed query contract
- preserve existing program, tuition, overview, and general-chat behavior
- adapt the AI interpretation DTO and validator where required
- adapt deterministic interpretation to produce the new model
- adapt follow-up resolution to preserve current behavior
- adapt retrieval dispatch only enough to remain backward compatible
- update prompts and tests for the new contract

This task must not yet implement:

- campus SQL retrieval
- university-by-city retrieval
- faculty or department retrieval
- language filtering
- admission lookup
- min/max/range tuition retrieval
- multi-filter execution
- generic comparison execution

Investigation First

Before modifying code, inspect the current:

- GraduateKnowledgeQuery
- GraduateKnowledgeIntent
- AI interpretation DTOs
- graduate-query-interpreter-prompt.txt
- AiGraduateQueryInterpretationAdapter
- GraduateQueryInterpretationValidator
- GraduateKnowledgeQueryInterpreter
- GraduateKnowledgeResolutionSupport
- GraduateFollowUpResolver
- GraduateKnowledgeRetrievalPort
- SqlGraduateKnowledgeRetrievalAdapter
- ChatApplicationService
- related tests

Required Investigation Output

Use exactly these sections:

1. Findings
2. Current Query Contract
3. Proposed Generalized Query Contract
4. Backward-Compatibility Mapping
5. Reusable Components
6. Risks
7. Files Likely To Change
8. Validation Plan
9. Review Decision

The review decision must be one of:

- APPROVE
- APPROVE WITH NOTES
- REJECT AND REWORK

Do not implement until the review decision is APPROVE or APPROVE WITH NOTES and the user explicitly says to continue.

Implementation Constraints

- Keep controllers thin.
- Keep orchestration in application services.
- Do not leak JPA or SQL concerns into the query model.
- Prefer immutable records or existing project conventions.
- Keep enums focused and typed.
- Do not introduce a generic Map<String, Object> filter model.
- Do not add a new framework or dependency.
- Preserve all existing supported behavior.
- Preserve safe fallback behavior.
- Avoid one large switch with duplicated resource-operation logic where a small typed dispatcher is more appropriate.
- Do not overengineer a full query language parser.

Acceptance Criteria

- Existing program lookup still works.
- Existing tuition average queries still work.
- Existing graduate overview queries still work.
- Existing general chat behavior still works.
- Existing comparison and follow-up tests remain green.
- The query contract can represent:
  - resource
  - operation
  - filters
  - aggregation
  - sorting
  - limit
  - follow-up references
- Unsupported combinations are rejected or clarified safely.
- Existing legacy intents are either mapped cleanly or removed without regressions.
- Tests cover backward compatibility and the new query contract.

Validation Commands

Run the smallest relevant test set first, then the complete server test suite.

At minimum:

./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest test
./mvnw -q -Dtest=GraduateFollowUpResolverTest test
./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Architectural Changes
3. Files Changed
4. Backward-Compatibility Notes
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

The implementation review decision must be one of:

- APPROVE
- APPROVE WITH NOTES
- REJECT AND REWORK

Suggested Commit Message

refactor(ai): generalize graduate knowledge query model
```

---

# TASK 2 — Campus and University Location Queries

```text
TASK CODE: AI_RETRIEVAL_005_LOCATION_AND_CAMPUS

Recommended Model:
GPT-5.6 Luna

Prerequisite

AI_RETRIEVAL_004_QUERY_MODEL_FOUNDATION must be implemented and approved.

Objective

Implement location-aware university and campus queries using the generalized query model.

Support:

1. Campus list by city
2. Campus count by city
3. Campus list by university
4. Campus count by university
5. University list by city
6. University count by city
7. University list by region, only if region data is explicitly represented and reliable
8. University count by region, only if region data is explicitly represented and reliable
9. Basic campus existence questions

Expected Questions

- How many campuses are in Beirut?
- Which campuses are in Beirut?
- Does LAU have a campus in Byblos?
- How many campuses does Lebanese University have?
- Which universities are in Beirut?
- How many universities are in Tripoli?
- Does BAU have a campus in Beirut?
- What campuses does USJ have?

Critical Data-Semantics Investigation

Before implementation, verify what a row in the current university/catalog schema represents.

Determine whether the data models:

- one university
- one campus
- one university-campus pair
- only a primary campus
- a comma-separated or free-text campus field
- multiple campus records in another table

Do not implement a misleading COUNT query until the count semantics are proven.

Define exactly whether each count means:

- distinct campus records
- distinct campus names
- distinct physical locations
- distinct universities with a campus in the city

If the current data cannot reliably answer one of these questions, report it and constrain the implementation accordingly.

Investigation First

Inspect:

- university schema and migrations
- UniversityCatalog
- UniversityCatalogRepositoryAdapter
- UniversityCatalogJpaRepository
- relevant entities and projections
- seed data
- graduate interpreter prompt
- deterministic interpreter
- validator
- follow-up resolver
- retrieval adapter
- context formatting
- tests

Required Investigation Output

Use exactly these sections:

1. Findings
2. Campus Data Semantics
3. Supported Location Fields
4. Proposed Query Mapping
5. Retrieval Design
6. Reusable Components
7. Risks
8. Files Likely To Change
9. Validation Plan
10. Review Decision

Do not implement until approved.

Implementation Constraints

- Use typed resource, operation, and location filters.
- Do not add string-special-case handling only for Beirut.
- City matching must be normalized safely.
- Do not infer regions from city names unless the project already has a reliable mapping.
- Use distinct counting semantics explicitly.
- Keep SQL bounded.
- Do not return duplicate universities or campuses.
- Preserve current program and tuition behavior.
- Extend the AI prompt and deterministic fallback consistently.
- Add follow-up support only for location/campus references required by this scope.

Acceptance Criteria

The following are answered without an unrelated program/tuition clarification:

- How many campuses are in Beirut?
- Which campuses are in Beirut?
- Which universities are in Beirut?
- How many universities are in Beirut?
- Does LAU have a campus in Byblos?
- What campuses does USJ have?

The implementation must also:

- distinguish LIST, COUNT, and EXISTS
- validate city and university filters
- return empty-result context safely
- avoid hallucinating missing campus data
- preserve existing tests
- include deterministic fallback coverage
- include AI interpretation validation coverage
- include SQL retrieval coverage
- include ChatApplicationService orchestration coverage

Validation Commands

./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest test
./mvnw -q -Dtest=GraduateFollowUpResolverTest test
./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Data Semantics Used
3. Files Changed
4. Supported Questions
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

feat(ai): support campus and university location queries
```

---

# TASK 3 — Program Count, Availability, Faculty, and Department Queries

```text
TASK CODE: AI_RETRIEVAL_006_ACADEMIC_STRUCTURE

Recommended Model:
GPT-5.6 Luna

Prerequisite

AI_RETRIEVAL_005_LOCATION_AND_CAMPUS must be implemented and approved.

Objective

Add academic-structure retrieval capabilities using the generalized query model.

Support:

1. Program count by university
2. Program count by degree type
3. Program availability or existence checks
4. Faculty or school list by university
5. Faculty or school count by university
6. Faculty existence checks
7. Department list by faculty or university
8. Department count by faculty or university
9. Department existence checks
10. Programs associated with a faculty or department, where the schema supports reliable joins

Expected Questions

- How many master's programs does AUB offer?
- How many PhD programs are available at LAU?
- Does AUB offer a master's in artificial intelligence?
- Does LAU have a PhD in computer science?
- What faculties does AUB have?
- Does LAU have a School of Engineering?
- Which universities have a Faculty of Medicine?
- How many faculties does BAU have?
- What departments are in the Faculty of Engineering?
- Does AUB have a Computer Science department?
- What programs are offered by the Mathematics department?

Investigation First

Confirm:

- faculty and school schema
- department schema
- program-to-faculty relationships
- program-to-department relationships
- whether department data is complete enough for retrieval
- how faculty names are normalized
- whether one program can belong to multiple faculties or departments
- how duplicate counts must be avoided
- whether degree types are normalized beyond MASTER and PHD

Required Investigation Output

Use exactly these sections:

1. Findings
2. Academic Data Relationships
3. Supported and Unsupported Joins
4. Proposed Query Mapping
5. Count and Existence Semantics
6. Reusable Components
7. Risks
8. Files Likely To Change
9. Validation Plan
10. Review Decision

Do not implement until approved.

Implementation Constraints

- Reuse typed LIST, COUNT, EXISTS, and DETAILS operations.
- Do not add a separate intent for every academic entity.
- Use distinct counting where joins can duplicate rows.
- Keep resource relationships explicit.
- Do not claim department support where the data is missing or unreliable.
- Preserve all earlier capabilities.
- Avoid fuzzy matching that can silently map to the wrong faculty or department.
- Keep retrieval projections bounded.

Acceptance Criteria

Support at minimum:

- program count by recognized university
- program count by MASTER or PHD
- program existence by university and subject/name
- faculty list by university
- faculty count by university
- department list where reliable data exists
- department count where reliable data exists
- program list filtered by faculty or department where reliable data exists

Add tests for:

- exact counts
- no duplicate counts
- recognized and unrecognized academic entities
- ambiguous faculty names
- absent department data
- boolean availability questions
- follow-up such as:
  - How many does it offer?
  - Does the second university offer it?
  - What departments does that faculty have?

Validation Commands

./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest test
./mvnw -q -Dtest=GraduateFollowUpResolverTest test
./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Data Relationships Used
3. Files Changed
4. Supported Questions
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

feat(ai): add academic structure query support
```

---

# TASK 4 — Language and Admission Queries

```text
TASK CODE: AI_RETRIEVAL_007_LANGUAGE_AND_ADMISSIONS

Recommended Model:
GPT-5.6 Luna

Prerequisite

AI_RETRIEVAL_006_ACADEMIC_STRUCTURE must be implemented and approved.

Objective

Promote language-of-instruction and admission requirements from passive program-detail fields into first-class searchable and retrievable capabilities.

Support:

1. Program filtering by language
2. Language availability by university
3. Language list by university or program
4. Admission-requirement details by program
5. Admission existence questions where the data supports them
6. Program filtering by known admission attributes only if those attributes are structured reliably

Expected Questions

- Which master's programs are taught in English?
- Does USJ offer programs in French?
- Are there Arabic-taught graduate programs?
- Which universities offer English computer science programs?
- What languages are used at BAU?
- What are the admission requirements for the MBA?
- Do I need the GMAT?
- Is work experience required?
- What GPA is needed for this master's program?
- Does this program require an entrance exam?
- What documents should I submit?

Investigation First

Determine:

- whether language data is normalized or free text
- whether programs can have multiple languages
- whether admissions are structured fields or free text
- which admission questions can be answered reliably
- whether GPA, GMAT, work experience, exams, and document requirements are individually queryable
- whether admissions should remain a details projection rather than a generic filter

Required Investigation Output

Use exactly these sections:

1. Findings
2. Language Data Semantics
3. Admission Data Semantics
4. Reliably Supported Questions
5. Questions That Must Remain Unsupported
6. Proposed Query Mapping
7. Reusable Components
8. Risks
9. Validation Plan
10. Review Decision

Do not implement until approved.

Implementation Constraints

- Do not parse free-text admission requirements into hard facts unless the data structure supports it.
- Do not claim that a requirement is absent merely because the text does not mention it.
- Normalize language matching safely.
- Support multiple languages where represented.
- Keep admission answers grounded in retrieved text.
- Preserve previous capabilities.
- Do not introduce semantic search libraries in this task.

Acceptance Criteria

Support reliable answers for:

- listing programs by language
- checking whether a university or program supports a language
- listing program languages
- retrieving admission requirements for a resolved program
- answering simple admission questions only when directly grounded in data

The system must safely handle:

- missing admission data
- conflicting language data
- ambiguous program names
- follow-ups such as:
  - Is it taught in English?
  - What are its admission requirements?
  - What about the second program?

Validation Commands

./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest test
./mvnw -q -Dtest=GraduateFollowUpResolverTest test
./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Data Semantics Used
3. Files Changed
4. Supported Questions
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

feat(ai): support language and admission queries
```

---

# TASK 5 — Tuition Analytics

```text
TASK CODE: AI_RETRIEVAL_008_TUITION_ANALYTICS

Recommended Model:
GPT-5.6 Luna

Prerequisite

AI_RETRIEVAL_007_LANGUAGE_AND_ADMISSIONS must be implemented and approved.

Objective

Expand tuition retrieval beyond averages.

Support:

1. Minimum tuition
2. Maximum tuition
3. Tuition range
4. Cheapest program or university
5. Most expensive program or university
6. Tuition threshold filters
7. Sorted tuition results
8. Top-N and bottom-N results
9. Existing average tuition behavior

Expected Questions

- What is the cheapest master's program?
- Which university has the lowest tuition?
- What is the most expensive PhD program?
- What is the tuition range at AUB?
- Show programs under $10,000.
- Which university is cheaper, LAU or AUB?
- List the five cheapest programs.
- What is the average tuition for master's programs?

Critical Financial Semantics

Before implementation, define how to handle:

- different currencies
- different billing bases
- per-credit versus annual tuition
- academic years
- missing tuition values
- duplicated fee records
- ranges across incomparable units

Do not compare or aggregate incompatible tuition records silently.

Investigation First

Inspect:

- tuition schema
- tuition SQL
- currency fields
- billing basis
- academic year handling
- existing average query semantics
- current comparison logic
- data completeness
- normalization utilities, if any

Required Investigation Output

Use exactly these sections:

1. Findings
2. Tuition Data Semantics
3. Comparable and Incomparable Records
4. Proposed Aggregation Rules
5. Proposed Sorting and Limit Rules
6. Query Mapping
7. Risks
8. Files Likely To Change
9. Validation Plan
10. Review Decision

Do not implement until approved.

Implementation Constraints

- Never compare different currencies without an existing trusted conversion mechanism.
- Do not add live currency conversion.
- Never combine per-credit and annual tuition in one numeric ranking.
- Group results by currency, billing basis, and academic year where needed.
- Keep SQL aggregation in the database.
- Apply safe result limits.
- Preserve existing average behavior.
- Avoid loading all tuition rows into memory for sorting.

Acceptance Criteria

Support:

- AVG
- MIN
- MAX
- RANGE
- threshold filtering
- ascending and descending sort
- top-N and bottom-N

All results must clearly include:

- currency
- billing basis
- academic year when relevant
- program or university identity

Add tests for:

- same-currency comparison
- mixed-currency separation
- mixed billing-basis separation
- missing tuition values
- threshold queries
- top-N queries
- two-university comparison
- follow-up questions such as:
  - Which one is cheaper?
  - What is the highest one?
  - Show only those below 10,000.

Validation Commands

./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest test
./mvnw -q -Dtest=GraduateFollowUpResolverTest test
./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Tuition Semantics Used
3. Files Changed
4. Supported Questions
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

feat(ai): expand graduate tuition analytics
```

---

# TASK 6 — Multi-Filter Search

```text
TASK CODE: AI_RETRIEVAL_009_MULTI_FILTER_SEARCH

Recommended Model:
GPT-5.6 Luna

Prerequisite

AI_RETRIEVAL_008_TUITION_ANALYTICS must be implemented and approved.

Objective

Enable composable graduate-knowledge searches using multiple validated filters and operations in one query.

Expected Questions

- Which universities in Beirut offer a master's in computer science?
- Show English-taught MBA programs under $15,000.
- Which PhD programs in engineering are available at AUB or LAU?
- Find master's programs in Beirut with yearly tuition below $10,000.
- Which universities offer medicine and have a campus outside Beirut?
- Show French-taught programs at USJ.
- List the five cheapest English master's programs in Beirut.

Scope

Support safe composition across capabilities already implemented:

- resource
- operation
- university
- campus or city
- degree type
- program subject or name
- faculty
- department
- language
- tuition bounds
- currency
- billing basis
- sort
- limit

Do not add unrelated new resources in this task.

Investigation First

Determine:

- which filter combinations are supported by reliable schema joins
- which combinations create duplicate rows
- which combinations require EXISTS subqueries
- query complexity and index implications
- maximum safe filters and result limits
- how the AI interpreter and deterministic fallback represent multi-filter requests
- validation rules for incompatible combinations

Required Investigation Output

Use exactly these sections:

1. Findings
2. Supported Filter Combinations
3. Unsupported or Unsafe Combinations
4. SQL Composition Strategy
5. Validation Rules
6. Performance Risks
7. Files Likely To Change
8. Validation Plan
9. Review Decision

Do not implement until approved.

Implementation Constraints

- Do not build SQL through unsafe string concatenation.
- Prefer parameterized SQL and reusable query fragments or repository methods.
- Keep SQL composition inside infrastructure.
- Keep application-layer query objects persistence-agnostic.
- Reject incompatible filter combinations explicitly.
- Apply strict result limits.
- Avoid Cartesian products.
- Use DISTINCT or EXISTS where semantically correct.
- Preserve all single-filter behavior.

Acceptance Criteria

The expected questions above must resolve correctly when data exists.

Add tests covering:

- two-filter queries
- three-filter queries
- location + degree + program
- language + tuition
- university + department
- sort + limit
- incompatible filters
- no-result cases
- duplicate prevention
- bounded SQL results
- fallback behavior when AI interpretation is unavailable

Validation Commands

./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest test
./mvnw -q -Dtest=GraduateFollowUpResolverTest test
./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Supported Filter Combinations
3. Files Changed
4. Tests Added or Updated
5. Validation Results
6. Performance Notes
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

feat(ai): support multi-filter graduate searches
```

---

# TASK 7 — Generic Comparison and Follow-Up Expansion

```text
TASK CODE: AI_RETRIEVAL_010_GENERIC_COMPARISON_FOLLOWUP

Recommended Model:
GPT-5.6 Luna

Prerequisite

AI_RETRIEVAL_009_MULTI_FILTER_SEARCH must be implemented and approved.

Objective

Expand comparison and follow-up handling beyond the current program and tuition paths.

Support comparisons across:

- university
- campus count
- program count
- faculty count
- department count
- language availability
- admission requirements
- tuition average, minimum, maximum, and range

Support follow-ups that inherit:

- resource
- operation
- selected entities
- city or region
- degree type
- program subject
- faculty
- department
- language
- tuition constraints
- comparison targets
- sort and limit where safe

Expected Questions

- Compare AUB and LAU.
- Which one has more campuses?
- Which university has more master's programs?
- Compare their admission requirements.
- Which one offers more English programs?
- Which university has lower MBA tuition?
- How many campuses does it have?
- Which one is in Beirut?
- What about its engineering faculty?
- Does the second university teach in English?
- How many departments does that faculty have?
- What about the second program?

Investigation First

Determine:

- how current memory and recent history represent prior results
- how ordinals are resolved
- how “it,” “that university,” “the second one,” and “that faculty” are resolved
- which comparisons are objective and data-backed
- which comparisons are subjective and require clarification
- whether comparison should retrieve two bounded result sets or one grouped result set
- how to avoid stale or incorrect inherited filters

Required Investigation Output

Use exactly these sections:

1. Findings
2. Current Follow-Up Resolution
3. Proposed Typed Reference Model
4. Supported Comparison Dimensions
5. Unsupported Subjective Comparisons
6. Retrieval Strategy
7. Risks
8. Files Likely To Change
9. Validation Plan
10. Review Decision

Do not implement until approved.

Implementation Constraints

- Never answer subjective “best” questions without a defined criterion.
- Preserve current ordinal handling.
- Do not inherit stale filters when the user clearly changes the topic.
- Clarify only when multiple plausible referents remain.
- Keep comparison dimensions explicit and typed.
- Bound comparison retrieval.
- Preserve existing memory and context-budget behavior.
- Do not store raw SQL or persistence identifiers in conversation memory.

Acceptance Criteria

Support:

- typed references to university, campus, program, faculty, and department
- ordinal follow-up references
- inherited location and degree filters
- objective comparisons across supported metrics
- safe clarification for ambiguous referents
- safe handling of topic changes
- existing program and tuition follow-ups without regression

Add tests for:

- “Which one has more campuses?”
- “How many does it have?”
- “Does the second university offer it?”
- “What departments does that faculty have?”
- “Compare their admission requirements.”
- “Which one is cheaper?”
- ambiguous “it”
- stale context rejection
- new-topic reset
- unsupported subjective “best university”

Validation Commands

./mvnw -q -Dtest=GraduateFollowUpResolverTest test
./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest test
./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest test
./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest test
./mvnw -q -Dtest=ChatApplicationServiceTest test
./mvnw -q test

Required Implementation Output

1. Summary
2. Comparison Dimensions Added
3. Follow-Up Resolution Changes
4. Files Changed
5. Tests Added or Updated
6. Validation Results
7. Remaining Limitations
8. Implementation Review Decision

Suggested Commit Message

feat(ai): generalize comparisons and follow-up resolution
```

---

# Final Regression and Documentation Task

```text
TASK CODE: AI_RETRIEVAL_011_FINAL_REGRESSION_DOCUMENTATION

Recommended Model:
GPT-5.6 Luna

Prerequisite

AI_RETRIEVAL_010_GENERIC_COMPARISON_FOLLOWUP must be implemented and approved.

Objective

Perform a final architecture, regression, performance, and documentation review of the complete graduate-retrieval expansion.

Do not add new product capabilities in this task.

Review:

- generalized query model
- AI interpretation prompt
- deterministic fallback
- validation
- follow-up resolution
- location and campus retrieval
- academic structure retrieval
- language and admission retrieval
- tuition analytics
- multi-filter search
- generic comparison
- context-budget behavior
- SQL safety and bounds
- test coverage
- documentation

Required Review Output

Use exactly these sections:

1. Executive Summary
2. Architecture Review
3. SOLID Review
4. Backward-Compatibility Review
5. Query Coverage Matrix
6. SQL and Performance Review
7. Data-Semantics Risks
8. Security and Safety Review
9. Test Coverage Review
10. Documentation Gaps
11. Required Fixes
12. Final Review Decision

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
- slowest relevant test classes if available
- any unbounded SQL path
- any unsupported combination that still produces misleading output

Documentation Requirements

Update the relevant technical documentation with:

- supported resources
- supported operations
- supported filters
- aggregation rules
- tuition comparison rules
- follow-up behavior
- known limitations
- example questions
- unsupported subjective questions
- extension guide for future resources

Suggested Commit Message

docs(ai): document generalized graduate retrieval capabilities
```

---

# Completion Checklist

Complete the sequence only when all are approved:

- [ ] AI_RETRIEVAL_004_QUERY_MODEL_FOUNDATION
- [ ] AI_RETRIEVAL_005_LOCATION_AND_CAMPUS
- [ ] AI_RETRIEVAL_006_ACADEMIC_STRUCTURE
- [ ] AI_RETRIEVAL_007_LANGUAGE_AND_ADMISSIONS
- [ ] AI_RETRIEVAL_008_TUITION_ANALYTICS
- [ ] AI_RETRIEVAL_009_MULTI_FILTER_SEARCH
- [ ] AI_RETRIEVAL_010_GENERIC_COMPARISON_FOLLOWUP
- [ ] AI_RETRIEVAL_011_FINAL_REGRESSION_DOCUMENTATION
