# LGU Seed Import Report

## University Counts

- University count: 1
- Faculty/school count: 3
- Department count: 0
- Degree type count: 2
- Language count: 3
- Source count: 15

## Program Counts

- Program count: 4
- MASTER count: 4
- PHD count: 0

## Tuition and Fee Counts

- Tuition rows: 0
- Fee item rows: 0
- Admission requirement rows: 7
- Required document rows: 7
- Deadline rows: 0
- Scholarship rows: 3
- Financial aid rows: 3
- Payment plan rows: 2
- Accreditation rows: 0
- Track rows: 0
- Alias rows: 0
- Program-source links: 10
- Out-of-scope skipped: 2

## Validation

- `research/lgu/programs.json` parses successfully.
- `research/lgu/out_of_scope_programs.json` parses successfully.
- `research/lgu/university.json` parses successfully.
- `research/lgu/sources.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate source IDs were found.
- No duplicate source URLs were found.
- Every source reference in the finalized LGU dataset resolves to an official LGU source.
- V24 enum compatibility is preserved for the seeded rows.
- The migration uses idempotent unique-key conflict handling and source-backed joins.
- `./mvnw -q -DskipTests compile` passed from `Server/`.

## Implementation Notes

- The migration seeds the university row, three faculties, two degree types, three languages, 15 sources, four graduate programs, and the centralized shared-data rows that were explicitly published.
- The MBA, Master of Science in Engineering, Master of Arts in Education, and Master of Public Health remain one record each.
- Ed.D. and DBA are intentionally skipped because they are out of scope for this seed.
- Tuition remains null for all programs because no official graduate tuition schedule was published in the recovered LGU source set.
- No fee-item rows were seeded because the official source set did not publish numeric graduate fee amounts.
- No department rows, deadline rows, accreditation rows, tracks, or aliases were inserted because the reviewed official source set did not expose stable data for them.
- Official program URL uniqueness remains intact because the inventory does not publish distinct graduate detail pages.
- No frontend or API code was changed.
