# UOB Import Report

## Files Changed

- Server/src/main/resources/db/migration/V29__seed_uob_graduate_data.sql
- research/uob/import_report.md

## Seed Mechanism Used

- Flyway seed migration following the established V25–V28 pattern.
- Uses idempotent upserts keyed by university, program_key, record_key, and unique source URLs.
- Drops the graduate program URL uniqueness constraint to allow intentional hub/catalog URL reuse.

## Row Counts

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

## Important Implementation Notes

- Tuition is seeded at the program level for every program with official tuition, matching the finalized JSON dataset.
- The only null tuition rows are the two Saint John of Damascus Institute of Theology records, because no official doctoral or theological tuition figure was published in the reviewed UOB fee source.
- Shared admissions, documents, deadlines, scholarships, aid, payment policy, and accreditation are centralized at university scope rather than duplicated across program rows.
- The university fee table is also imported as shared fee-item rows.
- Official program URL reuse is intentional for ALBA, FAS, and the theology hub/catalog pages; the migration removes the URL uniqueness constraint exactly as done for USJ and USEK.
- No aliases were present in the finalized dataset.
- DB execution was not performed in this workspace because no datasource is configured.

## Validation

- All research/uob/*.json files parse successfully.
- ./mvnw -q -DskipTests compile passed in Server/.

## Recommendation

- APPROVE WITH NOTES
