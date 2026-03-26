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
import { FaLink, FaGlobe, FaLinkedin } from 'react-icons/fa';
import { SiGithub } from 'react-icons/si';
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
      className="rounded-3xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-6"
    >
      <div className="grid grid-cols-1 gap-6">
        {/* Use theme token so icon color adapts consistently across all active themes */}
        <div className="flex items-start gap-3">
          <FaLinkedin className="mt-4 text-[var(--color-textPrimary)] text-xl shrink-0" aria-hidden="true" title="LinkedIn" />
          <div className="flex-1">
            <AnimatedInput
              value={form.linkedin}
              onChange={(e) => setField('linkedin', e.target.value)}
              label="LinkedIn URL"
              placeholder="https://linkedin.com/in/username"
            />
          </div>
        </div>

        {/* GitHub - Brand Icon (Themed) */}
        <div className="flex items-start gap-3">
          <SiGithub className="mt-4 text-[var(--color-textPrimary)] text-xl shrink-0" aria-hidden="true" title="GitHub" />
          <div className="flex-1">
            <AnimatedInput
              value={form.github}
              onChange={(e) => setField('github', e.target.value)}
              label="GitHub URL"
              placeholder="https://github.com/username"
            />
          </div>
        </div>

        {/* Portfolio - Generic Icon */}
        <div className="flex items-start gap-3">
          <FaGlobe className="mt-4 text-gray-400 text-xl shrink-0" aria-hidden="true" />
          <div className="flex-1">
            <AnimatedInput
              value={form.portfolio}
              onChange={(e) => setField('portfolio', e.target.value)}
              label="Portfolio URL"
              placeholder="https://myportfolio.com"
            />
          </div>
        </div>
      </div>
    </PersonalInfoSectionCard>
  );
};

export default SocialWebSection;
