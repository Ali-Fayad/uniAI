# Table 16 — AI Database Entities
Purpose: AI/knowledge logical ERD summary.
| Entity/table family | Responsibility |
|---|---|
| `university`, faculty, department | academic structure/location |
| `graduate_program`, aliases/tracks/relationships | programme data |
| `degree_type`, `language` | typed programme dimensions |
| `graduate_tuition_rate`, fee item | tuition/fee evidence |
| admission/document/deadline | admissions evidence |
| `source`, programme source | provenance |
Source evidence: V23/V24 and seed migrations. Notes: physically same PostgreSQL database. Suggested chapter: Database design. Last verification: 2026-07-18.
