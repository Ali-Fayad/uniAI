# LIU Import Report

## Seed Summary
- University count: 1
- Faculty/school count: 4
- Department count: 0
- Degree type count: 4
- Language count: 4
- Source count: 26
- Program count: 15
- MASTER count: 15
- PHD count: 0
- Tuition row count: 15
- Fee item rows: 2
- Admission requirement rows: 3
- Required document rows: 8
- Deadline rows: 1
- Scholarship rows: 0
- Financial aid rows: 1
- Payment plan rows: 2
- Accreditation rows: 0
- Track rows: 8
- Alias rows: 0
- Program-source links: 81
- Out-of-scope skipped: 2

## Validation
- All `research/liu/*.json` parse: pass
- No duplicate program IDs: pass
- No duplicate source IDs: pass
- No duplicate source URLs: pass
- No broken source references: pass
- Out-of-scope rows skipped: pass
- Enum compatibility with V24: pass
- Idempotent Flyway pattern: pass
- `./mvnw -q -DskipTests compile`: pass

## Implementation Notes
- LIU reuses one official catalogue PDF URL for four catalogue-only master records; the migration drops the 'uq_graduate_program_university_url' constraint before seeding to preserve that intentional reuse.
- MBA emphases are seeded as 'CONCENTRATION' tracks under the single MBA record rather than as separate programs.
- Tuition is seeded for all 15 master programs as 'PER_CREDIT' rows, using the official tuition page and the program evidence referenced in the discovery package.
- The LIU official source inventory was fully consumed; no orphan official sources remain.
- Program-level admissions are seeded only where official evidence was explicit enough to support a clean row.
- No distinct LIU graduate accreditation source was isolated in the reviewed package, so accreditation is left unseeded rather than inferred.
