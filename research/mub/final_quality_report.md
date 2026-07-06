# MUB Final Quality Report

Date accessed: 2026-07-06

## Validation Summary

- JSON parses: pass
- `programs.json` records: 2
- MASTER count: 1
- PHD count: 1
- `out_of_scope_programs.json` records: 3
- Duplicate program IDs: none
- Duplicate source IDs: none
- Duplicate source URLs: none
- Every source reference resolves: pass
- Shared data centralized in `university.json`: pass
- V24 compatibility: pass
- `./mvnw -q -DskipTests compile`: pass

## Inventory Status

- The final inventory contains one official MASTER program and one official PHD program, both in the Faculty of Islamic Studies.
- The official source set does not support any additional graduate inventory rows.
- The Faculty of Nursing and Health Sciences page contains only weak mission-level graduate wording and no safe current graduate program listing.
- The Faculty of Teacher Education page contains diplomas and certificates only, which are out of scope.

## Tuition Coverage

- Program-level tuition: null for both in-scope programs
- Shared graduate tuition captured in `university.json` and `fees_mapping_summary.json`
- Tuition coverage is therefore correct for this dataset shape: program rows remain null while faculty-level fee rows are centralized

## Orphan Sources

- None

## Completeness by Field

- `program_description`: 2/2
- `duration`: 2/2
- `thesis_or_non_thesis`: 2/2
- `admission_requirements`: 2/2
- `interview_requirement`: 1/2
- `concentrations_or_tracks`: 1/2
- `credits`: 0/2
- `delivery_mode`: 0/2
- `language`: 0/2
- `gre_requirement`: 0/2
- `gmat_requirement`: 0/2
- `portfolio_requirement`: 0/2
- `experience_requirement`: 0/2
- `accreditation`: 0/2

## Intentional Nulls

- `credits` remains null because the official Faculty of Islamic Studies page describes semesters and American credits in phase terms, but does not publish a single clean title-level credit total for the overall program.
- `delivery_mode`, `language`, `gre_requirement`, `gmat_requirement`, `portfolio_requirement`, `experience_requirement`, and `accreditation` remain null because the reviewed official source set did not publish safe title-level values.

## Official-Source Limitations

- No standalone graduate program pages were found for the Islamic Studies master’s or doctoral offerings.
- The faculty page is the only direct graduate evidence source.
- The tuition page publishes faculty-level graduate fee rows, not program-page tuition fields.

## Recommendation

APPROVE WITH NOTES
