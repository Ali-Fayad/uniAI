# AI_INTERPRETER_INVESTIGATION_001

## 1. Executive summary

Recommendation: **partially refactor the interpreter now, using a hybrid deterministic-first pipeline and a normalized query model**. A local repair alone is not sufficient. A full replacement of every AI/retrieval component in one change is also unnecessary and too risky.

The current pipeline has three production blockers and several architectural correctness defects:

1. Correct global `COUNT` interpretations can be overwritten by later generic scope guards in both `ChatApplicationService` and `route-aware conversation context`.
2. `typed route DAO adapters` always emits optional nullable predicates and binds untyped nulls, causing the observed PostgreSQL parameter-type failures across more than the two logged queries.
3. The interpretation response contract has 30 fields, the prompt requires all of them, the runtime output budget is 250 tokens, and Gemini is not configured for provider-enforced structured output. Truncation is therefore an expected operating condition, not an edge case.

The most important broader findings are:

- There is no single source of truth. Prompt rules, Java validation, deterministic recognition, follow-up resolution, service-level guards, query defaults, and retrieval guards all make overlapping routing decisions.
- A valid classification can be changed later. This happens demonstrably for global counts.
- University resolution is unsafe. Exact acronyms/names and broad individual-name-token matches are collected together. Words such as `Beirut` and `science` can resolve unrelated institutions.
- History is passed to the provider including assistant responses. Memory is also appended to a prompt that explicitly says not to use external memory. Java history analysis ignores assistant messages, but the provider does not receive that restriction structurally.
- The query model already has useful `resource`, `operation`, and typed filters, so it should be evolved rather than discarded. It lacks explicit scope, campus identity, requested fields, provenance/confidence, and an ambiguity reason.
- `LOCATION_LOOKUP` is overloaded as a compatibility intent for global counts, city counts/lists, campus lists, existence checks, and campus-name lookup. Routing should depend primarily on `resource + operation + scope/filters`, not this coarse intent.
- Location `COUNT` and `EXISTS` context is intentionally reduced to a one-line assertion. It drops the city, university, campus, matched entities, and campus type, causing weak or self-contradictory final answers.
- Current unit tests pass because they test isolated layers with canned complete JSON and recording JDBC templates. They do not test the real provider-to-validator-to-follow-up-to-router-to-PostgreSQL path.

The immediate production blockers should be repaired first in small tasks. The interpretation model and routing refactor should then be implemented in separate, staged tasks.

## Evidence and investigation boundary

- The requested `/mnt/data/logs.txt` path was not present. The attached runtime log was available as repository-root `logs.txt` and was used as primary runtime evidence.
- The working tree already contained uncommitted DB/campus implementation changes before this investigation. This report distinguishes runtime-log behavior from the currently checked-out source where they differ.
- No production code, prompt, migration, DTO, database data, or test was changed by this investigation.
- Existing targeted unit tests were executed and passed.
- Existing PostgreSQL integration tests were attempted but could not start because Testcontainers could not access a valid Docker environment. The runtime log itself contains the PostgreSQL failures and expanded prepared statements.

## 2. Runtime failure mapping

The log does not print user text, but request lengths and ordering exactly match the supplied runtime sequence. Where raw provider JSON is not logged, the provider's precise field choices cannot be reconstructed; the post-validation query is known from the application logs.

| User question | Interpretation / validation | Fallback | Retrieval / SQL route | Result | Root cause |
|---|---|---|---|---|---|
| How many universities do we have? | Gemini completed (`STOP`, 728 chars); final validation was `AMBIGUOUS`, 0 universities | Not used (`fallbackUsed=false`) | Stopped before retrieval | Generic university clarification | Provider likely emitted an unknown/ambiguous classification, or the follow-up scope guard rejected a global location query. The current deterministic recognizer can classify this phrase, but AI is called first and fallback is not used for ordinary AI ambiguity. Global scope is implicit as “no filters,” then treated as missing scope downstream. |
| How many campuses do we have? | Gemini returned 777 chars with `MAX_TOKENS`; JSON parse failed at end of object | Deterministic fallback ran but final status remained `AMBIGUOUS`, `fallbackUsed=false` | Stopped before retrieval | Generic university clarification | The 30-field JSON exceeds the safe 250-token envelope. The deterministic query is then rejected by the service's empty university/city guard; even if that were bypassed, the follow-up resolver rejects unscoped `LOCATION_LOOKUP`. |
| How many universities are in Beirut? | `VALID`, `LOCATION_LOOKUP`, 4 universities, `followUpResolved=true` | None | Location adapter; 31-character context | Final answer said four total but claimed Beirut count was unspecified | Broad token university matching can resolve every institution with `Beirut` in its name. History/provider carryover can add more. Retrieval returns only `Structured university count: 4.` and omits the city and matched names, so the final model cannot safely connect the number to Beirut. |
| How many campuses are in Beirut? | `VALID`, `LOCATION_LOOKUP`, 4 universities, `followUpResolved=true` | None | Location adapter; 27-character context | Final answer said five total but claimed Beirut count was unspecified | Same scope contamination. Count context is only `Structured campus count: 5.` with no filter metadata or matches. The final model is asked to infer semantics from an unlabeled scalar. |
| Does AUB have a campus in Beirut? | `VALID`, `LOCATION_LOOKUP`, 4 universities, `followUpResolved=true` | None | Location adapter; 59-character context | Correct but thin yes/no answer | University matching collects exact `AUB` and broad name-token matches for `Beirut`; exact current entity is not made exclusive. `EXISTS` formatting emits only a generic existence assertion and drops university/city/campus details. |
| List AUB campuses. | `VALID`, `LOCATION_LOOKUP`, 1 university, `followUpResolved=true` | None | Location adapter; 436-character context | Richest successful location route | Explicit acronym plus list operation happened to produce a bounded row list. This demonstrates that the projection has useful fields when the formatter does not collapse them. |
| Where is the Marine Research campus? | Gemini JSON truncated (`MAX_TOKENS`, 755 chars), parse failed | Deterministic fallback succeeded: `FALLBACK_USED`, 1 university, `LOCATION_LOOKUP` | Location adapter; 143-character context | Correct Batroun answer, still thin | The current deterministic source has campus-to-university/city catalog resolution. Provider reliability still failed. The query model has no campus-name filter, so resolution is indirect through university and city rather than an exact campus identity. |
| Tell me about Computer Science at AUB. | Gemini ended `MAX_TOKENS` at 767 chars but happened to contain parseable JSON; `VALID`, `PROGRAM_LOOKUP`, 3 universities, `followUpResolved=true` | None | `queryPrograms`; PostgreSQL failed at parameter `$5` | Request failed | Broad university matching can match institutions containing `science` as well as exact `AUB`; provider history can also carry entities. Program SQL always includes null optional guards and binds untyped nulls. |
| What is the tuition at LAU? | `VALID`, `TUITION_AGGREGATION`, 1 university, no follow-up | None | `queryTuitionAggregations`; PostgreSQL failed at `$3` | Request failed | Correct interpretation reached retrieval, proving this failure is independent of interpretation. The first `:city IS NULL` placeholder was an untyped null. |
| Admission requirements for USEK. | Gemini ended `MAX_TOKENS` at 770 chars but JSON parsed; `VALID`, `PROGRAM_LOOKUP`, 2 universities, `followUpResolved=true` | None | `queryPrograms`; PostgreSQL failed at `$4` | Request failed | Explicit scope was contaminated before retrieval, and the same nullable-parameter strategy failed. Admission retrieval is coupled to the program-details query, so it inherits the defect. |

The two successful location answers are not evidence that the pipeline is healthy: their interpretation scopes were sometimes contaminated, and the response formatter discards most retrieved structure for `COUNT` and `EXISTS`.

## 3. Decision-flow map

Current runtime flow:

```text
ChatApplicationService.sendMessage
  -> persist current user message
  -> load conversation memory
  -> load up to 6 prior messages (user + assistant)
  -> take last 4 as interpretation history
  -> load complete university catalog
  -> interpretGraduateQuery
       -> exact deterministic GENERAL_CHAT bypass only
       -> interpretation input budget
       -> AiGraduateRoutePlannerAdapter
            -> append memory to interpreter system prompt
            -> send prompt + raw history + current message to provider
            -> parse text as GraduateRoutePlanning JSON
       -> GraduateRouteArgumentValidator
            -> normalize enum/string fields
            -> resolve university mentions
            -> construct GraduateRoutePlan
            -> apply intent/resource/operation compatibility
            -> apply university-required guard
       -> if AI GENERAL_CHAT conflicts with Java graduate signal:
            deterministic fallback
       -> route-aware conversation context (for valid/ambiguous/fallback results)
            -> re-detect current entities and intents
            -> analyze user history and seed it with memory
            -> inherit/replace/merge universities and degree types
            -> assign clarification/ambiguity
       -> on provider exception:
            deterministic interpreter
            -> service-level ambiguity guard
            -> follow-up resolver
  -> stop for AMBIGUOUS / UNSUPPORTED
  -> route by resource:
       UNIVERSITY or CAMPUS -> SqlGraduateCatalogRouteDao
       otherwise            -> typed route DAO adapters
  -> format context as plain text
  -> final AI prompt + full bounded conversation history + context + memory
  -> persist final answer
  -> asynchronously update conversation memory
```

Decision ownership by stage:

- Intent is selected independently by the provider prompt, deterministic interpreter, and follow-up resolver's current-message re-detection.
- Resource and operation are selected by provider output or deterministic phrase rules; absent provider values fall back to intent-based defaults in `GraduateRoutePlan`.
- University entities are resolved by the interpretation validator, deterministic resolution support, follow-up resolver, and conversation-memory validator/merge code.
- History is prepared in `ChatApplicationService`; raw user and assistant messages are sent to the provider. Java follow-up analysis subsequently reads user messages only.
- Memory is loaded before interpretation, appended to the provider prompt, used again by the follow-up resolver, passed to the final model, and updated after the turn.
- Ambiguity can be assigned by the provider (although its `ambiguous` field is not consumed by the validator), validator, service fallback guard, deterministic query, or follow-up resolver.
- Fallback is selected by service budget rejection, provider/parse exception, or an unsafe AI `GENERAL_CHAT`; ordinary `AMBIGUOUS` AI output does not trigger deterministic fallback.
- Retrieval is routed solely by resource (`UNIVERSITY`/`CAMPUS` versus everything else), while individual adapters branch again by intent and operation.
- SQL is assembled in retrieval adapters. Location SQL omits absent filters dynamically; graduate-knowledge SQL generally emits nullable guards for absent filters.

## 4. Duplication and contradiction findings

| Rule / concern | Locations | Conflict or risk |
|---|---|---|
| Global/location-count recognition | Interpreter prompt; `GraduateRouteEntityResolver.detectLocationLookupIntent`, `detectLocationOperation`; validator intent mapping | Exact phrase rules and AI rules can disagree. Current uncommitted source added phrases for one observed question, but downstream guards still reject its global scope. |
| “University required” | `GraduateRouteArgumentValidator.requiresUniversity`; `ChatApplicationService.fallbackInterpretation`; `route-aware conversation context`; `typed route DAO adapters.retrieveContext`, `queryPrograms`, `queryTuitionAggregations` | Rules differ by layer. The service and follow-up guards are broader than the validator and incorrectly reject global location counts. |
| General chat versus graduate lookup | Prompt; deterministic exact casual-chat bypass; `hasGraduateSignal`; unsafe-general-chat fallback | Provider is still invoked for obvious graduate questions. Only one direction of disagreement—AI says general, Java sees graduate—gets repaired. AI ambiguity does not. |
| Follow-up detection | Prompt `followUp`; deterministic `isFollowUpMessage`; provider history; validator maps provider flag directly; follow-up resolver re-detects cues; memory comparison state | No single definition. Provider can set `followUp=true` from history even when the current question is standalone. |
| University resolution | Validator; `GraduateRouteEntityResolver`; `ConversationMemoryValidator`; memory merge matching | Similar matching is repeated with different behavior. Validator and resolution support accept broad individual tokens; memory validator uses substring checks. None establishes exact-match precedence globally. |
| Current entity versus inherited entity | Prompt; provider; follow-up resolver; memory merge | Explicit current entities are sometimes replaced correctly, but provider-produced extra entities can survive, and broad current-message matching itself creates extras. Location queries with a city preserve candidate universities even without a real follow-up cue. |
| Ambiguity | Provider fields `ambiguous`/`clarificationNeeded`; validator; deterministic query flag; service result status; follow-up resolver | Provider ambiguity metadata is validated for length but not used to make the result ambiguous. Conversely, later Java guards can override a non-ambiguous normalized query. |
| Resource/operation defaults | Prompt; validator; `GraduateRoutePlan.resourceFor/operationFor`; deterministic recognizer | `LOCATION_LOOKUP` defaults to `CAMPUS/LIST`, so omitted typed metadata silently changes university counts/list requests. |
| Count semantics | Prompt; deterministic recognizer; location adapter formatter | Query model can carry `COUNT`, but location retrieval fetches rows then uses `rows.size()` rather than a dedicated aggregate result. University rows are distinct by university **and city**, so an unfiltered global university count can overcount multi-city institutions. |
| Memory authority | Interpreter prompt says “use only bounded history” and “do not use external memory”; adapter appends “Trusted conversation memory” | Direct contradiction in the same provider request. |
| Assistant history | Java history analyzer filters to user role; provider receives assistant messages | An assistant clarification/error can influence provider interpretation even though Java follow-up logic intentionally ignores it. |
| Invalid output handling | AI adapter throws on malformed JSON; validator returns `INVALID` for semantic failures; service fallback paths differ | Parse failures use deterministic fallback. Semantic `INVALID` results bypass follow-up and later fall through to a separate deterministic call without the same status/fallback policy. |
| Optional SQL filters | Program, tuition, academic program/faculty/department, grouped comparisons | The same nullable-guard strategy is repeated, creating a family of latent PostgreSQL failures. |

Answer to the source-of-truth question: **neither the prompt nor Java validation is the sole source of truth**. The effective truth is the last layer that mutates or rejects the query, currently often `route-aware conversation context` or a retrieval guard. That is the architectural defect.

## 5. Query-model assessment

`GraduateRoutePlan` is more expressive than the coarse intent enum suggests. It already contains:

- resource
- operation
- typed filters
- aggregation
- sort and limit
- follow-up context
- detail level
- follow-up/ambiguity flags

It can represent the following only partially:

| Query shape | Current support | Gap |
|---|---|---|
| Global university/campus count | Representable as resource + `COUNT` + empty filters | No explicit `GLOBAL` scope; empty filters are mistaken for missing scope. University retrieval can count institution-city rows instead of institutions. |
| City count/list | Representable with city filter | Count context omits city and matches; inherited universities can accidentally narrow scope. |
| Campus list | Representable by campus resource + university filter | No campus ID/name filter. |
| Campus existence | Representable by `EXISTS` | Context is a generic boolean assertion with no matched details. |
| Program details | Strongly representable | Unsafe university resolution and nullable SQL break execution. |
| Tuition aggregate | Strongly representable | Resource remains `PROGRAM`; SQL strategy is broken for absent optional filters. |
| Admission requirements | Encoded as program details plus admission type filters | Admission is not a first-class resource/operation; broad requests depend on program-details routing. |
| Follow-up | Separate context and flag exist | No provenance per inherited field and no explicit “requires antecedent” decision. |

Missing normalized concepts:

- explicit scope (`GLOBAL`, `CITY`, `UNIVERSITY`, `CAMPUS`, etc.)
- campus ID/name
- requested fields or answer shape
- interpretation source (`DETERMINISTIC`, `AI`, `FALLBACK`)
- confidence or recognition strength
- ambiguity code/reason instead of one boolean
- per-filter provenance (`CURRENT_MESSAGE`, `HISTORY`, `MEMORY`)

`LOCATION_LOOKUP` is overloaded. It is functioning as a legacy domain bucket, while resource and operation now carry the useful routing semantics. It should remain temporarily for compatibility/metrics, but should cease being the primary routing key.

Paraphrase support is not robust. The deterministic recognizer uses bounded substring lists such as `how many`, `number of`, and exact university/campus wording. It does not provide a compositional lexical model for `unis`, `stored`, `available`, `operate`, `locations`, `branch`, or `present`. The provider may understand these, but provider use is precisely where truncation and history contamination occur.

## 6. Follow-up and memory assessment

Unrelated universities are carried into new questions through three mechanisms:

1. **Provider contamination:** the provider receives the last four raw messages, including assistant responses, plus appended memory. It can emit prior universities and `followUp=true` for a standalone query.
2. **Unsafe catalog matching:** the current message is matched against every distinctive token in every university name. `Beirut` and `science` are treated like aliases. Exact `AUB` does not suppress weaker matches.
3. **Resolver inheritance:** for non-location program/tuition requests, when no explicit university is detected, `resolveUniversities` can inherit a unique history/memory university even without a clear follow-up cue. For location requests, a city is enough to accept the provider candidate unchanged, including inherited universities.

Memory persistence compounds errors:

- A valid/fallback interpreted query updates active universities.
- Deterministic memory patches replace active universities with the query's resolved set.
- If a later query contains no universities, the merge policy preserves previous active universities unless explicitly cleared.
- There is no rule saying a standalone global or city query clears university scope.
- The memory allowlist does not include newer `LOCATION_LOOKUP` or `ACADEMIC_STRUCTURE_LOOKUP` intents, so AI memory patches for them can be rejected and replaced by deterministic patches; this creates another behavior split.

The desired precedence should be:

1. Explicit current entity/filter is authoritative and replaces the same inherited dimension.
2. A complete standalone query (for example resource + operation + city/global scope) does not inherit entities.
3. History/memory is consulted only when the current message contains a recognized anaphor, ellipsis, ordinal, correction, or explicit comparison continuation.
4. Assistant messages are never entity evidence for interpretation.
5. Memory can propose antecedents but cannot silently add filters.

Under these rules:

- `Tell me about LAU` → `How many universities are in Beirut?` has global city scope, not LAU scope.
- `List AUB campuses` → `What is the tuition at LAU?` replaces AUB with LAU.
- `Tell me about AUB` → `How many campuses does it have?` inherits AUB because `it` is a clear anaphor.

## 7. Provider schema and token-budget assessment

The interpreter DTO has 30 fields. The prompt prints a full JSON object and says the required shape includes every optional field, including many nulls and empty arrays. Runtime outputs are 707–777 characters and repeatedly terminate at `MAX_TOKENS` with a 250-token budget.

The current budget is not safe. Evidence:

- prompt length: approximately 6,515 characters / 1,629 estimated tokens
- interpreter output budget: 250 tokens
- complete/truncated output: approximately 700–770 characters
- multiple `MAX_TOKENS` responses
- at least two malformed end-of-input failures
- some `MAX_TOKENS` responses happened to end after parseable JSON, showing there is effectively no safety margin

The problem is not primarily verbose reasoning; the prompt correctly requests JSON only. The verbosity is structural: optional empty fields, long property names, redundant intent/resource/operation fields, and a schema that combines location, academic structure, tuition analytics, comparisons, admission filters, sort, and pagination in every response.

The current Gemini adapter sends only temperature and `max_output_tokens`. It does **not** configure a JSON MIME type or a provider response schema, and it does not reject `MAX_TOKENS` based on finish reason. Provider-enforced structured output may be supportable by the configured API/model, but that capability is not represented or tested in this repository and should be verified against the pinned provider API before implementation.

Corrective design:

- Use a compact, sparse schema: emit only applicable fields.
- Keep normalized enums but remove redundant booleans where derivable (`comparison`, for example, can derive from operation).
- Separate parse DTO from domain query so omitted values have explicit defaults.
- Configure provider-enforced JSON/schema when supported.
- Treat malformed/truncated output as provider failure and use deterministic fallback.
- Size output budget from measured worst-case valid schema plus a margin. Increasing the limit is a necessary short-term mitigation, but not the architectural solution.
- Do not send history/memory for high-confidence standalone deterministic queries.

## 8. SQL-generation assessment

The runtime PostgreSQL statement proves the failure mechanism. Named parameters appearing twice become separate positional placeholders:

```sql
AND ($3 IS NULL OR ... LOWER(BTRIM($4)))
```

When the value is absent, `$3` is a null used only in `IS NULL`, so PostgreSQL cannot infer a data type. `MapSqlParameterSource.addValue("city", null)` supplies no JDBC type. The duplicate value placeholder does not provide type information to the first placeholder.

Affected optional values and paths:

- `city`
  - program list/details
  - tuition aggregation
  - grouped program comparison
  - faculty/department count comparison
- `facultyName`
  - program list/details
  - tuition aggregation
  - academic program retrieval
  - academic faculty/department retrieval
- `departmentName`
  - program list/details
  - tuition aggregation
  - academic program/department retrieval
- `topicRegex`
  - program list/details
  - academic program retrieval

Filters already emitted conditionally—degree type, language, admission type, program name, tuition dimensions, and threshold—are less exposed to this specific null-typing defect because their clauses and parameters are generally added only when present.

Recommended strategy:

1. Build SQL predicates dynamically and omit absent optional clauses entirely.
2. Add only parameters whose clauses are present.
3. Specify JDBC types for nullable values only where a nullable bind is genuinely required.
4. Avoid `(:x IS NULL OR ...)` as the default query-construction pattern.
5. Add PostgreSQL integration tests for the power set of absent/present common filters, especially the all-absent case.

Explicit casts or `Types.VARCHAR` would repair the immediate exception, but dynamic omission is cleaner, produces simpler query plans, and makes filter composition observable and testable.

## 9. Retrieval-quality assessment

`SqlGraduateCatalogRouteDao` retrieves useful columns for list operations: university name/acronym, campus name, city, and campus type. The weakness is formatter branching:

- `COUNT` returns only `Structured <resource> count: N.`
- `EXISTS` returns only `A matching structured <resource> exists in the university data.`
- `LIST` renders the useful row fields.

This explains the observed context lengths of 27, 31, and 59 characters. The final system prompt correctly says not to infer missing official facts. Given an unlabeled scalar that omits `city=Beirut`, the final model cautiously says the Beirut-specific count is not specified.

There are also semantic count defects:

- Campus count uses fetched physical campus rows and `rows.size()`, which is semantically reasonable but should be an explicit SQL aggregate for count operations.
- University selection uses `DISTINCT` over university ID/name/acronym **and city**. For global count, a university with campuses in multiple cities produces multiple rows, so `rows.size()` is not “one university row per institution.”
- `EXISTS` does not render which row matched, even though rows were loaded.

Recommended context contract for aggregate/existence routes:

```text
Resource: UNIVERSITY
Operation: COUNT
Scope: CITY
City: Beirut
Total: N
Matched universities:
- ...
```

For existence/location details, include the full university name, acronym, campus name, city, and campus type. Optionally include sibling campuses when the operation is `DETAILS` or the user asks for all locations. Do not make every answer verbose; make the structured context complete enough for the final model to answer directly.

## 10. Test-gap assessment

### Why existing tests passed

- `GraduateRoutePlannerPortTest` directly tests the deterministic interpreter. The recently added global-count test never passes through `ChatApplicationService.fallbackInterpretation` or `route-aware conversation context`, the two layers that reject global scope.
- `ChatApplicationServiceTest.locationQueriesUseTheSeparateRetrievalPort` injects a perfect, canned AI interpretation with `city=Beirut`, uses recording retrieval ports, and asserts only which port was called.
- AI adapter tests use short, complete, static JSON. They test blank and syntactically invalid JSON, but not `finishReason=MAX_TOKENS`, end-truncated valid prefixes, or deterministic recovery through the full service.
- Follow-up tests exercise the resolver in isolation and intentionally test positive inheritance. They do not cover standalone city/global queries after a prior entity, explicit entity replacement across domains, or provider-contaminated candidates.
- JDBC unit tests use a recording template. They inspect SQL strings and parameters but do not ask PostgreSQL to prepare them.
- PostgreSQL tests construct `GraduateRoutePlan` directly, bypassing text interpretation, validation, history, memory, and routing.
- There is no test that starts with user text and reaches real generated PostgreSQL SQL and formatted context.
- Response tests use canned final-model responses, so context completeness and answer richness are not asserted.

### Execution during this investigation

- Passed: `GraduateRoutePlannerPortTest`, `route-aware conversation contextTest`, `AiGraduateRoutePlannerAdapterTest`, and `ChatApplicationServiceTest`.
- Not executed successfully: `typed route DAO adaptersIntegrationTest` and `SqlGraduateCatalogRouteDaoIntegrationTest`; Testcontainers could not find a valid Docker environment.
- Runtime evidence remains decisive for SQL: the supplied log contains PostgreSQL's expanded placeholders and exact `$3`, `$4`, and `$5` type errors.

### Required test matrix

Each standalone and paraphrase case should assert the normalized query **and** the retrieval result, not merely intent:

| Case | Required normalized assertion |
|---|---|
| How many universities do we have? / Total universities? | UNIVERSITY, COUNT, GLOBAL, no university filter |
| How many campuses do we have? / Number of campuses? | CAMPUS, COUNT, GLOBAL |
| How many universities are in Beirut? / Count unis in Beirut. | UNIVERSITY, COUNT, CITY, city=Beirut, no inherited university |
| How many campuses are in Beirut? | CAMPUS, COUNT, CITY, city=Beirut |
| List universities in Beirut. | UNIVERSITY, LIST, CITY |
| List AUB campuses. / Show AUB locations. | CAMPUS, LIST, UNIVERSITY, exactly AUB |
| Does AUB have a campus in Beirut? / Is there an AUB branch in Beirut? | CAMPUS, EXISTS, UNIVERSITY+CITY, exactly AUB |
| Where is the Marine Research campus? | CAMPUS, DETAILS/LIST, exact campus identity |
| Tell me about Computer Science at AUB. | PROGRAM, DETAILS, exactly AUB, subject/program filter |
| What is the tuition at LAU? | PROGRAM/TUITION aggregate, exactly LAU; SQL executes |
| Admission requirements for USEK. | Program/admission details, exactly USEK; SQL executes |

Conversation tests must run the whole normalizer/follow-up sequence:

1. AUB overview → “How many campuses does it have?” inherits AUB.
2. LAU overview → “How many universities are in Beirut?” clears LAU scope.
3. AUB campuses → “What is the tuition at LAU?” replaces AUB with LAU.
4. Malformed/truncated provider JSON → deterministic fallback produces the same normalized query as a provider success.
5. Prior assistant clarification/error mentioning universities → standalone current query ignores assistant entities.

PostgreSQL tests must include:

- programs with no city/faculty/department/topic filters
- tuition with no optional filters
- each optional filter individually present
- combined city + degree + faculty/department/topic filters
- admission requirements and required documents
- academic program/faculty/department list/count/exists
- grouped comparisons
- global and city aggregate location queries
- one university with campuses in multiple cities to verify institution count semantics

Response-quality tests should assert that formatted context includes resource, operation, scope/filter values, total, and useful matching fields. A deterministic final-answer fixture or contract test should verify that the answer does not need to infer an aggregate from a truncated list.

## 11. Recommended target architecture

Adopt **Approach B immediately, evolving toward the normalized parts of Approach C**.

### Approach comparison

| Approach | Assessment |
|---|---|
| A — Targeted repair | Necessary for immediate blockers, but insufficient as the end state. Fixing each guard, phrase, token budget, and SQL parameter separately leaves duplicated authority and makes future regressions likely. |
| B — Hybrid deterministic + AI | Best near-term architecture. Common bounded queries become reliable, cheap, and testable. AI remains valuable for complex language and ambiguous follow-ups. Both produce the same model and pass one validator. |
| C — Fully normalized interpretation model | Correct strategic direction, but should be incremental. Much of it already exists in `GraduateRoutePlan`; evolve that model instead of replacing all contracts at once. |

### Proposed components

1. `QueryRecognitionOrchestrator`
   - accepts current message plus explicitly bounded antecedent context
   - runs deterministic high-confidence recognizers first
   - invokes AI only when deterministic result is absent/low-confidence
   - applies one fallback policy

2. `DeterministicQueryRecognizer`
   - compositional resource/operation/scope lexicons and patterns
   - handles global/city counts, lists, direct university/campus references, and clear existence questions
   - produces an unresolved normalized query, not final catalog IDs

3. `AiQueryInterpreter`
   - handles complex constraints, comparisons, vague language, and true follow-ups
   - uses compact sparse structured output
   - never resolves database IDs and never generates SQL

4. `CatalogEntityResolver`
   - single implementation for university, campus, city, faculty, department, and program identity
   - exact alias/acronym/full-name matches first
   - weak token/fuzzy candidates only if no exact match and with ambiguity reporting
   - subject/location words cannot become institution aliases

5. `QueryNormalizerValidator`
   - the only layer allowed to create the retrieval-ready query
   - enforces resource/operation/scope/filter compatibility
   - establishes explicit-current-over-history precedence
   - emits typed ambiguity reason and provenance

6. `AntecedentResolver`
   - called only when the current parse marks a field as unresolved and contains a follow-up cue
   - consumes user-turn entities or trusted structured prior results, not arbitrary assistant prose
   - never silently adds filters to complete standalone queries

7. `RetrievalRouter`
   - dispatches by resource + operation, with scope/filters
   - does not infer route from English or coarse intent

8. Resource-specific query builders/handlers
   - location university count/list/exists
   - campus count/list/details/exists
   - program details/list
   - tuition aggregates
   - academic structure/admission handlers
   - omit absent SQL clauses and bind typed present values

9. `StructuredRetrievalContextFormatter`
   - preserves operation, scope, filters, total, matches, and citations
   - bounds lists separately from aggregate totals

### Proposed normalized query shape

```text
domain: GRADUATE_KNOWLEDGE
resource: UNIVERSITY | CAMPUS | PROGRAM | FACULTY | DEPARTMENT | TUITION | ADMISSION_REQUIREMENT
operation: COUNT | LIST | DETAILS | EXISTS | AGGREGATE | COMPARE | OVERVIEW
scope: GLOBAL | CITY | UNIVERSITY | CAMPUS | PROGRAM | FACULTY | DEPARTMENT
filters:
  universityIds
  campusId/campusName
  city
  program/subject
  degreeTypes
  facultyId/name
  departmentId/name
  tuition dimensions
aggregation/sort/limit
requestedFields
antecedentRequirements
provenance: DETERMINISTIC | AI | FALLBACK
confidence
ambiguityReason
```

This need not be a public DTO. It can be an internal model introduced alongside compatibility constructors.

### Deterministic versus AI boundary

Deterministic first:

- obvious global/city `COUNT`, `LIST`, and `EXISTS`
- exact university acronyms/aliases and campus names
- clear tuition/admission/program forms with explicit institutions
- explicit follow-up pronouns where one structured antecedent exists

AI:

- multi-constraint paraphrases not confidently recognized
- objective comparisons
- vague program/subject language
- ambiguous follow-ups with multiple plausible antecedents
- requests requiring normalization of several semantic dimensions

AI should not be called merely because a query is in natural language.

### Fallback and ambiguity policy

- Any provider failure, malformed/truncated output, unsupported schema, or invalid enum falls back to deterministic recognition.
- If deterministic recognition yields a complete high-confidence query, route it.
- If it yields a partial query with one missing required dimension, return a typed clarification.
- Global scope is valid and explicit; it is never represented as accidental missing data.
- AI ambiguity does not automatically win over a high-confidence deterministic parse.
- No layer after normalization may reinterpret intent or add inherited entities. Later layers may only reject a model invariant violation with a typed reason.

## 12. Refactoring plan

Do not combine these into one oversized implementation task.

| Task code | Objective | Likely components | Dependencies | Risk | Validation |
|---|---|---|---|---|---|
| `AI_INTERPRETER_FIX_002_SQL_OPTIONAL_FILTERS` | Repair PostgreSQL production failures by omitting absent predicates and typing binds | `typed route DAO adapters`, SQL tests | None | Medium | Real PostgreSQL tests for program, tuition, admissions, academic structure, comparisons |
| `AI_INTERPRETER_FIX_003_GLOBAL_AGGREGATES` | Make global/city university and campus counts deterministic and semantically correct | service fallback guard, follow-up resolver, location adapter, aggregate formatter | SQL fix independent | Medium | 35 universities / 71 campuses in target DB; city counts; multi-city institution fixture |
| `AI_INTERPRETER_FIX_004_ENTITY_RESOLUTION` | Centralize exact-first institution/campus matching and prevent `Beirut`/`science` false matches | validator, resolution support, memory validator, catalog aliases | None | High | Exact acronym/name, weak-token collision, multilingual and unknown entity tests |
| `AI_INTERPRETER_FIX_005_HISTORY_PRECEDENCE` | Stop scope contamination; define explicit-current and standalone-query precedence | history preparation, follow-up resolver, memory merge/trigger, interpreter request | Entity resolution preferred first | High | All four required conversation sequences and assistant-error contamination |
| `AI_INTERPRETER_FIX_006_PROVIDER_CONTRACT` | Compact sparse interpretation schema; safe output budget; structured output where supported | interpretation DTO/internal parse model, prompt, AI adapter, provider adapter/config | Normalized model decision | Medium | worst-case token measurements, `MAX_TOKENS`, malformed JSON, schema/version tests |
| `AI_INTERPRETER_REFACTOR_007_NORMALIZED_MODEL` | Add explicit scope, campus identity, provenance, and typed ambiguity while preserving compatibility | `GraduateRoutePlan`, filters/context/enums, validator | Entity resolution and policy decisions | High | model invariant/property tests; compatibility tests |
| `AI_INTERPRETER_REFACTOR_008_DETERMINISTIC_FIRST` | Add orchestrator and high-confidence deterministic-first routing | `ChatApplicationService`, interpreter ports/services, metrics | Normalized model | High | standalone/paraphrase matrix; provider call-count assertions; fallback equivalence |
| `AI_RETRIEVAL_REFACTOR_009_ROUTER_HANDLERS` | Route by resource + operation + scope and isolate query builders | retrieval port/router/adapters | Normalized model, SQL strategy | High | handler contract tests and PostgreSQL integration tests |
| `AI_RETRIEVAL_QUALITY_010_STRUCTURED_CONTEXT` | Return explicit aggregate/existence/list context with useful fields | location/program context formatters, final prompt only if needed | Stable routing | Low/Medium | golden context tests and final-answer quality scenarios |
| `AI_INTERPRETER_TEST_011_FULL_PIPELINE` | Add text-to-normalized-query-to-real-PostgreSQL integration suite | Spring integration fixtures, provider stubs, Testcontainers | Core fixes/refactor | Medium | entire required query/paraphrase/conversation matrix |

## 13. Priority order

### Production blockers

1. PostgreSQL nullable-parameter failures across program, tuition, admissions, and academic paths.
2. Global counts rejected by service/follow-up guards.
3. Provider output truncation without reliable fallback for simple queries.

### Correctness issues

1. Broad university token matching (`Beirut`, `science`) creates unrelated entities.
2. History/memory scope is applied to standalone questions.
3. Global university count counts university-city rows, not institutions.
4. Provider `ambiguous` metadata is ignored while later Java layers independently assign ambiguity.
5. Missing typed metadata defaults `LOCATION_LOOKUP` to `CAMPUS/LIST`.

### Reliability issues

1. No provider-enforced structured output in the current adapter.
2. Different failure classes follow different fallback paths.
3. `MAX_TOKENS` can be accepted when the JSON happens to parse.
4. No complete pipeline integration test.
5. Assistant responses and memory are both exposed to provider interpretation.

### Maintainability issues

1. Duplicate rules across prompt, validator, deterministic support, service guards, follow-up resolver, memory, and retrieval.
2. `LOCATION_LOOKUP` carries too many operations/resources/scopes.
3. Compatibility constructors and a 30-field provider DTO show schema accretion.
4. SQL filter construction is duplicated rather than modeled as present predicates.

### Response-quality improvements

1. Aggregate/existence context should include scope and matched data.
2. Campus answers should include university, campus name, city, and type when available.
3. Final answer generation should summarize available structured fields without inferring missing totals.

## 14. Final recommendation

A broad but staged refactor is justified.

Keep:

- the pre-retrieval interpretation boundary
- catalog-backed validation
- `GraduateRoutePlan` as the seed of the normalized internal model
- deterministic fallback as a concept
- separate structured retrieval from final answer generation
- bounded history and persistent memory, after their authority is constrained

Replace or substantially revise:

- AI-first orchestration for obvious queries
- duplicated intent/scope guards
- broad token-based university resolution
- implicit global scope
- the 30-field always-full provider contract
- uncontrolled history/memory inheritance
- intent-centric retrieval routing
- nullable-guard SQL construction
- scalar-only aggregate/existence context

Fix first, in separate production patches:

1. Dynamic optional SQL clauses and PostgreSQL coverage.
2. Global count scope through service, follow-up, and retrieval.
3. Exact-first entity resolution and history precedence.
4. Compact provider contract plus deterministic fallback guarantees.

Then introduce normalized scope/provenance and deterministic-first orchestration, followed by resource-operation handlers and full-pipeline tests.

Do **not** combine the SQL blocker, memory semantics, provider schema, normalized query migration, retrieval-router rewrite, and response-quality work into one implementation task. They have different failure modes and need independently reviewable validation.
