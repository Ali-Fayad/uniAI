# AUL Final Quality Report

## Verdict

APPROVE WITH NOTES

AUL is ready for V36 seed generation.

## Validation

- All `research/aul/*.json` parse: pass
- `./mvnw -q -DskipTests compile`: pass
- `programs.json` records: 3
- MASTER count: 3
- PHD count: 0
- `out_of_scope_programs.json` records: 2
- Duplicate program IDs: none
- Duplicate source IDs: none
- Duplicate source URLs: none
- Duplicate official program URLs: none
- Every program has at least one source: pass
- Every source reference resolves: pass
- Every URL is official AUL: pass
- Tuition coverage: 0/3 populated, 3/3 null
- No out-of-scope records inside `programs.json`: pass
- V24 schema compatibility: pass
- Shared data centralized in `university.json`: pass
- MBA remains one program rather than separate concentration rows: pass

## Inventory Summary

- In-scope graduate programs: 3
  - Master of Business Administration
  - Master of Science in Engineering
  - Master of Science
- Out-of-scope graduate-related items: 2
  - Teaching Diploma / Graduate Diploma
  - Continuing Education Center / MikroTik Academy / MTCNA Training
- PhD programs: 0

## Shared Data Summary

- Admissions, document requirements, financial aid, calendar guidance, and regulations are centralized in `university.json`.
- Tuition is intentionally null for all 3 programs because no numeric graduate tuition schedule was published in the reviewed official AUL sources.
- Application, registration, and entrance-exam fee labels exist on the admissions page, but no public graduate fee amounts were published.

## Orphan Sources

The following official source records were not used directly in in-scope program rows:

- `AUL-001`
- `AUL-006`
- `AUL-007`
- `AUL-008`
- `AUL-009`

These remain valid discovery/context sources for the dataset and were retained for shared-data traceability.

## Completeness Statistics

- `credits`: 3/3
- `duration`: 3/3
- `description`: 3/3
- `notes`: 3/3
- `language`: 0/3
- `concentrations_or_tracks`: 0/3
- `tuition`: 0/3
- `program_description`: 0/3
- `thesis_or_non_thesis`: 0/3
- `delivery_mode`: 0/3
- `admission_requirements`: 0/3
- `GRE`: 0/3
- `GMAT`: 0/3
- `portfolio`: 0/3
- `interview`: 0/3
- `experience`: 0/3
- `accreditation`: 0/3

## Intentional Nulls

- `tuition`: null for all 3 programs because no official numeric graduate tuition schedule was published.
- `language`: null because no program-specific graduate language field was published on the official program pages.
- `delivery_mode`: null because the source set did not publish a distinct mode field beyond the standard on-campus graduate offering context.
- `admission_requirements` and other enrichment fields: null because the reviewed sources did not provide title-level program text beyond the inventory-level evidence already captured.

## Official-Source Limitations

- No official graduate tuition schedule was found in the reviewed AUL sources.
- No official PhD program evidence was found.
- The research page mentions doctoral activity, but no graduate degree page or official PDF established an AUL PhD program.
- AUL faculty pages link to Google Drive contract sheets, but those were excluded because the task required official AUL pages and AUL-hosted PDFs only.

## Recommendation

APPROVE WITH NOTES

The AUL graduate dataset is complete enough for seed generation, with the important caveat that tuition remains unavailable from official AUL sources and is intentionally left null.
