# AUB Dedup Report

## Input Files Read
- programs.json
- fas_programs.json
- fhs_programs.json
- interfaculty_programs.json
- medicine_programs.json
- nursing_programs.json
- fafs_programs.json
- sources.json
- audit_nulls_summary.json
- fees_report.md

## Totals
- Total rows before dedup: 68
- Total unique programs after dedup: 62
- Duplicates removed: 6

## Duplicate IDs Found
- bmen-ms (2 rows across programs.json, interfaculty_programs.json) -> canonical bmen-ms
- bmen-phd (2 rows across programs.json, interfaculty_programs.json) -> canonical bmen-phd
- cee-ms-et (2 rows across programs.json, interfaculty_programs.json) -> canonical cee-ms-et
- soad-ms-environmental-sciences (2 rows across programs.json, interfaculty_programs.json) -> canonical soad-ms-environmental-sciences

## Same-Program Different-ID Cases
- fas-gpcs / fas-gpcs-ms -> canonical fas-gpcs-ms

## Canonical ID Decisions
- bmen-ms => bmen-ms (duplicate rows with identical id; chosen from interfaculty_programs.json)
- bmen-phd => bmen-phd (duplicate rows with identical id; chosen from interfaculty_programs.json)
- cee-ms-et => cee-ms-et (duplicate rows with identical id; chosen from interfaculty_programs.json)
- soad-ms-environmental-sciences => soad-ms-environmental-sciences (duplicate rows with identical id; chosen from interfaculty_programs.json)
- fas-gpcs / fas-gpcs-ms => fas-gpcs-ms (same program fields matched; chosen from fas_programs.json)

## Canonical Record Notes
- Dedicated faculty/interfaculty files were preferred over programs.json when records matched.
- `fas-gpcs-ms` was chosen as the canonical Computational Science ID; `fas-gpcs` is treated as the old duplicate ID.
- Interfaculty cross-listed records were kept once and annotated in `notes`.

## Unresolved Conflicts
- None.

## DB Import Safety
- Yes, the merged candidate is deduplicated and structurally consistent for import.
- Remaining caveat: this does not resolve non-dedup review items such as proxy records with null official degree names.

## Changed Files
- `research/aub/programs_merged_candidate.json`
- `research/aub/dedup_report.md`
- `research/aub/dedup_summary.json`
