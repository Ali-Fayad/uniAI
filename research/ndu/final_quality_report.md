# NDU Final Quality Report

Task code: `NDU_FINAL_QA_001`  
University: Notre Dame University-Louaize (NDU)  
Date accessed: 2026-07-01

## Verdict

**APPROVE WITH NOTES**

NDU is ready for V31 seed generation.

## Validation Matrix

| Check | Result | Notes |
|---|---|---|
| 1. JSON parses | PASS | All `research/ndu/*.json` files parse successfully. |
| 2. `programs.json` has exactly 29 records | PASS | 29 master’s programs present. |
| 3. `degree_type` is MASTER only | PASS | No non-MASTER records found. |
| 4. PHD count is 0 | PASS | No PhD rows exist. |
| 5. `out_of_scope_programs.json` has 2 records | PASS | Two excluded graduate diplomas remain out of scope. |
| 6. No duplicate program IDs | PASS | None found. |
| 7. No duplicate `official_program_url` values | PASS | None found. |
| 8. No duplicate source IDs | PASS | None found. |
| 9. No duplicate source URLs | PASS | None found. |
| 10. Every program has at least one source | PASS | All 29 programs have source references. |
| 11. Every source reference resolves | PASS | All source IDs used in structured files resolve to `sources.json`. |
| 12. Every URL is official NDU | PASS | Program URLs and source URLs are NDU-owned. |
| 13. Tuition coverage is 29/29 | PASS | Tuition is populated for all programs. |
| 14. Every tuition object references an existing official source | PASS | Tuition rows reference valid NDU source IDs. |
| 15. Shared admissions/documents/deadlines/English requirements are centralized in `university.json` | PASS | Shared policy lives in `university.json`; program rows only carry program-specific admissions where available. |
| 16. Program-level `admission_requirements` contain only program-specific values | PASS | One program remains null because no distinct program-specific value was published. |
| 17. Schema compatibility with V24 | PASS | `degree_type`, `delivery_mode`, `thesis_or_non_thesis`, `tuition.billing_basis`, and `language` are schema-safe. |
| 18. Detect orphan sources and classify them | PASS | See orphan source table below. |
| 19. Produce completeness stats by field | PASS | See field completeness table below. |
| 20. Confirm no PhD rows were accidentally created | PASS | Verified at dataset level. |

## Dataset Summary

| Metric | Value |
|---|---:|
| Programs | 29 |
| MASTER programs | 29 |
| PHD programs | 0 |
| Out-of-scope records | 2 |
| Tuition populated | 29 |
| Tuition null | 0 |

## Field Completeness

| Field | Non-null | Null | Coverage |
|---|---:|---:|---:|
| `description` | 29 | 0 | 100.0% |
| `program_description` | 29 | 0 | 100.0% |
| `delivery_mode` | 29 | 0 | 100.0% |
| `thesis_or_non_thesis` | 19 | 10 | 65.5% |
| `concentrations` | 8 | 21 | 27.6% |
| `concentrations_or_tracks` | 8 | 21 | 27.6% |
| `admission_requirements` | 28 | 1 | 96.6% |
| `GRE` | 3 | 26 | 10.3% |
| `GMAT` | 0 | 29 | 0.0% |
| `interview` | 7 | 22 | 24.1% |
| `experience` | 1 | 28 | 3.4% |
| `accreditation` | 4 | 25 | 13.8% |
| `language` | 1 | 28 | 3.4% |
| `credits` | 29 | 0 | 100.0% |
| `tuition` | 29 | 0 | 100.0% |

## Source Usage And Orphans

Structured source usage was checked across `programs.json`, `out_of_scope_programs.json`, `university.json`, and `fees_mapping_summary.json`.

| Source ID | Page Title | Usage | Classification |
|---|---|---:|---|
| `NDU-SRC-001` | Official website | 0 | discovery anchor |
| `NDU-SRC-004` | Faculties | 0 | reference |
| `NDU-SRC-016` | Admission Guide 2025-2026 | 0 | reference |
| `NDU-SRC-017` | NDU Catalog 2024-2025 | 0 | obsolete fallback |

Notes:

- `NDU-SRC-001` is the top-level discovery anchor and is retained as a reference source.
- `NDU-SRC-004` and `NDU-SRC-016` are official supporting references that were not needed in structured seed rows.
- `NDU-SRC-017` is a prior-catalog fallback and can be treated as obsolete for seeding purposes.
- No orphan source affects seed validity.

## Schema Compatibility

The dataset remains compatible with the V24 program schema:

- `degree_type`: `MASTER`
- `delivery_mode`: `ON_CAMPUS`
- `thesis_or_non_thesis`: `THESIS`, `THESIS_OR_NON_THESIS`, or `null`
- `tuition.billing_basis`: `PER_CREDIT`
- `language`: string or `null`

## Official Gaps

- NDU does not publish a distinct public graduate registration fee in a centralized way.
- NDU does not publish a standalone graduate assistantship policy in a single central location.
- NDU does not publish a separate graduate-only language-of-instruction policy beyond admissions English requirements.
- A graduate-only academic calendar was not isolated from the general academic calendar.
- Some official program pages omit thesis/non-thesis, interview, or experience details, so those fields remain null where not explicitly published.

## Recommendation

**APPROVE WITH NOTES**

NDU is ready for V31 seed generation.
