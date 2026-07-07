# ALBA Seed Import Report

## University Counts

- University count: 1
- Faculty count: 0
- Department count: 0
- Degree type count: 2
- Language count: 0
- Source count: 16

## Program Counts

- Program count: 8
- MASTER count: 8
- PHD count: 0

## Tuition and Fee Counts

- Tuition rows: 0
- Fee item rows: 0
- Admission requirement rows: 0
- Required document rows: 0
- Deadline rows: 0
- Scholarship rows: 0
- Financial aid rows: 0
- Payment plan rows: 0
- Accreditation rows: 0
- Track rows: 0
- Alias rows: 0
- Program-source links: 9
- Out-of-scope skipped: 1

## Validation

- `research/alba/programs.json` parses successfully.
- `research/alba/out_of_scope_programs.json` parses successfully.
- `research/alba/university.json` parses successfully.
- `research/alba/sources.json` parses successfully.
- `research/alba/final_quality_summary.json` parses successfully.
- No duplicate program IDs were found.
- No broken source references were found.
- V24 enum compatibility is preserved for the seeded rows.
- The migration uses an idempotent Flyway pattern with conflict handling on canonical keys.
- Tuition remains null for all eight programs because no official machine-readable graduate tuition values were published.
- No out-of-scope rows were seeded.
- `./mvnw -q -DskipTests compile` passed from `Server/`.

## Implementation Notes

- The migration seeds the ALBA university row, 16 official sources, two degree types, and exactly eight graduate MASTER programs.
- The Master in Global Design keeps its bilingual official naming. Its PDF source is retained as a secondary source link.
- The cinema school page covers both Master en Réalisation Cinéma and Master en Production Audiovisuelle. The database row for Production Audiovisuelle keeps `official_program_url` null because the schema enforces uniqueness on program URLs within a university.
- No tuition, fee, scholarship, financial-aid, deadline, or accreditation rows were seeded because the official ALBA source set did not expose machine-safe relational values for those tables.

## Recommendation

APPROVE WITH NOTES
