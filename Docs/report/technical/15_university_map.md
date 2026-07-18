# University Map

`MapPage` uses React Leaflet. Investigation confirms the map coordinate dataset is frontend static data (`Client/src/data/universities.ts`) rather than the backend `university` catalogue used by graduate retrieval. The authenticated `/api/universities` endpoint is a searchable catalog API, not a confirmed map-detail API.

