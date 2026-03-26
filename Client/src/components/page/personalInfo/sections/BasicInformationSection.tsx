/**
 * BasicInformationSection
 *
 * Responsibility:
 * - Render the basic contact/job fields of the Personal Info form.
 *
 * Does NOT:
 * - Fetch or save personal info
 * - Own navigation logic
 */

import React from 'react';
import { FaUser } from 'react-icons/fa';
import type { BasicFormState } from '../personalInfoTypes';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import AnimatedInput from '../../../common/AnimatedInput';

export interface BasicInformationSectionProps {
  form: BasicFormState;
  setField: (field: keyof BasicFormState, value: string) => void;
}

const BasicInformationSection: React.FC<BasicInformationSectionProps> = ({ form, setField }) => {
  return (
    <PersonalInfoSectionCard
      title="Basic Information"
      icon={<FaUser className="h-5 w-5" aria-hidden="true" />}
      className="rounded-3xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4"
    >
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <AnimatedInput
          value={form.phone}
          onChange={(e) => setField('phone', e.target.value)}
          label="Phone"
        />
        <AnimatedInput
          value={form.address}
          onChange={(e) => setField('address', e.target.value)}
          label="Address"
        />
        <AnimatedInput
          value={form.jobTitle}
          onChange={(e) => setField('jobTitle', e.target.value)}
          label="Job title"
        />
        <AnimatedInput
          value={form.company}
          onChange={(e) => setField('company', e.target.value)}
          label="Company"
        />
      </div>
    </PersonalInfoSectionCard>
  );
};

export default BasicInformationSection;
