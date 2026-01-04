# uniAI

[![Repo languages](https://img.shields.io/github/languages/top/Ali-Fayad/uniAI?color=blue)](https://github.com/Ali-Fayad/uniAI)
[![Repo size](https://img.shields.io/github/repo-size/Ali-Fayad/uniAI)](https://github.com/Ali-Fayad/uniAI)
[![License](https://img.shields.io/badge/license-None-lightgrey)](https://github.com/Ali-Fayad/uniAI)
[![Last commit](https://img.shields.io/github/last-commit/Ali-Fayad/uniAI)](https://github.com/Ali-Fayad/uniAI/commits/main)

A university information chatbot: a full-stack project that uses a Java Spring Boot backend and a React + TypeScript frontend to provide an AI-powered chatbot that answers questions and offers information about universities in Lebanon.

---

Table of contents
- [What is this project](#what-is-this-project)
- [Languages & composition](#languages--composition)
- [Visual architecture (diagram)](#visual-architecture-diagram)
- [Project structure (visual tree)](#project-structure-visual-tree)
- [Run (Docker Compose) — quick commands](#run-docker-compose---quick-commands)
- [Run (individually) — backend / frontend](#run-individually---backend--frontend)
- [Environment variables & notes](#environment-variables--notes)
- [Troubleshooting & tips](#troubleshooting--tips)
- [What I inspected](#what-i-inspected)

---

What is this project
- Backend: Spring Boot (Java 17) providing REST APIs, authentication (JWT), and server-side logic; includes Thymeleaf templates and JDBC/JPA support.
- Frontend: React + TypeScript (Vite) single-page app that communicates with backend APIs.
- nginx: acts as TLS terminator and reverse proxy to serve static files or proxy to the Vite dev server.
- Orchestration: Docker Compose config supports development and production-like flows, including generating local self-signed certs.

Languages & composition
- Java — 65.3% (Server / Spring Boot)
- TypeScript — 21.3% (Client / React + Vite)
- HTML — 11.4% (Client templates / static)
- Other — 2%

Visual architecture (diagram)
```mermaid
flowchart LR
  subgraph Host
    direction TB
    Certs[certs volume]:::vol
    nginx[nginx container\n(443/80)]:::srv
    app[Spring Boot app\n9090]:::srv
    client_dev[Vite dev\n5173]:::srv
    client_build[Client build -> /Server/client/dist]:::proc
  end

  Certs --> nginx
  nginx -->|/api ->| app
  nginx -->|static files| ServerDist[/Server/client/dist/]
  client_dev -->|proxy (dev)| nginx
  client_build --> ServerDist

  classDef srv fill:#f8f9fa,stroke:#333,stroke-width:1px;
  classDef vol fill:#fff7d6,stroke:#c79b00;
  classDef proc fill:#e6f7ff,stroke:#1a8cff;
```

(Project diagram: certs → nginx → app and static client files. Vite dev can be proxied by nginx or accessed directly for hot reload.)

Project structure (visual tree)
```text
uniAI/
├─ Client/                # React + TypeScript + Vite
│  ├─ public/
│  ├─ src/
│  ├─ index.html
│  ├─ package.json
│  └─ README.md
├─ Server/                # Spring Boot (Maven)
│  ├─ src/
│  ├─ pom.xml
│  ├─ server/Dockerfile
│  └─ docker-compose.yml (server-only)
├─ nginx/                 # nginx Dockerfile & config
├─ docker-compose.yml     # top-level compose (certs, app, client-build, client-dev, nginx)
└─ README.md
```

Run (Docker Compose) — quick commands
- Start entire stack (dev-friendly):
```bash
# build and run (foreground)
docker compose up --build

# or detach:
docker compose up --build -d
```
- Build frontend only (produces Server/client/dist) then run server + nginx:
```bash
# build frontend artifacts
docker compose run --rm client-build

# then run app + nginx (and certs)
docker compose up --build app nginx certs
```

What docker-compose does (top-level)
- certs: lightweight Alpine container that generates self-signed certs into a Docker volume.
- app: builds Server/server/Dockerfile and runs Spring Boot in dev mode (mvn spring-boot:run). Port mapped to host 9090.
- client-build: uses node to build frontend; copies to Server/client/dist for static serving.
- client-dev: runs Vite dev server with HMR on port 5173.
- nginx: serves static files from Server/client/dist or proxies to client-dev; terminates TLS (ports 8443 and 8080 on host in top-level compose).

Run (individually) — backend / frontend
- Backend (from Server/):
```bash
cd Server
# use included Maven wrapper:
./mvnw spring-boot:run
# or if you have Maven:
mvn spring-boot:run
```
- Frontend (from Client/):
```bash
cd Client
npm ci
npm run dev    # Vite dev server (http://localhost:5173)
npm run build  # builds dist/ (used by client-build)
```

Environment variables & notes
- Common vars (used by `app` in compose):
  - SPRING_PROFILES_ACTIVE (defaults to `dev` in compose)
  - APP_BASE_URL (example: https://localhost)
  - JWT_SECRET (used for JWT; must be set for auth features)
  - MAIL_USERNAME, MAIL_PASSWORD (for mail sender)
- Recommended: create a local `.env` (gitignored) with these keys for docker-compose:
```env
JWT_SECRET=supersecret
MAIL_USERNAME=you@example.com
MAIL_PASSWORD=yourpass
APP_BASE_URL=https://localhost
```

Visuals & badges ideas (included above)
- Shields for top language and repo size.
- Mermaid architecture diagram for quick onboarding.
- Placeholder for screenshots: add images/screenshots in `Client/public/screenshots/` and reference them here:
  - Example: `![chat-screenshot](Client/public/screenshots/chat.png)` (commit actual images to repo).

Troubleshooting & tips
- Permission issues with `~/.m2`: ensure Docker can read/write `.m2` or remove the mount if undesired.
- Port conflicts: top-level compose maps HTTPS to 8443 (to avoid clashing with system 443). Change ports in docker-compose.yml if needed.
- If nginx serves old files: ensure `client-build` finished and `Server/client/dist` contains updated files; try `docker compose run --rm client-build` then `docker compose restart nginx`.
- Large node_modules: compose uses a named volume `client_node_modules` to avoid re-installing every time.
- For production: replace self-signed certs with trusted CA certs (Let's Encrypt) and use a production-ready build of the backend (jar) instead of running `mvn spring-boot:run`.

What I inspected to create this README
- Top-level `docker-compose.yml` — orchestration and port mappings
- `Client/` — `package.json`, `README.md`, `vite.config.ts` and core project files
- `Server/` — `pom.xml`, `server/Dockerfile`, `docker-compose.yml`, and `src/` layout
- `nginx/` folder — to confirm reverse-proxy pattern
- Confirmed languages composition provided: Java (majority backend), TypeScript & HTML in frontend

Next suggestions (optional)
- Add screenshots or a short demo GIF in `Client/public/screenshots/` and reference them below the diagram to make the README more engaging.
- Add a small CI workflow to build the client and optionally deploy artifacts to a release or to GitHub Pages for a demo.
- Add sample `.env.example` containing the variable names (without secrets) for contributors.

---

If you'd like, I can now:
- Open a PR that replaces the repository README with this file (I can create the commit/PR for you), or
- Add screenshot placeholders and a `.env.example` and push them in a branch and open a PR.

Which would you prefer?  
