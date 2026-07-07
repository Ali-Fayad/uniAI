# AUB Graduate Data Model Design

## Goal
Design a PostgreSQL schema that can store the deduplicated AUB graduate inventory and remain usable for other universities without reworking the model.

## Design Summary
The canonical import model uses new `graduate_*` tables for programs and program facts, while the older graduate tables created in `V23__create_graduate_program_schema.sql` are kept as legacy compatibility tables.

### Keep
- `university`
- `degree_type`
- `language`
- `source`
- Legacy graduate tables from V23 remain untouched for compatibility:
  - `major_category`
  - `major`
  - `major_degree`
  - `tuition_fee`
  - `fee_item`
  - `admission_requirement`
  - `required_document`
  - `admission_deadline`
  - `scholarship`
  - `financial_aid`
  - `payment_plan`
  - `accreditation`

### Replace for the canonical import path
Use new `graduate_*` tables instead of the legacy graduate tables for all new program imports.

## Core Tables

### `university_faculty`
Represents a faculty, school, or interfaculty academic unit.
- `faculty_type` distinguishes `FACULTY`, `SCHOOL`, `INTERFACULTY`, `INSTITUTE`, `CENTER`, and `OTHER`.
- One university can have many faculties/schools.

### `university_department`
Represents a department or unit within a faculty.
- Departments are optional because some programs are school-wide or interfaculty.

### `graduate_program`
Represents one canonical graduate program.
- Stores the imported program identity in `program_key`.
- Stores `faculty`, `department`, `major_category`, `major`, `degree_type`, `official_degree_name`, thesis status, credits, duration, language, delivery mode, and description.
- Holds one primary source via `source_id`.
- Uses `official_program_url` plus `program_key` to prevent duplicates.
- Cross-listed and interfaculty records are stored once and linked via relationship/alias tables.

### `graduate_program_track`
Stores concentrations, tracks, pathways, and specializations.
- This is the normalized destination for `concentrations_or_tracks`.
- Tracks remain optional and can be multiple per program.

### `graduate_program_alias`
Stores alternate IDs or historical IDs.
- This is where old duplicate IDs such as `fas-gpcs` are preserved.
- Useful for imports, deduplication, and future chatbot lookup.

### `graduate_program_source`
Many-to-many link between a program and its sources.
- The program row itself has a primary `source_id`.
- This table stores additional source links and source roles such as `PROGRAM_PAGE`, `TUITION`, `ADMISSIONS`, `PDF`, and `CATALOG`.

### `graduate_program_relationship`
Stores explicit cross-listing or interfaculty relationships.
- Example values: `CROSS_LISTED`, `INTERFACULTY`, `LEGACY_DUPLICATE`, `ALIAS_OF`.
- This supports future cross-listed program questions without duplicating rows.

## Fact Tables
All major fact tables use a consistent pattern:
- `university_id`
- optional `faculty_id`, `department_id`, `program_id`
- `scope_level` with values like `UNIVERSITY`, `FACULTY`, `DEPARTMENT`, `PROGRAM`
- `source_id` for traceability
- `notes`
- `record_key` for duplicate prevention where natural keys may be incomplete

### `graduate_tuition_rate`
Stores structured tuition schedules.
- Supports academic year, currency, billing basis, amount, and category.
- Supports both generic faculty tuition and program-specific overrides.
- Supports semester-based and per-credit billing.

### `graduate_fee_item`
Stores shared university fees and program-specific fees.
- Covers items like deposit, activity fee, technology fee, late registration, and program-specific fees.
- Can represent university-wide, faculty-level, department-level, or program-level fees.

### `graduate_admission_requirement`
Stores GRE, GMAT, English, portfolio, interview, experience, and other admission requirements.
- Uses a controlled `requirement_type` check.
- Can carry threshold values when available.

### `graduate_required_document`
Stores admission document requirements.
- This is the structured destination for `required_documents`.

### `graduate_admission_deadline`
Stores program, faculty, or university deadlines.
- Supports a term, academic year, and exact deadline date where available.

### `graduate_scholarship`
Stores scholarship opportunities.

### `graduate_financial_aid`
Stores financial aid options.

### `graduate_payment_plan`
Stores payment plan options.

### `graduate_accreditation`
Stores accreditation or recognition statements tied to a program or faculty.

## Field Mapping From AUB JSON
- `id` -> `graduate_program.program_key`
- `faculty` -> `university_faculty.name`
- `department` -> `university_department.name`
- `major_category` -> `graduate_program.major_category`
- `major` -> `graduate_program.major`
- `degree_type` -> `degree_type.code` via `graduate_program.degree_type_id`
- `official_degree_name` -> `graduate_program.official_degree_name`
- `thesis_or_non_thesis` -> `graduate_program.thesis_or_non_thesis`
- `concentrations_or_tracks` -> `graduate_program_track`
- `credits` -> `graduate_program.credits`
- `duration` -> `graduate_program.duration_value` + `graduate_program.duration_unit`
- `language` -> `graduate_program.primary_language_id`
- `delivery_mode` -> `graduate_program.delivery_mode`
- `program_description` -> `graduate_program.program_description`
- `admission_requirements` -> `graduate_admission_requirement`
- `required_documents` -> `graduate_required_document`
- `gre_requirement`, `gmat_requirement`, `english_requirement`, `portfolio_requirement`, `interview_requirement`, `experience_requirement` -> `graduate_admission_requirement`
- `tuition` -> `graduate_tuition_rate`
- `additional_fees` -> `graduate_fee_item`
- `deadlines` -> `graduate_admission_deadline`
- `scholarships` -> `graduate_scholarship`
- `financial_aid` -> `graduate_financial_aid`
- `payment_plans` -> `graduate_payment_plan`
- `accreditation` -> `graduate_accreditation`
- `official_program_url` -> `graduate_program.official_program_url`
- `sources` -> `source` + `graduate_program_source`
- `notes` -> `graduate_program.notes` and the relevant fact-table notes

## Constraints And Indexing
- Unique constraints prevent duplicate faculties, departments, programs, aliases, tracks, and source links.
- Check constraints validate:
  - `delivery_mode`
  - `thesis_or_non_thesis`
  - `requirement_type`
  - `billing_basis`
  - `scope_level`
  - `relation_type`
  - `track_type`
- Foreign key indexes are added everywhere.
- `source.url` remains the canonical lookup key for official pages.

## Why Not Reuse The Legacy Graduate Tables Directly
The legacy tables are useful as an older normalized shape, but they do not model:
- faculties/schools explicitly
- department-level scoping
- cross-listed/interfaculty relationships
- canonical program aliases
- structured tuition scopes cleanly
- source traceability per program and per fact

Keeping them avoids breaking compatibility, while the new `graduate_*` tables provide the canonical import path for AUB and future universities.
