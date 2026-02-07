import apiClient from './api';
import { ENDPOINTS } from '../constants';
import type {
  ChatCreationResponseDto,
  SendMessageDto,
  MessageResponseDto,
  Chat,
  MessageResponse,
} from '../types/dto';

/**
 * Chat service for chat-related API calls
 */

export const chatService = {
  /**
   * Create a new chat
   */
  async createChat(): Promise<ChatCreationResponseDto> {
    const response = await apiClient.post<ChatCreationResponseDto>(
      ENDPOINTS.CHAT.CREATE
    );
    return response.data;
  },

  /**
   * Send a message in a chat
   */
  async sendMessage(data: SendMessageDto): Promise<MessageResponseDto> {
    const response = await apiClient.post<MessageResponseDto>(
      ENDPOINTS.CHAT.SEND_MESSAGE,
      data
    );
    return response.data;
  },

  /**
   * Get all user chats
   */
  async getChats(): Promise<Chat[]> {
    const response = await apiClient.get<Chat[]>(ENDPOINTS.CHAT.GET_ALL);
    return response.data;
  },

  /**
   * Get all messages from a specific chat
   */
  async getChatMessages(chatId: number): Promise<MessageResponseDto[]> {
    const response = await apiClient.get<MessageResponseDto[]>(
      ENDPOINTS.CHAT.GET_MESSAGES(chatId)
    );
    return response.data;
  },

  /**
   * Delete a specific chat
   */
  async deleteChat(chatId: number): Promise<MessageResponse> {
    const response = await apiClient.delete<MessageResponse>(
      ENDPOINTS.CHAT.DELETE(chatId)
    );
    return response.data;
  },

  /**
   * Delete all user chats
   */
  async deleteAllChats(): Promise<MessageResponse> {
    const response = await apiClient.delete<MessageResponse>(
      ENDPOINTS.CHAT.DELETE_ALL
    );
    return response.data;
  },
};
