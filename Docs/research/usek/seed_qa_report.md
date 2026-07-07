# USEK Seed QA Report

## Verdict

`APPROVE WITH NOTES`

## Migration Order

- `V28__seed_usek_graduate_data.sql` is correctly ordered after `V27__seed_usj_graduate_data.sql`.
- The filename is valid Flyway naming: versioned migration with the next sequential version number.

## Constraint Change Review

### Unique constraint dropped

The migration drops:

- `uq_graduate_program_university_url`

### Why it was blocking USEK

USEK intentionally reuses hub and faculty/catalog URLs across multiple graduate records. Keeping the unique constraint on `(university_id, official_program_url)` would prevent a valid seed import for those documented hub-page reuse cases.

### Consistency with USJ

This is consistent with the USJ seed strategy, which also dropped the same unique constraint for the same reason: documented reuse of hub/catalog URLs across multiple graduate programs.

### Remaining uniqueness protection

Program uniqueness is still protected by:

- `graduate_program.university_id + program_key`

Source URL uniqueness remains protected by:

- `source.university_id + url`

Child-table uniqueness remains protected by the schema constraints in `V24`:

- `graduate_program_source (program_id, source_id, source_role)`
- `graduate_tuition_rate (university_id, record_key)`
- `graduate_program_track (program_id, track_type, track_name)`
- `graduate_accreditation (university_id, record_key)`
- `graduate_required_document (university_id, record_key)`
- `graduate_admission_requirement (university_id, record_key)`
- `graduate_admission_deadline (university_id, record_key)`
- `graduate_scholarship (university_id, record_key)`
- `graduate_financial_aid (university_id, record_key)`
- `graduate_payment_plan (university_id, record_key)`

## SQL Safety

- PostgreSQL syntax: reviewed and consistent with prior seed migrations.
- Flyway compatibility: the migration uses the same idempotent seed pattern as `V25`, `V26`, and `V27`.
- Idempotency: insert-or-reuse logic is used throughout via unique keys and `ON CONFLICT`.

### Enum / check compatibility

Validated against `V24`:

- `degree_type` values: `MASTER`, `PHD`, `DIPLOMA`, `CERTIFICATE`
- `delivery_mode` values: null only in the current dataset; schema-safe
- `language` values: `ENGLISH`, `FRENCH`, `ARABIC`, `MULTILINGUAL`
- `thesis_or_non_thesis` values: `THESIS`, `THESIS_OR_PROJECT`, `PROJECT`, null
- `tuition.billing_basis` values: `PER_CREDIT`, `PER_SEMESTER`, `FLAT_FEE`, `PER_ACADEMIC_YEAR`
- requirement enums: `GENERAL`, `ENGLISH`, `PREREQUISITE`, `INTERVIEW`

No enum/check mismatch was found.

## Row-Count Consistency

The migration matches the finalized USEK dataset and import report:

- Universities inserted or reused: `1`
- Faculties / schools: `8`
- Departments: `15`
- Degree types: `4`
- Languages: `4`
- Source rows: `45`
- Graduate programs: `80`
- MASTER programs: `59`
- PHD programs: `21`
- Tuition rows: `59`
- Admission requirement rows: `4`
- Required document rows: `22`
- Deadline rows: `7`
- Scholarship rows: `3`
- Financial aid rows: `3`
- Payment plan rows: `2`
- Accreditation rows: `1`
- Track rows: `11`
- Alias rows: `0`
- Program-source links: `168`
- Out-of-scope rows skipped: `4`

## Referential Integrity

All seeded child rows reference valid parent rows by construction:

- each graduate program joins to a seeded university, faculty, degree type, and source
- each program-source link joins to an existing graduate program and source
- each tuition row joins to an existing graduate program and tuition source
- each admission requirement, required document, deadline, scholarship, aid, payment plan, and accreditation row joins to an existing source
- tracks join to existing graduate programs and sources

No orphan reference pattern was identified in the seed logic.

## Duplicate Prevention

Reviewed duplicate guards:

- duplicate `graduate_program.program_key`: prevented by `uq_graduate_program_university_program_key`
- duplicate source URLs: prevented by `source` unique `(university_id, url)`
- duplicate program-source links: prevented by `uq_graduate_program_source`
- duplicate tuition rows: prevented by `uq_graduate_tuition_rate_record_key`
- duplicate tracks: prevented by `uq_graduate_program_track`
- duplicate accreditation rows: prevented by `record_key`
- duplicate required documents, deadlines, scholarships, aid, payment plans: prevented by their respective `record_key` unique constraints

No duplicate insertion path was identified.

## Scope Safety

- `research/usek/out_of_scope_programs.json` is not inserted into `graduate_program`
- certificates, diplomas, and other out-of-scope records are excluded
- exactly `80` in-scope graduate programs are seeded

## Tuition Safety

- Every MASTER program has tuition
- Every tuition row maps to an existing MASTER program
- All 21 null tuition values belong to PHD programs
- Doctoral tuition is absent because the official fee source used for this pass does not publish doctoral tuition

## Issues Found

No blocking issues were found.

## Blockers

None.

## Nice-to-Have Improvements

- If USEK later publishes a doctoral tuition table, add it as a new tuition row set without changing the current program inventory.
- If USEK later publishes unique per-program pages for more hub-based entries, those can be added as optional source refinements.

## Conclusion

The USEK DB seed can be considered complete for the finalized graduate dataset.

Database execution was not performed in this workspace because no datasource is configured here.
