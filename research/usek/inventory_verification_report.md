# USEK Inventory Verification Report

## Verdict

APPROVE WITH NOTES

## Scope Checked

- `research/usek/programs.json`
- `research/usek/out_of_scope_programs.json`
- `research/usek/sources.json`
- `research/usek/program_inventory_report.md`
- Official USEK program pages already collected during discovery

## Expected vs Verified Count

- Expected graduate count from the official source set: 80
- Verified graduate count in `programs.json`: 80
- Expected MASTER count: 59
- Verified MASTER count: 59
- Expected PHD count: 21
- Verified PHD count: 21

## Verification Summary

### 1. MASTER records correspond to official graduate programs

Confirmed.

- Each MASTER record maps to a named graduate offering surfaced on an official USEK page.
- Tracks/emphases were not promoted to standalone rows.
- Hub pages were used only where USEK itself presents the official title list on a faculty hub or shared program page.

### 2. PHD records correspond to official doctoral programs

Confirmed.

- Each PHD record maps to a doctoral title surfaced on an official USEK page.
- Shared doctoral pages were split only when the page explicitly presents multiple distinct doctoral degrees.

### 3. Tracks / concentrations were not overcounted

Confirmed.

- MBA emphases such as Audit, Finance, Marketing, Financial Engineering, Human Resources, and Management and International Affairs were kept as tracks or separate officially published MBA offerings, not collapsed into extra rows.
- FAS education emphases were stored as tracks, not separate programs.
- Musicology was stored as a concentration for Master of Arts in Music.

### 4. Programs appearing on multiple faculty pages were not duplicated

Confirmed.

- Shared faculty or hub pages do not create extra rows beyond the officially named programs they list.
- The inventory uses one row per official title.

### 5. French / English mirrors were not duplicated

Confirmed.

- No separate mirror rows were added for the same program title in another language.
- Mixed-language official pages were used as one source surface when the official site presented one program title on that page.

### 6. MBA variants are legitimate separate offerings

Confirmed.

- The following are separate official USEK MBA offerings and are correctly represented as distinct rows:
  - Master of Business Administration
  - Master of Business Administration - Financial Engineering
  - Master of Business Administration - Human Resources
  - Master of Business Administration - Management and International Affairs

### 7. Joint / interdisciplinary programs are represented once

Confirmed.

- Cross-listed or partnered programs were represented once per official degree title.
- Examples:
  - Business Administration - Human Resources
  - Business Administration - Management and International Affairs
  - Master of Arts in Contemporary Art
  - Master of Arts in Digital Media

### 8. Hub pages did not create duplicate rows

Confirmed.

- Hub pages were used as the canonical official source for large program groups where USEK does not expose one stable unique URL per program in the discovered surface.
- The row count remains tied to official program titles, not to the number of page clicks or menu entries.

## Duplicate Candidates

No true duplicate program rows were found.

Potentially confusing but valid shared-source cases:

- `https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2`
  - Used as the hub source for 28 FAS master's titles plus two out-of-scope diplomas.
- `https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5`
  - Used as the doctoral hub source for 14 FAS PhD titles.
- `https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2`
  - Used as the engineering graduate hub source for 10 engineering master titles.
- `https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2`
  - Used as the law graduate hub source for 9 law master's titles, plus the separately verified Master in Criminology record.
- `https://www.usek.edu.lb/fmus/academic-program/phd?t=5`
  - Used for both doctoral music titles.

## Possible Missing Programs

One possible under-count remains unverified:

- Nursing graduate offerings.
  - Nursing surfaced indirectly in discovery and fee references.
  - A canonical graduate program page was not secured in the source set used for this inventory.
  - If USEK later exposes an official nursing graduate program page, it should be reviewed separately.

No other missing official graduate program surfaced from the collected USEK sources.

## Possible Over-counted Programs

None confirmed.

Notes:

- The Master in Criminology record is retained because USEK exposes it as a named graduate offering in the law graduate list and as a direct program page title, even though the captured body text on the page reused law content.
- The dual doctoral music page is counted as two distinct doctoral programs because the official page title explicitly lists both degrees.

## PhD Verification Table

| Faculty / Unit | Degree | Official Title | Source Surface |
| --- | --- | --- | --- |
| Business School | PHD | Ph.D. in Business | `https://www.usek.edu.lb/en/doctoral-studies/phd-in-business-2?t=5` |
| Business School | PHD | Doctorate in Business Administration | `https://www.usek.edu.lb/en/academic-programs/doctoral-studies/doctorate-in-business-administration?t=5` |
| School of Law and Political Sciences | PHD | Ph.D. in Law | `https://www.usek.edu.lb/en/law/phd-in-law-2?t=5` |
| Pontifical School of Theology | PHD | Ph.D. in Theology | `https://www.usek.edu.lb/en/pontifical-school-of-theology/phd-in-theology?t=5` |
| School of Medicine and Medical Sciences | PHD | Doctorate of Medicine | `https://www.usek.edu.lb/en/department-of-medical-sciences/doctorate-of-medicine-2?t=5` |
| School of Music and Performing Arts | PHD | Ph.D. in Music | `https://www.usek.edu.lb/fmus/academic-program/phd?t=5` |
| School of Music and Performing Arts | PHD | Ph.D. in Higher and Specialized Music Education | `https://www.usek.edu.lb/fmus/academic-program/phd?t=5` |
| Faculty of Arts and Sciences | PHD | Ph.D. in Agricultural and Food Sciences | `https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5` |
| Faculty of Arts and Sciences | PHD | Ph.D. in Arabic Language and Literature | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in Archeology and Art History | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in Chemistry and Life and Earth Sciences | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in Conservation, Restoration of Cultural Property & Sacred Art | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in Education Sciences | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in English Language and Literature | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in French Language and Literature | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in History | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in Language Sciences and Traductology | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in Philosophy | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in Psychology | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in Social Sciences | Faculty doctoral hub page |
| Faculty of Arts and Sciences | PHD | Ph.D. in Visual Arts | Faculty doctoral hub page |

## Final Recommendation

APPROVE WITH NOTES

