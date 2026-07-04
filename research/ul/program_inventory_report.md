# Lebanese University Program Inventory Report

## Summary

- Total programs: 32
- MASTER count: 29
- PHD count: 3
- Out-of-scope count: 2

## Duplicate URL Groups

- `https://ul.edu.lb/en/colleges-faculties-majors/343/Faculty-of-Engineering`
  - 9 master records reuse the Faculty of Engineering hub intentionally.
- `https://ul.edu.lb/en/colleges-faculties-majors/265/Faculty-of-Information`
  - 3 master records reuse the Faculty of Information hub intentionally.

## Intentional URL Reuse

- Faculty hub reuse is intentional where UL exposes a single graduate listing page for several master records.
- Science master records use distinct official PDF URLs, so no reuse there.

## Faculty Breakdown

- Faculty of Engineering: 9
- Faculty of Science: 6
- Faculty of Public Health: 1
- Faculty of Medical Sciences: 1
- Neuroscience Research Center: 1
- Medical Research Center: 1
- Faculty of Law and Political and Administrative Sciences: 1
- Faculty of Information: 3
- Faculty of Technology: 1
- Faculty of Pharmacy: 1
- Faculty of Dental Medicine: 1
- Faculty of Economics & Business Administration: 1
- Faculty of Letters and Human Sciences: 1
- Institute of Social Sciences: 1
- Doctoral School of Literature, Humanities & Social Sciences: 1
- Doctoral School of Law, Political, Administrative & Economic Sciences: 1
- Doctoral School of Science & Technology: 1

## Source Coverage

- Every record in `programs.json` has at least one official UL source.
- Source IDs resolve against `research/lu/sources.json`.
- Official URLs belong to `ul.edu.lb`, `lu.ul.edu.lb`, `ft.ul.edu.lb`, or `iut.ul.edu.lb`.

## Inventory Notes

- UL is decentralized; the canonical inventory mixes faculty hub pages, announcement pages, PDFs, and doctoral school pages.
- Some graduate offerings are published as hub-level groupings rather than isolated single-program pages.
- The faculty details page for Letters and Human Sciences is the strongest explicit master-level signal for that area in this pass.
- The doctoral school pages provide the cleanest PhD boundaries and were seeded as three doctoral records.

## Recommendation

APPROVE WITH NOTES

The inventory is structurally complete for the discovered official UL graduate scope, with 29 master records, 3 doctoral records, and 2 excluded non-program graduate-related records.
