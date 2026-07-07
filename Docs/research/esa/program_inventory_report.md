# ESA Program Inventory Report

Task code: INVENTORY_ESA_001  
University: École Supérieure des Affaires (ESA Business School)  
Official website: https://www.esa.edu.lb  
Discovery date: 2026-07-07

## Inventory summary

- Total programs: 10
- MASTER count: 9
- PHD count: 1
- Out-of-scope count: 0

## Faculty breakdown

- ESA Business School: 10

## Program set

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

## Modeling decisions

- GEMBA was not serialized as a separate program record. The official ESA page describes it as an optional international pathway for EMBA participants, so it was modeled as a track under the Executive MBA rather than a standalone degree.
- MBA variants were not split into extra rows because the discovery evidence did not confirm a separate degree award.
- DBA was recorded as the doctoral evidence because ESA publishes a doctorate-level program but no separate PhD-branded degree.

## Source coverage

- All in-scope records reference official ESA source IDs.
- Official program URLs are limited to `esa.edu.lb`.
- No duplicate official program URL groups were created.

## Out-of-scope summary

- No graduate-related out-of-scope degree records were serialized in this pass.
- Executive education, certificates, short programs, seminars, news, events, and similar non-degree offerings were excluded during discovery and were not promoted into the inventory.

## Remaining ambiguities

- The official ESA materials describe GEMBA as an optional EMBA pathway. The current inventory records it as a track, not a separate degree.
- The reviewed official source map did not expose a stable, explicit named track list for MIM/MBA beyond what was visible in the discovery notes, so no additional unnamed tracks were inferred.

## Recommendation

APPROVE WITH NOTES
