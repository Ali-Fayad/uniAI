# RHU Graduate Program Inventory Report

Date accessed: 2026-07-04

## Summary

- Discovered master's programs: 7 official degree records
- Discovered MBA emphases consolidated into the MBA record: 2
- MASTER records in `programs.json`: 7
- PHD records in `programs.json`: 0
- Out-of-scope records: 0

## Faculty Breakdown

- College of Business Administration: 1
- College of Engineering: 6

## Validation

- `research/rhu/programs.json` parses successfully.
- `research/rhu/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate non-null official program URLs were found.
- Every `source_id` in `programs.json` exists in `research/rhu/sources.json`.
- All non-null official program URLs belong to official RHU domains.
- `programs.json` contains MASTER records only.
- `programs.json` contains zero PHD records, per the discovery rule.

## Duplicate URL Justification

- None. Records without a distinct public program page keep `official_program_url` set to `null` rather than reusing the catalog or graduate programs page URL.

## Official Discrepancies

- MBA concentration naming:
  - RHU publishes the MBA with General Business Management and Oil and Gas Management emphases.
  - This inventory keeps one MBA record and records the emphases in notes rather than splitting them into separate degree rows.
- No PhD program:
  - No official RHU PhD / doctorate program was found in the inspected source set.

## Remaining Official Ambiguities

- Several RHU engineering master's programs do not have distinct public program pages recovered in this pass:
  - Biomedical Engineering
  - Civil and Environmental Engineering
  - Electrical Engineering
  - Mechatronics Engineering
- The MBA program is officially presented with emphases rather than separate graduate degree families, so the inventory models it as one program with concentration notes.
- Faculty and department labels are not consistently exposed on the public pages, so `department` is left `null` where no official label was recovered.

## Out-of-Scope Summary

- No out-of-scope program records were discovered in the provided RHU graduate source set for this pass.
- Per the task scope, no Bachelor, Diploma, Certificate, Continuing Education, Professional Program, News, or Events records were created.
- No PhD records were created because the discovery report found no official RHU PhD program.

## Recommendation

APPROVE WITH NOTES
