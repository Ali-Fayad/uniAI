import type {
  AuthenticationResponseDto,
  UpdateUserDto,
  ChangePasswordDto,
  DeleteAccountDto,
  FeedbackRequest,
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
  deleteAccount(data: DeleteAccountDto): Promise<void>;
  changePassword(data: ChangePasswordDto): Promise<void>;
  sendFeedback(data: FeedbackRequest): Promise<void>;
}
