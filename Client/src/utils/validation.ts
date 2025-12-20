// Frontend validation and formatting utilities
// References: validation_rules.MD (UniAI Input Validation Rules)

export const trim = (s?: string) => (s || '').trim();

export const capitalizeName = (s?: string) => {
  const t = trim(s);
  if (!t) return '';
  return t.charAt(0).toUpperCase() + t.slice(1).toLowerCase();
};

export const toLower = (s?: string) => (s || '').trim().toLowerCase();

export const isAlphaName = (s?: string) => {
  const t = trim(s);
  return /^[A-Za-z]{2,}$/.test(t);
};

export const isValidUsername = (s?: string) => {
  const t = trim(s);
  return /^[A-Za-z0-9_]{2,}$/.test(t);
};

export const isValidEmail = (s?: string) => {
  const t = trim(s).toLowerCase();
  // simple but robust email regex
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(t);
};

// Frontend will hash password before sending; ensure it's a SHA-256 hex string
export const isValidFrontendPasswordHash = (s?: string) => {
  if (!s) return false;
  return /^[a-fA-F0-9]{64}$/.test(s);
};

export const isValidRawPassword = (s?: string) => {
  if (!s) return false;
  // At least 8 chars, one uppercase, one number
  return /^(?=.*[A-Z])(?=.*\d).{8,}$/.test(s);
};

export const formatSignUpPayload = (payload: {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
}) => ({
  username: toLower(payload.username),
  firstName: capitalizeName(payload.firstName),
  lastName: capitalizeName(payload.lastName),
  email: toLower(payload.email),
});

export default {
  trim,
  capitalizeName,
  toLower,
  isAlphaName,
  isValidUsername,
  isValidEmail,
  isValidFrontendPasswordHash,
  isValidRawPassword,
  formatSignUpPayload,
};
