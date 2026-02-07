# uniAI Server — API Documentation

This document lists available API endpoints, request/response DTOs, expected success statuses, and error mappings.

---

## Overview

- Base path: `/api`
- Errors: exceptions from `GlobalExceptionHandler` are returned as a plain message string in the body.

## Common error statuses

- `400 Bad Request` — validation errors, `InvalidEmailOrPassword`, `InvalidVerificationCodeException`.
- `401 Unauthorized` — `InvalidTokenException`, `GoogleAuthException` (invalid/expired tokens or auth issues).
- `202 Accepted` — `VerificationNeededException` (action pending, e.g., verification code required).
- `404 Not Found` — `EmailNotFoundException`, `ChatNotFoundException`, `FeedbackNotValidException`.
- `409 Conflict` — `AlreadyExistsException`.

---

## AuthController — `/api/auth`

- POST `/api/auth/signup`

  - Request: `SignUpDto` { username, firstName, lastName, email, password }
  - Success: `200 OK` — `{ "token": "<jwt>" }`
  - Errors: `400`, `409` (already exists), `202` (verification needed)

- POST `/api/auth/signin`

  - Request: `SignInDto` { email, password }
  - Success: `200 OK` — `{ "token": "<jwt>" }`
  - Errors: `400`, `401`

- POST `/api/auth/verify`

  - Request: `VerifyDto` { email, verificationCode }
  - Success: `200 OK` — `{ "token": "<jwt>" }`
  - Errors: `400`, `401`

- POST `/api/auth/2fa/verify`

  - Request: `VerifyDto` { email, verificationCode }
  - Success: `200 OK` — `{ "token": "<jwt>" }`
  - Errors: `400`, `401`

- POST `/api/auth/forget-password`

  - Request: `EmailRequestDto` { email }
  - Success: `200 OK` — `{ "message": "verification code sent" }`
  - Errors: `404`, `400`

- POST `/api/auth/forget-password/confirm`

  - Request: `RequestPasswordDto` { email, verificationCode, newPassword }
  - Success: `200 OK` — `{ "token": "<jwt>" }`
  - Errors: `400`, `404`

- POST `/api/auth/google/url`
  - Request (optional body): `GoogleAuthUrlRequestDto` { redirectUri?, state? }
  - Success: `200 OK` — `{ "url": "https://accounts.google..." }`
  - Errors: `400`, `401`

---

## UserController — `/api/users`

- GET `/api/users/me`

  - Auth required.
  - Success: `200 OK` — `AuthenticationResponseDto` {
    username, firstName, lastName, email, isVerified, isTwoFacAuth
    }
  - Errors: `401`

- PUT `/api/users/me`

  - Request: `UpdateUserDto` { username?, firstName?, lastName?, enableTwoFactor? }
  - Success: `200 OK` — `AuthenticationResponseDto` (updated)
  - Errors: `400`, `401`

- DELETE `/api/users/me`

  - Request: `DeleteAccountDto` { password }
  - Success: `204 No Content` — no body
  - Errors: `400`, `401`

- POST `/api/users/change-password`

  - Request: `ChangePasswordDto` { currentPassword, newPassword }
  - Success: `200 OK` — empty body
  - Errors: `400`, `401`

- POST `/api/users/feedback`
  - Request: `FeedbackRequest` { email, comment }
  - Success: `200 OK` — empty body
  - Errors: `404`, `400`

---

## ChatController — `/api/chats`

- POST `/api/chats`

  - Auth required.
  - Success: `201 Created` — `ChatCreationResponseDto` { chatId }
  - Errors: `400`, `401`

- POST `/api/chats/messages`

  - Request: `SendMessageDto` { chatId, content }
  - Success: `200 OK` — `MessageResponseDto` { messageId, chatId, senderId, content, timestamp }
  - Errors: `400`, `401`, `404`

- GET `/api/chats`

  - Success: `200 OK` — `List<Chat>` (model objects)
  - Errors: `401`

- GET `/api/chats/{chatId}/messages`

  - Success: `200 OK` — `List<MessageResponseDto>`
  - Errors: `401`, `404`

- DELETE `/api/chats/{chatId}`

  - Success: `200 OK` — `{ "message": "Chat deleted successfully" }`
  - Errors: `401`, `404`

- DELETE `/api/chats`
  - Success: `200 OK` — `{ "message": "All chats deleted successfully" }`
  - Errors: `401`

---

## DTO quick reference

- `SignUpDto`: `username`, `firstName`, `lastName`, `email`, `password`
- `SignInDto`: `email`, `password`
- `VerifyDto`: `email`, `verificationCode`
- `RequestPasswordDto`: `email`, `verificationCode`, `newPassword`
- `GoogleAuthUrlRequestDto`: `redirectUri?`, `state?`
- `EmailRequestDto`: `email`
- `AuthenticationResponseDto`: `username`, `firstName`, `lastName`, `email`, `isVerified`, `isTwoFacAuth`
- `UpdateUserDto`: `username?`, `firstName?`, `lastName?`, `enableTwoFactor?`
- `DeleteAccountDto`: `password`
- `ChangePasswordDto`: `currentPassword`, `newPassword`
- `FeedbackRequest`: `email`, `comment`
- `ChatCreationResponseDto`: `chatId`
- `SendMessageDto`: `chatId`, `content`
- `MessageResponseDto`: `messageId`, `chatId`, `senderId`, `content`, `timestamp`

---

## Notes

- Validation constraints use Jakarta Validation; invalid payloads return `400 Bad Request`.
- Authentication-protected endpoints return `401` on missing/invalid tokens.
- Global exceptions map to specific statuses (see "Common error statuses").

If you'd like, I can also:

- add example request/response JSON samples,
- convert this to OpenAPI/Swagger YAML,
- or add this markdown into the `docs/` folder.
