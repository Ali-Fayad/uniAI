# ULS Graduate Program Inventory Report

Date accessed: 2026-07-05
University: Université La Sagesse (ULS)
Official website: https://www.uls.edu.lb
Task code: ULS_INVENTORY_001

## Summary

- MASTER count in `programs.json`: 13
- PHD count in `programs.json`: 0
- Out-of-scope count: 3

## In-scope programs

- Master in Private Law
- Master in Public Law
- Master in Comparative Law
- Master in Digital Law
- Master of Science in Business Administration and Finance (MSc)
- Professional Master in Business Administration and Finance (MBA)
- Double Master in Management Information Systems (MIAGE)
- Master in Political Science and International Relations
- Professional Master in Political Science and International Relations
- Professional Master in Political Science and International Relations
- Master in Hospital Management
- Master in Hospitality Management
- Master in Ecclesiastical Sciences

## Out-of-scope programs

- Doctorate in Business Administration (DBA)
- Diplôme d’établissement Étudiant-Entrepreneur (D2E)
- Diplôme pour avocats Droit canonique

## Validation

- `research/uls/programs.json` parses successfully.
- `research/uls/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- Every `source_id` in `programs.json` exists in `research/uls/sources.json`.
- Every `source_id` in `out_of_scope_programs.json` exists in `research/uls/sources.json`.
- All non-null `official_program_url` values are official ULS URLs.
- `programs.json` contains MASTER records only.
- `programs.json` contains zero PHD records.
- No official PhD program was found in the provided ULS source set.

## Modeling notes

- The Faculty of Economics and Business Administration publishes a DBA, but this task scope is MASTER/PHD only, so the DBA is preserved in `out_of_scope_programs.json`.
- The FEBA graduate hub also publishes the D2E diploma, which is out of scope for the current inventory.
- The Faculty of Canon Law graduate hub shows a diploma for lawyers on the same page as graduate programs, so it is preserved out of scope.
- The same Canon Law page also shows an unlabeled Master line. It was not serialized because the official title is not explicit enough to avoid inference.
- The public-hub language statements are captured conservatively; program rows were not split by inferred language variants.
- The official source set does not provide a conventional PhD program list.

## Recommendation

APPROVE WITH NOTES
