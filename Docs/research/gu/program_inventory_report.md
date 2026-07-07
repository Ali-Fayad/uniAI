# GU Graduate Program Inventory Report

Date accessed: 2026-07-06
University: Global University (GU)
Official website: https://www.gu.edu.lb
Task code: INVENTORY_GU_001

## Summary

- MASTER records in `programs.json`: 4
- PHD records in `programs.json`: 0
- Out-of-scope records: 0
- Total in-scope graduate records: 4

## In-Scope Programs

- Master of Business Administration (MBA)
- Master of Education
- Master in Arabic Language and Literature
- Master in Islamic Studies

## Out-of-Scope Programs

- None captured.

## Validation

- `research/gu/programs.json` parses successfully.
- `research/gu/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- Every `source_id` in `programs.json` exists in `research/gu/sources.json`.
- Every `source_id` in `out_of_scope_programs.json` exists in `research/gu/sources.json`.
- All non-null `official_program_url` values are official GU URLs.
- `programs.json` contains only MASTER records.
- `programs.json` contains zero PHD records.

## Modeling Notes

- GU has official master-level evidence on the Academics page, department pages, application page, catalogue PDF, contract sheet, financial policies, and accreditation/equivalence page.
- No official PhD program page, PhD admissions page, PhD catalog section, or official PhD PDF was found in the discovery report.
- The financial policies page contains a tuition row for "PHD Arabic", but discovery explicitly ruled that out as insufficient for a current PhD program record.
- The Islamic Studies evidence is modeled as one master record with track names preserved in `concentrations_or_tracks`; the tracks are not split into separate degrees.
- No dedicated program page was recovered for Islamic Studies, so `official_program_url` is null.

## Recommendation

APPROVE WITH NOTES
