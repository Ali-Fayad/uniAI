# LAU Seed QA Report

## Verdict

APPROVE WITH NOTES

## Files Inspected

- `Server/src/main/resources/db/migration/V26__seed_lau_graduate_data.sql`
- `Server/src/main/resources/db/migration/V25__seed_aub_graduate_data.sql`
- `Server/src/main/resources/db/migration/V24__graduate_program_schema_v2.sql`
- `research/lau/programs.json`
- `research/lau/university.json`
- `research/lau/sources.json`
- `research/lau/out_of_scope_programs.json`
- `research/lau/import_report.md`
- `research/lau/final_quality_report.md`

## Summary

The LAU seed migration is structurally sound and consistent with the canonical LAU research files.

- Version/order is correct: `V26` follows `V25` with no naming conflict.
- The migration is Flyway-compatible SQL and compiled successfully with Maven.
- Idempotency is implemented with `ON CONFLICT` or unique-key lookups on all seed tables that require repeat safety.
- All checked enum values satisfy the `V24` constraints.
- The row counts in the migration/report match the canonical LAU datasets.
- No out-of-scope programs are seeded into `graduate_program`.

## Row-Count Comparison

| Item | Expected | Observed | Result |
|---|---:|---:|---|
| Graduate programs | 33 | 33 | Match |
| PhD records | 0 | 0 | Match |
| Tuition rows | 33 | 33 | Match |
| Tracks / concentrations | 28 | 28 | Match |
| Accreditation rows | 10 | 10 | Match |
| Admission requirement rows | 36 | 36 | Match |
| Source records | 56 | 56 | Match |
| Program-source links | 131 | 131 | Match |
| Out-of-scope records skipped | 10 | 10 | Match |

## Constraint / Enum Check

Checked against `V24`:

- `degree_type.code`: `MASTER`, `PHD`
- `delivery_mode`: `ON_CAMPUS`, `ONLINE`
- `thesis_or_non_thesis`: `PROJECT`, `THESIS`, `THESIS_OR_NON_THESIS`, `THESIS_OR_PROJECT`
- `billing_basis`: `PER_CREDIT`
- `scope_level`: `PROGRAM`
- `track_type`: `TRACK`
- `relation_type`: not used by the LAU seed
- `requirement_type`: `GENERAL`, `GRE`, `GMAT`, `ENGLISH`, `EXPERIENCE`, `INTERVIEW`

All observed values are valid under the schema constraints.

## Duplicate Prevention

- `graduate_program`: protected by `UNIQUE (university_id, program_key)`
- `source`: protected by `UNIQUE (university_id, url)`
- `graduate_program_source`: protected by `UNIQUE (program_id, source_id, source_role)`
- `graduate_tuition_rate`: protected by `UNIQUE (university_id, record_key)`
- `graduate_program_track`: protected by `UNIQUE (program_id, track_type, track_name)`
- `graduate_accreditation`: protected by `UNIQUE (university_id, record_key)`
- `university_faculty` / `university_department`: protected by unique natural-key constraints

The seed can be rerun without creating duplicate rows in the seeded entities above.

## Referential Integrity

- Every seeded graduate program resolves to an existing university, faculty, department, degree type, language, and primary source.
- Every program-source link resolves to an existing program and source.
- Every tuition row resolves to an existing program and source.
- Every track row resolves to an existing program.
- Every accreditation row resolves to an existing program and source.
- Admission requirements resolve to existing programs and sources.

No orphan references are introduced by the migration as written.

## Source Traceability

- Every in-scope program has at least one source link.
- Tuition rows link to `lau_fees_2026_2027`.
- All source URLs in `research/lau/sources.json` are official LAU URLs or official LAU-hosted pages/documents.

## Issues Found

None blocking.

## Blockers

None.

## Nice-To-Have Improvements

- Consider adding a dedicated raw-duration field in the schema if future university imports need to preserve human-readable duration text separately from notes.
- If more universities are imported later, a reusable helper migration pattern for faculty/department seeding could reduce repeated SQL.

## Validation

- JSON parse validation: passed for all `research/lau/*.json` inputs.
- Maven compile: passed with `./mvnw -q -DskipTests compile`.
- Local Flyway execution: not possible in this environment because the Docker datasource was unavailable.

## Final Assessment

The LAU DB seed can be considered complete for the canonical in-scope master’s programs.
