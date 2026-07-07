# NDU Graduate Program Inventory Report

Date accessed: 2026-06-29

## Summary

- Total programs: 29
- MASTER count: 29
- PHD count: 0
- Out-of-scope count: 2

## Validation

- `research/ndu/programs.json` parses successfully.
- `research/ndu/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate official program URLs were found.
- Every program references at least one existing source ID from `research/ndu/sources.json`.
- All official program URLs belong to `ndu.edu.lb`.
- `programs.json` contains MASTER records only.
- `out_of_scope_programs.json` contains graduate-related non-master records only.

## Duplicate URL Groups

- None.

## Intentional URL Reuse

- None.

## Inventory Notes

- The official Degree Programs page surfaced 29 master-level records once the separate MS Business Strategy page was counted as its own official program page.
- The separate Astrophysics candidate mentioned in discovery notes was not added because no confirmed official source URL was recovered in the current source set.
- Two Teaching Diploma entries were moved to `out_of_scope_programs.json` because they are graduate-facing but are not MASTER programs.
- Tuition values were normalized from the official graduate tuition table by category:
  - Architecture: USD 670 per credit
  - Business: USD 635 per credit
  - Engineering: USD 765 per credit
  - Computer Sciences: USD 670 per credit
  - All Others: USD 590 per credit
- MBA and MS Business Strategy records preserve the official page-level mismatch between the page title, body text, and Degree Programs listing in the notes.

## Recommendation

APPROVE WITH NOTES
