# LAU Final Quality Audit

Date accessed: 2026-06-27

## Verdict

`APPROVE WITH NOTES`

## Overall Completeness

- Total program records: 33
- Total schema cells per record: 32
- Populated cells across the in-scope program table: 573 / 1056
- Overall completeness: 54.3%

Core completeness is effectively 100% for:
- `id`
- `faculty`
- `department`
- `major_category`
- `major`
- `degree_type`
- `official_degree_name`
- `tuition`
- `official_program_url`
- `sources`
- `notes`

## What Was Corrected In This Pass

- Normalized `delivery_mode` values to schema codes:
  - `ONLINE`
  - `ON_CAMPUS`
- Normalized `language` values to `ENGLISH`.
- Preserved the modular EMBA nuance in notes.
- Added missing canonical source entries for:
  - `lau_aksob_programs`
  - `lau_online_home`
  - `lau_prog_ms_finance_accounting`
- Remapped program source references from non-canonical school IDs to existing canonical IDs:
  - `lau_soas` -> `lau_school_soas`
  - `lau_soe` -> `lau_school_soe`
  - `lau_sard` -> `lau_school_sard`
  - `lau_pharmacy` -> `lau_school_pharmacy`

## Duplicate Detection

- Duplicate program IDs: none.
- Duplicate official program URLs inside `research/lau/programs.json`: none.
- Duplicate source URLs inside `research/lau/sources.json`: none.
- Duplicate information between `university.json` and `programs.json`: no conflicting duplicates found.

## Official Program URL Check

- All 33 in-scope `official_program_url` values resolve to official LAU pages.
- The only source-traceability mismatch found during review was `business-ms-finance-and-accounting`; it was corrected by adding the canonical source entry to `sources.json`.

## Remaining Missing Fields

These fields are still null in `programs.json` because the official LAU pages did not provide a reliable value in this pass:

- `credits`: 1 null
- `duration`: 13 null
- `thesis_or_non_thesis`: 15 null
- `concentrations_or_tracks`: 26 null
- `language`: 23 null
- `delivery_mode`: 21 null
- `program_description`: 1 null
- `admission_requirements`: 19 null
- `required_documents`: 33 null
- `gre_requirement`: 30 null
- `gmat_requirement`: 29 null
- `english_requirement`: 33 null
- `portfolio_requirement`: 33 null
- `interview_requirement`: 27 null
- `experience_requirement`: 24 null
- `additional_fees`: 33 null
- `deadlines`: 33 null
- `scholarships`: 33 null
- `financial_aid`: 33 null
- `payment_plans`: 33 null
- `accreditation`: 23 null

## Remaining Inconsistencies

- `delivery_mode` and `language` are now normalized in the dataset, but many programs still lack values because the official page did not expose them clearly enough to fill without inference.
- Source coverage is not fully closed because some discovery-only support pages remain unreferenced by the current in-scope objects.

## Source Coverage

Referenced source IDs now resolve cleanly for the in-scope program and university files.

Orphan source IDs still present in `research/lau/sources.json`:
- `lau_home`
- `lau_gsr_home`
- `lau_schools`
- `lau_request_info_grad_fall_2026`
- `lau_ugc`
- `lau_school_aksob`
- `lau_school_medicine`
- `lau_school_nursing`
- `lau_prog_md`
- `lau_prog_pharmd`

These are discovery/support sources or out-of-scope program pages and do not block the current in-scope master’s inventory.

## Validation Performed

- Parsed successfully:
  - `research/lau/programs.json`
  - `research/lau/university.json`
  - `research/lau/sources.json`
- Verified:
  - 33 program records in `programs.json`
  - 56 source records in `sources.json`
  - no duplicate program IDs
  - no duplicate official program URLs
  - no duplicate source URLs
- Run successfully:
  - `./mvnw -q -DskipTests compile`

## Files Changed

- `research/lau/programs.json`
- `research/lau/sources.json`
- `research/lau/final_quality_report.md`

## Final Recommendation

`APPROVE WITH NOTES`

The LAU graduate dataset is clean enough for completion of the current in-scope master’s inventory. The remaining nulls are concentrated in fields that were not reliably exposed on the official program pages, and the orphan source pages are discovery/support or out-of-scope pages rather than data conflicts.
