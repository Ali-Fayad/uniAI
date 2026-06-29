# UOB Program-Level Enrichment Report

## Summary

This pass enriched program-specific fields in `research/uob/programs.json` using only official University of Balamand pages and PDFs.

No changes were made to `research/uob/university.json`.
No new source URLs were required; the existing source registry already covered the official pages and PDFs used in this pass.

## Coverage Improvements

Field coverage before -> after:

- `program_description`: 0 -> 47
- `credits`: 2 -> 26
- `duration`: 2 -> 24
- `thesis_or_non_thesis`: 1 -> 26
- `concentrations_or_tracks`: 2 -> 16
- `admission_requirements`: 0 -> 3
- `interview_requirement`: 0 -> 1
- `experience_requirement`: 0 -> 1
- `accreditation`: 0 -> 1

Fields not intentionally changed in this pass:

- `delivery_mode`
- `gre_requirement`
- `gmat_requirement`
- `portfolio_requirement`
- `required_documents`
- `english_requirement`
- `tuition`
- `additional_fees`
- `deadlines`
- `scholarships`
- `financial_aid`
- `payment_plans`

## Programs Significantly Enriched

### Faculty of Business and Management

- `uob-fobm-mba`
- `uob-fobm-emba`

Updates:
- Added program descriptions.
- Populated credits and duration.
- Added MBA track list.
- Added EMBA admission requirements, interview requirement, and experience requirement.

### Faculty of Health Sciences

- `uob-fhs-master-public-health`
- `uob-fhs-master-clinical-laboratory-sciences-lab-management`
- `uob-fhs-master-medical-laboratory-sciences`
- `uob-fhs-master-nursing`

Updates:
- Added program descriptions.
- Populated credits, duration, and thesis/capstone structure.
- Added MPH track list.
- Added MS-CLS thesis-track vs professional-track split.
- Added Nursing accreditation note from the official page.

### Faculty of Medicine and Medical Sciences

- `uob-fom-master-biomedical-sciences`

Updates:
- Added program description.
- Populated duration and thesis-track structure.
- Added the five research-track areas listed on the official page.

### Faculty of Arts and Sciences

Updated program-specific fields for:

- `uob-fas-master-arabic-language-literature`
- `uob-fas-master-biology`
- `uob-fas-master-chemistry`
- `uob-fas-master-christian-muslim-studies`
- `uob-fas-master-computer-science`
- `uob-fas-master-education-aley`
- `uob-fas-master-education-kurah`
- `uob-fas-master-english-language-literature`
- `uob-fas-master-english-language-teaching`
- `uob-fas-master-environmental-sciences`
- `uob-fas-master-food-science-technology`
- `uob-fas-master-french-language-literature`
- `uob-fas-master-history`
- `uob-fas-master-languages-translation`
- `uob-fas-master-mass-media-communication`
- `uob-fas-master-mathematics`
- `uob-fas-master-philosophy`
- `uob-fas-master-physical-education`
- `uob-fas-master-political-science-international-affairs`
- `uob-fas-master-psychology`
- `uob-fas-master-sports-management`

Updates:
- Added program descriptions for all of the above.
- Populated explicit credits / duration / thesis-or-project structure where the handbook or page stated them.
- Added track lists where the official text named them.
- Added program-specific admission requirements for Political Science and International Affairs and Sports Management.
- Added the English/French/Arabic language normalization where the official text was explicit.

### ALBA

All 19 ALBA graduate records received catalog-derived program descriptions.

The following records were updated in that group:

- `uob-alba-master-architecture-march`
- `uob-alba-master-computer-graphics-interactive-media-mfa`
- `uob-alba-master-graphic-design-mfa`
- `uob-alba-master-interior-architecture-design-mfa`
- `uob-alba-master-animation-2d-3d`
- `uob-alba-master-architecture`
- `uob-alba-master-audiovisual-directing`
- `uob-alba-master-audiovisual-production`
- `uob-alba-master-cinema-directing`
- `uob-alba-master-global-design`
- `uob-alba-master-graphic-design-publicity`
- `uob-alba-master-illustration-comic`
- `uob-alba-master-interior-architecture`
- `uob-alba-master-landscape-management`
- `uob-alba-master-multimedia-creation`
- `uob-alba-master-photography`
- `uob-alba-master-television-digital-media`
- `uob-alba-master-urban-design`
- `uob-alba-master-visual-arts`

## Canonical URL Improvements

Two records now point to the dedicated official program pages instead of hub/catalog URLs:

- `uob-fas-master-food-science-technology` -> `https://www.balamand.edu.lb/faculties/FAS/Departments/Pages/FoodScience.aspx`
- `uob-fhs-master-medical-laboratory-sciences` -> `https://www.balamand.edu.lb/faculties/FHS/AcademicPrograms/Pages/Programs/MSClinicalLabSciences.aspx`

The program-source links were updated accordingly, and the official source IDs already existed in `research/uob/sources.json`.

## Remaining Official Gaps

- Engineering graduate records remain catalogue/hub based and still lack rich program descriptions.
- Theology remains hub/catalog based; the official theology subdomain was surfaced, but it was not fully fetched in discovery.
- Some ALBA records remain catalog-only and do not yet have directly verified credits or duration values.
- `uob-fom-master-cognitive-behavior-therapy` remains catalog-based with limited explicit program detail.
- `uob-fas-master-philosophy` has a description update, but its credit total and duration were not explicitly confirmed in the extracted section used for this pass.

## Validation

- `research/uob/programs.json` parses successfully.
- `research/uob/sources.json` parses successfully.
- No duplicate program IDs were introduced.
- No source URLs were added or duplicated.
- Every source ID referenced by a program exists in `research/uob/sources.json`.
- Official program URLs remain restricted to `balamand.edu.lb` or `theology.balamand.edu.lb`.
- Duplicate official program URLs remain intentional where the university uses a shared catalog or hub page for multiple records.
- `./mvnw -q -DskipTests compile` passed in `Server/`.

## Recommendation

- APPROVE WITH NOTES
