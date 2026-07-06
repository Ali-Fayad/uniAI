# TUI Graduate Program Inventory Report

Date accessed: 2026-07-06
University: Tripoli University Institute / University of Tripoli
Official websites: https://ut.edu.lb, https://new.ut.edu.lb
Task code: INVENTORY_TUI_001

## Summary

- MASTER count in `programs.json`: 2
- PHD count in `programs.json`: 2
- Out-of-scope count: 0

## In-scope programs

- Master of Islamic Sharia
- Master of Islamic Studies
- Doctor of Philosophy in Islamic Sharia
- Doctor of Philosophy in Islamic Studies

## Faculty Breakdown

- Faculty of Sharia and Islamic Studies: 2 MASTER, 2 PHD

## Validation

- `research/tui/programs.json` parses successfully.
- `research/tui/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- Every `source_id` in `programs.json` exists in `research/tui/sources.json`.
- All non-null `official_program_url` values are official UT URLs.
- `programs.json` contains MASTER and PHD records only.
- No Bachelor, Diploma, Certificate, Continuing Education, or Professional Training rows were created.

## Modeling Notes

- The Faculty of Sharia and Islamic Studies is the only faculty that produced official graduate program evidence in the captured source set.
- One degree-family record was created for each officially supported graduate award:
  - Master's in Islamic Sharia
  - Master's in Islamic Studies
  - PhD in Islamic Sharia
  - PhD in Islamic Studies
- Concentration and specialization lists were preserved in `concentrations_or_tracks` rather than split into separate degree rows.
- `official_program_url` points to the official faculty/program page because no separate dedicated program-detail pages were captured.

## Recommendation

APPROVE WITH NOTES

The inventory is limited to the officially supported graduate degree families evidenced by the admissions and faculty/program pages. No unsupported graduate programs were inferred from faculty names alone.
