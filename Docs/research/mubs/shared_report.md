# MUBS Shared Data Report

Date accessed: 2026-07-04
University: Modern University for Business and Science (MUBS)
Official website: https://www.mubs.edu.lb
Task code: MUBS_SHARED_001

## Scope

- Program count remains 2.
- MASTER count remains 2.
- PHD count remains 0.
- No legacy master references were re-added.
- Shared graduate data was centralized in `research/mubs/university.json`.
- Tuition was not inferred where the official source set did not expose a stable numeric amount.

## Centralized Shared Data

- admissions_process
- required_documents
- language_requirements
- tuition_model
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

- `research/mubs/fees_mapping_summary.json` records 0 mapped tuition rows and 2 unmapped programs.
- No program-level tuition was written into `programs.json` because no official numeric tuition was recovered.
- The Cardiff MBA pathways remain concentrations/tracks under one MBA record, as requested.

## Validation

- `research/mubs/programs.json` parses successfully.
- `research/mubs/university.json` parses successfully.
- `research/mubs/sources.json` parses successfully.
- `research/mubs/out_of_scope_programs.json` parses successfully.
- `research/mubs/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were introduced.
- Program count remains 2.
- MASTER count remains 2.
- PHD count remains 0.
- Every source ID in the shared data resolves against `research/mubs/sources.json`.
- All cited URLs remain official MUBS URLs or official MUBS subdomains.
- `./mvnw -q -DskipTests compile` passes from `Server/`.

## Notes

- The current tuition page and Cardiff tuition-fees page confirm fee-related flows, but not a stable numeric graduate tuition table in the accessible source set.
- The current admissions and regulations pages were not fully extractable in the browser pass, so catalogue PDFs remain the clearest official reference for graduate admission requirements and rules.
- The MEHE accreditation statement was preserved from the Computer Science Department page because no broader accreditation catalogue was recovered.

## Recommendation

APPROVE WITH NOTES
