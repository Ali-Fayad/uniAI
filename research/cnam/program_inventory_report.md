# CNAM Lebanon Graduate Program Inventory Report

Task code: `INVENTORY_CNAM_001`
University: Cnam Lebanon / ISSAE-Cnam Liban
Date accessed: 2026-07-07
Official domain: https://www.cnam-liban.fr

## Summary

- MASTER count in `programs.json`: 3
- PHD count in `programs.json`: 0
- Out-of-scope count: 0

## In-scope programs

- Master Finance Parcours Finance d'entreprise et ingénierie financière
- Master mathématiques appliquées, statistique Parcours Science des données
- Master entrepreneuriat et management de projet Parcours Management de projet et d'affaires

## Validation

- `research/cnam/programs.json` parses successfully.
- `research/cnam/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- Every `source_id` in `programs.json` exists in `research/cnam/sources.json`.
- All non-null `official_program_url` values are official CNAM URLs.
- `programs.json` contains MASTER records only.
- No individual modules, certificates, continuing-education entries, professional-training entries, news, or events were created.

## Modeling Notes

- Only officially published graduate degree pages were serialized.
- The catalogue general page was used as supporting catalogue evidence, but no programme was inferred from module listings.
- `official_program_url` was retained for each supported Master because a dedicated official programme page exists.
- No PhD record was created because no official CNAM Lebanon PhD evidence was found.

## Recommendation

APPROVE WITH NOTES

CNAM Lebanon has official Master's evidence for three programs on its programme pages. No official PhD evidence was found, so the inventory is limited to the supported Master records only.
