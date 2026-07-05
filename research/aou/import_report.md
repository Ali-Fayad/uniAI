# AOU Seed Import Report

## Scope

- University count: 1
- Faculty/school count: 3
- Department count: 0
- Degree type count: 4
- Language count: 4
- Source count: 20
- Program count: 5
- MASTER count: 5
- PHD count: 0
- Tuition row count: 5
- Fee item rows: 6
- Admission requirement rows: 2
- Required document rows: 7
- Deadline rows: 0
- Scholarship rows: 1
- Financial aid rows: 1
- Payment plan rows: 1
- Accreditation rows: 5
- Track rows: 0
- Alias rows: 0
- Program-source links: 38
- Out-of-scope skipped: 3 diploma programs

## Validation

- `research/aou/programs.json` parses successfully.
- `research/aou/out_of_scope_programs.json` parses successfully.
- `research/aou/university.json` parses successfully.
- `research/aou/sources.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate source IDs were found.
- No duplicate source URLs were found.
- No duplicate official program URLs were found.
- Every source reference in the finalized AOU dataset resolves to an official AOU source.
- V24 enum compatibility is preserved for the seeded rows.
- The migration uses an idempotent `ON CONFLICT` pattern and source-backed joins.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Implementation Notes

- The migration seeds the university row, three faculties, four degree types, four languages, all 20 source rows, five graduate programs, five tuition rows, centralized fee rows, centralized admission/document rows, scholarship and financial-aid rows, one payment plan, five program-level accreditation rows, and 38 program-source links.
- Program-source links are split between narrative source links and tuition source links for traceability.
- The MBA remains one program per the finalized inventory.
- No department rows were inserted because the official AOU source set did not expose stable graduate department names.
- No track rows were inserted because the finalized inventory does not expose dedicated graduate tracks as separate rows.
- No alias rows were inserted because no official aliases were retained in the finalized dataset.
- Program duration values were left null in the database because the official AOU pages publish duration as a range rather than a single canonical number.
- No deadline rows were inserted because the reviewed official AOU source set did not publish stable graduate application deadlines.
- No frontend or API code was changed.

## Recommendation

APPROVE WITH NOTES
