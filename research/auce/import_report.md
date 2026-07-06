# AUCE Import Report

Task code: `SEED_AUCE_001`
University: American University of Culture & Education (AUCE)
Date accessed: 2026-07-06

## Scope

- Seeded graduate programs: 2
- Seeded MASTER records: 2
- Seeded PHD records: 0
- Seeded out-of-scope records: 0
- Tuition rows seeded: 0

## Seeded Content

- Two officially supported master's programs were seeded:
  - Master of Business Administration
  - Master of Computer Science
- Shared graduate admissions data was centralized and seeded at university scope.
- Concentration / focus-area rows were seeded for both master programs.
- Source rows were seeded for all official AUCE sources listed in `research/auce/sources.json`.
- No out-of-scope graduate rows were imported.

## Tuition

- No graduate tuition rows were seeded.
- Tuition remains NULL because AUCE did not publish an official graduate tuition table in the reviewed source set.

## Validation

- `research/auce/programs.json` parses successfully.
- `research/auce/out_of_scope_programs.json` parses successfully.
- `research/auce/university.json` parses successfully.
- `research/auce/sources.json` parses successfully.
- `research/auce/final_quality_summary.json` parses successfully.
- No duplicate program IDs were found.
- No broken source references were found.
- V24 compatibility is preserved.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Notes

- The migration follows the canonical idempotent Flyway pattern used by the other graduate seeds in this repository.
- Source traceability is preserved through `source`, `graduate_program_source`, and the university-level shared tables.
- No PhD program was created because no official AUCE PhD evidence was found.

## Recommendation

APPROVE WITH NOTES
