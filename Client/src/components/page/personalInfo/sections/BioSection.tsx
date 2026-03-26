/**
 * BioSection
 *
 * Responsibility:
 * - Render the Personal Info summary textarea.
 *
 * Does NOT:
 * - Save content
 * - Perform API calls
 */

import React from 'react';
import type { BasicFormState } from '../personalInfoTypes';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';

export interface BioSectionProps {
  form: BasicFormState;
  setField: (field: keyof BasicFormState, value: string) => void;
}

const BioSection: React.FC<BioSectionProps> = ({ form, setField }) => {
  return (
    <PersonalInfoSectionCard
      title="Bio"
      className="rounded-3xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4"
    >
      <textarea
        value={form.summary}
        onChange={(e) => setField('summary', e.target.value)}
        placeholder="Short summary"
        rows={5}
        className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
      />
    </PersonalInfoSectionCard>
  );
};

export default BioSection;
