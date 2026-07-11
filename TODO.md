# uniAI AI Roadmap

## Phase 1 — AI Infrastructure

### ✅ Completed
- AI provider abstraction
- Placeholder provider
- Gemini provider
- Groq provider
- Ollama provider (optional)
- SQL retrieval
- Conversation history
- Prompt loader
- Environment configuration
- Structured AI observability/logging

### ✅ TODO-001 — Context Budget Manager ⭐⭐⭐⭐⭐
**Priority:** Critical

Implement a reusable context budget manager that:
- Estimates tokens
- Enforces provider-specific budgets
- Reserves tokens for:
  - System prompt
  - Conversation history
  - Retrieved context
  - User prompt
  - AI response
- Intelligently trims context when limits are exceeded.

**Status:** Completed
**Main files:** `Server/src/main/java/com/uniai/chat/application/budget/AiContextBudgetConfiguration.java`, `Server/src/main/java/com/uniai/chat/application/budget/AiTokenEstimator.java`, `Server/src/main/java/com/uniai/chat/application/budget/AiContextBudgetManager.java`, `Server/src/main/java/com/uniai/chat/application/budget/AiContextBudgetResult.java`, `Server/src/main/java/com/uniai/chat/infrastructure/config/ChatAiConfiguration.java`, `Server/src/main/java/com/uniai/chat/application/service/ChatApplicationService.java`
**Behavior:** Estimates request tokens with a character-based heuristic, reserves output tokens, applies app-owned global and provider-specific input budgets, trims oldest conversation history before retrieval context, marks blank-context sanitization correctly, keeps truncation markers only for actual content reduction, and short-circuits requests that still cannot fit without invoking the provider.
**Validation:** `./mvnw -q -Dtest=AiTokenEstimatorTest,AiContextBudgetManagerTest,ChatApplicationServiceTest test`, `./mvnw -q -Dtest=GroqAiServiceAdapterTest,OllamaAiServiceAdapterTest test`, `./mvnw -q -DskipTests compile`
**Commit:** `fix(chat): correct AI context budget boundaries`

### ✅ TODO-002 — Intent-aware Retrieval ⭐⭐⭐⭐⭐
**Priority:** Critical

Retrieve only the information needed for the user's intent instead of entire datasets.

**Status:** Completed
**Main files:** `Server/src/main/java/com/uniai/chat/application/retrieval/GraduateKnowledgeIntent.java`, `Server/src/main/java/com/uniai/chat/application/retrieval/GraduateProgramDetailLevel.java`, `Server/src/main/java/com/uniai/chat/application/retrieval/ResolvedUniversity.java`, `Server/src/main/java/com/uniai/chat/application/retrieval/GraduateKnowledgeQuery.java`, `Server/src/main/java/com/uniai/chat/application/retrieval/GraduateKnowledgeQueryInterpreter.java`, `Server/src/main/java/com/uniai/chat/application/port/out/GraduateKnowledgeRetrievalPort.java`, `Server/src/main/java/com/uniai/chat/application/service/ChatApplicationService.java`, `Server/src/main/java/com/uniai/chat/infrastructure/retrieval/SqlGraduateKnowledgeRetrievalAdapter.java`
**Behavior:** Uses an application-owned deterministic interpreter to resolve graduate intent, universities, and degree types from the current message plus a bounded recent conversation window, passes a structured `GraduateKnowledgeQuery` into the retrieval port, executes intent-specific SQL for program lookup and tuition aggregation, keeps program list projections narrow, performs tuition averages in SQL, preserves source URLs, and returns conditional context sections only for the requested intent.
**Validation:** `./mvnw -q -Dtest=GraduateKnowledgeQueryInterpreterTest,SqlGraduateKnowledgeRetrievalAdapterTest,ChatApplicationServiceTest test`, `./mvnw -q -Dtest=AiTokenEstimatorTest,AiContextBudgetManagerTest test`, `./mvnw -q -Dtest=GroqAiServiceAdapterTest,OllamaAiServiceAdapterTest test`, `./mvnw -q -DskipTests compile`
**Commit:** `feat(chat): add intent-aware graduate retrieval`

### ✅ TODO-003 — AI Query Interpretation ⭐⭐⭐⭐⭐
**Priority:** Critical

Introduce a lightweight AI interpretation call before retrieval.

The interpreter must:
- Use a dedicated, small system prompt separate from the main chat prompt
- Receive only:
  - The current user message
  - A bounded recent conversation window
  - The supported interpretation schema
- Return strict structured JSON
- Convert natural language, typos, synonyms, and indirect phrasing into a structured interpretation
- Support multilingual or informal wording where the configured provider can understand it
- Never answer the user directly
- Never generate SQL
- Never decide which data exists
- Never bypass backend validation

The backend must:
- Validate and normalize all AI-returned values
- Resolve universities against the trusted university catalog
- Resolve degree types and intents against supported enums
- Reject unsupported or invented values safely
- Convert the external interpretation result into the trusted internal `GraduateKnowledgeQuery`
- Fall back safely when the interpretation call fails or returns invalid JSON
- Keep the existing deterministic interpreter only where it remains useful for validation, normalization, or resilience
- Apply a small independent token budget to the interpretation request

Suggested flow:

```text
User message
→ AI query interpreter
→ structured interpretation JSON
→ backend validation and normalization
→ GraduateKnowledgeQuery
→ intent-aware SQL retrieval
→ retrieval ranking
→ context formatting
→ context budget manager
→ main AI response
```

Suggested prompt file:

`Server/src/main/resources/prompts/graduate-query-interpreter-prompt.txt`

**Status:** Completed
**Main files:** `Server/src/main/java/com/uniai/chat/application/interpretation/GraduateQueryInterpretation.java`, `Server/src/main/java/com/uniai/chat/application/interpretation/GraduateQueryInterpretationRequest.java`, `Server/src/main/java/com/uniai/chat/application/interpretation/GraduateQueryInterpretationResult.java`, `Server/src/main/java/com/uniai/chat/application/interpretation/GraduateQueryInterpretationStatus.java`, `Server/src/main/java/com/uniai/chat/application/interpretation/GraduateQueryInterpretationValidator.java`, `Server/src/main/java/com/uniai/chat/application/budget/GraduateQueryInterpretationBudgetConfiguration.java`, `Server/src/main/java/com/uniai/chat/application/budget/GraduateQueryInterpretationBudgetManager.java`, `Server/src/main/java/com/uniai/chat/application/port/out/GraduateQueryInterpretationPort.java`, `Server/src/main/java/com/uniai/chat/application/port/out/GraduateQueryInterpreterPromptPort.java`, `Server/src/main/java/com/uniai/chat/infrastructure/config/GraduateQueryInterpretationProperties.java`, `Server/src/main/java/com/uniai/chat/infrastructure/prompt/GraduateQueryInterpreterPromptProvider.java`, `Server/src/main/java/com/uniai/chat/infrastructure/interpretation/AiGraduateQueryInterpretationAdapter.java`, `Server/src/main/java/com/uniai/chat/infrastructure/config/ChatAiConfiguration.java`, `Server/src/main/java/com/uniai/chat/application/service/ChatApplicationService.java`, `Server/src/main/resources/prompts/graduate-query-interpreter-prompt.txt`
**Behavior:** Adds a dedicated lightweight AI interpretation step before retrieval, loads a separate prompt, parses strict structured JSON, validates and normalizes the untrusted AI output against the trusted catalog and enums, resolves multilingual university references including Arabic names, handles unsupported degree states safely, keeps deterministic fallback behavior, applies an independent interpretation budget, and preserves the existing main AI response flow.
**Validation:** `./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest,GraduateQueryInterpretationBudgetTest,AiGraduateQueryInterpretationAdapterTest,GraduateQueryInterpreterPromptProviderTest,GraduateKnowledgeQueryInterpreterTest,ChatApplicationServiceTest,SqlGraduateKnowledgeRetrievalAdapterTest,AiContextBudgetManagerTest,GroqAiServiceAdapterTest,OllamaAiServiceAdapterTest test`, `./mvnw -q -DskipTests compile`
**Commit:** `feat(chat): add AI query interpretation`

### TODO-004 — Retrieval Ranking ⭐⭐⭐⭐☆
Rank retrieved evidence by relevance before building context.

Ranking should:
- Consume the validated `GraduateKnowledgeQuery`
- Apply only to intents where multiple candidate rows exist
- Prefer deterministic, application-owned relevance rules
- Rank structured evidence before formatting
- Preserve source traceability
- Avoid duplicating context-budget logic
- Keep SQL responsible for filtering and aggregation
- Keep business relevance scoring outside the SQL adapter unless a simple SQL ordering is clearly sufficient

**Status:** Completed
**Main files:** `Server/src/main/java/com/uniai/chat/infrastructure/retrieval/SqlGraduateKnowledgeRetrievalAdapter.java`, `Server/src/test/java/com/uniai/chat/infrastructure/retrieval/SqlGraduateKnowledgeRetrievalAdapterRankingTest.java`
**Behavior:** Deterministically ranks already-materialized graduate program and tuition evidence inside the SQL retrieval adapter before context formatting, preserving comparison university order, source URLs, and all retrieved rows while favoring richer and better-supported evidence for context assembly.
**Validation:** `./mvnw -q -Dtest=SqlGraduateKnowledgeRetrievalAdapterTest,SqlGraduateKnowledgeRetrievalAdapterRankingTest test`, `./mvnw -q -Dtest=GraduateQueryInterpretationValidatorTest,GraduateKnowledgeQueryInterpreterTest,ChatApplicationServiceTest test`, `./mvnw -q -Dtest=AiTokenEstimatorTest,AiContextBudgetManagerTest,GraduateQueryInterpretationBudgetTest test`, `./mvnw -q -Dtest=GroqAiServiceAdapterTest,OllamaAiServiceAdapterTest test`, `./mvnw -q -DskipTests compile`
**Commit:** `feat(chat): rank retrieved graduate evidence`

### TODO-005 — Context Compression ⭐⭐⭐⭐☆
Summarize or group repetitive retrieval results to reduce prompt size.

Compression should happen after retrieval and ranking, while preserving:
- Important facts
- University and program distinctions
- Currency separation
- Missing-data notes
- Source traceability

### TODO-006 — Token Estimation ⭐⭐⭐⭐☆
Create a reusable token estimation utility for all providers and AI request types.

It should support:
- Main chat response requests
- AI query interpretation requests
- Future provider fallback requests
- Provider-specific estimation strategies where needed

---

## Phase 2 — Conversation Intelligence

### TODO-007 — Better Conversation Memory ⭐⭐⭐⭐☆
Separate conversation memory from retrieved knowledge.

### TODO-008 — Follow-up Resolution ⭐⭐⭐⭐☆
Improve reference resolution for follow-up questions.

This task should build on:
- The structured AI query interpretation result
- The bounded recent conversation window
- Validated universities, degree types, and intents

### TODO-009 — Automatic Chat Titles ⭐⭐⭐☆☆
Generate meaningful titles based on conversation content.

---

## Phase 3 — AI Providers

### TODO-010 — Provider Health Monitoring ⭐⭐⭐☆☆
Expose runtime provider availability and status.

### TODO-011 — Provider Capability Registry ⭐⭐⭐⭐☆
Centralize provider capabilities, including:
- Context size
- Streaming support
- Structured JSON support
- Tool support
- Vision support
- Suitable models for lightweight interpretation
- Suitable models for final response generation

### TODO-012 — Automatic Provider Fallback ⭐⭐⭐⭐☆
Automatically retry with another provider when appropriate.

Fallback behavior should distinguish:
- Query interpretation failures
- Invalid structured output
- Main answer-generation failures
- Provider unavailability
- Rate limits
- Local budget rejection

---

## Phase 4 — RAG Evolution

### TODO-013 — Hybrid Retrieval ⭐⭐⭐⭐⭐
Combine SQL retrieval with vector search.

### TODO-014 — pgvector Integration ⭐⭐⭐⭐☆
Introduce semantic search using pgvector.

### TODO-015 — Citation Engine ⭐⭐⭐⭐☆
Return structured citations with sources and metadata.

---

## Phase 5 — Quality

### TODO-016 — Prompt Versioning ⭐⭐⭐☆☆
Version prompts for controlled improvements and A/B testing.

Version prompt families independently:
- Main chat answer prompt
- Graduate query interpreter prompt
- Future specialized prompts

Track:
- Prompt version
- Prompt purpose
- Compatible structured schema version
- Provider/model compatibility
- Rollback behavior

### TODO-017 — AI Integration Tests ⭐⭐⭐⭐⭐
Create end-to-end tests for the complete AI pipeline.

The suite should cover both AI calls:
- Query interpretation
- Main answer generation

It should validate:
- Structured output parsing
- Backend normalization
- Retrieval selectivity
- Ranking
- Context budgeting
- Persistence
- Provider failure behavior

### TODO-018 — Retrieval Benchmark ⭐⭐⭐⭐☆
Build a benchmark suite to evaluate:
- Intent interpretation accuracy
- University and degree resolution
- Retrieval precision
- Ranking quality
- Context size
- End-to-end latency
- Provider usage

---

## Phase 6 — Production Readiness

### TODO-019 — Streaming Responses ⭐⭐⭐☆☆
Support incremental response streaming for the main answer-generation call.

The query interpretation call should remain non-streaming and structured.

### TODO-020 — Response Cache ⭐⭐⭐☆☆
Cache common AI responses.

Do not cache raw interpretation output without including:
- Interpreter prompt version
- Schema version
- Provider/model
- Relevant recent-history signature

### TODO-021 — Metrics Dashboard ⭐⭐⭐☆☆
Track:
- Provider latency
- Query interpretation latency
- Main response latency
- Retrieval latency
- Token estimates
- Interpretation token usage
- Main response token usage
- Context size
- Provider usage
- Fallbacks
- Invalid structured outputs
- Retrieval success rate
- Ranking candidate/selected counts

---

## Phase 7 — Local AI

### TODO-022 — Ollama Runtime Validation
Validate local execution with Ollama installed.

Validate separately:
- Structured query interpretation
- Main response generation

### TODO-023 — Local Model Benchmark
Benchmark Gemma, Qwen, Llama, and future local models against the same graduate-information dataset.

Compare:
- Structured interpretation accuracy
- JSON/schema compliance
- Retrieval-query quality
- Final answer quality
- Latency
- Resource usage

---

# Recommended Execution Order

1. Context Budget Manager
2. Intent-aware Retrieval
3. AI Query Interpretation
4. Retrieval Ranking
5. Context Compression
6. Better Conversation Memory
7. Follow-up Resolution
8. Provider Capability Registry
9. Automatic Provider Fallback
10. Hybrid Retrieval (SQL + Vector)
11. Prompt Versioning
12. AI Integration Tests
13. Retrieval Benchmark
14. Streaming Responses
15. Production Optimization
16. Local AI Validation and Benchmarking
