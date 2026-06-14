import apiClient from './api';
import { ENDPOINTS } from '../constants';
import type {
  AdminFeedbackResponse,
  AdminOverviewResponse,
  AdminUserDetailsResponse,
  AdminUserFeedbackResponse,
  AdminUserPersonalInfoResponse,
  AdminUserSearchResponse,
  UserRole,
} from '../types/dto';
import type { IAdminService } from '../interfaces';

/**
 * Admin service for admin-only API calls.
 * Keeps the service layer thin and delegates error handling to shared interceptors.
 */
export const adminService: IAdminService = {
  async getOverview(): Promise<AdminOverviewResponse> {
    const response = await apiClient.get<AdminOverviewResponse>(ENDPOINTS.ADMIN.OVERVIEW);
    return response.data;
  },

  async searchUsers(email: string): Promise<AdminUserSearchResponse[]> {
    const response = await apiClient.get<AdminUserSearchResponse[]>(
      ENDPOINTS.ADMIN.USERS.SEARCH,
      {
        params: { email },
      }
    );
    return response.data;
  },

  async getUserDetails(userId: number): Promise<AdminUserDetailsResponse> {
    const response = await apiClient.get<AdminUserDetailsResponse>(
      ENDPOINTS.ADMIN.USERS.BY_ID(userId)
    );
    return response.data;
  },

  async getUserPersonalInfo(userId: number): Promise<AdminUserPersonalInfoResponse> {
    const response = await apiClient.get<AdminUserPersonalInfoResponse>(
      ENDPOINTS.ADMIN.USERS.PERSONAL_INFO(userId)
    );
    return response.data;
  },

  async getUserFeedback(userId: number): Promise<AdminUserFeedbackResponse[]> {
    const response = await apiClient.get<AdminUserFeedbackResponse[]>(
      ENDPOINTS.ADMIN.USERS.FEEDBACK(userId)
    );
    return response.data;
  },

  async updateUserRole(userId: number, role: UserRole): Promise<AdminUserDetailsResponse> {
    const response = await apiClient.patch<AdminUserDetailsResponse>(
      ENDPOINTS.ADMIN.USERS.ROLE(userId),
      { role }
    );
    return response.data;
  },

  async deleteUser(userId: number): Promise<void> {
    await apiClient.delete(ENDPOINTS.ADMIN.USERS.BY_ID(userId));
  },

  async getFeedback(): Promise<AdminFeedbackResponse[]> {
    const response = await apiClient.get<AdminFeedbackResponse[]>(ENDPOINTS.ADMIN.FEEDBACK.LIST);
    return response.data;
  },

  async deleteFeedback(feedbackId: number): Promise<void> {
    await apiClient.delete(ENDPOINTS.ADMIN.FEEDBACK.BY_ID(feedbackId));
  },
};
