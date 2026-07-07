# AUOT Graduate Program Discovery Report

Task code: `AUOT_DISCOVERY_001_BROWSER_INV`  
University: American University of Technology (AUT/AUOT)  
Official website: <https://www.aut.edu>  
Discovery type: browser-based official-source discovery only

## Recommendation

APPROVE WITH NOTES

## Executive summary

Official Master's evidence found.

No official PhD evidence found.

The strongest current evidence is the official AUT Catalogue 2025-2026. It states that AUT offers graduate programs leading to master's degrees through the Faculty of Business Administration and the Faculty of Applied Science & Technology, and identifies: Master of Business Administration, Master of Science in Computer Science, and Master of Science in Information Technology.

A separate official AUT page exists for the Master of Science in Computer Science. AUT also has an official page for a University of London Master of Laws (LLM) support program; this should be captured with a note because AUT describes itself as a support office/recognized teaching centre for the University of London LLM, not clearly as the awarding institution.

## Official graduate programs found

### MASTER

| Program | Degree type | Evidence status | Source IDs | Notes |
|---|---|---|---|---|
| Master of Business Administration | MASTER | Official catalog evidence | AUOT_SRC_004 | Catalog says MBA is 39 credit hours and fully accredited by the Ministry of Education and Higher Education. Concentrations listed include Accounting, Banking & Finance, Entrepreneurship, Hospitality and Tourism Management, Human Resources Management, Management, Management Information Systems, Marketing, and School Administration. |
| Master of Science in Computer Science | MASTER | Official catalog + official program page evidence | AUOT_SRC_004, AUOT_SRC_005 | Catalog and program page describe a 39-credit MS in Computer Science. |
| Master of Science in Information Technology | MASTER | Official catalog evidence | AUOT_SRC_004 | Catalog states the Department of Computer Science offers two Master of Science degree programs: one in Computer Science and one in Information Technology. No separate official IT master page was found in this pass. |
| University of London Master of Laws (LLM) through AUT support program | MASTER / external-award note | Official AUT support-program page | AUOT_SRC_006 | AUT states it is a support office/recognized teaching centre for the University of London Master of Laws distance-learning program. Treat as external-awarded/support-program evidence unless the data model supports partner-awarded programs. |

### PHD

No official PhD evidence found.

Searches and catalog/manual inspection found no official AUT PhD or doctorate program page, catalog entry, graduate admissions route, or official PhD PDF. Incidental mentions of PhD qualifications in careers/faculty/biographical contexts were excluded by scope.

## Admissions and requirements evidence

The AUT admissions page states that graduate applicants need a university degree and minimum GPA of 2.7, and must submit a certified copy of the university degree, updated CV, two recommendation letters, official bachelor's transcript, and bachelor's degree equivalency if needed.

The 2025-2026 catalogue adds that graduate applicants should hold a bachelor's degree from an accredited program, need a minimum GPA of 2.7 on a 4.0 scale, and students from non-English instruction institutions may be required to take International TOEFL with a minimum score of 550 or another internationally recognized English test.

## Tuition and fees evidence

The AUT admissions page lists graduate tuition as $315 per credit and the application fee as $50.

The 2025-2026 catalogue also lists graduate credit at $315, registration per semester at $400, activities/yearbook/technology/internet/library access per semester at $250, and application/admission fees including placement exams at $50.

## Scholarships and financial aid evidence

The admissions page states financial aid is available in discounts, work-study, grants, and scholarships, and that the financial aid application should be submitted before September 15 for Fall and January 15 for Spring.

The catalogue lists scholarship/financial aid categories including financial aid, merit, sibling, and sport, and states new admitted students may request financial aid.

## International students evidence

The AUT admissions page states applicants from international universities should certify official transcripts at the embassy of the concerned country in Lebanon and at the Lebanese Ministry of Foreign Affairs.

The catalogue states foreign students should secure an entry visa to Lebanon, the Admissions Office provides visa guidance, and passports must be valid for at least 13 months.

## Academic calendar, payment, and regulations evidence

The official footer links to the academic calendar, financial calendar, course offerings, student manual, and final exam schedule. The 2025-2026 catalogue contains academic calendar and regulatory sections, and the student manual is an official policy/regulations source.

## Excluded evidence

- Careers/job postings mentioning graduate courses, DBA, or PhD qualifications were excluded.
- News/events and graduation ceremony content were not used as primary program evidence.
- Faculty pages and partnership descriptions were not used to infer programs.
- External University of London pages were not added to `sources.json` because validation requires official AUOT URLs only.

## Validation

- `sources.json` parses successfully.
- No duplicate source IDs.
- No duplicate URLs.
- All URLs in `sources.json` are official AUOT/aut.edu URLs.

## Notes / risks

1. The task calls the university abbreviation `AUOT`, while the website and institution branding use `AUT`. Files use the requested `auot` folder and task code but preserve the official AUT naming in content.
2. The LLM page is official AUT content, but the degree appears to be University of London-awarded with AUT acting as support/recognized teaching centre. Do not import it as a normal AUT-awarded program unless the target schema supports external-awarded/partner programs.
3. The catalogue contains clear evidence for MS Information Technology, but a standalone official program page was not found during this pass.
4. The graduation page lists Master of Accounting and Executive MBA, but the current catalog evidence inspected in this pass did not clearly list them as active offered programs. Treat them as historical/ceremony corroboration only, not as importable active programs without stronger current catalog/program-page evidence.
