# UA Import Report

Date accessed: 2026-07-04

## Counts

- University count: 1
- Faculty/school count: 6
- Department count: 0
- Degree type count: 4
- Language count: 4
- Source count: 50
- Program count: 21
- MASTER count: 21
- PHD count: 0
- Tuition row count: 19
- Fee item rows: 5
- Admission requirement rows: 34
- Required document rows: 7
- Deadline rows: 1
- Scholarship rows: 0
- Financial aid rows: 0
- Payment plan rows: 0
- Accreditation rows: 6
- Track rows: 0
- Alias rows: 0
- Program-source links: 78
- Out-of-scope skipped: 9

## Tuition Coverage by Faculty

- Antonine School of Business: 7
- Faculty of Information and Communication: 1
- Faculty of Music and Musicology: 6
- Faculty of Public Health: 1
- Faculty of Sport Sciences: 4
- Faculty of Theology: 0 (two master's programs intentionally remain null)

## Validation

- All research/ua/*.json parse: pass
- No duplicate IDs: pass
- No broken source references: pass
- Out-of-scope rows skipped: pass
- Enum compatibility with V24: pass
- Idempotent Flyway pattern: pass
- ./mvnw -q -DskipTests compile: pass

## Implementation Notes

- UA program tuition is seeded for 19 programs only; the two Theology master's programs remain null because no reliable official tuition value was published.
- Shared graduate admissions, required documents, deadlines, and fee items are seeded at university scope from university.json.
- The 11 interview requirements and the single GMAT-specific MBA condition are seeded as program-level admission requirements.
- Six Master of Music and Musicology records carry the official accreditation certificate as program-level accreditation rows.
- No departments, scholarship rows, financial aid rows, payment-plan rows, tracks, or aliases were published in the reviewed official source set.
- One MBA program page exposes a flexible credit range rather than a single integer; the seed leaves that program's credits null rather than inventing a normalized value.

## Recommendation

APPROVE
