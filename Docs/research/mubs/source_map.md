# MUBS Source Map

Task code: `MUBS_DISCOVERY_001_BROWSER_INV`  
Date accessed: 2026-07-04  
Official domain scope: `mubs.edu.lb` and official MUBS subdomains (`www`, `forms`, `ums`, `balums`, etc.)

## Primary navigation / site structure

| Source ID | URL | Purpose | Status |
|---|---|---|---|
| MUBS-SRC-001 | https://www.mubs.edu.lb/ | Main site navigation; links to admission, academics, faculties, financial aid, students, quick links. | Accessible |
| MUBS-SRC-002 | https://www.mubs.edu.lb/en/admission.aspx | Admission landing page; lists graduate majors, requirements, tuition, how to apply. | Search-accessible |
| MUBS-SRC-024 | https://www.mubs.edu.lb/en/academics_mubs/academic-calendar.aspx | Academic calendar. | Discovered; browser fetch returned verification/timeout |
| MUBS-SRC-025 | https://www.mubs.edu.lb/en/academics_mubs/rules.aspx | Rules and regulations. | Discovered; browser fetch failed |

## Admissions / tuition / financial aid

| Source ID | URL | Purpose | Status |
|---|---|---|---|
| MUBS-SRC-003 | https://mubs.edu.lb/en/admission/graduate-majors.aspx | Graduate majors target page. | Verification/loader during fetch |
| MUBS-SRC-004 | https://www.mubs.edu.lb/en/admission/admission-requirements.aspx | Admission requirements. | Verification/loader during fetch |
| MUBS-SRC-005 | https://mubs.edu.lb/en/admission/tuition.aspx | MUBS tuition and fees. | Verification/loader during fetch |
| MUBS-SRC-006 | https://mubs.edu.lb/en/financial-aid.aspx | Financial aid overview; aid types and links. | Accessible |
| MUBS-SRC-014 | https://mubs.edu.lb/en/faculties/cmu/tuitionfees.aspx | Cardiff Met tuition fees. | Accessible; fees are upon request |
| MUBS-SRC-015 | https://mubs.edu.lb/en/faculties/cmu/financialaidform.aspx | Cardiff Met financial aid. | Accessible, sparse content |
| MUBS-SRC-016 | https://www.mubs.edu.lb/cmu/info.aspx | Cardiff Met info request page. | Search-accessible |
| MUBS-SRC-017 | https://forms.mubs.edu.lb/index.php/944744?lang=en&newtest=Y | Cardiff Met dual degree info request. | Search-accessible |
| MUBS-SRC-018 | https://ums.mubs.edu.lb/application/applicant/newApplicant.php?major=Master+of+Science+in+Computer+Science+%28MCS%29 | MCS online application URL. | Search-accessible |

## Faculties / schools / institutes

| Source ID | URL | Purpose | Status |
|---|---|---|---|
| MUBS-SRC-007 | https://mubs.edu.lb/en/faculties/fba.aspx | Faculty of Business Administration. | Accessible |
| MUBS-SRC-008 | https://mubs.edu.lb/en/faculties/fhs.aspx | Faculty of Health Sciences. | Accessible |
| MUBS-SRC-009 | https://mubs.edu.lb/en/faculties/artsandsciences/csd.aspx | Computer Science Department; confirms MCS. | Accessible |
| MUBS-SRC-010 | https://mubs.edu.lb/en/faculties/artsandsciences.aspx | Faculty of Arts and Sciences landing page. | Discovered; inconsistent fetch |
| MUBS-SRC-011 | https://mubs.edu.lb/en/faculties/finearts.aspx | Faculty of Fine Arts and Design. | Discovered/search-accessible |
| MUBS-SRC-012 | https://www.mubs.edu.lb/en/faculties/cmu.aspx | Cardiff Metropolitan University at MUBS; MBA programme list. | Search-accessible |
| MUBS-SRC-013 | https://mubs.edu.lb/en/faculties/cmu/programs/postgraduate-level/7741.aspx | MBA General program page. | Accessible |

## Official PDFs / catalogues

| Source ID | URL | Purpose | Status |
|---|---|---|---|
| MUBS-SRC-019 | https://www.mubs.edu.lb/assets/templates/mubs/files/catalogue2012.pdf | Official catalogue; graduate admission requirements and legacy postgraduate program references. | Search-accessible PDF text |
| MUBS-SRC-020 | https://www.mubs.edu.lb/assets/templates/mubs/files/academiccatalogue.pdf | Official 2010-2011 academic catalogue; historical admissions/fees/regulations/MBA. | Search-accessible PDF text |
| MUBS-SRC-021 | https://www.mubs.edu.lb/assets/templates/mubs/files/catalogue2014.pdf | Official 2014 catalogue. | Search-accessible PDF text |
| MUBS-SRC-022 | https://mubs.edu.lb/assets/templates/mubs/files/catalogue2011.pdf | Official 2011 catalogue. | Search-accessible PDF text |
| MUBS-SRC-023 | https://www.mubs.edu.lb/assets/templates/bsml/files/MBA-Cardiff.pdf | Cardiff MBA PDF. | Search-accessible PDF text |

## Discovery notes

- The `www.mubs.edu.lb` host sometimes returned a verification/loader page; retrying equivalent `mubs.edu.lb` URLs worked for several faculty/program pages.
- The current Graduate Majors and Tuition pages could not be fully fetched in this browser pass, so current program confirmation was limited to pages/forms/PDFs that loaded or appeared in official search snippets.
- Out-of-scope content encountered and intentionally excluded: undergraduate programs, PACE/continuing education certificates, news, events, staff bios, and non-MUBS third-party pages.
