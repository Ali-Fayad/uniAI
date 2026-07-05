# JU Shared Graduate Data Report

## Summary

Jinan University's shared graduate data was centralized in `research/ju/university.json`.

## Program Counts

- Total graduate records: 20
- MASTER: 17
- PHD: 3
- Tuition populated: 0
- Tuition null: 20

## Shared Data Populated

- Admissions process
- Required documents
- Language requirements
- Tuition model
- Fee structure
- Payment methods
- Payment plans
- Scholarships
- Financial aid
- Academic calendar
- Graduate regulations
- International students
- Accreditation notes

## Tuition and Fees

- JU publishes a per-credit tuition methodology for graduate study.
- The recovered official source set did not expose a complete program-specific graduate tuition schedule.
- Tuition remains null in `programs.json` for all 20 graduate records.
- Application and registration fees were referenced in official admission material, but no safe numeric graduate amounts were visible in the extracted sources.
- The financial system page does publish first-payment amounts and installment deadlines, and those are captured in `university.json` under payment plans.

## Admissions Summary

- Graduate admission rules are centralized on the official Graduate and Entrance Registration pages.
- Required documents are based on prior academic credentials, identity documents, and completed application material.
- No invented interview or test thresholds were added.

## Scholarships and Financial Aid

- JU states that scholarship percentages apply only to study-unit value.
- Semester fees and foreign-language fees are excluded from scholarship percentages.
- The financial-aid page indicates that scholarships may cover part or all of the prescribed stage.

## Payment Summary

- Cash payment at the accounting department is published.
- Cash payment at Byblos Bank branches is published.
- International transfer to the university account is published.

## Remaining Official Gaps

- Exact program-specific graduate tuition amounts were not exposed.
- Exact application-fee and registration-fee amounts were not exposed.
- No explicit graduate-wide language threshold was found.
- No explicit accreditation statement was recovered.

## Validation

- `research/ju/programs.json` parses successfully.
- `research/ju/out_of_scope_programs.json` parses successfully.
- `research/ju/fees_mapping_summary.json` parses successfully.
- All source references resolve to the official JU source set.
- All URLs referenced in the source set are official JU URLs.
- `./mvnw -q -DskipTests compile` passed from `Server/`.

## Recommendation

APPROVE WITH NOTES
