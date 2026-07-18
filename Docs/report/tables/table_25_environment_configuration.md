# Table 25 — Environment Configuration
Purpose: safe configuration reference.
| Variable family | Purpose | Secret? |
|---|---|---|
| `POSTGRES_*` | database connection/container | password is secret |
| `JWT_SECRET`, `JWT_EXPIRATION_MS` | token signing/lifetime | secret/signing value |
| `MAIL_*`, `EMAIL_*` | mail delivery | credentials secret |
| `AI_*`, `GEMINI_*`, `GROQ_*`, `OLLAMA_*` | provider selection/configuration | API keys secret |
| `APP_BASE_URL`, `CORS_ALLOWED_ORIGINS` | runtime URLs/origins | not generally secret |
Source evidence: `application.properties`, `docker-compose.yml`. Notes: never commit actual values. Suggested chapter: Deployment appendix. Last verification: 2026-07-18.
