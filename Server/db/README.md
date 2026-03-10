# Server / db

This directory holds database initialisation scripts for the PostgreSQL container.

## How it works

The compose file mounts `./Server/db` to `/docker-entrypoint-initdb.d` inside the
`postgres` container.  PostgreSQL automatically executes all `*.sql` files in that
directory **on the very first start** (i.e. when the `postgres_data` volume is empty).

| File | Purpose |
|------|---------|
| `init.sql` | Creates tables, indexes, and enums that mirror the JPA entity schema |

## Resetting the database

```bash
# stop containers and remove the postgres volume
docker compose down -v

# restart – init.sql runs again from scratch
docker compose up --build
```

## Notes

- Hibernate is configured with `ddl-auto=update`, so it will reconcile any drift
  between `init.sql` and the JPA model at runtime.
- Add seed data at the bottom of `init.sql` (or in a separate `seed.sql` file –
  Postgres runs files alphabetically).
