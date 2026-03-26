/**
 * Personal Info Utilities
 *
 * Responsibility:
 * - Provide small, pure helpers used by the Personal Info page feature.
 *
 * Does NOT:
 * - Read React state
 * - Perform API calls
 */

export const createClientId = (): string => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
};

export const normalizeOptionId = (prefix: string, label: string): string =>
  `${prefix}-${label.trim().toLowerCase().replace(/\s+/g, '-')}`;

export const moveItem = <T,>(list: T[], from: number, to: number): T[] => {
  const next = [...list];
  const [item] = next.splice(from, 1);
  next.splice(to, 0, item);
  return next;
};
