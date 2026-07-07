# JU Program Inventory Report

## Inventory Summary

Jinan University's official graduate inventory was built from the university's own `jinan.edu.lb` source set. The authoritative graduate list is the official Majors & Programs page, supported by the Graduate and Entrance Registration pages.

## Counts

- Master records discovered: 17
- PhD records discovered: 3
- Out-of-scope graduate-adjacent records captured: 8
- Total in-scope graduate records: 20

## Faculty Breakdown

- Faculty of Literature and Humanities: 2 MASTER, 1 PHD
- Faculty of Business Administration: 6 MASTER, 1 PHD
- Faculty of Communication: 2 MASTER
- Faculty of Public Health: 3 MASTER
- Faculty of Education: 2 MASTER
- Political Science Institute: 1 MASTER
- The Faculty of Shariaa & Islamic Studies: 1 MASTER, 1 PHD

## Out-of-Scope Summary

Captured out-of-scope entries from the official Majors & Programs page:

- 6 Teaching Diploma programs under the Faculty of Education
- 1 Honors entry under the Faculty of Literature and Humanities
- 1 Honors entry under The Faculty of Shariaa & Islamic Studies

These were excluded because the task scope is MASTER and PHD only.

## Official Discrepancies

No material graduate-program discrepancy was introduced in the inventory step. The page-level extraction clearly separated MASTER, PHD, Teaching Diploma, and Honors entries, so no inferential cleanup was required.

## Duplicate URL Justification

All `official_program_url` values were intentionally left `null`.

Reason:

- The discovery pass exposed the graduate list and program titles, but did not expose stable dedicated program-detail URLs for each graduate degree.
- Reusing the Majors & Programs hub as a per-program URL would create duplicate URLs without adding program-specific evidence.

## Remaining Official Ambiguities

- JU's graduate majors page is the authoritative source for the program list, but the browser extraction did not expose a dedicated detail URL for each master or PhD program.
- The Graduate page exposes admissions and graduate-regulation tabs, but not all tab contents were fully rendered in the extracted text.

## Validation

- `research/ju/programs.json` parses successfully.
- `research/ju/out_of_scope_programs.json` parses successfully.
- Every included program references official JU source IDs.
- All source IDs resolve to the official JU source set.
- All URLs used in evidence are official JU URLs.
- No duplicate program IDs were introduced.
- No broken source references were introduced.
- `./mvnw -q -DskipTests compile` passed from `Server/`.

## Recommendation

APPROVE WITH NOTES
