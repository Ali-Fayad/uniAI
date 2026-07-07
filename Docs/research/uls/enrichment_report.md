# ULS Enrichment Report

Date accessed: 2026-07-05
University: Université La Sagesse (ULS)
Official website: https://www.uls.edu.lb
Task code: ULS_ENRICHMENT_001

## Scope

- Program count remains 13.
- MASTER count remains 13.
- PHD count remains 0.
- DBA and other out-of-scope items remain excluded from `programs.json`.
- Program-level tuition remains null by design; tuition is centralized in `research/uls/university.json`.

## Fields Enriched

- `language`
- `accreditation`
- Existing official `credits`
- Existing official `concentrations_or_tracks`
- Existing official `admission_requirements`
- Existing official `interview_requirement`

## Program-Level Enrichment Summary

- Law masters: language set to `MULTILINGUAL`, accreditation set to `FIBAA`, credits retained at 27.
- FEBA MSc, MBA, and MIAGE: language set to `MULTILINGUAL`, accreditation set to `FIBAA`, credits retained at 42.
- Political Science and International Relations: language set to `MULTILINGUAL`, interview requirement retained as `ORAL_INTERVIEW`, credits retained at 39.
- Public Health: language set to `MULTILINGUAL`, interview requirement retained as `ORAL_INTERVIEW`; credits remain null because the official page presents `39 + 6` and was not converted.
- Hospitality Management: language set to `ENGLISH`, accreditation set to the official École Hôtelière de Lausanne certification, interview requirement retained as `ORAL_INTERVIEW`, credits retained at 39.
- Ecclesiastical Sciences: language set to `ARABIC`, concentrations retained, admission requirement retained as published, interview requirement retained as `ORAL_INTERVIEW`; credits remain null because the official page lists track-specific values rather than a single published program total.

## Validation

- `research/uls/programs.json` parses successfully.
- Program count remains 13.
- MASTER count remains 13.
- PHD count remains 0.
- Every source reference resolves against `research/uls/sources.json`.
- `./mvnw -q -DskipTests compile` passes from `Server/`.

## Notes

- The unlabeled Canon Law master was not serialized and remains excluded.
- No new official sources were introduced, so `research/uls/sources.json` was unchanged.
- No tuition values were added at program level.

## Recommendation

APPROVE WITH NOTES
