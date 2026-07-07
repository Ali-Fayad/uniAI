# HU Discovery Source Map

Task code: `HU_DISCOVERY_001_BROWSER_INV`  
University: Haigazian University (HU)  
Official site: https://www.haigazian.edu.lb  
Date accessed: 2026-07-04  
Scope: MASTER and PHD only. Bachelor, diplomas, certificates, continuing education, training, professional programs, news, and events were excluded.

## Source coverage by discovery area

| Area | Primary source IDs | Notes |
|---|---:|---|
| Official homepage and navigation | HU_SRC_001 | Used to confirm site navigation and official domain. |
| Institutional/academic profile | HU_SRC_002, HU_SRC_032 | HU states it follows the U.S. model, uses English as language of instruction, and offers MA/MBA degrees. |
| Graduate program index | HU_SRC_003 | Primary source for in-scope program list and credits. |
| MBA programs | HU_SRC_004, HU_SRC_005, HU_SRC_006, HU_SRC_007, HU_SRC_008, HU_SRC_009, HU_SRC_032 | Official PDFs for General Business Administration, Accounting, Finance, HR Management, Management, and Marketing. Some PDFs duplicate combined catalog sections. |
| MA programs | HU_SRC_010, HU_SRC_011, HU_SRC_032 | Official PDFs for Education and Psychology. |
| PHD / Doctorate discovery | HU_SRC_003, HU_SRC_032 | No in-scope PhD/doctorate program found on the official graduate index or catalog. |
| Faculties / schools / divisions | HU_SRC_003, HU_SRC_031, HU_SRC_032 | Graduate programs map to Faculty of Business Administration and Economics and Faculty of Social and Behavioral Sciences. Faculty page also shows broader School of Arts and Sciences divisions. |
| Graduate admissions | HU_SRC_015, HU_SRC_016, HU_SRC_017, HU_SRC_018, HU_SRC_032 | Graduate admissions pages were sparse, but identify admission categories and online application path. Catalog should be used for fuller admissions details. |
| Required documents | HU_SRC_016, HU_SRC_023, HU_SRC_029, HU_SRC_032 | Graduate-specific document details were not fully machine-readable on HTML pages; forms and catalog are the safest official sources. |
| Tuition / fees | HU_SRC_019, HU_SRC_020, HU_SRC_021, HU_SRC_032 | Tuition policy, installment plan, subsidy, delinquency policy, calculator page, and catalog financial section found. Extracted HTML did not expose a full rate table. |
| Scholarships / financial aid | HU_SRC_022, HU_SRC_023, HU_SRC_024, HU_SRC_025, HU_SRC_026, HU_SRC_027, HU_SRC_032 | Financial aid pages include need-based aid, scholarships, graduate eligibility, application documents, and contact details. |
| Payment plans | HU_SRC_019, HU_SRC_032 | Installment payment policy found on Tuition & Fees page. |
| Academic calendar | HU_SRC_028, HU_SRC_032 | Academic calendar index includes current/recent calendars. |
| Graduate regulations / handbook | HU_SRC_012, HU_SRC_013, HU_SRC_014, HU_SRC_032 | Graduate academic rules and handbook found. |
| Language requirements | HU_SRC_002, HU_SRC_032 | HU profile states English is the language of instruction. TOEFL-specific details should be checked in catalog/admissions pages during seeding if needed. |
| International students | HU_SRC_030 | Page appears undergraduate/special-category focused; no graduate-specific international policy found in extracted content. |
| Official PDFs / catalogs | HU_SRC_004-HU_SRC_014, HU_SRC_032 | Program PDFs, graduate handbook PDF, and university catalog found. |

## In-scope program source map

| Program / track label on HU source | Degree | Credits | Faculty / school | Primary source IDs | Status |
|---|---:|---:|---|---|---|
| General Business Administration | MBA | 39 | Faculty of Business Administration and Economics | HU_SRC_003, HU_SRC_004, HU_SRC_032 | In scope |
| Accounting | MBA | 39 | Faculty of Business Administration and Economics | HU_SRC_003, HU_SRC_005, HU_SRC_032 | In scope |
| Finance | MBA | 39 | Faculty of Business Administration and Economics | HU_SRC_003, HU_SRC_006, HU_SRC_032 | In scope |
| Human Resources Management | MBA | 39 | Faculty of Business Administration and Economics | HU_SRC_003, HU_SRC_007, HU_SRC_032 | In scope |
| Management | MBA | 39 | Faculty of Business Administration and Economics | HU_SRC_003, HU_SRC_008, HU_SRC_032 | In scope |
| Marketing | MBA | 39 | Faculty of Business Administration and Economics | HU_SRC_003, HU_SRC_009, HU_SRC_032 | In scope |
| Education | MA | 33 | Faculty of Social and Behavioral Sciences | HU_SRC_003, HU_SRC_010, HU_SRC_032 | In scope |
| Psychology | MA | 33 | Faculty of Social and Behavioral Sciences | HU_SRC_003, HU_SRC_011, HU_SRC_032 | In scope |

## Excluded / not seeded from discovery

- Undergraduate programs on `undergraduate-programs` and undergraduate catalog sections.
- Teaching Diplomas, certificates, and diplomas such as Hospitality Operations Certificate and Hospitality Management Diploma.
- Center for Continuing Education.
- News, announcements, events, jobs, photos, and calendar/news posts.
- External scholarship/fund sites linked from the Financial Aid Bulletin; they are not official HU content, though the HU page itself was recorded as context.

## Notes for next phase

- Treat the six MBA rows as either one MBA program with tracks/emphases or six MBA program records only if the application model requires track-as-program. HU lists them as separate rows in the graduate program index but the program PDFs share a common MBA degree framework.
- No PhD/doctorate program was found on the official graduate program index or catalog during discovery.
- Tuition rate extraction may need manual verification from the catalog or calculator because the HTML graduate tuition page exposed limited text.
