# LGU Graduate Program Inventory Report

Date accessed: 2026-07-05

## Summary

- Discovered master's programs: 4 official degree records
- Discovered doctoral programs excluded from the requested scope: 2 official degree records
- MASTER records in `programs.json`: 4
- PHD records in `programs.json`: 0
- Out-of-scope records: 2

## Faculty Breakdown

- Faculty of Business and Insurance: 1
- Faculty of Arts and Education: 1
- Faculty of Public Health: 1
- Unassigned / not explicitly recovered in the public graduate evidence: 1

## Validation

- `research/lgu/programs.json` parses successfully.
- `research/lgu/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate non-null official program URLs were found.
- Every `source_id` in `programs.json` exists in `research/lgu/sources.json`.
- Every `source_id` in `out_of_scope_programs.json` exists in `research/lgu/sources.json`.
- All non-null official program URLs belong to official LGU domains. In this pass, all retained program URLs are `null`.
- `programs.json` contains MASTER records only.
- `programs.json` contains zero PHD records, per the discovery rule.

## Duplicate URL Justification

- None. No distinct graduate program-detail URLs were recovered during this pass, so `official_program_url` is set to `null` rather than reusing the generic graduate studies or academics pages.

## Official Discrepancies

- Graduate studies page vs. public programs catalog:
  - The Graduate Studies page names 4 master programs and 3 doctoral program labels.
  - The public Academic Programs catalog page shows only 11 Bachelor-level programs.
- Doctoral naming gap:
  - LGU publicly names `Doctor of Philosophy (Ph.D.) in various disciplines`, `Doctor of Education (Ed.D.)`, and `Doctor of Business Administration (DBA)`.
  - No standalone PhD program-detail pages, curricula, or doctoral degree pages were recovered in this pass, so no PHD rows were created.

## Remaining Official Ambiguities

- The Master of Science in Engineering record is officially named, but no dedicated public detail page or faculty assignment was recovered in this pass.
- LGU does not expose graduate tuition tables or graduate regulations in the recovered source set.
- The graduate page is a lightweight listing page rather than a full graduate catalog, so program URLs remain null.
- The doctoral section appears to be an umbrella list rather than a set of separately detailed public doctoral pages.

## Out-of-Scope Summary

- Doctor of Education (Ed.D.)
- Doctor of Business Administration (DBA)

These were excluded from `programs.json` because the task scope is limited to MASTER and PHD. No PhD rows were created because the discovery report did not provide sufficiently specific official PhD program detail to avoid inference.

## Recommendation

APPROVE WITH NOTES
