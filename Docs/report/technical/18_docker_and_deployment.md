# Docker and Deployment

The root compose file defines `postgres`, `app`, `client-build`, `client-dev`, and `nginx`. The configured physical data service is PostgreSQL 17. nginx exposes ports 80/443 and proxies application traffic. Certificates are mounted from `nginx/certs`; the repository instructs developers to generate them before first boot.

The compose file is development-oriented because it mounts source and runs a Vite dev service. Production hardening was not confirmed.

