# MEU Source Map

Task code: `MEU_DISCOVERY_001_BROWSER_INV`  
Date accessed: 2026-07-05  
Official domains used: `meu.edu.lb`, `catalog.meu.edu.lb`, `records.meu.edu.lb` links discovered from MEU pages where relevant. No third-party sources used for evidence.

## Primary discovery path

1. `MEU-S001` - Homepage (`https://meu.edu.lb/`)
   - Confirmed official institution site and navigation to Programs, Apply, Financial Aid, Catalog, Calendar, Tuition/Fees, and contact details.

2. `MEU-S002` - Programs page (`https://meu.edu.lb/programs/`)
   - Primary source for in-scope graduate program inventory.
   - Confirmed graduate programs visible on the official program list:
     - Master of Business Administration - Business Administration
     - Master of Arts - Education
     - Master of Arts - Islamic Studies
     - Master of Arts - Teaching
   - No PhD/Doctorate program appeared on the official program list during this pass.

3. Program detail pages
   - `MEU-S003` MBA: overview and concentrations: Finance, General Business, Management, Marketing.
   - `MEU-S004` MA Education: overview and concentrations: Curriculum and Instruction, Educational Leadership.
   - `MEU-S005` MA Islamic Studies: overview and catalog link.
   - `MEU-S006` MA Teaching: overview.

4. Catalog and faculty/department structure
   - `MEU-S009` Catalog index maps the academic catalog and faculties/departments.
   - Faculties listed in the catalog navigation:
     - Faculty of Arts and Sciences
     - Faculty of Education
     - Faculty of Business Administration
     - Faculty of Philosophy and Theology
   - `MEU-S012` Department of Mediterranean and Near Eastern Studies confirms MA Islamic Studies details, Arabic prerequisite note, course structure, and total credits.
   - Catalog pages for Department of Business Administration and Department of Teacher Education were discovered via program links but could not be fully fetched by the browser tool due to a decoding error. They remain official catalog targets for a later import pass if needed.

5. Admissions and graduate regulations
   - `MEU-S007` Application Procedure page confirms online application route and application fee context.
   - `MEU-S008` Catalog Admissions page is the primary source for graduate admissions, required documents, language requirements, graduate status categories, transfer, readmission, and graduate regulations.

6. Tuition / fees / financial aid
   - `MEU-S010` Catalog Financial Information page confirms payment expectations, payment arrangements through the Business Office, fee categories, international deposit policy, financial hold, financial aid, and refund policy.
   - `MEU-S011` Financial Aid & Scholarships page confirms financial aid categories and graduate-relevant scholarship/aid rules.
   - `MEU-S013` and `MEU-S014` are official graduate/master tuition PDF sources for 2025-2026.

7. Academic calendar
   - `MEU-S015` Academic Calendar 2025-2026 PDF.
   - `MEU-S016` Academic Calendar 2026-2027 PDF.

8. Student rules / handbook
   - `MEU-S017` Student Handbook 2024 PDF found as official context, but it is secondary for this discovery scope.

## Out-of-scope pages intentionally not expanded

- Undergraduate program pages.
- Diploma/certificate/language institute pages.
- News/events.
- Third-party social, WhatsApp, Google Maps, YouTube, and donation links.

## Discovery gaps / notes for later import

- No official PhD/Doctorate offering was found on the MEU Programs page or catalog navigation during this pass.
- MBA and Teacher Education catalog department pages were linked from official program pages, but the browser fetch failed with a Unicode decoding error. Keep the official URLs for later manual/browser verification:
  - `https://catalog.meu.edu.lb/catalog/departmentOfBusiness.jsp`
  - `https://catalog.meu.edu.lb/catalog/departmentOfTeacher.jsp`
- Do not create seed/import artifacts from this pass; this is discovery only.
