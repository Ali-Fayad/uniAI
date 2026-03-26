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
import { FaLink } from 'react-icons/fa';
import type { BasicFormState } from '../personalInfoTypes';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import AnimatedInput from '../../../common/AnimatedInput';

export interface SocialWebSectionProps {
  form: BasicFormState;
  setField: (field: keyof BasicFormState, value: string) => void;
}

const SocialWebSection: React.FC<SocialWebSectionProps> = ({ form, setField }) => {
  return (
    <PersonalInfoSectionCard
      title="Social & Web"
      icon={<FaLink className="h-5 w-5" aria-hidden="true" />}
      className="rounded-3xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4"
    >
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <AnimatedInput
          value={form.linkedin}
          onChange={(e) => setField('linkedin', e.target.value)}
          label="LinkedIn URL"
        />
        <AnimatedInput
          value={form.github}
          onChange={(e) => setField('github', e.target.value)}
          label="GitHub URL"
        />
        <AnimatedInput
          value={form.portfolio}
          onChange={(e) => setField('portfolio', e.target.value)}
          label="Portfolio URL"
        />
      </div>
    </PersonalInfoSectionCard>
  );
};

export default SocialWebSection;
