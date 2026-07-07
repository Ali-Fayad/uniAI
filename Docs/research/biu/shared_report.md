# BIU Shared Graduate Data Report

Task code: `SHARED_BIU_001`
University: Beirut Islamic University (BIU)
Date accessed: 2026-07-06

## Scope Summary

- In-scope programs: 6
- MASTER records: 3
- PHD records: 3
- Out-of-scope records: 2
- Tuition rows mapped: 0
- Tuition null: 6

## Centralized Shared Data

- Admissions process and stage-specific document requirements are centralized in `research/biu/university.json`.
- Language requirements remain centralized in `research/biu/university.json` because no numeric graduate threshold was published.
- Tuition remains centralized as "not published" in `research/biu/university.json`; no numeric graduate tuition table was recovered.
- Application fee and registration fee remain null in `research/biu/university.json` because the recovered fee page did not expose numeric values.
- Payment methods and payment plans are centralized as unpublished because no official graduate policy table was recovered.
- Scholarships and financial aid remain centralized as unpublished because no official graduate award or aid page was recovered.
- Academic calendar coverage is centralized through the official schedule and exam pages.
- Graduate regulations and research guidance are centralized through the postgraduate guidance and internal regulations pages.
- Accreditation / recognition evidence is centralized through the official accomplishments page.

## Tuition Mapping

- BIU publishes an official fees page, but the captured evidence did not expose machine-readable graduate tuition rows.
- No tuition amount was inferred.
- All six inventoried graduate programs keep `tuition` as `null`.

## Out-of-Scope Summary

- Preparatory Master's in Islamic Studies
- Preparatory Master's in Law

These remain excluded from the in-scope inventory because the task is limited to degree-level MASTER and PHD records.

## Validation

- `research/biu/programs.json` parses successfully.
- `research/biu/out_of_scope_programs.json` parses successfully.
- `research/biu/university.json` parses successfully.
- `research/biu/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were found.
- Every source reference in `research/biu/university.json` resolves to an entry in `research/biu/sources.json`.
- Every source reference in `research/biu/fees_mapping_summary.json` resolves to an entry in `research/biu/sources.json`.
- All cited URLs remain official BIU URLs.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Remaining Gaps

- No published graduate tuition table was recovered from the BIU fee page capture.
- No numeric graduate application or registration fee was recovered.
- No published graduate payment-method or payment-plan table was recovered.
- No graduate scholarship or financial-aid table was recovered.
- No dedicated graduate international-student policy page was recovered.

## Recommendation

APPROVE WITH NOTES
