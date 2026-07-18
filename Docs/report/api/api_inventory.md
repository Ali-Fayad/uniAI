# API Inventory

Controller-derived inventory. All routes are REST endpoints. Authentication/role enforcement is from `SecurityConfig`; request/response types are taken from controller signatures where confirmed.

| Method | Path | Auth/role | Purpose | Entry point |
|---|---|---|---|---|
| POST | `/api/auth/signup` | Public | create unverified account | `AuthController.signUp` |
| POST | `/api/auth/signin` | Public | authenticate | `AuthController.signIn` |
| POST | `/api/auth/verify`, `/api/auth/2fa/verify` | Public | verification flows | `AuthController` |
| POST | `/api/auth/verify/resend`, password endpoints | Public | recovery/verification | `AuthController` |
| GET/POST | `/api/auth/check-*`, `/api/auth/google/url` | Public | availability/OAuth URL | `AuthController` |
| GET/PUT/DELETE/POST | `/api/users/*` | Authenticated | current account | `UserController` |
| POST/GET/DELETE | `/api/chats*` | Authenticated | chats and messages | `ChatController` |
| GET/POST/PUT/DELETE | `/api/cv*` | Authenticated | CV Builder/profile | `CVController`, `PersonalInfoController` |
| GET | `/api/skills`, `/languages`, `/positions`, `/universities` | Authenticated | catalogs | `CatalogController` |
| POST | `/api/feedback` | Authenticated | feedback | `FeedbackController` |
| GET/DELETE/PATCH | `/api/admin/*` | ADMIN | administration | `AdminController` |

Important status codes are controller/service dependent: signup returns 202; chat creation returns 201; successful updates generally return 200; delete actions vary between 200 and 204; validation/security errors are handled through shared exception/security handling. See category files for detail.

