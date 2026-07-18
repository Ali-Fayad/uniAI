# Table 03 — Database Technology Comparison
Purpose: justify the implemented PostgreSQL choice.
| Technology | Strength | Trade-off | Project status |
|---|---|---|---|
| PostgreSQL | relational constraints, SQL aggregation, JSONB | operational database service required | Implemented |
| Document database | flexible documents | weaker fit for FK-rich schema | Not selected |
Source evidence: `docker-compose.yml`, Flyway SQL. Notes: comparison is architectural reasoning, not benchmark data. Suggested chapter: Technology selection. Last verification: 2026-07-18.
