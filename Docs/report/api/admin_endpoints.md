# Admin Endpoints

All routes below require ADMIN through `SecurityConfig`.

| Method/path | Purpose |
|---|---|
| GET `/api/admin/health` | admin health message |
| GET `/api/admin/overview` | dashboard overview |
| GET `/api/admin/users/search` | search users by optional email |
| GET `/api/admin/users/{userId}` | user details |
| GET `/api/admin/users/{userId}/personal-info` | user profile |
| GET `/api/admin/users/{userId}/feedback` | user feedback |
| GET `/api/admin/feedback` | feedback list |
| DELETE `/api/admin/users/{userId}` | delete user |
| DELETE `/api/admin/feedback/{feedbackId}` | delete feedback |
| PATCH `/api/admin/users/{userId}/role` | role update |

Source: `AdminController`, `AdminApplicationService`.
