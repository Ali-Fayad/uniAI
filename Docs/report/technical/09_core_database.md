# Core Project Database

The database is physically shared. This chapter documents the core application view: users, verification codes, personal information, CV structures, chats/messages, feedback, and catalogue-facing entities. Primary schema evidence is V1–V22, V4–V13, V14, and V56.

Migration V57 normalizes catalogue geography: `university` contains one institution per row and `campus` contains approved physical locations with city, locality, type, and optional coordinates. Graduate, source, CV education, and other university foreign keys are remapped to the approved canonical institution IDs.

See [core ERD](../diagrams/database/core_application_erd.puml). JPA entity names and migration table shapes should be preferred over any legacy document when they disagree.
