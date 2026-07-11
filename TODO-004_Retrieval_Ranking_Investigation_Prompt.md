# TODO-004 Retrieval Ranking Investigation Prompt

``` text
TASK CODE: AI_CONTEXT_004_RETRIEVAL_RANKING_INV

Recommended Model:
GPT-5.5

Objective

Investigate how to introduce deterministic retrieval ranking into the current AI pipeline.

The goal is NOT to change retrieval filtering or AI interpretation.

The goal is ONLY to rank already-retrieved structured evidence by business relevance before context formatting.

Do not implement anything.
Do not modify any files.
Do not generate code.
Only investigate the current architecture and recommend the smallest architecture-consistent implementation.

Background

Current pipeline:

User message
→ AI Query Interpretation (TODO-003)
→ backend validation
→ trusted GraduateKnowledgeQuery
→ SQL filtering / aggregation
→ structured evidence
→ context formatting
→ AI Context Budget Manager
→ main AI provider

The SQL adapter already returns only valid rows.

Scope

Investigate:
- retrieval result objects
- formatting pipeline
- SQL ordering
- insertion point for ranking
- deterministic scoring
- reusable components
- architecture
- testing

Do NOT investigate:
- embeddings
- vector search
- semantic search
- LLM ranking
- AI scoring
- retrieval redesign
- context compression
- budgeting
- provider selection
- SQL schema
- frontend

Questions

1. Describe the exact retrieval flow after TODO-003 and identify where ranking belongs.

2. What does the SQL adapter currently return? Can ranking happen before formatting?

3. Recommend the smallest architecture-consistent insertion point.

4. Should ranking belong to:
- Infrastructure
- Application
- Formatter
Explain why.

5. Which intents benefit from ranking?
- PROGRAM_LOOKUP
- TUITION_AGGREGATION
- UNKNOWN_OR_AMBIGUOUS
- Comparison queries

6. What fields already exist for scoring?

7. Recommend a deterministic scoring strategy only.

8. Should SQL ORDER BY change?

9. Should ranking consume GraduateKnowledgeQuery or the raw user message?

10. Explain how citations remain attached after ranking.

11. Verify interaction with Context Budget Manager:
- before budgeting
- no token estimation
- no trimming
- no duplicated budget logic

12. Identify reusable components.

13. Recommend files likely to change.

14. Identify risks and mitigations.

15. Provide a validation plan and include:

cd Server && ./mvnw -q -DskipTests compile

16. End with:

READY FOR IMPLEMENTATION PROMPT

Do not generate the implementation prompt.
```
