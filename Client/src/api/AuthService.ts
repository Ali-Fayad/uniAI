import type {
  SignUpDto,
  VerifyDto,
  SignInDto,
  TokenResponse,
} from "../utils/auth";
import { api } from "./api";

interface AuthResponse {
  status: number;
  token?: string;
  message?: string;
}

export const AuthService = {
  /**
   * Sign up a new user
   * Returns 202 if verification is needed
   */
    signUp: async (dto: SignUpDto): Promise<AuthResponse> => {
    try {
      const res = await api.post<TokenResponse>("/auth/signup", dto);
      // TokenResponse does not declare message, so cast to access optional message safely
      const data = res.data as unknown as { token?: string; message?: string };
      return {
        status: res.status,
        token: data.token,
        message: data.message,
      };
    } catch (err: any) {
      if (err.response) {
        return {
          status: err.response.status,
          message: err.response.data?.message || "Signup failed",
        };
      }
      throw err;
    }
  },

  /**
   * Sign in an existing user
   * Returns 200 with token if successful
   * Returns 202 if verification is needed
   */
  signIn: async (dto: SignInDto): Promise<AuthResponse> => {
    try {
      const res = await api.post<TokenResponse>("/auth/signin", dto);
      const data = res.data as unknown as { token?: string; message?: string };
      return {
        status: res.status,
        token: data.token,
        message: data.message,
      };
    } catch (err: any) {
      if (err.response) {
        return {
          status: err.response.status,
          message: err.response.data?.message || "Sign in failed",
        };
      }
      throw err;
    }
  },

  /**
   * Verify email with verification code
   * Returns 200 with token if successful
   */
  verifyCode: async (dto: VerifyDto): Promise<AuthResponse> => {
    try {
      const res = await api.post<TokenResponse>("/auth/verify", dto);
      const data = res.data as unknown as { token?: string; message?: string };
      return {
        status: res.status,
        token: data.token,
        message: data.message,
      };
    } catch (err: any) {
      if (err.response) {
        return {
          status: err.response.status,
          message: err.response.data?.message || "Verification failed",
        };
      }
      throw err;
    }
  },

  /**
   * Resend verification code
   */
  resendCode: async (email: string): Promise<void> => {
    try {
      await api.post("/auth/resend-code", { email });
    } catch (err) {
      console.error("Resend code failed:", err);
      throw err;
    }
  },
};
