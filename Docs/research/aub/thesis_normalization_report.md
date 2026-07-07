# Thesis Normalization Report

Date: 2026-06-26

## Summary
- Normalized thesis-related values in `research/aub/programs_merged_candidate.json`.
- Updated `Server/src/main/resources/db/migration/V24__graduate_program_schema_v2.sql` to allow `THESIS_OR_PROJECT`.
- Import is now unblocked from the thesis-enum issue.

## Original Values Found

- `Thesis` - 8 records
- `Thesis or non-thesis` - 1 record
- `THESIS_OR_NON_THESIS` - 6 records
- `Thesis or project` - 1 record
- `THESIS` - 11 records
- `null` - 35 records

## Normalized Values Used

- `THESIS`
- `THESIS_OR_NON_THESIS`
- `THESIS_OR_PROJECT`
- `null`

## Normalization Mapping Applied

- `Thesis` -> `THESIS`
- `THESIS` -> `THESIS`
- `Thesis or non-thesis` -> `THESIS_OR_NON_THESIS`
- `THESIS_OR_NON_THESIS` -> `THESIS_OR_NON_THESIS`
- `Thesis or project` -> `THESIS_OR_PROJECT`
- empty / unknown -> `null`

## Affected Program IDs

### Normalized to `THESIS`
- `dacp-ms-hmcb`
- `dacp-ms-neuroscience`
- `dacp-ms-physiology`
- `dbmg-ms`
- `epim-ms`
- `fm-dbms-phd`
- `pharm-ms`
- `hson-phd`

### Normalized to `THESIS_OR_NON_THESIS`
- `fas-faah-ma-art-curating`
- `fas-pspa-ppia`
- `fas-soam-media`
- `me-applied-energy`
- `me-energy-studies`
- `me-mechanical`
- `hson-msn`

### Normalized to `THESIS_OR_PROJECT`
- `fhs-ms-environmental-sciences`

## Notes Handling
- The original thesis text was appended to `notes` on affected records as a normalization audit trail.
- Existing notes were preserved and extended rather than replaced.

## V24 Migration Change
- **Yes**
- `ck_graduate_program_thesis_or_non_thesis` now allows:
  - `THESIS`
  - `NON_THESIS`
  - `THESIS_OR_NON_THESIS`
  - `PROJECT`
  - `THESIS_OR_PROJECT`
  - `UNKNOWN`

## Import Readiness
- **Unblocked**
- The thesis-value mismatch that blocked import in the schema review has been resolved.

## Validation
- `research/aub/programs_merged_candidate.json` parses successfully.
- `./mvnw -q -DskipTests compile` passed.
- Flyway migration could not be run locally because no datasource is configured in this workspace.
