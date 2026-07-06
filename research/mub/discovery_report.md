# MUB Graduate Discovery Report

Task code: `BROWSER_MUB_DISCOVERY_001`  
University: Makassed University of Beirut (MUB)  
Discovery mode: browser-based, discovery only  
Output scope: source map, sources registry, discovery report only

## Executive conclusion

Official graduate evidence was found on the Makassed University of Beirut website.

- Master's evidence: FOUND
- PhD evidence: FOUND
- Recommended graduate inventory status: APPROVE WITH NOTES

The only strong official in-scope evidence found is for the Faculty of Islamic Studies. The Faculty of Islamic Studies page states that the faculty focuses exclusively on graduate studies and offers master’s and doctoral programs. It also provides master admission requirements and a doctoral program description.

Do not create graduate inventory rows for Nursing or Teacher Education based on this discovery pass alone.

## Official website / domain resolution

The task prompt listed `https://www.makassed.edu.lb`, but the official university pages discovered and inspected are hosted on:

- `https://mub.edu.lb/`

The MUB homepage, faculty pages, tuition page, financial support page, and academic calendar are all on `mub.edu.lb`. The broader Makassed organization appears separately as `makassed.org`; it was not used as program evidence.

## Scope rules applied

### In scope

- MASTER
- PHD

### Out of scope

- Bachelor
- Diploma
- Certificate
- Continuing Education
- Professional Programs
- Training
- News
- Events

### Evidence rules applied

Graduate programs were accepted only where official MUB pages directly describe master’s or doctoral programs, graduate admissions, or graduate tuition. Faculty pages, research references, partnerships, news, conferences, and staff profiles were not treated as evidence unless they directly described an official program.

## Pages inspected

| Source ID | Page | URL | Result |
|---|---|---|---|
| MUB_SRC_001 | Home | https://mub.edu.lb/ | Official identity/navigation; no direct graduate evidence |
| MUB_SRC_002 | Faculties | https://mub.edu.lb/faculties/ | Lists three faculties; no direct graduate evidence |
| MUB_SRC_003 | Faculty of Islamic Studies | https://mub.edu.lb/faculty-of-islamic-studies/ | Primary official MASTER and PHD evidence |
| MUB_SRC_004 | Tuition fees | https://mub.edu.lb/tuition-fees/ | Supporting tuition evidence for Islamic Studies Master Diploma and PHD Diploma |
| MUB_SRC_005 | Apply Online | https://mub.edu.lb/apply-online/ | Direct fetch blocked; search cache showed application document requirements; not primary evidence |
| MUB_SRC_006 | Financial Support | https://mub.edu.lb/financial-support/ | Financial aid form; no graduate-specific rule found |
| MUB_SRC_007 | Academic Calendar | https://mub.edu.lb/news-and-events/academic-calendar/ | 2025-2026 academic calendar/holiday page; no graduate-specific calendar found |
| MUB_SRC_008 | Faculty of Nursing and Health Sciences | https://mub.edu.lb/faculty-of-nursing-and-health-sciences/ | Weak mission-level Masters reference; no clear current program listing captured |
| MUB_SRC_009 | Faculty of Teacher Education | https://mub.edu.lb/faculties/the-faculty-of-teacher-education/ | Diplomas/certificates only; out of scope |

## Graduate program findings

### Faculty of Islamic Studies — Master's evidence

Status: official evidence found.

Evidence summary:

- The page states that the faculty focuses exclusively on graduate studies and offers only master’s and doctoral programs.
- The page describes a Master’s Degree over four semesters.
- It describes preparatory and regular phases.
- It lists focus areas including charitable institutions management, media and publishing, scientific research and academia, religious relations and conflict resolution, Islamic financial management/economic development, family affairs, cultural documentation/manuscript management, and education.
- It states admission requirements for the Master’s program, including proof of bachelor’s degree, communication/oration skills, a personal statement, and a personal interview.
- It includes 2025-2026 master schedule sections in Arabic for preparatory and regular master phases.

Inventory implication:

- A future extraction pass can create at least one MASTER-level entry for Islamic Studies, but the exact canonical program title should be extracted carefully from the page and/or any official catalog/PDF if available.

### Faculty of Islamic Studies — PhD evidence

Status: official evidence found.

Evidence summary:

- The page describes a Doctoral Program requiring a minimum of three years of study focused on thesis preparation.
- It states applicants need a master’s degree in Islamic studies or equivalent, with at least a “good” grade.
- It describes admissions committee review and scientific council/supervisor assignment.
- The tuition page lists Faculty of Islamic Studies PHD Diploma tuition and defense fee rows for 2025-2026.

Inventory implication:

- A future extraction pass can create a PHD-level Islamic Studies entry if the project accepts the site wording as the official program evidence.

### Faculty of Nursing and Health Sciences

Status: insufficient for inventory row.

The Nursing page includes a mission-level sentence saying the Masters’ program is developed to prepare nurses for advanced practice. However, the captured program section is Bachelor of Science in Nursing focused, and no current official master program detail/admission/curriculum section was captured during this pass.

Decision: do not create a Nursing MASTER inventory row from this discovery alone.

### Faculty of Teacher Education

Status: no in-scope graduate evidence found.

The page lists Teaching Diploma programs and postgraduate professional certificates in EdTech and AI. These are out of scope because diplomas and certificates are excluded. A faculty-profile reference to another university’s graduate program was ignored.

Decision: do not create Teacher Education MASTER/PHD inventory rows.

## Required discovery areas

| Area | Finding |
|---|---|
| Graduate programs | Found for Faculty of Islamic Studies only |
| Graduate admissions | Found for Faculty of Islamic Studies Master’s and Doctoral Program descriptions |
| Tuition | Found graduate-supporting tuition rows for Faculty of Islamic Studies Master Diploma and PHD Diploma |
| Scholarships / financial aid | Financial support form found; no graduate-specific rule captured |
| Required documents | Apply Online page direct fetch blocked; search cache showed document requirements for students from other universities/private university degree equivalency; not used as primary program evidence |
| Language requirements | No explicit graduate language requirement found in captured official pages |
| Academic calendar | 2025-2026 academic calendar page found; no graduate-specific calendar captured |
| Graduate regulations | Faculty of Islamic Studies page contains academic rules/regulation section; captured section appears conduct-focused; no complete graduate regulation document found |

## Explicit absence statements

Master's: official Master's evidence found for Faculty of Islamic Studies.

PhD: official PhD evidence found for Faculty of Islamic Studies.

## Recommendation

APPROVE WITH NOTES

Rationale:

- Approve the discovery pass because official MASTER and PHD evidence was found on an official MUB page.
- Use caution in the next extraction phase: create graduate inventory only for Islamic Studies unless additional official pages/PDFs prove Nursing or other graduate programs.
- Do not infer additional graduate programs from tuition rows, faculty mission statements, diplomas/certificates, news, conferences, or staff biographies.

## Validation

- `sources.json` parses successfully.
- No duplicate source IDs.
- No duplicate URLs.
- Source URLs are official MUB university URLs only (`https://mub.edu.lb/...`).
- No `programs.json`, `university.json`, DB migration, or seed file was created.
