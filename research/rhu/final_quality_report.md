# RHU Final Quality Report

Date accessed: 2026-07-04

## Scope

- Finalized inventory records: 7 MASTER
- PHD records: 0
- Out-of-scope records: 0
- Shared graduate data centralized in `research/rhu/university.json`
- Tuition fully mapped in `research/rhu/fees_mapping_summary.json`

## Validation Results

- `research/rhu/programs.json` parses successfully.
- `research/rhu/out_of_scope_programs.json` parses successfully.
- `research/rhu/university.json` parses successfully.
- `research/rhu/sources.json` parses successfully.
- `research/rhu/fees_mapping_summary.json` parses successfully.
- `research/rhu/shared_report.md` is present and consistent with the shared JSON.
- `research/rhu/enrichment_report.md` is present and consistent with the enrichment pass.
- `research/rhu/program_inventory_report.md` is present and consistent with the frozen inventory.

## Checklist

- 1. `programs.json` has 7 records: pass.
- 2. MASTER = 7: pass.
- 3. PHD = 0: pass.
- 4. `out_of_scope_programs.json` has 0 records: pass.
- 5. Tuition = 7/7: pass.
- 6. No duplicate IDs / source URLs: pass.
- 7. Every source reference resolves: pass.
- 8. Shared data centralized in `university.json`: pass.
- 9. MBA emphases remain concentrations/notes: pass.
- 10. Engineering credits intentionally remain null where thesis paths differ: pass.
- 11. Schema compatibility with V24: pass, using MASTER-only graduate rows and schema-safe null handling where values are not officially published.
- 12. All `research/rhu/*.json` parse: pass.
- 13. `./mvnw -q -DskipTests compile`: pass.

## Program Inventory

- Total programs: 7
- MASTER programs: 7
- PHD programs: 0
- Out-of-scope programs: 0

## Tuition Coverage

- Tuition populated: 7
- Tuition null: 0
- Tuition model: USD per credit hour
- College of Business Administration: USD 200 per credit hour
- College of Engineering: USD 280 per credit hour

## Shared Data Centralization

The following are centralized in `research/rhu/university.json`:

- admissions process
- required documents
- language requirements
- tuition model
- fee structure
- payment methods
- payment plans
- scholarships
- financial aid
- academic calendar
- graduate regulations
- accreditation
- international students

## Official Discrepancies Carried Forward

- MBA emphases:
  - RHU publishes General Business Management and Oil and Gas Management emphases under the MBA.
  - The inventory keeps one MBA degree row and records the emphases in notes / concentrations.
- Engineering credits:
  - The official source set supports thesis and non-thesis engineering paths.
  - A single fixed credit count was not assigned to the engineering programs because the source set does not safely support one canonical number across paths.

## Remaining Official Gaps

- Explicit accreditation statement
- Distinct public program pages for several engineering master's programs
- Distinct public program page for MBA emphases beyond the consolidated MBA record

## Orphan Sources

No orphan-source classification was required in this final pass beyond the source set referenced by the shared and program-level records.

## Recommendation

APPROVE WITH NOTES

## Final Verdict

RHU is ready for V37 seed generation.
