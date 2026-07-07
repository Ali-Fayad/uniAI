# USEK Final Quality Report

## Verdict
APPROVE WITH NOTES

## Validation Summary
- Programs: 80
- MASTER: 59
- PHD: 21
- Out-of-scope records: 4
- Sources: 45
- Tuition objects: 59
- Null tuition values: 21

## Duplicate Checks
- Duplicate program IDs: 0
- Duplicate source IDs: 0
- Duplicate source URLs: 0
- Duplicate official program URLs: 5 intentional reuse groups

## Source Reference Checks
- Programs without sources: 0
- Programs with unresolved source IDs: 0
- Programs with non-official URLs: 0
- Broken source references: NO

## Normalization Checks
- degree_type values: MASTER, PHD
- delivery_mode values: none
- language values: ENGLISH, MULTILINGUAL
- thesis_or_non_thesis values: THESIS, THESIS_OR_PROJECT, PROJECT
- tuition.billing_basis values: PER_CREDIT
- tuition.currency values: USD
- requirement enum values: no violations found

## Tuition Validation
- Tuition rows populated: 59
- Null tuition values: 21
- Master’s with null tuition: 0
- PhD with null tuition: 21
- Tuition internally consistent: YES

## Official Program URL Reuse
The following reuse groups are intentional and documented, not errors:
- https://www.usek.edu.lb/law/academic-programs/master-in-business-law?t=2 (9 programs)
- https://www.usek.edu.lb/en/fi-academic-programs/department-of-biomedical-engineering-2/master-of-science-in-biomedical-engineering?t=2 (10 programs)
- https://www.usek.edu.lb/fmus/academic-program/phd?t=5 (2 programs)
- https://www.usek.edu.lb/academics/faculty-of-arts-and-sciences/department-of-languages-and-literatures/diploma-in-interpretation?t=2 (28 programs)
- https://www.usek.edu.lb/faculty-of-arts-and-sciences/academic-programs/department-of-nutrition-and-food-sciences/phd-in-agricultural-and-food-sciences?t=5 (14 programs)

## Completeness Metrics
- Program-field completeness across reviewed fields: 7.62% populated (128/1680 cells)

| Field | Populated | Null | Null % |
|---|---:|---:|---:|
| credits | 22 | 58 | 72.5% |
| duration | 0 | 80 | 100% |
| language | 21 | 59 | 73.8% |
| delivery_mode | 0 | 80 | 100% |
| program_description | 19 | 61 | 76.3% |
| admission_requirements | 0 | 80 | 100% |
| required_documents | 0 | 80 | 100% |
| gre_requirement | 0 | 80 | 100% |
| gmat_requirement | 0 | 80 | 100% |
| english_requirement | 0 | 80 | 100% |
| portfolio_requirement | 0 | 80 | 100% |
| interview_requirement | 0 | 80 | 100% |
| experience_requirement | 0 | 80 | 100% |
| tuition | 59 | 21 | 26.3% |
| additional_fees | 0 | 80 | 100% |
| deadlines | 0 | 80 | 100% |
| scholarships | 0 | 80 | 100% |
| financial_aid | 0 | 80 | 100% |
| payment_plans | 0 | 80 | 100% |
| accreditation | 0 | 80 | 100% |
| concentrations_or_tracks | 7 | 73 | 91.3% |

## Shared University Data Status
- admissions: present
- required_documents: present
- doctoral_required_documents: present
- language_requirements: present
- deadlines: present
- scholarships: present
- financial_aid: present
- payment_plans: present
- academic_calendar: present
- academic_regulations: present
- accreditation: present
- tuition_model: present

## Remaining Missing Information
- Doctoral tuition: officially unavailable in the graduate fee table used for this pass.
- Many program descriptions, durations, delivery modes, and tracks remain null because the official pages do not consistently publish them or they were not surfaced on the hub pages used for inventory.
- These gaps are a mix of officially unavailable data and information intentionally centralized in `university.json`.
- Nursing remains the main program-surface caveat from earlier discovery; however, no duplicate row or schema inconsistency was introduced.

## Orphan Sources
- [reference] usek_architecture_combined_page :: Holy Spirit University of Kaslik | Bachelor and Master in Architecture (Combined Program)
- [discovery] usek_multiple_sclerosis_diploma :: Holy Spirit University of Kaslik | University Diploma in Multiple Sclerosis

## PhD Verification Table
| Program ID | Official Degree Name | Tuition Null |
|---|---|---:|
| usek-business-phd-business-administration | Ph.D. in Business | yes |
| usek-business-doctorate-business-administration | Doctorate in Business Administration | yes |
| usek-law-phd-law | Ph.D. in Law | yes |
| usek-theology-phd-theology | Ph.D. in Theology | yes |
| usek-medicine-doctorate-medicine | Doctorate of Medicine | yes |
| usek-music-phd-music | Ph.D. in Music | yes |
| usek-music-phd-higher-specialized-music-education | Ph.D. in Higher and Specialized Music Education | yes |
| usek-fas-phd-agricultural-food-sciences | Ph.D. in Agricultural and Food Sciences | yes |
| usek-fas-phd-arabic-language-literature | Ph.D. in Arabic Language and Literature | yes |
| usek-fas-phd-archeology-art-history | Ph.D. in Archeology and Art History | yes |
| usek-fas-phd-chemistry-life-earth-sciences | Ph.D. in Chemistry and Life and Earth Sciences | yes |
| usek-fas-phd-conservation-restoration-cultural-property-sacred-art | Ph.D. in Conservation, Restoration of Cultural Property & Sacred Art | yes |
| usek-fas-phd-education-sciences | Ph.D. in Education Sciences | yes |
| usek-fas-phd-english-language-literature | Ph.D. in English Language and Literature | yes |
| usek-fas-phd-french-language-literature | Ph.D. in French Language and Literature | yes |
| usek-fas-phd-history | Ph.D. in History | yes |
| usek-fas-phd-language-sciences-traductology | Ph.D. in Language Sciences and Traductology | yes |
| usek-fas-phd-philosophy | Ph.D. in Philosophy | yes |
| usek-fas-phd-psychology | Ph.D. in Psychology | yes |
| usek-fas-phd-social-sciences | Ph.D. in Social Sciences | yes |
| usek-fas-phd-visual-arts | Ph.D. in Visual Arts | yes |

## Recommendation
APPROVE WITH NOTES
