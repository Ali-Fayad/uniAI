# BAU Final Acceptance QA

**Task:** `BAU_ACCEPTANCE_QA_002_AFTER_NORMALIZATION`

## Verdict
**APPROVE WITH NOTES**

## Bottom Line
The normalized BAU dataset is source-traceable, internally consistent, and constrained to the strict MASTER/PHD import scope.

## Validation Summary
- JSON parsing: passed for `programs.json`, `out_of_scope_programs.json`, `university.json`, `sources.json`, and `fees_mapping_summary.json`.
- Duplicate program IDs: none.
- Duplicate source IDs: none.
- Duplicate source URLs: none.
- Every program has at least one source: yes.
- Every tuition object references an official BAU source: yes.
- Every `official_program_url` is an official BAU URL: yes.
- `./mvnw -q -DskipTests compile`: could not run from this workspace because `./mvnw` is not present here. This is an environment/repository-layout issue, not a data issue.

## Scope Safety
- Certificates, diplomas, speciality diplomas, and similar non-degree items are correctly isolated in `out_of_scope_programs.json`.
- No undergraduate programs were found in `programs.json`.
- No postgraduate residency programs were found in `programs.json`.
- The remaining non-scope degree families were moved out of `programs.json` and into `out_of_scope_programs.json`.

## Duplicate and Collision Checks
- No duplicate program IDs.
- No duplicate official-program URL collisions for the same degree/name pair.
- No normalized title collisions within faculty/department/degree buckets.
- No duplicate source IDs or URLs.

## Tuition and Provenance
- Tuition coverage: `109/109`.
- Tuition sources: all tuition objects point to `bau_grad_tuition_pdf`.
- Source metadata: `access_date` and `notes` are present on every source.
- All source URLs are official BAU URLs.

## University Centralization
Shared admissions, documents, deadlines, language rules, scholarships, and related graduate-wide information are centralized in `university.json` as intended.

## Final Assessment
The dataset now satisfies the MASTER/PHD-only acceptance rule and is ready for V30 seed generation.

## Ready For V30?
**Yes.** BAU is ready for V30 seed generation.
