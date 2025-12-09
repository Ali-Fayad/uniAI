import type {
  SignUpDto,
  VerifyDto,
  AuthenticationResponseDto,
  SignInDto,
} from "../utils/auth";
import { api } from "./api"; // base axios instance or fetch wrapper

export const AuthService = {
  async signUp(dto: SignUpDto): Promise<void> {
    try {
      await api.post("/auth/signup", dto);
    } catch (err) {
      console.error("Signup failed:", err);
      throw err;
    }
  },

  signIn: async (data: SignInDto) => {
    const res = await api.post("/auth/signin", data);
    return {
      status: res.status,
      body: res.data,
    };
  },

  async verifyCode(dto: VerifyDto): Promise<void> {
    try {
      await api.post("/auth/verify", dto);
    } catch (err) {
      console.error("Verification failed:", err);
      throw err;
    }
  },

  async resendCode(email: string): Promise<void> {
    try {
      await api.post("/auth/resend-code", { email });
    } catch (err) {
      console.error("Resend code failed:", err);
      throw err;
    }
  },
};
