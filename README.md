# uniAI

[![Top language](https://img.shields.io/github/languages/top/Ali-Fayad/uniAI?color=blue)](https://github.com/Ali-Fayad/uniAI)
[![Repo size](https://img.shields.io/github/repo-size/Ali-Fayad/uniAI)](https://github.com/Ali-Fayad/uniAI)
[![Last commit](https://img.shields.io/github/last-commit/Ali-Fayad/uniAI)](https://github.com/Ali-Fayad/uniAI/commits/main)

A university information chatbot — a full‑stack application using a Java Spring Boot backend and a React + TypeScript frontend to answer questions and provide information about universities in Lebanon.

---

## Table of contents
- [What is this project](#what-is-this-project)
- [Languages & composition](#languages--composition)
- [Architecture (visual)](#architecture-visual)
- [Project structure (visual tree)](#project-structure-visual-tree)
- [Quick start — Docker Compose](#quick-start---docker-compose)
- [Run individually (dev)](#run-individually-dev)
- [Environment variables](#environment-variables)
- [Troubleshooting & tips](#troubleshooting--tips)
- [Screenshots / Demo ideas](#screenshots--demo-ideas)
- [What I inspected](#what-i-inspected)
- [Next steps / suggestions](#next-steps--suggestions)

---

## What is this project
- Backend: Java Spring Boot (Java 17) exposing REST endpoints, handling authentication (JWT) and server-side logic. Includes Thymeleaf templates and JPA/JDBC support.
- Frontend: React + TypeScript (Vite) single page application delivering the UI and interacting with backend APIs.
- nginx: TLS termination and reverse proxy to serve the built static frontend and proxy API requests to the backend.
- Orchestration: Docker Compose workflows for development (with hot reload) and production-like static serving.

---

## Languages & composition
This repository language breakdown (provided):
- Java — 65.3% (Server / Spring Boot)
- TypeScript — 21.3% (Client / React + Vite)
- HTML — 11.4% (Client static files / templates)
- Other — 2%

---

## Architecture (visual)

Paste this Mermaid diagram into GitHub README (it uses `<br>` inside quoted labels, which GitHub's Mermaid accepts):

```mermaid
flowchart LR
  subgraph Host
    direction TB
    Certs["certs volume"]:::vol
    nginx["nginx container<br>ports: 8443(host)/443(container) or 8080(host)/80(container)"]:::srv
    app["Spring Boot app<br>9090"]:::srv
    client_dev["Vite dev server<br>5173"]:::srv
    client_build["Client build → Server/client/dist"]:::proc
    ServerDist["Server/client/dist (static files)"]:::fs
  end

  Certs --> nginx
  nginx -->|/api →| app
  nginx -->|serves static| ServerDist
  client_dev -->|proxy (dev)| nginx
  client_build --> ServerDist

  classDef srv fill:#f8f9fa,stroke:#333,stroke-width:1px;
  classDef vol fill:#fff7d6,stroke:#c79b00;
  classDef proc fill:#e6f7ff,stroke:#1a8cff;
  classDef fs fill:#f0fff4,stroke:#1f8f3b;
```

Short explanation:
- certs: generates self-signed certs for local TLS.
- nginx: serves static files or proxies to Vite dev and proxies /api to the Spring Boot app.
- client-dev: Vite dev server for hot reload (port 5173).
- client-build: builds static assets into `Server/client/dist`.
- app: Spring Boot backend (port 9090).

---

## Project structure (visual tree)
```text
uniAI/
├─ Client/                # React + TypeScript + Vite frontend
│  ├─ public/
│  ├─ src/
│  ├─ index.html
│  ├─ package.json
│  └─ README.md
├─ Server/                # Spring Boot (Maven) backend
│  ├─ src/
│  ├─ pom.xml
│  ├─ server/Dockerfile
│  └─ docker-compose.yml (server-only)
├─ nginx/                 # nginx Dockerfile & config
├─ docker-compose.yml     # top-level compose (certs, app, client-build, client-dev, nginx)
└─ README.md
```

---

## Quick start — Docker Compose

Recommended: Docker and Docker Compose (or `docker compose` v2).

From repository root:

1. Build and run the full development-friendly stack:
```bash
docker compose up --build
# or detached
docker compose up --build -d
```

2. Build frontend only (to produce `Server/client/dist`), then run app + nginx:
```bash
# build frontend artifacts
docker compose run --rm client-build

# then run server + nginx + certs
docker compose up --build app nginx certs
```

What each service does (top-level compose):
- certs: creates self-signed certs stored in a Docker volume.
- client-build: runs `npm ci && npm run build` and copies `dist` to `Server/client/dist`.
- client-dev: runs Vite dev server with HMR (port 5173).
- app: builds/runs the Spring Boot app using the provided Dockerfile / Maven wrapper; port 9090.
- nginx: serves static files from `Server/client/dist` or proxies to client-dev; terminates TLS and forwards /api to the backend.

Note: Top-level compose maps HTTPS to host 8443 (to avoid clashing with system 443). You can change host ports in `docker-compose.yml`.

---

## Run individually (development)

Backend (local, no Docker)
```bash
cd Server
# use the included Maven wrapper
./mvnw spring-boot:run
# or with global maven:
mvn spring-boot:run
```

Frontend (local, no Docker)
```bash
cd Client
npm ci
npm run dev      # Vite dev server — http://localhost:5173
npm run build    # production build -> dist/
```

---

## Environment variables

Used (or referenced) by services:
- SPRING_PROFILES_ACTIVE (compose uses `dev`)
- APP_BASE_URL (ex: https://localhost)
- JWT_SECRET (required for JWT auth flows)
- MAIL_USERNAME, MAIL_PASSWORD (email sending)

Create a `.env` file (gitignore it) in repo root for local docker-compose:
```env
JWT_SECRET=supersecret
MAIL_USERNAME=your@mail.com
MAIL_PASSWORD=strongpassword
APP_BASE_URL=https://localhost
```

Do not commit secrets.

---

## Troubleshooting & tips
- Permission issues with `~/.m2`: ensure Docker can read/write your `.m2` directory or remove the mount in `docker-compose.yml`.
- Port conflicts: adjust host port mappings if 8443/8080/5173/9090 are in use.
- Nginx still serving old files: re-run `docker compose run --rm client-build` then `docker compose restart nginx`.
- If client HMR is desired, use `client-dev` and access Vite directly on port 5173 or configure nginx to proxy to it.
- For production: replace self-signed certs with real certificates (Let's Encrypt) and use a built jar/wrapper instead of running `mvn spring-boot:run`.

---

## Screenshots / Demo ideas
Add screenshots or a short gif in `Client/public/screenshots/` and reference them here:

Example:
```markdown
![chat-screenshot](Client/public/screenshots/chat.png)
```

Screenshots improve onboarding and make the README visually attractive.

---

## What I inspected to produce this README
- Top-level `docker-compose.yml` — orchestration, ports and services
- `Client/` — `package.json`, `README.md`, `vite.config.ts`, `index.html`
- `Server/` — `pom.xml`, `server/Dockerfile`, `docker-compose.yml`, `src/` layout
- `nginx/` — to confirm reverse-proxy pattern
- Provided language composition: Java (majority), TypeScript, HTML

---

## Next steps / suggestions
- Add a small `.env.example` (no secrets) listing required env variables for contributors.
- Add screenshots or a demo GIF to `Client/public/screenshots/`.
- Add a GitHub Actions workflow to:
  - run frontend build,
  - run tests,
  - optionally update `Server/client/dist` artifacts or produce an artifact/ release.
- Consider a short CONTRIBUTING.md and issue templates to onboard contributors.

---

If you'd like, I can:
- Open a PR that replaces/updates the repository README with this file.
- Create `/.env.example` and screenshot placeholders and open a PR for them.
Tell me which action you'd like next and I can create the changes and/or PR for you.
