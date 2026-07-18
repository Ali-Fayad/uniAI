# Authentication Endpoints

Source: `Server/src/main/java/com/uniai/user/presentation/controller/AuthController.java`.

| Method/path | Request | Response/status | Purpose |
|---|---|---|---|
| POST `/api/auth/signup` | `SignUpCommand` | message, 202 | register and initiate verification |
| POST `/api/auth/signin` | `SignInCommand` | token, 200 | authenticate |
| POST `/api/auth/verify` | `VerifyCommand` | token, 200 | verify email |
| POST `/api/auth/2fa/verify` | `VerifyCommand` | token, 200 | verify 2FA code |
| POST `/api/auth/verify/resend` | `EmailRequestCommand` | message | resend code |
| POST `/api/auth/forget-password` | email record | message | request reset |
| POST `/api/auth/forget-password/confirm` | `RequestPasswordCommand` | token | reset password |
| POST `/api/auth/google/url` | optional redirect/state | URL | create Google authorization URL only |
| GET `/api/auth/check-email` | query email | availability | check email |
| GET `/api/auth/check-username` | query username | availability | check username |

All are public. The Google endpoint does not prove completion of the OAuth callback flow.

