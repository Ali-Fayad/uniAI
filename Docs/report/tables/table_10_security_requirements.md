# Table 10 — Security Requirements
Purpose: summarize confirmed security behavior.
| Requirement | Status | Evidence |
|---|---|---|
| Stateless JWT authentication | Implemented | `SecurityConfig`, `JwtFilter` |
| Password hashing | Implemented | BCrypt bean |
| Admin role protection | Implemented | `/api/admin/**` rule |
| Input validation | Implemented in many controllers | `@Valid` commands |
| Restricted production CORS | Planned | TODO in `SecurityConfig` |
Source evidence: security code. Notes: no compliance certification is claimed. Suggested chapter: Security. Last verification: 2026-07-18.
