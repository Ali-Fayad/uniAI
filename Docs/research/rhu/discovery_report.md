# RHU Discovery Report

Task code: RHU_DISCOVERY_001_BROWSER_INV  
Institution: Rafik Hariri University (RHU)  
Official website: https://www.rhu.edu.lb  
Date accessed: 2026-07-04  
Recommendation: APPROVE WITH NOTES

## Scope result

Discovery-only pass completed for RHU using official RHU pages and official RHU PDFs only.

Created files:

- `research/rhu/source_map.md`
- `research/rhu/sources.json`
- `research/rhu/discovery_report.md`

Not created:

- `programs.json`
- `university.json`
- DB migrations
- seed files

## Executive summary

RHU has official graduate-level offerings in Business Administration and Engineering. The strongest source is the official `RHU_Graduate_Catalog_2025-2026.pdf`, supported by the graduate programs web page and current 2025-2026 tuition/fees PDF.

Discovered in-scope programs:

| Level | Program / option | College | Notes |
|---|---|---|---|
| MASTER | MBA - General Track / General Business Management | College of Business Administration | Catalog treats General Business Management as an MBA specialization/emphasis. |
| MASTER | MBA - Oil and Gas Management | College of Business Administration | Catalog treats Oil and Gas Management as an MBA specialization/emphasis. |
| MASTER | MS in Civil and Environmental Engineering | College of Engineering | Engineering MS program. |
| MASTER | MS in Biomedical Engineering | College of Engineering | Engineering MS program. |
| MASTER | MS in Computer and Communications Engineering | College of Engineering | Engineering MS program. |
| MASTER | MS in Electrical Engineering | College of Engineering | Engineering MS program. |
| MASTER | MS in Mechanical Engineering | College of Engineering | Engineering MS program. |
| MASTER | MS in Mechatronics Engineering | College of Engineering | Engineering MS program. |
| PHD | None found | N/A | No official PhD / Doctorate program found in inspected RHU sources. |

## Primary findings

### 1. Graduate programs

The graduate programs page lists RHU graduate offerings by college. It lists two MBA options under the College of Business Administration and six MS engineering disciplines under the College of Engineering.

The Graduate Catalog 2025-2026 confirms that RHU graduate programs are concentrated in:

- College of Business Administration: MBA Program.
- College of Engineering: MS programs in engineering disciplines.

### 2. MBA structure

The MBA program is a 36-credit graduate program. The catalog describes two specialization options/emphases:

- General Business Management.
- Oil and Gas Management.

Seed note: prefer modeling these as tracks/concentrations under MBA unless the existing schema requires separate program rows.

### 3. Engineering MS structure

Engineering MS programs support thesis and non-thesis options. The catalog states that to earn an MS degree in engineering, students complete 30 credits for the thesis option or 33 credits for the non-thesis option, with additional minimum credit requirements depending on whether the student enters with a BS or BE background.

Seed note: the web graduate programs page displays minimum credit requirements beyond a bachelor’s degree. Use the catalog as canonical for thesis/non-thesis structure and requirements.

### 4. Admissions and required documents

Graduate admission is merit-based and available to applicants holding undergraduate degrees from RHU or other recognized institutions.

Required documents found across the graduate admission requirements page and Graduate Catalog include:

- Certified copy of undergraduate degree and equivalence from the Ministry of Higher Education.
- Official transcripts attested by the Ministry of Higher Education.
- Certified copy of Lebanese Official High School Certificate or equivalent.
- RHU English Entrance Exam or standardized English competency exam.
- Recommendation letters.
- Additional application package items listed in the Graduate Catalog.

### 5. Application deadlines and methods

The Before Applying page states that undergraduate and graduate applications must be completed and submitted with all required documents no later than:

- August 25 for Fall Semester.
- December 25 for Spring Semester.

Application methods:

- In person at RHU Admissions Office, Block E, Mechref Village, Damour, Lebanon.
- Online through the official RHU application link referenced on the RHU page.

### 6. Tuition, fees, and payment plans

The 2025-2026 tuition and fees PDF is the current preferred fee source.

Key fee findings:

- College of Business Administration graduate program: 5,300,000 LBP / USD 200 per credit.
- College of Engineering graduate program: 8,100,000 LBP / USD 280 per credit.
- Service fees, enrollment, deposit, NSSF, late registration, late payment, transportation, parking, and dormitory fees are listed.
- Deferred payment is listed as a payment option, with tuition fees scheduled over four installments for fall/spring semesters and two installments for summer.

### 7. Financial aid and graduate assistantship

The Graduate Catalog includes both Financial Aid Program and Graduate Assistantship sections. Graduate assistantships are described as limited merit-based assistantships offered every term, excluding summer.

Seed note: do not infer universal eligibility or exact award amounts without deeper source extraction in seed phase.

### 8. Academic calendar and regulations

The Graduate Catalog 2025-2026 includes:

- Academic Calendar 2025-2026.
- Graduate academic regulations.
- Period of study.
- Orientation.
- Supervision.
- Courses and grades.
- Course load.
- Plan of study.
- Academic standing.
- Graduate assistantship.
- Graduation requirements.

### 9. International students / passport and visa

The Graduate Catalog includes a Passport and Visa section. The Before Applying page and fee documents include housing/dormitory information. No separate international graduate admissions page was confirmed during this discovery pass.

## Sources discovered

See `research/rhu/sources.json` and `research/rhu/source_map.md`.

## Validation results

- `sources.json` parses: PASS
- Duplicate source IDs: PASS
- Duplicate URLs: PASS
- Official RHU URLs only: PASS
- Compile: not required

## Recommendation

APPROVE WITH NOTES.

Notes:

1. Use `RHU_Graduate_Catalog_2025-2026.pdf` as the canonical source in the seed phase.
2. Use `tuitionandfees2526.pdf` as the canonical current tuition/fees source.
3. MBA options should likely be modeled as tracks/concentrations, not independent degree families, unless the import schema requires separate program rows.
4. PHD count should be 0 based on the inspected official sources.
5. Avoid undergraduate catalogs and undergraduate program pages in the seed phase except for cross-reference context already embedded in graduate requirements.
