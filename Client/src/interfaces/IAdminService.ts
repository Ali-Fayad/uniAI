import type {
  AdminFeedbackResponse,
  AdminOverviewResponse,
  AdminUserDetailsResponse,
  AdminUserFeedbackResponse,
  AdminUserPersonalInfoResponse,
  AdminUserSearchResponse,
  UserRole,
  AdminCatalogItem,
  AdminPromptResponse,
} from '../types/dto';

/**
 * IAdminService
 *
 * Abstraction for admin API operations.
 */
export interface IAdminService {
  getOverview(): Promise<AdminOverviewResponse>;
  searchUsers(email: string): Promise<AdminUserSearchResponse[]>;
  getUserDetails(userId: number): Promise<AdminUserDetailsResponse>;
  getUserPersonalInfo(userId: number): Promise<AdminUserPersonalInfoResponse>;
  getUserFeedback(userId: number): Promise<AdminUserFeedbackResponse[]>;
  updateUserRole(userId: number, role: UserRole): Promise<AdminUserDetailsResponse>;
  deleteUser(userId: number): Promise<void>;
  getFeedback(): Promise<AdminFeedbackResponse[]>;
  deleteFeedback(feedbackId: number): Promise<void>;
  searchSkills(query?: string): Promise<AdminCatalogItem[]>;
  createSkill(name: string, category?: string): Promise<AdminCatalogItem>;
  searchPositions(query?: string): Promise<AdminCatalogItem[]>;
  createPosition(name: string): Promise<AdminCatalogItem>;
  listPrompts(): Promise<AdminPromptResponse[]>;
  getPrompt(key: string): Promise<AdminPromptResponse>;
}
