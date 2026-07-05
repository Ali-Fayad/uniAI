# HU Discovery Report

Task code: `HU_DISCOVERY_001_BROWSER_INV`  
University: Haigazian University (HU)  
Official website: https://www.haigazian.edu.lb  
Date accessed: 2026-07-04  
Recommendation: **APPROVE WITH NOTES**

## Verdict

**APPROVE WITH NOTES.** HU discovery is complete enough to proceed to the next research/normalization phase for MASTER programs. No PHD/doctorate programs were found on official HU sources during this discovery pass.

## Files created

- `research/hu/source_map.md`
- `research/hu/sources.json`
- `research/hu/discovery_report.md`

No `programs.json`, `university.json`, DB migrations, or seed files were created.

## Validation

- `sources.json` parses: pass
- Duplicate source IDs: none
- Duplicate URLs: none
- Official HU URLs only: pass
- Compile: not required

## Discovery summary

Official HU sources show the following in-scope graduate offering set:

| Program / track label | Degree | Credits | Faculty / school | Discovery status |
|---|---:|---:|---|---|
| General Business Administration | MBA | 39 | Faculty of Business Administration and Economics | Found |
| Accounting | MBA | 39 | Faculty of Business Administration and Economics | Found |
| Finance | MBA | 39 | Faculty of Business Administration and Economics | Found |
| Human Resources Management | MBA | 39 | Faculty of Business Administration and Economics | Found |
| Management | MBA | 39 | Faculty of Business Administration and Economics | Found |
| Marketing | MBA | 39 | Faculty of Business Administration and Economics | Found |
| Education | MA | 33 | Faculty of Social and Behavioral Sciences | Found |
| Psychology | MA | 33 | Faculty of Social and Behavioral Sciences | Found |
| PHD / Doctorate | PHD | n/a | n/a | Not found |

## Important findings

### Graduate program structure

- HU’s graduate program index lists eight in-scope MASTER-level rows: six MBA rows and two MA rows.
- MBA rows appear as separate entries on the index, but their PDFs share a common MBA framework and present Accounting, Finance, General MBA, Human Resources Management, Management, and Marketing as specialization areas.
- MA programs found: Education and Psychology.
- No PHD/doctorate offering was found in the official graduate program index or the official catalog.

### Faculties / schools

- MBA programs are under the Faculty of Business Administration and Economics.
- MA Education and MA Psychology are under the Faculty of Social and Behavioral Sciences.
- HU faculty listing also exposes broader structure including School of Arts and Sciences, Natural Sciences, Mathematical Sciences, Social and Behavioral Sciences, and Humanities, but only Business Administration/Economics and Social/Behavioral Sciences are directly relevant to the in-scope graduate programs found.

### Admissions

- Graduate admissions pages were found, but extracted HTML is sparse.
- Graduate admissions information page lists categories such as regular graduate students, undergraduate students, special students, auditors, transfer students, and re-admission.
- General admissions page states admission decisions consider chosen course of study, academic record, recommendation/character, and entrance examination results.
- Apply Now page provides the online application path.
- The official catalog should be treated as the fallback source for full admission details during normalization.

### Tuition / fees / payment plans

- Tuition & Fees page confirms tuition is based on current rates, financial arrangements are due at the beginning of each term, tuition/fees are due at registration and before semester end, and installment payments are allowed through a signed request/declaration form.
- The page notes a 10% tuition subsidy for 2025-2026, including undergraduate and graduate programs.
- Graduate Tuition and Fees page was found, but the extracted content mostly exposed delinquent-payment policy rather than a complete rate table.
- Graduate Tuition Fee Calculator page was found, but rates were not machine-readable in extracted content.

### Financial aid / scholarships

- Financial Aid page says financial assistance is based on academic achievement and demonstrated need and is granted annually to regular full-time students.
- Financial Aid FAQ explicitly says full-time graduate students may apply for graduate financial aid, with limited recipients and priority given to demonstrated need.
- Types of Financial Aid page lists need-based financial aid and academic scholarships.
- Apply for Financial Aid page lists supporting documents and says complete applications are required.
- Financial Aid Bulletin lists other additional aid sources, but external linked fund pages were not used as official HU sources.

### Graduate regulations / academic rules

- Graduate Academic Information page covers time limitation, normal academic progress, examinations and grading, courses and requirements, repeating/withdrawal/change of major, graduation, and separate academic rules for MA and MBA.
- Graduate Handbook page and PDF were found and should be used for thesis procedures and detailed graduate-study rules.

### Language / international students

- Academics/catalog sources state HU operates on the U.S. higher-education model and uses English as the language of instruction.
- International Students page was found, but extracted content is primarily Freshman/Sophomore/Transfer/Special category and states international students must fulfill regular undergraduate admission requirements. No graduate-specific international-student policy was found in extracted content.

## Risks / notes

1. **MBA modeling risk:** HU lists six MBA rows, but the program PDFs frame them as MBA specialization areas. Next phase should decide whether the app models these as separate programs or as one MBA with tracks/concentrations.
2. **Tuition extraction risk:** Graduate tuition rates were not fully machine-readable from the HTML graduate tuition page/calculator. Use the catalog and/or manually inspect the calculator/PDF if tuition rows are required later.
3. **Admissions detail risk:** Graduate admissions HTML is sparse. Use the official catalog for full admissions requirements and required documents during seeding.
4. **No PHD found:** This appears to be a genuine absence from official HU graduate sources, not a missed section.

## Recommendation

Proceed to the next phase with **APPROVE WITH NOTES**. The discovery source set is sufficient for HU MASTER seeding research, with the MBA modeling and tuition-rate caveats handled explicitly before creating normalized data.
