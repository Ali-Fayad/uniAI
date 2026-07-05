# MEU Graduate Program Inventory Report

Date accessed: 2026-07-05
University: Middle East University (MEU)
Official website: https://meu.edu.lb
Task code: MEU_INVENTORY_001

## Summary

- MASTER count in `programs.json`: 4
- PHD count in `programs.json`: 0
- Out-of-scope count: 0

## In-scope programs

- Master of Business Administration
- Master of Arts in Education
- Master of Arts in Islamic Studies
- Master of Arts in Teaching

## Validation

- `research/meu/programs.json` parses successfully.
- `research/meu/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- Every `source_id` in `programs.json` exists in `research/meu/sources.json`.
- All non-null `official_program_url` values are official MEU URLs.
- `programs.json` contains MASTER records only.
- `programs.json` contains zero PHD records.
- No official graduate PhD program was found in the provided MEU source set.

## Modeling notes

- Cardiff-style concentration data is not relevant for MEU beyond the MBA and MA Education programs, where the official pages explicitly list concentrations/tracks.
- MA Islamic Studies is retained as a single master record with its official Arabic prerequisite note and 44-credit structure.
- MA Teaching is retained as a single master record with no invented concentration split.
- The provided source set did not surface any official graduate programs outside MASTER scope, so `out_of_scope_programs.json` is empty.

## Recommendation

APPROVE WITH NOTES
