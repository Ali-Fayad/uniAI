/**
 * SocialWebSection
 *
 * Responsibility:
 * - Render Social & Web URL fields for the Personal Info form.
 *
 * Does NOT:
 * - Validate URLs
 * - Fetch or save personal info
 */

import React from 'react';
import type { BasicFormState } from '../personalInfoTypes';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';

export interface SocialWebSectionProps {
  form: BasicFormState;
  setField: (field: keyof BasicFormState, value: string) => void;
}

const SocialWebSection: React.FC<SocialWebSectionProps> = ({ form, setField }) => {
  return (
    <PersonalInfoSectionCard
      title="Social & Web"
      className="rounded-3xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4"
    >
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <input
          value={form.linkedin}
          onChange={(e) => setField('linkedin', e.target.value)}
          placeholder="LinkedIn URL"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <input
          value={form.github}
          onChange={(e) => setField('github', e.target.value)}
          placeholder="GitHub URL"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <input
          value={form.portfolio}
          onChange={(e) => setField('portfolio', e.target.value)}
          placeholder="Portfolio URL"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
      </div>
    </PersonalInfoSectionCard>
  );
};

export default SocialWebSection;
