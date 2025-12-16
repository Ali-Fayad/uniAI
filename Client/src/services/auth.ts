import apiClient from './api';
import { ENDPOINTS } from '../constants';
import type {
  SignUpDto,
  SignInDto,
  VerifyDto,
  RequestPasswordDto,
  EmailRequestDto,
  GoogleAuthUrlRequestDto,
  TokenResponse,
  MessageResponse,
  UrlResponse,
} from '../types/dto';

/**
 * Authentication service for all auth-related API calls
 */

export const authService = {
  /**
   * Sign up a new user
   */
  async signUp(data: SignUpDto): Promise<TokenResponse> {
    const response = await apiClient.post<TokenResponse>(
      ENDPOINTS.AUTH.SIGNUP,
      data
    );
    return response.data;
  },

  /**
   * Sign in an existing user
   */
  async signIn(data: SignInDto): Promise<TokenResponse> {
    const response = await apiClient.post<TokenResponse>(
      ENDPOINTS.AUTH.SIGNIN,
      data
    );
    return response.data;
  },

  /**
   * Verify email with code
   */
  async verify(data: VerifyDto): Promise<TokenResponse> {
    const response = await apiClient.post<TokenResponse>(
      ENDPOINTS.AUTH.VERIFY,
      data
    );
    return response.data;
  },

  /**
   * Verify 2FA code
   */
  async verify2FA(data: VerifyDto): Promise<TokenResponse> {
    const response = await apiClient.post<TokenResponse>(
      ENDPOINTS.AUTH.VERIFY_2FA,
      data
    );
    return response.data;
  },

  /**
   * Request password reset email
   */
  async forgotPassword(email: string): Promise<MessageResponse> {
    const data: EmailRequestDto = { email };
    const response = await apiClient.post<MessageResponse>(
      ENDPOINTS.AUTH.FORGOT_PASSWORD,
      data
    );
    return response.data;
  },

  /**
   * Confirm password reset with code and new password
   */
  async forgotPasswordConfirm(
    data: RequestPasswordDto
  ): Promise<TokenResponse> {
    const response = await apiClient.post<TokenResponse>(
      ENDPOINTS.AUTH.FORGOT_PASSWORD_CONFIRM,
      data
    );
    return response.data;
  },

  /**
   * Get Google OAuth authorization URL
   */
  async getGoogleAuthUrl(
    data?: GoogleAuthUrlRequestDto
  ): Promise<UrlResponse> {
    const response = await apiClient.post<UrlResponse>(
      ENDPOINTS.AUTH.GOOGLE_URL,
      data || {}
    );
    return response.data;
  },
};
