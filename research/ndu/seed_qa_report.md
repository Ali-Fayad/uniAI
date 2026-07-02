# NDU Seed QA Report

Task code: `NDU_SEED_QA_001`  
University: Notre Dame University-Louaize (NDU)  
Date accessed: 2026-07-02

## Verdict

**APPROVE WITH NOTES**

NDU seed migration V31 is structurally ready for import, with one operational caveat: Flyway was not executed against a live datasource in this workspace, so runtime execution remains unconfirmed here.

## Checks

| Check | Result | Notes |
|---|---|---|
| V31 ordered after V30 | PASS | `V31__seed_ndu_graduate_data.sql` sorts after `V30__seed_bau_graduate_data.sql`. |
| No PhD rows seeded | PASS | Finalized dataset has 0 PhD programs. |
| 29 MASTER programs seeded | PASS | `programs.json` contains 29 master’s records. |
| 2 out-of-scope diplomas skipped | PASS | `out_of_scope_programs.json` contains 2 records and none are imported. |
| 29 tuition rows present | PASS | One tuition row per master’s program. |
| Every program has source links | PASS | All 29 programs have `source_ids`. |
| Every tuition row maps to an existing program/source | PASS | Tuition rows reference valid program records and official NDU tuition source IDs. |
| Enum/check compatibility with V24 | PASS | `degree_type`, `delivery_mode`, `thesis_or_non_thesis`, `tuition.billing_basis`, and `language` conform to V24 constraints. |
| Idempotency and duplicate prevention | PASS | Migration uses `WHERE NOT EXISTS` and `ON CONFLICT DO UPDATE`; no duplicate IDs or URLs were found. |
| Source URL uniqueness | PASS | `sources.json` has 18 unique URLs. |
| `official_program_url` uniqueness | PASS | No duplicate program URLs exist in the finalized dataset. |
| Row counts match `import_report.md` | PASS | All checked counts match the import report. |

## Mechanical Validation

- JSON parse of all `research/ndu/*.json` files: pass
- Maven compile: pass with `./mvnw -q -DskipTests compile`
- Flyway runtime execution: not run here because no live datasource is configured in this workspace

## Count Crosswalk

| Item | Dataset / Report Count |
|---|---:|
| Programs | 29 |
| MASTER programs | 29 |
| PHD programs | 0 |
| Out-of-scope records | 2 |
| Tuition rows | 29 |
| Fee item rows | 13 |
| Admission requirement rows | 41 |
| Required document rows | 6 |
| Deadline rows | 6 |
| Scholarship rows | 1 |
| Financial aid rows | 2 |
| Payment plan rows | 1 |
| Accreditation rows | 5 |
| Track rows | 10 |
| Alias rows | 0 |
| Program-source links | 116 |

## Source And URL Notes

- Program source references resolve cleanly against `sources.json`.
- No duplicate source IDs or duplicate source URLs were found.
- No duplicate `official_program_url` values were found.
- The finalized dataset contains no intentional URL reuse.

## Implementation Notes

- The migration seeds university, faculties, departments, degree types, languages, sources, programs, tuition, fee items, admission requirements, required documents, deadlines, scholarships, financial aid, payment plans, accreditation, tracks, and program-source links.
- Shared university-level admissions and financial policy data remain centralized in `university.json` and are seeded at university scope.
- The two out-of-scope diploma records are not imported.
- The migration is written in the same idempotent pattern used by the earlier university seed migrations.

## Recommendation

**APPROVE WITH NOTES**

NDU can proceed to import, subject to executing the migration in an environment with a configured datasource.
