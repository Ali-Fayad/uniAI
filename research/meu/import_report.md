# MEU Seed Import Report

Task code: `MEU_IMPORT_001`
University: Middle East University (MEU)
Date accessed: 2026-07-05

## Scope Summary

- University count: 1
- Faculty count: 4
- Department count: 3
- Degree type count: 4
- Language count: 2
- Source count: 17
- Program count: 4
- MASTER count: 4
- PHD count: 0
- Tuition row count: 4
- Fee item rows: 11
- Admission requirement rows: 6
- Required document rows: 9
- Deadline rows: 4
- Scholarship rows: 2
- Financial aid rows: 3
- Payment plan rows: 1
- Accreditation rows: 1
- Track rows: 6
- Alias rows: 4
- Program-source links: 21
- Out-of-scope skipped: 0

## Validation

- `research/meu/programs.json` parses successfully.
- `research/meu/out_of_scope_programs.json` parses successfully.
- `research/meu/university.json` parses successfully.
- `research/meu/sources.json` parses successfully.
- `research/meu/final_quality_summary.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate source IDs were found in `research/meu/sources.json`.
- No duplicate source URLs were found in `research/meu/sources.json`.
- All non-null `official_program_url` values are official MEU URLs.
- Every source reference used by the migration resolves to an official MEU source row.
- V24 enum compatibility was preserved.
- The migration follows the idempotent `ON CONFLICT` pattern used in prior university seed files.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Import Notes

- Tuition is seeded from the official MEU shared graduate fee schedule and stored at program scope for the four master records.
- The official graduate tuition amount used is USD 305 per credit.
- The MBA business-program fee is captured separately as two fee items, one for the semester amount and one for the summer-session amount.
- Required documents and admissions rules were seeded at university scope because the official catalog publishes them as shared graduate rules.
- MA Islamic Studies keeps the official 44-credit structure, on-campus delivery, and Arabic prerequisite condition.
- No inferred program-language values were written. All four program `language` fields remain `NULL` because the reviewed MEU sources did not explicitly publish a per-program language field.
- The catalog index source was retained in the seed through faculty/department traceability.
- The homepage accreditation mention is modeled as a generic university-level accreditation row because the accessible official source excerpt does not enumerate the accrediting bodies.

## Recommendation

APPROVE WITH NOTES
