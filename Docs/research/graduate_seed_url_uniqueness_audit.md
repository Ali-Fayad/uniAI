# Graduate Seed URL Uniqueness Audit

## Root Cause

`V24__graduate_program_schema_v2.sql` defines a unique constraint on:

`graduate_program (university_id, official_program_url)`

That constraint conflicts with later graduate seed migrations that intentionally reuse hub/catalog/program-list URLs for multiple graduate rows. The first visible failure was in `V25__seed_aub_graduate_data.sql`.

## Fixes Applied

- `Server/src/main/resources/db/migration/V23__create_graduate_program_schema.sql`
  - Added nullable `source.notes TEXT` to align the base schema with later seed migrations that already insert `notes` into `source`.
- `Server/src/main/resources/db/migration/V25__seed_aub_graduate_data.sql`
  - Added `ALTER TABLE graduate_program DROP CONSTRAINT IF EXISTS uq_graduate_program_university_url;`
- `Server/src/main/resources/db/migration/V44__seed_uls_graduate_data.sql`
  - Added `ALTER TABLE graduate_program DROP CONSTRAINT IF EXISTS uq_graduate_program_university_url;`

The canonical duplicate-prevention key remains:

- `graduate_program (university_id, program_key)`

Source URL uniqueness remains untouched.

## Duplicate URL Groups Found in AUB (V25)

All of the following are intentional shared hub/catalog/program-list pages reused across multiple graduate rows:

| URL | Reuse Count |
|---|---:|
| `https://www.aub.edu.lb/msfea/cee/Pages/default.aspx` | 5 |
| `https://www.aub.edu.lb/msfea/me/Pages/default.aspx` | 5 |
| `https://www.aub.edu.lb/fas/pspa/Pages/programs.aspx` | 3 |
| `https://www.aub.edu.lb/fas/soam/Pages/Graduateprograms.aspx` | 3 |
| `https://www.aub.edu.lb/fm/DACP/Pages/MS-Programs.aspx` | 3 |
| `https://www.aub.edu.lb/msfea/ard/Pages/default.aspx` | 3 |
| `https://www.aub.edu.lb/fas/arabic/Pages/program-learning-outcomes-for-degrees-granted.aspx` | 2 |
| `https://www.aub.edu.lb/fas/biology/Pages/biologydept.aspx` | 2 |
| `https://www.aub.edu.lb/fas/economics/Pages/graduate_programs.aspx` | 2 |
| `https://www.aub.edu.lb/fas/english/Pages/graduatestudies.aspx` | 2 |
| `https://www.aub.edu.lb/fas/histarc/Pages/History-Program.aspx` | 2 |
| `https://www.aub.edu.lb/fas/math/Pages/programs.aspx` | 2 |
| `https://www.aub.edu.lb/msfea/BME/Pages/default.aspx` | 2 |
| `https://www.aub.edu.lb/msfea/ece/Pages/default.aspx` | 2 |

## Duplicate URL Groups Found in ULS (V44)

These were also intentional shared faculty/program-list pages and now reuse is allowed consistently:

| URL | Reuse Count |
|---|---:|
| `https://www.uls.edu.lb/academics/law/` | 4 |
| `https://www.uls.edu.lb/academics/political-science/` | 3 |
| `https://www.uls.edu.lb/academics/economics-business/` | 2 |

## Migrations Audited

| Migration | University | Duplicate URLs? | Constraint Handling | Change Required |
|---|---|---:|---|---|
| V25 | AUB | Yes | Added drop in this audit | Yes |
| V26 | LAU | No | Not needed | No |
| V27 | USJ | Yes | Already handled before this audit | No |
| V28 | USEK | Yes | Already handled before this audit | No |
| V29 | UOB | Yes | Already handled before this audit | No |
| V30 | BAU | No | Not needed | No |
| V31 | NDU | No | Not needed | No |
| V32 | UL | Yes | Already handled before this audit | No |
| V33 | UA | No program URLs serialized | Not needed | No |
| V34 | LIU | Yes | Already handled before this audit | No |
| V35 | AUST | No | Not needed | No |
| V36 | HU | No | Not needed | No |
| V37 | RHU | No | Not needed | No |
| V38 | MUBS | No program URLs serialized | Not needed | No |
| V39 | AUL | No | Not needed | No |
| V40 | AOU | No | Not needed | No |
| V41 | MEU | No program URLs serialized | Not needed | No |
| V42 | LCU | No program URLs serialized | Not needed | No |
| V43 | LGU | No program URLs serialized | Not needed | No |
| V44 | ULS | Yes | Added drop in this audit | Yes |
| V45 | PU | No program URLs serialized | Not needed | No |
| V46 | JU | No program URLs serialized | Not needed | No |
| V47 | MUB | No program URLs serialized | Not needed | No |
| V48 | BIU | No program URLs serialized | Not needed | No |
| V49 | AUCE | No program URLs serialized | Not needed | No |
| V50 | AUOT | No program URLs serialized | Not needed | No |
| V51 | GU | No program URLs serialized | Not needed | No |
| V52 | TUI | No program URLs serialized | Not needed | No |
| V53 | ESA | No program URLs serialized | Not needed | No |
| V54 | ALBA | No program URLs serialized | Not needed | No |
| V55 | CNAM | No program URLs serialized | Not needed | No |

## Validation Results

- `./mvnw -q -DskipTests compile`: pass
- Temporary clean-database replay:
  - Reached and applied `V25`, `V26`, `V27`, `V28` successfully after the fix
  - Continued beyond the URL-uniqueness issue
  - Stopped later in `V29__seed_uob_graduate_data.sql` on a separate, pre-existing `graduate_admission_requirement` duplicate-row conflict unrelated to `official_program_url`

## Summary

- The AUB URL-uniqueness failure is fixed.
- ULS URL reuse is now handled consistently as well.
- `graduate_program` uniqueness remains anchored on `(university_id, program_key)`.
- Source URL uniqueness is untouched.
- No graduate program records were removed.

