# Table 15 — Core Database Entities
Purpose: core logical ERD summary.
| Entity/table family | Responsibility |
|---|---|
| `users`, `verification_code` | identity, verification, role |
| `personal_info` | user profile aggregate |
| `cvs`, `cv_templates`, section tables | CV Builder |
| `chats`, `messages` | conversation history/memory |
| `feedback` | user feedback |
Source evidence: V1–V22, V56 and JPA entities. Notes: logical view only. Suggested chapter: Database design. Last verification: 2026-07-18.
