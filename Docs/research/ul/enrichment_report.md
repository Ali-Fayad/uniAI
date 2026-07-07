# UL Program Enrichment Report

Date accessed: 2026-07-03

## Summary

- Program count: 46
- MASTER count: 43
- PHD count: 3
- Tuition remains null for all programs by design

## Fields Populated Before vs After

| Field | Before | After |
|---|---:|---:|
| `description` | 46 | 46 |
| `program_description` | 46 | 46 |
| `credits` | 1 | 2 |
| `duration` | 0 | 0 |
| `thesis_or_non_thesis` | 3 | 28 |
| `concentrations` | 7 | 7 |
| `concentrations_or_tracks` | 7 | 7 |
| `delivery_mode` | 0 | 0 |
| `language` | 0 | 0 |
| `admission_requirements` | 0 | 0 |
| `GRE` | 0 | 0 |
| `GMAT` | 0 | 0 |
| `interview` | 0 | 0 |
| `experience` | 0 | 0 |
| `accreditation` | 4 | 4 |

## Programs Updated

- `lu-fe-master-hydrosciences`
- `lu-fe-master-mechanics`
- `lu-fe-master-natural-risks`
- `lu-fe-master-renewable-energies`
- `lu-fe-master-robotics`
- `lu-fe-master-tcmis`
- `lu-fe-master-telecommunications`
- `lu-fe-master-civil-engineering`
- `lu-fe-master-htte`
- `lu-fph-master-programs`
- `lu-law-master-programs`
- `lu-information-master-corporate-communications`
- `lu-information-master-digital-media`
- `lu-information-master-economic-development-journalism`
- `lu-technology-master-geotechnics-environment`
- `lu-technology-master-mechatronics-energy`
- `lu-technology-master-communication-systems-engineering`
- `lu-technology-master-information-systems`
- `lu-pharmacy-master-clinical-pharmacy`
- `lu-pharmacy-master-industrial-cosmetology-dermopharmacy`
- `lu-pharmacy-master-pharmaceutical-industry`
- `lu-pharmacy-master-pharmaceutical-mba`
- `lu-pharmacy-master-clinical-pharmacy-pharmacoepidemiology`
- `lu-pharmacy-master-pharmaceutical-biotechnology`
- `lu-pharmacy-master-pharmacology-toxicology`
## What Was Populated

- `thesis_or_non_thesis` was added for clusters where UL explicitly describes the master offering as research/professional or professional/research.
- `credits` was added for `lu-technology-master-geotechnics-environment` from the official 120 ECTS curriculum note.
- `thesis_or_non_thesis` for the doctoral school rows remains `THESIS`, matching the doctoral structure published by UL.

## Remaining Nulls

- `duration`: 46 null
- `delivery_mode`: 46 null
- `language`: 46 null
- `admission_requirements`: 46 null
- `GRE`: 46 null
- `GMAT`: 46 null
- `interview`: 46 null
- `experience`: 46 null

## Official-Source Gaps

- UL’s publicly visible graduate pages are cluster-based rather than program-page based for many faculties.
- Most faculty hubs do not publish title-level duration, delivery mode, or language information.
- UL does not expose a central graduate requirements catalog that cleanly maps program-specific admissions, GRE/GMAT, interview, or experience requirements.
- The science, engineering, pharmacy, dental, and law clusters expose enough evidence for thesis/non-thesis categorization only at the cluster level, not for every title-specific detail.

## Validation

- JSON parses: pass
- Program count remains 46: pass
- Source references resolve: pass
- Compile: pass with `./mvnw -q -DskipTests compile`

## Recommendation

APPROVE WITH NOTES
