# Implementation Status

| Feature | Status | Evidence | Notes |
|---|---|---|---|
| JWT authentication, email verification, password reset, 2FA flow | Implemented | `user/application`, `AuthController`, `SecurityConfig` | Email delivery depends on SMTP configuration |
| Google OAuth URL generation | Partially Implemented | `AuthController`, `AuthApplicationService` | No confirmed callback controller exchanging a code for a uniAI JWT |
| Chat persistence and history | Implemented | `ChatApplicationService`, `chats`, `messages` | Authenticated ownership checks apply |
| Configurable AI providers | Implemented | `ChatAiConfiguration` | Gemini/Groq need keys; Ollama needs a local service; placeholder is available |
| Graduate retrieval | Implemented with notes | retrieval/interpreter/validator/adapters | See `technical/13_graduate_retrieval.md` |
| Conversation memory | Implemented | V56, `ConversationMemoryManager` | Prompt-visible form excludes persistence IDs |
| CV Builder | Implemented | `cvbuilder` module, `CVController` | PDF/export presentation behavior is frontend-owned |
| University catalog/map | Implemented | catalog module and `MapPage` | Map uses frontend static coordinate data, not the retrieval SQL model |
| Feedback | Implemented | `FeedbackController`, `Feedback` | Authenticated submission |
| Admin dashboard/API | Implemented | `AdminController`, `/admin` route | Requires ADMIN role |
| Docker integration/screenshot execution | Blocked by External Dependency | Testcontainers results | Requires reachable Docker daemon and safe demo configuration |

