# AUB Graduate Schema Review

Date: 2026-06-26

## Verdict
- **Approve with notes**

## Bottom Line
The new canonical graduate schema in `Server/src/main/resources/db/migration/V24__graduate_program_schema_v2.sql` is suitable for the current AUB graduate inventory after thesis-value normalization. The import is now unblocked from the thesis constraint issue.

## Migration Safety

### What looks good
- `V24__graduate_program_schema_v2.sql` is the correct next migration version after `V23__create_graduate_program_schema.sql`.
- `V23` legacy tables are preserved.
- The new model uses `graduate_*` tables, so it does not collide with the existing legacy graduate tables.
- PostgreSQL syntax is valid for the constructs used in the migration.
- Flyway-compatible naming and DDL style are used.
- Existing `university`, `language`, `degree_type`, and `source` tables are referenced rather than recreated.

### Review note
- The migration could not be executed against a live database in this workspace because no datasource is configured.
- `./mvnw -q -DskipTests compile` passed.

## Data Fit

The schema can represent every field in `research/aub/programs_merged_candidate.json`.

### Direct program fields
- `id` -> `graduate_program.program_key`
- `faculty` -> `university_faculty`
- `department` -> `university_department`
- `major_category` -> `graduate_program.major_category`
- `major` -> `graduate_program.major`
- `degree_type` -> `degree_type`
- `official_degree_name` -> `graduate_program.official_degree_name`
- `thesis_or_non_thesis` -> `graduate_program.thesis_or_non_thesis`
- `credits` -> `graduate_program.credits`
- `duration` -> `graduate_program.duration_value` + `duration_unit`
- `language` -> `graduate_program.primary_language_id`
- `delivery_mode` -> `graduate_program.delivery_mode`
- `program_description` -> `graduate_program.program_description`
- `official_program_url` -> `graduate_program.official_program_url`
- `notes` -> `graduate_program.notes`

### Child tables / relational facts
- `concentrations_or_tracks` -> `graduate_program_track`
- `admission_requirements` -> `graduate_admission_requirement`
- `required_documents` -> `graduate_required_document`
- `gre_requirement`, `gmat_requirement`, `english_requirement`, `portfolio_requirement`, `interview_requirement`, `experience_requirement` -> `graduate_admission_requirement`
- `tuition` -> `graduate_tuition_rate`
- `additional_fees` -> `graduate_fee_item`
- `deadlines` -> `graduate_admission_deadline`
- `scholarships` -> `graduate_scholarship`
- `financial_aid` -> `graduate_financial_aid`
- `payment_plans` -> `graduate_payment_plan`
- `accreditation` -> `graduate_accreditation`
- `sources` -> `source` + `graduate_program_source`

## Constraint Review

### degree_type.code
- Current AUB values: `MASTER`, `PHD`
- Constraint: uppercase check
- Result: safe

### delivery_mode
- Current non-null AUB value found: `Online`
- Constraint allows: `ONLINE`
- Result: safe after standard casing normalization during import

### thesis_or_non_thesis
- Current AUB values found after normalization:
  - `THESIS`
  - `THESIS_OR_NON_THESIS`
  - `THESIS_OR_PROJECT`
- The V24 constraint was updated to allow all three plus `NON_THESIS`, `PROJECT`, and `UNKNOWN`.
- Result: safe

### billing_basis
- Current AUB values found:
  - `PER_CREDIT`
  - `PER_SEMESTER`
- Constraint allows both.
- Result: safe

### scope_level
- Constraint values are reasonable for university, faculty, department, and program facts.
- Result: safe

### track_type
- Constraint values are reasonable for tracks, concentrations, specializations, options, and pathways.
- Result: safe

### relation_type
- Constraint values are reasonable for cross-listed and alias-style relationships.
- Result: safe

### requirement_type
- Constraint values are sufficient for current AUB admissions fields when normalized.
- Result: safe

## Import Readiness

### Required transformations
1. Split `duration` strings into numeric value + unit.
2. Expand `concentrations_or_tracks` arrays into `graduate_program_track`.
3. Expand `sources` into `source` and `graduate_program_source`.
4. Expand `tuition` objects into `graduate_tuition_rate`.
5. Expand admissions fields into `graduate_admission_requirement`.
6. Expand `required_documents`, `deadlines`, `scholarships`, `financial_aid`, `payment_plans`, and `accreditation` if they are later populated.
7. Normalize `delivery_mode` text to the DB enum value during import, for example `Online` -> `ONLINE`.

## Remaining Blockers
- None.

### Fields that should map to JSONB
- None of the current core AUB import fields need to be JSONB for the canonical model.

### Fields that may be lost if imported as-is
- `duration` would be lost if not split into value/unit.
- `concentrations_or_tracks` would be lost if not expanded into child rows.
- `sources` would be reduced to a single row unless normalized into source tables.

## Recommended Fixes Before Import

### Required
- None from the thesis-value blocker remain.

### Nice to have
- Consider a raw payload JSONB column if future importers need to preserve source snapshots.
- Consider an import-side symmetric dedup rule for `graduate_program_relationship` so cross-listed pairs do not get inserted twice in reverse order.

## Whether AUB Import Can Proceed
- **Yes**
- The thesis-value blocker has been removed by normalizing the JSON values and updating V24 to accept `THESIS_OR_PROJECT`.
- `delivery_mode` still needs import-side normalization from `Online` to `ONLINE`, but that is straightforward and does not require a schema change.

## Next Prompt For Import
> TASK AUB_IMPORT_001: Import `research/aub/programs_merged_candidate.json` into the new graduate schema using the canonical mapping in `research/aub/schema_design.md`, normalizing `delivery_mode`, `duration`, and all structured child objects into relational tables. Do not change frontend or API code. Validate row counts, source traceability, and duplicate prevention after import.
