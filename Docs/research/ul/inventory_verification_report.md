# UL Inventory Verification Report

Date: 2026-07-02

## Verdict

**REJECT**

The current `research/ul/programs.json` is structurally valid, but it is not yet a complete canonical graduate inventory for UL. The inventory currently compresses several official UL graduate clusters into single hub-level records, while UL's discovery package and source notes already evidence a materially larger set of distinct graduate items.

## Inputs Reviewed

- `research/ul/programs.json`
- `research/ul/out_of_scope_programs.json`
- `research/ul/source_map.md`
- `research/ul/sources.json`
- `research/ul/discovery_report.md`
- `research/lu/scope_verification_report.md` was not present in this workspace

## Structural Validation

- JSON parses: pass
- Duplicate program IDs: none observed in the current file set
- Duplicate official URLs: intentional hub reuse only
- Compile: pass with `./mvnw -q -DskipTests compile`

## Why 32 Programs Is Not Complete

The current inventory contains:

- 29 MASTER records
- 3 PHD records

That total of 32 is explainable as a grouped inventory, but it is not the same as a canonical per-program extraction.

UL's own discovery package explicitly says some master lists still needed manual follow-up, including:

- Faculty of Fine Arts & Architecture
- Faculty of Agronomy
- Faculty of Pedagogy
- Faculty of Letters and Human Sciences
- Faculty of Economics & Business Administration

In addition, several faculties that are already represented in `programs.json` are represented as grouped hub records rather than as separate official program rows.

## Faculty-by-Faculty Expected vs Extracted

| Faculty / Unit | Official evidence in UL sources | Expected from official sources | Extracted in `programs.json` | Status |
|---|---:|---:|---:|---|
| Faculty of Engineering | 9 named master programs in the source notes | 9 | 9 | Complete |
| Faculty of Science | 1 M1 + 5 M2 programs in official PDFs | 6 | 6 | Complete |
| Faculty of Public Health | master hub plus admission PDF | at least 1 | 1 | Provisional |
| Faculty of Medical Sciences | Clinical Investigation | 1 | 1 | Complete |
| Neuroscience Research Center | Neuroimaging, Neuropsychology, Neuroscience | 3 | 1 grouped record | Under-extracted |
| Medical Research Center | Health Administration and related master material | at least 1 | 1 grouped record | Provisional |
| Faculty of Law and Political and Administrative Sciences | plural master programs and PhD applications | multiple | 1 grouped record | Under-extracted |
| Faculty of Information | 3 named professional master programs | 3 | 3 | Complete |
| Faculty of Technology | 4 named master tracks in source notes/PDFs | 4 | 1 grouped record | Under-extracted |
| Faculty of Pharmacy | 7 named master programs in the source notes | 7 | 1 grouped record | Under-extracted |
| Faculty of Dental Medicine | 4 named master programs in the source notes | 4 | 1 grouped record | Under-extracted |
| Faculty of Economics & Business Administration | master-level activity confirmed, list not fully mined | at least 1 | 1 grouped record | Provisional |
| Faculty of Letters and Human Sciences | master's-level accreditation confirmed, list not fully mined | at least 1 | 1 grouped record | Provisional |
| Institute of Social Sciences | professional master confirmed | 1 | 1 | Complete |
| Doctoral School of Literature, Humanities & Social Sciences | doctoral degree and HDR confirmed | 1 | 1 | Complete |
| Doctoral School of Law, Political, Administrative & Economic Sciences | doctoral degree confirmed | 1 | 1 | Complete |
| Doctoral School of Science & Technology | doctoral degree confirmed | 1 | 1 | Complete |

## Lower-Bound Evidence From Official UL Sources

The discovery package already supports a lower bound of **46 distinct graduate items** before even counting the still-unresolved faculties:

- Engineering: 9
- Science: 6
- Medical Sciences / NRC / MRC named master items: at least 5
- Information: 3
- Technology: 4
- Pharmacy: 7
- Dental Medicine: 4
- Institute of Social Sciences: 1
- Public Health: at least 1
- Doctoral schools: 3
- Law: at least 1
- Economics & Business Administration: at least 1
- Letters and Human Sciences: at least 1

That lower bound is already greater than 32, which means the current inventory is not complete at the program granularity UL exposes.

## Tracks / Concentrations

No evidence was found that tracks or concentrations were intentionally removed from the inventory. The problem is the opposite: several UL hubs expose multiple named programs or tracks, but those clusters are still collapsed into single records.

## Announcements / News

Announcements and news were not counted as standalone programs. That part of the extraction is consistent with the scope rules.

## PDFs

The official PDFs were not fully mined into separate program rows for all faculties. The already-captured PDFs support additional program granularity beyond the current grouped records, especially for:

- Faculty of Technology
- Faculty of Pharmacy
- Faculty of Dental Medicine
- Neuroscience Research Center / Medical Research Center

## Conclusion

The inventory is structurally clean, but not complete enough for shared-data enrichment.

Recommended next step:

1. Run a focused extraction pass on the grouped faculty hubs and PDFs.
2. Split hub-level records into separate official program rows where UL publishes distinct program titles.
3. Re-run inventory QA after the program-level rows are expanded.

