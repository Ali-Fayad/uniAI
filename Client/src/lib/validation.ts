export const EMAIL_MAX_LENGTH = 100;
export const USERNAME_PATTERN = /^[A-Za-z0-9_]+$/;

export const isValidEmail = (value: string): boolean => {
  const email = value.trim().toLowerCase();
  return email.length <= EMAIL_MAX_LENGTH && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

export const isValidUsername = (value: string): boolean => {
  const username = value.trim();
  return username.length >= 2 && username.length <= 50 && USERNAME_PATTERN.test(username);
};

export const isStrongPassword = (value: string): boolean => value.length >= 8
  && value.length <= 100
  && !/\s/.test(value)
  && /[a-z]/.test(value)
  && /[A-Z]/.test(value)
  && /\d/.test(value)
  && /[^A-Za-z0-9]/.test(value);

export const isHttpUrl = (value: string): boolean => {
  if (!value.trim()) return true;
  try {
    const url = new URL(value.trim());
    return (url.protocol === 'http:' || url.protocol === 'https:') && value.length <= 2048;
  } catch {
    return false;
  }
};

export const isValidDateRange = (start: string, end?: string): boolean => {
  if (!start || !end) return true;
  const startDate = new Date(`${start}T00:00:00`);
  const endDate = new Date(`${end}T00:00:00`);
  return !Number.isNaN(startDate.valueOf()) && !Number.isNaN(endDate.valueOf()) && endDate >= startDate;
};
