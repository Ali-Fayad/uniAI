# AOU Lebanon Graduate Program Inventory Report

Date accessed: 2026-07-05

## Summary

- Discovered master's programs: 5
- MASTER records in `programs.json`: 5
- PHD records in `programs.json`: 0
- Out-of-scope records: 3

## Faculty Breakdown

- Faculty of Business Studies: 3
- Faculty of Computer Studies: 1
- Faculty of Language Studies: 1

## Validation

- `research/aou/programs.json` parses successfully.
- `research/aou/out_of_scope_programs.json` parses successfully.
- No duplicate program IDs were found.
- No duplicate non-null official program URLs were found.
- Every `source_id` in `programs.json` exists in `research/aou/sources.json`.
- All non-null official program URLs belong to official AOU Lebanon domains under `*.aou.edu.lb`.
- `programs.json` contains MASTER records only.
- `programs.json` contains zero PHD records, per the discovery rule.

## Duplicate URL Justification

- None. Each in-scope graduate degree has its own official program-detail URL, and out-of-scope diplomas are kept separate in `out_of_scope_programs.json`.

## Official Discrepancies

- AOU's MA in TEFL materials mention both dissertation and comprehensive-exam tracks, while the central graduate listing exposes the thesis-track title.
- The official site is served under `web.aou.edu.lb` in the active crawl context even though the user-supplied domain was `www.aou.edu.lb`; all retained URLs are official `*.aou.edu.lb` URLs.

## Remaining Official Ambiguities

- The MA in TEFL page references multiple track structures, but only the thesis-track title is exposed in the central graduate listing.
- The MBA detail pages expose additional admissions, fee, and refund detail that is useful for later shared-data or enrichment passes, but this inventory only captures the degree rows.
- No official PhD or doctorate program was found on the inspected AOU Lebanon pages.

## Out-of-Scope Summary

- Teaching Diploma in Education
- Teaching Diploma in Physical Education and Sports
- Teaching Diploma in Special Education - Learning Difficulties

These were excluded because the task scope is limited to MASTER and PHD.

## Recommendation

APPROVE WITH NOTES
