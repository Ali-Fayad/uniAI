# LCU Import Report

## University Counts

- University count: 1
- Faculty/school count: 1
- Department count: 0
- Degree type count: 4
- Language count: 4
- Source count: 10

## Program Counts

- Program count: 2
- MASTER count: 2
- PHD count: 0

## Tuition and Fee Counts

- Tuition rows: 0
- Fee item rows: 2
- Admission requirement rows: 1
- Required document rows: 9
- Deadline rows: 0
- Scholarship rows: 0
- Financial aid rows: 0
- Payment plan rows: 0
- Accreditation rows: 0
- Track rows: 0
- Alias rows: 0
- Program-source links: 6
- Out-of-scope skipped: 0

## Validation

- JSON parse: pass
- No duplicate program IDs: pass
- No duplicate source IDs: pass
- No duplicate source URLs: pass
- No broken source references: pass
- V24 enum compatibility: pass
- Idempotent Flyway pattern: pass
- `./mvnw -q -DskipTests compile`: pass

## Implementation Notes

- LCU is seeded conservatively as two graduate master's programs: Research MBA and Executive MBA.
- MBA emphases remain part of the single MBA structure rather than separate degrees.
- Tuition remains null for both programs because no reliable numeric graduate tuition value was published in the accessible official sources.
- The public Apply Now document list was seeded at university scope because it is the only available admissions document list, but it is explicitly undergraduate/freshman-facing in the discovery notes.
- No official PhD program evidence was found.
