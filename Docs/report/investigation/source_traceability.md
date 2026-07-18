# Source Traceability

| Report topic | Primary code evidence | Data/API evidence | Tests | Diagram |
|---|---|---|---|---|
| Authentication | `user/application/service/AuthApplicationService.java`, `shared/infrastructure/config/SecurityConfig.java` | `AuthController`, `users`, `verification_code` | `AuthApplicationServiceTest`, `JwtTokenHelperTest` | `sequences/authentication_sequence.puml` |
| Chat and AI | `chat/application/service/ChatApplicationService.java` | `ChatController`, `chats`, `messages` | `ChatApplicationServiceTest` | `ai/ai_request_pipeline.puml` |
| Graduate retrieval | `chat/application/retrieval/*`, SQL adapters | graduate tables V23/V24 | interpreter, resolver, retrieval tests | `ai/graduate_retrieval_flow.puml` |
| CV Builder | `cvbuilder/application/service/*` | `CVController`, CV tables | controller/service tests where present | `sequences/cv_builder_sequence.puml` |
| Map/catalogue | `catalog/*`, `Client/src/components/page/MapPage.tsx` | `CatalogController`, `university` | catalog adapter test | `sequences/university_map_sequence.puml` |
| Admin/feedback | `admin/*`, `feedback/*` | controller routes and tables | admin/feedback tests | `sequences/admin_user_management_sequence.puml` |

