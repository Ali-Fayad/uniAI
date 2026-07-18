# Chat and Memory

Chats and messages are persisted through repository adapters. The service uses a bounded recent-history window and V56 stores conversation memory as JSONB on `chats`. `ConversationMemoryManager` validates, budgets, merges, persists, and formats memory. Prompt-visible memory retains logical references and omits persistence identifiers.

The title and memory update operations use the same provider boundary and may degrade safely if an AI operation fails.

