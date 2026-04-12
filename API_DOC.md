# uniAI API Documentation

## Overview

Base URL: /api

This document lists all REST endpoints provided by the uniAI backend. Authentication uses JWT tokens issued by the auth endpoints. The Security configuration permits all `/api/auth/**` endpoints and requires a valid JWT for all other endpoints.

## Authentication

- Flow: `POST /api/auth/signin` or `POST /api/auth/signup` returns a JWT token. Include the token on protected requests using the `Authorization: Bearer <token>` header.
- Public endpoints: All `/api/auth/**` routes.
- Protected endpoints: All other `/api/**` routes (JWT required). No role-based restrictions detected in the codebase.
- Health endpoint: `GET /actuator/health` is public for runtime/compose checks.

## Error responses

Global exception handler maps domain exceptions to plain-text bodies and common HTTP statuses:

- 400 Bad Request — validation errors, `InvalidEmailOrPassword`, `InvalidVerificationCode`, `InvalidMessageException`, `JsonProcessingException`, `FeedbackNotValidException`.
- 401 Unauthorized — invalid token, `InvalidTokenException`, `UnauthorizedAccessException`, `GoogleAuthException`.
- 202 Accepted — `VerificationNeededException` (used by sign-in flow when verification required).
- 404 Not Found — `EmailNotFoundException`, `ChatNotFoundException`, `CVNotFoundException`, `SectionNotFoundException`, `UniversityNotFoundException`.
- 409 Conflict — `AlreadyExistsException`.
- 500 Internal Server Error — any other uncaught exceptions may return 500.

For validation errors triggered by `@Valid` the response will be 400 with validation message(s).

---

## Endpoints

### User Module

#### `POST /api/auth/signup`

**Description:** Register a new user and receive a JWT if registration completes.

**Authentication:** Not required

**Request:**
- **Request Body:**
```json
{
  "username": "jdoe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "secret"
}
```
Field	Type	Required	Description
username	string	Yes	User handle (2-50 chars)
firstName	string	No	
lastName	string	No	
email	string	Yes	(2-100 chars)
password	string	Yes	(2-100 chars)

**Response:**

200 OK - Success:

```json
{
  "token": "<jwt>"
}
```
Field	Type	Description
token	string	JWT token

Errors:
- 400 Bad Request: validation errors or InvalidEmailOrPassword
- 409 Conflict: `Email already registered` or username conflict

---

#### `GET /api/auth/check-email?email=...`

**Description:** Check if an email is available for registration.

**Authentication:** Not required

**Query Parameters:**
- `email` (string, required)

**Response:**

200 OK:

```json
{
  "available": true,
  "message": "Email available"
}
```

When unavailable:

```json
{
  "available": false,
  "message": "Email already in use"
}
```

---

#### `GET /api/auth/check-username?username=...`

**Description:** Check if a username is available for registration.

**Authentication:** Not required

**Query Parameters:**
- `username` (string, required)

**Response:**

200 OK:

```json
{
  "available": true,
  "message": "Username available"
}
```

When unavailable:

```json
{
  "available": false,
  "message": "Username already in use"
}
```

---

#### `POST /api/auth/signin`

**Description:** Authenticate a user and return a JWT.

**Authentication:** Not required

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "secret"
}
```
Field	Type	Required	Description
email	string	Yes	(2-100 chars)
password	string	Yes	(2-100 chars)

**Response:**

200 OK:

```json
{ "token": "<jwt>" }
```

Errors:
- 400 Bad Request: InvalidEmailOrPassword
- 202 Accepted: VerificationNeededException (account needs verification)

---

#### `POST /api/auth/verify`

**Description:** Verify email using OTP code and receive a JWT.

**Authentication:** Not required

**Request Body:**
```json
{ "email": "john@example.com", "verificationCode": "123456" }
```

Field	Type	Required	Description
email	string	No	Email to verify
verificationCode	string	No	OTP code

**Response:**

200 OK:

```json
{ "token": "<jwt>" }
```

Errors:
- 400 Bad Request: InvalidVerificationCode
- 401 Unauthorized: InvalidTokenException (if token flow used elsewhere)

---

#### `POST /api/auth/2fa/verify`

**Description:** Verify two-factor code and receive a JWT.

**Authentication:** Not required

**Request Body:** same as `/verify` (VerifyCommand)

**Response:** 200 OK `{ "token": "<jwt>" }`

Errors: 400 Bad Request for invalid code

---

#### `POST /api/auth/forget-password`

**Description:** Request a password reset (sends verification code to email).

**Authentication:** Not required

**Request Body:**
```json
{ "email": "john@example.com" }
```

Response:

200 OK:

```json
{ "message": "Verification code sent" }
```

Errors: 404 EmailNotFoundException

---

#### `POST /api/auth/forget-password/confirm`

**Description:** Confirm password reset using OTP and set new password; returns JWT.

**Authentication:** Not required

**Request Body (RequestPasswordCommand):**
```json
{
  "email": "john@example.com",
  "verificationCode": "123456",
  "newPassword": "newSecret"
}
```

Response:

200 OK - `{ "token": "<jwt>" }`

Errors: 400 InvalidVerificationCode, 404 EmailNotFoundException

---

#### `POST /api/auth/google/url`

**Description:** Get OAuth2 Google authorization URL (accepts optional redirect/state).

**Authentication:** Not required

**Request Body (optional):**
```json
{ "redirectUri": "https://app.example.com/callback", "state": "abc" }
```

**Response:**

200 OK:

```json
{ "url": "https://accounts.google.com/...?state=..." }
```

Errors: 401 GoogleAuthException

---

### Profile / User

#### `GET /api/users/me`

**Description:** Return the authenticated user's profile.

**Authentication:** JWT required

**Request:** none

**Response:** 200 OK - `AuthResponseDto`

```json
{
  "username": "jdoe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "isVerified": false,
  "isTwoFacAuth": false
}
```

Field	Type	Description
username	string	User handle
firstName	string
lastName	string
email	string
isVerified	boolean	Email verified flag
isTwoFacAuth	boolean	2FA enabled flag

Errors: 401 Unauthorized

---

#### `PUT /api/users/me`

**Description:** Update authenticated user's profile.

**Authentication:** JWT required

**Request Body (UpdateUserCommand):**
```json
{
  "username": "jdoe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "enableTwoFactor": true
}
```

Fields: `username` (2-50), `firstName` (max 100), `lastName` (max 100), `email` (valid email, max 100), `enableTwoFactor` (boolean)

**Response:** 200 OK - `AuthResponseDto` (same schema as `GET /api/users/me`)

Errors: 400 validation, 401 Unauthorized, 409 Conflict (`Email already registered`)

---

#### `DELETE /api/users/me`

**Description:** Delete authenticated user's account (requires current password in body).

**Authentication:** JWT required

**Request Body (DeleteUserCommand):**
```json
{ "password": "currentPassword" }
```

**Response:** 204 No Content

Errors: 400 InvalidEmailOrPassword, 401 Unauthorized

---

#### `POST /api/users/change-password`

**Description:** Change password for authenticated user.

**Authentication:** JWT required

**Request Body (ChangePasswordCommand):**
```json
{
  "currentPassword": "oldPwd",
  "newPassword": "newSecret"
}
```

Constraints: `newPassword` min length 8

**Response:** 200 OK (empty body)

Errors: 400 validation, 401 Unauthorized

---

### Feedback Module

#### `POST /api/feedback`

**Description:** Submit feedback for the authenticated user.

**Authentication:** JWT required

**Request Body:**
```json
{
  "rating": 5,
  "content": "Great experience overall"
}
```

Fields:
- `rating` (integer, optional, 1..5)
- `content` (string, required, non-empty)

**Response:** 200 OK (empty body)

Errors:
- 400 Bad Request (`FeedbackNotValidException`)
- 403 Forbidden (`Authentication required`) when token is missing or invalid

---

### Catalog Module

#### `GET /api/skills`

**Description:** Return skills from local database (synced from external API).

**Authentication:** JWT required

**Query Parameters:**
- `search` (string, optional) - case-insensitive partial match by skill name

**Response:** 200 OK

```json
[
  { "id": 1, "name": "java", "category": "Programming" },
  { "id": 2, "name": "ux-design", "category": "Design" }
]
```

#### `GET /api/languages`

**Description:** Return static seeded languages from local database.

**Authentication:** JWT required

**Query Parameters:**
- `search` (string, optional) - case-insensitive partial match by `name` or `nativeName`

**Response:** 200 OK

```json
[
  { "id": 1, "name": "English", "code": "en", "nativeName": "English" },
  { "id": 2, "name": "Arabic", "code": "ar", "nativeName": "العربية" }
]
```

#### `GET /api/positions`

**Description:** Return positions from local database (synced from external API on startup and daily schedule).

**Authentication:** JWT required

**Query Parameters:**
- `search` (string, optional) - case-insensitive partial match by position name

**Response:** 200 OK

```json
[
  { "id": 1, "name": "Software Engineer" },
  { "id": 2, "name": "Product Manager" }
]
```

#### `GET /api/universities`

**Description:** Return universities from local database.

**Authentication:** JWT required

**Query Parameters:**
- `search` (string, optional) - case-insensitive partial match by university name

**Response:** 200 OK

```json
[
  { "id": 1, "name": "American University of Beirut", "acronym": "AUB", "nameAr": "الجامعة الأمريكية في بيروت" },
  { "id": 2, "name": "Lebanese University", "acronym": "LU", "nameAr": "الجامعة اللبنانية" }
]
```

---

### Chat Module

All chat endpoints require JWT authentication.

#### `POST /api/chats`

**Description:** Create a new chat for the authenticated user.

**Authentication:** JWT required

**Request:** none

**Response:** 201 Created - `ChatCreationResponseDto`

```json
{ "chatId": 123 }
```

Errors: 401 Unauthorized

---

#### `POST /api/chats/messages`

**Description:** Send a message in a chat.

**Authentication:** JWT required

**Request Body (SendMessageCommand):**
```json
{
  "chatId": 123,
  "content": "Hello"
}
```

Field	Type	Required	Description
chatId	integer	Yes	Chat identifier
content	string	Yes	Message body (not blank)

**Response:** 200 OK - `MessageResponseDto`

```json
{
  "messageId": 1,
  "chatId": 123,
  "senderId": 10,
  "content": "Hello",
  "timestamp": "2026-03-23T12:00:00"
}
```

Errors: 400 InvalidMessageException, 401 Unauthorized, 404 ChatNotFoundException

---

#### `GET /api/chats`

**Description:** List chats for the authenticated user.

**Authentication:** JWT required

**Response:** 200 OK - list of `Chat` domain objects (server returns domain `Chat` model)

Errors: 401 Unauthorized

---

#### `GET /api/chats/{chatId}/messages`

**Description:** Get messages for a chat.

**Authentication:** JWT required

**Path Parameters:**
- `chatId` (integer, required) - id of the chat

**Response:** 200 OK - `List<MessageResponseDto>`

Errors: 401 Unauthorized, 404 ChatNotFoundException

---

#### `DELETE /api/chats/{chatId}`

**Description:** Delete a single chat belonging to the authenticated user.

**Authentication:** JWT required

**Path Parameters:** `chatId` (integer, required)

**Response:** 200 OK

```json
{ "message": "Chat deleted successfully" }
```

Errors: 401 Unauthorized, 404 ChatNotFoundException

---

#### `DELETE /api/chats`

**Description:** Delete all chats belonging to the authenticated user.

**Authentication:** JWT required

**Response:** 200 OK

```json
{ "message": "All chats deleted successfully" }
```

Errors: 401 Unauthorized

---

### CV Builder Module

All CV endpoints require JWT authentication.

#### `GET /api/cv/templates`

**Description:** List all active CV templates available for user selection.

**Authentication:** JWT required

**Response:** 200 OK - `List<CVTemplateResponse>`

`CVTemplateResponse` fields:

- `id` (number)
- `name` (string)
- `description` (string, optional)
- `thumbnailUrl` (string, optional)
- `componentName` (string)
- `isActive` (boolean)

---

#### `GET /api/cv/templates/{id}`

**Description:** Get details for a specific CV template.

**Authentication:** JWT required

**Path Parameters:** `id` (integer, required)

**Response:** 200 OK - `CVTemplateResponse`

Errors: 404 CVTemplateNotFoundException, 401 Unauthorized

---

#### `GET /api/cv`

**Description:** List CVs for the authenticated user.

**Authentication:** JWT required

**Response:** 200 OK - `List<CVResponse>` (see `CVResponse` schema below)

---

#### `GET /api/cv/{id}`

**Description:** Get a single CV by id for the authenticated user.

**Authentication:** JWT required

**Path Parameters:** `id` (integer, required)

**Response:** 200 OK - `CVResponse`

Errors: 404 CVNotFoundException, 401 Unauthorized

---

#### `POST /api/cv`

**Description:** Create a new CV (shell) for the authenticated user.

**Authentication:** JWT required

**Request Body (CreateCVCommand):**
```json
{
  "cvName": "My CV",
  "templateId": 1,
  "sectionsOrder": ["education", "experience", "skills", "languages", "projects", "certificates"],
  "isDefault": false
}
```

Field	Type	Required	Description
cvName	string	Yes	Name for the CV
templateId	number	No	Selected template id (defaults to Modern when omitted)
sectionsOrder	array[string]	No	Ordered list of enabled sections
isDefault	boolean	No	Make this CV the user's default

**Response:** 201 Created - `CVResponse`

`CVResponse` now includes:

- `templateId` (number, nullable)
- `templateName` (string, nullable)
- `templateComponentName` (string, nullable)
- `sectionsOrder` (array[string])
- existing nested section/personal-info payloads remain unchanged

Errors: 400 validation, 401 Unauthorized

---

#### `PUT /api/cv/{id}`

**Description:** Update CV metadata.

**Authentication:** JWT required

**Request Body (UpdateCVCommand):** fields: `cvName`, `templateId`, `template` (legacy compatibility), `sectionsOrder`, `isDefault` (all optional)

**Response:** 200 OK - `CVResponse`

Errors: 404 CVNotFoundException, 401 Unauthorized

---

#### `DELETE /api/cv/{id}`

**Description:** Delete a CV belonging to the authenticated user.

**Authentication:** JWT required

**Response:** 204 No Content

Errors: 404 CVNotFoundException, 401 Unauthorized

---

Section CRUD (education, experience, skill, project, language, certificate)

Each `POST` with `/{cvId}/{section}` creates a section entry and returns 201 + the corresponding Response DTO. `PUT` on `/api/cv/{section}/{id}` updates and returns 200 + Response DTO. `DELETE` returns 204 No Content.

Schematics for common commands and responses:

- `AddEducationCommand` fields: `universityId` (Long), `degree` (string, required), `fieldOfStudy` (string, required), `startDate` (date, required), `endDate` (date, optional), `grade` (string), `description` (string)
- `EducationResponse`: id, cvId, universityId, degree, fieldOfStudy, startDate, endDate, grade, description

- `AddExperienceCommand` fields: `position` (string, required), `company` (string, required), `location` (string), `startDate` (date, required), `endDate` (date), `isCurrent` (boolean), `description` (string), `achievements` (array of strings)
- `ExperienceResponse`: id, cvId, position, company, location, startDate, endDate, isCurrent, description, achievements

- `AddSkillCommand` fields: `name` (string, required), `level` (string), `order` (integer)
- `SkillResponse`: id, cvId, name, level, order

- `AddProjectCommand` fields: `name` (string, required), `description` (string), `githubUrl` (string), `liveUrl` (string), `startDate` (date), `endDate` (date), `technologies` (array of strings)
- `ProjectResponse`: id, cvId, name, description, githubUrl, liveUrl, startDate, endDate, technologies

- `AddLanguageCommand` fields: `name` (string, required), `proficiency` (string)
- `LanguageResponse`: id, cvId, name, proficiency

- `AddCertificateCommand` fields: `name` (string, required), `issuer` (string), `date` (date), `credentialUrl` (string)
- `CertificateResponse`: id, cvId, name, issuer, date, credentialUrl

Common status codes for section operations:

- 201 Created (POST)
- 200 OK (PUT)
- 204 No Content (DELETE)
- 400 Bad Request (validation)
- 404 Not Found (SectionNotFoundException, CVNotFoundException)

---

#### Lookup endpoints

`GET /api/cv/skills` — returns `List<String>` of skill suggestions (200 OK, cached in backend)

`GET /api/cv/positions` — returns `List<String>` of position suggestions (200 OK, cached in backend)

`GET /api/cv/universities` — returns `List<UniversityResponse>`

`UniversityResponse` fields: id, name, nameAr, acronym, latitude, longitude, campusName, campusType

---

### Personal Info

#### `GET /api/cv/personal-info`

**Description:** Get the authenticated user's personal info used in CVs.

**Authentication:** JWT required

**Response:** 200 OK - `PersonalInfoResponse`

Fields:

- `userId` (number)
- `hasPersonalInfo` (boolean) — indicates whether any profile data exists
- `phone`, `address`, `linkedin`, `github`, `portfolio`, `summary`, `jobTitle`, `company` (string, optional)
- `education` (array, optional):
  - `id` (string)
  - `universityId` (number, optional)
  - `universityName` (string)
- `skills` (array, optional):
  - `id` (string)
  - `skillId` (string)
  - `name` (string)
- `experience` (array, optional):
  - `id` (string)
  - `positionId` (string)
  - `position` (string)
  - `company` (string)

---

#### `PUT /api/cv/personal-info`

**Description:** Update the authenticated user's personal info.

**Authentication:** JWT required

**Request Body (UpdatePersonalInfoCommand):**

- Scalars (all optional): `phone`, `address`, `linkedin`, `github`, `portfolio`, `summary`, `jobTitle`, `company`
- List sections (all optional):
  - `education`: array of `{ id, universityId, universityName }`
  - `skills`: array of `{ id, skillId, name }`
  - `experience`: array of `{ id, positionId, position, company }`

If a list section is omitted in the payload, the existing stored value is kept.

**Response:** 200 OK - updated `PersonalInfoResponse`

Errors: 401 Unauthorized

---

### Feedback Module

#### `POST /api/feedback`

**Description:** Submit user feedback (publicly accessible to authenticated or anonymous callers — controller does not check auth but security requires JWT on non-auth routes; code uses no JWT here so this endpoint still requires JWT per SecurityConfig).

**Authentication:** JWT required

**Request Body (SubmitFeedbackCommand):**
```json
{ "email": "optional@example.com", "comment": "Feedback text" }
```

**Response:** 200 OK (empty body)

Errors: 400 FeedbackNotValidException

---

## Notes and assumptions

- All non-`/api/auth/**` endpoints require a valid JWT per `SecurityConfig` which installs `JwtFilter` and calls `anyRequest().authenticated()`.
- Role-based access was not found in the codebase; no `@PreAuthorize` or `@RolesAllowed` annotations were detected on controller methods.
- Error responses are plain text messages produced by `GlobalExceptionHandler`.
- Date/time formats: responses use Java types such as `LocalDate` and `LocalDateTime`. The JSON serializer used by the application will determine exact formatting (typically ISO-8601).

If you'd like, I can:

- Add example curl requests for the most common flows (signup/signin/use token).
- Generate OpenAPI (Swagger) YAML from these DTOs and controllers.
## CV Access Validation
All CV endpoints (except /api/cv/personal-info) return `410 Gone` if the user has not completed their profile.
