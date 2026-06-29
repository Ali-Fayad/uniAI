# BAU Graduate Dataset Review

**Task:** `BAU_FINAL_REVIEW_001`

## Verdict
**REJECT**

The dataset is structurally clean, but it is not ready for repository acceptance or V30 seeding because the shared data, tuition, and enrichment layers are still materially incomplete.

## Validation Summary
- JSON parsing: passed for all inspected JSON files.
- Duplicate IDs: none found in `sources.json`, `programs.json`, or `out_of_scope_programs.json`.
- Duplicate source URLs: none found.
- Missing source references: none found.
- Official BAU source URLs: all source URLs are BAU domains and all 25 checked URLs returned `200 OK`.
- Live inventory cross-check: the official BAU graduate faculty pages matched the local program inventory after accounting for BAU section labels such as `MBA`, `DBA`, `MArch`, and `Speciality Diploma`.
- Maven compile: attempted with `./mvnw -q -DskipTests compile`, but `./mvnw` is not present in this folder, so the compile validation could not be executed here.

## Findings

### 1. Shared university data is not centralized
BAU shared admissions, deadlines, document requirements, and related graduate-wide rules are summarized in reports and repeated as generic program-level text, but they are not actually centralized in `university.json`.

- Affected files:
  - `university.json`
  - `programs.json`
  - `out_of_scope_programs.json`
  - `shared_report.md`
- Proposed fix:
  - Move the shared graduate rules into `university.json`.
  - Remove repeated generic admission text from individual program records unless a program has a true exception.

### 2. `official_program_url` is missing for every program record
The live BAU graduate pages expose program URLs for every listing, but `official_program_url` is `null` for all 130 records.

- Affected files:
  - `programs.json`
  - `out_of_scope_programs.json`
- Proposed fix:
  - Populate `official_program_url` from the official BAU program pages.
  - Keep the faculty listing URL only as a fallback, not the program URL.

### 3. Tuition is not actually populated at program level
The BAU tuition PDF is reachable, but `fees_mapping_summary.json` contains no `program_fee_mappings`, and every program `tuition` field remains `null`.

- Affected files:
  - `fees_mapping_summary.json`
  - `programs.json`
- Proposed fix:
  - Parse the official BAU tuition PDF into program-level tuition entries.
  - Keep `null` only where BAU does not publish a program-specific value.

### 4. Source provenance is incomplete
`sources.json` contains official URLs and sensible scopes, but no access dates are stored.

- Affected files:
  - `sources.json`
- Proposed fix:
  - Add access dates for each source entry.
  - Preserve the current source IDs and URLs.

### 5. Enrichment remains mostly blank
This is not a data integrity error, but it is a completeness gap. For in-scope programs, `language`, `delivery_mode`, `tracks_concentrations`, `gre_gmat`, `interview`, `experience`, `accreditation`, and `official_program_url` are all `null` across the board.

- Affected files:
  - `programs.json`
  - `out_of_scope_programs.json`
- Proposed fix:
  - Keep fields `null` unless BAU explicitly publishes the value.
  - Enrich only from official BAU pages/PDFs.

## Completeness
- Core record completeness: 100% for identity/source-backed program records.
- Program inventory completeness: 100% against the live BAU faculty pages that were checked.
- Tuition completeness: 0% at program level.
- Enrichment completeness: partial only; most optional enrichment fields are still `null`.

## Inventory Counts
- In-scope graduate programs: 116
- Out-of-scope graduate diploma/speciality programs: 14
- Master: 59
- PhD: 50
- DBA: 1
- MBA: 3
- MArch: 3
- Diploma: 6
- Speciality Diploma: 8

## Final Assessment
The BAU dataset is **not ready for DB seeding (V30)** yet. It requires further corrections to centralize shared data, populate program URLs, and complete tuition extraction before acceptance.
