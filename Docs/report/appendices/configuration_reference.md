# Configuration Reference

Configuration sources: root `docker-compose.yml` and `Server/src/main/resources/application.properties`.

| Group | Examples | Handling |
|---|---|---|
| Database | `SPRING_DATASOURCE_*`, `POSTGRES_*` | do not publish password values |
| AI | `AI_PROVIDER`, `GEMINI_API_KEY`, `GROQ_API_KEY`, `OLLAMA_*` | keys are secrets |
| JWT | `JWT_SECRET`, expiration | signing secret is secret |
| Mail | `MAIL_USERNAME`, `MAIL_PASSWORD` | credentials are secrets |
| URL/origins | `APP_BASE_URL`, `CORS_ALLOWED_ORIGINS` | environment-specific |

