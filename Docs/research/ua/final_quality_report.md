# UA Final Quality Report

Date accessed: 2026-07-04

## Verdict

APPROVE WITH NOTES

UA is ready for V33 seed generation.

## Validation

- All `research/ua/*.json` parse: pass
- `./mvnw -q -DskipTests compile`: pass
- `programs.json` records: 21
- MASTER count: 21
- PHD count: 0
- `out_of_scope_programs.json` records: 9
- Duplicate program IDs: none
- Duplicate source IDs: none
- Duplicate source URLs: none
- Every program has at least one source: pass
- Every source reference resolves: pass
- Every program URL is an official UA URL: pass
- No out-of-scope records appear in `programs.json`: pass

## Tuition Coverage

- Tuition populated: 19/21
- Tuition null: 2/21
- Null tuition programs:
  - `ua-fot-master-theological-sciences-pastoral-studies`
  - `ua-fot-master-theological-sciences-pastoral-studies-consecrated-life`
- Both null tuition values are limited to the two Theology master's programs because no reliable official tuition value was published.

## Shared Data Centralization

- Shared admissions, required documents, deadlines, and fees are centralized in `university.json`: pass
- Program-level `admission_requirements` are populated only where the official program page published program-specific requirements: pass

## Schema Compatibility With V24

- `degree_type`: pass
- `delivery_mode`: pass
- `thesis_or_non_thesis`: pass
- `language`: pass
- `tuition`: pass

## Orphan Sources

Orphan sources are official UA sources present in `sources.json` but not directly referenced by any program record.

- Discovery sources:
  - `UA051` - Advanced Diploma in Dental Laboratory Technology
  - `UA052` - Specialized Diploma in Gerontology Nursing
  - `UA053` - Doctor of Physical Therapy
- Reference sources:
  - `UA001` - Home | Antonine University
  - `UA002` - Graduate | Antonine University
  - `UA004` - Required Documents | Graduate | Antonine University
  - `UA005` - Application | Graduate | Antonine University
  - `UA007` - Entrance Exams | Graduate | Antonine University
  - `UA009` - Tuition Fees Fall 2026-27
  - `UA010` - ADMISSIONS GUIDE
  - `UA011` - Antonine University Office of Orientation & Admissions Graduate Application Form
  - `UA012` - Apply | Orientation and Admissions | Antonine University
  - `UA013` - Academic Calendar | Antonine University
  - `UA014` - Financial Aid and Scholarships | Antonine University
  - `UA015` - Rules and Regulations | Social Affairs | Antonine University
  - `UA016` - Services | Language Center | Antonine University
  - `UA017` - Language Courses | Antonine University
  - `UA018` - Overview | International | Antonine University
  - `UA019` - Scholarship | Antonine University
  - `UA020` - Mobility Opportunity | Antonine University
  - `UA021` - International News | Antonine University
  - `UA022` - Research Policies | Antonine University
  - `UA023` - SAFAR Scholarships 2025 | Antonine University
  - `UA024` - Supporting Excellence in Research | Antonine University
  - `UA025` - Faculty of Engineering and Technology | Antonine University
- Obsolete:
  - none

## Completeness Statistics

- `program_description`: 21/21
- `credits`: 21/21
- `duration`: 21/21
- `thesis_or_non_thesis`: 14/21
- `concentrations_or_tracks`: 0/21
- `delivery_mode`: 21/21
- `language`: 21/21
- `admission_requirements`: 21/21
- `required_documents`: 0/21
- `gre_requirement`: 0/21
- `gmat_requirement`: 1/21
- `english_requirement`: 0/21
- `portfolio_requirement`: 0/21
- `interview_requirement`: 11/21
- `experience_requirement`: 0/21
- `tuition`: 19/21
- `additional_fees`: 0/21
- `deadlines`: 0/21
- `scholarships`: 0/21
- `financial_aid`: 0/21
- `payment_plans`: 0/21
- `accreditation`: 6/21

## Inventory Notes

- The dataset contains only MASTER records and no PhD rows.
- Official UA sources support the two Theology programs but do not expose reliable tuition values in the reviewed source set.
- Shared university-level admissions, documents, deadlines, and fees were modeled once in `university.json` rather than duplicated across programs.

## Recommendation

APPROVE WITH NOTES
