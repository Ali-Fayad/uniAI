# Server DTOs Reference

This document lists the DTOs used by the server API. Keep the frontend type definitions in `Client/src/types/dto.ts` aligned with these shapes.

## Auth DTOs

- SignUpDto
  - username: String
  - firstName: String
  - lastName: String
  - email: String
  - password: String

- SignInDto
  - email: String
  - password: String

- VerifyDto
  - email: String
  - verificationCode: String

- RequestPasswordDto
  - email: String
  - verificationCode: String
  - newPassword: String

- EmailRequestDto
  - email: String

- GoogleAuthUrlRequestDto
  - redirectUri: String (optional)
  - state: String (optional)

## Responses

- TokenResponse
  - token: String

- MessageResponse
  - message: String

- UrlResponse
  - url: String

## AuthenticationResponseDto (returned as user data inside JWT claims)

- username: String
- firstName: String
- lastName: String
- email: String
- isVerified: boolean
- isTwoFacAuth: boolean


Notes:
- The server sends a `TokenResponse` which contains only the token string. The frontend should extract user information from the token when needed.
- Verification-related requests use the field name `verificationCode` (not `code`). Update frontend DTOs and request payloads accordingly.

