# ESA Import Report

Task code: SEED_ESA_001  
University: École Supérieure des Affaires (ESA Business School)  
Official website: https://www.esa.edu.lb  
Date accessed: 2026-07-07

## Import summary

- University count: 1
- Faculty/school count: 1
- Department count: 0
- Degree type count: 4
- Language count: 4
- Source count: 18
- Program count: 10
- MASTER count: 9
- PHD count: 1
- Tuition rows: 0
- Fee item rows: 0
- Admission requirement rows: 4
- Required document rows: 2
- Deadline rows: 0
- Scholarship rows: 2
- Financial aid rows: 1
- Payment plan rows: 0
- Accreditation rows: 1
- Track rows: 1
- Alias rows: 0
- Program-source links: 24
- Out-of-scope skipped: 0

## Validation

- JSON parses: pass
- No duplicate IDs: pass
- No broken source references: pass
- V24 compatibility: pass
- Idempotent Flyway pattern: pass
- `./mvnw -q -DskipTests compile`: pass

## Implementation notes

- ESA's reviewed official sources did not expose a stable public numeric tuition table, so program tuition remains null and no tuition rows were seeded.
- GEMBA is preserved as a PATHWAY track under the Executive MBA rather than as a separate degree row.
- DBA is preserved as the only doctoral record.
- Shared admissions, required documents, scholarships, financial aid, and accreditation were centralized from the official ESA sources that supported them.
- No out-of-scope graduate degree records were serialized in the seed migration.
- All 18 official ESA source records are used across program rows, program-source links, the GEMBA track, admissions, required documents, scholarships, financial aid, and accreditation.
