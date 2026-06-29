# BAU Final Quality Report

## Verdict
DEFER.

## Reason
Official BAU sources were discovered, but full recursive crawl and PDF extraction could not be completed due to fetch failures/timeouts. The generated data is source-traceable and avoids assumptions, but it is not complete enough for production import.

## Counts
- Sources: 25
- In-scope graduate entries: 116
- Out-of-scope graduate diploma entries: 14

## Quality checks
- Only official BAU URLs included.
- Missing values are null.
- Every program has source IDs.
- Program-specific fields requiring unopened pages/PDFs are not invented.

## Next pass
Use Atlas/browser agent to recursively open each faculty listing and each program link, then download and parse BAU tuition/bylaws/brochure PDFs.
