# GU Final Quality Report

Date accessed: 2026-07-06
University: Global University (GU)
Official website: https://www.gu.edu.lb
Task code: FINAL_QA_GU_001

## Scope

- `programs.json` records: 4
- MASTER records: 4
- PHD records: 0
- `out_of_scope_programs.json` records: 0
- Tuition populated: 0
- Tuition null: 4
- Program count consistency across `programs.json`, `program_inventory_report.md`, `enrichment_report.md`, and this final QA report: pass

## Validation Results

- `research/gu/programs.json` parses successfully.
- `research/gu/out_of_scope_programs.json` parses successfully.
- `research/gu/university.json` parses successfully.
- `research/gu/fees_mapping_summary.json` parses successfully.
- `research/gu/sources.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate source IDs were found in `research/gu/sources.json`.
- No duplicate source URLs were found in `research/gu/sources.json`.
- Every source reference in `research/gu/programs.json` resolves to an entry in `research/gu/sources.json`.
- Every source reference in `research/gu/university.json` resolves to an entry in `research/gu/sources.json`.
- Every source reference in `research/gu/fees_mapping_summary.json` resolves to an entry in `research/gu/sources.json`.
- All cited URLs are official GU URLs.
- Tuition is centralized correctly in `research/gu/university.json`.
- Program-level tuition remains null in `research/gu/programs.json`.
- V24 schema compatibility is preserved.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Report Consistency

- `research/gu/program_inventory_report.md` reports 4 in-scope graduate records.
- `research/gu/enrichment_report.md` reports 4 reviewed program records.
- `research/gu/final_quality_report.md` reports 4 program records.
- MASTER count consistency across the three reports and `programs.json`: 4.
- PHD count consistency across the three reports and `programs.json`: 0.

## Tuition / Fees

- Tuition populated count: 0
- Tuition null count: 4
- `research/gu/fees_mapping_summary.json` records 2 mapped tuition rows and 1 unmapped master's program.
- The official graduate tuition remains centralized at the university level, and no program-level tuition was duplicated.

## Shared Data

- Shared admissions, required documents, language guidance, fee structure, payment plans, scholarships, financial aid, academic calendar, graduate regulations, international student notes, and accreditation are centralized in `research/gu/university.json`.
- Unsupported program-level fields remain null rather than inferred.
- The MBA enrichment is limited to the officially supported thesis / non-thesis structure.

## V24 Compatibility

- `degree_type` values are limited to `MASTER`.
- `thesis_or_non_thesis` uses a supported enum-style value where supported and remains null elsewhere.
- `duration`, `credits`, and `official_program_url` are only populated where officially supported.
- No unsupported schema values were introduced.

## Recommendation

APPROVE WITH NOTES
