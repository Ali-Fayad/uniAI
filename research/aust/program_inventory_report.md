# AUST Graduate Program Inventory Report

Date accessed: 2026-07-04

## Summary

- Discovered master's programs: 17
- MASTER records in `programs.json`: 17
- PHD records in `programs.json`: 0
- Out-of-scope records: 0

## Faculty Breakdown

- Faculty of Arts and Sciences: 3
- Faculty of Business and Economics: 8
- Faculty of Engineering: 3
- Faculty of Health Sciences: 3

## Validation

- `research/aust/programs.json` parses successfully.
- `research/aust/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate non-null official program URLs were found.
- Every `source_id` in `programs.json` exists in `research/aust/sources.json`.
- All non-null official program URLs belong to official AUST domains.
- `programs.json` contains MASTER records only.
- `programs.json` contains zero PHD records, per the discovery rule.

## Duplicate URL Justification

- None. Records without a distinct public program page keep `official_program_url` set to `null` rather than reusing the graduate instructions URL.

## Official Discrepancies

- MBA credit count discrepancy:
  - Official blended MBA brochure: 36 credits
  - Current admissions/program page: 39 credits
  - Inventory follows the current admissions/program page and records the brochure discrepancy in the MBA notes.
- English requirement discrepancy:
  - One official source states TOEFL 575
  - Another official source states TOEFL 600
  - This inventory does not resolve the discrepancy because language requirements are out of scope for the inventory pass.

## Remaining Official Ambiguities

- Several Business and Economics tracks are listed in the graduate application instructions but do not have distinct public program pages recovered in this pass:
  - Accounting
  - Economics
  - Hospitality Management
  - Management Information Systems
  - Marketing
- Several Engineering offerings are listed in the graduate application instructions without a distinct public program page recovered in this pass:
  - Computer & Communications Engineering
  - Biomedical Engineering
- Health Sciences Biotechnology appears as one program with track names in official materials; the recovered public page highlights the Forensic Science track while discovery notes also record DNA Technologies.
- Faculty and department labels are not consistently exposed on the public program pages, so `department` is left `null` where no official label was recovered.

## Out-of-Scope Summary

- No out-of-scope program records were discovered in the provided AUST source set for this pass.
- Per the task scope, no Bachelor, Diploma, Certificate, Continuing Education, Professional Doctorate, News, or Events records were created.
- No PhD records were created because the discovery report found no official AUST PhD program.

## Recommendation

APPROVE WITH NOTES
