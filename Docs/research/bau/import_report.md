# BAU Import Report

## Files Changed

- Server/src/main/resources/db/migration/V30__seed_bau_graduate_data.sql
- research/bau/import_report.md

## Seed Pattern Used

- Flyway idempotent seed migration following the V25-V29 architecture.
- Upserts by university, program_key, record_key, and source URL.
- Canonical graduate schema only; no legacy V23 tables were populated.
- BAU does not require dropping official_program_url uniqueness because the finalized in-scope dataset uses unique URLs.

## Row Counts

- Universities: 1
- Faculties/schools: 10
- Departments: 0
- Degree types: 4
- Languages: 4
- Sources: 25
- Programs: 109
- MASTER: 59
- PHD: 50
- Tuition rows: 109
- Fee item rows: 2
- Admission requirement rows: 7
- Required document rows: 14
- Deadline rows: 1
- Scholarship rows: 3
- Financial aid rows: 1
- Payment plan rows: 0
- Accreditation rows: 1
- Track rows: 0
- Alias rows: 0
- Program-source links: 436
- Out-of-scope rows skipped: 21

## Validation Result

- All research/bau/*.json files parse successfully.
- No duplicate in-scope program IDs.
- No broken source references.
- Tuition count matches the 109 in-scope programs.
- Out-of-scope records were excluded from graduate_program.
- Enum values are compatible with V24.
- Migration follows the V25-V29 idempotent seed pattern.
- ./mvnw -q -DskipTests compile was run from Server and passed.
- Flyway execution was not run because no datasource is configured in this workspace.

## Warnings / Manual Review Notes

- 26 programs have multiple published tuition variants; the seed stores one canonical tuition row per program and preserves the variant details in notes.
- Departments were not seeded because the finalized BAU inventory contains no department-level graduate rows.
- Program-level credits were normalized where a single total was explicit; ambiguous route-only credit strings were preserved in notes.

## Final Recommendation

**APPROVE WITH NOTES**
