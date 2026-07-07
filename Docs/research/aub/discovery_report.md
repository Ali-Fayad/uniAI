# AUB Graduate Discovery Report

Date accessed: 2026-06-26

## Scope

Official AUB sources only. No database writes, no migrations, no backend code.

## Discovered Structure

- Faculties checked: 5 of 7 required categories
- Graduate-affiliated departments/program units inspected: 13
- Master’s offerings captured: 20
- PhD offerings captured: 5
- Total graduate offerings captured: 25

## What Was Captured

- University-level admissions entry points
- Graduate English/RUSE requirement policy
- Financial aid and scholarship landing pages
- Graduate Council overview and assistantship guidance
- Registrar and student tuition/payment entry points
- Graduate offerings visible from:
  - Faculty of Arts and Sciences
  - Faculty of Health Sciences
  - Maroun Semaan Faculty of Engineering and Architecture
  - Suliman S. Olayan School of Business

## Programs By Faculty

### Faculty of Arts and Sciences

- Graduate Program in Computational Science (Master's Degree in Computational Science)

### Faculty of Health Sciences

- Graduate Public Health Program

### Maroun Semaan Faculty of Engineering and Architecture

- Biomedical Engineering Program, M.S. and Ph.D.
- Civil and Environmental Engineering, M.E. and Ph.D.
- Chemical Engineering, M.E.
- Electrical and Computer Engineering, M.E. and Ph.D.
- Industrial Engineering and Management, M.E.
- Mechanical Engineering, M.E. and Ph.D.
- School of Architecture and Design, urban planning / urban design / environmental sciences graduate offerings

### Suliman S. Olayan School of Business

- Master's in Human Resource Management
- Master's of Science in Business Analytics
- Master's in Finance
- MBA Online

### Faculty of Agricultural and Food Sciences

- No master's or PhD program page could be verified in this pass from the public HTML that was accessible here.

## Missing Information

- Many program pages do not expose credits, duration, thesis/non-thesis status, or detailed admission document lists in the publicly visible HTML.
- Tuition for most graduate offerings was not published on the inspected program pages.
- Program-specific deadlines were not fully exposed in the pages inspected here.
- Payment-plan details were not fully exposed for graduate programs.

## Ambiguous Information

- The Graduate Public Health Program page does not render the official degree title in the visible text, so the degree name was left null.
- The Mechanical Engineering page uses mixed wording for the dual degree and the Applied Energy option; the records reflect the visible wording and are marked where needed.
- The School of Architecture and Design page presents the urban planning and urban design degrees together; they were split into separate records for database readiness.
- The FAS computational science page renders a master’s title and track list clearly, but it is not enough to confirm every related FAS graduate degree page.

## Broken Or Gated Links

- MSFEA graduate program links for some pages redirected to AUB sign-in when clicked from this session:
  - `MSFEA-Graduate-Programs.aspx`
  - `MFSEA-PhD-Programs.aspx`
- Several faculty pages are JS-heavy and only exposed partial content in the browser tool.
- Direct access to the Medicine and Nursing faculty program pages could not be verified in this session:
  - `https://www.aub.edu.lb/medicine/Pages/default.aspx`
  - `https://www.aub.edu.lb/nursing/Pages/default.aspx`

## Recommendations Before Database Import

1. Manually verify FAS and FAFS graduate offerings from a view that renders their hidden program content or from official PDFs/brochures.
2. Confirm the official degree name for the Faculty of Health Sciences Graduate Public Health Program.
3. Recheck program-level credits, duration, tuition, deadlines, and required documents from brochures or current catalogs before import.
4. Treat the current dataset as a verified foundation, not a final complete catalog.
5. If the database should store only master’s and PhD offerings, keep graduate diploma entries out of the first import.

## Notes

- All values in `programs.json` are either directly visible on official AUB pages, or explicitly marked as inferred/unknown in the record notes.
- No third-party sources were used.
- Confidence level: PARTIAL
