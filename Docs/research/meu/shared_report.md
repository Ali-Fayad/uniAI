# MEU Shared Graduate Data Report

Task code: `MEU_SHARED_001`
University: Middle East University (MEU)
Date accessed: 2026-07-05

## Scope Summary

- Program count: 4
- MASTER count: 4
- PHD count: 0
- Out-of-scope count: 0
- Tuition rows mapped: 1 shared graduate tuition row
- Tuition source references resolved: yes

## Centralized Shared Data

- Admissions process and timing are centralized in `research/meu/university.json`.
- Required documents are centralized in `research/meu/university.json`.
- Language requirements are centralized in `research/meu/university.json`.
- Tuition, application fee, registration fee, deposits, and recurring program fees are centralized in `research/meu/university.json`.
- Scholarships, financial aid, payment methods, payment plans, academic calendar, graduate regulations, international student rules, and accreditation notes are centralized in `research/meu/university.json`.

## Tuition Mapping

- Official graduate tuition published by MEU: USD 305 per credit.
- Application fee: USD 85 for graduate applicants.
- Registration fee: USD 220.
- MBA business program fee: USD 135 per semester and USD 85 per summer session.
- Foreign student advance deposit: USD 2,500, credited to tuition and fees.
- Lebanese registration deposit: USD 200, credited to tuition and fees.
- No tuition amount was inferred beyond what the official MEU fee schedule publishes.

## Validation

- `research/meu/programs.json` parses successfully.
- `research/meu/out_of_scope_programs.json` parses successfully.
- `research/meu/university.json` parses successfully.
- `research/meu/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were found.
- No broken source references were found.
- All non-null `official_program_url` values are official MEU URLs.
- Program count remains 4.
- MASTER count remains 4.
- PHD count remains 0.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Notes

- Cardiff MBA remains one program with four concentrations/tracks.
- The official MEU source set does not include any confirmed PhD program, so none were created.
- Shared fee data was intentionally centralized instead of duplicated across program records where possible.

## Recommendation

APPROVE WITH NOTES
