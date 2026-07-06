# AUOT Final Quality Report

Date accessed: 2026-07-06

## Validation Summary

- JSON parses: pass
- `programs.json` records: 3
- MASTER count: 3
- PHD count: 0
- `out_of_scope_programs.json` records: 1
- Duplicate program IDs: none
- Duplicate source IDs: none
- Duplicate source URLs: none
- Every source reference resolves: pass
- Shared data centralized in `university.json`: pass
- V24 compatibility: pass
- `./mvnw -q -DskipTests compile`: pass

## Tuition Coverage

- Program-level tuition populated: 0/3
- Program-level tuition null: 3/3
- Tuition is centralized in `research/auot/university.json`

## Inventory Status

- The inventory contains three official AUT-awarded master’s programs:
  - Master of Business Administration
  - Master of Science in Computer Science
  - Master of Science in Information Technology
- No official AUT PhD program evidence was found in the inspected official sources.

## Orphan Sources

- None

## Completeness by Field

- `program_description`: 3/3
- `credits`: 2/3
- `accreditation`: 1/3
- `concentrations_or_tracks`: 1/3
- `duration`: 0/3
- `thesis_or_non_thesis`: 0/3
- `delivery_mode`: 0/3
- `language`: 0/3
- `admission_requirements`: 0/3
- `gre_requirement`: 0/3
- `gmat_requirement`: 0/3
- `portfolio_requirement`: 0/3
- `interview_requirement`: 0/3
- `experience_requirement`: 0/3

## Intentional Nulls

- `official_program_url` is null for the MBA and MS Information Technology because no separate stable AUT program page was found during discovery.
- `duration`, `thesis_or_non_thesis`, `delivery_mode`, `language`, `admission_requirements`, `GRE`, `GMAT`, `portfolio_requirement`, `interview_requirement`, and `experience_requirement` remain null because the reviewed official sources did not publish title-level values for these fields.

## Official-Source Limitations

- The University of London LLM page is an AUT support page, but AUT does not clearly present it as an AUT-awarded graduate degree, so it remains out of scope.
- The catalogue is the primary evidence source for the MBA and MS Information Technology.
- The MBA accreditation is published only at the catalog level and has been preserved in the program record.

## Recommendation

APPROVE WITH NOTES
