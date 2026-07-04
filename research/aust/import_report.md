# AUST Seed Import Report

## Scope

- University count: 1
- Faculty/school count: 4
- Department count: 0
- Degree type count: 4
- Language count: 4
- Source count: 27
- Program count: 17
- MASTER count: 17
- PHD count: 0
- Tuition row count: 17
- Fee item rows: 6
- Admission requirement rows: 2
- Required document rows: 8
- Deadline rows: 3
- Scholarship rows: 1
- Financial aid rows: 1
- Payment plan rows: 2
- Accreditation rows: 0
- Track rows: 2
- Alias rows: 0
- Program-source links: 63
- Out-of-scope skipped: 0

## Validation

- `research/aust/programs.json` parses successfully.
- `research/aust/out_of_scope_programs.json` parses successfully.
- `research/aust/university.json` parses successfully.
- `research/aust/sources.json` parses successfully.
- `research/aust/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate source IDs were found.
- No duplicate source URLs were found.
- Every source reference in the finalized AUST dataset resolves to an official AUST source.
- V24 enum compatibility is preserved for the seeded rows.
- The migration uses idempotent unique-key conflict handling and source-backed joins.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Implementation Notes

- The migration seeds the university row, four faculties, four degree types, four languages, 27 sources, 17 graduate programs, 17 tuition rows, and the centralized shared-data rows.
- No department rows were inserted because the official AUST source set did not expose stable department names for the finalized inventory.
- No accreditation rows were inserted because the reviewed AUST source set did not expose a stable official accreditation statement.
- MBA and TOEFL discrepancies remain preserved in notes and were not resolved by guessing.
- Official program URL uniqueness remains intact.
- No frontend or API code was changed.

## Recommendation

APPROVE WITH NOTES
