# MUBS Graduate Program Inventory Report

Date accessed: 2026-07-04
University: Modern University for Business and Science (MUBS)
Official website: https://www.mubs.edu.lb
Task code: MUBS_INVENTORY_001

## Summary

- MASTER count in `programs.json`: 2
- PHD count in `programs.json`: 0
- Out-of-scope count: 2

## In-scope programs

- Master of Science in Computer Science
- Master of Business Administration (MBA)

## Out-of-scope programs

- Master in Social Work
- MBA in Marketing and Entrepreneurship

## Validation

- `research/mubs/programs.json` parses successfully.
- `research/mubs/out_of_scope_programs.json` parses successfully.
- No duplicate IDs were found.
- Every `source_id` in `programs.json` exists in `research/mubs/sources.json`.
- Every `source_id` in `out_of_scope_programs.json` exists in `research/mubs/sources.json`.
- All non-null `official_program_url` values are official MUBS URLs.
- `programs.json` contains MASTER records only.
- `programs.json` contains zero PHD records.

## Modeling notes

- The current Cardiff MBA offering is modeled as one master program with the officially documented pathway names captured in `concentrations_or_tracks`.
- The MCS record captures the four specialization areas listed on the Computer Science Department page.
- Legacy catalogue-only references were kept out of the in-scope inventory because the discovery pass did not confirm them as current active graduate programs.

## Recommendation

APPROVE WITH NOTES
