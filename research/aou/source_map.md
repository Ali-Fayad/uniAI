# AOU Lebanon Source Map

Task code: AOU_DISCOVERY_001_BROWSER_INV  
Date accessed: 2026-07-05  
Scope: MASTER and PHD only. Bachelor, diplomas, certificates, continuing education, training, professional programs, news, and events are out of scope.

## Official domain note

The user supplied `https://www.aou.edu.lb`, but the active official Lebanon website resolved and was crawled under `https://web.aou.edu.lb`. All retained sources are on `*.aou.edu.lb` and therefore satisfy the official AOU domain constraint. External navigation links such as Open University, Google Maps, Outlook, LMS, SIS, alumni, and other AOU regional/shared systems were discovered but not retained as source records unless they were on `aou.edu.lb`.

## Discovery routes inspected

### Root / navigation

- `AOU_SRC_001` — Arab Open University - Lebanon Home — https://web.aou.edu.lb/
  - Used to confirm official navigation structure: About, Academics, Admission, Students, Research, Centers, Giving, and quick links.

### Admission / graduate hub

- `AOU_SRC_002` — Graduate Programs — https://web.aou.edu.lb/admission/pages/graduate-programs.aspx
  - Central graduate listing.
  - In-scope master's programs found:
    - Masters in Business Administration
    - Masters in Business Administration (Finance)
    - Masters in Business Administration (HRM)
    - MSc in Computing (Cyber Security and Forensics)
    - MA in Teaching English as a Foreign Language (TEFL) - Thesis Track
  - Out-of-scope diplomas found under Faculty of Education:
    - Teaching Diploma in Education
    - Teaching Diploma in Physical Education and Sports
    - Teaching Diploma in Special Education - Learning Difficulties

- `AOU_SRC_003` — Graduate Requirements — https://web.aou.edu.lb/admission/pages/graduate-requirements.aspx
  - Used for graduate admission requirements, English placement/IELTS/TOEFL exemptions, GEE/GMAT exemption, MSc background rules, MA TEFL language requirements, and required documents.

- `AOU_SRC_004` — Admission Fees - Graduate — https://web.aou.edu.lb/admission/pages/graduate-fees.aspx
  - Used for graduate application fee, registration fee, English placement fee, MBA GEE fee, AGFUND support note, and installment payment statement.

- `AOU_SRC_005` — Apply - Graduate — https://web.aou.edu.lb/admission/pages/graduate-apply.aspx
  - Used to cross-check MBA, MSc, and MA TEFL admission requirements and application links.

- `AOU_SRC_006` — Graduate FAQ — https://web.aou.edu.lb/admission/pages/graduate-faq.aspx?list=faq_graduate
  - Used to cross-check available graduate majors, degree recognition, dual-degree note, credit totals for MBA/MSc, installment schedule, and class timing.

- `AOU_SRC_007` — Admission Procedure — https://web.aou.edu.lb/admission/Pages/default.aspx
  - Used for general admission workflow and postgraduate acceptance/payment process.

### Business graduate programs

- `AOU_SRC_008` — Faculty of Business Studies - Postgraduate Programs — https://web.aou.edu.lb/faculties/business/Pages/postgraduate-programs.aspx
  - Listing page for MBA General, MBA Finance, MBA HRM.

- `AOU_SRC_009` — Masters in Business Administration — https://web.aou.edu.lb/faculties/business/Pages/program-details.aspx?degree=2&iid=4
  - Program detail page for MBA General.
  - Used for overview, admission requirements, duration, delivery, academic plan, 48 credit total, tuition at USD 170/credit, operational fees, registration fees, NSSF note, installments, and refunds.

- `AOU_SRC_010` — Masters in Business Administration (Finance) — https://web.aou.edu.lb/faculties/business/Pages/program-details.aspx?degree=2&iid=5
  - Program detail page for MBA Finance.

- `AOU_SRC_011` — Masters in Business Administration (HRM) — https://web.aou.edu.lb/faculties/business/Pages/program-details.aspx?degree=2&iid=6
  - Program detail page for MBA HRM.

### Computer graduate programs

- `AOU_SRC_012` — Faculty of Computer Studies - Postgraduate Programs — https://web.aou.edu.lb/faculties/computer/Pages/postgraduate-programs.aspx
  - Listing page for MSc in Computing (Cyber Security and Forensics).
  - Also exposes MSc Programme Specification and MSc Student Handbook PDF links.

- `AOU_SRC_013` — MSc in Computing (Cyber Security and Forensics) — https://web.aou.edu.lb/faculties/computer/Pages/program-details.aspx?degree=2&iid=7
  - Program detail page for MSc Computing.

### Language graduate programs

- `AOU_SRC_014` — Faculty of Language Studies — https://web.aou.edu.lb/faculties/language/Pages/default.aspx
  - Faculty page describing MA in TEFL duration, blended/full-time delivery, staged postgraduate awards, and dual degree structure.

- `AOU_SRC_015` — Faculty of Language Studies - Postgraduate Programs — https://web.aou.edu.lb/faculties/language/Pages/postgraduate-programs.aspx
  - Listing page for MA in Teaching English as a Foreign Language (TEFL) - Thesis Track.
  - Also exposes MA in TEFL Programme Specification and Student Handbook links.

- `AOU_SRC_016` — MA in Teaching English as a Foreign Language (TEFL) - Thesis Track — https://web.aou.edu.lb/faculties/language/Pages/program-details.aspx?degree=2&iid=17
  - Program detail page for MA TEFL.
  - Used for overview, requirements, delivery mode, academic plan, tuition/fees, installments, refund notes.

### Calendar / academic year / fees

- `AOU_SRC_017` — Academic Calendar — https://web.aou.edu.lb/students/pages/academic-calendar.aspx
  - Official academic calendar location. Crawl showed limited content, but link retained because it is the canonical calendar page.

- `AOU_SRC_018` — Academic Year — https://web.aou.edu.lb/students/guide/Pages/academic-year.aspx
  - Student guide explanation of semester and optional summer term structure.

- `AOU_SRC_019` — Fees - Student Guide — https://web.aou.edu.lb/students/guide/Pages/fees.aspx
  - Cross-check for graduate application/registration/placement/GEE fees.

- `AOU_SRC_020` — AOU Lebanon Student's Guide — https://web.aou.edu.lb/students/guide
  - Student guide root linked from program pages and student navigation.

## Official PDFs discovered

The official postgraduate listing pages expose PDF links for OU-validated programs. The crawl exposed titles but not all resolved PDF URLs in a stable way. Keep these as official PDF leads for a later import pass:

- MBA Programme Specification 2021-2026 (Lebanon)
- MBA Student Handbook 2021-2026 (Lebanon)
- MSC Programme Specification-Lebanon.pdf
- MSC Student Handbook -Lebanon.pdf
- MA in TEFL Programme Specification
- MA in TEFL Student Handbook

## Excluded / out-of-scope findings

- Bachelor programs under Business, Computer, Language, Education, and Graphic/Multimedia were discovered through navigation but excluded.
- Faculty of Education graduate diplomas were discovered but excluded because diplomas are out of scope.
- News, events, announcements, continuing education center pages, media pages, career pages, and student life pages were discovered but excluded.
- No PhD / Doctorate programs were found on the official AOU Lebanon pages inspected.
