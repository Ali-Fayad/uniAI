# Automated Test Results

Last attempted: 2026-07-18.

| Command | Result | Notes |
|---|---|---|
| `cd Server && ./mvnw -q -DskipTests compile` | Passed | compilation succeeded |
| `cd Server && ./mvnw -q test` | Environment-limited | 255 tests reported; 0 assertion failures; 25 errors |
| Focused graduate unit suite | Passed | interpretation, resolver, query, memory, retrieval, and chat focused classes passed in prior validation |
| `cd Client && npm run build` | Passed | production bundle built; Vite warned about chunks above 500 kB |
| `cd Client && npm run lint` | Failed | 66 errors and 25 warnings in existing client source |
| `docker compose config` | Passed | configuration rendered successfully |

The full test errors were not fabricated as product failures: Testcontainers could not access a Docker daemon, and some Mockito inline tests could not self-attach in the current JDK sandbox. Docker-dependent test classes are therefore pending execution in a Docker-enabled environment.
