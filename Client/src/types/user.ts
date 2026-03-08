// ============================================================
// User DTOs
// ============================================================

export interface UserData {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  role?: string;
  twoFactorEnabled?: boolean;
  provider?: string;
}

export interface UpdateUserDto {
  username?: string;
  firstName?: string;
  lastName?: string;
  enableTwoFactor?: boolean;
}

export interface FeedbackRequest {
  email: string;
  comment: string;
}

export interface ChangePasswordDto {
  currentPassword: string;
  newPassword: string;
}

export interface DeleteAccountDto {
  password: string;
}
