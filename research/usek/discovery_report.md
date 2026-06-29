# USEK Discovery Report

## Verdict

APPROVE WITH NOTES

## What Was Confirmed

- USEK offers master’s programs.
- USEK offers doctorate / PhD programs.
- Graduate admissions are officially exposed.
- Graduate tuition and fees are officially exposed.
- Scholarships and financial aid are officially exposed.
- Academic calendar is officially exposed.
- University catalogue is officially exposed.
- Doctoral studies are officially exposed through a dedicated Doctoral College and doctoral admissions page.

## Source Count

- Official source URLs found: 31

## Graduate-Offering Units Surfaced

- Business School
- School of Engineering
- School of Architecture and Design
- School of Law and Political Sciences
- School of Medicine and Medical Sciences
- Pontifical School of Theology
- Faculty of Arts and Sciences
- Doctoral College
- Higher Institute of Nursing Sciences was surfaced indirectly through graduate-fee references, but the graduate program page was not canonicalized in this pass.

## Program Pages Discovered

- Confirmed graduate program pages: 10
- Out-of-scope program pages surfaced: 2
- Total program-like pages surfaced: 12

## Source Categories Found

- Home / university hub
- Academics hub
- University catalogue
- Graduate admissions requirements
- Graduate admissions deadlines
- Doctoral admissions
- Admissions guide PDF
- Academic calendar PDF
- Graduate tuition / fees
- Scholarships and financial aid
- School/faculty pages
- Individual graduate program pages
- Doctoral college hub
- Out-of-scope diploma pages

## Missing Source Categories

- Standalone graduate handbook
- Standalone graduate scholarship / assistantship hub
- Dedicated graduate tuition PDF
- Canonical English-only graduate catalog landing page
- A single flat index of all graduate program pages

## Risks / Notes for Next Phase

- Several graduate programs are nested deep in faculty/department URLs with query parameters.
- USEK mixes English and French content on the same site, so later extraction should preserve official page language and title wording.
- Out-of-scope diploma pages are easy to stumble into from the faculty navigation.
- Doctoral requirements and doctoral program lists are split between admissions and Doctoral College surfaces.
- Tuition is centralized on the graduate-fees page, which is helpful, but later work should still check for any program-specific exceptions.

## Validation

- sources.json parses successfully.
- No duplicate source IDs.
- No duplicate source URLs.
- All URLs are official USEK URLs.
- Compile check passed (`./mvnw -q -DskipTests compile` in `Server/`).
