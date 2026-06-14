  # Admin Dashboard TODO

  ## Goal

  Build a secure admin system for uniAI that allows administrators to monitor platform usage, manage users, review feedback, and view application analytics.

  The implementation should follow the existing project architecture:

  - Backend: Spring Boot + DDD + Hexagonal
  - Frontend: React + TypeScript
  - Authentication: JWT
  - Authorization: Role-based (USER / ADMIN)

  The admin dashboard should remain simple, operational, and maintainable.

  ---

  # ADMIN-DASHBOARD-002
  ## Role Foundation

  ### Goal
  Introduce user roles into the system.

  ### Scope
  - Create UserRole enum:
    - USER
    - ADMIN
  - Add role column to users table
  - Add role field to User domain model
  - Default role = USER
  - Backfill existing users

  ### Deliverables
  - Flyway migration
  - User entity update
  - User builder update

  ### Notes
  No JWT changes.
  No admin APIs.
  No frontend changes.

  ---

  # ADMIN-DASHBOARD-003
  ## First Admin Bootstrap

  ### Goal
  Automatically create the first administrator.

  ### Rules
  - First registered account becomes ADMIN
  - Every later account becomes USER

  ### Scope
  - Update signup flow
  - Add user count check
  - Keep logic inside application service

  ### Notes
  No JWT changes.
  No security changes.

  ---

  # ADMIN-DASHBOARD-004
  ## JWT Role Support

  ### Goal
  Expose user role through authentication tokens.

  ### Scope
  - Add role claim to JWT
  - Add role to JwtTokenPayload
  - Parse role from token
  - Support old tokens when possible

  ### Deliverables
  - JWT generation update
  - JWT parsing update

  ### Notes
  No route protection yet.

  ---

  # ADMIN-DASHBOARD-005
  ## Admin Authorization

  ### Goal
  Protect admin endpoints.

  ### Scope
  - Convert role claim into Spring Security authorities
  - Introduce:
    - ROLE_USER
    - ROLE_ADMIN
  - Protect:
    - /api/admin/**

  ### Deliverables
  - SecurityConfig update
  - Authority mapping update

  ### Validation
  - USER receives 403
  - ADMIN receives 200

  ---

  # ADMIN-DASHBOARD-006
  ## Frontend Role Awareness

  ### Goal
  Allow frontend to understand roles.

  ### Scope
  - Decode role from JWT
  - Store role in AuthContext
  - Create AdminRoute or requiredRole support

  ### Deliverables
  - AuthContext update
  - Route guard update

  ### Validation
  - Non-admin cannot access admin pages

  ---

# ADMIN-DASHBOARD-007
## Admin Overview Backend API

### Status
Implemented.

### Endpoint
GET /api/admin/overview

### Response
- totalUsers
- totalChats
- totalMessages
- totalFeedback
- averageChatsPerUser
- averageMessagesPerChat
- averageMessagesPerUser

### Notes
Read-only API.
Frontend can map these fixed fields into flexible stat cards.
Do not refactor this unless statistics become dynamic later.

### Commit
feat(admin): add overview statistics endpoint

---

# ADMIN-DASHBOARD-008
## Admin User Lookup Backend API

### Goal
Allow admins to search for a user by email and load selected user details on demand.

### Endpoints

GET /api/admin/users/search?email=

GET /api/admin/users/{userId}

GET /api/admin/users/{userId}/personal-info

GET /api/admin/users/{userId}/feedback

### Search Behavior
- Search by email only
- Prefer partial email search for UX
- Return lightweight results only

### Search Response
- id
- email
- username
- firstName
- lastName
- role

### User Details Response
- id
- username
- firstName
- lastName
- email
- role
- isVerified
- isTwoFacAuth
- chatCount
- messageCount
- averageMessagesPerChat
- cvCount

### Personal Info Response
- Load only when admin opens the Personal Info tab
- Reuse or map existing personal-info DTO safely

### Feedback Response
- Load only when admin opens the Feedback tab
- Return feedback submitted by the selected user

### Notes
Read-only API.
Use lightweight DTOs and lazy-loaded detail endpoints.
Do not use Flyweight.

---

# ADMIN-DASHBOARD-009
## Admin Role Management API

### Goal
Allow admins to promote/demote users from the selected user dialog.

### Endpoint
PATCH /api/admin/users/{userId}/role

### Rules
- ADMIN only
- Prevent removing last admin
- Prevent accidental self-demotion

### Request
{
  "role": "ADMIN"
}

or

{
  "role": "USER"
}

### Notes
Used from the selected user dialog actions.

---

# ADMIN-DASHBOARD-010
## Admin User Deletion API

### Goal
Allow admins to delete a selected user from the user dialog.

### Endpoint
DELETE /api/admin/users/{userId}

### Rules
- ADMIN only
- Prevent self-delete
- Prevent deleting last admin
- Confirm cleanup behavior before implementation

### Investigation Required
Check cleanup behavior for:
- personal_info
- cvs
- cv sections
- chats
- messages
- verification codes
- feedback

### Notes
This task should be investigated carefully.
Use GPT-5.5 if needed.

---

# ADMIN-DASHBOARD-011
## Admin Feedback Backend API

### Goal
Support feedback viewing in both:
- global admin feedback view later
- selected user dialog feedback tab

### Endpoints

GET /api/admin/feedback

GET /api/admin/users/{userId}/feedback

DELETE /api/admin/feedback/{feedbackId}

### Response
- id
- userId
- email if available
- rating
- content/comment
- createdDate

### Notes
The per-user feedback endpoint is needed for the selected user dialog.
The global feedback endpoint can be used later for moderation.

---

# ADMIN-DASHBOARD-012
## Admin Service Layer Frontend

### Goal
Create frontend API integration for the admin dashboard.

### Scope
Create:
- adminService.ts

Add DTOs:
- AdminOverviewResponse
- AdminStatCardResponse
- AdminUserSearchResult
- AdminUserDetailsResponse
- AdminUserPersonalInfoResponse
- AdminUserFeedbackResponse
- AdminFeedbackResponse

Add API functions:
- getOverview()
- searchUsersByEmail(email)
- getUserDetails(userId)
- getUserPersonalInfo(userId)
- getUserFeedback(userId)
- updateUserRole(userId, role)
- deleteUser(userId)
- getFeedback()
- deleteFeedback(feedbackId)

### Notes
No UI yet.

---

# ADMIN-DASHBOARD-013
## Admin Dashboard Page Shell

### Goal
Create the main admin dashboard page structure.

### Route
/admin

### Main Tabs
1. Statistics
2. User Search

### Layout
- Keep page as composition layer only
- Logic belongs in controller hooks
- Follow existing uniAI theme

### Notes
Do not implement all user actions here.
This task creates structure only.

---

# ADMIN-DASHBOARD-014
## Admin Statistics Tab UI

### Goal
Render dashboard statistics in the main Statistics tab.

### Features
- Stat cards
- Loading state
- Empty state
- Error state
- Flexible rendering based on statistic card definitions

### Notes
Design should allow adding new statistics later without rewriting the whole UI.

---

# ADMIN-DASHBOARD-015
## Admin User Search UI

### Goal
Allow admins to search users by email and select a user.

### Features
- Email search input
- Search button
- Search results list
- Empty state
- Loading state
- Open selected user dialog

### Notes
Do not show all users by default.
Search-first flow only.

---

# ADMIN-DASHBOARD-016
## Selected User Dialog UI

### Goal
Show selected user details in a dialog with internal tabs.

### Dialog Tabs
1. Statistics
2. Personal Info
3. Feedback

### Statistics Tab
- username
- firstName
- lastName
- email
- role
- verified status
- 2FA status
- chatCount
- messageCount
- averageMessagesPerChat
- cvCount

### Personal Info Tab
- Load only when tab is opened
- Show profile/personal-info fields
- Loading/error/empty states

### Feedback Tab
- Load only when tab is opened
- Show user feedback list
- Allow feedback delete if already supported

### Dialog Actions
- Promote/Demote user
- Delete user
- Close

### Notes
Do not load personal info and feedback until needed.
Keep logic in hooks, not page components.

---

# ADMIN-DASHBOARD-017
## Hardening & Validation

### Backend Validation
- First user becomes ADMIN
- Later users become USER
- JWT contains role
- USER receives 403 on admin endpoints
- ADMIN can access admin endpoints
- Last admin protection works
- User deletion cleanup works
- User lookup by email works
- Selected user details stats are correct
- Personal info loads only from admin endpoint
- User feedback loads correctly

### Frontend Validation
- Admin route protection works
- Non-admin users redirected safely
- Admin button visible only for ADMIN
- Statistics tab loads correctly
- User email search works
- Selected user dialog opens correctly
- Dialog tabs lazy-load data correctly
- 403 does not destroy valid sessions
- Empty states render correctly
- Loading/error states handled

### Cleanup
- Remove dead code
- Remove duplicate logic
- Verify imports
- Run frontend build
- Run backend build

# RESEND-Veri
## Frontend Role Awareness

### Goal
Allow frontend to understand roles.

### Scope
- Decode role from JWT
- Store role in AuthContext
- Create AdminRoute or requiredRole support

### Deliverables
- AuthContext update
- Route guard update

### Validation
- Non-admin cannot access admin pages

---

# Future Enhancements Not V1

## Optional
- Admin action audit log
- Announcement system
- User suspension
- Chat inspection tools
- Catalog management
- University management
- Analytics charts
- Email campaign tools
- Support tooling
- AI-powered admin insights