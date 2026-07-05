# HU Import Report

## University Counts

- University count: 1
- Faculty/school count: 2
- Department count: 0
- Degree type count: 4
- Language count: 4
- Source count: 32

## Program Counts

- Program count: 3
- MASTER count: 3
- PHD count: 0

## Tuition and Fee Counts

- Tuition rows: 3
- Fee item rows: 3
- Admission requirement rows: 3
- Required document rows: 7
- Deadline rows: 2
- Scholarship rows: 2
- Financial aid rows: 1
- Payment plan rows: 1
- Accreditation rows: 1
- Track rows: 13
- Alias rows: 0
- Program-source links: 23
- Out-of-scope skipped: 2

## Validation

- JSON parse: pass
- No duplicate program IDs: pass
- No duplicate source IDs: pass
- No duplicate source URLs: pass
- No broken source references: pass
- V24 compatibility: pass
- Idempotent Flyway pattern: pass
- `./mvnw -q -DskipTests compile`: pass

## Implementation Notes

- HU is modeled conservatively as 3 in-scope graduate programs: one MBA with 6 concentrations, MA Education, and MA Psychology.
- The MBA specialization areas are stored as tracks/concentrations under a single program record.
- Program tuition is seeded exactly as modeled at USD 455 per credit for all 3 programs.
- Shared admissions, required documents, deadlines, fees, scholarships, financial aid, payment plans, and accreditation are centralized at university scope.
- No PhD rows were seeded.
- The two excluded graduate-related offerings remain in `out_of_scope_programs.json` and are not imported.