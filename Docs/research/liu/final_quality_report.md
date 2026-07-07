# LIU Final Quality Report

## Verdict
APPROVE WITH NOTES

LIU is ready for V34 seed generation.

## Validation
- All `research/liu/*.json` parse: pass
- `programs.json` records: 15
- MASTER count: 15
- PHD count: 0
- `out_of_scope_programs.json` records: 2
- Duplicate program IDs: none
- Duplicate source IDs: none
- Duplicate source URLs: none
- Duplicate official program URL group: 1 intentional catalogue reuse group
- Every program has at least one source: pass
- Every source reference resolves: pass
- Every URL is an official LIU URL: pass
- Tuition coverage: 15 populated, 0 null
- Shared admissions/documents/deadlines/fees centralized in `university.json`: pass
- Program-level admission requirements only appear where LIU published program-specific evidence: pass
- No PhD rows exist: pass
- V24 schema compatibility: pass
- `./mvnw -q -DskipTests compile`: pass

## Completeness Statistics
- `program_description`: 0/15
- `credits`: 3/15
- `duration`: 0/15
- `thesis_or_non_thesis`: 1/15
- `concentrations_or_tracks`: 1/15
- `delivery_mode`: 0/15
- `language`: 0/15
- `admission_requirements`: 0/15
- `gre_requirement`: 0/15
- `gmat_requirement`: 0/15
- `portfolio_requirement`: 0/15
- `interview_requirement`: 0/15
- `experience_requirement`: 0/15
- `accreditation`: 0/15

## Source Coverage
- All 26 official LIU source records are referenced by the inventory or centralized shared data.
- No orphan sources remain after final mapping.

## Source-Usage Classification
- Discovery / institution-level reference sources:
  - `LIU-SRC-001`
  - `LIU-SRC-010`
  - `LIU-SRC-011`
  - `LIU-SRC-012`
  - `LIU-SRC-013`
  - `LIU-SRC-014`
  - `LIU-SRC-015`
  - `LIU-SRC-016`
- Program-level and tuition sources:
  - `LIU-SRC-002` through `LIU-SRC-009`
  - `LIU-SRC-017` through `LIU-SRC-026`

## Inventory Notes
- MBA emphases remain concentrations under the single MBA record.
- MBAT remains a distinct master’s title because LIU publishes it explicitly in the catalogue.
- WORMS / LIU-Hochschule Worms MBA dual degree remains distinct because LIU provides a dedicated plan-of-study PDF.
- The catalogue PDF is intentionally reused for a few title-level records where LIU did not expose a separate stable program page.
- The 15-program inventory plus 2 out-of-scope records matches the finalized LIU graduate footprint without inventing unsupported programs.

