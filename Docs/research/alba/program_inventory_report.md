# ALBA Graduate Program Inventory Report

Date accessed: 2026-07-07  
University: Académie Libanaise des Beaux-Arts (ALBA)  
Official website: https://www.alba.edu.lb  
Task code: INVENTORY_ALBA_001

## Summary

- MASTER records in `programs.json`: 8
- PHD records in `programs.json`: 0
- Out-of-scope records: 1
- Total in-scope graduate records: 8

## In-Scope Programs

- Master en Architecture d'Intérieur
- Master en Design Global
- Master en Graphisme et Publicité
- Master en Illustration et Bande Dessinée
- Master en Animation 2D/3D
- Master en Réalisation Cinéma
- Master en Production Audiovisuelle
- Master en Design Urbain

## Out-of-Scope Programs

- Licence en Design de Mode

## Validation

- `research/alba/programs.json` parses successfully.
- `research/alba/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- Every `source_id` in `programs.json` exists in `research/alba/sources.json`.
- Every `source_id` in `out_of_scope_programs.json` exists in `research/alba/sources.json`.
- All non-null `official_program_url` values are official ALBA URLs.
- `programs.json` contains only MASTER records.
- `programs.json` contains zero PHD records.

## Modeling Notes

- ALBA has official Master's evidence on its own program pages and official Master application PDF.
- No official ALBA PhD program page, PhD admissions page, graduate catalog entry, or official PhD program PDF was found in the discovery report.
- The Master in Global Design is preserved as one master record; it is not split into separate degrees.
- The Institute of Urbanism page mentions landscape training, but no separate official graduate title was recovered, so only the explicit Master en Design Urbain record was serialized.
- The fashion school page exposes a licence-level program only, which is captured in `out_of_scope_programs.json`.

## Recommendation

APPROVE WITH NOTES
