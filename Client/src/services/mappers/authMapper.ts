import type { UserData, UserRole } from '../../types/dto';

type JwtPayload = Record<string, unknown>;

const isUserRole = (value: unknown): value is UserRole => value === 'USER' || value === 'ADMIN';

/**
 * Maps the backend JWT payload into the frontend auth user model.
 */
export const mapJwtPayloadToUserData = (payload: JwtPayload): UserData => {
  const isTwoFacAuth = Boolean(payload.isTwoFacAuth ?? payload.twoFactorEnabled);

  return {
    id: typeof payload.id === 'number' ? payload.id : typeof payload.userId === 'number' ? payload.userId : undefined,
    firstName: String(payload.firstName ?? ''),
    lastName: String(payload.lastName ?? ''),
    email: String(payload.email ?? payload.sub ?? ''),
    username: String(payload.username ?? payload.preferred_username ?? ''),
    isVerified: payload.isVerified !== false,
    isTwoFacAuth,
    twoFactorEnabled: isTwoFacAuth,
    role: isUserRole(payload.role) ? payload.role : 'USER',
    provider: typeof payload.provider === 'string' ? payload.provider : undefined,
  };
};
