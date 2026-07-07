# UL Inventory Expansion Report

## Result

Expanded the UL graduate inventory from 32 to 46 total records.

The final targeted pass found no additional safe title-level splits among the remaining grouped hubs.

## Validation

- JSON parses: pass
- Duplicate program IDs: none
- Duplicate program-title rows: none
- Every program has at least one source: pass
- Every source reference resolves: pass
- Only MASTER/PHD remain in programs.json: pass
- Compile: pass with ./mvnw -q -DskipTests compile

## Added Rows

- Neuroscience Research Center: 2 new master rows
- Faculty of Technology: 3 new master rows
- Faculty of Pharmacy: 6 new master rows
- Faculty of Dental Medicine: 3 new master rows

## Remaining Manual-Review Risks

- Faculty of Public Health
- Faculty of Law and Political and Administrative Sciences
- Faculty of Economics & Business Administration
- Faculty of Letters and Human Sciences
- Medical Research Center
- Faculty of Medical Sciences
- Fine Arts & Architecture
- Agronomy
- Pedagogy

## Final Pass Result

- Newly added programs: 0
- New inventory count: 46
- Grouped records remaining: 13
- Reason they could not be expanded: the reviewed official UL sources expose only hub-level or generic graduate-area language, not additional distinct program titles.

## Recommendation

APPROVE WITH NOTES
