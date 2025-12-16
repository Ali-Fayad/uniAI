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
// Response DTOs
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

// ============================================================
// User DTOs
// ============================================================

export interface UserData {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role?: string;
  twoFactorEnabled?: boolean;
  provider?: string;
}

export interface UpdateUserDto {
  firstName?: string;
  lastName?: string;
  email?: string;
  twoFactorEnabled?: boolean;
}

export interface ChangePasswordDto {
  currentPassword: string;
  newPassword: string;
}

export interface DeleteAccountDto {
  password: string;
}

// ============================================================
// Chat DTOs
// ============================================================

export interface ChatCreationResponseDto {
  id: number;
  userId: number;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface SendMessageDto {
  chatId: number;
  message: string;
}

export interface MessageResponseDto {
  id: number;
  chatId: number;
  senderId: number; // 0 indicates AI responses
  message: string;
  createdAt: string;
}

export interface Chat {
  id: number;
  userId: number;
  title: string;
  createdAt: string;
  updatedAt: string;
}
