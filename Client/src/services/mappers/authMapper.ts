import type { UserData } from '../../types/dto';

type JwtPayload = Record<string, unknown>;

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
    isVerified: Boolean(payload.isVerified),
    isTwoFacAuth,
    twoFactorEnabled: isTwoFacAuth,
    role: typeof payload.role === 'string' ? payload.role : undefined,
    provider: typeof payload.provider === 'string' ? payload.provider : undefined,
  };
};
