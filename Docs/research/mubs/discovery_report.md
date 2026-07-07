# MUBS Discovery Report

Task code: `MUBS_DISCOVERY_001_BROWSER_INV`  
University: Modern University for Business and Science (MUBS)  
Official website: https://www.mubs.edu.lb  
Date accessed: 2026-07-04  
Scope: Master and PhD only  
Recommendation: **APPROVE WITH NOTES**

## Executive summary

A browser-based discovery pass was completed using only official MUBS domains/subdomains and official MUBS PDFs. The current site exposes graduate-related information across the Admission, Faculties, Cardiff Metropolitan University at MUBS, Computer Science Department, Financial Aid, UMS/forms, and catalogue PDF areas.

The strongest current master-level confirmations found are:

1. **Master of Computer Science (MCS)** under the Computer Science Department / Faculty of Arts and Sciences.
2. **Cardiff Metropolitan University MBA programmes at MUBS**, including MBA General plus named MBA pathways/tracks shown on the Cardiff Met at MUBS page.

No current official PhD / doctorate program page was found during this pass.

## Access notes / limitations

Several official pages were discoverable through site navigation but returned a verification/loader, timeout, or fetch failure during browser inspection:

- `https://mubs.edu.lb/en/admission/graduate-majors.aspx` (`MUBS-SRC-003`)
- `https://www.mubs.edu.lb/en/admission/admission-requirements.aspx` (`MUBS-SRC-004`)
- `https://mubs.edu.lb/en/admission/tuition.aspx` (`MUBS-SRC-005`)
- `https://www.mubs.edu.lb/en/academics_mubs/academic-calendar.aspx` (`MUBS-SRC-024`)
- `https://www.mubs.edu.lb/en/academics_mubs/rules.aspx` (`MUBS-SRC-025`)

Because of this, program discovery relies on accessible current pages, official application/info forms, and official MUBS catalogue PDFs. Older catalogues are treated as legacy evidence unless a current page/form also confirms the same program.

## Faculties / academic units found

| Unit | Source IDs | Notes |
|---|---|---|
| Faculty of Business Administration | MUBS-SRC-007 | Current faculty page. Links to Cardiff Metropolitan University at MUBS. No direct local master list visible in fetched content. |
| Faculty of Health Sciences | MUBS-SRC-008 | Current faculty page. No in-scope master/PhD program details visible in fetched content. |
| Faculty of Arts and Sciences / Computer Science Department | MUBS-SRC-009, MUBS-SRC-010 | Computer Science Department page confirms the MCS. |
| Faculty of Fine Arts and Design | MUBS-SRC-011 | No in-scope master/PhD program details found. |
| Cardiff Metropolitan University at MUBS | MUBS-SRC-012, MUBS-SRC-013, MUBS-SRC-014, MUBS-SRC-015, MUBS-SRC-016, MUBS-SRC-017, MUBS-SRC-023 | Current Cardiff Met pages/forms confirm MBA offerings and fee/info request flow. |

## In-scope graduate programs found

### Current / strongly supported

| Program | Level | Unit | Evidence | Notes |
|---|---:|---|---|---|
| Master of Computer Science (MCS) | MASTER | Computer Science Department, Faculty of Arts and Sciences | MUBS-SRC-009, MUBS-SRC-018 | Current department page states the MCS is designed for graduates with a CS bachelor’s and offers advanced study paths in Software Development, Cyber Security, Artificial Intelligence, and Internet of Things. UMS application URL also confirms the major name. |
| MBA Master of Business Administration (General) | MASTER | Cardiff Metropolitan University at MUBS | MUBS-SRC-012, MUBS-SRC-013, MUBS-SRC-023 | Current program page includes overview, core modules, pathway modules, final project, employability, and application steps. |
| MBA in Project Management | MASTER | Cardiff Metropolitan University at MUBS | MUBS-SRC-012, MUBS-SRC-013, MUBS-SRC-023 | Listed in Cardiff Met at MUBS programmes at a glance. MBA General page module text includes project-management pathway modules; treat as a Cardiff MBA pathway/program depending on final model rules. |
| MBA in Human Resource Management | MASTER | Cardiff Metropolitan University at MUBS | MUBS-SRC-012 | Listed in Cardiff Met at MUBS programmes at a glance. Separate detailed page was not fetched in this pass. |
| MBA in Marketing | MASTER | Cardiff Metropolitan University at MUBS | MUBS-SRC-012 | Listed in Cardiff Met at MUBS programmes at a glance. Separate detailed page was not fetched in this pass. |
| MBA in Health Sector Management | MASTER | Cardiff Metropolitan University at MUBS | MUBS-SRC-012 | Listed in Cardiff Met at MUBS programmes at a glance. Separate detailed page was not fetched in this pass. |
| MBA in Supply Chain & Logistics Management | MASTER | Cardiff Metropolitan University at MUBS | MUBS-SRC-012 | Listed in Cardiff Met at MUBS programmes at a glance. Also referenced by current Cardiff pages as a launched programme/news item, but news was not used as a primary source. |

### Legacy / needs current confirmation before seeding

| Program | Level | Unit | Evidence | Notes |
|---|---:|---|---|---|
| Master in Social Work | MASTER | Social Work / legacy Faculty of Education and Social Work context | MUBS-SRC-019 | Official 2012 catalogue references admission/transfer to Master in Social Work. No current page confirming active offering was found. Do not seed as current without additional verification. |
| MBA in Marketing and Entrepreneurship | MASTER | Business / legacy catalogue context | MUBS-SRC-019 | Official 2012 catalogue/search snippet references this MBA. No current page confirming active offering was found. Do not seed as current without additional verification. |
| Historical MBA / Business Administration references | MASTER | Business / legacy catalogue context | MUBS-SRC-020, MUBS-SRC-022 | Older catalogues mention MBA and graduate admissions. Use only for historical context or if current source confirms active offering. |

## PhD / doctorate findings

No current official MUBS PhD or doctorate program offering was found. Staff biographies and faculty credentials sometimes mention doctorates held by staff, but these are not program offerings and were excluded.

## Admissions findings

Graduate admission details were found in official catalogue PDFs rather than fully accessible current admissions pages.

From `MUBS-SRC-019` / `MUBS-SRC-022` legacy catalogue evidence:

- Graduate admission is based on evidence that the applicant has attained minimal academic proficiency and can successfully pursue a master degree.
- Clear admission to a master program requires a bachelor degree from a fully accredited higher-education institution.
- Minimum overall undergraduate GPA stated in catalogue evidence: **2.75**.
- Required graduate admission documents listed in catalogue evidence:
  - Application form
  - Certified copies of official certificates/diplomas and official transcripts
  - Identity card or passport photocopy and family status document
  - Two recommendation letters
  - MUBS Graduate Admission Test
  - TOEFL score requirement in catalogue evidence: paper-based 600 / computer-based 250 / internet-based 100
  - CV
  - Two photos

Notes:

- Current admissions requirement page was discovered but could not be fully fetched due verification/loader behavior.
- Treat catalogue admission values as official but potentially outdated until cross-checked against the current admissions page or admissions office.

## Tuition / fees findings

| Area | Finding | Sources |
|---|---|---|
| MUBS general tuition | Current tuition page was discovered but returned verification/loader during fetch. No current MUBS-local graduate tuition amount was confirmed. | MUBS-SRC-005 |
| Cardiff Met graduate tuition | Dual-Degree Programme fees and Hybrid Programme fees are available upon request through forms. No numeric amount published in fetched page content. | MUBS-SRC-014, MUBS-SRC-016, MUBS-SRC-017 |
| Payment plan / tuition dates | Homepage/current calendar preview listed academic dates including a third tuition payment deadline for Summer Semester and withdrawal/evaluation dates. Full academic calendar page could not be fetched. | MUBS-SRC-001, MUBS-SRC-024 |

## Scholarships / financial aid findings

The MUBS financial aid overview states that financial aid helps students/families cover higher education expenses and is provided to qualifying students based on academic achievements and demonstrated needs. It lists forms of aid including:

- Scholarships
- Academic merits
- Athletic scholarship
- Need-based grants

It also states annual aid is over $3.5M. Source: `MUBS-SRC-006`.

Cardiff Met financial aid page was accessible but sparse; it primarily showed application steps and contact details. Source: `MUBS-SRC-015`.

## Academic calendar / regulations findings

- Academic calendar URL found: `MUBS-SRC-024`; full fetch returned verification/timeout. Homepage preview showed upcoming academic dates, including tuition payment and withdrawal/evaluation events.
- Rules & Regulations URL found: `MUBS-SRC-025`; direct fetch failed. Catalogue PDFs include historical academic regulations sections but should be treated as legacy unless current version is confirmed.

## Language requirements

Catalogue evidence lists TOEFL score requirements for graduate admissions:

- Paper-based: 600
- Computer-based: 250
- Internet-based: 100

Source: `MUBS-SRC-019`. Current admission requirement page could not be fetched, so this should be verified before final seeding.

## International students

No dedicated current international-student graduate admissions page was found in the accessible discovery set. Main navigation includes International Affairs, and Cardiff Met pages/forms accept country data and promote internationally recognized degrees, but no separate graduate international-student rules were confirmed.

## Out-of-scope content excluded

Excluded from program findings:

- Bachelor/undergraduate programs
- Diplomas/certificates
- PACE / professional and continuing education programs
- Training / professional development
- News and events
- Faculty/staff credential biographies
- Third-party domains and non-MUBS sources

## Validation results

- `sources.json` parses: pass
- Duplicate `source_id`: none
- Duplicate `url`: none
- All URLs official MUBS domain/subdomain: pass
- Compile: not required

## Recommendation

**APPROVE WITH NOTES**

The discovery set is sufficient to proceed to a cautious next phase, but current admissions/tuition/graduate-major pages had access issues. Seed generation should use only strongly supported current programs unless the missing current pages are successfully fetched later. Legacy catalogue-only programs should be marked as needs-verification and not seeded as active current programs without additional confirmation.
