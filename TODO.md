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

### TODO-003 — Retrieval Ranking ⭐⭐⭐⭐☆
Rank retrieved rows by relevance before building context.

### TODO-004 — Context Compression ⭐⭐⭐⭐☆
Summarize or group repetitive retrieval results to reduce prompt size.

### TODO-005 — Token Estimation ⭐⭐⭐⭐☆
Create a reusable token estimation utility for all providers.

---

## Phase 2 — Conversation Intelligence

### TODO-006 — Better Conversation Memory ⭐⭐⭐⭐☆
Separate conversation memory from retrieved knowledge.

### TODO-007 — Follow-up Resolution ⭐⭐⭐⭐☆
Improve reference resolution for follow-up questions.

### TODO-008 — Automatic Chat Titles ⭐⭐⭐☆☆
Generate meaningful titles based on conversation content.

---

## Phase 3 — AI Providers

### TODO-009 — Provider Health Monitoring ⭐⭐⭐☆☆
Expose runtime provider availability and status.

### TODO-010 — Provider Capability Registry ⭐⭐⭐⭐☆
Centralize provider capabilities (context size, streaming, JSON, tools, vision, etc.).

### TODO-011 — Automatic Provider Fallback ⭐⭐⭐⭐☆
Automatically retry with another provider when appropriate.

---

## Phase 4 — RAG Evolution

### TODO-012 — Hybrid Retrieval ⭐⭐⭐⭐⭐
Combine SQL retrieval with vector search.

### TODO-013 — pgvector Integration ⭐⭐⭐⭐☆
Introduce semantic search using pgvector.

### TODO-014 — Citation Engine ⭐⭐⭐⭐☆
Return structured citations with sources and metadata.

---

## Phase 5 — Quality

### TODO-015 — Prompt Versioning ⭐⭐⭐☆☆
Version prompts for controlled improvements and A/B testing.

### TODO-016 — AI Integration Tests ⭐⭐⭐⭐⭐
Create end-to-end tests for the complete AI pipeline.

### TODO-017 — Retrieval Benchmark ⭐⭐⭐⭐☆
Build a benchmark suite to evaluate retrieval quality and latency.

---

## Phase 6 — Production Readiness

### TODO-018 — Streaming Responses ⭐⭐⭐☆☆
Support incremental response streaming.

### TODO-019 — Response Cache ⭐⭐⭐☆☆
Cache common AI responses.

### TODO-020 — Metrics Dashboard ⭐⭐⭐☆☆
Track:
- Provider latency
- Retrieval latency
- Token estimates
- Context size
- Provider usage
- Fallbacks
- Retrieval success rate

---

## Phase 7 — Local AI

### TODO-021 — Ollama Runtime Validation
Validate local execution with Ollama installed.

### TODO-022 — Local Model Benchmark
Benchmark Gemma, Qwen, Llama, and future local models against the same graduate-information dataset.

---

# Recommended Execution Order

1. Context Budget Manager
2. Intent-aware Retrieval
3. Retrieval Ranking
4. Context Compression
5. Conversation Memory
6. Automatic Provider Fallback
7. Hybrid Retrieval (SQL + Vector)
8. Streaming Responses
9. Production Optimization
10. Local AI Benchmarking
