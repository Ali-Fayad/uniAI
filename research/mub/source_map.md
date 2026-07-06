# MUB Source Map — Graduate Discovery

Task code: `BROWSER_MUB_DISCOVERY_001`  
University: Makassed University of Beirut (MUB)  
Official discovery domain used: `https://mub.edu.lb/`

## Domain note

The task prompt listed `https://www.makassed.edu.lb`, but the public official university pages discovered and inspected are hosted on `https://mub.edu.lb/`. The broader Makassed organization site is `makassed.org`; it was not used as program evidence. This discovery keeps source records to official MUB university URLs only.

## Discovery routes inspected

| Source ID | URL | Page type | Status | Graduate relevance |
|---|---|---|---|---|
| MUB_SRC_001 | https://mub.edu.lb/ | Official homepage | Reviewed | Institution identity, navigation, faculty list; no direct graduate evidence |
| MUB_SRC_002 | https://mub.edu.lb/faculties/ | Official faculty index | Reviewed | Lists three faculties; faculty existence alone not evidence |
| MUB_SRC_003 | https://mub.edu.lb/faculty-of-islamic-studies/ | Official faculty/program page | Reviewed | Primary MASTER and PHD evidence |
| MUB_SRC_004 | https://mub.edu.lb/tuition-fees/ | Official tuition page | Reviewed | Supporting graduate tuition evidence for Islamic Studies Master Diploma and PHD Diploma |
| MUB_SRC_005 | https://mub.edu.lb/apply-online/ | Official application form | Partially reviewed via search cache; direct fetch blocked | Supporting document requirements only; not primary evidence |
| MUB_SRC_006 | https://mub.edu.lb/financial-support/ | Official financial support form | Reviewed | Financial aid form; no graduate-specific rule found |
| MUB_SRC_007 | https://mub.edu.lb/news-and-events/academic-calendar/ | Official academic calendar | Reviewed | 2025-2026 calendar/holiday page; no graduate-specific calendar found |
| MUB_SRC_008 | https://mub.edu.lb/faculty-of-nursing-and-health-sciences/ | Official faculty page | Reviewed | Weak mission-level Masters reference only; no clear current graduate program listing captured |
| MUB_SRC_009 | https://mub.edu.lb/faculties/the-faculty-of-teacher-education/ | Official faculty page | Reviewed | Teaching Diplomas and certificates are out of scope; no in-scope graduate program evidence |

## Evidence map

### In-scope evidence

- `MUB_SRC_003` states that the Faculty of Islamic Studies focuses exclusively on graduate studies and offers master’s and doctoral programs.
- `MUB_SRC_003` describes the Master’s Degree structure, study phases, focus areas, and Master’s admission requirements.
- `MUB_SRC_003` describes the Doctoral Program as a minimum of three years focused on thesis preparation, with master’s degree admission requirement.
- `MUB_SRC_004` lists 2025-2026 tuition rows for Faculty of Islamic Studies Master Diploma and PHD Diploma.

### Out-of-scope or insufficient evidence

- `MUB_SRC_008`: Nursing page includes a mission-level sentence mentioning a Masters’ program, but the captured faculty programs section is Bachelor-focused. This was treated as weak evidence only, not enough to create an inventory program row.
- `MUB_SRC_009`: Teacher Education page lists Teaching Diploma and postgraduate professional certificates. These are explicitly out of scope.
- News, conferences, interviews, AI bootcamp pages, and faculty-profile references were ignored as evidence for graduate inventory.

## Discovery conclusion

Official graduate evidence was found for:

- MASTER: Faculty of Islamic Studies
- PHD: Faculty of Islamic Studies

A cautious graduate inventory can be created for Islamic Studies only after a follow-up extraction pass. Do not create graduate rows for Nursing or Teacher Education from this discovery alone.
