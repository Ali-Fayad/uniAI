# LIU Seed QA Report

## Verdict
APPROVE WITH NOTES

LIU is ready for V34 seed generation.

## Validation
- V34 ordered after V33: pass
- All `research/liu/*.json` parse: pass
- `programs.json` records: 15
- MASTER count: 15
- PHD count: 0
- `out_of_scope_programs.json` records: 2
- Tuition rows: 15
- Out-of-scope rows skipped: pass
- MBA emphases seeded as tracks/concentrations, not separate programs: pass
- Catalogue URL reuse handled correctly: pass
- No broken source references: pass
- Every program has source links: pass
- V24 enum/check compatibility: pass
- Idempotency and duplicate prevention: pass
- Row counts match `research/liu/import_report.md`: pass
- `./mvnw -q -DskipTests compile`: pass

## Duplicate / Reuse Review
- Duplicate program IDs: none
- Duplicate source IDs: none
- Duplicate source URLs: none
- Intentional duplicate official program URL group:
  - `https://syslb.liu.edu.lb/syslbdatadir/Documents/09_University_Catalog.pdf`
  - `liu-sobu-mbat`
  - `liu-soe-ms-electronics-engineering`
  - `liu-soe-ms-electronics-engineering-biomedical-emphasis`
  - `liu-soas-ms-mathematics-applied-mathematics`

## Notes
- MBA emphases are correctly represented as `CONCENTRATION` tracks under the single MBA record.
- The LIU catalogue PDF reuse is intentional and required to preserve the official catalog-only program titles.
- The LIU seed migration intentionally omits accreditation because no distinct LIU graduate accreditation source was isolated in the reviewed package.

