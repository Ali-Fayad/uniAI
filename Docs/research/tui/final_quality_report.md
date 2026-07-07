# TUI Final Quality Report

Task code: `FINAL_QA_TUI_001`
University: Tripoli University Institute / University of Tripoli
Date accessed: 2026-07-06
Official websites: https://ut.edu.lb, https://new.ut.edu.lb

## Scope Check

- Program count remains 4.
- MASTER count is 2.
- PHD count is 2.
- Out-of-scope count is 0.
- Both master's programs remain present.
- Both PhD programs remain present.
- The PhD credit requirement remains 42 where officially published.

## Shared Data Check

- Shared graduate data remains centralized in `research/tui/university.json`.
- Tuition remains correctly unmapped in `research/tui/programs.json`.
- `research/tui/fees_mapping_summary.json` records 0 tuition rows and 4 unmapped programs.
- No tuition value was inferred.

## Validation

- `research/tui/programs.json` parses successfully.
- `research/tui/out_of_scope_programs.json` parses successfully.
- `research/tui/university.json` parses successfully.
- `research/tui/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate source IDs were found in `research/tui/sources.json`.
- No duplicate source URLs were found in `research/tui/sources.json`.
- Every source reference in the structured TUI files resolves to an entry in `research/tui/sources.json`.
- All non-null URLs are official UT URLs.
- V24 compatibility is preserved by the current TUI artifact structure.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Notes

- The captured official evidence still supports the two master's degrees and two PhD degrees in the Faculty of Sharia and Islamic Studies.
- The shared source set still does not expose a structured graduate tuition table.
- No unsupported graduate program was introduced during enrichment.

## Recommendation

APPROVE WITH NOTES
