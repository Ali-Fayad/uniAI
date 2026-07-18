# Backend Architecture

Backend modules follow a pragmatic domain/application/infrastructure/presentation organisation. Controllers delegate to application services or use cases; repository ports/adapters isolate persistence in several modules; graduate retrieval is infrastructure-owned SQL behind retrieval ports. Entities are also JPA persistence entities, so this is not a fully separate domain/persistence model.

See [backend diagrams](../diagrams/backend/).

