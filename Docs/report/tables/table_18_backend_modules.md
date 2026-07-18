# Table 18 — Backend Modules
Purpose: module responsibilities.
| Module | Responsibility |
|---|---|
| `user` | authentication and profile account operations |
| `chat` | messages, AI, retrieval, memory, citations |
| `cvbuilder` | profile/CV data and APIs |
| `catalog` | searchable catalog data |
| `feedback` | user feedback |
| `admin` | privileged administration |
| `shared` | security, JWT, errors, infrastructure helpers |
Source evidence: `Server/src/main/java/com/uniai`. Notes: package-level organization. Suggested chapter: Backend architecture. Last verification: 2026-07-18.
