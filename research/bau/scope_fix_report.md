# BAU Scope Fix Report

**Task:** `BAU_SCOPE_FIX_001`

## Summary
The BAU graduate seed scope has been reduced to strict MASTER/PHD coverage in `programs.json`.

## Changes Made
- Moved 7 non-scope records from `programs.json` to `out_of_scope_programs.json`.
- Preserved full record data, sources, tuition, and official program URLs on the moved records.
- Added the required scope note to each moved record.
- Reduced main tuition mappings to the 109 in-scope programs.

## Resulting Counts
- `programs.json`: 109 records.
- `out_of_scope_programs.json`: 21 records.
- In-scope degree split: Master 59, PhD 50.

## Validation
- JSON parse: passed for all inspected files.
- Duplicate IDs across `programs.json` and `out_of_scope_programs.json`: none.
- Duplicate source IDs: none.
- Duplicate source URLs: none.
- Every program has at least one source: yes.
- Every source reference resolves: yes.
- Every official program URL is official BAU: yes.
- Compile check: could not run here because `./mvnw` is not present in this workspace path.

## Recommendation
**APPROVE WITH NOTES**

BAU is ready for V30 seed generation.
