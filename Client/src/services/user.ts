import apiClient from './api';
import { ENDPOINTS } from '../constants';
import type {
  AuthenticationResponseDto,
  UpdateUserDto,
  ChangePasswordDto,
  DeleteAccountDto,
} from '../types/dto';

/**
 * User service for user-related API calls
 */

export const userService = {
  /**
   * Get current user profile
   */
  async getMe(): Promise<AuthenticationResponseDto> {
    const response = await apiClient.get<AuthenticationResponseDto>(
      ENDPOINTS.USER.ME
    );
    return response.data;
  },

  /**
   * Update current user profile
   */
  async updateMe(data: UpdateUserDto): Promise<AuthenticationResponseDto> {
    const response = await apiClient.put<AuthenticationResponseDto>(
      ENDPOINTS.USER.UPDATE_ME,
      data
    );
    return response.data;
  },

  /**
   * Delete current user account
   */
  async deleteAccount(data: DeleteAccountDto): Promise<void> {
    await apiClient.delete(ENDPOINTS.USER.DELETE_ME, {
      data,
    });
  },

  /**
   * Change user password
   */
  async changePassword(data: ChangePasswordDto): Promise<void> {
    await apiClient.post(ENDPOINTS.USER.CHANGE_PASSWORD, data);
  },

  /**
   * Send feedback
   */
  async sendFeedback(data: { email: string; comment: string }): Promise<void> {
    await apiClient.post(ENDPOINTS.USER.FEEDBACK, data);
  },
};
