# UL Import Report

Date accessed: 2026-07-03

## Seed Summary

- University rows: 1
- Faculty/school rows: 17
- Department rows: 0
- Degree type rows: 4
- Language rows: 4
- Source rows: 62
- Program rows: 46
- MASTER rows: 43
- PHD rows: 3
- Tuition rows: 0
- Fee item rows: 2
- Admission requirement rows: 2
- Required document rows: 9
- Deadline rows: 1
- Scholarship rows: 0
- Financial aid rows: 1
- Payment plan rows: 1
- Accreditation rows: 4
- Track rows: 34
- Alias rows: 0
- Program-source links: 106
- Out-of-scope skipped: 2

## Validation

- `research/ul/*.json` parse: pass
- No duplicate program IDs: pass
- No duplicate source IDs: pass
- No duplicate source URLs: pass
- No broken source references: pass
- No tuition rows inserted: pass
- Out-of-scope graduate records skipped: pass
- V24 enum compatibility: pass for `degree_type`, `delivery_mode`, `thesis_or_non_thesis`, `scope_level`, `billing_basis`, and null tuition handling
- Idempotent Flyway pattern: pass
- `./mvnw -q -DskipTests compile`: pass

## Implementation Notes

- UL reuses hub/catalog URLs across multiple graduate programs, so the migration drops `uq_graduate_program_university_url` before seeding.
- Program tuition remains null by design; the shared 745,000 LBP graduate enrollment/registration fee is seeded as a university-level fee item instead.
- Shared admissions, required documents, academic-calendar reference, aid, payment-plan, accreditation, and track data are centralized at university or program scope as appropriate.
- No standalone graduate scholarship row was seeded because the reviewed official sources do not publish a broad scholarship catalog.
- The 106 program-source links preserve traceability to every official UL source referenced in the frozen inventory.
- 8 orphan/reference sources remain in the source catalog by design.

## Recommendation

APPROVE
