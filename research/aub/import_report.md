# AUB Graduate Import Report

## Import Mechanism

Chosen mechanism: Flyway seed migration.

Reason:
- The AUB graduate dataset is static candidate data with a canonical merged JSON source.
- A Flyway seed migration is repeatable, source-controlled, and idempotent.
- It fits future university imports better than a one-off importer because the same pattern can be reused with new seed migrations and the same canonical schema.

Seed artifact:
- `Server/src/main/resources/db/migration/V25__seed_aub_graduate_data.sql`

Schema adjustment carried forward for import readiness:
- `Server/src/main/resources/db/migration/V24__graduate_program_schema_v2.sql`
- `official_degree_name` is nullable so programs like `fhs-gphp` can be imported without inventing missing values.

## Files Changed

- `Server/src/main/resources/db/migration/V24__graduate_program_schema_v2.sql`
- `Server/src/main/resources/db/migration/V25__seed_aub_graduate_data.sql`
- `research/aub/import_report.md`

## Validation

- All `research/aub/*.json` files parse successfully.
- `./mvnw -q -DskipTests compile` passed in `Server/`.
- Flyway migration was not executed against a live database because no datasource is configured in this workspace.

## Expected Row Counts

From `research/aub/programs_merged_candidate.json` and the embedded seed payload in `V25`:

- Programs: 62
- Faculties / schools: 6
- Departments: 33
- Sources: 69
- Tuition rates: 62
- Tracks / concentrations: 34
- Admission requirement rows: 46
- Accreditation rows: 1
- Alias rows: 1
- Program-source links: 151

Program-source links break down as:
- 88 links from program source references
- 63 links from tuition source references

## Actual Generated / Inserted Counts

Because the database was not reachable, no rows were physically inserted.

Generated seed counts in `V25`:
- Source seed rows: 69
- Faculty seed rows: 6
- Department seed rows: 33
- Program seed rows: 62

Planned upserts / inserts:
- `university`: 1
- `degree_type`: 4
- `language`: 1
- `source`: 69
- `university_faculty`: 6
- `university_department`: 33
- `graduate_program`: 62
- `graduate_program_alias`: 1
- `graduate_program_track`: 34
- `graduate_tuition_rate`: 62
- `graduate_admission_requirement`: 46
- `graduate_accreditation`: 1
- `graduate_program_source`: 151

## Programs Imported

All 62 canonical AUB graduate programs in `programs_merged_candidate.json` are covered by the seed migration.

Faculty breakdown:
- Faculty of Arts and Sciences: 26
- Faculty of Health Sciences: 4
- Faculty of Medicine: 7
- Maroun Semaan Faculty of Engineering and Architecture: 19
- Rafic Hariri School of Nursing: 2
- Suliman S. Olayan School of Business: 4

Degree breakdown:
- Master: 51
- PhD: 11

## Sources Imported

The seed migration loads 69 canonical source rows and then links them back to programs.

Source traceability preserved:
- Every program has at least one source link.
- Tuition rows carry `source_id` support through `graduate_program_source`.
- `official_program_url` remains stored on the program row.

## Tuition Rates Imported

All 62 programs have a structured tuition object and are seeded into `graduate_tuition_rate`.

Observed tuition mapping:
- Generic faculty tuition for most programs
- Program-specific overrides where needed, including:
  - `MBA Online`
  - `Executive Master in Healthcare Leadership`
  - `Master of Public Health`
  - `Master of Science in Environmental Sciences`
  - `Nursing Administration and Management` track context preserved through notes / source links

## Records Skipped

No canonical program records were skipped.

No tuition rows were skipped.

One canonical alias row was added:
- `fas-gpcs` -> `fas-gpcs-ms`

## Warnings / Manual Review

- `fhs-gphp` has `official_degree_name = null` in the candidate and is imported that way intentionally.
- `delivery_mode` normalization is handled during import:
  - `Online` -> `ONLINE`
- Some programs intentionally retain nulls for credits, duration, admissions, and related details because the import scope is program inventory plus tuition/source traceability, not full enrichment.
- The migration could not be executed against a live database in this workspace because there is no configured datasource.

## Result

The AUB graduate data is import-ready as a repeatable Flyway seed migration, but DB execution was not performed in this environment.

