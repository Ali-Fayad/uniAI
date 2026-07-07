# BAU Seed QA Report

## Verdict

**APPROVE WITH NOTES**

## Scope Reviewed

- `Server/src/main/resources/db/migration/V24__graduate_program_schema_v2.sql`
- `Server/src/main/resources/db/migration/V25__seed_aub_graduate_data.sql`
- `Server/src/main/resources/db/migration/V26__seed_lau_graduate_data.sql`
- `Server/src/main/resources/db/migration/V27__seed_usj_graduate_data.sql`
- `Server/src/main/resources/db/migration/V28__seed_usek_graduate_data.sql`
- `Server/src/main/resources/db/migration/V29__seed_uob_graduate_data.sql`
- `Server/src/main/resources/db/migration/V30__seed_bau_graduate_data.sql`
- `research/bau/programs.json`
- `research/bau/out_of_scope_programs.json`
- `research/bau/sources.json`
- `research/bau/fees_mapping_summary.json`
- `research/bau/import_report.md`
- `research/bau/final_acceptance_report.md`
- `research/bau/scope_fix_report.md`

## Migration Order

- `V30__seed_bau_graduate_data.sql` is correctly ordered after `V29__seed_uob_graduate_data.sql`.
- Naming is Flyway-compatible and follows the established seed sequence.

## Constraint Review

- `V30` does **not** drop `uq_graduate_program_university_url`.
- That is correct for BAU because the finalized in-scope dataset uses unique official program URLs.
- Program uniqueness remains protected by `graduate_program.program_key`.
- Source URL uniqueness remains protected in `source`.
- No additional uniqueness guarantees were weakened.

## Schema / Enum Compatibility

- Degree types used in in-scope BAU programs are only `MASTER` and `PHD`.
- V24 enum constraints remain compatible with the generated seed.
- The seed uses the canonical graduate schema only.
- Department-level rows were not seeded because the finalized BAU inventory contains no department-level graduate records.

## Row Count Comparison

### Expected / Verified

- Universities: `1`
- Faculties/schools: `10`
- Departments: `0`
- Degree types: `4`
- Languages: `4`
- Sources: `25`
- Programs: `109`
- MASTER: `59`
- PHD: `50`
- Tuition rows: `109`
- Fee item rows: `2`
- Admission requirement rows: `7`
- Required document rows: `14`
- Deadline rows: `1`
- Scholarship rows: `3`
- Financial aid rows: `1`
- Payment plan rows: `0`
- Accreditation rows: `1`
- Track rows: `0`
- Alias rows: `0`
- Program-source links: `436`
- Out-of-scope rows skipped: `21`

## Integrity Checks

- Every in-scope program has at least one source.
- Every source reference resolves to a known source record.
- Every tuition row maps to an existing program and official BAU source.
- All 21 out-of-scope records are excluded from `graduate_program`.
- No duplicate program IDs were found.
- No duplicate source IDs were found.
- No duplicate source URLs were found.
- No duplicate program-source links were found.
- No duplicate tuition rows were found.
- No duplicate fee items were found.
- No duplicate shared university facts were found.

## PhD Review

- All 50 PhD programs are legitimate doctoral offerings from the finalized BAU inventory.
- They are not accidental tracks or routes promoted into standalone programs.
- The BAU doctoral entries intentionally mirror the official catalog structure and tuition table.

## Tuition Notes

- Tuition coverage is complete for all 109 in-scope programs.
- 26 programs have multiple published tuition variants in the BAU fee PDF.
- The seed stores one canonical tuition row per program and preserves variant details in `notes`.
- No tuition rows are missing.

## Important Implementation Notes

- The migration follows the idempotent V25-V29 seed pattern.
- The BAU seed preserves source traceability through program-source links.
- No departments were seeded because none were present in the finalized BAU graduate inventory.
- The seed does not require an `official_program_url` uniqueness drop.
- Flyway execution was not run here because no datasource is configured in this workspace.

## Blockers

- None.

## Nice-to-Have Improvements

- Add richer program-level notes for the few BAU records whose credits/duration were preserved as raw text.
- If a future BAU pass introduces department-level graduate rows, seed departments explicitly.

## Final Recommendation

**APPROVE WITH NOTES**
