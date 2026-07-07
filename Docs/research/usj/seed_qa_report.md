# USJ Seed QA Report

## Verdict

APPROVE WITH NOTES

## Checks

- Programs in scope: 51
- Master’s: 47
- PhD: 4
- Tuition rows: 47
- Sources: 32
- Program-source links: 108
- Admission requirement rows: 5
- Accreditation rows: 1
- Out-of-scope records inserted as graduate programs: 0

## Migration Review

- V27 is the correct next Flyway migration after V26.
- The seed migration name is consistent with the sequence: `V27__seed_usj_graduate_data.sql`.
- The unique constraint on `graduate_program(university_id, official_program_url)` is intentionally dropped at the top of V27 so USJ hub/catalog URLs can be reused across multiple official program records.
- V24 enum/check constraints remain compatible with the USJ data.

## Integrity Review

- Every graduate program has at least one source link.
- Every tuition row maps to an existing graduate program.
- No out-of-scope records were imported as graduate programs.
- Tuition is absent only for the 4 PhD records.
- Program IDs remain unchanged from the canonical inventory.
- Duplicate IDs are not present in the in-scope set.
- Duplicate source URLs are not present in the seed registry.

## Tuition Review

- The tuition mapping uses the official USJ second-cycle PDF only.
- The seed now inserts 47 tuition rows.
- The 4 doctorate programs remain null because the provided PDF does not publish third-cycle tuition.

## Validation

- `research/usj/programs.json` parses.
- `research/usj/sources.json` parses.
- `./mvnw -q -DskipTests compile` passed in `Server/`.
- Flyway database execution was not performed because no datasource is configured in this workspace.

## Recommendation

APPROVE WITH NOTES
