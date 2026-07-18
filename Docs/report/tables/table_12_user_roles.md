# Table 12 — User Roles
Purpose: summarize implemented authorization roles.
| Role | Confirmed capabilities | Evidence |
|---|---|---|
| Authenticated user | profile, CV, chat, catalog, feedback | `SecurityConfig` |
| ADMIN | admin overview, users, feedback, role changes | `/api/admin/**` |
| Anonymous | auth endpoints and health | `SecurityConfig` |
Source evidence: `SecurityConfig`, `AdminController`. Notes: no additional role hierarchy was confirmed. Suggested chapter: Requirements. Last verification: 2026-07-18.
