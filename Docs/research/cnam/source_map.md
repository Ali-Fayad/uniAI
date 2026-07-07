# CNAM Lebanon Source Map

Task code: `CNAM_DISCOVERY_001_BROWSER_INV`  
Official website: <https://www.cnam-liban.fr>  
Generated: 2026-07-07

## Scope

In scope:
- Master's programs
- PhD/Doctorat programs
- Official graduate catalogues, admissions pages, required-document pages, financial-aid pages, payment/fee evidence, regulations, and official PDFs

Out of scope:
- Individual courses/modules
- Certificates
- Diplomas unless clearly official Master's-level degrees
- Continuing education/professional training
- News/events/ads
- Faculty profiles or partnerships without explicit graduate-program evidence

## Official source inventory

| Source ID | Official URL | Type | Relevance |
|---|---|---|---|
| `cnam_home` | <https://www.cnam-liban.fr/> | Homepage | Official entry point; links to catalogues, departments, documents/procedures, and contact pages. |
| `cnam_catalogue_general` | <https://www.cnam-liban.fr/offre-de-formation/catalogue-general/> | Catalogue | Main official catalogue. States results include Cnam Liban formations and distance formations from other Cnam centres; lists 349 formations, including diploma/certificate/course categories. |
| `cnam_master_finance` | <https://www.cnam-liban.fr/offre-de-formation/catalogue-general/master-finance-parcours-finance-d-entreprise-et-ingenierie-financiere-1497513.kjsp?RF=libcatagene> | Official Master program page | Official evidence for `MR10701A-LIB`, 120-credit Master Finance, Bac+5 exit level, accredited through 2029-2030, with M1/M2 access details and Liban centre offering years. |
| `cnam_master_data_science` | <https://www.cnam-liban.fr/offre-de-formation/catalogue-general/master-mathematiques-appliquees-statistique-parcours-science-des-donnees-1497515.kjsp?RF=libcatagene> | Official Master program page | Official evidence for `MR12303A-LIB`, 120-credit Master mathématiques appliquées/statistique, Science des données, Bac+5 exit level, accredited through 2029-2030, with prerequisites and Liban centre offering years. |
| `cnam_master_entrepreneurship_project_management` | <https://www.cnam-liban.fr/offre-de-formation/catalogue-general/master-entrepreneuriat-et-management-de-projet-parcours-management-de-projet-et-d-affaires-1573881.kjsp?RF=libcatagene> | Official Master program page | Official page for `MR12001A-LIB`, 120-credit Master entrepreneuriat et management de projet, Bac+5 exit level, accredited through 2029-2030. Included with verification note because captured centre-offering block did not show Liban year lines. |
| `cnam_diplomes_mode_emploi` | <https://www.cnam-liban.fr/offre-de-formation/diplomes-mode-d-emploi/> | Degree policy / mode d'emploi | Explains Cnam degree levels and national diploma categories including Master and Doctorat. This supports terminology only; it is not a specific PhD program offering. |
| `cnam_documents_procedures` | <https://www.cnam-liban.fr/documents-et-procedures/documents-et-procedures-784703.kjsp> | Admissions/required documents | Lists required documents for new students and holders of higher-education diplomas, plus documents for diploma, dispensation, admission, and thesis/memoire procedures. |
| `cnam_financial_aid` | <https://www.cnam-liban.fr/cnam-liban/formulaire-de-demande-d-aide-financiere-pour-les-droits-d-inscription-1455358.kjsp> | Financial aid | Official financial-aid instructions for tuition/registration fees. |
| `cnam_financial_aid_pdf` | <https://www.cnam-liban.fr/medias/fichier/formulaire-aide-financiere-2023-2024_1701352067187-pdf> | Official PDF | Financial-aid form asking for diploma in progress and fee amounts paid/due. |
| `cnam_eicnam_admission_request` | <https://www.cnam-liban.fr/cnam-liban/demande-d-inscription-a-l-examen-d-admission-a-l-eicnam-1353357.kjsp> | Admissions/fee evidence | Engineering-school admission-exam page with 72 USD fee. Not Master evidence, but relevant to admissions/fees discovered. |
| `cnam_documents_download` | <https://www.cnam-liban.fr/documents-et-procedures/documents-a-telecharger-785116.kjsp> | Documents/regulations hub | Official download hub for habilitation, candidacy, memoire, and engineering regulations. No Master/PhD-specific regulation was identified in the inspected source results. |

## Graduate evidence map

### Master's evidence found

1. `MR10701A-LIB` — Master Finance Parcours Finance d'entreprise et ingénierie financière
   - Official page: `cnam_master_finance`
   - Strong evidence: programme code, 120 credits, Bac+5 exit level, official title, accreditation, M1/M2 admissions, programme structure, Liban centre offering years.

2. `MR12303A-LIB` — Master mathématiques appliquées, statistique Parcours Science des données
   - Official page: `cnam_master_data_science`
   - Strong evidence: programme code, 120 credits, Bac+5 exit level, accreditation, prerequisites, M2 dossier admission, Liban centre offering years.

3. `MR12001A-LIB` — Master entrepreneuriat et management de projet Parcours Management de projet et d'affaires
   - Official page: `cnam_master_entrepreneurship_project_management`
   - Evidence: programme code, 120 credits, Bac+5 exit level, accreditation, prerequisites, selection on file.
   - Note: the captured lines did not show a populated “Centre(s) d'enseignement proposant cette formation” block, so verify availability before import if the project requires only actively scheduled Liban centre offerings.

### PhD evidence found

No official PhD program page, Doctorat programme listing, graduate PhD admissions page, or official CNAM Lebanon PhD PDF was found.

The site uses “doctorat” in generic diploma-level/category pages, but those generic references are not accepted as evidence of a current PhD offering.

Required wording: **No official PhD evidence found.**

## Search/discovery notes

Queries and inspections covered:
- Site homepage/navigation
- Catalogue général
- Catalogue des diplômes et certificats
- Master keyword pages
- Doctorat/PhD keyword searches
- Admission/document pages
- Financial-aid/fee pages and PDF
- Download/regulation hub
- Degree mode d'emploi

## Validation notes

- All recorded source URLs are official `https://www.cnam-liban.fr/...` URLs.
- External links found on CNAM pages, such as national catalogue/RNCP/department links and Google Drive forms, were not added to `sources.json` because validation requires official CNAM URLs only.
