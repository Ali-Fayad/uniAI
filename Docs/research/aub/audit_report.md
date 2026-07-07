# AUB Audit Report

Date accessed: 2026-06-26

## Validation

- JSON files checked: 9
- Invalid JSON files: 0
- Unique program records across the faculty files: 63
- Total program rows across files: 68

### File Status
- programs.json: valid (array, 25 items)
- fas_programs.json: valid (array, 26 items)
- fhs_programs.json: valid (array, 3 items)
- interfaculty_programs.json: valid (array, 5 items)
- medicine_programs.json: valid (array, 7 items)
- nursing_programs.json: valid (array, 2 items)
- fafs_programs.json: valid (array, 0 items)
- sources.json: valid (array, 32 items)
- university.json: valid (object)

## Schema Consistency

- programs.json: schema issues found
  - Records missing fields: 20
  - Records with field-order mismatch: 20
- fas_programs.json: matches canonical field order and source schema
- fhs_programs.json: matches canonical field order and source schema
- interfaculty_programs.json: matches canonical field order and source schema
- medicine_programs.json: matches canonical field order and source schema
- nursing_programs.json: matches canonical field order and source schema
- fafs_programs.json: matches canonical field order and source schema (empty array)
- Canonical schema reference records in `programs.json` are not fully uniform: 20 records omit the trailing `notes` key entirely.
- No source object format mismatches were found in any `sources` array.

## Inventory Summary

- Target faculties/schools checked: 7
- Faculties/school buckets with records: 6
- FAFS records found: 0
- Master’s programs: 52
- PhD programs: 11

### Maroun Semaan Faculty of Engineering and Architecture
- MASTER: 14
- PHD: 5
- Total: 19
- Degree IDs: bmen-ms, bmen-phd, cee-me-civil, cee-me-ewre, cee-ms-et, cee-phd-civil, cee-phd-ewre, chen-me, ece-me, ece-phd, iem-mem, me-mechanical, me-applied-energy, me-energy-studies, me-dual-degree-em-es, me-phd, soad-mupp, soad-mud, soad-ms-environmental-sciences
- Official degree names: Master of Science in Biomedical Engineering, Doctor of Philosophy in Biomedical Engineering, Master of Engineering in Civil Engineering, Master of Engineering in Environmental & Water Resources Engineering, Master of Science in Environmental Technology, Ph.D. in Civil Engineering, PhD in Environmental & Water Resources Engineering, Master of Engineering in Chemical Engineering, Master of Engineering in Electrical and Computer Engineering, Doctor of Philosophy in Electrical and Computer Engineering, Master degree in Engineering Management (MEM), Master of Engineering major Mechanical Engineering, Master of Mechanical Engineering major in Applied Energy, Master of Science in Energy Studies, Master of Engineering dual degree in Engineering Management & Energy Studies, Doctor of Philosophy (Ph.D.) in Mechanical Engineering, Urban Planning and Policy, Urban Design, Interfaculty Master of Science in Environmental Sciences, with a major in Ecosystem Management

### Faculty of Health Sciences
- MASTER: 4
- PHD: 0
- Total: 4
- Degree IDs: fhs-gphp, fhs-mph, fhs-ms-environmental-sciences, fhs-emhcl
- Official degree names: null, Master of Public Health, Master of Science in Environmental Sciences, Executive Master in Health Care Leadership (EMHCL)

### Suliman S. Olayan School of Business
- MASTER: 4
- PHD: 0
- Total: 4
- Degree IDs: osb-mhrm, osb-msba, osb-mfin, osb-mba-online
- Official degree names: Master's in Human Resource Management, Master's of Science in Business Analytics, Master's in Finance, MBA Online

### Faculty of Arts and Sciences
- MASTER: 23
- PHD: 4
- Total: 27
- Degree IDs: fas-gpcs, fas-gpcs-ms, fas-soam-anth, fas-soam-soc, fas-soam-media, fas-pspa-pols, fas-pspa-puba, fas-pspa-ppia, fas-arabic-ma, fas-arabic-phd, fas-english-ma-lang, fas-english-ma-lit, fas-histarc-ma-history, fas-histarc-phd-history, fas-histarc-ma-archaeology, fas-philosophy-ma, fas-psychology-ma, fas-econ-ma, fas-econ-mfe, fas-biology-ms, fas-biology-phd, fas-chemistry-ms, fas-earthsciences-ms, fas-cs-ms, fas-math-ms, fas-math-phd, fas-faah-ma-art-curating
- Official degree names: Master’s Degree in Computational Science, Master's Degree in Computational Science, MA - Anthropology, MA - Sociology, MA - Media Studies, Master of Arts in Political Studies, Master of Arts in Public Administration, Master of Arts in Public Policy and International Affairs, MA in Arabic Language and Literature, PhD in Arabic Language and Literature, MA in English Language, MA in English Literature, Master of Arts in Arab and Middle Eastern History, PhD in Arab & Middle Eastern History, Master of Arts in Archaeology, M.A. in Philosophy, Master of Arts in Psychology, Master of Arts in Economics, Master of Arts in Financial Economics, Master of Science in Biology, Doctor of Philosophy in Cell and Molecular Biology, Master of Science in Chemistry, Master of Science in Geology, Master of Science in Computer Science, Master of Science in Mathematics, PhD in Mathematics, Master of Arts in Art History and Curating

### Faculty of Medicine
- MASTER: 6
- PHD: 1
- Total: 7
- Degree IDs: dbmg-ms, dacp-ms-hmcb, dacp-ms-neuroscience, dacp-ms-physiology, epim-ms, pharm-ms, fm-dbms-phd
- Official degree names: Master of Science in Biochemistry, Master of Science in Human Morphology and Cell Biology, Master of Science in Neuroscience, Master of Science in Physiology, Master of Science in Microbiology and Immunology, Master of Science in Pharmacology and Therapeutics, Doctor of Philosophy in Biomedical Sciences

### Rafic Hariri School of Nursing
- MASTER: 1
- PHD: 1
- Total: 2
- Degree IDs: hson-msn, hson-phd
- Official degree names: Master of Science in Nursing, Doctor of Philosophy in Nursing Science

## Null Analysis

| Field | Total | Populated | Null | Null % |
| --- | ---: | ---: | ---: | ---: |
| id | 63 | 63 | 0 | 0% |
| faculty | 63 | 63 | 0 | 0% |
| department | 63 | 63 | 0 | 0% |
| major_category | 63 | 63 | 0 | 0% |
| major | 63 | 63 | 0 | 0% |
| degree_type | 63 | 63 | 0 | 0% |
| official_degree_name | 63 | 62 | 1 | 1.6% |
| thesis_or_non_thesis | 63 | 27 | 36 | 57.1% |
| concentrations_or_tracks | 63 | 11 | 52 | 82.5% |
| credits | 63 | 23 | 40 | 63.5% |
| duration | 63 | 6 | 57 | 90.5% |
| language | 63 | 39 | 24 | 38.1% |
| delivery_mode | 63 | 2 | 61 | 96.8% |
| program_description | 63 | 37 | 26 | 41.3% |
| admission_requirements | 63 | 6 | 57 | 90.5% |
| required_documents | 63 | 0 | 63 | 100% |
| gre_requirement | 63 | 2 | 61 | 96.8% |
| gmat_requirement | 63 | 1 | 62 | 98.4% |
| english_requirement | 63 | 36 | 27 | 42.9% |
| portfolio_requirement | 63 | 0 | 63 | 100% |
| interview_requirement | 63 | 0 | 63 | 100% |
| experience_requirement | 63 | 2 | 61 | 96.8% |
| tuition | 63 | 1 | 62 | 98.4% |
| additional_fees | 63 | 0 | 63 | 100% |
| deadlines | 63 | 0 | 63 | 100% |
| scholarships | 63 | 0 | 63 | 100% |
| financial_aid | 63 | 0 | 63 | 100% |
| payment_plans | 63 | 0 | 63 | 100% |
| accreditation | 63 | 1 | 62 | 98.4% |
| official_program_url | 63 | 63 | 0 | 0% |
| sources | 63 | 63 | 0 | 0% |
| notes | 63 | 18 | 45 | 71.4% |

Fields with the highest null rates are `required_documents`, `portfolio_requirement`, `interview_requirement`, `scholarships`, `financial_aid`, `payment_plans`, and `deadlines`, all of which are completely absent in the current inventory.

## Null Classification

### A. Program-specific / program-page fields
- `credits`
- `duration`
- `thesis_or_non_thesis`
- `concentrations_or_tracks`
- `program_description`
- `delivery_mode`
- `accreditation`
- `language`
### B. University-wide / shared AUB pages
- `tuition`
- `additional_fees`
- `required_documents`
- `english_requirement`
- `deadlines`
- `scholarships`
- `financial_aid`
- `payment_plans`

### C. Faculty-level / mixed program-faculty pages
- `admission_requirements`
- `gre_requirement`
- `gmat_requirement`
- `portfolio_requirement`
- `interview_requirement`
- `experience_requirement`

## Source Gap Analysis

Captured source categories:
- program_page: 47 URLs
- faculty_page: 6 URLs
- handbook_pdf: 5 URLs
- admissions_page: 3 URLs
- tuition_fees_page: 1 URLs
- financial_aid_page: 1 URLs
- scholarship_page: 1 URLs
- catalogue_page: 2 URLs
- other_shared: 1 URLs

Missing source categories worth searching next:
- application_fee_page
- graduate_calendar_page
- assistantships_fellowships_page
- faculty_specific_tuition_pages
- unverified_FAFS_graduate_pages
- unverified_FAS_Education_graduate_page
- unverified_FAS_Physics_graduate_page

Recommended AUB pages to search next:
- https://www.aub.edu.lb/comptroller/Pages/Student.aspx
- https://www.aub.edu.lb/graduatecouncil/Pages/default.aspx
- https://www.aub.edu.lb/admissions/Pages/default.aspx
- https://www.aub.edu.lb/admissions/applications/Pages/applications.aspx
- https://www.aub.edu.lb/admissions/Pages/EnglishRequirements.aspx
- https://www.aub.edu.lb/faid/Pages/default.aspx
- https://www.aub.edu.lb/faid/Pages/Scholarships.aspx
- https://www.aub.edu.lb/registrar/Pages/default.aspx
- https://www.aub.edu.lb/fafs/Pages/default.aspx
- https://www.aub.edu.lb/fas/Pages/departments.aspx

## Duplicate And Conflict Audit

- Exact duplicate IDs across files: `bmen-ms`, `bmen-phd`, `cee-ms-et`, `soad-ms-environmental-sciences`, `fas-gpcs`.
- Same program surface with different IDs: `fas-gpcs` and `fas-gpcs-ms` point to the same faculty, department, major, and program URL.
- No source URL was found with different source IDs.

## Missing Faculties / Pages

- Faculty of Agricultural and Food Sciences has no master’s or PhD records in the current JSON set.
- FAS Education graduate page was not verified in this pass.
- FAS Physics graduate page was not verified in this pass.
- The previous `discovery_report.md` is stale and reflects the earlier partial pass rather than this audited inventory.

## Next Steps

1. Complete any remaining missing FAS inventory pages before merging the datasets.
2. Pull shared university-wide pages for tuition, deadlines, documents, scholarships, financial aid, and payment plans.
3. Enrich program-page-specific fields for credits, duration, tracks, and admissions details once the shared fields are in place.
