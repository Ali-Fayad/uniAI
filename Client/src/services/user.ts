import apiClient from './api';
import { ENDPOINTS } from '../constants';
import type {
  AuthenticationResponseDto,
  UpdateUserDto,
  ChangePasswordDto,
  DeleteAccountDto,
  FeedbackRequest,
} from '../types/dto';
import type { IUserService } from '../interfaces';

/**
 * User service for user-related API calls.
 * Implements IUserService so callers can depend on the abstraction (DIP).
 */

export const userService: IUserService = {
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
  async sendFeedback(data: FeedbackRequest): Promise<void> {
    await apiClient.post(ENDPOINTS.USER.FEEDBACK, data);
  },

  /**
   * Check whether current user already has personal info
   */
  async hasPersonalInfo(): Promise<boolean> {
    const response = await apiClient.get<{
      hasPersonalInfo?: boolean;
      phone?: string | null;
      address?: string | null;
      linkedin?: string | null;
      github?: string | null;
      portfolio?: string | null;
      summary?: string | null;
      jobTitle?: string | null;
      company?: string | null;
    }>(
      ENDPOINTS.CV.PERSONAL_INFO
    );

    const info = response.data ?? {};
    if (typeof info.hasPersonalInfo === 'boolean') {
      return info.hasPersonalInfo;
    }

    const fields = [
      info.phone,
      info.address,
      info.linkedin,
      info.github,
      info.portfolio,
      info.summary,
      info.jobTitle,
      info.company,
    ];

    return fields.some((value) => typeof value === 'string' && value.trim().length > 0);
  },
};
