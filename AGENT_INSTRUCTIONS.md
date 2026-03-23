# 📋 uniAI Agent Instructions

## General Rules

### SOLID Principles
Always follow SOLID principles:

- **Single Responsibility:** Each class has one job
- **Open/Closed:** Open for extension, closed for modification
- **Liskov Substitution:** Subtypes must be substitutable for base types
- **Interface Segregation:** Small, focused interfaces
- **Dependency Inversion:** Depend on abstractions, not concretions

### Documentation
Always add documentation:

- JavaDoc for all public methods and classes
- Explain why not just what
- Include `@param`, `@return`, `@throws` tags
- Keep `API_DOC.md` in project root updated with any API changes

### Ask Questions
If anything is unclear or you need to add new conditions, ask. Don’t assume.

---

## Frontend Rules

### Project Structure
Follow existing structure exactly:

```text
Client/src/
├── components/         # Shared components
│   ├── animations/     # Framer Motion animations (FadeIn, SlideIn, etc.)
│   ├── auth/           # Auth-specific components
│   ├── chat/           # Chat components
│   ├── common/         # Shared UI components
│   ├── layout/         # Layout components (Navbar, etc.)
│   ├── page/           # Page components
│   │   ├── auth/       # Auth pages (SignIn, SignUp, Verify, etc.)
│   │   └── *.tsx       # MainPage, ChatPage, MapPage, SettingsPage, AboutPage
│   └── settings/       # Settings components
├── constants/          # Constants and static data
├── context/            # React context (AuthContext, etc.)
├── data/               # Static data files (universities.ts)
├── hooks/              # Custom hooks (useAuth, useChat, useTheme, etc.)
├── http/               # HTTP error handlers
├── interfaces/         # TypeScript interfaces (IAuthService, IChatService, etc.)
├── lib/                # Utility libraries
├── services/           # API services (api.ts, auth.ts, chat.ts, user.ts)
├── styles/             # Global styles (chat.css, themes.ts, variables.css)
├── types/              # TypeScript types (dto.ts)
├── utils/              # Utility functions (JwtDecode, Storage, webgl.ts)
├── App.tsx
├── index.css
├── main.tsx
└── router.tsx
```

### UI Rules

- Only use TailwindCSS for styling - no other CSS libraries without permission
- No inline styles unless absolutely necessary
- Use existing design system (colors, spacing, typography) from theme
- All new components must be responsive (mobile-first)
- Follow existing naming conventions (PascalCase for components, camelCase for functions)
- Check `components.json` for shadcn/ui components if available
 - Always use the same Framer Motion opening and closing animations for any newly added or modified frontend components; reuse the shared animations located in `Client/src/components/animations/` to ensure consistent motion across the app.
 - Always analyze the general UI (colors, fonts, spacing) of the surrounding screens and reuse existing theme tokens and style choices from `Client/src/styles/` so new or modified components visually match the app's established style.

---

## Backend Rules

### Architecture: DDD + Hexagonal
Follow the exact structure from existing modules:

```text
Server/src/main/java/com/uniai/[feature-name]/
├── application/
│   ├── dto/
│   │   ├── command/     # Request DTOs (CreateXxxCommand, UpdateXxxCommand)
│   │   └── response/    # Response DTOs (XxxResponse)
│   ├── port/
│   │   └── in/          # Input ports (interfaces for use cases)
│   └── service/         # Application services (orchestration)
├── domain/
│   ├── builder/         # Domain builders (for complex object construction)
│   ├── model/           # Domain entities
│   ├── repository/      # Repository interfaces
│   └── valueobject/     # Value objects (if needed)
├── infrastructure/
│   ├── client/          # External API clients
│   ├── config/          # Configuration classes
│   ├── mapper/          # Map between domain and JPA entities
│   └── persistence/     # JPA repositories and adapters
│       ├── adapter/     # Repository implementations
│       └── repository/  # Spring Data JPA interfaces
└── presentation/
    └── controller/      # REST controllers
```

### Existing Modules to Reference

- `chat/` - Chat feature (reference for DDD structure)
- `cvbuilder/` - CV Builder feature (new, follow this pattern)
- `feedback/` - Feedback feature
- `user/` - User authentication
- `shared/` - Shared utilities (exceptions, config, JWT, email)

### Key Patterns

- Domain models should not have JPA annotations (separate JPA entities in infrastructure)
- Repositories are interfaces in domain, implemented in infrastructure
- DTOs are immutable (use builders or record classes)
- Validation on command DTOs using `@Valid` annotations
- Authentication: Use `@RequestAttribute Long userId` in controllers to get current user
- Exceptions: Add new exceptions to `shared/exception/` and handle in `GlobalExceptionHandler.java`

---

## API Documentation Rule

EVERY time you:

- Create a new endpoint
- Update an existing endpoint
- Change request/response body
- Change HTTP status codes
- Change authentication requirements

You MUST update `API_DOC.md` in the project root with the changes.

## DTO Rule

EVERY time you:

- Create a new DTO
- Update fields in a DTO
- Add validation annotations

You MUST update `API_DOC.md` with the new/updated schema.

---

## Database Rules

### Migration Files

- Location: `Server/src/main/resources/db/migration/`
- Naming: `V{version}__description.sql`
- Example: `V1__create_cvbuilder_tables.sql`, `V2__personal_info_use_user_id_pk.sql`

### Existing Tables

- `universities` - University data from `UnisCoordinateTable.md`
- `personal_info` - User profile data (`user_id` as PK)
- `cv` - CV metadata
- `education`, `experience`, `skill`, `project`, `language`, `certificate` - CV sections

### Data Files

| File | Location | Purpose |
|------|----------|---------|
| `API_DOC.md` | Root | All API documentation |
| `UnisCoordinateTable.md` | Root | University coordinates data |
| `externalAPI.md` | Root | External API documentation |
| `universities.ts` | `Client/src/data/` | Frontend university data |
| `application.properties` | `Server/src/main/resources/` | Backend configuration |

---

## Before You Ask

Check these first:

- Does my code follow SOLID principles?
- Is documentation complete (JavaDoc + `API_DOC.md`)?
- Does frontend code follow the existing structure?
- Does backend code follow DDD + Hexagonal?
- Is `API_DOC.md` updated with any endpoint changes?
- Are new exceptions added to `shared/exception/`?
- Are new constants added to appropriate files?

## Ask When

- You need to add a new library/dependency
- You’re unsure about the architecture placement of new code
- You need to add new validation rules
- You’re unsure about authentication requirements
- You need to change existing patterns
- Anything is ambiguous or unclear
- You need to modify existing files in ways that might affect other features
