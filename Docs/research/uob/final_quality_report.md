# UOB Final Quality Report

## Verdict

**APPROVE WITH NOTES**

## Validation Performed

- Parsed successfully:
  - `research/uob/programs.json`
  - `research/uob/out_of_scope_programs.json`
  - `research/uob/university.json`
  - `research/uob/sources.json`
  - `research/uob/fees_mapping_summary.json`
- Ran successfully:
  - `./mvnw -q -DskipTests compile`

## Core Counts

- In-scope graduate programs: 60
- MASTER: 59
- PHD: 1
- Out-of-scope records: 2
- Source records: 28
- Unique source URLs: 28

## Duplicate Checks

- Duplicate program IDs: none
- Duplicate source IDs: none
- Duplicate source URLs: none
- Duplicate `official_program_url` values: intentional only

### Intentional URL Reuse

Documented and expected reuse cases:

- `https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/ALBAGraduate.pdf`
  - 19 ALBA graduate programs
- `https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/FASGraduate.pdf`
  - 20 Faculty of Arts and Sciences graduate programs
- `https://www.balamand.edu.lb/Style%20Library/PDFs/Catalogue/General-Section-Graduate.pdf`
  - 11 programs, mostly Business, Engineering, and FOM catalogue-only offerings
- `https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSClinicalLabSciences.aspx`
  - 2 Clinical Laboratory Sciences rows
- `https://theology.balamand.edu.lb/`
  - 2 Theology rows

These are not duplicate-program errors because the underlying records remain distinct by ID and source set.

## Source Integrity

- Every program has at least one source.
- Every program source reference resolves to a source in `research/uob/sources.json`.
- All official program URLs are official UOB or official theology subdomain URLs.
- Orphan sources: none

## Schema Compatibility

The dataset remains compatible with the graduate schema introduced in V24.

Checked values:

- `degree_type`: `MASTER`, `PHD`
- `delivery_mode`: remains null
- `language`: `ARABIC`, `ENGLISH`, `FRENCH`, `MULTILINGUAL`
- `thesis_or_non_thesis`: `THESIS`, `NON_THESIS`, `THESIS_OR_PROJECT`, `PROJECT`, null
- tuition `billing_basis`: `PER_CREDIT`, `PER_ACADEMIC_YEAR`
- tuition `currency`: `USD`, `LBP`

No enum or check-constraint mismatch was found.

## Tuition Consistency

- Tuition rows present: 58
- Tuition rows absent: 2
- All 2 null tuition rows belong to Theology.
- All master’s programs have tuition except:
  - `uob-theology-master-theology`

This is an official-source limitation, not a data error.

The tuition totals match `research/uob/shared_report.md` and `research/uob/fees_mapping_summary.json`.

## Completeness Statistics

### Overall completeness

Across the 14 reviewed per-program fields, including tuition:

- Filled cells: 263 / 840
- Overall completeness: 31.3%

Excluding tuition:

- Filled cells: 205 / 780
- Completeness: 26.3%

### Field coverage

- `program_description`: 47 / 60
- `credits`: 26 / 60
- `duration`: 24 / 60
- `thesis_or_non_thesis`: 26 / 60
- `concentrations_or_tracks`: 16 / 60
- `delivery_mode`: 0 / 60
- `language`: 60 / 60
- `admission_requirements`: 3 / 60
- `gre_requirement`: 0 / 60
- `gmat_requirement`: 0 / 60
- `interview_requirement`: 1 / 60
- `experience_requirement`: 1 / 60
- `accreditation`: 1 / 60

## Shared Data Centralization

The following shared graduate fields are correctly centralized in `research/uob/university.json` and intentionally left null in `programs.json` unless program-specific wording exists:

- `required_documents`
- `deadlines`
- `scholarships`
- `financial_aid`
- `payment_plans`
- `english_requirement`
- `admission_requirements`
- `additional_fees`

The shared university record also holds:

- admissions summary
- academic calendar
- academic regulations
- accreditation
- international student notes
- language policy

## Key Findings

- No duplicate program IDs were introduced.
- No duplicate source IDs or source URLs were introduced.
- No broken source references exist.
- Program-level enrichment improved the dataset materially without overwriting university-wide shared fields.
- Two canonical program URLs were improved:
  - Food Science and Technology now uses its dedicated official page.
  - Medical Laboratory Sciences now uses the dedicated FHS program page.
- The Theology and ALBA hub/catalog reuse is intentional and documented.

## Remaining Gaps

Official-source gaps still present:

- Theology tuition is not published in the reviewed UOB sources.
- Engineering graduate offerings remain mostly catalogue/hub based.
- Some ALBA records still lack directly verified credits and duration.
- `uob-fom-master-cognitive-behavior-therapy` remains catalogue-level only.
- `uob-fas-master-philosophy` still lacks directly verified credit/duration detail.

## Out-of-Scope Review

- `research/uob/out_of_scope_programs.json` contains 2 records.
- It does not overlap with `programs.json` by program ID.
- The shared general graduate catalogue URL is reused intentionally for out-of-scope items, but no out-of-scope record was inserted into the graduate program inventory.

## Final Recommendation

The UOB graduate dataset is ready for database seeding with notes.
