# UA Discovery Report

Task code: UA_DISCOVERY_001_BROWSER_INV  
University: Université Antonine / Antonine University (UA)  
Official website: https://ua.edu.lb  
Date accessed: 2026-07-04  
Scope: Master and PhD only

## Verdict

APPROVE WITH NOTES

UA has enough official source coverage to proceed to a later inventory extraction for Master-level graduate programs. PhD should not be extracted as a normal UA degree inventory item at this stage because no official UA graduate program inventory page was found listing PhD / Doctor of Philosophy programs. Doctoral co-supervision and doctoral scholarship content exists, but it appears as research/international scholarship activity rather than a UA-owned program listing.

## Master's offered?

YES.

The official graduate hub frames UA graduate studies as master's-level study, and the official Graduate Programs page lists Master/MBA/MA/MS programs across several faculties/schools. The official Tuition Fees page and the official 2026-27 tuition PDF also confirm graduate-cycle programs, credits, and prices.

## PhD offered?

DEFER / NOT CONFIRMED AS A UA PROGRAM.

Evidence found:

- The official Graduate Programs page does not list any PhD / Doctor of Philosophy program.
- The official Graduate page describes the audience as master's students.
- Official UA pages do mention doctoral students, doctoral co-supervision, doctoral scholarships, and PhD research mobility, especially under International and Research sections.
- Those doctoral references are not sufficient to create UA PhD program records because they do not provide program titles, faculties, credits, admissions requirements, or a normal UA program page.

Recommended handling: keep PhD as `not confirmed` for program extraction, with a follow-up focused on official research-policy PDFs and doctoral co-supervision documents if the project later needs to model doctoral partnerships/scholarships separately.

## Faculties / schools discovered

- Antonine School of Business
- Faculty of Engineering and Technology
- Faculty of Information and Communication
- Faculty of Music and Musicology
- Faculty of Public Health
- Faculty of Sport Sciences
- Faculty of Theology

## Graduate programs discovered in scope

### Antonine School of Business

- Master in Business Administration – Accounting and Auditing
- Master in Business Administration – Marketing and International Management
- Master in Business Administration – Banking and Finance
- Master in Business Administration – Human Resource Management
- Master in Business Administration – Operations and Logistics Management
- Master in Business Administration – Digital Marketing
- Master of Business Administration – General Management (MBA) for non-business students

### Faculty of Information and Communication

- Master of Arts in Information and Communication

### Faculty of Music and Musicology

- Master in Music and Musicology – General Musicology of Modal Traditions
- Master in Music and Musicology – Music Education
- Master in Music and Musicology – Music Therapy
- Master in Music and Musicology – Music Technology and Media
- Master in Music and Musicology – Art Music of the Mashriq
- Master in Music and Musicology – European Art Music

### Faculty of Public Health

- Master of Science in Nursing Sciences
- Doctor of Physical Therapy (DPT) appears in the graduate inventory, but it should be treated as a professional doctorate/graduate health program, not a PhD.

Out-of-scope FPH items found:

- Advanced Diploma in Dental Laboratory Technology
- Specialized Diploma in Gerontology Nursing

### Faculty of Sport Sciences

- Master of Arts in Sport Sciences – Motricity Education and Adapted Physical Activities
- Master of Arts in Sport Sciences – Sports Training
- Master of Arts in Sport Sciences – Sports Management
- MBA in Sports Management (ESG)

### Faculty of Theology

- Master of Arts in Theological Sciences and Pastoral Studies
- Master of Arts in Theological Sciences and Pastoral Studies – Theology of Consecrated Life

## Official sources found

30 official UA sources were recorded in `sources.json`.

Primary extraction sources:

- UA002 — Graduate hub
- UA003 — Graduate programs
- UA004 — Graduate required documents
- UA005 — Graduate application
- UA006 — Graduate selection criteria
- UA007 — Graduate entrance exams
- UA008 — Graduate tuition fees
- UA009 — Tuition and general fees PDF
- UA013 — Academic calendar

Supporting discovery sources:

- UA010 — Admissions guide PDF
- UA011 — Graduate application form PDF
- UA012 — Office of Orientation and Admissions apply page
- UA014 / UA015 — Financial aid pages
- UA016 / UA017 — Language Center pages
- UA018-021 — International pages
- UA022-024 — Research/doctoral context pages
- UA026-029 — Representative program detail pages
- UA030 — FMM Master accreditation PDF

## PDFs found

- `ua-admissions-guide.pdf`
- `ooa-application-form-graduate.pdf`
- `tuition-and-general-fees-fall-2026-27.pdf`
- `certificate-fmm-master.pdf`

Additional official UA PDFs appeared in search results, but some were event/research/news related and therefore not used as core program sources.

## Admissions / required documents discovered

Graduate admissions sources found:

- Graduate application page
- Graduate required documents page
- Selection criteria page
- Entrance exams page
- Office of Orientation and Admissions Apply page
- Graduate application form PDF
- Admissions guide PDF

Required documents include passport photos, ID/civil status/passport copies, family status record, Lebanese Baccalaureate or equivalent certified by MEHE, transfer records/course descriptions where applicable, and Bachelor degree or equivalent certified by MEHE.

Application-related fees found:

- Application fee: $70
- Entrance exam fee: $50

## Tuition / fees discovered

Graduate tuition was found on both the Graduate Tuition Fees page and the official Tuition Fees Fall 2026-27 PDF.

Examples discovered:

- MBA / Master of Business Administration: $230 per credit
- MA Information and Communication: $215 per credit
- Master in Music and Musicology: collective and individual course pricing shown
- MS Nursing Sciences: $235 per credit
- MA Sport Sciences: $265 per credit
- MA Theological Sciences and Pastoral Studies: $90 per credit
- DPT: listed in tuition PDF as graduate, 42 credits, $380 per credit

General/fixed fees discovered include registration fee, student ID card, insurance, NSSF if applicable, application fee, and entrance exam fee.

## Scholarships / financial aid discovered

- Office of Social Affairs financial aid overview was found.
- Office of Social Affairs rules state financial aid and scholarships are available only to undergraduate students.
- International scholarship pages include external opportunities for Master and Doctorate/PhD mobility/scholarship calls.

Graduate extraction implication: do not assume institutional graduate financial aid from the Office of Social Affairs pages. International/external scholarship opportunities should be modeled separately if needed.

## Academic calendar discovered

The academic calendar page provides 2025-26 registration, online enrollment, add/drop, payment, withdrawal, exam, and result-display dates.

## Language requirements / support discovered

Language Center sources were found for:

- Placement tests
- English/French remedial courses
- IELTS services
- DELF-related services
- Language support and certificates

Program-specific entrance requirements also list French tests or English tests for some graduate programs/departments.

## International students / mobility discovered

International pages confirm UA internationalization activity, mobility opportunities, international scholarships, and that UA hosts international students. These sources are useful context but not enough alone for admissions extraction unless later focused on international-applicant requirements.

## Missing categories / gaps

- No normal UA PhD / Doctor of Philosophy program page found.
- No dedicated graduate catalog PDF was conclusively identified beyond the admissions guide and application form.
- Some program detail pages should still be opened individually during the later `programs.json` extraction phase to capture exact credits, duration, language, campus, price, objectives, PLOs, and downloadable program structure PDFs.
- Some dynamic pages contain `Loading...` placeholders; detail extraction may need direct browser inspection per program page.
- No graduate-specific financial-aid policy was found; the Social Affairs rules indicate undergraduate-only aid/scholarships.

## Browser limitations

- The UA site emits repeated navigation/menu text and dynamic placeholders in the browser text extraction.
- Container-level network access was unavailable, so recursive crawling could not be automated from the shell. Discovery was performed through browser/web inspection and official search results.
- Repository root was not available in this runtime, so the requested files were created under `/mnt/data/research/ua`.
- Compile validation could not be run because no repository root/build files were available.

## Validation

Completed locally:

- `sources.json` parses as valid JSON.
- No duplicate `source_id` values.
- No duplicate URLs.
- Every recorded URL host is `ua.edu.lb` or an official UA subdomain.

Not completed:

- Repository compile/build validation: repository root not available.

## Recommendation

Proceed to a later UA inventory extraction for Master-level programs only.

For PhD, keep status as `not confirmed` / `defer` until a focused official-source pass proves UA has normal PhD program pages with program titles and admissions requirements. Current official evidence supports doctoral co-supervision/research scholarship activity, not extractable UA PhD program records.
