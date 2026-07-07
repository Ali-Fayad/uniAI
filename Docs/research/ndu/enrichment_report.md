# NDU Enrichment Report

Task code: NDU_ENRICHMENT_001

## Field Counts

| Field | Before | After |
|---|---:|---:|
| description | 0 | 29 |
| program_description | 0 | 29 |
| delivery_mode | 0 | 29 |
| thesis_or_non_thesis | 2 | 19 |
| concentrations | 8 | 8 |
| concentrations_or_tracks | 0 | 8 |
| admission_requirements | 0 | 28 |
| GRE | 5 | 3 |
| GMAT | 2 | 0 |
| interview | 0 | 7 |
| experience | 2 | 1 |
| accreditation | 4 | 4 |
| language | 0 | 1 |

## Programs Updated

- ndu-faad-master-design
- ndu-fh-master-education
- ndu-fh-master-english-language-literature-applied-linguistics-tefl
- ndu-fh-master-english-language-literature-literature
- ndu-flps-master-international-affairs-diplomacy
- ndu-flps-master-international-affairs-diplomacy-international-law
- ndu-fh-master-media-studies
- ndu-faad-master-music
- ndu-flps-master-political-science
- ndu-flps-master-political-science-human-rights
- ndu-flps-master-political-science-ngos
- ndu-fh-master-psychology-educational
- ndu-flps-master-public-administration
- ndu-fh-master-translation
- ndu-fnas-master-actuarial-sciences
- ndu-fnas-master-biology
- ndu-fbae-master-business-strategy
- ndu-fe-master-civil-engineering
- ndu-fnas-master-computer-science
- ndu-fe-master-electrical-computer-engineering
- ndu-fbae-master-financial-risk-management
- ndu-fnhs-master-food-safety-quality-management
- ndu-fnhs-master-human-nutrition
- ndu-fnas-master-industrial-chemistry
- ndu-fnas-master-mathematics
- ndu-fe-master-mechanical-engineering
- ndu-faad-master-sustainable-architecture
- ndu-fbae-master-mba
- ndu-fbae-master-ms-business-strategy

## Notes

- Delivery mode was normalized to ON_CAMPUS using the official Degree Programs listing, which labels the graduate master’s inventory as Main Campus.
- Program-level descriptions, thesis flags, and admissions details were filled only where the official NDU program pages published them.
- Business Strategy was corrected away from the MBA page mismatch. MBA admissions were also corrected to the wording visible on the official MBA page.
- Human Nutrition and Food Safety and Quality Management were set to thesis/non-thesis where the official pages explicitly said both paths are available.
- Remaining nulls reflect official gaps rather than missing extraction.

## Official Gaps

- Several science and engineering pages do not publish a thesis/non-thesis choice in the visible public content.
- Program pages often omit language-of-instruction labels even when English proficiency is required.
- A few pages publish interview or experience expectations as preference rather than hard requirements; those were left null unless the page explicitly required them.

## Recommendation

APPROVE WITH NOTES
