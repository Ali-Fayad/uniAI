# MUBS Program Enrichment Report

Date accessed: 2026-07-04
University: Modern University for Business and Science (MUBS)
Official website: https://www.mubs.edu.lb
Task code: MUBS_ENRICHMENT_001

## Scope

- Finalized inventory remains 2 MASTER programs.
- PHD programs remain 0.
- Tuition remains unmapped / null by official-source limitation.
- Cardiff MBA pathways remain concentrations/tracks under one MBA record.
- Legacy master references remain out of scope.

## Program-Specific Fields Enriched

### Master of Science in Computer Science

- `program_description` populated.
- `admission_requirements` populated.
- `accreditation` populated.
- `concentrations_or_tracks` retained from the inventory pass.

### Master of Business Administration (MBA)

- `program_description` populated.
- `accreditation` populated.
- `concentrations_or_tracks` retained from the inventory pass.

## Fields Not Populated

- `credits`: no stable official program-level value was recovered.
- `duration`: no stable official program-level value was recovered.
- `thesis_or_non_thesis`: not published for these records.
- `delivery_mode`: not populated to avoid inferring a mode the source set did not state explicitly in a program-specific field.
- `language`: not populated to avoid inferring a language the source set did not state explicitly in a program-specific field.
- `GRE/GMAT`: not published for these records.
- `interview`: not published for these records.
- `experience`: not published for these records.

## Validation

- `research/mubs/programs.json` parses successfully.
- `research/mubs/out_of_scope_programs.json` parses successfully.
- `research/mubs/university.json` parses successfully.
- `research/mubs/sources.json` parses successfully.
- `research/mubs/fees_mapping_summary.json` parses successfully.
- Program count remains 2.
- MASTER count remains 2.
- PHD count remains 0.
- No new sources were added, so `research/mubs/sources.json` was unchanged.
- Every source reference in the enriched program records resolves against `research/mubs/sources.json`.
- `./mvnw -q -DskipTests compile` passes from `Server/`.

## Notes

- The MCS page supports the description and admission wording directly through the source notes already captured in discovery.
- The Cardiff MBA sources support the program description and pathway structure, but not a stable numeric tuition figure or a separate page for each pathway.
- The hybrid/flexible wording for Cardiff appears in the info-request source, but was not promoted into a structured delivery-mode field because the repo does not use a HYBRID enum consistently and the evidence was not explicit enough for a schema-safe normalization.

## Recommendation

APPROVE WITH NOTES
