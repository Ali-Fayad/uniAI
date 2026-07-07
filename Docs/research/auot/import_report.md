# AUOT Import Report

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
- Faculty count: 2
- Department count: 1
- Degree type count: 4
- Language count: 4
- Source count: 9
- Program count: 3
- MASTER count: 3
- PHD count: 0
- Tuition rows: 1
- Fee item rows: 3
- Admission requirement rows: 3
- Required document rows: 5
- Deadline rows: 1
- Scholarship rows: 5
- Financial aid rows: 1
- Payment plan rows: 0
- Accreditation rows: 1
- Track rows: 9
- Alias rows: 0
- Program-source links: 4
- Out-of-scope skipped: 1

## Implementation Notes

- Program-level tuition remains null in `research/auot/programs.json`; tuition is centralized at university scope with one per-credit tuition row.
- The MBA is seeded as one program with nine concentrations recorded as tracks.
- The MBA accreditation is seeded at program scope because the official catalogue explicitly states the MBA is fully accredited by the Ministry of Education and Higher Education.
- The MS Computer Science program uses both the dedicated program page and the current catalogue.
- The MS Information Technology program is seeded from the catalogue only; no standalone official program page was found in this pass.
- The University of London LLM support page remains out of scope because AUT does not clearly present it as an AUT-awarded graduate degree.
- No graduate payment plan was published in the reviewed sources.
