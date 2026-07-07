# LAU Graduate Import Report

## Files Changed

- `Server/src/main/resources/db/migration/V26__seed_lau_graduate_data.sql`
- `research/lau/import_report.md`

## Seed Mechanism

- Flyway SQL migration using `DO $$ ... $$` and `jsonb_to_recordset` temp tables.
- Idempotent keys:
  - `university.name`
  - `degree_type.code`
  - `source.university_id + url`
  - `university_faculty.university_id + name`
  - `university_department.university_id + faculty_id + name`
  - `graduate_program.university_id + program_key`
  - `graduate_program_source.program_id + source_id + source_role`
  - `graduate_tuition_rate.university_id + record_key`
  - `graduate_admission_requirement.university_id + record_key`
  - `graduate_program_track.program_id + track_type + track_name`
  - `graduate_accreditation.university_id + record_key`

## Expected Row Counts

- University records inserted or reused: 1
- Faculties/schools: 5
- Departments: 5
- Degree types seeded: 2
- Graduate programs: 33
- Program-source links: 131
- Admission requirements: 36
- Tuition rows: 33
- Tracks/concentrations: 28
- Accreditation rows: 10
- Source records: 56
- Skipped out-of-scope records: 10

## Validation Result

- JSON parse validation: passed for `research/lau/programs.json`, `research/lau/sources.json`, `research/lau/university.json`, and `research/lau/out_of_scope_programs.json`.
- In-scope programs verified: 33 master's records.
- PhD records imported: 0.
- Out-of-scope graduate-program inserts: 0.
- Maven compile: passed with `./mvnw -q -DskipTests compile`.
- Local Flyway run: not executed because the local Docker datasource was unavailable in this environment.

## Warnings / Manual Review

- Duration text is preserved in `graduate_program.notes` because the canonical graduate schema does not expose a dedicated raw-duration text field.
- No separate LAU doctoral records were present in the in-scope program JSON.
- Out-of-scope certificates/professional degrees were intentionally excluded from `graduate_program`.
- The local Docker daemon was not available, so database-level execution could not be verified here.
