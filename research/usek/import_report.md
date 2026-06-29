# USEK Graduate Import Report

## Files Changed

- `Server/src/main/resources/db/migration/V28__seed_usek_graduate_data.sql`
- `research/usek/import_report.md`

## Seed Mechanism

The USEK graduate dataset was prepared as an idempotent Flyway seed migration aligned with the canonical graduate schema introduced in `V24`.

The migration:

- reuses or inserts the USEK university row
- seeds faculties and departments
- seeds degree types and languages
- seeds all official source rows from `research/usek/sources.json`
- seeds all 80 in-scope graduate programs from `research/usek/programs.json`
- seeds program-source links
- seeds program tracks
- seeds tuition rows for all 59 master’s programs
- seeds university-level admissions, documents, deadlines, scholarships, financial aid, payment plans, fees, and accreditation from `research/usek/university.json`
- skips `research/usek/out_of_scope_programs.json`
- drops the `uq_graduate_program_university_url` constraint so documented hub-page reuse remains valid

## Expected Row Counts

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
- Skipped out-of-scope rows: `4`

## Tuition Summary

USEK publishes a single official graduate fee schedule for second-cycle / master’s study. The seed applies program-level per-credit tuition for every master’s program that has an official fee mapping.

- Tuition rows inserted: `59`
- Master’s programs without tuition: `0`
- Doctoral programs without tuition: `21`

Doctoral tuition remains null by design because the official fee source used for this pass does not publish a doctoral tuition table.

## Hub / Catalog URL Reuse

USEK intentionally reuses several hub or faculty URLs across multiple graduate records. The migration therefore removes the unique constraint on `(university_id, official_program_url)`.

Documented reuse groups include:

- Faculty of Arts and Sciences hub pages
- Faculty of Sciences graduate hub pages
- School of Law program hub pages
- School of Business hub pages
- School of Music doctoral hub page

This reuse is documented in `research/usek/inventory_verification_report.md` and is not treated as a defect.

## Validation Results

- `research/usek/programs.json` parses successfully.
- `research/usek/out_of_scope_programs.json` parses successfully.
- `research/usek/university.json` parses successfully.
- `research/usek/sources.json` parses successfully.
- `research/usek/fees_mapping_summary.json` parses successfully.
- `research/usek/final_quality_summary.json` parses successfully.
- Program IDs remain unchanged and unique.
- Source IDs remain unique.
- Source URLs remain unique.
- Tuition mapping is internally consistent with the finalized USEK dataset.
- All null tuition values belong to doctorate / PhD records.
- `./mvnw -q -DskipTests compile` passes in `Server/`.

## Warnings / Manual Review Items

- No official USEK doctoral tuition schedule was published in the fee source used for this pass, so the 21 doctoral tuition values remain null.
- Several graduate records intentionally reuse hub pages instead of unique program URLs, which is acceptable for this import because it was documented during discovery and verification.

## Import Readiness

The USEK graduate seed is ready for database execution.

Database execution was not performed in this workspace because no datasource is configured here.
