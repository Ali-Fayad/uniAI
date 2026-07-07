# LAU Graduate Program Inventory Report

Date accessed: 2026-06-26

## Summary

- In-scope graduate programs: 33
- Master’s programs: 33
- PhD programs: 0
- Graduate certificates moved out of scope: 8
- Professional graduate records moved out of scope: 2
- Diplomas: 0
- Schools: 6
- Departments used in the inventory: 6

## Validation

- `research/lau/programs.json` parses successfully.
- `research/lau/out_of_scope_programs.json` parses successfully.
- Every in-scope record contains the full AUB-compatible schema field set.
- All IDs are unique in both files.
- All URLs used in `official_program_url` are official LAU URLs.
- No in-scope record is missing its own official page URL.

## School Breakdown

### Adnan Kassar School of Business
- Count: 11
- IDs:
  - `business-emba`
  - `business-ma-applied-economics`
  - `business-mba`
  - `business-llm-business-law`
  - `business-ms-finance-and-accounting`
  - `business-ms-data-analytics`
  - `business-ms-human-resources-management`
  - `business-msl`
  - `business-mba-business-analytics-online`
  - `business-mba-global-business-administration-online`
  - `business-mba-healthcare-management-online`

### School of Architecture and Design
- Count: 1
- IDs:
  - `sard-ma-islamic-art`

### School of Arts and Sciences
- Count: 14
- IDs:
  - `soas-ma-comparative-literature`
  - `soas-ma-education`
  - `soas-ma-interdisciplinary-gender-studies`
  - `soas-ma-international-affairs`
  - `soas-ma-migration-studies`
  - `soas-ma-multimedia-journalism`
  - `soas-ms-applied-artificial-intelligence-online`
  - `soas-ms-biological-sciences`
  - `soas-ms-computer-science`
  - `soas-ms-computer-science-online`
  - `soas-ms-cybersecurity-online`
  - `soas-ms-data-science`
  - `soas-ms-data-science-online`
  - `soas-ms-nutrition`

### School of Engineering
- Count: 6
- IDs:
  - `soe-ms-civil-environmental-engineering`
  - `soe-ms-computer-engineering`
  - `soe-ms-engineering-management-online`
  - `soe-ms-industrial-engineering-management`
  - `soe-ms-international-construction-management-online`
  - `soe-ms-mechanical-engineering`

### School of Medicine
- Count: 0
- IDs: none in scope

### School of Pharmacy
- Count: 1
- IDs:
  - `pharmacy-ms-pharmaceutical-development-management`

## Duplicate Detection

- Duplicate IDs: none.
- Same URL with different IDs:
  - `https://sb.lau.edu.lb/academics/programs/emba/` is used by:
    - `business-emba`
    - out-of-scope executive certificate records
  - This is intentional because LAU exposes the EMBA degree and embedded executive certificates on the same official page.
- Same program with different URLs:
  - `MBA` appears as an on-campus program and as three distinct online programs.
  - `MS in Computer Science` appears as on-campus and online offerings.
  - `MS in Data Science` appears as on-campus and online offerings.
  - `MS in Cybersecurity`, `MS in Applied Artificial Intelligence`, `MS in Engineering Management`, and `MS in International Construction Management` are online-only distinct offerings in the public LAU surface.
- Same URL with different IDs that are not duplicates:
  - None beyond the EMBA page and its embedded certificate records.

## Missing Program Pages

- None at the URL-discovery level.

## Programs Lacking Official Pages

- None.

## Notes

- The in-scope inventory now contains only master’s records.
- The following records were moved to [out_of_scope_programs.json](./out_of_scope_programs.json):
  - `business-ai-in-business-certificate`
  - `business-emba-consultancy-certificate`
  - `business-emba-digital-innovation-certificate`
  - `business-emba-finance-technology-certificate`
  - `business-digital-marketing-certificate`
  - `soas-ai-fundamentals-certificate`
  - `soas-cybersecurity-fundamentals-certificate`
  - `soas-data-science-fundamentals-certificate`
  - `medicine-md`
  - `pharmacy-pharmd`
