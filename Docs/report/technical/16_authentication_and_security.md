# Authentication and Security

Spring Security is stateless and inserts `JwtFilter` before username/password authentication. `/api/auth/**` and health are public; catalog, application APIs, and `/api/admin/**` are protected, with ADMIN role required for administration and selected actuator endpoints. Passwords use BCrypt. JWT configuration comes from environment variables.

Security note: `SecurityConfig` currently permits wildcard CORS origin patterns for local/network development and explicitly marks production restriction as TODO. This must be hardened before production deployment.

