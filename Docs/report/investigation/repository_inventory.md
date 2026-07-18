# Repository Inventory

## Confirmed structure

| Area | Evidence | Confirmed contents |
|---|---|---|
| Frontend | `Client/` | React 19, TypeScript, Vite, React Router, Axios, Leaflet, CV and chat UI |
| Backend | `Server/` | Spring Boot 4 / Java 17; `admin`, `catalog`, `chat`, `cvbuilder`, `feedback`, `shared`, `user` |
| Data | `Server/src/main/resources/db/migration/` | Flyway V1–V56; core and graduate knowledge tables |
| Deployment | `docker-compose.yml`, `nginx/` | PostgreSQL 17, Spring Boot, Vite, nginx, volumes and TLS certificate mount |
| Report evidence | `Docs/`, `README.md`, source/tests | Existing technical documentation, research sources, test suite |

## Physical database finding

`docker-compose.yml` and `application.properties` configure one PostgreSQL datasource. The report therefore presents the database through two logical ERD views only: core application and AI/knowledge.

## Source of truth

Code and Flyway migrations take precedence over legacy documentation. The root README and `Client/README.md` contained stale Docker and template claims at investigation time.

