# Feedback Endpoints

| Method/path | Auth | Request | Response | Entry point |
|---|---|---|---|---|
| POST `/api/feedback` | authenticated | `SubmitFeedbackCommand` | 200 empty body | `SubmitFeedbackUseCase.submitFeedback` |

Source: `FeedbackController`. The authenticated user ID is resolved server-side.

