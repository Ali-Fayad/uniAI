# API Architecture

REST controllers expose authentication, users, chat, CV/personal information, catalogue, feedback, and administration. Controllers are thin and delegate to use cases/application services. Authentication identity is recovered through `JwtFacade`/security context. See the [API inventory](../api/api_inventory.md).

