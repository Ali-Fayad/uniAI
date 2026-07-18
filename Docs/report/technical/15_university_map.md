# University Map

`MapPage` uses React Leaflet. Investigation confirms the map coordinate dataset is frontend static data (`Client/src/data/universities.ts`) rather than the backend `university` catalogue used by graduate retrieval. The authenticated `/api/universities` endpoint is a searchable catalog API, not a confirmed map-detail API.

The backend catalogue now returns canonical institutions with nested campus records from `campus`; clients needing authoritative locations should use those records rather than treating an institution row as a campus.
