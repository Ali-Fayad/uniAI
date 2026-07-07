# ESA Final Quality Report

Task code: FINAL_QA_ESA_001  
University: École Supérieure des Affaires (ESA Business School)  
Official website: https://www.esa.edu.lb  
Date accessed: 2026-07-07

## Verdict

APPROVE WITH NOTES. ESA is ready for seed generation.

## Validation

- All `research/esa/*.json` parse: pass
- Program count remains consistent: pass
- MASTER count: 9
- PHD count: 1
- Tuition remains correctly unmapped: pass
- No duplicate program IDs: pass
- No duplicate source IDs: pass
- No duplicate URLs: pass
- Every source reference resolves: pass
- Shared data centralized: pass
- GEMBA remains a track under Executive MBA: pass
- DBA remains the only doctoral record: pass
- V24 compatibility: pass
- `./mvnw -q -DskipTests compile`: pass

## Inventory summary

- Total programs: 10
- MASTER: 9
- PHD: 1
- Out-of-scope records: 0

## Tuition status

- Program tuition is intentionally unmapped at the program level.
- ESA's reviewed official pages point to brochures for fee detail, but the discovery pass did not normalize a stable numeric tuition table.

## Shared data status

- Shared admissions, language, scholarships/financial aid, and regulations were centralized in `research/esa/university.json`.
- No university-wide numeric tuition schedule was confirmed in the reviewed official sources.

## Source coverage

- Total official source records: 18
- Sources used by inventory records: 14
- Orphan sources:
  - `esa_home_en`
  - `esa_admissions_en`
  - `esa_financial_aid_en`
  - `esa_financial_aid_pdf_2025_2026`

## Completeness notes

- `program_description`: 10/10
- `duration`: 4/10
- `concentrations_or_tracks`: 1/10
- All other enrichment fields remain null where the official ESA sources did not expose a stable value.

## Modeling notes

- GEMBA was kept as a track under the Executive MBA because the official page describes it as an optional pathway for EMBA participants rather than a separate degree.
- DBA is the only doctoral record because ESA publishes a doctorate-level DBA and no separate PhD-branded program was confirmed.
- No out-of-scope graduate degree records were serialized in this inventory pass.

## Recommendation

APPROVE WITH NOTES
