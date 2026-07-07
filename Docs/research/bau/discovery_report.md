# BAU Graduate Discovery Report

## Status
DEFER / PARTIAL.

## What was discovered
- 10 official graduate faculty listing pages.
- 116 in-scope graduate degree entries across Master, MBA, MArch, DBA and PhD categories.
- 14 graduate non-Master/non-PhD diploma entries captured in `out_of_scope_programs.json`.
- Official BAU sources for admissions, applying, dates/deadlines, degree requirements, tuition PDF, brochures, bylaws and scholarships.

## Important limitation
The crawler could not fully open several BAU HTML/PDF sources due to intermittent BAU fetch errors/timeouts. No third-party sources were used.

## Recommendation
Run a second pass with a browser/Atlas agent or a network environment that can download BAU PDFs and program pages directly, then enrich `official_program_url`, program-specific tuition, language, departments, interview/exam details, and accreditation.
