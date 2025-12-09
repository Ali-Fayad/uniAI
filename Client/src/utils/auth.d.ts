export interface SignInDto {
  email: string;
  password: string;
}

export interface SignUpDto {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface TokenResponse {
  token: string;
}

export interface VerifyDto {
	  email: string;
	  verificationCode: string;
}

export interface AuthenticationResponseDto {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  isVerified: boolean;
  isTwoFacAuth: boolean;
}
