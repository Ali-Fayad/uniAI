# uniAI — API Reference (server)

This document lists all HTTP endpoints implemented in the `Server` module with their request and response DTOs. Paths are relative to the server base (e.g. `http://host:9090`).

Authentication: protected endpoints require a valid JWT. The project uses an authentication filter, so controllers extract the authenticated user from the security context — supply `Authorization: Bearer <token>` for protected endpoints.

---

## AuthController (/api/auth)

### POST /api/auth/signup
- Request: `SignUpDto` (`com.uniai.dto.auth.SignUpDto`)
  - username: string (2..50)
  - firstName: string (optional)
  - lastName: string (optional)
  - email: string (2..100)
  - password: string (2..100)
- Response: `{ token: string }` (TokenResponse)
- Notes: If user is created but not verified, a verification code is sent and a `VerificationNeededException` is thrown.

### POST /api/auth/signin
- Request: `SignInDto` (`com.uniai.dto.auth.SignInDto`)
  - email: string
  - password: string
- Response: `{ token: string }` (TokenResponse)
- Notes: If 2FA is enabled the server will send a TWO_FACT_AUTH code and throw a `VerificationNeededException`.

### POST /api/auth/verify
- Request: `VerifyDto` (`com.uniai.dto.auth.VerifyDto`)
  - email: string
  - verificationCode: string
- Response: `{ token: string }` (TokenResponse)
- Notes: Verifies a VERIFY code and returns a JWT.

### POST /api/auth/2fa/verify
- Request: `VerifyDto`
- Response: `{ token: string }`
- Notes: Verifies a TWO_FACT_AUTH code and returns a JWT.

### POST /api/auth/forget-password
- Request: `EmailRequestDto` (`com.uniai.dto.user.EmailRequestDto`)
  - email: string
- Response: `{ message: string }` (MessageResponse)
- Notes: Sends a CHANGE_PASSWORD verification code when the email exists.

### POST /api/auth/forget-password/confirm
- Request: `RequestPasswordDto` (`com.uniai.dto.auth.RequestPasswordDto`)
  - email: string
  - verificationCode: string
  - newPassword: string
- Response: `{ token: string }` (TokenResponse)
- Notes: Verifies CHANGE_PASSWORD and returns a new JWT.

### POST /api/auth/google/url
- Request: `GoogleAuthUrlRequestDto` (`com.uniai.dto.auth.GoogleAuthUrlRequestDto`) (optional body)
  - redirectUri: string (optional)
  - state: string (optional)
- Response: `{ url: string }` (UrlResponse)
- Notes: Returns a Google OAuth2 authorization URL.

---

## UserController (/api/users/*)
All endpoints below require authentication (JWT).

### GET /api/users/me
- Request: none (Authorization header required)
- Response: `AuthenticationResponseDto` (`com.uniai.dto.auth.AuthenticationResponseDto`)
  - username, firstName, lastName, email, isVerified, isTwoFacAuth

### PUT /api/users/me
- Request: `UpdateUserDto` (`com.uniai.dto.user.UpdateUserDto`)
  - username: string (optional)
  - firstName: string (optional)
  - lastName: string (optional)
  - enableTwoFactor: Boolean (optional)
- Response: `AuthenticationResponseDto`

### DELETE /api/users/me
- Request: `DeleteAccountDto` (`com.uniai.dto.user.DeleteAccountDto`)
  - password: string
- Response: 204 No Content

### POST /api/users/change-password
- Request: `ChangePasswordDto` (`com.uniai.dto.user.ChangePasswordDto`)
  - currentPassword: string
  - newPassword: string (min 8)
- Response: 200 OK (empty body)

---

## ChatController (/api/chats)
All endpoints require authentication (JWT).

### POST /api/chats
- Request: none
- Response: `ChatCreationResponseDto` (`com.uniai.dto.chat.ChatCreationResponseDto`)
  - chatId: number

### POST /api/chats/messages
- Request: `SendMessageDto` (`com.uniai.dto.chat.SendMessageDto`)
  - chatId: number (required)
  - content: string (required)
- Response: `MessageResponseDto` (`com.uniai.dto.auth.MessageResponseDto`)
  - messageId, chatId, senderId (0 = AI or user id), content, timestamp

### GET /api/chats
- Request: none
- Response: `List<Chat>` (model `com.uniai.model.Chat`)

### GET /api/chats/{chatId}/messages
- Request: path param chatId
- Response: `List<MessageResponseDto>`

### DELETE /api/chats/{chatId}
- Request: path param chatId
- Response: `{ message: string }` (MessageResponse)

### DELETE /api/chats
- Request: none
- Response: `{ message: string }` (MessageResponse)

---

## DTO quick reference (request / response shapes)
- `SignUpDto` — username, firstName, lastName, email, password
- `SignInDto` — email, password
- `VerifyDto` — email, verificationCode
- `RequestPasswordDto` — email, verificationCode, newPassword
- `EmailRequestDto` — email
- `AuthenticationResponseDto` — username, firstName, lastName, email, isVerified, isTwoFacAuth
- `ChatCreationResponseDto` — chatId
- `SendMessageDto` — chatId, content
- `MessageResponseDto` — messageId, chatId, senderId, content, timestamp
- `UpdateUserDto` — username, firstName, lastName, enableTwoFactor
- `ChangePasswordDto` — currentPassword, newPassword
- `DeleteAccountDto` — password

---

## Notes & conventions
- Protected endpoints obtain the authenticated user's email from the security context; controllers no longer expect `Authorization` headers directly in method signatures.
- All email/verification flows use `com.uniai.domain.VerificationCodeType` with types: `VERIFY`, `TWO_FACT_AUTH`, `CHANGE_PASSWORD`.
- Use `application.properties` for message templates and email settings (see `app.email.*`).

If you want, I can:
- Generate example JSON request/response examples for each endpoint.
- Produce an OpenAPI (Swagger) YAML/JSON from the controllers and DTOs.
- Add a minimal Postman collection file.

What would you like next? (examples / OpenAPI / Postman)
