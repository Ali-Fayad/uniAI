# AUL Import Report

## University Counts

- University count: 1
- Faculty/school count: 3
- Department count: 0
- Degree type count: 4
- Language count: 4
- Source count: 9

## Program Counts

- Program count: 3
- MASTER count: 3
- PHD count: 0

## Tuition and Fee Counts

- Tuition rows: 0
- Fee item rows: 3
- Admission requirement rows: 1
- Required document rows: 11
- Deadline rows: 1
- Scholarship rows: 3
- Financial aid rows: 1
- Payment plan rows: 1
- Accreditation rows: 0
- Track rows: 0
- Alias rows: 0
- Program-source links: 6
- Out-of-scope skipped: 2

## Validation

- JSON parse: pass
- No duplicate program IDs: pass
- No duplicate source IDs: pass
- No duplicate source URLs: pass
- No broken source references: pass
- V24 enum compatibility: pass
- Idempotent Flyway pattern: pass
- `./mvnw -q -DskipTests compile`: pass

## Implementation Notes

- AUL is modeled conservatively as 3 in-scope graduate programs: one MBA, one Master of Science in Engineering, and one Master of Science.
- No PhD rows were seeded because no official PhD program evidence was found.
- Tuition remains null for all programs because no official graduate tuition schedule was published in the reviewed AUL sources.
- Fee rows are limited to the public labels exposed by the admissions page; numeric amounts were not published.
- Scholarships are represented only where AUL exposed scholarship labels, not award values.
- Payment-plan details and accreditation details were not published in the reviewed official sources and are therefore not seeded beyond the available policy context.