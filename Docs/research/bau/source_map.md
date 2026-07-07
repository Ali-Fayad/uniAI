# BAU Graduate Source Map

## Crawl seed
- https://www.bau.edu.lb
- https://www.bau.edu.lb/Graduate/Programs
- https://www.bau.edu.lb/Admissions/GraduateStudents

## Official BAU graduate source categories
- Graduate program index and faculty graduate listings
- Graduate admissions, applying, dates/deadlines
- Graduate degree requirements and registration information
- Tuition fees PDF
- Graduate brochures PDFs
- Postgraduate bylaws PDF
- Scholarships and awards pages

## Fetch limitation
The official BAU site and PDFs were intermittently returning 400/502/timeouts through the fetch layer. This package therefore preserves discovered official URLs and uses only parsed official snippets/page context. Fields that require full program-page/PDF parsing are left null.
