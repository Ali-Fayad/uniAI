import type { UserData } from '../types/dto';

/**
 * Decode a JWT token payload without verifying the signature
 * (Signature verification should be done on the backend)
 */
export function decodeJwt(token: string): Record<string, unknown> | null {
  try {
    // JWT format: header.payload.signature
    const parts = token.split('.');
    if (parts.length !== 3) {
      console.error('Invalid JWT format');
      return null;
    }

    // Decode the payload (second part)
    const payload = parts[1];
    const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    
    return JSON.parse(decodedPayload);
  } catch (error) {
    console.error('Failed to decode JWT:', error);
    return null;
  }
}

/**
 * Extract user information from JWT token
 */
export function extractUserFromToken(token: string): UserData | null {
  const payload = decodeJwt(token);
  if (!payload) return null;

  try {
    // Extract user data from JWT payload
    // Adjust field names based on your backend's JWT structure
    return {
      id: payload.id as number || payload.userId as number,
      firstName: payload.firstName as string || '',
      lastName: payload.lastName as string || '',
      email: payload.email as string || payload.sub as string || '',
      role: payload.role as string,
      twoFactorEnabled: payload.twoFactorEnabled as boolean,
      provider: payload.provider as string,
    };
  } catch (error) {
    console.error('Failed to extract user from token:', error);
    return null;
  }
}

/**
 * Check if token is expired
 */
export function isTokenExpired(token: string): boolean {
  const payload = decodeJwt(token);
  if (!payload || !payload.exp) return true;

  const expirationTime = (payload.exp as number) * 1000; // Convert to milliseconds
  return Date.now() >= expirationTime;
}

/**
 * Get token expiration date
 */
export function getTokenExpiration(token: string): Date | null {
  const payload = decodeJwt(token);
  if (!payload || !payload.exp) return null;

  return new Date((payload.exp as number) * 1000);
}
