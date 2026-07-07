# UL Seed QA Report

Date accessed: 2026-07-04

## Verdict

APPROVE WITH NOTES

UL is ready for DB import completion, with the note that Flyway migration execution was not run against a live datasource in this workspace.

## Validation

- `research/ul/*.json` parse: pass
- `./mvnw -q -DskipTests compile`: pass
- `V32__seed_ul_graduate_data.sql` appears after `V31__seed_ndu_graduate_data.sql`: pass
- Program rows seeded: 46
- `MASTER` rows seeded: 43
- `PHD` rows seeded: 3
- Tuition rows seeded: 0
- Shared 745,000 LBP fee seeded only as a university fee item: pass
- Out-of-scope rows skipped: pass
- Hub/catalog URL reuse handled correctly by dropping the UL program URL uniqueness constraint: pass
- Broken source references: none detected
- Every program has at least one source link: pass
- V24 enum/check compatibility: pass for `degree_type`, `delivery_mode`, `thesis_or_non_thesis`, `billing_basis`, `scope_level`, and null tuition handling
- Duplicate prevention/idempotency: pass
- Source URL uniqueness in `research/ul/sources.json`: pass
- Row counts match `research/ul/import_report.md`: pass
- Flyway runtime execution: not run here because no datasource is available in this workspace

## Count Check

| Item | Count |
|---|---:|
| University rows | 1 |
| Faculty/school rows | 17 |
| Department rows | 0 |
| Degree type rows | 4 |
| Language rows | 4 |
| Source rows | 62 |
| Program rows | 46 |
| MASTER rows | 43 |
| PHD rows | 3 |
| Tuition rows | 0 |
| Fee item rows | 2 |
| Admission requirement rows | 2 |
| Required document rows | 9 |
| Deadline rows | 1 |
| Scholarship rows | 0 |
| Financial aid rows | 1 |
| Payment plan rows | 1 |
| Accreditation rows | 4 |
| Track rows | 34 |
| Alias rows | 0 |
| Program-source links | 106 |
| Out-of-scope skipped | 2 |

## Notes

- The 745,000 LBP amount is modeled correctly as a shared university enrollment/registration fee item, not as program tuition.
- UL’s official graduate pages reuse hub/catalog URLs across multiple graduate programs, so the seed migration intentionally drops `uq_graduate_program_university_url` before inserting the UL inventory.
- The imported shared data stays centralized in `university.json`, and the program tuition fields remain null by design.
- No additional graduate scholarship catalog was published in the reviewed official UL sources; only doctoral grant support is seeded as financial aid.

## Recommendation

APPROVE WITH NOTES

