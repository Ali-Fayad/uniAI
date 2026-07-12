# TASK CODE: AI_FOLLOWUP_008_REFERENCE_RESOLUTION_IMPLEMENTATION

**Recommended Model:** GPT-5.5

# Objective

Implement **TODO-008 --- Follow-up Resolution**.

Add a dedicated, deterministic, application-layer follow-up resolver
that enriches incomplete but valid interpreted queries using:

-   the current user message
-   the validated AI interpretation result
-   trusted conversation memory from TODO-007
-   a bounded recent raw-conversation window
-   the trusted university catalog
-   supported intent and degree enums

The resolver must produce a fully resolved, trusted
`GraduateKnowledgeQuery` before retrieval, or return a safe
clarification-required result when a unique resolution cannot be proven.

This task must not redesign Conversation Memory, retrieval, ranking,
compression, budgeting, provider infrastructure, or SQL.

------------------------------------------------------------------------

# Current Problem

The current pipeline already performs AI query interpretation and
backend validation.

However, the main successful AI interpretation path can still
short-circuit as ambiguous before deterministic follow-up resolution
gets a chance to use memory and recent history.

Current conceptual flow:

``` text
User message
→ AI interpretation
→ backend validation
→ ambiguous/unsupported short-circuit
→ retrieval
```

Current deterministic follow-up behavior exists mainly inside the
fallback interpreter.

Target flow:

``` text
User message
→ AI interpretation
→ backend validation
→ dedicated follow-up resolver
   using:
   - current explicit values
   - trusted conversation memory
   - bounded recent history
   - trusted university catalog
→ fully resolved GraduateKnowledgeQuery
→ ambiguity/unsupported short-circuit
→ retrieval
```

The resolver must run on the normal AI-success path and not only during
deterministic fallback.

------------------------------------------------------------------------

# Architecture Requirements

Implement a small, pure application component.

Suggested name:

-   `GraduateFollowUpResolver` or
-   `GraduateKnowledgeFollowUpResolver`

Use the name that best matches the repository.

The resolver must:

-   be application-owned
-   be framework-independent
-   have no Spring imports
-   have no infrastructure imports
-   have no Jackson imports
-   have no SQL or JDBC knowledge
-   not call providers
-   not persist memory
-   not modify conversation memory
-   not perform retrieval
-   not generate user-facing answers

It should only resolve references and produce a trusted result.

Do not fold the full responsibility into:

-   `GraduateQueryInterpretationValidator`
-   `SqlGraduateKnowledgeRetrievalAdapter`
-   provider adapters
-   prompt files
-   memory persistence

------------------------------------------------------------------------

# Inputs

The resolver should receive only trusted or bounded inputs:

-   current user message
-   validated candidate `GraduateKnowledgeQuery`
-   bounded recent conversation window
-   trusted `ConversationMemory`
-   trusted university catalog

Do not pass:

-   raw AI JSON
-   raw provider response
-   retrieved context
-   SQL rows
-   full chat history
-   source URLs
-   assistant response generation state
-   provider configuration

------------------------------------------------------------------------

# Output

Use a focused result model if necessary.

Recommended concept:

``` text
GraduateFollowUpResolutionResult
```

Possible fields:

-   status
-   resolvedQuery
-   clarificationReason
-   resolutionMetadata

Recommended statuses:

-   RESOLVED
-   CLARIFICATION_REQUIRED
-   UNCHANGED
-   UNSUPPORTED

Avoid a large state hierarchy.

The result must make it impossible for retrieval to receive an
unresolved or unsafe query accidentally.

If the existing `GraduateQueryInterpretationResult` can carry this
cleanly without mixing responsibilities, a narrow extension is allowed.

Do not overload `GraduateKnowledgeQuery` with failure-state semantics
unless absolutely necessary.

------------------------------------------------------------------------

# Core Resolution Rules

## 1. Current Explicit Values Win

Explicit entities in the current message always override inherited
values.

Examples:

``` text
No, I meant USJ
```

Must replace the inherited university.

``` text
And for PhD?
```

Must replace the inherited degree type.

``` text
What about tuition?
```

Must replace/infer intent while retaining compatible university and
degree state.

Never let memory override explicit current input.

------------------------------------------------------------------------

## 2. Structured Interpretation Before Heuristics

If validated AI interpretation already contains a trusted:

-   university
-   degree type
-   intent
-   comparison state

keep it.

Only fill missing fields.

Do not replace valid interpreted fields with heuristic history or memory
values.

------------------------------------------------------------------------

## 3. Recent History Before Older Memory

When current input is incomplete:

1.  use recent raw history if it provides a unique, clear antecedent
2.  otherwise use trusted conversation memory
3.  if history and memory conflict and the current message does not
    disambiguate, require clarification

Recent history should be treated as the fresher conversational signal.

Memory should be the fallback durable state.

------------------------------------------------------------------------

## 4. Resolve Only Unique Antecedents

Pronouns and omitted references may be resolved only when there is one
safe candidate.

Supported follow-up phrases should include at minimum:

-   it
-   there
-   that
-   that one
-   those
-   them
-   same
-   same degree
-   same university
-   what about
-   how about
-   and for
-   also
-   compare them
-   first
-   first one
-   first university
-   second
-   second one
-   second university

If multiple candidates remain possible, return `CLARIFICATION_REQUIRED`.

Do not guess.

------------------------------------------------------------------------

## 5. University Resolution

Universities may be inherited from:

-   current explicit validated interpretation
-   latest unambiguous recent-history mention
-   active memory state
-   comparison memory state

Rules:

-   explicit current university wins
-   correction language replaces old university
-   singular pronouns require one unique active candidate
-   plural pronouns may use an active, ordered comparison set
-   `first` and `second` map only to an active ordered comparison list
-   do not broaden to all universities
-   do not invent universities
-   all inherited universities must already be trusted/canonical

------------------------------------------------------------------------

## 6. Degree-Type Resolution

Degree types may be inherited only when:

-   the current message omits degree type
-   a unique recent or memory degree exists
-   the inherited degree remains compatible with the current intent

Rules:

-   explicit current degree wins
-   `same degree` inherits the latest unique valid degree
-   `and for PhD` replaces the previous degree
-   ambiguous multiple active degrees require clarification
-   unsupported degree values remain unsupported
-   never silently broaden to all degree types

------------------------------------------------------------------------

## 7. Intent Resolution

Intent may be inherited when the current message is a follow-up and
omits a clear intent.

Examples:

``` text
Same at LAU?
```

May inherit the previous tuition intent if the latest active topic is
tuition.

``` text
How much does it cost?
```

Should resolve to tuition intent when the active subject is a
program/university with a unique antecedent.

``` text
What about admission requirements?
```

Should preserve current university/program context while switching to
the compatible program-details intent or detail level supported by the
current model.

Use only supported intents.

Do not invent a new intent enum.

If a concept is outside supported retrieval capabilities, return
unsupported or clarification-required according to current application
conventions.

------------------------------------------------------------------------

## 8. Comparison Resolution

Comparison state should be explicit and ordered.

Rules:

-   preserve the order of trusted comparison universities
-   `first` maps to index 0
-   `second` maps to index 1
-   `them` or `compare them` may refer to the full active comparison set
-   singular `it` must not resolve to a multi-university comparison
-   comparison state should not leak into a clearly unrelated new topic
-   explicit non-comparison current input may clear inherited comparison
    behavior for this query
-   resolver must not mutate persisted memory

If comparison target cannot be proven, require clarification.

------------------------------------------------------------------------

## 9. Correction Handling

Detect explicit correction patterns such as:

-   no, I meant
-   actually
-   instead
-   not AUB, LAU
-   sorry, I meant
-   rather
-   change that to

Correction behavior:

-   explicit current corrected entity wins
-   old inherited entity is removed from the resolved query
-   unrelated valid state may remain
-   correction should not append the corrected value to the stale one
    unless the user clearly requests comparison
-   no memory write occurs inside the resolver

Do not build a broad natural-language correction engine.

Use the smallest deterministic patterns supported by tests.

------------------------------------------------------------------------

## 10. Clarification Required

Return clarification instead of guessing when:

-   multiple recent universities are plausible
-   active comparison order is unavailable for `first`/`second`
-   singular pronoun refers to multiple candidates
-   history and memory conflict
-   no university can be safely resolved for a university-required
    intent
-   multiple degree types are active and `same degree` is used
-   unsupported reference type is requested
-   comparison target is unclear
-   query remains incomplete after all safe inheritance

Clarification must happen before retrieval.

The resolver should return a safe reason/category, not a full
user-facing paragraph if the service already owns response wording.

------------------------------------------------------------------------

# Reuse Existing Deterministic Logic

Inspect `GraduateKnowledgeQueryInterpreter`.

Reuse or extract existing helpers where appropriate for:

-   university matching
-   follow-up cue detection
-   degree matching
-   intent inheritance
-   comparison detection
-   ambiguity checks
-   history scanning

Do not duplicate large keyword sets in two classes.

Preferred options:

## Option A

Extract small shared package-private/application helpers used by:

-   deterministic fallback interpreter
-   new follow-up resolver

## Option B

Delegate selected trusted helper methods if visibility can be changed
narrowly.

Do not make the new resolver depend on fallback-only behavior
accidentally.

Keep the fallback interpreter working.

------------------------------------------------------------------------

# ChatApplicationService Integration

Update the main flow carefully.

Required order:

1.  validate request
2.  persist user message once
3.  load trusted conversation memory
4.  load bounded recent raw history
5.  call AI interpretation
6.  validate AI interpretation
7.  run follow-up resolver
8.  if clarification required:
    -   skip retrieval
    -   skip main AI provider unless current architecture explicitly
        uses it for clarification
    -   persist one safe assistant clarification response
9.  if unsupported:
    -   preserve existing unsupported behavior
10. if resolved:

-   pass only the final trusted query to retrieval

11. continue ranking, compression, main budget, and main provider
12. persist assistant response once
13. leave memory update behavior unchanged

The resolver must run:

-   on successful AI interpretation
-   on deterministic fallback output where useful

Avoid resolving the same query twice.

Do not create duplicate fallback messages.

------------------------------------------------------------------------

# Interaction With Conversation Memory

The resolver may read:

-   activeUniversities
-   activeDegreeTypes
-   lastIntent
-   comparisonActive
-   comparisonUniversities
-   pendingTopics
-   corrections if already stable
-   unresolvedReferences only if TODO-007 finalized its semantics

The resolver must not:

-   persist memory
-   update memory
-   clear memory
-   call memory AI updater
-   rely on raw invalid memory
-   store resolution output directly

If `unresolvedReferences` remains reserved/dead state, do not build
TODO-008 around it.

Use current trusted fields only.

------------------------------------------------------------------------

# Recent Raw History

Use the existing bounded recent-history window.

Do not expand history limits.

The resolver should inspect:

-   chronological message order
-   user and assistant roles
-   latest explicit university mentions
-   latest explicit degree mentions
-   latest supported intent/topic cues
-   latest comparison order
-   explicit correction turns

Do not treat assistant factual statements as authoritative university
facts.

Use assistant turns only for conversational reference order and
immediate discourse context.

Do not parse old retrieved context.

------------------------------------------------------------------------

# Retrieval Safety

The final retrieval query must contain only:

-   trusted university IDs/canonical values
-   supported degree enums
-   supported intent enum
-   trusted detail level
-   deterministic comparison ordering

Never pass:

-   pronouns
-   raw user fragments
-   AI-proposed IDs
-   unresolved references
-   unsupported degree strings
-   arbitrary topic strings into SQL filters

The SQL adapter must remain unchanged unless compilation requires a
narrow compatibility update.

Do not broaden retrieval when resolution fails.

------------------------------------------------------------------------

# User-Facing Clarification

Use a backend-controlled safe clarification response.

Examples:

``` text
Which university are you referring to?
```

``` text
Do you mean AUB or LAU?
```

``` text
Which degree type should I use?
```

Keep responses concise.

Do not expose internal resolution details.

Do not ask the main provider to improvise when the reference is
unresolved unless the existing application architecture already requires
that path and it remains safe.

------------------------------------------------------------------------

# Logging

Add metadata-only logs if needed.

Allowed:

-   resolution attempted
-   resolution status
-   source used:
    -   current message
    -   recent history
    -   conversation memory
    -   comparison state
-   resolved university count
-   resolved degree count
-   clarification category
-   correction detected
-   latency

Do not log:

-   full user message
-   full history
-   full memory
-   assistant text
-   raw interpretation JSON
-   retrieved context
-   source URLs
-   secrets

------------------------------------------------------------------------

# Tests

Add focused resolver tests.

Suggested test class:

-   `GraduateFollowUpResolverTest`

## University Inheritance

-   `How much does it cost?` inherits AUB from recent context
-   `What about LAU?` uses explicit LAU and inherited intent/degree
-   `What about there?` resolves only with one clear university
-   singular `it` with two active universities requires clarification

## Degree Inheritance

-   `The same degree` inherits one unique degree
-   multiple active degrees require clarification
-   `And for PhD?` replaces inherited MASTER
-   explicit current degree wins over memory

## Intent Inheritance

-   tuition follow-up inherits tuition intent
-   program-details follow-up inherits program context
-   explicit current intent wins
-   unrelated new topic does not inherit stale comparison intent

## Comparison References

-   `Compare them` resolves the active comparison set
-   `The first one` resolves first ordered comparison university
-   `The second university` resolves second ordered university
-   missing comparison order requires clarification
-   singular `it` does not resolve to both universities

## Corrections

-   `No, I meant USJ` replaces inherited university
-   `Actually, PhD` replaces inherited degree
-   correction does not create accidental comparison
-   unrelated fields remain preserved

## Conflict Handling

-   recent history wins over stale memory
-   explicit current message wins over both
-   history-memory conflict without explicit current value requires
    clarification
-   resolver does not mutate memory

## No Broad Retrieval

-   unresolved university produces clarification
-   unresolved degree does not become all degrees
-   unresolved comparison does not retrieve all active universities

## Service Integration

Update `ChatApplicationServiceTest` to verify:

-   resolver runs on successful AI interpretation path
-   resolver runs before ambiguous short-circuit
-   resolved query reaches retrieval
-   clarification path skips retrieval
-   clarification path skips main AI provider when designed
-   one user message persisted
-   one assistant message persisted
-   deterministic fallback remains working
-   memory update behavior unchanged
-   resolver does not persist memory

## Regression

Keep passing:

-   GraduateQueryInterpretationValidatorTest
-   GraduateKnowledgeQueryInterpreterTest
-   ChatApplicationServiceTest
-   conversation-memory tests
-   SqlGraduateKnowledgeRetrievalAdapterTest
-   SqlGraduateKnowledgeRetrievalAdapterRankingTest
-   SqlGraduateKnowledgeRetrievalAdapterCompressionTest
-   AiContextBudgetManagerTest
-   GraduateQueryInterpretationBudgetTest
-   provider adapter tests

------------------------------------------------------------------------

# Files Likely To Change

Expected:

-   `Server/src/main/java/com/uniai/chat/application/service/ChatApplicationService.java`
-   new resolver under:
    -   `Server/src/main/java/com/uniai/chat/application/retrieval/` or
        a focused follow-up package
-   optional focused resolution result/status model
-   `GraduateKnowledgeQueryInterpreter.java` only for shared-helper
    extraction
-   resolver tests
-   `ChatApplicationServiceTest.java`
-   `GraduateKnowledgeQueryInterpreterTest.java` only where shared
    behavior changes
-   `TODO.md` after validation

Potential but only if justified:

-   `GraduateQueryInterpretationResult.java`
-   `GraduateKnowledgeQuery.java`

Do not modify:

-   database migrations
-   Chat memory storage
-   memory persistence
-   provider adapters
-   prompts
-   SQL retrieval behavior
-   ranking
-   compression
-   budgeting
-   frontend

------------------------------------------------------------------------

# Validation Commands

Run at minimum:

``` bash
cd Server

./mvnw -q -Dtest=GraduateFollowUpResolverTest,GraduateKnowledgeQueryInterpreterTest,ChatApplicationServiceTest test

./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest,GraduateQueryInterpretationBudgetTest,AiGraduateQueryInterpretationAdapterTest test

./mvnw -q -Dtest=ConversationMemoryValidatorTest,ConversationMemoryMergePolicyTest,ConversationMemoryTriggerPolicyTest,ConversationMemoryPersistenceTest test

./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest,SqlGraduateKnowledgeRetrievalAdapterRankingTest,SqlGraduateKnowledgeRetrievalAdapterCompressionTest test

./mvnw -q -Dtest=AiTokenEstimatorTest,AiContextBudgetManagerTest,ConversationMemoryBudgetTest test

./mvnw -q -Dtest=GroqAiServiceAdapterTest,OllamaAiServiceAdapterTest test

./mvnw -q -DskipTests compile

git diff --check
```

If test names differ, run the actual equivalents and report exact
commands.

If `GeminiAiServiceAdapterTest` exists, run it.

Architecture checks:

``` bash
grep -R "com.uniai.chat.infrastructure" \
  Server/src/main/java/com/uniai/chat/application/retrieval \
  Server/src/main/java/com/uniai/chat/application/followup \
  || true

grep -R "org.springframework" \
  Server/src/main/java/com/uniai/chat/application/retrieval \
  Server/src/main/java/com/uniai/chat/application/followup \
  || true

grep -R "com.fasterxml.jackson" \
  Server/src/main/java/com/uniai/chat/application/retrieval \
  Server/src/main/java/com/uniai/chat/application/followup \
  || true
```

Use only paths that exist.

Expected:

-   no infrastructure imports
-   no Spring imports
-   no Jackson imports

Inspect the full Git diff for unrelated changes.

------------------------------------------------------------------------

# Acceptance Criteria

Architecture:

-   dedicated application-layer resolver exists
-   resolver is framework-independent
-   resolver runs after interpretation validation and before retrieval
-   resolver reads but never mutates conversation memory
-   retrieval receives only fully resolved trusted queries
-   no SQL/provider/persistence concerns leak into resolver

Resolution:

-   explicit current values win
-   corrections win over inherited state
-   recent history wins over stale memory
-   memory is used only as fallback durable state
-   `same`, `it`, `there`, `first`, `second`, and comparison references
    resolve only when unique
-   ambiguous references return clarification
-   no guessing
-   no broad retrieval

Orchestration:

-   main AI-success path uses resolver
-   deterministic fallback path remains valid
-   clarification skips retrieval safely
-   user/assistant messages persist exactly once
-   memory behavior remains unchanged

Scope:

-   no migration
-   no memory redesign
-   no provider changes
-   no prompt changes
-   no SQL changes
-   no ranking/compression changes
-   no budget changes
-   no frontend changes

Tests:

-   resolver rules covered
-   conflict precedence covered
-   corrections covered
-   comparison ordinals covered
-   no-broad-retrieval covered
-   service integration covered
-   existing regressions pass
-   compile passes
-   `git diff --check` passes

------------------------------------------------------------------------

# Roadmap Update

After all validation passes:

-   Mark only TODO-008 as completed in `TODO.md`.
-   Add:
    -   status
    -   main files
    -   behavior
    -   validation commands
    -   commit message
-   Do not change TODO-009 or later statuses.

If validation fails:

-   Do not mark TODO-008 completed.
-   Report failures clearly.

------------------------------------------------------------------------

# Deliverables

Return exactly these sections:

1.  Investigation Confirmation
2.  Architecture Implemented
3.  Resolution Result Contract
4.  Resolution Rules
5.  Precedence Rules
6.  University Resolution
7.  Degree Resolution
8.  Intent Resolution
9.  Comparison and Ordinal Resolution
10. Correction Handling
11. Clarification Behavior
12. Chat Orchestration
13. Files Changed
14. Test Coverage
15. Validation Results
16. TODO Roadmap Update
17. Remaining Risks
18. Commit Message

------------------------------------------------------------------------

# Commit Message

``` text
feat(chat): add follow-up reference resolution
```
