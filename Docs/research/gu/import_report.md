# GU Seed Import Report

## University Counts

- University count: 1
- Faculty count: 2
- Department count: 3
- Degree type count: 2
- Language count: 0
- Source count: 12

## Program Counts

- Program count: 4
- MASTER count: 4
- PHD count: 0

## Tuition and Fee Counts

- Tuition rows: 2
- Fee item rows: 3
- Admission requirement rows: 1
- Required document rows: 11
- Deadline rows: 1
- Scholarship rows: 0
- Financial aid rows: 0
- Payment plan rows: 2
- Accreditation rows: 1
- Track rows: 8
- Alias rows: 0
- Program-source links: 16
- Out-of-scope skipped: 0

## Validation

- `research/gu/programs.json` parses successfully.
- `research/gu/out_of_scope_programs.json` parses successfully.
- `research/gu/university.json` parses successfully.
- `research/gu/sources.json` parses successfully.
- `research/gu/final_quality_summary.json` parses successfully.
- No duplicate program IDs were found.
- No broken source references were found.
- V24 enum compatibility is preserved for the seeded rows.
- The migration uses an idempotent Flyway pattern with conflict handling on canonical keys.
- Program tuition remains null for all four programs because the centralized university tuition is not duplicated at the program level.
- MBA thesis / non-thesis information was preserved as a program track set.
- Islamic Studies concentrations were preserved as a single master record with track rows.
- `./mvnw -q -DskipTests compile` passed from `Server/`.

## Implementation Notes

- The migration seeds the GU university row, two faculties, three departments, two degree types, 12 official sources, and exactly four graduate programs.
- The MBA is seeded as one MASTER record with two OPTION tracks: With Thesis and With Project.
- The Islamic Studies master is seeded as one MASTER record with six CONCENTRATION rows.
- Program-source links preserve source traceability through the official academics, admissions, application, department, catalogue, contract sheet, financial policies, calendar, and accreditation pages.
- Tuition is centralized into faculty-level tuition rows rather than copied into program records.
- Admissions requirements, required documents, academic calendar, payment plans, accreditation, and graduate fee rows are seeded only where the official GU source set exposes machine-safe values.

## Recommendation

APPROVE WITH NOTES
