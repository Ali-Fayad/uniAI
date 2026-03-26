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
        <input
          value={form.phone}
          onChange={(e) => setField('phone', e.target.value)}
          placeholder="Phone"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <input
          value={form.address}
          onChange={(e) => setField('address', e.target.value)}
          placeholder="Address"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <input
          value={form.jobTitle}
          onChange={(e) => setField('jobTitle', e.target.value)}
          placeholder="Job title"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <input
          value={form.company}
          onChange={(e) => setField('company', e.target.value)}
          placeholder="Company"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
      </div>
    </PersonalInfoSectionCard>
  );
};

export default BasicInformationSection;
