import type {
  SignUpDto,
  SignInDto,
  VerifyDto,
  RequestPasswordDto,
  GoogleAuthUrlRequestDto,
  TokenResponse,
  MessageResponse,
  UrlResponse,
} from '../types/dto';

/**
 * IAuthService
 *
 * Abstraction for all authentication-related API operations.
 * Following the Interface Segregation Principle, this interface exposes
 * only authentication concerns, keeping it focused and replaceable.
 */
export interface IAuthService {
  signUp(data: SignUpDto): Promise<TokenResponse>;
  signIn(data: SignInDto): Promise<TokenResponse>;
  verify(data: VerifyDto): Promise<TokenResponse>;
  verify2FA(data: VerifyDto): Promise<TokenResponse>;
  forgotPassword(email: string): Promise<MessageResponse>;
  forgotPasswordConfirm(data: RequestPasswordDto): Promise<TokenResponse>;
  getGoogleAuthUrl(data?: GoogleAuthUrlRequestDto): Promise<UrlResponse>;
}
