# NDU PhD / Doctorate Verification Report

Task code: `NDU_DISCOVERY_002_PHD_VERIFY`  
University: Notre Dame University-Louaize (NDU)  
Date accessed: 2026-06-29  
Scope: official NDU sources already collected, official NDU catalog/catalog index pages, official NDU graduate/admissions/registrar/program pages, and official NDU search results for doctorate indicators.

## Result

**PhD confirmed: NO**

No official NDU PhD, Doctorate, Doctor of Philosophy, Doctoral, DPhil, DBA, EdD, or Professional Doctorate program was confirmed from the official NDU program index, graduate admissions pages, registrar catalog index, or official NDU search results available during this verification pass.

## Search terms checked

The verification specifically checked for:

- `PhD`
- `Ph.D.`
- `Doctorate`
- `Doctor of Philosophy`
- `Doctoral`
- `DPhil`
- `DBA`
- `EdD`
- `Professional Doctorate`

## Evidence reviewed

### 1. Degree Programs page

The official NDU Degree Programs page was inspected because it is the central program index discovered in `sources.json`.

Findings:

- The page lists graduate programs as Master's-level entries only.
- The visible graduate list includes Master of Arts, Master of Science, MBA, and related master's entries.
- No matching `Doctor`, `PhD`, or equivalent doctorate term appeared in the Degree Programs page text during this pass.

Relevant source:

- `NDU-SRC-003` — Degree Programs — `https://www.ndu.edu.lb/academics/degree-programs`

### 2. Graduate overview / graduate admissions pages

The official Graduate page and Graduate Admission Requirements page were inspected because they define NDU graduate admission scope.

Findings:

- The pages describe graduate studies and graduate admissions eligibility.
- Requirements are framed around applicants holding a recognized bachelor's degree for graduate studies.
- Program-specific requirements mentioned are for MBA and MS Engineering applicants.
- The only doctorate-related wording found is that MBA applicants may be exempt from GRE/GMAT if they already hold a doctoral degree; this describes an applicant credential, not an NDU doctoral program.
- No NDU PhD/Doctorate program title or doctoral admission pathway was found.

Relevant sources:

- `NDU-SRC-002` — Graduate — `https://www.ndu.edu.lb/academics/graduate`
- `NDU-SRC-006` — Graduate Admission Requirements — `https://www.ndu.edu.lb/admissions/admission-requirements/graduate`

### 3. Registrar catalog index and official catalogs

The official Registrar Catalog page was inspected because it links the current and prior academic catalogs.

Findings:

- The official catalog index links Academic Year 2025-2026 and Academic Year 2024-2025 catalogs.
- The 2025-2026 catalog PDF was found as an official NDU source.
- Direct PDF fetching/parsing from the execution environment was not available, but official web search over the catalog URL did not surface any NDU doctoral program result for the searched doctorate terms.
- The prior discovery had already marked catalog parsing as the remaining blocker; after this verification pass, no affirmative PhD evidence was found from the catalog source path.

Relevant sources:

- `NDU-SRC-015` — NDU Catalog 2025-2026 — `https://www.ndu.edu.lb/Assets/Library/Gallery/PDFs/NDU%20Catalog%202025-2026.pdf`
- `NDU-SRC-017` — NDU Catalog 2024-2025 — `https://www.ndu.edu.lb/Library/Assets/Files/Documents/OfficeoftheRegistrar/Catalog%202024-2025.pdf`
- Registrar catalog page — `https://www.ndu.edu.lb/offices/office-of-the-registrar/catalog`

### 4. Official search results for doctorate indicators

Official NDU-only searches were performed for doctorate indicators.

Findings:

- Matches for `Ph.D.`, `Doctorate`, and `DBA` primarily appeared in faculty biography/credential pages, not program pages.
- No official NDU page was found that presents an NDU PhD/Doctorate/DBA/EdD/DPhil/Professional Doctorate degree program.
- The Office of Research and Graduate Studies source describes oversight of graduate programs, including master's degrees, but no doctoral program was identified.

Relevant source:

- `NDU-SRC-013` — Office of Research and Graduate Studies — `https://www.ndu.edu.lb/offices/office-of-research-and-graduate-studies/message`

## Confirmed PhD programs

None.

## Conclusion

Based on the official NDU sources inspected in this verification pass, NDU should be treated as having **no confirmed PhD/Doctorate programs** for the current graduate database crawl.

Recommended database handling:

- Do not create any PhD/Doctorate program rows for NDU.
- Keep master's programs in scope.
- Preserve evidence notes that doctorate-related terms found on official NDU pages refer to faculty credentials or applicant credentials, not NDU degree offerings.

## Limitations

- The recovered `sources.json` currently contains 17 explicit URLs because the prior recovery task could not reconstruct the 29 missing program URLs from the pasted source map.
- Direct local PDF download/parsing was unavailable in the execution environment. The verification therefore used the official catalog source path, official catalog index page, official NDU pages, and official NDU-only search results.
- If a future task obtains the catalog PDF as a local file, it can be text-parsed as a final mechanical cross-check, but no affirmative PhD evidence was found in the current official-source verification.

## Validation

- `sources.json` parses successfully: PASS
- No duplicate `source_id`: PASS
- No duplicate `url`: PASS
