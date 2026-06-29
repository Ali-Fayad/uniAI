# UOB Seed QA Report

## Verdict

**APPROVE WITH NOTES**

## Scope Reviewed

- `Server/src/main/resources/db/migration/V24__graduate_program_schema_v2.sql`
- `Server/src/main/resources/db/migration/V25__seed_aub_graduate_data.sql`
- `Server/src/main/resources/db/migration/V26__seed_lau_graduate_data.sql`
- `Server/src/main/resources/db/migration/V27__seed_usj_graduate_data.sql`
- `Server/src/main/resources/db/migration/V28__seed_usek_graduate_data.sql`
- `Server/src/main/resources/db/migration/V29__seed_uob_graduate_data.sql`
- `research/uob/programs.json`
- `research/uob/sources.json`
- `research/uob/final_quality_report.md`
- `research/uob/final_quality_summary.json`
- `research/uob/import_report.md`

## Validation Performed

- Parsed successfully:
  - `research/uob/programs.json`
  - `research/uob/out_of_scope_programs.json`
  - `research/uob/university.json`
  - `research/uob/sources.json`
  - `research/uob/fees_mapping_summary.json`
  - `research/uob/final_quality_summary.json`
  - `research/uob/import_report.md`
- Ran successfully:
  - `./mvnw -q -DskipTests compile` from `Server/`
- Flyway database execution was not performed because no datasource is configured in this workspace.

## Row-Count Comparison

Final dataset counts match the finalized JSON and the seed migration report:

- Universities: 1
- Faculties/schools: 9
- Departments: 23
- Degree types: 4
- Languages: 4
- Sources: 28
- Programs: 60
- MASTER: 59
- PHD: 1
- Tuition rows: 58
- Fee item rows: 14
- Admission requirement rows: 8
- Required document rows: 4
- Deadline rows: 1
- Scholarship rows: 1
- Financial aid rows: 1
- Payment plan rows: 1
- Accreditation rows: 2
- Track rows: 43
- Alias rows: 0
- Program-source links: 180
- Out-of-scope rows skipped: 2

## Migration Order And Constraint Review

- `V29__seed_uob_graduate_data.sql` is correctly ordered after `V28__seed_usek_graduate_data.sql`.
- The migration removes `uq_graduate_program_university_url`.
- That change is justified because UOB intentionally reuses hub/catalog URLs for multiple graduate programs.
- Program uniqueness remains protected by `(university_id, program_key)`.
- Source URL uniqueness remains protected by the source table uniqueness on `(university_id, url)`.
- The change is consistent with the USJ and USEK seed approach.

## Enum And Schema Checks

No incompatibility was found with the V24 graduate schema.

Checked values remain within the allowed enums/checks:

- `degree_type`: `MASTER`, `PHD`
- `delivery_mode`: null only
- `language`: `ARABIC`, `ENGLISH`, `FRENCH`, `MULTILINGUAL`
- `thesis_or_non_thesis`: `THESIS`, `NON_THESIS`, `THESIS_OR_PROJECT`, `PROJECT`, null
- tuition `billing_basis`: `PER_CREDIT`, `PER_ACADEMIC_YEAR`
- tuition `currency`: `USD`

## Referential Integrity Review

- Every program has at least one source.
- Every program-source link resolves to an existing source.
- Every tuition row maps to an existing program and source.
- Every tuition row references a program that exists in the finalized dataset.
- Out-of-scope records are not inserted into `graduate_program`.
- No orphan sources were found.

## Duplicate Checks

No duplicate issues were found in the finalized dataset:

- Duplicate program IDs: none
- Duplicate source IDs: none
- Duplicate source URLs: none
- Duplicate program-source links: none detected in the finalized dataset
- Duplicate tuition rows: none detected
- Duplicate tracks per program: none detected
- Duplicate fee items: none detected
- Duplicate accreditation rows: none detected
- Duplicate shared university-scope facts: none detected

## Tuition Safety

- Tuition rows inserted: 58
- Null tuition rows: 2
- The only null tuition rows are the two Theology records:
  - `uob-theology-master-theology`
  - `uob-theology-phd-theology`
- No other master’s program has null tuition.
- No PhD tuition was invented.
- The Theology tuition gap is an official-source limitation, not a seed defect.

## Key Issues Found

1. Official program URL reuse is intentional and required for UOB.
2. Theology tuition remains unavailable from the reviewed official sources.
3. Several hub/catalog-based programs do not have dedicated individual pages, so the canonical URL reuse is expected.

## Blockers

- None.

## Nice-To-Have Improvements

- Add more dedicated canonical program pages where UOB later publishes them.
- If UOB publishes a theology tuition source later, populate the two null tuition rows.
- If UOB publishes more program-specific pages for hub-based offerings, narrow the URL reuse further.

## Final Assessment

The UOB DB seed can be considered complete for the finalized graduate dataset.

## Recommendation

**APPROVE WITH NOTES**

