# Table 06 — AI Provider or Model Comparison
Purpose: record configurable provider boundary.
| Provider mode | Configuration | External dependency |
|---|---|---|
| Gemini | `ai.provider=gemini` | API key/network |
| Groq | `ai.provider=groq` | API key/network |
| Ollama | `ai.provider=ollama` | local running model |
| Placeholder | fallback configuration path | no external provider |
Source evidence: `ChatAiConfiguration`, `application.properties`. Notes: no quality or latency benchmark is claimed. Suggested chapter: AI architecture. Last verification: 2026-07-18.
