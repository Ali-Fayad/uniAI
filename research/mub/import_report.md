# MUB Import Report

Date accessed: 2026-07-06

## Validation

- JSON parses: pass
- No duplicate IDs: pass
- No broken source references: pass
- V24 compatibility: pass
- Idempotent Flyway pattern: pass
- `./mvnw -q -DskipTests compile`: pass

## Import Summary

- University count: 1
- Faculty count: 1
- Department count: 0
- Degree type count: 4
- Language count: 4
- Source count: 9
- Program count: 2
- MASTER count: 1
- PHD count: 1
- Tuition rows: 3
- Fee item rows: 6
- Admission requirement rows: 2
- Required document rows: 4
- Deadline rows: 1
- Scholarship rows: 0
- Financial aid rows: 1
- Payment plan rows: 0
- Accreditation rows: 0
- Track rows: 8
- Alias rows: 0
- Program-source links: 4
- Out-of-scope skipped: 3

## Implementation Notes

- Program-level tuition remains null in `research/mub/programs.json`; tuition is centralized at university scope in the database seed.
- The three university-scoped tuition rows represent the master program total fee, the doctoral program total fee, and the per-credit fee published on the Faculty of Islamic Studies tuition page.
- The six fee items cover application, registration/printed matter, and defense fees for both graduate levels.
- The master program retains all eight officially published focus areas as `graduate_program_track` records.
- Shared admissions, documents, deadline, and financial support records are seeded at university scope because the official sources do not publish separate program-page checklists.
- No scholarships, payment plans, or accreditation rows were seeded because no safe official graduate-level rows were published in the reviewed sources.
