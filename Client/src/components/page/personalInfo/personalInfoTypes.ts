/**
 * personalInfoTypes
 *
 * Responsibility:
 * - Define shared Personal Info page types used across controller and sections.
 *
 * Does NOT:
 * - Contain business logic or side effects
 */

export type BasicFormState = {
  phone: string;
  address: string;
  jobTitle: string;
  company: string;
  linkedin: string;
  github: string;
  portfolio: string;
  summary: string;
};
