# BIU Program Enrichment Report

Task code: `ENRICHMENT_BIU_001`
University: Beirut Islamic University (BIU)
Date accessed: 2026-07-06

## Scope Summary

- In-scope programs reviewed: 6
- MASTER records: 3
- PHD records: 3
- Tuition unchanged for all programs

## Fields Enriched

- `duration`
- `thesis_or_non_thesis`

## Program-Level Enrichment Summary

- Master of Usul al-Fiqh: duration set to `1 academic year coursework; thesis period of 1-2 years`, thesis structure set to `THESIS`.
- Master of Comparative Fiqh: duration set to `1 academic year coursework; thesis period of 1-2 years`, thesis structure set to `THESIS`.
- Master of Islamic Studies: duration set to `1 academic year coursework; thesis period of 1-2 years`, thesis structure set to `THESIS`.
- Doctorate in Usul al-Fiqh: duration set to `3 years`, thesis structure set to `THESIS`.
- Doctorate in Comparative Fiqh: duration set to `3 years`, thesis structure set to `THESIS`.
- Doctorate in Islamic Studies: duration set to `3 years`, thesis structure set to `THESIS`.

## Fields Not Enriched

The following program-level fields remain unchanged because the inspected official BIU source set did not publish them explicitly:

- `concentrations_or_tracks`
- `admission_requirements`
- `language`
- `delivery_mode`
- `gre_requirement`
- `gmat_requirement`
- `portfolio_requirement`
- `interview_requirement`
- `experience_requirement`
- `accreditation`
- `credits`

## Validation

- `research/biu/programs.json` parses successfully.
- `research/biu/sources.json` parses successfully.
- Every source reference used by the program records resolves to an official BIU source.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Notes

- No excluded graduate programs were revisited.
- `research/biu/sources.json` was not modified.
- Tuition values were left unchanged.
- No unsupported fields were inferred.

## Recommendation

APPROVE WITH NOTES
