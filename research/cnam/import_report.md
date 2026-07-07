# CNAM Lebanon Import Report

Task code: `SEED_CNAM_001`
University: Cnam Lebanon / ISSAE-Cnam Liban
Date accessed: 2026-07-07
Official website: https://www.cnam-liban.fr

## Seeded Data

- 3 MASTER program rows
- 0 PHD program rows
- 0 tuition rows

## Preserved

- All three supported master's programs from the official CNAM graduate inventory
- Lebanon-only evidence for `MR12001A-LIB`
- Source traceability through `source` and `graduate_program_source`
- Idempotent Flyway `ON CONFLICT` pattern
- V24-compatible schema usage

## Not Seeded

- No PHD rows were seeded because no official CNAM Lebanon PhD evidence was found.
- No tuition rows were seeded because no official graduate tuition table was published.
- No out-of-scope rows were seeded.

## Validation

- `research/cnam/programs.json` parses successfully.
- `research/cnam/out_of_scope_programs.json` parses successfully.
- `research/cnam/university.json` parses successfully.
- `research/cnam/sources.json` parses successfully.
- `research/cnam/final_quality_summary.json` parses successfully.
- No duplicate program IDs were introduced.
- No broken source references were introduced.
- All source URLs remain official CNAM URLs.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Recommendation

APPROVE WITH NOTES

CNAM Lebanon has three official master's programs in scope. No official PhD evidence was found, so the seed is limited to the supported Master records only.
