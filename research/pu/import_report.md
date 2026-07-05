# PU Import Report

## University Counts

- University count: 1
- Faculty/school count: 2
- Department count: 0
- Degree type count: 4
- Language count: 4
- Source count: 12

## Program Counts

- Program count: 2
- MASTER count: 2
- PHD count: 0

## Tuition and Fee Counts

- Tuition rows: 2
- Fee item rows: 1
- Admission requirement rows: 1
- Required document rows: 11
- Deadline rows: 1
- Scholarship rows: 4
- Financial aid rows: 1
- Payment plan rows: 1
- Accreditation rows: 0
- Track rows: 0
- Alias rows: 0
- Program-source links: 15
- Out-of-scope skipped: 0

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

- PU is seeded conservatively as two graduate master's programs: MBA and LL.M.
- MBA and LL.M. remain single graduate programs, not split into inferred subprograms.
- Tuition is seeded as graduate tuition rows in the database and remains summarized in `research/pu/university.json` as the canonical shared mapping.
- The application fee is seeded once at university scope from the official admissions pages.
- No official PhD evidence was found.
