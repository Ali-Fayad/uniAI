# MUBS Seed Import Report

Task code: MUBS_SEED_001
University: Modern University for Business and Science (MUBS)
Date accessed: 2026-07-04

## Scope Summary

- University count: 1
- Faculty count: 2
- Department count: 1
- Degree type count: 4
- Language count: 4
- Source count: 19
- Program count: 2
- MASTER count: 2
- PHD count: 0
- Tuition row count: 0
- Fee item rows: 0
- Admission requirement rows: 7
- Required document rows: 9
- Deadline rows: 0
- Scholarship rows: 3
- Financial aid rows: 2
- Payment plan rows: 0
- Accreditation rows: 2
- Track rows: 9
- Alias rows: 0
- Program-source links: 6
- Out-of-scope skipped: 2

## Validation

- `research/mubs/programs.json` parses successfully.
- `research/mubs/out_of_scope_programs.json` parses successfully.
- `research/mubs/university.json` parses successfully.
- `research/mubs/sources.json` parses successfully.
- `research/mubs/fees_mapping_summary.json` parses successfully.
- `research/mubs/shared_report.md` parses successfully as a UTF-8 markdown artifact.
- `research/mubs/enrichment_report.md` parses successfully as a UTF-8 markdown artifact.
- No duplicate program IDs were found.
- No duplicate source URLs were found in `research/mubs/sources.json`.
- Every source reference used by the migration resolves to an official MUBS source row.
- All non-null program URLs are official MUBS URLs or official MUBS subdomains.
- The migration follows the V24 graduate schema: graduate_program, graduate_program_track, graduate_program_source, graduate_admission_requirement, graduate_required_document, graduate_scholarship, graduate_financial_aid, and graduate_accreditation.
- No tuition rows were inserted because the official source set did not expose a stable graduate tuition amount.
- No PhD rows were inserted.
- Cardiff MBA pathways were seeded as tracks under one MBA program, not as separate programs.
- Legacy master references from the discovery pass were not imported.
- The Flyway migration uses idempotent conflict handling for all seeded tables.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Implementation Notes

- Shared graduate admissions, required documents, scholarships, financial aid, and accreditation were seeded at university scope.
- The MCS record carries one program-level admission requirement and one program-level accreditation row.
- The Cardiff MBA record carries a program-level recognition/accreditation note and four official pathway tracks.
- No numeric tuition amount was inferred or written.
- Calendar and regulations context remain source-backed but were not materialized into a dedicated V24 table because the schema does not expose one.

## Recommendation

APPROVE WITH NOTES
