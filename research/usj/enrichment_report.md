# USJ Program-Level Enrichment Report

Date accessed: 2026-06-27

## Scope

This pass enriched only program-specific fields for the 51 in-scope USJ graduate programs.

Shared university-wide fields were not modified.

## Fields populated before vs after

| Field | Before | After | Newly populated |
|---|---:|---:|---:|
| credits | 0 | 3 | 3 |
| duration | 0 | 3 | 3 |
| thesis_or_non_thesis | 0 | 3 | 3 |
| concentrations_or_tracks | 0 | 0 | 0 |
| delivery_mode | 0 | 0 | 0 |
| language | 0 | 3 | 3 |
| program_description | 0 | 3 | 3 |
| admission_requirements | 0 | 3 | 3 |
| gre_requirement | 0 | 0 | 0 |
| gmat_requirement | 0 | 0 | 0 |
| portfolio_requirement | 0 | 0 | 0 |
| interview_requirement | 0 | 2 | 2 |
| experience_requirement | 0 | 0 | 0 |
| accreditation | 0 | 0 | 0 |
| notes | 51 | 51 | 3 updated notes |

## Programs updated

### 1. `usj-fs-master-biomarketing`

Updated fields:

- `official_program_url`
- `thesis_or_non_thesis`
- `credits`
- `duration`
- `language`
- `program_description`
- `admission_requirements`
- `interview_requirement`
- `notes`

Source pages used:

- `usj_fs_biomarketing_pdf`
- `usj_school_fs`
- `usj_grad_catalog_fr`

Remaining null fields:

- `department`
- `concentrations_or_tracks`
- `delivery_mode`
- `required_documents`
- `gre_requirement`
- `gmat_requirement`
- `english_requirement`
- `portfolio_requirement`
- `experience_requirement`
- `tuition`
- `additional_fees`
- `deadlines`
- `scholarships`
- `financial_aid`
- `payment_plans`
- `accreditation`

### 2. `usj-fs-master-data-science`

Updated fields:

- `official_program_url`
- `thesis_or_non_thesis`
- `credits`
- `duration`
- `language`
- `program_description`
- `admission_requirements`
- `interview_requirement`
- `notes`

Source pages used:

- `usj_fs_data_science_pdf`
- `usj_school_fs`
- `usj_grad_catalog_fr`

Remaining null fields:

- `department`
- `concentrations_or_tracks`
- `delivery_mode`
- `required_documents`
- `gre_requirement`
- `gmat_requirement`
- `english_requirement`
- `portfolio_requirement`
- `experience_requirement`
- `tuition`
- `additional_fees`
- `deadlines`
- `scholarships`
- `financial_aid`
- `payment_plans`
- `accreditation`

### 3. `usj-fs-master-environment-management`

Updated fields:

- `official_program_url`
- `thesis_or_non_thesis`
- `credits`
- `duration`
- `language`
- `program_description`
- `admission_requirements`
- `notes`

Source pages used:

- `usj_fs_environment_pdf`
- `usj_school_fs`
- `usj_grad_catalog_fr`

Remaining null fields:

- `concentrations_or_tracks`
- `delivery_mode`
- `required_documents`
- `gre_requirement`
- `gmat_requirement`
- `english_requirement`
- `portfolio_requirement`
- `interview_requirement`
- `experience_requirement`
- `tuition`
- `additional_fees`
- `deadlines`
- `scholarships`
- `financial_aid`
- `payment_plans`
- `accreditation`

## Programs still using hub/catalog URLs only

48 programs still point to a faculty hub or catalog hub because no individual official program page was safely verified in this pass.

These include the remaining programs in:

- Faculté des lettres et des sciences humaines
- Faculté de gestion et de management
- Faculté de droit et des sciences politiques
- Faculté de médecine
- Faculté de sciences infirmières
- Faculté de pharmacie
- most of the Faculty of Sciences programs
- the PhD programs on the Faculty of Sciences hub

These hub URLs intentionally remain reused across multiple records:

- `https://usj.edu.lb/fr/e-doors/masters` - 24 programs
- `https://usj.edu.lb/fr/fs/formations` - 8 programs
- `https://usj.edu.lb/fgm/` - 6 programs
- `https://usj.edu.lb/fdsp/` - 5 programs
- `https://usj.edu.lb/fmd/` - 3 programs

## New sources added

No new source registry entries were required in this pass.

The three PDF sources used here were already present in `research/usj/sources.json`:

- `usj_fs_biomarketing_pdf`
- `usj_fs_data_science_pdf`
- `usj_fs_environment_pdf`

## Remaining null fields

Across the full 51-program inventory, the remaining nulls are concentrated in:

- `required_documents`
- `english_requirement`
- `deadlines`
- `scholarships`
- `financial_aid`
- `payment_plans`
- `additional_fees`
- `delivery_mode`
- `concentrations_or_tracks`
- `gre_requirement`
- `gmat_requirement`
- `portfolio_requirement`
- `experience_requirement`
- `accreditation`

These remain null unless an official program or faculty page explicitly states them.

## Official pages searched but not useful

The following official surfaces were reviewed but did not produce additional safe program-level detail for most programs:

- `https://usj.edu.lb/fr/fgm`
- `https://usj.edu.lb/fdsp/`
- `https://usj.edu.lb/fmd/`
- `https://usj.edu.lb/fsi/`
- `https://usj.edu.lb/fp/`
- `https://usj.edu.lb/fr/fs/formations` for the programs not backed by PDF sheets

## Manual review items

- The Faculty of Sciences PDF-backed programs are now significantly richer than the rest of the inventory.
- Most other faculties remain hub-only until individual program pages are verified.
- `delivery_mode` was intentionally left null because the campus field is not the same as an explicit on-campus delivery statement.
- `accreditation` was left null for the programs unless a program-specific statement appears later.
- Duplicate program URLs remain intentional for hub-anchored listings and are documented in the record notes.

## Recommendation

`APPROVE WITH NOTES`

Reason:

- The pass safely enriched the three programs with the strongest official evidence.
- No shared university-wide data was overwritten.
- The remaining nulls are still legitimate because the official pages reviewed here did not state them.
