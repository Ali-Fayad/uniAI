# TUI Import Report

Task code: `SEED_TUI_001`
University: Tripoli University Institute / University of Tripoli
Date accessed: 2026-07-06
Official websites: https://ut.edu.lb, https://new.ut.edu.lb

## Seeded Data

- 4 graduate program rows
- 2 MASTER rows
- 2 PHD rows
- 2 program-level doctoral requirement rows for the official 42-credit PhD rule
- 0 tuition rows

## Preserved

- Both master's programs
- Both PhD programs
- Official 42-credit PhD requirement
- Source traceability through `source` and `graduate_program_source`
- Idempotent Flyway `ON CONFLICT` pattern
- V24-compatible schema usage

## Not Seeded

- No tuition rows were seeded because no official graduate tuition table was published.
- No out-of-scope rows were seeded.
- `official_program_url` was left null in the seed because the captured official evidence does not provide dedicated per-program pages and the schema enforces URL uniqueness per university.

## Validation

- `research/tui/programs.json` parses successfully.
- `research/tui/out_of_scope_programs.json` parses successfully.
- `research/tui/university.json` parses successfully.
- `research/tui/sources.json` parses successfully.
- `research/tui/final_quality_summary.json` parses successfully.
- No duplicate program IDs were introduced.
- No broken source references were introduced.
- All source URLs remain official UT URLs.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Recommendation

APPROVE WITH NOTES
