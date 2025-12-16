// ============================================================
// Authentication DTOs
// ============================================================

export interface SignUpDto {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface SignInDto {
  email: string;
  password: string;
}

export interface VerifyDto {
  email: string;
  code: string;
}

export interface RequestPasswordDto {
  email: string;
  code: string;
  newPassword: string;
}

export interface EmailRequestDto {
  email: string;
}

export interface GoogleAuthUrlRequestDto {
  redirectUrl?: string;
}

// ============================================================
// Response DTOs
// ============================================================

export interface TokenResponse {
  token: string;
  user: UserData;
}

export interface AuthenticationResponseDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  twoFactorEnabled: boolean;
  provider: string;
  createdAt: string;
  updatedAt: string;
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
