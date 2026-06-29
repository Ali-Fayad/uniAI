# BAU Normalization Report

**Task:** `BAU_NORMALIZATION_001`

## Summary
The BAU graduate dataset is normalized to the strict MASTER/PHD import scope.

## Files Updated
- `programs.json`
- `university.json`
- `sources.json`
- `fees_mapping_summary.json`
- `shared_report.md`

## Official Program URLs
- Coverage: 109 / 109 in-scope graduate programs.
- Source: canonical program links extracted from official BAU graduate faculty listing pages.
- Remaining nulls: 0.

## Tuition
- Coverage: 109 / 109 in-scope graduate programs.
- Source: official BAU Graduate Studies Tuition Fees PDF for academic year 2025-2026.
- Billing basis: `program_total` for single published rows; `program_total_by_published_variant` where BAU publishes multiple routes or credit bands.
- Currency: USD.
- LBP conversion column preserved as `*_lbp_rate_89500` fields.
- Recurring global fees preserved in `fees_mapping_summary.json` and `university.json`:
  - Healthcare fees: USD 100 per semester.
  - Technology Fees: USD 100 per semester.

## Shared Information
Shared graduate information was moved into `university.json`, including:
- Admissions criteria.
- Required documents.
- Language requirements.
- Deadlines.
- Scholarships and financial aid links.
- Regulations and postgraduate bylaws.
- Accreditation link.
- International student note.
- Academic calendar link.
- Tuition and recurring fee notes.

Program records now leave `admission_requirements` as `null` unless program-specific admissions are later verified from official BAU program pages.

## Sources
- Source IDs: no duplicates.
- Source URLs: no duplicates.
- Each source now has `access_date`.
- Each source now has `notes`.
- Tuition source status updated to `downloaded_rendered_official_pdf`.

## Validation
- JSON parse: passed.
- Duplicate program IDs: none.
- Duplicate source IDs: none.
- Duplicate source URLs: none.
- Programs without sources: none.
- Missing source references: none.
- Non-BAU official program URLs: none.
- Tuition objects without official source reference: none.
- Source entries missing `access_date`: none.
- Source entries missing `notes`: none.

## Remaining Nulls
- `admission_requirements`: 109 program records, intentionally centralized in `university.json`.
- `language`: still null at program level unless BAU publishes a program-specific language.
- `delivery_mode`: still null at program level unless BAU publishes a program-specific delivery mode.
- `tracks_concentrations`: still null unless explicitly published.
- `gre_gmat`, `interview`, `experience`, and `accreditation`: still null unless explicitly published at program level.
- `university.payment_plans`: null because no official graduate payment-plan details were identified in the reviewed BAU sources.

## Recommendation
**APPROVE WITH NOTES**

The dataset is ready for acceptance with the remaining nulls treated as explicit official-source gaps, not normalization defects.
