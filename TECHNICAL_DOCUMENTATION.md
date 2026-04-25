# uniAI Technical Documentation

## Documentation Basis

This document was generated from the current codebase and validated against the existing Markdown documents in the repository.

- Source of truth: application code, configuration, Docker files, and Flyway migrations.
- Legacy context used: `README.md`, `API_DOC.md`, `DTOs.md`, `externalAPI.md`, `UnisCoordinateTable.md`, and `Server/docker-compose.yml`.
- When legacy docs conflict with code, this document follows the code and calls out the mismatch explicitly.

---

## 1. System Overview

### What the system currently is

`uniAI` is a full-stack web application with three main product areas:

1. Authentication and user profile management
2. CV and personal-information management
3. A chat UI backed by a placeholder AI adapter

It also includes:

- Catalog lookup APIs for skills, languages, positions, and universities
- A university map page in the frontend
- Feedback submission
- Dockerized local development with PostgreSQL, Spring Boot, React/Vite, and nginx

### High-level architecture

```text
Browser
  -> React SPA (Vite in dev, nginx entrypoint in Docker)
  -> REST API calls to /api/*
  -> Spring Boot backend
  -> PostgreSQL via Spring Data JPA / Flyway
  -> JSON response back to frontend
```

### Main runtime components

| Component | Technology | Purpose |
|---|---|---|
| Frontend | React 19 + TypeScript + Vite | SPA for auth, chat, CV builder, personal info, settings, map |
| Backend | Spring Boot 4, Spring Security, Spring Data JPA | REST API, JWT auth, business logic, data persistence |
| Database | PostgreSQL 17 | Persistent storage for users, CVs, chats, catalogs, feedback |
| Reverse proxy | nginx | TLS termination and routing to the backend and frontend dev server |
| Data migrations | Flyway | Schema creation and seed data |

### Current backend architecture style

The backend is organized by bounded context and each context follows a DDD/hexagonal package layout:

- `domain`: entities, builders, repositories
- `application`: DTOs, use-case ports, application services
- `infrastructure`: JPA adapters, config, external clients, converters
- `presentation`: REST controllers

Main backend modules:

- `user`
- `chat`
- `cvbuilder`
- `catalog`
- `feedback`
- `shared`

### Important reality checks

- The chat module is not connected to a real LLM provider yet. `PlaceholderAiServiceAdapter` currently returns `AI response to: <user message>`.
- Google OAuth is only partially implemented. The backend exposes `POST /api/auth/google/url`, but there is no controller callback endpoint that exchanges a Google authorization code for a uniAI JWT.
- The map page uses a static frontend dataset from `Client/src/data/universities.ts`, not the backend university catalog.

---

## 2. Repository Structure

```text
uniAI/
├── Client/                     React + TypeScript frontend
├── Server/                     Spring Boot backend
│   ├── src/main/java/com/uniai
│   ├── src/main/resources
│   ├── server/Dockerfile
│   └── pom.xml
├── nginx/                      nginx image, config, local TLS certs
├── docker-compose.yml          Main full-stack Docker orchestration
├── TECHNICAL_DOCUMENTATION.md  This document
└── legacy markdown docs        Historical references, partially outdated
```

---

## 3. Backend Documentation

### 3.1 Backend project structure

Important packages under `Server/src/main/java/com/uniai`:

| Package | Responsibility |
|---|---|
| `user` | Sign-up, sign-in, verification, password reset, profile updates |
| `chat` | Chat creation, message storage, AI response generation |
| `cvbuilder` | Personal info, CV templates, CV CRUD, CV section CRUD |
| `catalog` | Searchable lookup data for skills, languages, positions, universities |
| `feedback` | Feedback submission |
| `shared` | JWT, security config, exception handling, email utilities, dev bootstrap |

### 3.2 Security and authentication flow

#### Authentication model

- JWT-based stateless authentication
- Public endpoints: `/api/auth/**` and `/actuator/health`
- All other endpoints require authentication
- JWT is validated by `JwtFilter`
- The filter extracts the user email from JWT claims and stores it as the Spring Security principal

#### JWT payload

The token carries user identity fields, including:

- `username`
- `firstName`
- `lastName`
- `email`
- `isVerified`
- `isTwoFacAuth`

The backend rejects tokens where `isVerified` is false.

#### Sign-up and sign-in behavior

1. `POST /api/auth/signup`
2. User is created with `isVerified = false`
3. Verification code is emailed
4. Backend throws `VerificationNeededException`
5. Global exception handler returns `202 Accepted`
6. Frontend redirects to `/verify`

Sign-in follows the same pattern:

- If email is not verified, backend returns `202`
- If 2FA is enabled, backend sends a code and returns `401`
- If fully valid and verified, backend returns `200` with `{ "token": "..." }`

#### Profile completion gate for CV APIs

`ProfileCompletionInterceptor` protects most `/api/cv/**` routes.

- If personal info is missing or `is_filled` is false, the backend throws `PersonalInfoGoneException`
- The response status is `410 Gone`
- The frontend handles this by redirecting the user to `/personal-info`

Excluded from that gate:

- `/api/cv/personal-info`
- `/api/cv/personal-info/status`
- `/api/cv/skills`
- `/api/cv/positions`
- `/api/cv/universities`
- `/api/cv/languages` is excluded in config, but no such controller endpoint exists

### 3.3 Backend service layers

#### User module

- `AuthApplicationService`: sign-up, sign-in, email verification, 2FA verification, password reset, availability checks, Google auth URL creation
- `UserApplicationService`: get current user, update profile, delete account, change password

#### Chat module

- `ChatApplicationService`: create chat, send messages, list chats, get messages, delete one chat, delete all chats
- `PlaceholderAiServiceAdapter`: mock AI provider

#### CV builder module

- `PersonalInfoApplicationService`: read/update user-level personal info JSON sections
- `CVApplicationService`: CV CRUD, default CV selection, section CRUD, template selection
- `ExternalApiApplicationService`: legacy string-list skills and positions endpoints

#### Catalog module

- `CatalogQueryService`: searchable catalog endpoints backed by database tables
- `CatalogSyncService`: refreshes skill and position catalog data
- `ExternalCatalogSyncService`: runs sync at startup and on schedule

#### Feedback module

- `FeedbackApplicationService`: validates and stores feedback

### 3.4 API endpoint reference

#### Authentication endpoints

| Method | Path | Auth | Notes |
|---|---|---|---|
| `POST` | `/api/auth/signup` | No | Creates user, usually returns `202` until verification is completed |
| `POST` | `/api/auth/signin` | No | Returns token, or `202` for email verification, or `401` for 2FA |
| `POST` | `/api/auth/verify` | No | Verifies email code and returns token |
| `POST` | `/api/auth/2fa/verify` | No | Verifies 2FA code and returns token |
| `POST` | `/api/auth/forget-password` | No | Sends password reset code |
| `POST` | `/api/auth/forget-password/confirm` | No | Confirms reset and returns token |
| `POST` | `/api/auth/google/url` | No | Returns Google authorization URL only |
| `GET` | `/api/auth/check-email?email=...` | No | Returns `{ available, message }` |
| `GET` | `/api/auth/check-username?username=...` | No | Returns `{ available, message }` |

#### User endpoints

| Method | Path | Auth | Notes |
|---|---|---|---|
| `GET` | `/api/users/me` | Yes | Returns current profile |
| `PUT` | `/api/users/me` | Yes | Updates username, names, email, 2FA flag |
| `DELETE` | `/api/users/me` | Yes | Deletes account after password confirmation |
| `POST` | `/api/users/change-password` | Yes | Changes password |

#### Chat endpoints

| Method | Path | Auth | Notes |
|---|---|---|---|
| `POST` | `/api/chats` | Yes | Creates an empty chat |
| `POST` | `/api/chats/messages` | Yes | Stores user message, generates placeholder AI response |
| `GET` | `/api/chats` | Yes | Returns user chats |
| `GET` | `/api/chats/{chatId}/messages` | Yes | Returns ordered messages |
| `DELETE` | `/api/chats/{chatId}` | Yes | Deletes one chat |
| `DELETE` | `/api/chats` | Yes | Deletes all chats |

#### Catalog endpoints

All catalog endpoints below require authentication.

| Method | Path | Query | Notes |
|---|---|---|---|
| `GET` | `/api/skills` | `search` optional | DB-backed skill catalog |
| `GET` | `/api/languages` | `search` optional | DB-backed language catalog |
| `GET` | `/api/positions` | `search` optional | DB-backed position catalog |
| `GET` | `/api/universities` | `search` optional | DB-backed university catalog |

#### CV and personal-info endpoints

| Method | Path | Auth | Notes |
|---|---|---|---|
| `GET` | `/api/cv/personal-info` | Yes | Current personal info aggregate |
| `PUT` | `/api/cv/personal-info` | Yes | Upserts personal info and marks profile complete |
| `GET` | `/api/cv/personal-info/status` | Yes | Returns `{ isFilled, missingFields }` |
| `GET` | `/api/cv/templates` | Yes | Active CV templates |
| `GET` | `/api/cv/templates/{id}` | Yes | One template |
| `GET` | `/api/cv` | Yes | User CVs |
| `GET` | `/api/cv/{id}` | Yes | One CV with nested sections |
| `POST` | `/api/cv` | Yes | Creates a CV |
| `PUT` | `/api/cv/{id}` | Yes | Updates a CV |
| `DELETE` | `/api/cv/{id}` | Yes | Deletes a CV and its sections |

Section CRUD under `/api/cv`:

| Method | Path |
|---|---|
| `POST` | `/api/cv/{cvId}/education` |
| `PUT` | `/api/cv/education/{id}` |
| `DELETE` | `/api/cv/education/{id}` |
| `POST` | `/api/cv/{cvId}/experience` |
| `PUT` | `/api/cv/experience/{id}` |
| `DELETE` | `/api/cv/experience/{id}` |
| `POST` | `/api/cv/{cvId}/skill` |
| `PUT` | `/api/cv/skill/{id}` |
| `DELETE` | `/api/cv/skill/{id}` |
| `POST` | `/api/cv/{cvId}/project` |
| `PUT` | `/api/cv/project/{id}` |
| `DELETE` | `/api/cv/project/{id}` |
| `POST` | `/api/cv/{cvId}/language` |
| `PUT` | `/api/cv/language/{id}` |
| `DELETE` | `/api/cv/language/{id}` |
| `POST` | `/api/cv/{cvId}/certificate` |
| `PUT` | `/api/cv/certificate/{id}` |
| `DELETE` | `/api/cv/certificate/{id}` |

Additional CV lookup endpoints:

| Method | Path | Notes |
|---|---|---|
| `GET` | `/api/cv/skills` | Legacy string-list endpoint from external source |
| `GET` | `/api/cv/positions` | Legacy string-list endpoint from local JSON file |
| `GET` | `/api/cv/universities` | Detailed university campus records |

#### Feedback endpoint

| Method | Path | Auth | Notes |
|---|---|---|---|
| `POST` | `/api/feedback` | Yes | Stores feedback content and optional rating |

### 3.5 Request and response model summary

#### Core auth payloads

- `SignUpCommand`: `username`, `firstName`, `lastName`, `email`, `password`
- `SignInCommand`: `email`, `password`
- `VerifyCommand`: `email`, `verificationCode`
- `RequestPasswordCommand`: `email`, `verificationCode`, `newPassword`

#### Personal info payload

`UpdatePersonalInfoCommand` contains:

- scalar fields: `phone`, `address`, `linkedin`, `github`, `portfolio`, `summary`, `jobTitle`, `company`
- list sections: `education`, `skills`, `languages`, `experience`, `projects`, `certificates`

Validation currently requires:

- `phone`
- `address`
- `summary`
- at least one `skills` entry

Phone must match:

```text
+{countryCode} {number}
```

Example:

```text
+961 70123456
```

#### CV payload

- `CreateCVCommand`: `cvName`, `templateId`, optional `sectionsOrder`, `selectedItems`, `itemsOrder`, `isDefault`
- `UpdateCVCommand`: partial update equivalent

`CVResponse` returns:

- CV metadata
- selected template metadata
- selected item ids and item order
- merged `personalInfo`
- persisted CV sections

### 3.6 Data model

Primary tables created by Flyway:

| Table | Purpose |
|---|---|
| `users` | Core user accounts |
| `verification_code` | Email verification, 2FA, password reset codes |
| `personal_info` | User-level reusable profile and CV source data |
| `cvs` | CV shells owned by users |
| `cv_templates` | Available templates |
| `educations`, `experiences`, `skills`, `languages`, `projects`, `certificates` | CV section records |
| `chats`, `messages` | Chat history |
| `skill`, `position`, `language`, `university` | Catalog tables |
| `feedback` | User feedback |

Important modeling notes:

- `personal_info` stores several sections as JSON text fields rather than normalized relational tables.
- `cvs.sections_order`, `cvs.selected_items`, and `cvs.items_order` are JSON-based fields.
- University data exists in two forms:
  - `university` table used by backend catalogs and CV associations
  - static frontend map data used by the map page

### 3.7 Error handling

The global exception handler uses mixed response styles:

- Many errors return plain text bodies
- Personal-info validation returns JSON with `missingFields`
- Incomplete profile gate returns `410 Gone`

Common statuses:

| Status | Typical cause |
|---|---|
| `400` | Validation errors, invalid credentials, invalid verification code |
| `401` | 2FA required, unauthorized access in service layer |
| `403` | Missing or invalid JWT in filter/security layer |
| `404` | Missing chat, CV, user-linked resources |
| `409` | Duplicate email or username |
| `410` | Personal info not completed for CV flow |
| `202` | Verification code required after sign-up or sign-in |

---

## 4. Frontend Documentation

### 4.1 Frontend structure

Important frontend folders:

| Path | Purpose |
|---|---|
| `Client/src/components/page` | Route-level composition components |
| `Client/src/components/common` | Shared UI primitives |
| `Client/src/components/chat` | Chat-specific components |
| `Client/src/components/page/personalInfo` | Personal-info page shell, state helpers, sections |
| `Client/src/components/page/cvBuilder` | CV builder page shell, templates, controller |
| `Client/src/context` | Auth and notification context |
| `Client/src/hooks` | Reusable stateful hooks |
| `Client/src/services` | API integration layer |
| `Client/src/types` | DTOs mirrored from backend contracts |
| `Client/src/constants` | Endpoint constants and static UI text |

### 4.2 Routing

Routes defined in `Client/src/router.tsx`:

| Route | Protection | Purpose |
|---|---|---|
| `/` | Public | Landing page |
| `/auth` | Public | Auth landing |
| `/signin` | Public | Sign-in |
| `/signup` | Public | Sign-up |
| `/verify` | Public | Email verification |
| `/2fa/verify` | Public | 2FA verification |
| `/forgot-password` | Public | Request reset code |
| `/forgot-password/confirm` | Public | Confirm password reset |
| `/google/callback` | Public | Frontend callback page expecting `token` query param |
| `/chat` | Protected | Chat UI |
| `/welcome` | Protected | Post-auth onboarding |
| `/personal-info` | Protected | Profile completion page |
| `/cvs` | Protected | CV list |
| `/cv-builder` | Protected | Create CV |
| `/cv-builder/:cvId` | Protected | Edit CV |
| `/settings` | Public route in router, but most actions expect auth |
| `/map` | Public | Static university map |
| `/about` | Public | About page |

### 4.3 State management

The frontend uses local React state and context, not Redux.

#### Global state

- `AuthContext`
  - stores current user and auth status
  - restores auth from `sessionStorage`
  - extracts user data from JWT claims
- `NotificationProvider`
  - local app notifications

#### Feature state

- Chat state: `useChat`
- Sign-in and sign-up state: `useSignIn`, `useSignUp`
- Settings state: `useProfileSettings`, `useThemeSettings`, `useSettingsFeedback`
- Personal info page: `usePersonalInfoController` plus supporting hooks
- CV builder: `useCVBuilderController`
- CV list: `useCVListController`

### 4.4 Authentication handling in the frontend

- Token and user data are stored in `sessionStorage`
- Axios interceptor adds `Authorization: Bearer <token>` to non-auth requests
- `ProtectedRoute` blocks protected pages until auth state is restored
- Error interceptor behavior:
  - `401`: clear storage and redirect to auth
  - `403`: for protected API calls, clear storage and redirect
  - `410`: redirect to `/personal-info`

Important nuance:

- The JWT does not include a numeric user id
- `extractUserFromToken()` therefore cannot reliably populate `user.id`
- Some frontend code still assumes a numeric id for optimistic chat rendering
- The backend remains the authority for ownership and sender identity

### 4.5 API integration layer

Frontend API services:

| Service | Responsibility |
|---|---|
| `authService` | Auth and verification endpoints |
| `userService` | `/api/users/*` and `/api/feedback` |
| `chatService` | `/api/chats/*` |
| `cvService` | personal info, catalogs, templates, CV CRUD |

API base URL behavior:

- `VITE_API_URL = ""` by default
- In Docker/nginx mode, requests are same-origin to `/api/...`
- In direct Vite development, Vite proxies `/api` to `VITE_BACKEND_TARGET` or `http://localhost:9090`

### 4.6 Frontend feature flow

#### Auth flow

1. User submits sign-in or sign-up form
2. Frontend calls `authService`
3. Depending on status code:
   - `200`: save JWT and continue
   - `202`: navigate to verification page
   - `401`: navigate to 2FA verification page
4. After auth, onboarding checks whether personal info already exists

#### Personal info flow

1. Frontend loads `/api/cv/personal-info`
2. User edits scalar fields and section lists
3. Frontend validates required fields before submit
4. Backend persists JSON-backed personal info aggregate
5. Successful save marks profile as complete

#### CV builder flow

1. Frontend loads personal info and template catalog
2. User selects template, enabled sections, selected items, and ordering
3. Frontend creates or updates `/api/cv`
4. PDF download currently uses `window.print()`

#### Chat flow

1. Frontend creates a chat if needed
2. Frontend sends message to `/api/chats/messages`
3. Backend stores user message
4. Placeholder AI adapter returns mock response
5. Backend stores AI response and returns it

### 4.7 Map feature

The map page is independent from the backend catalog:

- source: `Client/src/data/universities.ts`
- rendering: `react-leaflet`
- behavior: local hook `useMap`

This differs from older documentation that implies map data is fully backend-driven.

---

## 5. Docker Setup

### 5.1 Main Docker Compose file

The root `docker-compose.yml` is the active full-stack setup.

Services:

| Service | Purpose | Internal address |
|---|---|---|
| `postgres` | PostgreSQL database | `postgres:5432` |
| `app` | Spring Boot backend | `app:9090` |
| `client-build` | Produces frontend build into shared volume | writes to `client_dist` |
| `client-dev` | Vite dev server with HMR | `client-dev:5173` |
| `nginx` | TLS terminator and reverse proxy | exposed on host `80` and `443` |

### 5.2 Container communication

Current nginx routing in `nginx/default.conf`:

- `/api...` -> `http://app:9090`
- `/health` -> `http://app:9090/actuator/health`
- `/` -> `http://client-dev:5173`

Important operational consequence:

- Although `client-build` populates `/usr/share/nginx/html`, the current nginx config always proxies `/` to the Vite dev server.
- This means the built static frontend volume is mounted but not used for normal page traffic unless nginx config is changed.

### 5.3 Environment variables

#### Root Docker Compose

| Variable | Used by | Purpose |
|---|---|---|
| `POSTGRES_USER` | postgres, app | DB username |
| `POSTGRES_PASSWORD` | postgres, app | DB password |
| `POSTGRES_DB` | postgres, app | DB name |
| `MAIL_USERNAME` | app | SMTP username |
| `MAIL_PASSWORD` | app | SMTP password |
| `JWT_SECRET` | app | JWT signing key |
| `APP_BASE_URL` | app | Base URL for links in emails |

#### Backend configuration

Also supported in `application.properties`:

| Variable | Purpose |
|---|---|
| `GOOGLE_CLIENT_ID` | Google OAuth setup |
| `GOOGLE_CLIENT_SECRET` | Google OAuth setup |
| `GOOGLE_REDIRECT_URI` | Google callback target |
| `SKILLS_API_URL` | StackExchange skills source |
| `POSITIONS_API_URL` | Declared but not used by `PositionsApiClient` |
| `SYNC_EXTERNAL_CATALOG_CRON` | Catalog sync schedule |
| `CORS_ALLOWED_ORIGINS` | Declared in properties but not used by `SecurityConfig` |

#### Frontend development variables

| Variable | Purpose |
|---|---|
| `VITE_API_URL` | Direct API base URL override |
| `VITE_BACKEND_TARGET` | Vite dev proxy target |

### 5.4 Certificates

The main root compose file expects local cert files in `nginx/certs/`:

```bash
sh nginx/certs/generate.sh
docker compose up --build
```

This is different from `Server/docker-compose.yml`, which contains a legacy `certs` service that generates self-signed certificates automatically.

### 5.5 Legacy Docker notes

`Server/docker-compose.yml` is an older backend-focused setup and no longer reflects the main architecture.

Differences from the current root setup:

- no PostgreSQL service
- no root-level React `Client/` integration
- includes a `certs` helper service
- assumes a client build path that no longer matches the root project layout

Also note:

- `Server/db` is mounted into Postgres as `/docker-entrypoint-initdb.d`, but the directory is currently empty
- schema creation is handled by Flyway, not by SQL init scripts in that folder

---

## 6. Full System Flow

### 6.1 Standard authenticated request cycle

```text
User action in browser
  -> React component/hook updates local state
  -> service layer calls Axios client
  -> Axios adds JWT from sessionStorage
  -> nginx forwards /api request to Spring Boot
  -> Spring Security + JwtFilter validate token
  -> controller calls use-case/application service
  -> application service reads/writes PostgreSQL through repository adapters
  -> response serialized as JSON
  -> frontend updates UI state
```

### 6.2 Chat message cycle

```text
User sends message
  -> frontend creates chat if needed
  -> POST /api/chats/messages
  -> backend validates ownership and content
  -> backend stores user message
  -> placeholder AI adapter generates response text
  -> backend stores AI message
  -> frontend appends response to chat history
```

### 6.3 CV flow

```text
User completes personal info
  -> PUT /api/cv/personal-info
  -> backend marks profile complete
  -> user opens CV builder
  -> frontend loads templates + personal info
  -> user selects sections/items/template
  -> POST or PUT /api/cv
  -> backend stores CV config and linked section data
  -> frontend can print the preview via browser print dialog
```

---

## 7. Developer Guide

### 7.1 Run locally without Docker

#### Backend

```bash
cd Server
./mvnw spring-boot:run
```

Default backend port:

```text
http://localhost:9090
```

#### Frontend

```bash
cd Client
npm ci
npm run dev
```

Default frontend port:

```text
http://localhost:5173
```

Vite proxies `/api` to the backend by default.

### 7.2 Run with Docker

Generate certificates first:

```bash
sh nginx/certs/generate.sh
```

Then run:

```bash
docker compose up --build
```

Primary entrypoint:

```text
https://localhost
```

### 7.3 Debugging tips

#### Backend

- check JWT and security flow first when seeing `403`
- inspect `GlobalExceptionHandler` for actual status mapping
- inspect `ProfileCompletionInterceptor` when CV routes return `410`
- check Flyway migrations under `Server/src/main/resources/db/migration`
- catalog sync issues usually come from:
  - unreachable StackExchange API
  - missing local positions JSON file

#### Frontend

- auth state lives in `sessionStorage`, not `localStorage`
- `handleResponseError()` may silently redirect on `401`, `403`, or `410`
- if direct Vite requests fail, check `VITE_BACKEND_TARGET`
- map data problems are frontend-data issues, not backend API issues

#### Docker

- if nginx starts but pages fail, confirm cert files exist in `nginx/certs`
- if backend cannot connect to DB, verify `SPRING_DATASOURCE_*` environment variables
- if frontend works only through Vite and not as a static build, that is expected with the current nginx config because `/` proxies to `client-dev`

---

## 8. Legacy Documentation Differences and Deprecated Assumptions

### README.md

Useful as historical context, but outdated in several ways:

- It describes the product mainly as a university information chatbot.
- The current codebase is broader: auth, personal info, CV builder, feedback, catalogs, and chat.
- It implies a production-like nginx static-serving flow, but the current nginx config routes `/` to `client-dev`.
- It does not reflect the PostgreSQL-first root compose architecture accurately enough.

### API_DOC.md

Partially useful, but incomplete and outdated:

- It does not document the profile-completion `410 Gone` flow.
- It does not reflect the catalog endpoints comprehensively.
- It does not call out that `/api/cv/skills` and `/api/cv/positions` are legacy alternate endpoints while the current frontend uses `/api/skills` and `/api/positions`.
- It does not mention that missing/invalid JWT commonly returns `403`, not only `401`.

### DTOs.md

Still useful for auth DTO names, but it lags behind the live frontend/backend integration:

- frontend `UserData` expects an `id`, but JWT payloads do not currently include one
- backend `PersonalInfoResponse` is the authoritative response shape for profile completion

### externalAPI.md

Outdated:

- it lists StackExchange tags for skills, which is still directionally correct
- it lists a positions endpoint that is no longer the runtime source
- the current `PositionsApiClient` reads positions from a local JSON file under `Server/json/positions.json`

### Google OAuth expectation

Legacy and UI hints suggest a full Google login flow, but the current code is incomplete:

- frontend has `/google/callback`
- backend has `GoogleOAuthAdapter`
- backend does not expose a callback controller that turns a Google code into a uniAI JWT

This feature should be documented as partial, not complete.

---

## 9. Current Limitations

- Chat responses are mocked, not generated by a real AI provider
- Google OAuth is not fully wired end to end
- Frontend map data is static and separate from backend catalog data
- The current nginx config favors the Vite dev server over serving the built frontend volume
- Several security-sensitive defaults exist in `application.properties` and should be overridden in real environments

