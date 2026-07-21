import type {
  AuthenticationResponseDto,
  UpdateUserDto,
  ChangePasswordDto,
  DeleteAccountDto,
  FeedbackRequest,
  CheckUsernameResponse,
} from '../types/dto';

/**
 * IUserService
 *
 * Abstraction for user profile API operations.
 * Isolated from chat and auth concerns following ISP.
 */
export interface IUserService {
  getMe(): Promise<AuthenticationResponseDto>;
  updateMe(data: UpdateUserDto): Promise<AuthenticationResponseDto>;
  checkUsernameAvailability(username: string): Promise<CheckUsernameResponse>;
  deleteAccount(data: DeleteAccountDto): Promise<void>;
  changePassword(data: ChangePasswordDto): Promise<void>;
  sendFeedback(data: FeedbackRequest): Promise<void>;
  hasPersonalInfo(): Promise<boolean>;
}
