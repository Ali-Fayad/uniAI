# LIU Enrichment Report

## Fields Populated
- `credits`: 0 -> 3
- `thesis_or_non_thesis`: 0 -> 1
- `concentrations_or_tracks`: 0 -> 1
- All other requested enrichment fields remain null because LIU did not publish title-level values in the reviewed source set.

## Programs Updated
- `liu-sobu-mba`
- `liu-sobu-mbat`
- `liu-soas-ms-computer-science`
- `liu-soas-ms-food-technology`
- `liu-soas-ms-mathematics-applied-mathematics`

## Updated Values
- MBA now carries the eight official emphases as `concentrations_or_tracks` under one program record.
- MBAT is marked `THESIS`.
- The three School of Arts and Sciences master’s programs are marked as `36` credits each.

## Remaining Nulls
- `program_description`: 15/15 null
- `duration`: 15/15 null
- `delivery_mode`: 15/15 null
- `language`: 15/15 null
- `admission_requirements`: 15/15 null
- `gre_requirement`: 15/15 null
- `gmat_requirement`: 15/15 null
- `portfolio_requirement`: 15/15 null
- `interview_requirement`: 15/15 null
- `experience_requirement`: 15/15 null
- `accreditation`: 15/15 null

## Official-Source Gaps
- LIU’s reviewed sources do not expose program-level descriptions beyond titles and school/catalog references.
- No official program-level duration values were captured in the discovery package.
- No program-specific graduate language policy page was found.
- No program-specific GRE, GMAT, portfolio, interview, or experience requirements were published.
- No program-level accreditation statements were isolated for the graduate inventory.

## Validation
- JSON parse: pass
- Program count: 15
- MASTER count: 15
- PHD count: 0
- Every source reference resolves: pass
- `./mvnw -q -DskipTests compile`: pass

## Recommendation
APPROVE WITH NOTES
