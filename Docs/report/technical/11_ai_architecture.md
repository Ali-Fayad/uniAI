# AI Architecture

`ChatApplicationService` orchestrates interpretation, optional deterministic fallback, retrieval, context budgeting, provider invocation, citation extraction, persistence, title generation, and asynchronous memory update. `ChatAiConfiguration` selects Gemini, Groq, Ollama, or a placeholder adapter from `ai.provider`. Provider credentials are external configuration and are not stored in this package.

Failure handling includes provider fallback classification, ambiguity/unsupported clarification, and budget rejection. See [AI pipeline](../diagrams/ai/ai_request_pipeline.puml).

