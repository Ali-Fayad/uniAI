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

### Goal
Provide dashboard statistics.

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

---

# ADMIN-DASHBOARD-008
## Admin Users Backend API

### Goal
Allow admins to view platform users.

### Endpoint
GET /api/admin/users

### Features
- Pagination if needed
- Search by:
  - username
  - email
  - name

### Response
- id
- username
- firstName
- lastName
- email
- role
- isVerified
- isTwoFacAuth

### Notes
Read-only API.

---

# ADMIN-DASHBOARD-009
## Admin Role Management API

### Goal
Allow admins to promote/demote users.

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

---

# ADMIN-DASHBOARD-010
## Admin User Deletion API

### Goal
Allow admins to remove users.

### Endpoint
DELETE /api/admin/users/{userId}

### Rules
- ADMIN only
- Prevent self-delete
- Prevent deleting last admin

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
Feedback currently appears to be the highest risk area for orphaned records.

---

# ADMIN-DASHBOARD-011
## Admin Feedback Backend API

### Goal
Allow admins to review feedback.

### Endpoints

GET /api/admin/feedback

DELETE /api/admin/feedback/{feedbackId}

### Response
- id
- userId
- email
- rating
- content/comment
- createdDate

---

# ADMIN-DASHBOARD-012
## Admin Service Layer (Frontend)

### Goal
Create frontend integration layer.

### Scope
Create:
- adminService.ts

Add DTOs:
- AdminOverviewResponse
- AdminUserResponse
- AdminFeedbackResponse
- AdminChatAnalyticsResponse

### Notes
No UI yet.

---

# ADMIN-DASHBOARD-013
## Admin Dashboard Page

### Goal
Create admin landing page.

### Route
/admin

### Sections
- Overview cards
- Quick stats
- Recent activity

### Layout
Follow existing uniAI theme and page architecture.

### Notes
Page = composition only.
Logic belongs in controller hooks.

---

# ADMIN-DASHBOARD-014
## Admin Users UI

### Goal
Manage users visually.

### Features
- User table
- Search
- Role badge
- Promote/Demote
- Delete user

### Notes
Reuse existing table and card patterns when possible.

---

# ADMIN-DASHBOARD-015
## Admin Feedback UI

### Goal
Manage platform feedback.

### Features
- Feedback table
- Rating display
- User/email display
- Delete feedback

### Notes
Simple moderation interface.

---

# ADMIN-DASHBOARD-016
## Admin Analytics UI

### Goal
Visualize platform activity.

### Metrics
- Average chats per user
- Average messages per chat
- Average messages per user
- Most active users
- Empty chats count

### Notes
Start with cards/tables.
Charts are optional.

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

### Frontend Validation
- Admin route protection works
- Non-admin users redirected safely
- 403 does not destroy valid sessions
- Empty states render correctly
- Loading/error states handled

### Cleanup
- Remove dead code
- Remove duplicate logic
- Verify imports
- Run frontend build
- Run backend build

---

# Future Enhancements (Not V1)

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