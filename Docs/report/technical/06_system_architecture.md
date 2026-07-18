# System Architecture

The browser hosts the React SPA. It calls Spring REST APIs, which authenticate requests, use JPA/JDBC adapters, and persist to PostgreSQL. nginx fronts the Docker stack and proxies API requests. Chat adds an AI provider boundary and, for graduate questions, a SQL-backed retrieval context.

See [high-level architecture](../diagrams/system/high_level_architecture.puml).

