/**
 * PersonalInfoSettingsSection
 *
 * Responsibility:
 * - Render the Settings section entry-point to Personal Info.
 *
 * Does NOT:
 * - Perform navigation directly (delegates click handler)
 */

import React from 'react';
import SettingsSection from '../../settings/SettingsSection';

export interface PersonalInfoSettingsSectionProps {
  onUpdatePersonalInfo: () => void;
}

const PersonalInfoSettingsSection: React.FC<PersonalInfoSettingsSectionProps> = ({
  onUpdatePersonalInfo,
}) => {
  return (
    <SettingsSection title="Personal Information" icon="badge">
      <p className="text-sm text-[var(--color-textSecondary)] mb-6">
        Your personal information helps build your CV
      </p>
      <div className="flex justify-end">
        <button
          type="button"
          onClick={onUpdatePersonalInfo}
          className="inline-flex items-center justify-center rounded-md bg-[var(--color-primary)] px-4 py-2 text-sm font-medium text-[var(--color-background)] hover:opacity-90"
        >
          Update Personal Info
        </button>
      </div>
    </SettingsSection>
  );
};

export default PersonalInfoSettingsSection;
