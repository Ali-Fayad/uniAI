# Table 21 — Risk Register
Purpose: report implementation risks.
| Risk | Mitigation/current state |
|---|---|
| AI provider outage or invalid credentials | provider fallback/status handling; external dependency remains |
| Oversized AI request | context budgeting and token estimation |
| Unsafe follow-up/filter broadening | typed validation and clarification |
| Mixed tuition semantics | typed scopes/currency/basis validation |
| Production CORS exposure | known TODO; restrict before production |
Source evidence: chat/security code. Notes: qualitative risk register. Suggested chapter: Risk management. Last verification: 2026-07-18.
