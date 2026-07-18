# Chat Endpoints

Source: `ChatController`; all require authentication.

| Method/path | Request/response | Service entry |
|---|---|---|
| POST `/api/chats` | `ChatCreationResponseDto`, 201 | `createChat` |
| POST `/api/chats/messages` | `SendMessageCommand` → `MessageResponseDto` | `sendMessage` |
| GET `/api/chats` | chat summaries | `getUserChats` |
| GET `/api/chats/{chatId}/messages` | ordered messages | `getChatMessages` |
| DELETE `/api/chats/{chatId}` | message | `deleteChat` |
| DELETE `/api/chats` | message | `deleteAllChats` |

Graduate retrieval is internal to `sendMessage`; it is not exposed as a separate public API.

