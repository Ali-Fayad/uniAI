// ============================================================
// Authentication DTOs
// ============================================================

export interface SignUpDto {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  password: string;
}

export interface SignInDto {
  email: string;
  password: string;
}

export interface VerifyDto {
  email: string;
  verificationCode: string; // server expects verificationCode
}

export interface RequestPasswordDto {
  email: string;
  verificationCode: string;
  newPassword: string;
}

export interface EmailRequestDto {
  email: string;
}

export interface GoogleAuthUrlRequestDto {
  redirectUri?: string;
  state?: string;
}

// ============================================================
// Auth Response DTOs
// ============================================================

export interface TokenResponse {
  token: string; // server returns only { token }
}

export interface AuthenticationResponseDto {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  isVerified: boolean;
  isTwoFacAuth: boolean;
}

export interface MessageResponse {
  message: string;
}

export interface UrlResponse {
  url: string;
}
