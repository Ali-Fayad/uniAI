# AUCE Graduate Program Inventory Report

Date accessed: 2026-07-06
University: American University of Culture & Education (AUCE)
Official website: https://www.auce.edu.lb
Task code: INVENTORY_AUCE_001

## Summary

- MASTER count in `programs.json`: 2
- PHD count in `programs.json`: 0
- Out-of-scope count: 0

## In-scope programs

- Master of Business Administration
- Master of Computer Science

## Validation

- `research/auce/programs.json` parses successfully.
- `research/auce/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- Every `source_id` in `programs.json` exists in `research/auce/sources.json`.
- `programs.json` contains MASTER records only.
- `programs.json` contains zero PHD records.
- No official AUCE PhD or doctoral program was found in the provided source set.

## Modeling notes

- The official AUCE source set supports exactly two current master's programs: MBA and Master of Computer Science.
- `official_program_url` is `null` for both records because no dedicated current program page was recovered in the source set.
- Concentration-style data from the Academic Programs page is captured in `concentrations_or_tracks`.
- No graduate out-of-scope degree rows were enumerated from the provided source set, so `out_of_scope_programs.json` is empty.

## Recommendation

APPROVE WITH NOTES

The inventory is limited to the officially supported master's programs and does not invent any PhD offering.
