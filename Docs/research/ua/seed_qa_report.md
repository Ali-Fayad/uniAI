# UA Seed QA Report

## Findings
- V33 follows V32 in migration order: pass
- Program count: 21
- MASTER count: 21
- PHD count: 0
- Tuition rows: 19
- Theology tuition nulls: 2 (expected 2)
- Out-of-scope rows skipped: 9
- Every program has source links: pass
- Every source reference resolves: pass
- Official UA URLs only: pass
- Duplicate program IDs: 0
- Duplicate official_program_url values: 0
- V24 schema compatibility: pass
- Idempotent Flyway pattern: pass
- Row counts match import_report.md: pass
- Flyway runtime execution: not run here because no datasource is available in this workspace

## Notes
- Program tuition is intentionally null for the two Theology master's programs only.
- Shared admissions, required documents, deadlines, and fee items are centralized at university scope.
- No out-of-scope records were imported into programs.json.

## Recommendation
APPROVE WITH NOTES
