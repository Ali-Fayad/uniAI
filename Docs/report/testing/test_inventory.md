# Test Inventory

Backend test classes are under `Server/src/test/java`. Coverage includes:

- authentication, JWT, email diagnostics, users, feedback, admin, catalog;
- chat application service, titles, provider adapters/status, budgets, citations;
- graduate query interpretation, query model, follow-up resolver, memory, SQL retrieval ranking/compression;
- Testcontainers integration tests for chat, catalog, authentication, admin, and graduate retrieval.

No frontend test/spec files were found in `Client/src` or `Client/test`. The frontend package exposes `lint` and `build`, but no `test` script.

