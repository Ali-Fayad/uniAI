import type {
  ChatCreationResponseDto,
  SendMessageDto,
  MessageResponseDto,
  Chat,
  MessageResponse,
} from '../types/dto';

/**
 * IChatService
 *
 * Abstraction for all chat-related API operations.
 * Following ISP, only chat concerns are exposed here.
 */
export interface IChatService {
  createChat(): Promise<ChatCreationResponseDto>;
  sendMessage(data: SendMessageDto): Promise<MessageResponseDto>;
  getChats(): Promise<Chat[]>;
  getChatMessages(chatId: number): Promise<MessageResponseDto[]>;
  deleteChat(chatId: number): Promise<MessageResponse>;
  deleteAllChats(): Promise<MessageResponse>;
}
