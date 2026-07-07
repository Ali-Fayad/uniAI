# NDU Seed Import Report

Task code: `NDU_SEED_001`

## Row Counts

- university: 1
- faculty: 7
- department: 16
- degree type: 4
- language: 4
- source: 18
- program: 29
- master: 29
- phd: 0
- tuition rows: 29
- fee item rows: 13
- admission requirement rows: 41
- required document rows: 6
- deadline rows: 6
- scholarship rows: 1
- financial aid rows: 2
- payment plan rows: 1
- accreditation rows: 5
- track rows: 10
- alias rows: 0
- program source links: 116
- out of scope skipped: 2

## Validation

- All `research/ndu/*.json` files parse successfully.
- No duplicate program IDs or official program URLs are present in the finalized dataset.
- All program source references resolve to official NDU sources.
- Tuition rows match the 29 MASTER programs.
- Out-of-scope records skipped: 2.
- Enum usage is compatible with the V24 schema (`degree_type`, `delivery_mode`, `thesis_or_non_thesis`, `tuition.billing_basis`, `language`).
- The migration follows an idempotent Flyway pattern using `WHERE NOT EXISTS`, `ON CONFLICT DO UPDATE`, and stable record keys.
- `./mvnw -q -DskipTests compile` passed.

## Implementation Notes

- Shared admissions, documents, deadlines, fees, scholarships, financial aid, and payment plans are seeded at university scope.
- Program-specific admissions, thesis flags, tracks, accreditation, tuition, and source links are seeded per program where explicitly published.
- Program-source links preserve the original NDU evidence chain for each master’s record.
- No out-of-scope graduate diplomas were imported.
- No PhD rows were created because the final QA verified that NDU has no confirmed doctoral programs in scope.

## Source Notes

- Sources imported: 18
- Sources used by the finalized master’s dataset: 4
- Orphan / reference sources remain in the source table for completeness: NDU-SRC-001, NDU-SRC-004, NDU-SRC-016, NDU-SRC-017
