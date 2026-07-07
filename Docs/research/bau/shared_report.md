# BAU Shared Graduate Data Report

## Normalization Status
Shared graduate information has been centralized in `university.json`. Program records no longer repeat the generic graduate admission sentence; program-level `admission_requirements` remain `null` unless a program-specific value is later verified from an official BAU source.

## Centralized Fields
- Admissions criteria and faculty-specific admission notes.
- Required documents for Master's, Diploma, and PhD admissions.
- Language proficiency requirements and minimum scores.
- Current published graduate admissions cycle deadlines.
- Graduate scholarships and financial aid links.
- Regulations, postgraduate bylaws, accreditation link, international student note, and graduate academic calendar link.
- Graduate tuition global notes and recurring healthcare/technology fees.

## Official Sources Used
- `bau_grad_admissions`
- `bau_grad_admission_requirements`
- `bau_grad_applying`
- `bau_grad_dates`
- `bau_grad_tuition_pdf`
- `bau_grad_bylaws_pdf`
- `bau_grad_program_scholarships`
- `bau_grad_merit_scholarships`
- `bau_grad_scholarships_awards`

## Remaining Shared Gaps
- BAU payment plan details were not explicitly identified in the reviewed official graduate sources, so `payment_plans` remains `null`.
- Program-level admissions exceptions remain outside program records unless explicitly published on dedicated program pages.
