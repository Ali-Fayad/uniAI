# AUST Program Enrichment Report

Date accessed: 2026-07-04

## Scope

- Finalized inventory: 17 MASTER programs
- PHD programs: 0
- Tuition coverage before this pass: 17/17
- Shared university data remains centralized in `research/aust/university.json`

## Enrichment Completed

- `credits` populated for all 17 master's records.
- `concentrations_or_tracks` populated for the Biotechnology program because the official FHS material lists DNA Technologies and Forensic Science tracks.
- `notes` retained the official MBA credit discrepancy.

## Field Coverage

- `program_description`: not populated in this pass because no stable program-level description text was recovered from the official AUST pages used here.
- `credits`: 17/17 populated.
- `duration`: not populated in this pass.
- `thesis_or_non_thesis`: not populated in this pass.
- `concentrations_or_tracks`: populated only where officially available.
- `delivery_mode`: not populated in this pass.
- `language`: not populated in this pass.
- `program-specific admission_requirements`: not populated in this pass.
- `gre_requirement`: not populated in this pass.
- `gmat_requirement`: not populated in this pass.
- `portfolio_requirement`: not populated in this pass.
- `interview_requirement`: not populated in this pass.
- `experience_requirement`: not populated in this pass.
- `accreditation`: not populated in this pass.

## Official Basis Used

- Graduate Application Instructions
- Faculty/program pages for the discovered programs
- Faculty of Health Sciences graduate page for Biotechnology track structure
- Current graduate admissions/program pages continued to take precedence over brochure PDFs

## Validation

- `research/aust/programs.json` parses successfully.
- `research/aust/university.json` parses successfully.
- `research/aust/sources.json` parses successfully.
- Program count remains 17.
- MASTER count remains 17.
- PHD count remains 0.
- Every source reference resolves to an entry in `research/aust/sources.json`.
- `./mvnw -q -DskipTests compile` passed from `Server/`.

## Remaining Program-Level Gaps

- Stable program descriptions were not exposed for the majority of programs in the accessible official source set.
- Program-specific admissions, interview, GRE/GMAT, portfolio, and experience requirements were not published at the individual program level in the recovered sources.
- Delivery mode and duration were not consistently published at the program level.

## Official Discrepancy Carried Forward

- MBA credits remain noted as:
  - brochure: 36 credits
  - current admissions/program page: 39 credits
  - inventory and enrichment continue to follow the current admissions/program page

## Recommendation

APPROVE WITH NOTES
