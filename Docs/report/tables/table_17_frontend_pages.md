# Table 17 — Frontend Pages
Purpose: route inventory.
| Route family | Status | Access |
|---|---|---|
| landing/auth/verification/reset | Implemented | public or flow-specific |
| `/chat` | Implemented | protected |
| `/personal-info`, `/cvs`, `/cv-builder` | Implemented | protected |
| `/admin` | Implemented | ADMIN protected |
| `/map`, `/about`, `/settings` | Implemented | router-defined |
Source evidence: `Client/src/router.tsx`. Notes: route visibility does not by itself prove every screen state was manually tested. Suggested chapter: Frontend. Last verification: 2026-07-18.
