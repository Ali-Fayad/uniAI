# GU Shared Graduate Data Report

Date accessed: 2026-07-06  
University: Global University (GU)  
Official website: https://www.gu.edu.lb  
Task code: SHARED_GU_001

## Scope Summary

- Program count remains 4.
- MASTER count remains 4.
- PHD count remains 0.
- Shared graduate data was centralized in `research/gu/university.json`.
- Tuition was not inferred at the program level; it remains null in `research/gu/programs.json`.

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

- `research/gu/fees_mapping_summary.json` records 2 published tuition rows and 1 unmapped master's program.
- Program-level tuition remains null in `research/gu/programs.json` because tuition is centralized in `research/gu/university.json`.
- The Financial Policies page also lists a `PHD Arabic` tuition row, but no official PhD program evidence was recovered, so that row was not mapped.

## Validation

- `research/gu/programs.json` parses successfully.
- `research/gu/out_of_scope_programs.json` parses successfully.
- `research/gu/university.json` parses successfully.
- `research/gu/sources.json` parses successfully.
- `research/gu/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were introduced.
- Program count remains 4.
- MASTER count remains 4.
- PHD count remains 0.
- Every source ID in the shared data resolves against `research/gu/sources.json`.
- All cited URLs remain official GU URLs.
- `./mvnw -q -DskipTests compile` passes from `Server/`.

## Notes

- GU publishes official graduate admissions requirements, required documents, a 2026-2027 academic calendar, and financial policies covering fees, payment timing, refund rules, and the existence of a financial-aid and scholarships section.
- The tuition schedule is official and numeric for the MBA, Master of Education, and Master in Arabic Language and Literature, but those values are centralized here instead of duplicated in program records.
- The Islamic Studies master's tracks remain a single program record with `concentrations_or_tracks`; no separate program page or numeric tuition row was recovered for that master's record.
- The external Google Drive financial-aid file referenced by GU was not retained because validation requires official GU URLs only.

## Recommendation

APPROVE WITH NOTES
