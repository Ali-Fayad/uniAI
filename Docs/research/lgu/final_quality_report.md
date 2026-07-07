# LGU Final Quality Report

Date accessed: 2026-07-05

## Summary

- `programs.json` records: 4
- MASTER records: 4
- PHD records: 0
- Out-of-scope records: 2
- Tuition populated: 0
- Tuition null: 4

## Validation

- `research/lgu/programs.json` parses successfully.
- `research/lgu/out_of_scope_programs.json` parses successfully.
- `research/lgu/university.json` parses successfully.
- `research/lgu/sources.json` parses successfully.
- `research/lgu/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate non-null official program URLs were found.
- Every `source_id` in `programs.json` resolves to `research/lgu/sources.json`.
- Every `source_id` in `out_of_scope_programs.json` resolves to `research/lgu/sources.json`.
- All non-null official program URLs belong to official LGU domains. In this pass, all retained program URLs are `null`.
- Shared data is centralized in `university.json`.
- Schema compatibility with V24 is preserved.
- `./mvnw -q -DskipTests compile` passed from `Server/`.

## Ed.D. and DBA

- `Doctor of Education (Ed.D.)` remains out of scope.
- `Doctor of Business Administration (DBA)` remains out of scope.
- No PHD rows were created because the discovery pass did not recover separately detailed official PhD program pages.

## Tuition

- Tuition remains `null` for all 4 in-scope programs.
- This is intentional and reflects the official-source limitation.
- No graduate tuition table or numeric graduate fee schedule was published in the recovered LGU source set.

## Official Source Limitations

- The official Graduate Studies page names the four master programs, but no dedicated graduate program-detail pages were recovered.
- The public Academic Programs catalog page shows only bachelor-level programs.
- No numeric graduate tuition table was found.
- No numeric graduate application or registration fee amounts were published.
- No standalone graduate regulations handbook or catalog was recovered.

## Completeness

- In-scope graduate master programs are fully inventoried at the source-set level available.
- Program-specific enrichment is limited to published descriptive text only.
- Shared admissions, documents, language guidance, fee notes, calendar, financial aid, and accreditation notes are centralized in `university.json`.

## Recommendation

APPROVE WITH NOTES

LGU is ready for V43 seed generation.
