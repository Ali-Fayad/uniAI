# ALBA Shared Graduate Data Report

Date accessed: 2026-07-07  
University: Académie Libanaise des Beaux-Arts (ALBA)  
Official website: https://www.alba.edu.lb  
Task code: SHARED_ALBA_001

## Scope Summary

- Program count remains 8.
- MASTER count remains 8.
- PHD count remains 0.
- Shared graduate data was centralized in `research/alba/university.json`.
- Tuition was not inferred at the program level; it remains null in `research/alba/programs.json`.

## Centralized Shared Data

- admissions_process
- required_documents
- language_requirements
- tuition_model
- tuition_table
- fee_structure
- payment_methods
- payment_plans
- scholarships
- financial_aid
- academic_calendar
- graduate_regulations
- international_students
- accreditation

## Tuition Mapping

- `research/alba/fees_mapping_summary.json` records 0 published tuition rows and 8 unmapped master's programs.
- Program-level tuition remains null in `research/alba/programs.json` because tuition is centralized in `research/alba/university.json`.
- The admissions page includes tuition/fees language, but no machine-readable graduate tuition amounts were recovered from the inspected official sources.

## Validation

- `research/alba/programs.json` parses successfully.
- `research/alba/out_of_scope_programs.json` parses successfully.
- `research/alba/university.json` parses successfully.
- `research/alba/sources.json` parses successfully.
- `research/alba/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were introduced.
- Program count remains 8.
- MASTER count remains 8.
- PHD count remains 0.
- Every source ID in the shared data resolves against `research/alba/sources.json`.
- All cited URLs remain official ALBA URLs.
- `./mvnw -q -DskipTests compile` passes from `Server/`.

## Notes

- ALBA publishes official master program pages and an official master application PDF.
- No official PhD evidence was found in the discovery pass.
- The recovered admissions page confirms the existence of tuition/fees, financial aid, and orientation sections, but the captured content did not expose machine-readable numeric amounts.
- The student booklet and general brochure are retained as the official graduate guidance/regulations references for this pass.

## Recommendation

APPROVE WITH NOTES
