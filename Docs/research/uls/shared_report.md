# ULS Shared Data Report

Date accessed: 2026-07-05
University: Université La Sagesse (ULS)
Official website: https://www.uls.edu.lb
Task code: ULS_SHARED_001

## Scope

- Program count remains 13.
- MASTER count remains 13.
- PHD count remains 0.
- No DBA, diploma, or other out-of-scope records were moved into the graduate inventory.
- Shared graduate data was centralized in `research/uls/university.json`.
- Tuition was not inferred where the official source set did not expose a stable numeric amount.

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

- `research/uls/fees_mapping_summary.json` records 7 published tuition rows and 1 unmapped program.
- Program-level tuition remains null in `programs.json` because tuition is centralized in `university.json`.
- The MIAGE double master is intentionally left unmapped because no stable numeric tuition figure was published in the inspected official source set.

## Validation

- `research/uls/programs.json` parses successfully.
- `research/uls/out_of_scope_programs.json` parses successfully.
- `research/uls/university.json` parses successfully.
- `research/uls/sources.json` parses successfully.
- `research/uls/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were introduced.
- Program count remains 13.
- MASTER count remains 13.
- PHD count remains 0.
- Every source ID in the shared data resolves against `research/uls/sources.json`.
- All cited URLs remain official ULS URLs.
- `./mvnw -q -DskipTests compile` passes from `Server/`.

## Notes

- The public admissions pages confirm the graduate application flow, but they do not expose a complete graduate-specific admissions manual in the inspected source set.
- The public sources do not expose a detailed graduate checklist, so `required_documents.items` remains empty rather than inferred.
- The official tuition-fees page provides faculty-level graduate tuition and fee amounts for the listed graduate faculties.
- The official Academics page also shows a DBA, a D2E diploma, and a Canon Law lawyer diploma; these remain excluded from the MASTER/PHD inventory.
- The unlabeled Canon Law Master line was not serialized because the official title is not explicit enough to avoid inference.

## Recommendation

APPROVE WITH NOTES
