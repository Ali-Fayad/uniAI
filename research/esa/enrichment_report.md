# ESA Enrichment Report

Task code: ENRICHMENT_ESA_001
University: École Supérieure des Affaires (ESA Business School)
Date accessed: 2026-07-07

## Summary

This enrichment pass only populated fields that were explicitly supported by the official ESA sources reviewed in discovery and shared-data handling.

## Fields populated

- `program_description` for all 10 records
- `duration` for 4 records:
  - MIM: 24 months
  - MIAD: 20 months
  - EMLux: 15 months
  - DBA: left null because the reviewed official sources did not expose a clean stable duration value in the source map notes

## Programs updated

- Master in International Management (MIM)
- Master in Innovation and Entrepreneurship (MENT)
- Specialized Master in International Affairs and Diplomacy (MIAD)
- Master in Business Administration (MBA)
- Executive Master in Luxury Transformation and Leadership (EMLux)
- Specialized Master in Marketing and Communication (MMC)
- Master Exécutif en Management de la Santé / Executive Master in Healthcare Management (MEMS)
- Executive Master in Financial Management (EMFM)
- Executive MBA (EMBA)
- Doctorate in Business Administration (DBA)

## Remaining nulls

The following fields remain null for all or most records because the official ESA pages inspected in this pass did not expose stable, machine-safe values:

- `credits`
- `thesis_or_non_thesis`
- `delivery_mode`
- `language`
- `admission_requirements`
- `gre_requirement`
- `gmat_requirement`
- `portfolio_requirement`
- `interview_requirement`
- `experience_requirement`
- `accreditation`

## Notes

- GEMBA remains modeled as a track under EMBA and was not split into a standalone record.
- Tuition was not changed.
- `sources.json` was not modified.
- No inferred values were introduced.

## Recommendation

APPROVE WITH NOTES
