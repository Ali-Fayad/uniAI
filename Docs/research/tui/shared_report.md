# TUI Shared Graduate Data Report

Task code: `SHARED_TUI_001`
University: Tripoli University Institute / University of Tripoli
Date accessed: 2026-07-06
Official websites: https://ut.edu.lb, https://new.ut.edu.lb

## Scope Summary

- Program count: 4
- MASTER count: 2
- PHD count: 2
- Out-of-scope count: 0
- Tuition rows mapped: 0
- Tuition source references resolved: yes

## Centralized Shared Data

- Admissions process is centralized in `research/tui/university.json`.
- Required documents are centralized in `research/tui/university.json`.
- Language requirements are centralized in `research/tui/university.json`.
- Tuition remains null because no official graduate tuition table was captured in the inspected source set.
- Application and registration fee fields are centralized in `research/tui/university.json` with null amounts because no stable numeric fee values were recovered.
- Payment methods, payment plans, scholarships, financial aid, academic calendar, graduate regulations, international students, and accreditation notes are centralized in `research/tui/university.json`.

## Tuition Mapping

- `research/tui/fees_mapping_summary.json` records 0 tuition rows and 4 unmapped programs.
- No official graduate tuition schedule or stable numeric tuition amount was recovered from the captured UT pages.
- Tuition is not inferred for any of the inventoried graduate programs.

## Validation

- `research/tui/programs.json` parses successfully.
- `research/tui/out_of_scope_programs.json` parses successfully.
- `research/tui/university.json` parses successfully.
- `research/tui/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were introduced.
- No broken source references were found.
- All non-null URLs remain official UT URLs.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Notes

- The official admissions pages and faculty page confirm graduate study in the Faculty of Sharia and Islamic Studies.
- The captured source set exposes graduate rules and study-system details, but not a complete structured tuition schedule.
- The official academic calendar and foreign-students pages are retained as shared-supporting sources.

## Recommendation

APPROVE WITH NOTES
