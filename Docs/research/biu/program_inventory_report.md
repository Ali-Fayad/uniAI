# BIU Graduate Program Inventory Report

Date accessed: 2026-07-06
University: Beirut Islamic University (BIU)
Official website: https://www.biu.edu.lb
Task code: INVENTORY_BIU_001

## Summary

- MASTER records in `programs.json`: 3
- PHD records in `programs.json`: 3
- Out-of-scope records: 2
- Total in-scope graduate records: 6

## In-Scope Programs

- Master of Usul al-Fiqh
- Master of Comparative Fiqh
- Master of Islamic Studies
- Doctorate in Usul al-Fiqh
- Doctorate in Comparative Fiqh
- Doctorate in Islamic Studies

## Out-of-Scope Programs

- Preparatory Master's in Islamic Studies
- Preparatory Master's in Law

## Validation

- `research/biu/programs.json` parses successfully.
- `research/biu/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- Every `source_id` in `programs.json` exists in `research/biu/sources.json`.
- Every `source_id` in `out_of_scope_programs.json` exists in `research/biu/sources.json`.
- All non-null `official_program_url` values belong to official BIU domains. In this pass, all retained program URLs are `null`.
- `programs.json` contains only MASTER and PHD records.
- No broken source references were introduced.

## Modeling Notes

- BIU's official master's requirements page directly supports three master's specializations: Usul al-Fiqh, Comparative Fiqh, and Islamic Studies.
- BIU's official doctorate requirements page directly supports three doctorate specializations in the same fields.
- The syllabus index also mentions `ماجستير في القضاء الشرعي`, but no dedicated program page or other program-specific source was recovered in this pass, so it was not serialized to avoid inference.
- No dedicated program-detail pages were recovered for the six in-scope records, so `official_program_url` is `null` for each one.

## Recommendation

APPROVE WITH NOTES
