# University and Catalog Endpoints

Source: `CatalogController`; all require authentication.

| Method/path | Optional query | Response |
|---|---|---|
| GET `/api/skills` | `search` | `List<SkillCatalogResponse>` |
| GET `/api/languages` | `search` | `List<LanguageCatalogResponse>` |
| GET `/api/positions` | `search` | `List<PositionCatalogResponse>` |
| GET `/api/universities` | `search` | `List<UniversityCatalogResponse>` |

The frontend map itself uses static coordinate data and is not confirmed to fetch these endpoints for marker rendering.

