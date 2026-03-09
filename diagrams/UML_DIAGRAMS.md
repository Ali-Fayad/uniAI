# UniAI System - UML Diagrams

> **Project:** UniAI - AI Chat Application with Authentication
> **Actors:** User, System, AI
> **Generated:** January 2, 2026

---

## Table of Contents
1. [Use Case Diagram](#1-use-case-diagram)
2. [Class Diagram](#2-class-diagram)
3. [Sequence Diagrams](#3-sequence-diagrams)
   - [3.1 Authentication with 2FA](#31-authentication-with-2fa)
   - [3.2 Feedback Submission](#32-feedback-submission-with-auth-check)
   - [3.3 Theme Change (No Auth)](#33-theme-change-no-auth)
   - [3.4 User ↔ AI Chat Interaction](#34-user--ai-chat-interaction)

---

## 1. Use Case Diagram

```plantuml
@startuml
left to right direction
skinparam actorStyle awesome

actor User as U
actor "AI Service" as AI
actor System as SYS

' Authentication Use Cases
rectangle "Authentication" {
  usecase (Sign Up) as UC1
  usecase (Sign In) as UC2
  usecase (Sign In with Google) as UC3
  usecase (Verify Email) as UC4
  usecase (Verify 2FA Code) as UC5
  usecase (Reset Password) as UC6
}

' User Management Use Cases
rectangle "User Management" {
  usecase (Update Profile) as UC7
  usecase (Change Username) as UC8
  usecase (Change Password) as UC9
  usecase (Enable/Disable 2FA) as UC10
  usecase (Delete Account) as UC11
}

' Chat Use Cases (Authenticated)
rectangle "Chat Operations" #LightBlue {
  usecase (Create Chat) as UC12
  usecase (Send Message) as UC13
  usecase (Get Chats) as UC14
  usecase (Get Messages) as UC15
  usecase (Delete Chat) as UC16
}

' Feedback Use Case (Authenticated)
rectangle "Feedback" #LightBlue {
  usecase (Send Feedback) as UC17
}

' Theme Use Case (No Auth Required)
rectangle "UI Settings" #LightGreen {
  usecase (Change Theme) as UC18
}

' System Use Cases
rectangle "System Operations" {
  usecase (Send Verification Email) as UC19
  usecase (Validate JWT Token) as UC20
  usecase (Persist Data) as UC21
}

' User Interactions
U --> UC1
U --> UC2
U --> UC3
U --> UC4
U --> UC5
U --> UC6
U --> UC7
U --> UC8
U --> UC9
U --> UC10
U --> UC11
U --> UC12
U --> UC13
U --> UC14
U --> UC15
U --> UC16
U --> UC17
U --> UC18

' AI Interactions
UC13 --> AI : generates response

' System Interactions
UC1 ..> UC19 : <<include>>
UC2 ..> UC19 : <<include>> (if 2FA enabled)
UC5 ..> UC19 : <<include>>
UC6 ..> UC19 : <<include>>

UC2 ..> UC20 : <<include>>
UC7 ..> UC20 : <<include>>
UC8 ..> UC20 : <<include>>
UC9 ..> UC20 : <<include>>
UC10 ..> UC20 : <<include>>
UC11 ..> UC20 : <<include>>
UC12 ..> UC20 : <<include>>
UC13 ..> UC20 : <<include>>
UC14 ..> UC20 : <<include>>
UC15 ..> UC20 : <<include>>
UC16 ..> UC20 : <<include>>
UC17 ..> UC20 : <<include>>

UC1 ..> UC21 : <<include>>
UC7 ..> UC21 : <<include>>
UC12 ..> UC21 : <<include>>
UC13 ..> UC21 : <<include>>
UC17 ..> UC21 : <<include>>

SYS --> UC19
SYS --> UC20
SYS --> UC21

note right of UC18
  Theme switching is
  client-side only.
  No authentication required.
end note

note bottom of UC13
  Authenticated users can
  send messages to AI
end note

@enduml
```

---

## 2. Class Diagram

```plantuml
@startuml
skinparam linetype ortho

' ============================================================
' MODELS / ENTITIES
' ============================================================
package "com.uniai.model" {
  class User {
    - id: Long
    - firstName: String
    - lastName: String
    - username: String
    - email: String
    - password: String
    - isVerified: boolean
    - isTwoFacAuth: boolean
  }

  class Chat {
    - id: Long
    - user: User
    - title: String
    - createdAt: LocalDateTime
    - updatedAt: LocalDateTime
    + onCreate(): void
    + onUpdate(): void
  }

  class Message {
    - id: Long
    - chatId: Long
    - senderId: Long
    - content: String
    - timestamp: LocalDateTime
    + onCreate(): void
  }

  class VerifyCode {
    - id: Long
    - email: String
    - code: String
    - type: VerificationCodeType
    - expirationTime: LocalDateTime
    + saveCode(email, code, type): void
  }

  class Feedback {
    - id: Long
    - email: String
    - comment: String
  }
}

' ============================================================
' ENUMS / DOMAIN
' ============================================================
package "com.uniai.domain" {
  enum VerificationCodeType {
    VERIFY
    TWO_FACT_AUTH
    CHANGE_PASSWORD
  }
}

' ============================================================
' REPOSITORIES
' ============================================================
package "com.uniai.repository" {
  interface UserRepository {
    + findByEmail(email): User
    + findByUsername(username): User
    + existsByEmail(email): boolean
    + existsByUsername(username): boolean
    + deleteByEmail(email): boolean
    + deleteByUsername(username): boolean
  }

  interface ChatRepository {
    + findByUserUsernameOrderByUpdatedAtDesc(username): List<Chat>
    + findTitleById(chatId): String
    + findById(chatId): Optional<Chat>
  }

  interface MessageRepository {
    + findByChatIdOrderByTimestampAsc(chatId): List<Message>
    + findTop10ByChatIdOrderByTimestampDesc(chatId): List<Message>
    + deleteByChatId(chatId): void
    + deleteByChatIdIn(chatIds): void
    + countByChatId(chatId): long
    + existsByChatId(chatId): boolean
  }

  interface VerifyCodeRepository {
    + deleteByEmailAndType(email, type): void
    + findTopByEmailAndTypeOrderByExpirationTimeDesc(email, type): VerifyCode
  }

  interface FeedbackRepository {
  }
}

' ============================================================
' DTOs
' ============================================================
package "com.uniai.dto" {
  class SignUpDto {
    + firstName: String
    + lastName: String
    + username: String
    + email: String
    + password: String
  }

  class SignInDto {
    + email: String
    + password: String
  }

  class VerifyDto {
    + email: String
    + verificationCode: String
  }

  class RequestPasswordDto {
    + email: String
    + verificationCode: String
    + newPassword: String
  }

  class UpdateUserDto {
    + username: String
    + firstName: String
    + lastName: String
    + enableTwoFactor: Boolean
  }

  class FeedbackRequest {
    + email: String
    + comment: String
  }

  class SendMessageDto {
    + chatId: Long
    + content: String
  }

  class ChatCreationResponseDto {
    + chatId: Long
  }

  class MessageResponseDto {
    + messageId: Long
    + chatId: Long
    + senderId: Long
    + content: String
    + timestamp: LocalDateTime
  }

  class AuthenticationResponseDto {
    + username: String
    + firstName: String
    + lastName: String
    + email: String
    + isVerified: boolean
    + isTwoFacAuth: boolean
  }

  class TokenResponse {
    + token: String
  }
}

' ============================================================
' SERVICES
' ============================================================
package "com.uniai.services" {
  class AuthService {
    - userRepository: UserRepository
    - emailService: EmailService
    - passwordEncoder: PasswordEncoder
    - jwtUtil: JwtUtil
    + signUp(signUpDto): String
    + signIn(signInDto): String
    + verifyAndGenerateToken(email, code): String
    + checkTwoFactorAndGenerate(email, code): String
    + forgetPassword(email): void
    + resetPasswordWithCode(email, code, newPassword): String
  }

  class UserService {
    - userRepository: UserRepository
    - feedbackRepository: FeedbackRepository
    - passwordEncoder: PasswordEncoder
    + getMe(email): AuthenticationResponseDto
    + updateUserProfile(email, updateDto): AuthenticationResponseDto
    + deleteCurrentUser(email, password): void
    + changePasswordForUser(email, currentPassword, newPassword): void
    + sendFeedback(feedbackRequest): void
  }

  class ChatService {
    - chatRepository: ChatRepository
    - messageRepository: MessageRepository
    - userRepository: UserRepository
    + createChat(email): ChatCreationResponseDto
    + sendMessage(email, dto): MessageResponseDto
    + getUserChats(email): List<Chat>
    + getChatMessages(email, chatId): List<MessageResponseDto>
    + deleteChat(email, chatId): void
    + deleteAllChats(email): void
    - generateAIResponse(userContent): String
    - generateChatTitle(firstMessageContent): String
  }

  class EmailService {
    - mailSender: JavaMailSender
    - templateEngine: TemplateEngine
    - verifyCodeRepository: VerifyCodeRepository
    - userRepository: UserRepository
    + sendVerificationCode(email, type): String
    + verifyCode(email, code, type): User
  }

  class OAuthGoogleService {
    - userRepository: UserRepository
    - jwtUtil: JwtUtil
    + getGoogleAuthorizationUrl(): String
    + processGoogleCallback(code): String
  }
}

' ============================================================
' CONTROLLERS
' ============================================================
package "com.uniai.controller" {
  class AuthController {
    - authService: AuthService
    - oAuthGoogleService: OAuthGoogleService
    + signUp(signUpDto): ResponseEntity
    + signIn(signInDto): ResponseEntity
    + verifyCode(verifyDto): ResponseEntity
    + verifyTwoFactor(verifyDto): ResponseEntity
    + forgetPassword(emailDto): ResponseEntity
    + confirmForgetPassword(dto): ResponseEntity
    + getGoogleAuthUrl(request): ResponseEntity
  }

  class UserController {
    - userService: UserService
    - jwtFacade: JwtFacade
    + getMe(): ResponseEntity
    + updateMe(updateDto): ResponseEntity
    + deleteMe(dto): ResponseEntity
    + changePassword(dto): ResponseEntity
    + sendFeedback(entity): ResponseEntity
  }

  class ChatController {
    - chatService: ChatService
    - jwtFacade: JwtFacade
    + createChat(): ResponseEntity
    + sendMessage(dto): ResponseEntity
    + getUserChats(): ResponseEntity
    + getChatMessages(chatId): ResponseEntity
    + deleteChat(chatId): ResponseEntity
    + deleteAllChats(): ResponseEntity
  }
}

' ============================================================
' SECURITY
' ============================================================
package "com.uniai.security.jwt" {
  class JwtUtil {
    + generateToken(dto): String
    + validateToken(token): boolean
    + getUserDtoFromToken(token): AuthenticationResponseDto
  }

  class JwtFilter {
    - jwtUtil: JwtUtil
    + doFilterInternal(request, response, chain): void
  }

  class JwtFacade {
    + getAuthenticatedUserEmail(): String
  }
}

' ============================================================
' BUILDERS
' ============================================================
package "com.uniai.builder" {
  class ChatBuilder {
    + {static} toChatCreationResponse(chat): ChatCreationResponseDto
    + {static} toMessageResponse(message): MessageResponseDto
    + {static} buildUserMessage(dto, userId): Message
    + {static} buildAIMessage(chatId, content): Message
  }

  class AuthenticationResponseBuilder {
    + {static} getUserFromSignUpDto(dto, encoder): User
    + {static} getAuthenticationResponseDtoFromUser(user): AuthenticationResponseDto
  }

  class FeedbackBuilder {
    + {static} getFeedbackFromFeedbackRequest(request): Feedback
  }
}

' ============================================================
' RELATIONSHIPS - MODELS
' ============================================================
User "1" -- "0..*" Chat : owns >
VerifyCode --> VerificationCodeType : uses

' ============================================================
' RELATIONSHIPS - REPOSITORIES
' ============================================================
UserRepository --|> JpaRepository
ChatRepository --|> JpaRepository
MessageRepository --|> JpaRepository
VerifyCodeRepository --|> JpaRepository
FeedbackRepository --|> JpaRepository

' ============================================================
' RELATIONSHIPS - SERVICES
' ============================================================
AuthService --> UserRepository
AuthService --> EmailService
AuthService --> JwtUtil
UserService --> UserRepository
UserService --> FeedbackRepository
ChatService --> ChatRepository
ChatService --> MessageRepository
ChatService --> UserRepository
EmailService --> VerifyCodeRepository
EmailService --> UserRepository
OAuthGoogleService --> UserRepository
OAuthGoogleService --> JwtUtil

' ============================================================
' RELATIONSHIPS - CONTROLLERS
' ============================================================
AuthController --> AuthService
AuthController --> OAuthGoogleService
UserController --> UserService
UserController --> JwtFacade
ChatController --> ChatService
ChatController --> JwtFacade

' ============================================================
' RELATIONSHIPS - SECURITY
' ============================================================
JwtFilter --> JwtUtil
JwtFacade --> SecurityContextHolder

' ============================================================
' RELATIONSHIPS - BUILDERS
' ============================================================
ChatService --> ChatBuilder
AuthService --> AuthenticationResponseBuilder
UserService --> FeedbackBuilder

note right of Message
  senderId = 0 means AI
  senderId > 0 means User
end note

note right of Chat
  title is NULL until
  first message is sent
end note

note bottom of ChatService
  generateAIResponse() is a
  placeholder for actual AI
  integration
end note

@enduml
```

---

## 3. Sequence Diagrams

### 3.1 Authentication with 2FA

```plantuml
@startuml
actor User
participant "Client\n(Browser)" as Client
participant AuthController
participant AuthService
participant EmailService
participant VerifyCodeRepository
participant UserRepository
participant JwtUtil
database Database

== Sign In with 2FA Enabled ==

User -> Client: Enter email & password
Client -> AuthController: POST /api/auth/signin\n{email, password}
activate AuthController

AuthController -> AuthService: signIn(signInDto)
activate AuthService

AuthService -> UserRepository: findByEmail(email)
activate UserRepository
UserRepository -> Database: SELECT * FROM users WHERE email=?
Database --> UserRepository: User record
UserRepository --> AuthService: User
deactivate UserRepository

AuthService -> AuthService: passwordEncoder.matches(password, user.password)

alt User not verified
  AuthService -> EmailService: sendVerificationCode(email, VERIFY)
  AuthService --> AuthController: throw VerificationNeededException
  AuthController --> Client: 401 "verification code sent"
else User has 2FA enabled
  AuthService -> EmailService: sendVerificationCode(email, TWO_FACT_AUTH)
  activate EmailService

  EmailService -> EmailService: generateVerificationCode()
  EmailService -> VerifyCodeRepository: deleteByEmailAndType(email, TWO_FACT_AUTH)
  EmailService -> VerifyCodeRepository: save(new VerifyCode)
  activate VerifyCodeRepository
  VerifyCodeRepository -> Database: INSERT INTO verify_codes
  deactivate VerifyCodeRepository

  EmailService -> EmailService: sendEmail(to, subject, context)
  EmailService --> AuthService: code sent
  deactivate EmailService

  AuthService --> AuthController: throw UnauthorizedAccessException\n"two-factor authentication code sent"
  AuthController --> Client: 401 "2FA code sent to email"
end
deactivate AuthService
deactivate AuthController

== User Enters 2FA Code ==

User -> Client: Enter 2FA code
Client -> AuthController: POST /api/auth/2fa/verify\n{email, verificationCode}
activate AuthController

AuthController -> AuthService: checkTwoFactorAndGenerate(email, code)
activate AuthService

AuthService -> EmailService: verifyCode(email, code, TWO_FACT_AUTH)
activate EmailService

EmailService -> VerifyCodeRepository: findTopByEmailAndTypeOrderByExpirationTimeDesc(email, TWO_FACT_AUTH)
activate VerifyCodeRepository
VerifyCodeRepository -> Database: SELECT * FROM verify_codes\nWHERE email=? AND type=?\nORDER BY expiration_time DESC LIMIT 1
Database --> VerifyCodeRepository: VerifyCode record
VerifyCodeRepository --> EmailService: VerifyCode
deactivate VerifyCodeRepository

EmailService -> EmailService: validate code & expiration
EmailService -> UserRepository: findByEmail(email)
UserRepository --> EmailService: User

EmailService -> VerifyCodeRepository: delete(verifyCode)
EmailService --> AuthService: User
deactivate EmailService

AuthService -> AuthenticationResponseBuilder: getAuthenticationResponseDtoFromUser(user)
AuthenticationResponseBuilder --> AuthService: AuthenticationResponseDto

AuthService -> JwtUtil: generateToken(dto)
activate JwtUtil
JwtUtil --> AuthService: JWT token
deactivate JwtUtil

AuthService --> AuthController: token
deactivate AuthService

AuthController --> Client: 200 {token}
deactivate AuthController

Client -> Client: Store token in localStorage
Client --> User: Redirect to dashboard

@enduml
```

---

### 3.2 Feedback Submission (with Auth Check)

```plantuml
@startuml
actor User
participant "Client\n(Browser)" as Client
participant UserController
participant JwtFacade
participant UserService
participant UserRepository
participant FeedbackRepository
database Database

User -> Client: Fill feedback form\n{email, comment}
Client -> UserController: POST /api/users/feedback\nAuthorization: Bearer <token>\n{email, comment}
activate UserController

UserController -> JwtFacade: getAuthenticatedUserEmail()
activate JwtFacade
JwtFacade -> JwtFacade: Get email from SecurityContext
JwtFacade --> UserController: authenticated email
deactivate JwtFacade

note right of UserController
  JwtFilter has already validated
  the token before reaching controller
end note

UserController -> UserService: sendFeedback(feedbackRequest)
activate UserService

UserService -> UserRepository: existsByEmail(feedbackRequest.email)
activate UserRepository
UserRepository -> Database: SELECT EXISTS(SELECT 1 FROM users WHERE email=?)
Database --> UserRepository: true/false
UserRepository --> UserService: exists
deactivate UserRepository

alt Email exists and comment is valid
  UserService -> FeedbackBuilder: getFeedbackFromFeedbackRequest(feedbackRequest)
  FeedbackBuilder --> UserService: Feedback

  UserService -> FeedbackRepository: save(feedback)
  activate FeedbackRepository
  FeedbackRepository -> Database: INSERT INTO feedbacks (email, comment)
  Database --> FeedbackRepository: saved Feedback
  FeedbackRepository --> UserService: Feedback
  deactivate FeedbackRepository

  UserService --> UserController: void
  UserController --> Client: 200 OK
else Invalid feedback
  UserService --> UserController: throw FeedbackNotValidException
  UserController --> Client: 400 "Feedback is not valid"
end

deactivate UserService
deactivate UserController

Client --> User: "Feedback submitted successfully"

@enduml
```

---

### 3.3 Theme Change (No Auth)

```plantuml
@startuml
actor User
participant "Client\n(Browser)" as Client
participant "useTheme\nHook" as Hook
participant "localStorage" as Storage
participant "CSS Variables" as CSS

note over Client, CSS
  Theme switching is entirely client-side.
  No backend API call required.
  No authentication needed.
end note

User -> Client: Click theme toggle button
activate Client

Client -> Hook: switchTheme(newTheme)
activate Hook

Hook -> Storage: localStorage.setItem('theme', newTheme)
activate Storage
Storage --> Hook: stored
deactivate Storage

Hook -> CSS: Apply CSS variables for new theme
activate CSS
CSS -> CSS: Update --color-primary, --color-background, etc.
CSS --> Hook: updated
deactivate CSS

Hook -> Hook: Dispatch 'themeChanged' event
Hook --> Client: Theme updated
deactivate Hook

Client -> Client: Re-render UI with new theme
Client --> User: UI displays new theme
deactivate Client

note right of User
  Available themes:
  - light
  - dark
  - future
end note

@enduml
```

---

### 3.4 User ↔ AI Chat Interaction

```plantuml
@startuml
actor User
participant "Client\n(ChatPage)" as Client
participant ChatController
participant JwtFacade
participant ChatService
participant ChatRepository
participant MessageRepository
participant UserRepository
participant "AI Service\n(External)" as AI
database Database

== Create New Chat (First Time) ==

User -> Client: Click "New Chat"
Client -> ChatController: POST /api/chats\nAuthorization: Bearer <token>
activate ChatController

ChatController -> JwtFacade: getAuthenticatedUserEmail()
JwtFacade --> ChatController: email

ChatController -> ChatService: createChat(email)
activate ChatService

ChatService -> UserRepository: findByEmail(email)
activate UserRepository
UserRepository -> Database: SELECT * FROM users WHERE email=?
UserRepository --> ChatService: User
deactivate UserRepository

ChatService -> ChatBuilder: Build new Chat(user, title=null)
ChatBuilder --> ChatService: Chat

ChatService -> ChatRepository: save(chat)
activate ChatRepository
ChatRepository -> Database: INSERT INTO chats (user_id, title, created_at, updated_at)
ChatRepository --> ChatService: saved Chat
deactivate ChatRepository

ChatService -> ChatBuilder: toChatCreationResponse(chat)
ChatBuilder --> ChatService: ChatCreationResponseDto

ChatService --> ChatController: ChatCreationResponseDto
deactivate ChatService

ChatController --> Client: 201 {chatId}
deactivate ChatController

Client -> Client: Set currentChatId
Client --> User: Empty chat ready

== Send First Message ==

User -> Client: Type message & press send
Client -> Client: Optimistically add user message to UI

Client -> ChatController: POST /api/chats/messages\nAuthorization: Bearer <token>\n{chatId, content}
activate ChatController

ChatController -> JwtFacade: getAuthenticatedUserEmail()
JwtFacade --> ChatController: email

ChatController -> ChatService: sendMessage(email, dto)
activate ChatService

ChatService -> ChatRepository: findById(dto.chatId)
activate ChatRepository
ChatRepository -> Database: SELECT * FROM chats WHERE id=?
ChatRepository --> ChatService: Chat
deactivate ChatRepository

ChatService -> UserRepository: findByEmail(email)
UserRepository --> ChatService: User

ChatService -> ChatService: validateChatOwnership(chat, user)

ChatService -> ChatService: isFirstMessage = (chat.title == null)

ChatService -> ChatBuilder: buildUserMessage(dto, user.id)
ChatBuilder --> ChatService: userMessage

ChatService -> MessageRepository: save(userMessage)
activate MessageRepository
MessageRepository -> Database: INSERT INTO messages\n(chat_id, sender_id, content, timestamp)
MessageRepository --> ChatService: saved userMessage
deactivate MessageRepository

ChatService -> AI: generateAIResponse(userContent)
activate AI
note right of AI
  In production, this would call
  an external AI service (e.g., OpenAI)
  Currently returns placeholder:
  "AI response to: {userContent}"
end note
AI --> ChatService: aiResponseContent
deactivate AI

ChatService -> ChatBuilder: buildAIMessage(chatId, aiResponseContent)
ChatBuilder --> ChatService: aiMessage (senderId=0)

ChatService -> MessageRepository: save(aiMessage)
activate MessageRepository
MessageRepository -> Database: INSERT INTO messages\n(chat_id, sender_id=0, content, timestamp)
MessageRepository --> ChatService: saved aiMessage
deactivate MessageRepository

alt isFirstMessage
  ChatService -> ChatService: generateChatTitle(userMessage.content)
  ChatService -> Chat: setTitle(generatedTitle)
end

ChatService -> Chat: setUpdatedAt(now)
ChatService -> ChatRepository: save(chat)
ChatRepository -> Database: UPDATE chats SET title=?, updated_at=?
ChatRepository --> ChatService: updated Chat

ChatService -> ChatBuilder: toMessageResponse(aiMessage)
ChatBuilder --> ChatService: MessageResponseDto

ChatService --> ChatController: MessageResponseDto
deactivate ChatService

ChatController --> Client: 200 {AI message}
deactivate ChatController

Client -> Client: Append AI message to UI
Client --> User: Display AI response

== Get Chat History ==

User -> Client: Select existing chat from sidebar
Client -> ChatController: GET /api/chats/{chatId}/messages\nAuthorization: Bearer <token>
activate ChatController

ChatController -> JwtFacade: getAuthenticatedUserEmail()
JwtFacade --> ChatController: email

ChatController -> ChatService: getChatMessages(email, chatId)
activate ChatService

ChatService -> ChatRepository: findById(chatId)
ChatRepository --> ChatService: Chat

ChatService -> UserRepository: findByEmail(email)
UserRepository --> ChatService: User

ChatService -> ChatService: validateChatOwnership(chat, user)

ChatService -> MessageRepository: findByChatIdOrderByTimestampAsc(chatId)
activate MessageRepository
MessageRepository -> Database: SELECT * FROM messages\nWHERE chat_id=?\nORDER BY timestamp ASC
MessageRepository --> ChatService: List<Message>
deactivate MessageRepository

ChatService -> ChatBuilder: toMessageResponse(messages)
ChatBuilder --> ChatService: List<MessageResponseDto>

ChatService --> ChatController: List<MessageResponseDto>
deactivate ChatService

ChatController --> Client: 200 [messages]
deactivate ChatController

Client -> Client: Render all messages in order
Client --> User: Display chat history

@enduml
```

---

## Notes

### Authentication Flow
- **Sign Up:** User registers → Email verification code sent → User verifies → JWT token issued
- **Sign In:** User logs in → If 2FA enabled → 2FA code sent → User verifies 2FA → JWT token issued
- **Google OAuth:** User clicks Google login → Redirected to Google → Returns with code → Backend exchanges code for user info → JWT token issued

### Authorization
- **Protected Endpoints:** All `/api/users/*` and `/api/chats/*` endpoints require valid JWT token
- **JwtFilter:** Validates token on every request and populates SecurityContext
- **JwtFacade:** Provides clean abstraction to get authenticated user's email from SecurityContext

### AI Integration
- Currently, `ChatService.generateAIResponse()` returns a placeholder
- In production, this should integrate with external AI service (e.g., OpenAI, Anthropic)
- Message with `senderId = 0` indicates AI response
- Message with `senderId > 0` indicates user message

### Theme Management
- Entirely client-side operation
- No backend API or authentication required
- Stored in browser's localStorage
- Supports: light, dark, future themes

### Data Persistence
- **User:** Stored in `users` table
- **Chat:** Stored in `chats` table with foreign key to user
- **Message:** Stored in `messages` table with chatId reference
- **VerifyCode:** Temporary codes for email verification (VERIFY, TWO_FACT_AUTH, CHANGE_PASSWORD)
- **Feedback:** Stored in `feedbacks` table

---

**End of UML Diagrams Document**
