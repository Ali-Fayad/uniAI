# uniAI

uniAI is a full-stack university guidance platform. It combines authenticated user features, a CV Builder, a university map, feedback and administration with configurable AI-assisted chat and SQL-backed graduate-program retrieval.

## Principal features

- JWT authentication, email verification, password reset, and 2FA flows
- User profiles and CV Builder with templates and section management
- Persisted chat history, configurable AI providers, context budgeting, and conversation memory
- Graduate information retrieval for programmes, locations, academic structures, language, admissions, tuition, multi-filter search, and objective comparisons
- Searchable catalogs, frontend Leaflet university map, feedback, and ADMIN-protected administration

## Architecture

- Frontend: React 19, TypeScript, Vite, React Router, Axios, Leaflet
- Backend: Java 17, Spring Boot 4, Spring Security, JPA/JDBC, Flyway
- Database: PostgreSQL 17; one physical database with core and AI/knowledge logical domains
- Operations: Docker Compose with PostgreSQL, Spring Boot, Vite, nginx, and mounted local TLS certificates
- AI: configurable Gemini, Groq, Ollama, or placeholder adapter behind `AiServicePort`

See [technical documentation](Docs/TECHNICAL_DOCUMENTATION.md) and the [capstone report asset package](Docs/report/README.md).

## Project structure

```text
Client/       React + TypeScript + Vite SPA
Server/       Spring Boot backend, Flyway migrations, Maven wrapper
Docs/         existing engineering and research material
Docs/report/  code-grounded report assets, diagrams, tables, inventories
nginx/        reverse proxy and certificate tooling
docker-compose.yml
```

## Prerequisites

- Java 17
- Node.js/npm compatible with `Client/package-lock.json`
- Docker Desktop and Docker Compose v2 for the full stack
- PostgreSQL only when running the backend without Docker
- AI provider credentials only when using Gemini/Groq; Ollama requires a local server/model

## Environment setup

Create a local `.env` file for Docker. Do not commit it.

```env
POSTGRES_USER=uniai
POSTGRES_PASSWORD=replace-me
POSTGRES_DB=uniai
JWT_SECRET=replace-with-a-long-random-secret
MAIL_USERNAME=your-mail-account
MAIL_PASSWORD=your-mail-secret
APP_BASE_URL=https://localhost
AI_PROVIDER=placeholder
```

Additional optional AI variables are documented in `Server/src/main/resources/application.properties`: `GEMINI_API_KEY`, `GROQ_API_KEY`, provider URLs/models, and Ollama settings. Never publish real keys, passwords, tokens, or connection strings.

## Run with Docker

Generate local certificates once if needed:

```bash
sh nginx/certs/generate.sh
```

Start the stack from the repository root:

```bash
docker compose up --build
```

The compose configuration exposes nginx on ports 80/443, Spring Boot on 9090, and Vite on 5173. It includes development-oriented source mounts and a Vite service; production hardening is a separate concern.

Validate the compose syntax:

```bash
docker compose config
```

## Run locally

Backend:

```bash
cd Server
./mvnw spring-boot:run
```

Frontend:

```bash
cd Client
npm ci
npm run dev
```

For direct Vite development, configure `VITE_BACKEND_TARGET` or `VITE_API_URL` appropriately. The backend needs a reachable PostgreSQL datasource and required runtime environment values.

## Build and test

Backend:

```bash
cd Server
./mvnw -q -DskipTests compile
./mvnw -q test
```

Frontend:

```bash
cd Client
npm run lint
npm run build
```

The backend contains Testcontainers integration tests, so the full test suite needs a reachable Docker daemon. The frontend currently has lint/build scripts but no confirmed test script.

## Documentation

- [Engineering documentation](Docs/TECHNICAL_DOCUMENTATION.md)
- [Report asset package](Docs/report/README.md)
- [Report asset index](Docs/report/REPORT_ASSET_INDEX.md)
- [Diagram sources](Docs/report/diagrams/README.md)
- [API inventory](Docs/report/api/api_inventory.md)
- [Screenshot manifest](Docs/report/screenshots/screenshot_manifest.md)

## Known limitations

- Google OAuth URL generation is implemented; a callback/code-exchange endpoint was not confirmed.
- AI responses require provider availability and valid configuration unless the placeholder adapter is selected.
- Docker-dependent integration tests and report screenshots cannot be completed without Docker and safe demo data/accounts.
- The map uses static frontend coordinates, separate from the graduate catalogue.
- Development CORS uses a wildcard policy and must be restricted before production.

## Maintenance notes

Keep Flyway migrations immutable once applied, keep secrets outside the repository, maintain the typed graduate query boundary when extending retrieval, and update `Docs/report/` when architecture, API, or schema behavior changes.
