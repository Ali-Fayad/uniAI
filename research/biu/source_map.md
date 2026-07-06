# BIU Discovery Source Map

Task code: `BROWSER_BIU_DISCOVERY_001`

Official website inspected: <https://www.biu.edu.lb>

## Discovery objective

Determine whether Beirut Islamic University (BIU) currently provides official evidence for graduate programs, specifically Master's and PhD/Doctorate levels. This is discovery only. No `programs.json`, `university.json`, migrations, or seed files were created.

## Official source coverage

| Source ID | URL | Area inspected | Graduate relevance | Notes |
|---|---|---|---|---|
| `BIU_OFFICIAL_HOME` | <https://www.biu.edu.lb/> | Homepage/navigation | MASTER, PHD links visible | Navigation exposes `الماجستير`, `الدكتوراه`, admissions, fees, thesis/dissertation pages. |
| `BIU_MASTERS_REQUIREMENTS` | <https://biu.edu.lb/pages/majors/masters/requirements> | Master's requirements | MASTER evidence | Official page for `متطلبات التسجيل لمرحلة الماجستير`; describes specializations, duration, thesis, and admission conditions. |
| `BIU_DOCTORATE_REQUIREMENTS` | <https://biu.edu.lb/pages/majors/doctorat/requirements> | Doctorate requirements | PHD evidence | Official page for `مرحلة العالمية الدكتوراه`; describes doctorate study, dissertation, publications, and admission conditions. |
| `BIU_ADMISSION_REQUIREMENTS` | <https://biu.edu.lb/pages/admission/requirements> | Required documents | MASTER, PHD evidence | Lists required documents for Master's, preparatory Master's, and Doctorate. |
| `BIU_APPLICATIONS` | <https://biu.edu.lb/pages/admission/applications> | Application forms | MASTER, PHD evidence | Links application forms for Master's, preparatory Master's, and Doctorate. |
| `BIU_SYLLABUS` | <https://biu.edu.lb/pages/academics/syllabus> | Course descriptions index | MASTER evidence | Postgraduate section lists graduate tracks including Islamic Studies, Comparative Fiqh, Sharia Judiciary Master's, and preparatory Master's. |
| `BIU_PREP_MASTERS_BOOKS` | <https://www.biu.edu.lb/pages/books/prepdirasat> | Book list | MASTER support | 2025-2026 preparatory Master's materials for Islamic Studies. |
| `BIU_POSTGRAD_GUIDANCE` | <https://biu.edu.lb/pages/news/mdinfo> | Postgraduate research guidance | MASTER, PHD support | Guidance for Master's and Doctorate research plans, thesis/dissertation form, and references. |
| `BIU_ACCOMPLISHMENTS_RECOGNITION` | <https://biu.edu.lb/pages/about/accomplishment> | Recognition/history | MASTER, PHD support | States 2006 recognition to grant Master's and Doctorate specializations. |
| `BIU_FEES` | <https://biu.edu.lb/pages/admission/fees> | Tuition/fees | Tuition support | Annual fees page for 1447 / 2025-2026. Fee details are image-rendered in captured text. |
| `BIU_SCHEDULE` | <https://biu.edu.lb/pages/academics/schedule> | Lecture schedule | Schedule support | Official page exists, but captured text has no detailed active schedule. |
| `BIU_EXAMS` | <https://biu.edu.lb/pages/academics/examList> | Exam schedule | Exam support | Official page exists; captured text was incomplete for detailed exam data. |
| `BIU_INTERNAL_REGULATIONS` | <https://biu.edu.lb/pages/administration/internal> | Internal regulations | Regulations support | Included in recursive inspection for graduate regulation coverage. |

## Explicit exclusions

The following were treated as out of scope and were not used as official graduate program evidence:

- News/events and thesis-defense announcement pages, except where used only as navigation context.
- Faculty profiles, staff pages, social media, external aggregators, and unrelated BIU domains.
- Non-BIU domains such as `biuinternational.com`, `biu.us`, `biu.ac.il`, `biu.edu.ng`, and third-party university directories.
- Google Drive files linked from BIU pages were noted only as official-linked supporting documents, but they were not included in `sources.json` because validation requested official BIU URLs only.

## Crawl notes

- The official site uses Arabic content and many pages have repeated navigation/footer blocks.
- Some useful fee/schedule details appear to be images or externally hosted files, so the captured text is limited for those areas.
- Graduate evidence is not inferred from thesis listings or announcements; it is grounded in official requirements, admission, application, syllabus, and recognition pages.
