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
import { FaPencilAlt } from 'react-icons/fa';
import type { BasicFormState } from '../personalInfoTypes';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import AnimatedTextarea from '../../../common/AnimatedTextarea';

export interface BioSectionProps {
  form: BasicFormState;
  setField: (field: keyof BasicFormState, value: string) => void;
}

const BioSection: React.FC<BioSectionProps> = ({ form, setField }) => {
  return (
    <PersonalInfoSectionCard
      title="Bio"
      icon={<FaPencilAlt className="h-5 w-5" aria-hidden="true" />}
      className="rounded-3xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4"
    >
      <AnimatedTextarea
        label="Short summary"
        value={form.summary}
        onChange={(e) => setField('summary', e.target.value)}
        rows={5}
        className="w-full"
      />
    </PersonalInfoSectionCard>
  );
};

export default BioSection;
