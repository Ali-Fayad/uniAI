// ============================================================
// Chat DTOs
// ============================================================

import type { UserData } from './user';

export interface ChatCreationResponseDto {
  chatId: number;
}

export interface SendMessageDto {
  chatId: number;
  content: string;
}

export interface MessageResponseDto {
  messageId: number;
  chatId: number;
  senderId: number; // 0 = AI, user ID = user
  content: string;
  timestamp: string;
}

export interface Chat {
  id: number;
  user: UserData;
  title: string | null;
  createdAt: string;
  updatedAt: string;
}
