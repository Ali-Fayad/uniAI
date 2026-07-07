# AOU Final Quality Report

## Executive Summary

- Discovery: complete
- Inventory: complete
- Shared data: complete
- Enrichment: complete

Result: `APPROVE WITH NOTES`

## Dataset Checks

- `programs.json` records: 5
- MASTER records: 5
- PHD records: 0
- `out_of_scope_programs.json` records: 3
- Tuition populated: 5/5
- Tuition missing: 0/5
- Duplicate program IDs: none
- Duplicate source IDs: none
- Duplicate source URLs: none
- Duplicate official program URLs: none
- Every source reference resolves: yes
- No out-of-scope records in `programs.json`: yes
- V24 compatibility: preserved

## GMAT Verification

- GMAT-related text was populated only for the MBA records.
- The MBA GMAT text is explicitly published as a GMAT exemption rule: applicants with a minimum GMAT score of 500 or equivalent are exempted from the Graduate Entrance Exam.
- No inferred GMAT values were retained.
- MBA Finance and MBA HRM carry the same explicitly published exemption rule.
- MSc Computing and MA TEFL correctly remain null for GMAT.

## Tuition Coverage

- MBA General: USD 170 per credit
- MBA Finance: USD 170 per credit
- MBA HRM: USD 170 per credit
- MSc in Computing (Cyber Security and Forensics): USD 170 per credit
- MA in Teaching English as a Foreign Language (TEFL) - Thesis Track: USD 170 per credit

## Completeness Statistics

- `program_description`: 5/5
- `credits`: 5/5
- `duration`: 5/5
- `thesis_or_non_thesis`: 5/5
- `concentrations_or_tracks`: 0/5
- `delivery_mode`: 5/5
- `language`: 0/5
- `admission_requirements`: 5/5
- `gre_requirement`: 0/5
- `gmat_requirement`: 3/5
- `portfolio_requirement`: 0/5
- `interview_requirement`: 0/5
- `experience_requirement`: 3/5
- `accreditation`: 5/5
- `tuition`: 5/5

## Orphan Sources

- `AOU_SRC_001` - Arab Open University - Lebanon Home
  - Classification: discovery/navigation hub only
  - Rationale: retained in the discovery/source map phase but not needed as a direct data-source reference in the finalized inventory/shared/enrichment dataset.

## Intentional Nulls

- `concentrations_or_tracks`: not published as a distinct graduate-degree field for these programs.
- `language`: no stable per-program language field was published in a normalized form.
- `gre_requirement`: not published for any of the 5 programs.
- `portfolio_requirement`: not published for any of the 5 programs.
- `interview_requirement`: not published for any of the 5 programs.
- `experience_requirement`: only MBA records publish relevant professional experience.
- `gmat_requirement`: only MBA records publish the GMAT exemption rule.

## Official-Source Limitations

- AOU uses `web.aou.edu.lb` as the live official Lebanon site in the crawl context.
- MA in TEFL materials mention two tracks in the detail page, while the central graduate listing exposes the thesis-track title.
- No separate graduate PhD / doctorate program was found.
- The shared source set does not expose additional stable per-program fields beyond those captured here.

## Validation

- All `research/aou/*.json` files parse successfully.
- `./mvnw -q -DskipTests compile` passed from `Server/`.

## Recommendation

APPROVE WITH NOTES
