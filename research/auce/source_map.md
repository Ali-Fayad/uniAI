# AUCE Source Map

Task code: `BROWSER_AUCE_DISCOVERY_001`
Official website: <https://www.auce.edu.lb>

## Discovery scope

This pass inspected official AUCE web pages only. The discovery goal was to determine whether AUCE currently has official graduate program evidence for MASTER or PHD programs. Bachelor, diploma, certificate, continuing education, professional training, news, events, faculty profiles, partnerships, and research-center references were treated as out of scope unless they linked to official Master's or PhD program evidence.

## Official pages inspected

| Source ID | URL | Purpose | Discovery result |
|---|---|---|---|
| `auce-home` | https://www.auce.edu.lb/ | Homepage/navigation and initial crawl seed | Navigation lists MBA Program and Master of Computer Science under Graduate; used as navigation signal. |
| `auce-academics-programs` | https://auce.edu.lb/fees-and-tuitions/index.php?page=academics | Academic programs / graduate program evidence | Officially lists Master of Business Administration and Master of Computer Science as graduate programs. |
| `auce-admissions` | https://auce.edu.lb/index.php?page=admissions | Graduate admissions, requirements, documents, aid | Officially names Masters Programs admission requirements for MBA and Master of Computer Science applicants. |
| `auce-contact-faq` | https://auce.edu.lb/index.php?page=contact | FAQ / language requirement confirmation | FAQ confirms graduate English requirement: TOEFL 97+ or IELTS 7.0+. |
| `auce-arts-sciences` | https://auce.edu.lb/departements/masters-of-science/index.php?page=faculty-arts-sciences | Arts & Sciences context | Contains undergraduate major content and navigation to Master of Computer Science; not standalone graduate evidence. |
| `auce-business-faculty` | https://auce.edu.lb/4011/index.php?page=faculty-business | Business faculty context | Undergraduate faculty page; not standalone graduate evidence. |
| `auce-campuses` | https://auce.edu.lb/departements/masters-of-science/index.php?page=campuses | Campus context | States Beirut has full range of undergraduate and graduate programs; not standalone program evidence. |
| `auce-about-accreditation` | https://www.auce.edu.lb/index.php?page=about | Institution context/licensing | States AUCE licensing/recognition; footer lists MBA and MCS navigation links. |

## Evidence classification

### Accepted MASTER evidence

Accepted because the evidence comes from official AUCE academic/admissions pages that explicitly name Master's-level programs or Master's admission requirements:

- Master of Business Administration (MBA)
- Master of Computer Science (MCS)

### Rejected / non-counting evidence

The following were not counted as standalone graduate program evidence:

- Homepage/faculty cards saying faculties are "Undergraduate & Graduate" without full program/admission detail.
- Campus page statements such as "full range of undergraduate and graduate programs" without naming a Master's or PhD program.
- Research and international partnership pages, because they do not establish official graduate programs.
- Old event/footer references to "Post Graduate Programs," because events/news/footer navigation are out of scope and not current program evidence.

## Search coverage notes

Inspected official AUCE navigation and searched official AUCE pages for: graduate programs, Masters/Master, MBA, Master of Computer Science, MCS, PhD, Doctorate, graduate admissions, tuition/fees, scholarships, financial aid, required documents, language requirements, academic calendar, and graduate regulations.

No official AUCE PhD/Doctorate program page, graduate catalog, or PhD admissions page was found during this pass.

No official graduate tuition amount page was found. The URL path `fees-and-tuitions/index.php?page=academics` currently renders an Academic Programs page, not detailed tuition tables.

No official graduate academic calendar or graduate regulations page was found during this pass.
