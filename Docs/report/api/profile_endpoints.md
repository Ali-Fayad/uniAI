# Profile Endpoints

| Method/path | Auth | Response | Evidence |
|---|---|---|---|
| GET `/api/users/me` | authenticated | current user | `UserController` |
| PUT `/api/users/me` | authenticated | updated user | `UserController` |
| DELETE `/api/users/me` | authenticated | account deletion response | `UserController` |
| POST `/api/users/change-password` | authenticated | change result | `UserController` |
| GET `/api/cv/personal-info` | authenticated | `PersonalInfoResponse` | `PersonalInfoController` |
| PUT `/api/cv/personal-info` | authenticated | `PersonalInfoResponse` | `PersonalInfoController` |
| GET `/api/cv/personal-info/status` | authenticated | filled/missing fields map | `PersonalInfoController` |

