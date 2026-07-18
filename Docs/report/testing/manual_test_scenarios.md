# Manual Test Scenarios

| Scenario | Route/action | Expected result |
|---|---|---|
| Registration/verification | `/signup` then `/verify` | verified account and token flow |
| Chat general response | `/chat`, send casual message | stored response and history |
| Graduate result | `/chat`, ask a supported programme query | bounded cited retrieval context used |
| Follow-up safety | ask “the second one” without rendered ordering | clarification rather than broad result |
| CV Builder | `/cv-builder` | create/edit CV and sections |
| Map | `/map` | static Leaflet markers render |
| Feedback | settings feedback UI/API | authenticated submission |
| Admin | `/admin` as ADMIN | overview and management operations |

Run only with a safe non-personal demo account and configured external dependencies.

