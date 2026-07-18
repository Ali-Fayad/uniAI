# Table 19 — AI Pipeline Components
Purpose: component responsibility map.
| Component | Responsibility |
|---|---|
| `ChatApplicationService` | orchestrates chat lifecycle |
| `GraduateQueryInterpretationValidator` | trusts only valid typed interpretation |
| `GraduateKnowledgeQueryInterpreter` | deterministic fallback |
| `GraduateFollowUpResolver` | safe reference inheritance |
| `AiContextBudgetManager` | prompt size control |
| SQL retrieval adapters | bounded evidence retrieval |
| `GraduateCitationEngine` | citation instructions/extraction |
| `ConversationMemoryManager` | memory lifecycle |
Source evidence: `chat/application` and `chat/infrastructure`. Notes: provider response quality is external. Suggested chapter: AI architecture. Last verification: 2026-07-18.
