# USJ Final Quality Report

Date accessed: 2026-06-27

## Verdict

`APPROVE WITH NOTES`

## Validation Performed

- Parsed successfully:
  - `research/usj/programs.json`
  - `research/usj/out_of_scope_programs.json`
  - `research/usj/university.json`
  - `research/usj/sources.json`
  - `research/usj/fees_mapping_summary.json`
- Verified:
  - in-scope program count = 51
  - out-of-scope program count = 5
  - degree types in `programs.json` are only `MASTER` and `PHD`
  - duplicate program IDs = 0
  - every program has at least one source
  - all program source IDs exist in `sources.json`
  - all source URLs are official USJ URLs or official USJ subdomains
  - `out_of_scope_programs.json` does not overlap with `programs.json`
- Ran successfully:
  - `./mvnw -q -DskipTests compile` in `Server/`

## Completeness

- Raw schema-cell completeness across all 51 program records: `30.51%`
- Optional program-field completeness only: `3.32%`

Interpretation:

- The raw percentage is high because required identity fields are fully populated.
- The optional-field percentage is the more meaningful measure of enrichment depth and reflects the fact that USJ exposes most program-specific details only on a few PDF-backed pages.

## Duplicate Checks

- Duplicate IDs in `programs.json`: none
- Duplicate IDs in `out_of_scope_programs.json`: none
- Overlap between in-scope and out-of-scope IDs: none
- Duplicate source URLs in `sources.json`: none
- Duplicate source IDs in `sources.json`: none
- Duplicate official program URLs: present by design for hub/catalog reuse

### Intentional hub/catalog reuse

- `https://usj.edu.lb/fr/e-doors/masters` - 24 programs
- `https://usj.edu.lb/fr/fs/formations` - 8 programs
- `https://usj.edu.lb/fgm/` - 6 programs
- `https://usj.edu.lb/fdsp/` - 5 programs
- `https://usj.edu.lb/fmd/` - 3 programs

This reuse is intentional and documented in program notes because USJ often exposes one hub page instead of one canonical HTML page per graduate program.

## Normalization Checks

- `degree_type`: normalized and valid
  - `MASTER`
  - `PHD`
- `thesis_or_non_thesis`: normalized where present
  - `THESIS`
  - `PROJECT`
  - `THESIS_OR_NON_THESIS` not used in the current in-scope set
- `language`: normalized where present
  - `MULTILINGUAL`
  - remaining records are null because the official page did not explicitly state a language
- `delivery_mode`: no explicit program-level values were safely verified, so it remains null
- tuition billing basis: not present in `programs.json` because no official program-level tuition schedule was published

## Null Analysis

### Structural fields

| Field | Populated | Null | Classification |
|---|---:|---:|---|
| id | 51 | 0 | required |
| faculty | 51 | 0 | required |
| department | 19 | 32 | partially available from official pages; missing for hub-anchored records |
| major_category | 51 | 0 | required |
| major | 51 | 0 | required |
| degree_type | 51 | 0 | required |
| official_degree_name | 51 | 0 | required |
| official_program_url | 51 | 0 | required |
| sources | 51 | 0 | required |
| notes | 51 | 0 | required |

### Program-specific fields

| Field | Populated | Null | Classification |
|---|---:|---:|---|
| thesis_or_non_thesis | 3 | 48 | program-specific; mostly unavailable on hub pages |
| concentrations_or_tracks | 0 | 51 | program-specific; not exposed on reviewed pages |
| credits | 3 | 48 | program-specific; only PDF-backed FS programs published credits |
| duration | 3 | 48 | program-specific; only PDF-backed FS programs published duration |
| language | 3 | 48 | program-specific; only PDF-backed FS PDFs explicitly stated it |
| delivery_mode | 0 | 51 | program-specific; not explicitly stated |
| program_description | 3 | 48 | program-specific; only PDF-backed FS programs published descriptions |
| admission_requirements | 3 | 48 | program-specific; only PDF-backed FS programs published detailed requirements |
| gre_requirement | 0 | 51 | not published on reviewed official pages |
| gmat_requirement | 0 | 51 | not published on reviewed official pages |
| portfolio_requirement | 0 | 51 | not published on reviewed official pages |
| interview_requirement | 2 | 49 | program-specific; only two PDF-backed FS programs explicitly mention it |
| experience_requirement | 0 | 51 | not published on reviewed official pages |
| accreditation | 0 | 51 | not published at program level on reviewed pages |

### Shared fields intentionally stored in `university.json`

| Field | Populated in programs.json | Null in programs.json | Classification |
|---|---:|---:|---|
| required_documents | 0 | 51 | intentionally shared in `university.json` |
| english_requirement | 0 | 51 | intentionally shared in `university.json` |
| deadlines | 0 | 51 | intentionally shared in `university.json` |
| scholarships | 0 | 51 | intentionally shared in `university.json` |
| financial_aid | 0 | 51 | intentionally shared in `university.json` |
| payment_plans | 0 | 51 | intentionally shared in `university.json` |
| tuition | 0 | 51 | no official program-level tuition amounts published |
| additional_fees | 0 | 51 | no official program-level fee table published |

## Major Remaining Gaps

- No program-level tuition amounts were published on most official pages.
- No centralized graduate tuition table was found.
- No explicit per-program delivery mode was published for most programs.
- No explicit per-program accreditation statements were published.
- Most programs remain anchored to hub/catalog URLs instead of unique program URLs.
- Most program pages do not expose detailed admissions text beyond the shared university policy.

## Hub / Catalog Reuse Summary

The following hub/catalog URLs are reused intentionally:

- `https://usj.edu.lb/fr/e-doors/masters`
- `https://usj.edu.lb/fr/fs/formations`
- `https://usj.edu.lb/fgm/`
- `https://usj.edu.lb/fdsp/`
- `https://usj.edu.lb/fmd/`

This is a source-structure issue, not a data error. It reflects how USJ publishes graduate information publicly.

## Orphan Source Summary

Unused sources in `research/usj/sources.json`:

- `usj_home_en`
- `usj_equivalences`
- `usj_fgm_mba_dossier_pdf`
- `usj_fgm_presentation_master_pdf`
- `usj_fgm_masters_presentation_pdf`
- `usj_school_fia`
- `usj_school_elfs`

Classification:

- Useful discovery/reference sources: all 7
- Broken or invalid: 0

These are still useful for future refinement even though they were not referenced by the current in-scope program inventory or university-level record.

## Source / URL Checks

- Every source URL belongs to an official USJ domain or official USJ subdomain.
- Every source referenced by a program or by `university.json` exists in `sources.json`.
- No source URL duplication was found.

## Schema Compatibility With AUB / LAU

No schema compatibility issue was found.

- `programs.json` uses the same program-record field set as AUB and LAU.
- `sources.json` follows the same source-object format as AUB and LAU.
- `university.json` is an institution-level companion file and does not affect program schema compatibility.

## Final Recommendation

`APPROVE WITH NOTES`

Notes:

- The dataset is internally consistent and ready for downstream use.
- Remaining nulls are mostly due to official USJ source limitations or intentional shared-data storage in `university.json`.
- The source structure is hub-heavy, so some reuse of catalog and faculty URLs is expected and documented.
