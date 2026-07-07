# Graduate Seed Runtime Audit

## Scope

Graduate seed migrations audited during the clean replay:

- `Server/src/main/resources/db/migration/V25__seed_aub_graduate_data.sql`
- `Server/src/main/resources/db/migration/V29__seed_uob_graduate_data.sql`
- `Server/src/main/resources/db/migration/V31__seed_ndu_graduate_data.sql`
- `Server/src/main/resources/db/migration/V33__seed_ua_graduate_data.sql`
- `Server/src/main/resources/db/migration/V34__seed_liu_graduate_data.sql`
- `Server/src/main/resources/db/migration/V36__seed_hu_graduate_data.sql`
- `Server/src/main/resources/db/migration/V39__seed_aul_graduate_data.sql`
- `Server/src/main/resources/db/migration/V40__seed_aou_graduate_data.sql`
- `Server/src/main/resources/db/migration/V44__seed_uls_graduate_data.sql`
- `Server/src/main/resources/db/migration/V45__seed_pu_graduate_data.sql`
- `Server/src/main/resources/db/migration/V46__seed_ju_graduate_data.sql`
- `Server/src/main/resources/db/migration/V53__seed_esa_graduate_data.sql`

## Root Causes and Fixes

### V25 / AUB and V44 / ULS official URL uniqueness

The seed chain used the `graduate_program.official_program_url` uniqueness constraint while intentionally reusing hub/catalog URLs for multiple programs.

Fix:

- Added `ALTER TABLE graduate_program DROP CONSTRAINT IF EXISTS uq_graduate_program_university_url;` to the affected seed migrations.
- Preserved `program_key` as the canonical duplicate-prevention key.

### V29 / UOB duplicate admission requirements

The UOB seed migration contained the same `graduate_admission_requirement` program block twice.

The second copy reinserted the following exact record keys:

- `uob-fas-master-political-science-international-affairs:admission_requirements`
- `uob-fas-master-sports-management:admission_requirements`
- `uob-fobm-emba:admission_requirements`
- `uob-fobm-emba:interview_requirement`
- `uob-fobm-emba:experience_requirement`

Because the second copy also joined each row against multiple `source_ids`, it produced duplicate rows for the same `(university_id, record_key)` within one `INSERT ... ON CONFLICT DO UPDATE` command, which PostgreSQL rejects with:

`ON CONFLICT DO UPDATE command cannot affect row a second time`

Fix Applied:

- Removed the duplicated late `UOB_PROG_ADM2` admission-requirement insert block from `V29__seed_uob_graduate_data.sql`.
- Kept the earlier valid `graduate_admission_requirement` insert block intact.
- No UOB records were removed.
- No unrelated constraints were weakened.
- Source traceability and `program_key`-based duplicate protection remain unchanged.

### V31 / NDU schema alignment

The NDU migration referenced `faculty_id` / `department_id` columns in `graduate_program_track`, but V24 defines tracks without those columns.

Fix:

- Reworked the `graduate_program_track` insert to use the V24 column set.
- Qualified ambiguous `notes` references in the NDU program-source insert.

### V33 / UA tuition and source-source alignment

The UA migration had two independent issues:

- `ua_tuition_seed` rows contained an extra leading value, causing a column-count mismatch.
- `graduate_program_source` was being inserted with V24-incompatible `faculty_id` / `department_id` columns.

Fix:

- Removed the accidental extra leading value from the tuition seed rows.
- Switched the program-source insert to the V24 column set.
- Later, aligned the LIU-style source JSON field names used by the UA source seed.

### V34 / LIU source-field mapping

The LIU migration imported source JSON using `title`, but the source registry uses `page_title`.

Fix:

- Mapped `page_title` to the temporary source seed title column.
- Kept all source URLs unchanged.

### V36 / HU source lookup and schema mapping

The HU migration looked up an admissions URL that did not match the official source registry, and later used a document relation column that does not exist in V24.

Fix:

- Replaced the mismatched admissions URL with the official seeded URL.
- Removed the nonexistent `admission_requirement_id` column from `graduate_required_document`.

### V39 / AUL faculty mapping

The AUL program JSON used `faculty` while the temp table expected `faculty_name`.

Fix:

- Mapped `faculty` to `faculty_name` during program seed import.

### V40 / AOU SQL literals and fee basis

The AOU migration had an unescaped apostrophe in a document name and used an unsupported `ANNUAL` fee basis.

Fix:

- Escaped the apostrophe in `bachelor''s`.
- Replaced `ANNUAL` with `PER_YEAR` for the annual NSSF fee.

### V44 / ULS faculty mapping, tuition seed, and fee basis

The ULS migration:

- defaulted faculty types to null even though the table requires a value,
- referenced a missing `uls_tuition_seed` temp table,
- and used unsupported `FLAT_FEE_PLUS_LBP` billing bases.

Fix:

- Defaulted missing faculty types to `FACULTY`.
- Added a missing `uls_tuition_seed` temp table with official 2025-2026 tuition rates.
- Changed mixed-currency fee rows to schema-safe `FLAT_FEE` values while preserving the LBP component in notes.

### V45 / PU duplicate tuition-source rows

The PU migration proposed the same `TUITION` source twice for each program within one `INSERT ... ON CONFLICT DO UPDATE` statement.

Fix:

- Removed the duplicate `TUITION` source rows for MBA and LL.M.
- Kept one canonical tuition-source entry per program.

### V46 / JU fee schema alignment

The JU migration used null currencies for fee rows, and one fee row used unsupported `PER_COURSE`.

Fix:

- Set the fee currencies to `USD` so the rows satisfy the V24 schema.
- Replaced `PER_COURSE` with `FLAT_FEE` for the remedial foreign-language fee.

### V53 / ESA document and source lookup alignment

The ESA migration:

- inserted a non-existent `admission_requirement_id` column into `graduate_required_document`,
- and referenced a solidarity-aid PDF URL that did not match the official source registry.

Fix:

- Removed the nonexistent `admission_requirement_id` column from the document insert.
- Corrected the solidarity-aid PDF URL to the exact seeded source URL.

## Validation

- `./mvnw -q -DskipTests compile`: pass
- Clean temporary PostgreSQL replay:
  - V1 through V55 applied successfully
  - V25 no longer fails on duplicate `official_program_url`
  - V29 now completes successfully after removing the duplicated admission block
  - V31, V33, V34, V36, V39, V40, V44, V45, V46, and V53 were each fixed with the smallest safe idempotent change

## Next Blocker

- None encountered through V55 during the latest clean replay.

## Summary

- The graduate seed chain now replays cleanly through the latest migration on a fresh database.
- The fixes preserved source traceability, avoided data removal, and kept the canonical duplicate-prevention key on `program_key`.
