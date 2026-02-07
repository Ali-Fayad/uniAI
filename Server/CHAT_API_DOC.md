# CHAT API Documentation

This document describes the chat-related HTTP endpoints exposed by `ChatController` (`/api/chats`). All endpoints require authentication via JWT (send `Authorization: Bearer <token>`).

---

**Common DTOs / Models**

- `ChatCreationResponseDto`:

  - `chatId` (Long)

- `SendMessageDto` (request for sending a message):

  - `chatId` (Long) — required
  - `content` (String) — required, non-empty, max 5000 chars

- `MessageResponseDto` (message returned to client):

  - `messageId` (Long)
  - `chatId` (Long)
  - `senderId` (Long) — `0` means AI, otherwise user's id
  - `content` (String)
  - `timestamp` (ISO datetime)

- `Chat` (returned by list chats):
  - `id` (Long)
  - `user` (object) — user owner reference
  - `title` (String | null) — null until first message
  - `createdAt` (ISO datetime)
  - `updatedAt` (ISO datetime)

---

**Endpoints**

1. Create chat

- Method: POST
- Path: `/api/chats`
- Auth: required
- Request body: none
- Success Response:
  - Status: `201 Created`
  - Body (application/json):
    ```json
    {
      "chatId": 123
    }
    ```
- Possible error responses:
  - `401 Unauthorized` — invalid/missing token (handled by authentication layer)
  - `404 Not Found` — `EmailNotFoundException` if authenticated email not found
  - `500 Internal Server Error` — unexpected server errors

---

2. Send message (user -> chat -> AI responds)

- Method: POST
- Path: `/api/chats/messages`
- Auth: required
- Request body (application/json): `SendMessageDto`
  ```json
  {
    "chatId": 123,
    "content": "Hello AI"
  }
  ```
- Validation rules (server-side):
  - `chatId` must not be null
  - `content` must be non-empty and <= 5000 chars
- Success Response:
  - Status: `200 OK`
  - Body: `MessageResponseDto` (the AI reply only). Example:
    ```json
    {
      "messageId": 456,
      "chatId": 123,
      "senderId": 0,
      "content": "AI response to: Hello AI",
      "timestamp": "2025-12-17T12:34:56"
    }
    ```
- Possible error responses:
  - `400 Bad Request` — validation failures (invalid/missing fields). The service may throw `InvalidMessageException` or validation framework messages.
  - `401 Unauthorized` — invalid/missing token
  - `403 Forbidden` — user does not own the target chat (service throws `UnauthorizedAccessException`)
  - `404 Not Found` — `ChatNotFoundException` when chat id does not exist or `EmailNotFoundException` if user not found
  - `500 Internal Server Error` — unexpected server errors or AI integration failures

---

3. Get user's chats (list)

- Method: GET
- Path: `/api/chats`
- Auth: required
- Request body: none
- Success Response:
  - Status: `200 OK`
  - Body: JSON array of `Chat` objects (ordered by `updatedAt` desc). Example:
    ```json
    [
      {
        "id": 123,
        "user": { "id": 11, "username": "alice", "email": "alice@example.com" },
        "title": "My question...",
        "createdAt": "2025-12-17T10:00:00",
        "updatedAt": "2025-12-17T12:00:00"
      }
    ]
    ```
- Possible error responses:
  - `401 Unauthorized` — invalid/missing token
  - `404 Not Found` — `EmailNotFoundException` if user not found
  - `500 Internal Server Error`

---

4. Get messages for a chat (history)

- Method: GET
- Path: `/api/chats/{chatId}/messages`
- Auth: required
- Path params:
  - `chatId` (Long)
- Success Response:
  - Status: `200 OK`
  - Body: JSON array of `MessageResponseDto` ordered by `timestamp` ascending. Example:
    ```json
    [
      {
        "messageId": 1,
        "chatId": 123,
        "senderId": 11,
        "content": "Hi",
        "timestamp": "2025-12-17T10:01:00"
      },
      {
        "messageId": 2,
        "chatId": 123,
        "senderId": 0,
        "content": "AI response...",
        "timestamp": "2025-12-17T10:01:01"
      }
    ]
    ```
- Possible error responses:
  - `401 Unauthorized` — invalid/missing token
  - `403 Forbidden` — user does not own this chat
  - `404 Not Found` — `ChatNotFoundException` if chat doesn't exist
  - `500 Internal Server Error`

---

5. Delete a chat (single)

- Method: DELETE
- Path: `/api/chats/{chatId}`
- Auth: required
- Path params: `chatId` (Long)
- Success Response:
  - Status: `200 OK`
  - Body: simple message object:
    ```json
    { "message": "Chat deleted successfully" }
    ```
- Possible error responses:
  - `401 Unauthorized`
  - `403 Forbidden` — not chat owner
  - `404 Not Found` — `ChatNotFoundException`
  - `500 Internal Server Error`

---

6. Delete all chats for the user

- Method: DELETE
- Path: `/api/chats`
- Auth: required
- Request body: none
- Success Response:
  - Status: `200 OK`
  - Body:
    ```json
    { "message": "All chats deleted successfully" }
    ```
- Possible error responses:
  - `401 Unauthorized`
  - `404 Not Found` — `EmailNotFoundException` if user not found
  - `500 Internal Server Error`

---

**Notes / Implementation details**

- Authentication: controller uses `JwtFacade.getAuthenticatedUserEmail()` to resolve the authenticated user email; include `Authorization: Bearer <token>` on requests.
- Error mapping: some exceptions are mapped in `exeptionhandler/GlobalExceptionHandler` (e.g. `ChatNotFoundException -> 404`, `EmailNotFoundException -> 404`, `InvalidTokenException -> 401`). Other service-thrown exceptions (`InvalidMessageException`, `UnauthorizedAccessException`) are not explicitly mapped in `GlobalExceptionHandler` in the current codebase; they will propagate to the global error handler. Consider adding explicit mappings for `InvalidMessageException -> 400` and `UnauthorizedAccessException -> 403` for clearer API behavior.
- Validation: `@Valid` is used on `SendMessageDto`. DTO-level annotations enforce non-null/blank constraints; service layer also validates and enforces message length.

---

If you want, I can:

- Add example `curl` commands for each endpoint.
- Add OpenAPI/Swagger annotations to `ChatController` for automated docs.
- Add explicit exception handlers for `InvalidMessageException` and `UnauthorizedAccessException` to normalize responses.

File: [Server/CHAT_API_DOC.md](Server/CHAT_API_DOC.md)
