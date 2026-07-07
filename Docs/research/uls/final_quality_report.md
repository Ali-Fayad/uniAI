# ULS Final Quality Report

Date accessed: 2026-07-05
University: Université La Sagesse (ULS)
Official website: https://www.uls.edu.lb
Task code: ULS_FINAL_QA_001

## Scope

- `programs.json` records: 13
- MASTER records: 13
- PHD records: 0
- `out_of_scope_programs.json` records: 3
- DBA and other out-of-scope records remain excluded
- The unlabeled Canon Law master remains excluded
- Program-level tuition remains null by design
- Faculty/group tuition is centralized in `research/uls/university.json`

## Validation Results

- `research/uls/programs.json` parses successfully.
- `research/uls/out_of_scope_programs.json` parses successfully.
- `research/uls/sources.json` parses successfully.
- `research/uls/university.json` parses successfully.
- `research/uls/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate source IDs were found in `research/uls/sources.json`.
- No duplicate source URLs were found in `research/uls/sources.json`.
- Every source reference in the structured ULS bundle resolves to an entry in `research/uls/sources.json`.
- All non-null `official_program_url` values are official ULS URLs.
- Shared graduate data is centralized in `research/uls/university.json`.
- Tuition mapping is summarized in `research/uls/fees_mapping_summary.json`.
- Schema compatibility with V24 is preserved.

## Tuition / Fees

- `research/uls/fees_mapping_summary.json` records 7 published tuition rows.
- `research/uls/fees_mapping_summary.json` records 1 unmapped program.
- The MIAGE double master remains unmapped because no stable numeric tuition figure was recovered from the inspected official source set.
- Program-level tuition remains null rather than inferred.

## Build Validation

- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`

## Recommendation

APPROVE WITH NOTES

## Final Verdict

ULS is ready for V44 seed generation.
