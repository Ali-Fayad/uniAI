# CNAM Lebanon Shared Graduate Data Report

Task code: `SHARED_CNAM_001`
University: Cnam Lebanon / ISSAE-Cnam Liban
Date accessed: 2026-07-07
Official website: https://www.cnam-liban.fr

## Scope Summary

- Program count: 3
- MASTER count: 3
- PHD count: 0
- Out-of-scope count: 0
- Tuition rows mapped: 0
- Tuition source references resolved: yes

## Centralized Shared Data

- Admissions process is centralized in `research/cnam/university.json`.
- Required documents are centralized in `research/cnam/university.json`.
- Language requirements are centralized in `research/cnam/university.json`.
- Tuition remains null because no official graduate tuition table was published in the captured source set.
- Fees are represented only where the source set supported a stable graduate-level statement; no numeric graduate fee table was recovered.
- Scholarships and financial aid are centralized in `research/cnam/university.json`.
- Payment methods, payment plans, academic calendar, regulations, international students, and accreditation notes are centralized in `research/cnam/university.json`.

## Tuition Mapping

- `research/cnam/fees_mapping_summary.json` records 0 tuition rows and 3 unmapped programs.
- No official graduate tuition schedule or stable numeric tuition amount was recovered for the supported Master programmes.
- Tuition is not inferred for any programme.

## Validation

- `research/cnam/programs.json` parses successfully.
- `research/cnam/out_of_scope_programs.json` parses successfully.
- `research/cnam/university.json` parses successfully.
- `research/cnam/fees_mapping_summary.json` parses successfully.
- No duplicate program IDs were introduced.
- No broken source references were found.
- All non-null URLs remain official CNAM URLs.
- `./mvnw -q -DskipTests compile` passed from `/Users/alifayad/uni/uniAI/Server`.

## Notes

- Official evidence supports three Master programmes only.
- No official PhD evidence was found, so no PhD shared data was inferred.
- The generic diploma-policy page mentions Doctorat as a degree level, but it is not treated as a specific active CNAM Lebanon PhD programme.

## Recommendation

APPROVE WITH NOTES
