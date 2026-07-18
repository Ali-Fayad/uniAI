# University and Campus Normalization Decision Report

Verification date: 2026-07-18

## 1. Summary

This report records the data decisions used by `university-campus-normalization.csv`. The matrix contains all 82 rows from the live `university` table and maps them to 35 existing canonical institution IDs. It proposes 71 campus records, marks 79 rows `APPROVED`, zero `NEEDS_REVIEW`, and three `DO_NOT_MIGRATE`. No row is marked `BLOCKED`.

The matrix deliberately does not treat every legacy coordinate or campus label as authoritative. Conflicting locations are corrected only where repository research or an official university source supports the correction. Unresolved coordinates are blanked in the proposed target rather than silently retaining the first value.

## 2. Source Methodology

Sources were used in the required order:

1. The live PostgreSQL `university` values and foreign-key usage established the old-row inventory and canonical ID ownership.
2. `UnisCoordinateTable.md` supplied the repository's existing campus names, location labels, and coordinates.
3. `Docs/research/<institution>/` supplied research based on official catalogues and university pages.
4. URLs already stored in the `source` table established official institution domains.
5. Official university pages were consulted only for unresolved or contradictory rows.

`DATABASE` is used only where the current database itself is the relevant evidence, principally institution-only records. `REPOSITORY_RESEARCH` identifies repository evidence. `OFFICIAL_WEB` identifies an official university URL. No Wikipedia, map snippet, directory, social-media source, or coordinate-only reverse inference was used.

Coordinates were copied only when the old row and approved location remained consistent. They were removed from the target decision for NDU Shouf, Phoenicia University, Al Maaref University, BIU, AUCE, and AUT where the old coordinate was unverified, contradictory, or represented an unresolved multi-coordinate value. AUB Marine Research retains its legacy coordinates because official AUB material confirms Batroun.

## 3. Canonical Institution Decisions

All duplicate acronym groups are documented below.

| Institution group | Old IDs | Canonical ID | Canonical name | Why this ID | Campuses created | Non-campus rows | Remaining uncertainty |
|---|---|---:|---|---|---:|---|---|
| AUB | 1–4 | 1 | American University of Beirut | ID 1 owns 465 dependent rows | 4 | None | ID 4 Marine Research approved in Batroun from official AUB material |
| LAU | 5–6 | 5 | Lebanese American University | ID 5 owns 337 dependent rows | 2 | None | None |
| USJ | 7–10, 75 | 75 | Université Saint-Joseph de Beyrouth | Institution-only ID 75 owns 261 dependent rows | 4 | 75 | None after official campus verification |
| UL | 11–16 | 11 | Lebanese University | ID 11 owns 285 dependent rows | 6 | None | Locations rely on repository campus evidence |
| NDU | 17–19, 76 | 76 | Notre Dame University-Louaize | Institution-only ID 76 owns 300 dependent rows | 2 | 18, 76 | Legacy `Metn Campus` is unsupported and excluded |
| USEK | 20–21 | 20 | Holy Spirit University of Kaslik | ID 20 owns 433 dependent rows | 2 | None | None recorded |
| UOB | 22–23 | 22 | University of Balamand | ID 22 owns 433 dependent rows | 1 | 23 | Legacy Tripoli medical campus is unsupported by reviewed official-catalogue research |
| BAU | 24–26 | 24 | Beirut Arab University | ID 24 owns 718 dependent rows | 3 | None | None recorded |
| LIU | 27–33 | 27 | Lebanese International University | ID 27 owns 166 dependent rows | 7 | None | None recorded |
| UA | 34–36, 77 | 77 | Antonine University | Institution-only ID 77 owns 227 dependent rows | 3 | 77 | Legacy campus names normalized to current official names |
| AUST | 38–43 | 38 | American University of Science and Technology | ID 38 owns 153 dependent rows | 6 | None | Locations rely on repository evidence |
| AUL | 44–46, 78 | 78 | Arts, Sciences and Technology University in Lebanon | Institution-only ID 78 owns 36 dependent rows | 3 | 78 | None recorded |
| AKU | 47–48 | 47 | Al-Kafaàt University | No dependent owner exists; ID 47 is retained as the first institution row | 2 | None | Canonical choice causes no current FK movement |
| MUBS | 49–51 | 49 | Modern University for Business and Science | ID 49 owns 63 dependent rows | 3 | None | None recorded |
| AOU | 52–55, 79 | 79 | Arab Open University - Lebanon | Institution-only ID 79 owns 94 dependent rows | 4 | 79 | None recorded |
| CNAM | 67, 82 | 82 | Cnam Lebanon / ISSAE-Cnam Liban | Institution-only ID 82 owns 25 dependent rows | 1 | 82 | Other official teaching centres are not represented by legacy rows |
| TUI | 72, 80 | 80 | Tripoli University Institute / University of Tripoli | Institution-only ID 80 owns 37 dependent rows | 1 | 80 | Acronym remains the existing `TUI` for compatibility |
| ESA | 74, 81 | 81 | École Supérieure des Affaires | Institution-only ID 81 owns 64 dependent rows | 1 | 81 | Accent normalized to the official name |

Singleton institutions retain their existing IDs. Their current mapping is explicit in the CSV and does not create synthetic IDs.

## 4. Campus Decisions

Named physical campuses, branches, research facilities, medical facilities, and institutes create campuses only when a usable location exists or the row is explicitly held for review.

Important corrections include:

- AUB AREC uses `Haush Sneid`, supported by AUB's official location page; `Beqaa Valley` is not stored as a city.
- USJ campus names use the current official forms, including `Campus de l'Innovation et du Sport` and `Campus des Sciences et Technologies`.
- UA campuses use `Hadat–Baabda`, `Nabi Ayla–Zahle`, and `Mejdlaya–Zgharta` official names.
- UOB row 22 uses `Al-Kurah Campus`, supported by repository research from the official graduate catalogue.
- CNAM row 67 becomes `Beirut Center` at Bir Hassan.
- AUCE row 69 becomes the official `Badaro Campus`; its inconsistent legacy coordinate is not retained.
- Al Maaref University is corrected from Tripoli to its officially documented Beirut campus.

Institution-only rows 75–82 use `creates_campus=FALSE`. Unsupported NDU row 18 and UOB row 23 also use `creates_campus=FALSE` with `DO_NOT_MIGRATE` so they cannot become physical campuses accidentally.

## 5. Country Decisions

All 35 canonical institutions are assigned `Lebanon`. This is supported by the Lebanese institution scope of the repository research, official university domains and contact pages, and the existing institution-only rows where country was already populated.

Country is repeated consistently across every old row belonging to the same canonical institution. Country is never copied from a blank legacy value, and it is not placed on campus records.

## 6. City and Locality Decisions

City represents the broad city or municipality. Locality is used only for a more specific area that was explicitly present in repository or official evidence.

Examples:

- Beirut / Ras Beirut
- Beirut / Hamra
- Beirut / Kantari
- Beirut / Achrafieh
- Beirut / Tariq El Jdideh
- Beirut / Badaro
- Beirut / Bir Hassan
- Beirut / Clemenceau
- Zahle / Nabi Ayla

Combined source labels were split. Streets such as Damascus Road and Airport Avenue were not forced into the locality field. District or regional labels were not silently converted to cities. Phoenicia University's Zahrani row is `DO_NOT_MIGRATE` because the official source identifies the district but not a municipality-level city required by the target schema.

No locality was inferred from latitude or longitude.

## 7. Ambiguous Rows

| Old ID | Institution | Decision | Reason |
|---:|---|---|---|
| 4 | AUB | `APPROVED` | Official AUB annual-report material identifies Marine Science and Oceanography in Batroun, Lebanon; the legacy coordinates are retained |
| 59 | Phoenicia University | `DO_NOT_MIGRATE` | Official pages confirm a physical Main Campus in the District of Zahrani but do not provide a municipality-level city; the target campus is therefore not created |
| 66 | Beirut Islamic University | `APPROVED` | Official contact page confirms one BIU site on Ibn Rushd Street in Beirut; unresolved legacy coordinates are excluded |
| 70 | American University of Technology | `APPROVED` | Official AUT page confirms the Main Campus in Halat; unresolved legacy coordinates are excluded |

These rows are not migration-ready even where `creates_campus=TRUE`. An implementation must filter for `review_status='APPROVED'` or fail validation.

## 8. Blocked Rows

There are no rows with `review_status=BLOCKED`.

Two rows are `DO_NOT_MIGRATE`:

- ID 18, NDU `Metn Campus`: current official NDU material identifies Zouk Mosbeh, Barsa/Koura, and Deir El Qamar campuses, not a Metn campus.
- ID 23, UOB `Tripoli Campus` marked Medical: repository research based on the official catalogue identifies Al-Kurah, Dekouaneh, and Souk El Gharb-Aley campus contexts, not this legacy campus.

These rows remain mapped to their canonical institutions for completeness but must not create campuses.

## 9. BIU and AUOT Coordinate Review

### BIU

`UnisCoordinateTable.md` contains three coordinate pairs for one `Main` row. The official BIU contact page confirms a Beirut site on Ibn Rushd Street, south of Dar Al-Fatwa, but does not associate the three coordinates with three named campuses.

Decision:

- Keep one proposed `Main Campus` in Beirut.
- Do not migrate any of the three coordinates.
- Approve row 66 as one `Main Campus` in Beirut without retaining the unresolved coordinate list.

### AUOT/AUT

`UnisCoordinateTable.md` contains two coordinate pairs and an `Unknown` location. AUT's official campus page identifies Main Campus at Halat, North Campus at Ras Maska/Koura, and Akkar Campus on Halba main road.

Decision:

- Map the old `Main` row only to `Main Campus`, Halat.
- Do not retain either legacy coordinate.
- Do not manufacture extra CSV rows for North or Akkar campuses because the required matrix is one row per existing ID.
- Approve row 70 as `Main Campus` in Halat without retaining the unresolved coordinate list. The separately named North and Akkar campuses require separate source rows in a future data revision if they are to be added to this matrix.

## 10. Canonical ID Selection Rationale

Canonical IDs prioritize the existing ID already owning graduate-data foreign keys. The live union of direct dependent rows produced the following important ownership counts:

| Canonical ID | Acronym | Dependent rows | Rationale |
|---:|---|---:|---|
| 1 | AUB | 465 | Existing graduate owner |
| 5 | LAU | 337 | Existing graduate owner |
| 75 | USJ | 261 | Institution-only graduate owner |
| 11 | UL | 285 | Existing graduate owner |
| 76 | NDU | 300 | Institution-only graduate owner |
| 20 | USEK | 433 | Existing graduate owner |
| 22 | UOB | 433 | Existing graduate owner |
| 24 | BAU | 718 | Existing graduate owner |
| 27 | LIU | 166 | Existing graduate owner |
| 77 | UA | 227 | Institution-only graduate owner |
| 38 | AUST | 153 | Existing graduate owner |
| 78 | AUL | 36 | Institution-only graduate owner |
| 49 | MUBS | 63 | Existing graduate owner |
| 79 | AOU | 94 | Institution-only graduate owner |
| 82 | CNAM | 25 | Institution-only graduate owner |
| 80 | TUI | 37 | Institution-only graduate owner |
| 81 | ESA | 64 | Institution-only graduate owner |

Other populated singleton owners remain unchanged: HU 37, MEU 56, JU 57, RHU 58, PU 59, LCU 61, LGU 62, ULS 63, MUB 65, BIU 66, AUCE 69, AUOT 70, GU 71, and ALBA 73. AKU, Al Maaref University, USAL, and the Lebanese National Conservatory currently have no direct dependent rows, so their only existing institution IDs are retained.

## 11. Duplicate and Collision Analysis

The live database has 29 physical foreign keys to `university(id)`. In every currently populated dependent table, a normalized institution uses only one university ID. The live preflight therefore found no case where a populated table would merge two old IDs for one institution.

This does not prove every environment is collision-free. The validation SQL checks future collisions for institution-scoped unique keys, including faculty names, department names, program keys, source URLs, aliases, scoped `record_key` values, and legacy graduate names. Materially different rows that collide must abort migration; they must not be deleted merely to satisfy a unique constraint.

The CSV itself has no duplicate `(canonical_university_id, normalized campus_name)` key.

## 12. Readiness Assessment

| Measure | Count |
|---|---:|
| Current rows represented | 82 |
| Canonical institutions | 35 |
| Approved rows | 79 |
| Needs review | 0 |
| Blocked | 0 |
| Do not migrate | 3 |
| Rows proposing campuses | 71 |

All rows now have final decisions. IDs 18 and 23 must remain excluded unless new official evidence reverses their status. ID 59 is also explicitly excluded because its official location is a district rather than a municipality-level city.

## 13. Recommended Next Step

Run `university-campus-normalization-validation.sql` against the current database and consume only rows marked `APPROVED` when generating the forward-only migration. Keep rows 18, 23, and 59 excluded from campus creation.

READY_FOR_IMPLEMENTATION
