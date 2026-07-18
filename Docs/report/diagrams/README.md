# Diagrams

PlantUML source is grouped by view. Blue denotes frontend, green backend/service, yellow persistence, and purple external provider. The sources are split for readability.

The two principal database views are [core application](database/core_application_erd.puml) and [AI/knowledge](database/ai_knowledge_erd.puml). They are logical views of one configured PostgreSQL database.

Validation note: no `plantuml` executable or PlantUML JAR was available in the inspected environment, so rendering and compiler validation are pending. Source files were checked structurally for paired `@startuml`/`@enduml` markers. Render with `plantuml -checkonly docs/report/diagrams/**/*.puml` (or an equivalent recursive invocation) in an environment with PlantUML installed.
