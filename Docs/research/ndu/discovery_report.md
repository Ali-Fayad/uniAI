# NDU Discovery Report

Task code: `NDU_DISCOVERY_001_BROWSER_INV`  
University: Notre Dame University-Louaize (NDU)  
Official website: https://www.ndu.edu.lb  
Date accessed: 2026-06-29  
Mode: browser-based discovery pass

## Files changed

- `research/ndu/source_map.md`
- `research/ndu/sources.json`
- `research/ndu/discovery_report.md`

## Executive summary

NDU has official graduate/master's program sources on its website. The central `Degree Programs` page lists graduate programs, and the `Graduate` page confirms graduate admissions eligibility, English proficiency, required documents, and graduate application flow.

During this pass, 28 graduate program entry pages were discovered directly from the official Degree Programs listing. One additional official master candidate, `Master of Science in Astrophysics`, was found through official NDU search results but was not observed in the Degree Programs list during this pass, so it is marked for follow-up rather than final inclusion.

No official PhD/doctorate program entry page was found in the Degree Programs graduate listing. Official searches for `PhD`, `Ph.D.`, and `Doctorate` mainly returned faculty credential pages, not NDU doctoral program pages. The 2025-2026 Catalog PDF must still be parsed before concluding definitively that NDU has no in-scope PhD programs.

Recommendation: **APPROVE WITH NOTES**.

## Graduate scope discovered

### Master's programs

Status: **verified present**.

Evidence:

- NDU has a dedicated Graduate page.
- NDU has a Degree Programs page with a Graduate filter/listing.
- 28 official graduate program entry pages were discovered from the Degree Programs page.
- One additional official master page was found and marked for follow-up.

### PhD / doctorate programs

Status: **not found in browser listing**.

Notes:

- No PhD/doctorate program appeared in the observed Graduate program listing.
- Search results for PhD/Doctorate were dominated by faculty profile credentials.
- Catalog PDF parsing is required before finalizing this as `not offered` or `not found`.

## Graduate faculties/schools discovered

Graduate program pages were discovered under these official NDU faculties:

- Ramez G. Chagoury Faculty of Architecture, Arts, and Design (FAAD)
- Faculty of Business Administration and Economics (FBAE)
- Faculty of Engineering (FE)
- Faculty of Humanities (FH)
- Faculty of Law and Political Science (FLPS)
- Faculty of Natural and Applied Sciences (FNAS)
- Faculty of Nursing and Health Sciences (FNHS)

## Official sources found

- Total sources in `sources.json`: **46**
- Official non-program source pages/PDFs: **17**
- Program entry pages/candidates: **29**
- Program entry pages found directly from Degree Programs listing: **28**
- Program follow-up candidates: **1**
- Official PDFs found: **3**

Official PDFs:

1. `NDU Catalog 2025-2026`
2. `Admission Guide 2025-2026`
3. `NDU Catalog 2024-2025` fallback/reference only

## Tuition/fees source status

Status: **verified**.

The official Tuition Fees page includes Graduate Tuition Fees for Academic Year 2025-2026. It lists tuition per credit by category:

- Architecture: USD 670/credit
- Business: USD 635/credit
- Engineering: USD 765/credit
- Computer Sciences: USD 670/credit
- Remedial: USD 520/credit
- All Others: USD 590/credit

It also lists the first payment, technology/student services fees, and other fees including the admission application fee. The Yearly Schedule page provides payment schedule dates.

## Admissions source status

Status: **verified**.

Sources found:

- Graduate page
- Graduate Admission Requirements page
- Application Process page
- Important Dates page

Discovered admissions facts for follow-up extraction:

- Recognized bachelor's degree or equivalent is required for graduate studies.
- Minimum cumulative GPA is 2.7/4.0 or equivalent.
- Lebanese Baccalaureate Part II or equivalent is required.
- English proficiency is required for non-NDU graduates via TOEFL, SAT, IELTS, or NDU EET.
- Required documents include bachelor's degree copy, undergraduate transcript, Lebanese Baccalaureate copy/equivalent, ID/passport, photo, and recommendation letters.
- MBA and MS Engineering have additional GRE/GMAT/CV/employment certificate requirements.
- Application fee is USD 30.

## Financial aid/scholarship source status

Status: **partially verified**.

Graduate-specific source found:

- Graduate Tuition Reduction: 25% tuition reduction for eligible recent NDU graduates entering graduate studies within two consecutive regular semesters.

General financial aid source found:

- Scholarship and Financial Aid - New Students.

Assistantships:

- Some program pages include Graduate Teaching Assistantship / Graduate Research Assistantship text and eligibility. This should be extracted program-by-program and cross-checked with the Catalog PDF.

## Missing source categories / follow-up needed

- Final confirmation of whether NDU offers any PhD/doctorate programs.
- Full parsing of `NDU Catalog 2025-2026.pdf` for graduate regulations, assistantship policy, course load, registration, refund policy, and degree requirements.
- Full parsing of `Admission Guide 2025-2026.pdf` for graduate admission overview and program list cross-check.
- International students page/source was not clearly isolated during this pass.
- Payment methods/details page should be opened from the Tuition Fees page in the next phase.
- Office of Grants and Research Policies may be relevant for assistantships/research support but were not deeply crawled in this discovery pass.
- The official `Master of Science in Astrophysics` page requires follow-up because it was found outside the observed Degree Programs listing.
- `Master of Science in Human Nutrition` URL includes a duplicated path segment; verify canonical URL during extraction.
- `Masters of Arts in Sustainable Architecture` URL contains `sustainable-arachitecture`; preserve official spelling but verify canonical URL/title.

## Possible crawl/browser limitations

- The Degree Programs page appears dynamic/filterable; browser text extraction captured the full listing visible to the model, but dynamic filtering may hide or lazy-load entries.
- PDF content was discovered but not deeply parsed in this discovery-only pass.
- Search results may expose older or duplicate NDU URLs; older `Sub.aspx` pages should be treated as fallback only unless canonical pages are unavailable.
- Some application/registration links point to `sis.ndu.edu.lb`; these are official but may require credentials and were not entered.

## Validation results

Performed locally on the generated files:

- `sources.json` parses successfully: **PASS**
- No duplicate `source_id`: **PASS**
- No duplicate `url`: **PASS**
- Every URL is official NDU domain/subdomain: **PASS**

Not performed:

- `./mvnw -q -DskipTests compile`

Reason: the active execution environment does not contain the uniAI repository root or Maven wrapper. Only the generated discovery files were created in `/mnt/data/research/ndu`.

## Recommendation

**APPROVE WITH NOTES**

Proceed to the next task only after:

1. Parsing the 2025-2026 Catalog PDF.
2. Verifying the `Master of Science in Astrophysics` page against the official program list/catalog.
3. Confirming whether PhD/doctorate offerings are truly absent or only missing from the web listing.