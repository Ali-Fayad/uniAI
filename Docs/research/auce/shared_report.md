# AUCE Shared Graduate Data Report

Task code: `SHARED_AUCE_001`
University: American University of Culture & Education (AUCE)
Date accessed: 2026-07-06

## Scope Summary

- Program count: 2
- MASTER count: 2
- PHD count: 0
- Out-of-scope count: 0
- Tuition rows mapped: 0
- Tuition source references resolved: yes

## Centralized Shared Data

- Admissions process is centralized in `research/auce/university.json`.
- Required documents are centralized in `research/auce/university.json`.
- Language requirements are centralized in `research/auce/university.json`.
- Tuition remains null because no official graduate tuition table was published in the inspected source set.
- The graduate application fee is centralized in `research/auce/university.json`.
- General scholarship and financial-aid categories are centralized in `research/auce/university.json`.

## Tuition Mapping

- No official graduate tuition amount table was found in the AUCE source set.
- `research/auce/fees_mapping_summary.json` records 0 tuition rows and 2 unmapped programs.
- Tuition is not inferred for either master's program.

## Validation

- `research/auce/programs.json` parses successfully.
- `research/auce/out_of_scope_programs.json` parses successfully.
- `research/auce/university.json` parses successfully.
- `research/auce/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were introduced.
- No broken source references were found.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Notes

- AUCE officially supports two master's programs only.
- No official PhD program evidence was found, so no PhD shared data was inferred.
- The official pages publish an English requirement and a $50 USD application fee, but not a graduate tuition table.
- Scholarship and aid references are general categories only; no award amounts were published.

## Recommendation

APPROVE WITH NOTES
