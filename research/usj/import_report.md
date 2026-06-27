# USJ Graduate Import Report

## Files Changed

- `Server/src/main/resources/db/migration/V27__seed_usj_graduate_data.sql`
- `research/usj/import_report.md`

## Seed Mechanism

- Flyway SQL migration using `DO $$ ... $$` and `jsonb_to_recordset` temp tables.
- Idempotent keys:
  - `university.name`
  - `degree_type.code`
  - `language.code`
  - `source.university_id + url`
  - `university_faculty.university_id + name`
  - `university_department.university_id + faculty_id + name`
  - `graduate_program.university_id + program_key`
  - `graduate_program_source.program_id + source_id + source_role`
  - `graduate_admission_requirement.university_id + record_key`
  - `graduate_accreditation.university_id + record_key`
- Schema adjustment included in this migration: the unique constraint on `graduate_program.official_program_url` is dropped so USJ hub/catalog URLs can be reused across multiple official programs.

## Expected Row Counts

- University records inserted or reused: 1
- Faculties/schools/institutes: 10
- Departments: 7
- Degree types seeded: 2
- Languages seeded: 4
- Graduate programs: 51
- Program-source links: 108
- Program-specific admission requirements: 5
- Tuition rows: 0
- Tracks/concentrations: 0
- Accreditation rows: 1
- Source records: 31
- Skipped out-of-scope records: 5

## Validation Result

- JSON parse validation: passed for `research/usj/programs.json`, `research/usj/sources.json`, `research/usj/university.json`, and `research/usj/out_of_scope_programs.json`.
- In-scope programs verified: 47 master's records and 4 doctorate records.
- Out-of-scope graduate-program inserts: 0.
- Hub/catalog URL reuse: intentional and preserved for programs that only expose a shared official listing page.
- Maven compile: passed (`./mvnw -q -DskipTests compile` in `Server/`).
- Local Flyway run: not executed because the local datasource is unavailable in this environment.

## Warnings / Manual Review

- Tuition amounts were not seeded because no official numeric graduate tuition schedule was published in the reviewed USJ sources.
- Most program fields remain null by design and were not invented.
- The migration intentionally preserves French official titles and hub/catalog URLs where USJ only exposed shared graduate listings.
- The current environment does not expose a local datasource, so database execution could not be verified here.
