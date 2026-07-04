# UL Final Quality Report

Date accessed: 2026-07-03

## Verdict

APPROVE WITH NOTES

UL is ready for V32 seed generation.

## Validation

- `research/ul/*.json` parse: pass
- `./mvnw -q -DskipTests compile`: pass
- `programs.json` record count: 46
- `MASTER` count: 43
- `PHD` count: 3
- `out_of_scope_programs.json` record count: 2
- Duplicate program IDs: none
- Duplicate source IDs: none
- Duplicate source URLs: none
- Every program has at least one source: pass
- Every source reference resolves: pass
- Every official program URL is UL-owned: pass
- Program tuition null for all 46 programs: pass
- No out-of-scope records in `programs.json`: pass
- Schema compatibility with V24: pass for `degree_type`, `delivery_mode`, `thesis_or_non_thesis`, `language`, and null tuition handling

## Fee and Shared Data Checks

- The 745,000 LBP amount is stored only as a shared university enrollment/registration fee item in `university.json`.
- `UL-SRC-059` and `UL-SRC-060` are the preferred fee sources.
- `UL-SRC-061` is legacy/supporting only.
- Shared admissions, documents, deadlines, and fees are centralized in `university.json`.
- Program-level `admission_requirements` remain null unless a program-specific value was explicitly published.

## Orphan Sources

Orphan sources are sources not referenced by any program row or shared university/fee object.

| Source ID | Classification | Reason |
|---|---|---|
| `UL-SRC-001` | shared/reference | Homepage entry point and navigation hub |
| `UL-SRC-002` | shared/reference | Central admissions landing page |
| `UL-SRC-003` | shared/reference | Specializations search hub |
| `UL-SRC-004` | shared/reference | Global majors hub |
| `UL-SRC-007` | reference | Faculty landing page for Science |
| `UL-SRC-033` | reference | Faculty landing page for Information |
| `UL-SRC-037` | legacy/supporting | Faculty of Technology tuition page flagged as stale |
| `UL-SRC-046` | shared/reference | Faculty details page for Fine Arts & Architecture |

## Completeness Stats

| Field | Populated |
|---|---:|
| `description` | 46 |
| `program_description` | 46 |
| `credits` | 2 |
| `duration` | 0 |
| `thesis_or_non_thesis` | 28 |
| `concentrations` | 7 |
| `concentrations_or_tracks` | 7 |
| `delivery_mode` | 0 |
| `language` | 0 |
| `admission_requirements` | 0 |
| `GRE` | 0 |
| `GMAT` | 0 |
| `interview` | 0 |
| `experience` | 0 |
| `accreditation` | 4 |
| `notes` | 46 |

## Inventory Notes

- UL’s graduate publication surface remains cluster-based for many faculties.
- Most programs still do not expose title-level duration, language, delivery mode, or application requirement detail.
- The current dataset is internally consistent and ready for seed generation with the shared university fee item model.

## Recommendation

APPROVE WITH NOTES

