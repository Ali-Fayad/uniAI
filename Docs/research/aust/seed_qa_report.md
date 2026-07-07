# AUST Seed QA Report

## Scope

Reviewed:
- `Server/src/main/resources/db/migration/V35__seed_aust_graduate_data.sql`
- `research/aust/import_report.md`
- `research/aust/programs.json`
- `research/aust/out_of_scope_programs.json`
- `research/aust/sources.json`
- `research/aust/final_quality_report.md`

## Result

- V35 is present after V34 in the migration set.
- The seed covers 17 graduate programs, all `MASTER`.
- No `PHD` rows are present.
- Tuition is populated for all 17 programs.
- No out-of-scope records are present.
- MBA and TOEFL discrepancies remain preserved in notes.
- Official program URL uniqueness is intact.
- Source traceability is preserved.
- The migration follows an idempotent Flyway pattern and avoids duplicate inserts.
- V24 enum/check compatibility is preserved.
- `./mvnw -q -DskipTests compile` passed from `Server/`.

## Count Reconciliation

| Item | import_report.md | seed QA |
|---|---:|---:|
| University rows | 1 | 1 |
| Faculty/school rows | 4 | 4 |
| Department rows | 0 | 0 |
| Degree type rows | 4 | 4 |
| Language rows | 4 | 4 |
| Source rows | 27 | 27 |
| Program rows | 17 | 17 |
| MASTER rows | 17 | 17 |
| PHD rows | 0 | 0 |
| Tuition rows | 17 | 17 |
| Fee item rows | 6 | 6 |
| Admission requirement rows | 2 | 2 |
| Required document rows | 8 | 8 |
| Deadline rows | 3 | 3 |
| Scholarship rows | 1 | 1 |
| Financial aid rows | 1 | 1 |
| Payment plan rows | 2 | 2 |
| Accreditation rows | 0 | 0 |
| Track rows | 2 | 2 |
| Alias rows | 0 | 0 |
| Program-source links | 63 | 63 |
| Out-of-scope skipped | 0 | 0 |

## Checks

- V35 ordered after V34: pass.
- 17 programs seeded: pass.
- 17 MASTER: pass.
- 0 PHD: pass.
- 17 tuition rows: pass.
- 0 out-of-scope rows: pass.
- MBA credit discrepancy preserved in notes: pass.
- TOEFL discrepancy preserved in notes: pass.
- Official program URL uniqueness preserved: pass.
- No broken source references: pass.
- Every program has source links: pass.
- V24 enum/check compatibility: pass.
- Idempotency and duplicate prevention: pass.
- Row counts match `research/aust/import_report.md`: pass.

## Notes

- No department rows were seeded because the official AUST source set did not expose stable department names.
- No accreditation rows were seeded because the official AUST source set did not expose a stable accreditation statement.
- The final structured dataset remains inventory-safe for V35 seeding.

## Verdict

APPROVE WITH NOTES
