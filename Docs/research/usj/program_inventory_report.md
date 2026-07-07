# USJ Graduate Program Inventory Report

Date accessed: 2026-06-27

## Summary

- Total raw titles reviewed: 106
- In-scope programs: 51
- Master’s count: 47
- PhD count: 4
- Out-of-scope records: 5
- Faculties / schools / institutes used in the inventory: 10

## Validation

- `research/usj/programs.json` parses successfully.
- `research/usj/out_of_scope_programs.json` parses successfully.
- All IDs are unique across in-scope and out-of-scope files.
- All URLs belong to official USJ properties.
- No duplicate source IDs were introduced.

## Program Groups

### Faculté des lettres et des sciences humaines Ramez G. Chagoury
- Count: 18

### Institut d'études scéniques, audiovisuelles et cinématographiques
- Count: 4

### Faculté de droit et des sciences politiques
- Count: 5

### Faculté de gestion et de management
- Count: 6

### Faculté des sciences
- Count: 6 master’s + 4 doctoral

### Faculté de médecine / health schools
- Count: 5

## Duplicate / Cross-Listed Cases

- English mirror titles exist on the graduate hub for several programs. They were not modeled as separate records.
- French and Arabic variants appear for some USJ programs; the canonical record uses one title and captures alternates in notes where visible.
- The source hub is used as the canonical discovery source for many entries because individual page canonicalization was not completed in this inventory pass.
- Reused official_program_url values are intentional for hub-anchored records:
  - `https://usj.edu.lb/fr/e-doors/masters` used by 24 records
  - `https://usj.edu.lb/fr/fs/formations` used by 11 records
  - `https://usj.edu.lb/fgm/` used by 6 records
  - `https://usj.edu.lb/fdsp/` used by 5 records
  - `https://usj.edu.lb/fmd/` used by 3 records

## French-Only Pages

Most source pages used here are French-first or French-only. This was noted in the source map and remains true for the majority of in-scope programs.

## Missing Individual Program URLs

- Some records still point to a faculty hub or the graduate hub rather than a verified program-specific URL.
- Those are the main remaining items to canonicalize in a follow-up pass.

## Uncertainty List

- Dual-degree and university-diploma items were moved to out-of-scope.
- Titles with multiple language variants were canonicalized to one record when the alternate page did not clearly represent a distinct degree.

## Recommendation

`APPROVE WITH NOTES`

The in-scope inventory is usable, but a follow-up canonical URL normalization pass is still recommended for records currently anchored to hub pages.
