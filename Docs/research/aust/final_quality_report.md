# AUST Final Quality Report

Date accessed: 2026-07-04

## Scope

- Finalized inventory records: 17 MASTER
- PHD records: 0
- Out-of-scope records: 0
- Shared university data centralized in `research/aust/university.json`
- Tuition fully mapped in `research/aust/fees_mapping_summary.json`

## Validation Results

- `research/aust/programs.json` parses successfully.
- `research/aust/out_of_scope_programs.json` parses successfully.
- `research/aust/university.json` parses successfully.
- `research/aust/sources.json` parses successfully.
- `research/aust/fees_mapping_summary.json` parses successfully.
- `research/aust/shared_report.md` is present and consistent with the shared JSON.
- `research/aust/enrichment_report.md` is present and consistent with the enrichment pass.
- `research/aust/program_inventory_report.md` is present and consistent with the frozen inventory.

## Checklist

- 1. JSON parses: pass.
- 2. `programs.json` has exactly 17 records: pass.
- 3. MASTER count = 17: pass.
- 4. PHD count = 0: pass.
- 5. `out_of_scope_programs.json` has 0 records: pass.
- 6. No duplicate program IDs: pass.
- 7. No duplicate source IDs: pass.
- 8. No duplicate source URLs: pass.
- 9. Every program has at least one source: pass.
- 10. Every source reference resolves: pass.
- 11. Every URL is an official AUST URL: pass.
- 12. Tuition coverage is complete: pass, 17 populated tuition objects and 0 null tuition.
- 13. Shared admissions/documents/deadlines/fees are centralized in `university.json`: pass.
- 14. Program-level `admission_requirements` are only populated where officially program-specific: pass, none are populated.
- 15. Official discrepancies remain documented: pass.
- 16. No PhD rows exist: pass.
- 17. Schema compatibility with V24: pass, using MASTER-only graduate rows and schema-safe null handling for unavailable fields.
- 18. Orphan sources detected and classified: pass.
- 19. Completeness statistics by field: pass.
- 20. No out-of-scope records exist in `programs.json`: pass.

## Program Inventory

- Total programs: 17
- MASTER programs: 17
- PHD programs: 0
- Out-of-scope programs: 0

## Tuition Coverage

- Tuition populated: 17
- Tuition null: 0
- Tuition model: USD 320 per credit hour for all 17 MASTER programs
- All tuition rows reference `AUST-SRC-007`

## Shared Data Centralization

The following are centralized in `research/aust/university.json` and not duplicated at the program level:

- admissions process
- required documents
- language requirements
- tuition model
- fee structure
- payment methods
- payment plans
- scholarships
- financial aid
- academic calendar
- graduate regulations
- accreditation
- international students

Program-level admissions remain null across all 17 records, which is consistent with the source set: AUST published admissions information centrally rather than program-by-program.

## Official Discrepancies Carried Forward

- MBA credits:
  - official brochure: 36 credits
  - current admissions/program pages: 39 credits
  - inventory and tuition mapping follow the current admissions/program pages
- English requirement:
  - one official source: TOEFL 575
  - another official source: TOEFL 600
  - retained as an unresolved official discrepancy

## Orphan Sources

These source IDs exist in `research/aust/sources.json` but are not referenced by the structured dataset files (`programs.json`, `university.json`, or `fees_mapping_summary.json`):

- `AUST-SRC-001`: official homepage; discovery/navigation support only
- `AUST-SRC-024`: MBA Finance brochure PDF; brochure-only marketing source not needed for the finalized structured dataset
- `AUST-SRC-025`: MBA Management brochure PDF; brochure-only marketing source not needed for the finalized structured dataset

## Completeness Statistics

Program field coverage in `research/aust/programs.json`:

| Field | Populated | Null | Coverage |
|---|---:|---:|---:|
| `program_description` | 0 | 17 | 0% |
| `credits` | 17 | 0 | 100% |
| `duration` | 0 | 17 | 0% |
| `thesis_or_non_thesis` | 0 | 17 | 0% |
| `concentrations_or_tracks` | 1 | 16 | 5.9% |
| `delivery_mode` | 0 | 17 | 0% |
| `language` | 0 | 17 | 0% |
| `admission_requirements` | 0 | 17 | 0% |
| `gre_requirement` | 0 | 17 | 0% |
| `gmat_requirement` | 0 | 17 | 0% |
| `portfolio_requirement` | 0 | 17 | 0% |
| `interview_requirement` | 0 | 17 | 0% |
| `experience_requirement` | 0 | 17 | 0% |
| `accreditation` | 0 | 17 | 0% |
| `notes` | 17 | 0 | 100% |

University-level field coverage in `research/aust/university.json`:

- `admissions_process`: populated
- `required_documents`: populated
- `language_requirements`: populated with discrepancy note
- `tuition_model`: populated
- `fee_structure`: populated with null amounts where no stable official value was recovered
- `payment_methods`: populated
- `payment_plans`: populated
- `scholarships`: populated
- `financial_aid`: populated
- `academic_calendar`: populated
- `graduate_regulations`: populated
- `accreditation`: explicitly null because no official statement was recovered
- `international_students`: populated

## Schema Compatibility With V24

- `degree_type` values are MASTER only.
- No PHD, diploma, certificate, or professional doctorate rows are present in `programs.json`.
- Structured tuition uses a single per-credit row with explicit source attribution.
- Optional fields remain `null` rather than being fabricated.
- No unexpected enum values were introduced in the final AUST bundle.

## Build Validation

- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`

## Recommendation

APPROVE WITH NOTES

## Final Verdict

AUST is ready for V35 seed generation.
