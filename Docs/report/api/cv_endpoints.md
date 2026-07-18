# CV Endpoints

Source: `CVController`; authenticated. Base path `/api/cv`.

Confirmed groups: templates (`GET /templates`, `GET /templates/{id}`); CV CRUD (`GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`); section create/update/delete for education, experience, skill, project, language, and certificate; and CV-facing skills, positions, university lookup endpoints. DTOs are in `cvbuilder/application/dto` and controller signatures.

